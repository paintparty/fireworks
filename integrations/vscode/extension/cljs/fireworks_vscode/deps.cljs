
(ns fireworks-vscode.deps
  "Pure reading and additive editing of a project deps.edn for live coding. Same boundary
   discipline as the other fireworks-vscode namespaces: no VS Code, no Calva, no file I/O —
   text in, data (or new text) out. Reading: alias names + whether an alias's classpath carries
   test-refresh/Fireworks + what its :main-opts do. Editing (all additive, prompt-then-write on
   the TS side, mirroring the Leiningen project.clj flow): add a :live-code alias, or patch an
   existing alias's :extra-deps / :main-opts / :extra-paths with what's missing. On unparseable
   input a reader returns nil and an editor returns {:error :unparseable}, so TS can abort cleanly."
  (:require [rewrite-clj.zip :as z]))

(defn- alias->str
  "An alias key as it appears after the `-M:` (colon dropped, namespace kept):
   :test -> \"test\", :my/alias -> \"my/alias\"."
  [k]
  (if (keyword? k) (subs (str k) 1) (str k)))

(defn alias-names
  "The alias names defined under :aliases in deps.edn `text`, in file order, as a vector
   of strings without the leading colon (e.g. [\"test\" \"test-refresh\"]). Returns []
   when there is no :aliases map, and nil when the text won't parse."
  [text]
  (try
    (if-let [aliases (z/get (z/of-string text) :aliases)]
      (->> (z/sexpr aliases) keys (mapv alias->str))
      [])
    (catch :default _ nil)))

;; The artifact names (the `name` segment of the coordinate symbol) the launch classpath must
;; carry. Matched by name so any group / mvn|git|local form / version all count: e.g.
;; com.jakemccrary/test-refresh and io.github.paintparty/fireworks.
(def ^:private test-refresh-artifact "test-refresh")
(def ^:private fireworks-artifact "fireworks")

(defn- coord-artifacts
  "The set of artifact names among a deps-map's coordinate symbols (non-symbol keys ignored)."
  [deps-map]
  (into #{} (comp (filter symbol?) (map name)) (keys deps-map)))

;; The main ns test-refresh runs under (`clojure -M:<alias>` must invoke it via :main-opts).
(def ^:private test-refresh-main "com.jakemccrary.test-refresh")

;; Defaults injected when the extension writes an alias. Kept in step with examples/deps-project.
(def ^:private test-refresh-sym 'com.jakemccrary/test-refresh)
(def ^:private test-refresh-version "0.26.0")
(def ^:private fireworks-sym 'io.github.paintparty/fireworks)
(def ^:private fireworks-version "0.21.0")
(def ^:private main-opts-vec ["-m" test-refresh-main])
(def ^:private extra-paths-vec ["test"])

(defn- alias-classpath-deps
  "The dep maps that feed a `-M:<alias>` classpath, merged: project :deps + the alias's
   :extra-deps / :replace-deps / :override-deps / :deps."
  [data alias-map]
  (merge (:deps data)
         (:extra-deps alias-map)
         (:replace-deps alias-map)
         (:override-deps alias-map)
         (:deps alias-map)))

(defn- main-opts-kind
  "What an alias's :main-opts do, as a string: \"none\" (absent/empty), \"test-refresh\" (runs the
   test-refresh main), or \"other\" (some different -m/main — a conflict for auto-patching)."
  [alias-map]
  (let [mo (:main-opts alias-map)]
    (cond
      (empty? mo)                             "none"
      (some #{test-refresh-main} mo)          "test-refresh"
      :else                                   "other")))

(defn alias-deps-status
  "Whether `clojure -M:<alias>` will have test-refresh and Fireworks on its classpath (project
   :deps merged with the alias's :extra-deps / :replace-deps / :override-deps / :deps; matched by
   artifact name so version and mvn/git/local form don't matter), and what the alias's :main-opts
   do. Returns {:has-test-refresh bool :has-fireworks bool :main-opts \"none|test-refresh|other\"},
   or nil when the text won't parse (TS surfaces + aborts, like the other readers)."
  [text alias]
  (try
    (let [data      (z/sexpr (z/of-string text))
          alias-map (get-in data [:aliases (keyword alias)])
          artifacts (coord-artifacts (alias-classpath-deps data alias-map))]
      {:has-test-refresh (contains? artifacts test-refresh-artifact)
       :has-fireworks    (contains? artifacts fireworks-artifact)
       :main-opts        (main-opts-kind alias-map)})
    (catch :default _ nil)))

;; --- additive editors (prompt-then-write on the TS side) ------------------

(defn- unique-alias-name
  "\"live-code\" unless that alias already exists, else \"fireworks-live-code\"."
  [existing-names]
  (if (contains? existing-names "live-code") "fireworks-live-code" "live-code"))

(defn add-live-code-alias
  "Add a self-contained live-coding alias to deps.edn `text`: {:extra-paths [\"test\"] :extra-deps
   {test-refresh … fireworks …} :main-opts [\"-m\" …]}. Creates the :aliases map if absent. Names it
   :live-code, or :fireworks-live-code if :live-code is taken. Returns {:text new-text :alias name
   :changed true}, or {:error :unparseable}."
  [text]
  (try
    (let [root       (z/of-string text)
          az         (z/get root :aliases)
          existing   (if az (set (map alias->str (keys (z/sexpr az)))) #{})
          name       (unique-alias-name existing)
          body       {:extra-paths extra-paths-vec
                      :extra-deps  {test-refresh-sym {:mvn/version test-refresh-version}
                                    fireworks-sym    {:mvn/version fireworks-version}}
                      :main-opts   main-opts-vec}
          root'      (if az
                       (z/assoc az (keyword name) body)
                       (z/assoc root :aliases {(keyword name) body}))]
      {:text (z/root-string root') :alias name :changed true})
    (catch :default _ {:error :unparseable})))

(defn patch-alias
  "Additively fix an existing alias in deps.edn `text` so `-M:<alias>` runs test-refresh + Fireworks:
   add whichever of test-refresh / Fireworks is missing from the classpath into the alias's
   :extra-deps, add :main-opts if absent, add :extra-paths [\"test\"] if absent. Never touches an
   existing value (a pinned version, a user's :main-opts). Returns {:text new-text :changed bool
   :added [strings]}, or {:error :unparseable} (also when the alias isn't found)."
  [text alias]
  (try
    (let [data      (z/sexpr (z/of-string text))
          akw       (keyword alias)
          alias-map (get-in data [:aliases akw])]
      (if (nil? alias-map)
        {:error :unparseable}
        (let [artifacts  (coord-artifacts (alias-classpath-deps data alias-map))
              add-deps   (cond-> {}
                           (not (contains? artifacts test-refresh-artifact))
                           (assoc test-refresh-sym {:mvn/version test-refresh-version})
                           (not (contains? artifacts fireworks-artifact))
                           (assoc fireworks-sym {:mvn/version fireworks-version}))
              add-main?  (empty? (:main-opts alias-map))
              add-paths? (nil? (:extra-paths alias-map))
              added      (cond-> []
                           (contains? add-deps test-refresh-sym) (conj (str test-refresh-sym))
                           (contains? add-deps fireworks-sym)    (conj (str fireworks-sym))
                           add-main?                             (conj ":main-opts")
                           add-paths?                            (conj ":extra-paths"))
              az         (z/get (z/of-string text) :aliases)
              amap       (z/get az akw)
              amap       (if (seq add-deps)
                           (z/assoc amap :extra-deps (merge (:extra-deps alias-map) add-deps))
                           amap)
              amap       (if add-main? (z/assoc amap :main-opts main-opts-vec) amap)
              amap       (if add-paths? (z/assoc amap :extra-paths extra-paths-vec) amap)]
          {:text (z/root-string amap) :changed (boolean (seq added)) :added added})))
    (catch :default _ {:error :unparseable})))
