
(ns fireworks-vscode.lein
  "Pure reading/editing of a Leiningen project.clj (and ~/.lein/profiles.clj) for live
   coding. Same boundary discipline as fireworks-vscode.deps / .config: text in, data or
   new text out; no VS Code, no Calva, no file I/O. The TS side owns reads, the modal
   prompt, and the actual writes.

   A project is Live-Code-eligible when its defproject has a :profiles entry with at least
   one profile (any name) whose :plugins vector contains exactly
   [com.jakemccrary/lein-test-refresh \"0.26.0\"]. The watcher then runs
   `lein with-profile +<profile> test-refresh`. The top-level defproject :test-refresh map
   is ensured against fireworks-vscode.config/baseline (the same option set the deps flow
   seeds into .test-refresh.edn).

   Note: defproject is a flat kv LIST `(defproject name version :k v …)`, not a map — the
   body helpers walk/edit the pairs after name + version. On unparseable input every public
   fn returns {:error :unparseable} rather than throwing, so TS can abort cleanly."
  (:require [cljfmt.core :as cljfmt]
            [clojure.string :as str]
            [fireworks-vscode.config :as config]
            [fireworks-vscode.versions :as v]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [rewrite-clj.zip :as z]))

(defn- reformat
  "cljfmt the whole project.clj `text` to canonical indentation (comments + blank lines preserved,
   and non-indenting spaces — e.g. the aligned :test-refresh values — left alone). Defensive: on any
   cljfmt failure return the text unchanged so a launch is never blocked."
  [text]
  (try (cljfmt/reformat-string text) (catch :default _ text)))

(def ^:private plugin-sym (symbol v/lein-test-refresh-sym))
(def ^:private plugin-version v/lein-test-refresh-version)
;; The exact plugin coordinate the project must carry to be eligible.
(def ^:private coordinate [plugin-sym plugin-version])

(defn- plugin-match?
  "True when v is the exact eligible coordinate [com.jakemccrary/lein-test-refresh \"0.26.0\"]."
  [v]
  (and (vector? v) (= (first v) plugin-sym) (= (second v) plugin-version)))

(defn- profile-has-plugin?
  "True when a profile value (a map) carries the exact coordinate in its :plugins vector."
  [profile-val]
  (boolean (and (map? profile-val) (some plugin-match? (:plugins profile-val)))))

(defn- profile-key->str
  "A profile key as the user picks/runs it (colon dropped, namespace kept):
   :dev -> \"dev\", :my/runner -> \"my/runner\"."
  [k]
  (if (keyword? k) (subs (str k) 1) (str k)))

;; --- defproject (flat kv list) navigation ---------------------------------

(defn- body-kv-start
  "Zipper at the first key in the defproject body — i.e. after `defproject`, the name, and
   the version. nil when the defproject has no body (name + version only)."
  [zdefproject]
  (some-> zdefproject z/down z/right z/right z/right))

(defn- find-key
  "Walk the defproject body kv pairs from `start` (a key zloc); return the VALUE zloc for
   key `k`, or nil. Stays robust to whitespace/comments (z/right skips them)."
  [start k]
  (loop [kz start]
    (when kz
      (if (= (z/sexpr kz) k)
        (z/right kz)
        (recur (some-> kz z/right z/right))))))

;; --- public surface -------------------------------------------------------

(defn profiles
  "Read the profile names from project.clj `text`. Returns
   {:all [\"dev\" \"test\"] :eligible [\"dev\"]} — names in :profiles order, :eligible being
   those whose :plugins carries the exact coordinate. {:all [] :eligible []} when there is
   no :profiles entry; {:error :unparseable} when the text won't parse."
  [text]
  (try
    (let [pz (find-key (body-kv-start (z/of-string text)) :profiles)]
      (if (nil? pz)
        {:all [] :eligible []}
        (let [pmap (z/sexpr pz)]
          {:all      (mapv profile-key->str (keys pmap))
           :eligible (->> pmap
                          (filter (fn [[_ v]] (profile-has-plugin? v)))
                          (mapv (comp profile-key->str key)))})))
    (catch :default _ {:error :unparseable})))

;; --- Fireworks dependency detection ---------------------------------------
;; The artifact name Live Code needs on the classpath so namespaces that call `?` reload.
;; Matched by name, so group / version / mvn|git form all count (io.github.paintparty/fireworks).
(def ^:private fireworks-artifact "fireworks")

;; The coordinate the extension appends to the top-level :dependencies when a project's classpath
;; lacks Fireworks. Kept in step with examples/leiningen-project (a git-style coord, which lein
;; supports); matched on launch by artifact name, so any group/version already present is left alone.
(def ^:private fireworks-sym (symbol v/fireworks-sym))
(def ^:private fireworks-version v/fireworks-version)
(def ^:private fireworks-coordinate [fireworks-sym fireworks-version])

(defn- deps-vec-has-fireworks?
  "True when a Leiningen :dependencies vector carries a coordinate whose artifact name is
   \"fireworks\" (e.g. [io.github.paintparty/fireworks \"0.21.0\"])."
  [deps]
  (boolean (and (sequential? deps)
                (some (fn [d]
                        (and (vector? d)
                             (symbol? (first d))
                             (= (name (first d)) fireworks-artifact)))
                      deps))))

(defn fireworks-dep-status
  "Whether project.clj `text` carries a Fireworks dependency — in the top-level :dependencies or
   any profile's :dependencies (either merges onto the `lein … test-refresh` classpath). Returns
   {:has-fireworks bool}, or {:error :unparseable} when the text won't parse."
  [text]
  (try
    (let [start     (body-kv-start (z/of-string text))
          top       (some-> (find-key start :dependencies) z/sexpr)
          profs     (some-> (find-key start :profiles) z/sexpr)
          prof-deps (when (map? profs) (keep #(when (map? %) (:dependencies %)) (vals profs)))]
      {:has-fireworks (boolean (or (deps-vec-has-fireworks? top)
                                   (some deps-vec-has-fireworks? prof-deps)))})
    (catch :default _ {:error :unparseable})))

(defn- append-to-vector
  "Append form `x` to the vector zloc `zvec`, on its own line aligned under the vector's existing
   first child (or via z/append-child when the vector is empty). `zvec` must come from a
   position-tracked zipper so the child column can be read. Returns the vector zloc."
  [zvec x]
  (if-let [first-child (z/down zvec)]
    (let [col (second (z/position first-child))]
      (-> zvec z/down z/rightmost
          (z/insert-right* (n/coerce x))
          (z/insert-right* (n/spaces (dec col)))
          (z/insert-right* (n/newlines 1))
          z/up))
    (z/append-child zvec x)))

(defn ensure-fireworks
  "Ensure project.clj `text` carries a Fireworks dependency. When it's absent everywhere (the
   top-level and every profile :dependencies), append the coordinate to the top-level :dependencies
   vector — aligned under the existing entries — creating that vector when the project has none.
   Matched by artifact name, so any group/version already present counts. Returns
   {:text new-text :changed bool} — :changed false (text unchanged) when already present — or
   {:error :unparseable}. Mirrors fireworks-vscode.deps/ensure-fireworks on the Clojure CLI side."
  [text]
  (try
    (if (:has-fireworks (fireworks-dep-status text))
      {:text text :changed false}
      (let [depsz (find-key (body-kv-start (z/of-string text {:track-position? true})) :dependencies)]
        {:text    (if depsz
                    (z/root-string (append-to-vector depsz fireworks-coordinate))
                    ;; No :dependencies at all — splice one at the end of the defproject body.
                    (let [block    (str "\n  :dependencies [" (pr-str fireworks-coordinate) "]")
                          children (n/children (p/parse-string-all block))
                          end      (-> (z/of-string text) z/down z/rightmost)]
                      (z/root-string (reduce z/insert-right* end (reverse children)))))
         :changed true}))
    (catch :default _ {:error :unparseable})))

;; --- :live-code profile (additive, picker-consented on the TS side) -------

(def ^:private live-code-profile-block
  "The commented :profiles block add-live-code-profile splices when a project.clj has no :profiles.
   Mirrors examples/leiningen-project: a dedicated :live-code profile carrying exactly the
   lein-test-refresh plugin (kept out of the default build), documented by the comment header.
   Parsed and inserted verbatim so the comments/alignment survive. The map value mirrors `coordinate`."
  "
  ;; The VSCode extension command `Fireworks: Live Code` runs:
  ;; `lein with-profile +live-code test-refresh`.
  ;; The plugin lives in a profile (not top-level :plugins) so the extension
  ;; detects it as an eligible profile and it stays out of the default build.
  :profiles {:live-code {:plugins [[com.jakemccrary/lein-test-refresh \"0.26.0\"]]}}")

(defn add-live-code-profile
  "Add a fresh live-coding profile to project.clj `text`, mirroring fireworks-vscode.deps/
   add-live-code-alias on the Clojure CLI side: a :live-code profile whose :plugins carries exactly
   the eligible coordinate — never rewriting a profile the user already has. When the project has no
   :profiles, splice the commented block (always :live-code); when :profiles exists, assoc a fresh
   profile (:live-code, or :fireworks-live-code when :live-code is taken). Returns
   {:text new-text :profile name :changed true} or {:error :unparseable}."
  [text]
  (try
    (let [pz (find-key (body-kv-start (z/of-string text)) :profiles)]
      (if (nil? pz)
        {:text    (let [children (n/children (p/parse-string-all live-code-profile-block))
                        end      (-> (z/of-string text) z/down z/rightmost)]
                    (z/root-string (reduce z/insert-right* end (reverse children))))
         :profile "live-code"
         :changed true}
        (let [existing (set (map profile-key->str (keys (z/sexpr pz))))
              name     (if (contains? existing "live-code") "fireworks-live-code" "live-code")
              kw       (keyword name)
              ;; assoc appends on the same line as the last profile — insert a newline before the new
              ;; key so it lands on its own line, then let cljfmt fix the indentation (as deps does).
              mz       (z/assoc pz kw {:plugins [coordinate]})
              mz       (z/insert-newline-left (z/left (z/get mz kw)))]
          {:text    (reformat (z/root-string mz))
           :profile name
           :changed true})))
    (catch :default _ {:error :unparseable})))

(def ^:private test-refresh-block
  "The exact :test-refresh block ensure-test-refresh splices in when a project.clj has none:
   a leading newline, the 2-space-indented comment header, and an aligned map. The values
   mirror fireworks-vscode.config/baseline; the key order here is intentional (changes-only
   first). Parsed and inserted verbatim so the comments and alignment are preserved."
  "
  ;; `test-refresh` options.
  ;; The Fireworks extension keeps this map present.
  ;; If it is not present when the user runs the `Fireworks: Live Code` command,
  ;; the extension will ask the user for permission to add it to the `project.clj`
  :test-refresh {:changes-only      true
                 :quiet             true
                 :notify-on-success false
                 :debug             true
                 :banner            \"🔥\"
                 :debug-banner      \"🔥\"
                 :test-banner       \"🧪 Running tests...\"
                 :clear             true}")

(defn ensure-test-refresh
  "Ensure the top-level defproject :test-refresh map in project.clj `text` against
   config/baseline. Absent -> splice in test-refresh-block (the commented, aligned map);
   present -> assoc only the missing baseline keys (existing values untouched). Returns
   {:text new-text :changed bool :added-keys [\"quiet\" …]} or {:error :unparseable}."
  [text]
  (try
    (let [trz (find-key (body-kv-start (z/of-string text)) :test-refresh)]
      (if (nil? trz)
        {:text       (let [children (n/children (p/parse-string-all test-refresh-block))
                           end      (-> (z/of-string text) z/down z/rightmost)]
                       (z/root-string
                        (reduce z/insert-right* end (reverse children))))
         :changed    true
         :added-keys (mapv name (keys config/baseline))}
        (let [present (set (keys (z/sexpr trz)))
              missing (remove present (keys config/baseline))]
          (if (empty? missing)
            {:text text :changed false :added-keys []}
            {:text       (z/root-string
                          (reduce (fn [m k] (z/assoc m k (get config/baseline k)))
                                  trz missing))
             :changed    true
             :added-keys (mapv name missing)}))))
    (catch :default _ {:error :unparseable})))

;; --- ~/.lein/profiles.clj (global fallback, read-only) --------------------

(defn- deep-has-coordinate?
  "True when the exact coordinate appears anywhere nested inside x."
  [x]
  (cond
    (plugin-match? x) true
    (coll? x)         (boolean (some deep-has-coordinate? x))
    :else             false))

(defn- deep-has-fireworks?
  "True when a Fireworks coordinate (a vector whose artifact name is \"fireworks\", e.g.
   [io.github.paintparty/fireworks \"0.21.0\"]) appears anywhere nested inside x. Matched by name,
   so any group/version counts."
  [x]
  (cond
    (and (vector? x) (symbol? (first x)) (= (name (first x)) fireworks-artifact)) true
    (coll? x)                                                                     (boolean (some deep-has-fireworks? x))
    :else                                                                         false))

(defn user-profile-status
  "Read ~/.lein/profiles.clj `text` (a map). Deep-scan the :user entry for the test-refresh plugin
   coordinate and a Fireworks dependency, and check whether :user has a :test-refresh options key.
   All three are required for Live Code to run off the global profile. Returns
   {:has-plugin bool :has-fireworks bool :has-test-refresh bool} or {:error :unparseable}."
  [text]
  (try
    (let [user (get (z/sexpr (z/of-string text)) :user)]
      {:has-plugin       (deep-has-coordinate? user)
       :has-fireworks    (deep-has-fireworks? user)
       :has-test-refresh (and (map? user) (contains? user :test-refresh))})
    (catch :default _ {:error :unparseable})))

(defn test-refresh-snippet
  "A human-readable :test-refresh { … } EDN snippet (from config/baseline) for the confirm /
   global-fallback modals."
  []
  (str ":test-refresh\n{"
       (->> config/baseline
            (map (fn [[k v]] (str k " " (pr-str v))))
            (str/join "\n "))
       "}"))
