
(ns fireworks-vscode.deps
  "Pure reading and additive editing of a project deps.edn for live coding. Same boundary
   discipline as the other fireworks-vscode namespaces: no VS Code, no Calva, no file I/O —
   text in, data (or new text) out. Reading: alias names + whether an alias's classpath carries
   test-refresh/Fireworks + what its :main-opts do. Editing (all additive, prompt-then-write on
   the TS side, mirroring the Leiningen project.clj flow): add a fresh :live-code alias — Fireworks
   into the top-level :deps, test-refresh into the alias's :extra-deps — never touching an existing
   alias. On unparseable input a reader returns nil and the editor returns {:error :unparseable},
   so TS can abort cleanly."
  (:require [rewrite-clj.zip :as z]
            [cljfmt.core :as cljfmt]
            [clojure.string :as str]))

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
(def ^:private fireworks-version "0.21.1")

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

;; The alias body as a multi-line source string (test-refresh in :extra-deps; Fireworks is a project
;; dep). Written with newlines between entries so the appended alias isn't a single flat line — the
;; final cljfmt pass then aligns the indentation. Kept in step with examples/deps-project.
(def ^:private alias-body-str
  (str "{:extra-paths [\"test\"]\n"
       ":extra-deps {" test-refresh-sym " {:mvn/version \"" test-refresh-version "\"}}\n"
       ":main-opts [\"-m\" \"" test-refresh-main "\"]}"))

(defn- append-nl
  "Append key `k` -> `val-form` (a Clojure form or a rewrite-clj node) to the end of the map at
   zipper `map-zip`, inserting a newline before the new key (and, when `body-nl?`, between the key and
   its value so the value starts on its own line). Indentation is left rough on purpose — the caller's
   cljfmt pass cleans it up. Returns a zipper (root reachable via z/root-string)."
  [map-zip k val-form body-nl?]
  (let [mz   (z/assoc map-zip k val-form)
        mz   (if body-nl? (-> mz (z/get k) z/insert-newline-left z/up) mz)
        keyz (-> mz (z/get k) z/left)]
    (z/insert-newline-left keyz)))

(defn- reformat
  "cljfmt the whole deps.edn `text` to canonical indentation (comments + blank lines preserved).
   Defensive: on any cljfmt failure return the text unchanged so a launch is never blocked."
  [text]
  (try (cljfmt/reformat-string text) (catch :default _ text)))

(defn- fireworks-present?
  "Does the top-level :deps of parsed deps.edn `text` already carry the fireworks artifact?"
  [text]
  (let [dz (z/get (z/of-string text) :deps)]
    (boolean (and dz (contains? (coord-artifacts (z/sexpr dz)) fireworks-artifact)))))

(defn- append-fireworks-plain
  "Add the Fireworks coordinate to the top-level :deps of deps.edn `text` (appended to the map on its
   own line, or a :deps map created when absent) and cljfmt — no elide comment. Assumes Fireworks is
   not already present (callers check). Returns new text."
  [text]
  (let [root (z/of-string text)
        dz   (z/get root :deps)]
    (reformat
     (if (nil? dz)
       (z/root-string (append-nl root :deps {fireworks-sym {:mvn/version fireworks-version}} false))
       (z/root-string (append-nl dz fireworks-sym {:mvn/version fireworks-version} false))))))

(defn ensure-fireworks
  "Ensure the top-level :deps of deps.edn `text` carries the Fireworks coordinate (plain — no
   comment). Matched by artifact name, so any group / version / mvn|git|local form counts. Returns
   {:text new-text :changed bool} — :changed false (text unchanged) when already present — or
   {:error :unparseable}. Standalone path: patch an eligible alias's project deps before launch."
  [text]
  (try
    (if (fireworks-present? text)
      {:text text :changed false}
      {:text (append-fireworks-plain text) :changed true})
    (catch :default _ {:error :unparseable})))

;; The live-code alias documentation block, placed between the :aliases keyword and its map value.
;; `%A%` is the actual alias name (live-code, or fireworks-live-code on collision). Each non-blank
;; line is indented two spaces at emit time. When an alias is added this block carries the elide
;; guidance, so the Fireworks coordinate in :deps is left plain (no comment).
(def ^:private alias-comment-template
  [";; Example :%A% alias, which is added by the Fireworks VSCode extension."
   ""
   ";; The extension command `Fireworks: Live Code`, with `Integrated Terminal`"
   ";; selected will run: `clojure -M:%A%`."
   ""
   ";; Or, in your preferred external terminal: `clojure -M:%A%`"
   ""
   ";; The `test-refresh` lib watches `src/` + `test/`, reloads on save,"
   ";; re-runs top-level `fireworks.core/?` forms) and (optionally) runs tests."
   ""
   ";; Scoped to this alias so the dev tool stays out of the project's normal"
   ";; dependency set."
   ""
   ";; Elide Fireworks for non-dev builds -> {:jvm-opts [\"-Dfireworks.elide=true\"]}"
   ";; Separate AOT/uberjar build should also elide it this way."])

(defn- alias-comment-block
  "The comment block as emit-ready lines (2-space indent, blank lines kept blank, `%A%` -> `alias`)."
  [alias]
  (mapv #(if (= % "") "" (str "  " (str/replace % "%A%" alias))) alias-comment-template))

(defn- force-aliases-newline
  "Ensure the :aliases keyword and its map value are on separate lines (idempotent — a no-op when
   they already are), so cljfmt indents the map under a `{` at column 2 and insert-alias-comment-block
   can splice the block after a standalone `:aliases` line. Compares the key/value rows."
  [text]
  (let [root (z/of-string text {:track-position? true})
        valz (z/get root :aliases)]
    (if (nil? valz)
      text
      (let [krow (first (z/position (z/left valz)))
            vrow (first (z/position valz))]
        (if (> vrow krow) text (z/root-string (z/insert-newline-left valz)))))))

(defn- insert-alias-comment-block
  "Splice the alias comment block into cljfmt'd deps.edn `text`, right after the `:aliases` line (i.e.
   between the :aliases keyword and its `{` map value). Line-based: cljfmt won't re-indent comments,
   so we place them ourselves after the final cljfmt pass. Relies on force-aliases-newline having put
   :aliases on its own line. No-op if that line isn't found."
  [text alias]
  (let [lines (vec (str/split text #"\n" -1))
        idx   (first (keep-indexed (fn [i l] (when (re-find #":aliases\s*$" l) i)) lines))]
    (if (nil? idx)
      text
      (str/join "\n" (concat (subvec lines 0 (inc idx))
                             (alias-comment-block alias)
                             (subvec lines (inc idx)))))))

(defn add-live-code-alias
  "Add a live-coding alias to deps.edn `text`, the canonical wiring from examples/deps-project:
   Fireworks into the top-level :deps (plain, no comment — the alias block below carries the elide
   guidance) and a new alias {:extra-paths [\"test\"] :extra-deps {test-refresh …} :main-opts [\"-m\" …]}
   into :aliases (test-refresh lives in the alias, Fireworks is a project dep), documented by a
   comment block placed between the :aliases keyword and its map. Creates the :aliases map if absent,
   appends when present — existing aliases are never touched. cljfmt aligns the code; comments are
   placed after. Names it :live-code, or :fireworks-live-code if :live-code is taken.
   Returns {:text new-text :alias name :changed true}, or {:error :unparseable}."
  [text]
  (try
    (let [text'    (if (fireworks-present? text) text (append-fireworks-plain text))
          root     (z/of-string text')
          az       (z/get root :aliases)
          existing (if az (set (map alias->str (keys (z/sexpr az)))) #{})
          name     (unique-alias-name existing)
          kw       (keyword name)
          body     (z/node (z/of-string alias-body-str))
          root'    (if az
                     (append-nl az kw body true)
                     (append-nl root :aliases
                                (z/node (z/of-string (str "{" kw "\n" alias-body-str "}")))
                                false))
          text2    (reformat (force-aliases-newline (z/root-string root')))]
      {:text (insert-alias-comment-block text2 name) :alias name :changed true})
    (catch :default _ {:error :unparseable})))
