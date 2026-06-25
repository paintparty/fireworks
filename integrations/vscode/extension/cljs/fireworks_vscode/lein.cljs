
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
  (:require [clojure.string :as str]
            [fireworks-vscode.config :as config]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(def ^:private plugin-sym 'com.jakemccrary/lein-test-refresh)
(def ^:private plugin-version "0.26.0")
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

;; --- :plugins vector editing ----------------------------------------------

(defn- find-plugin-child
  "The zloc of the lein-test-refresh coordinate (any version) within the :plugins vector
   zloc `zvec`, or nil."
  [zvec]
  (loop [c (z/down zvec)]
    (when c
      (let [s (try (z/sexpr c) (catch :default _ nil))]
        (if (and (vector? s) (= (first s) plugin-sym))
          c
          (recur (z/right c)))))))

(defn- append-coord
  "Append the exact coordinate to the :plugins vector zloc `zvec`. Returns a vector zloc."
  [zvec]
  (if-let [last-child (-> zvec z/down z/rightmost)]
    (-> last-child
        (z/insert-right* (n/coerce coordinate))
        (z/insert-right* (n/spaces 1))
        z/up)
    (z/replace zvec (n/coerce [coordinate])))) ; empty []

(defn- ensure-plugin-in-vector
  "Ensure the exact coordinate is in the :plugins vector zloc. Replaces a wrong-version
   lein-test-refresh entry rather than duplicating. Returns {:loc vector-zloc :changed bool}."
  [zvec]
  (let [existing (find-plugin-child zvec)]
    (cond
      (and existing (= (z/sexpr existing) coordinate))
      {:loc zvec :changed false}
      existing
      {:loc (z/up (z/replace existing (n/coerce coordinate))) :changed true}
      :else
      {:loc (append-coord zvec) :changed true})))

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

(defn add-plugin-to-profile
  "Ensure the exact coordinate in profile `profile-name`'s :plugins vector in project.clj
   `text`: create the :plugins vector if absent, replace a wrong-version entry, or append.
   Returns {:text new-text :changed bool} or {:error :unparseable}."
  [text profile-name]
  (try
    (let [pkey (keyword profile-name)
          pz   (find-key (body-kv-start (z/of-string text)) :profiles)
          prof (some-> pz (z/get pkey))]
      (cond
        (nil? prof)
        {:error :unparseable}

        (nil? (z/get prof :plugins))
        {:text (-> prof (z/assoc :plugins [coordinate]) z/root-string) :changed true}

        :else
        (let [{:keys [loc changed]} (ensure-plugin-in-vector (z/get prof :plugins))]
          {:text (z/root-string loc) :changed changed})))
    (catch :default _ {:error :unparseable})))

(defn ensure-test-refresh
  "Ensure the top-level defproject :test-refresh map in project.clj `text` against
   config/baseline. Absent -> add the full baseline on its own line; present -> assoc only
   the missing baseline keys (existing values untouched). Returns
   {:text new-text :changed bool :added-keys [\"quiet\" …]} or {:error :unparseable}."
  [text]
  (try
    (let [trz (find-key (body-kv-start (z/of-string text)) :test-refresh)]
      (if (nil? trz)
        {:text       (-> (z/of-string text)
                         z/down            ; defproject
                         z/rightmost       ; last body element
                         (z/insert-right* (n/coerce config/baseline))
                         (z/insert-right* (n/spaces 1))
                         (z/insert-right* (n/keyword-node :test-refresh))
                         (z/insert-right* (n/spaces 2))
                         (z/insert-right* (n/newlines 1))
                         z/root-string)
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

(defn user-profile-status
  "Read ~/.lein/profiles.clj `text` (a map). Deep-scan the :user entry for the exact
   coordinate and check whether :user has a :test-refresh key. Returns
   {:has-plugin bool :has-test-refresh bool} or {:error :unparseable}."
  [text]
  (try
    (let [user (get (z/sexpr (z/of-string text)) :user)]
      {:has-plugin       (deep-has-coordinate? user)
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
