;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.config
  "Pure config-editing logic for Phase 2 (live coding). Same boundary discipline
   as fireworks-vscode.toggle: no VS Code, no Calva, no file I/O. Functions take
   the current file text (or nil/blank when the file does not exist yet) plus the
   values to apply, and return new text using rewrite-clj so surrounding content,
   comments, and formatting survive.

   All merges are MANAGED-KEY ONLY: add what's missing, update the keys the
   extension owns, never touch unrelated aliases, profiles, or keys (see
   fireworks-vscode-extension-phase2-design.md and CLAUDE.md). On unparseable
   input a function returns {:error :unparseable} rather than throwing, so the TS
   side can abort cleanly. The bridge wraps these results for the JS boundary."
  (:require [clojure.string :as str]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [rewrite-clj.zip :as z]))

;; ---------------------------------------------------------------------------
;; Managed identifiers and defaults (the values the extension owns)
;; ---------------------------------------------------------------------------

(def ^:private test-refresh-dep 'com.jakemccrary/test-refresh)
(def ^:private fireworks-dep    'io.github.paintparty/fireworks)
(def ^:private lein-plugin      'com.jakemccrary/lein-test-refresh)

(def ^:private tap-banner-default  "🔥🔥🔥🔥🔥🔥🔥🔥🔥")
(def ^:private test-banner-default "📋 Running tests...")

;; Rendered key order for a created .test-refresh.edn (and any keys we add to one).
(def ^:private option-order
  [:quiet :changes-only :notify-on-success :debug :banner :debug-mode-opts :clear])

(defn default-options
  "The default option map for `mode`: tap mode carries :debug true, the fire banner,
   and :debug-mode-opts; test mode flips :debug/:banner and drops :debug-mode-opts
   (which only applies in tap/debug mode). Mirrors the design doc; used to seed a
   created .test-refresh.edn (deps) or :test-refresh map (lein)."
  [mode]
  (let [base {:quiet             true
              :changes-only      true
              :notify-on-success false
              :debug             (= mode :tap)
              :banner            (if (= mode :tap) tap-banner-default test-banner-default)
              :clear             true}]
    (if (= mode :tap)
      (assoc base :debug-mode-opts {:print-full-stack-trace? true})
      base)))

;; ---------------------------------------------------------------------------
;; rewrite-clj insertion helpers
;;
;; Plain z/assoc preserves all unrelated content and formats new entries cleanly
;; when the target map is already multi-line (the common deps.edn/.test-refresh.edn
;; shape). For the rarer case of adding a key to a single-line or empty map, we
;; replace the separator whitespace with a newline + indent so the new key lands
;; on its own line — guarded so we never touch the opening delimiter of the first
;; key in a map (which would corrupt it).
;; ---------------------------------------------------------------------------

(defn- newline-before-key
  "key-loc is positioned at a freshly-added map key. If it has a preceding sibling,
   replace the single separator whitespace to its left with newline + `indent`
   spaces; otherwise it is the first key, so leave it. Returns a loc at the map."
  [key-loc indent]
  (if (z/left key-loc)
    (-> key-loc
        z/left*                              ; the separator whitespace node
        (z/replace* (n/newlines 1))
        (z/insert-right* (n/spaces indent))
        z/up)
    (z/up key-loc)))

(defn- assoc-nl
  "Assoc k -> v (a coerced value) into the map at zmap. Update in place if present,
   else place on its own line indented `indent`. Returns a loc at the map."
  [zmap k v indent]
  (if (z/get zmap k)
    (z/assoc zmap k v)
    (-> zmap (z/assoc k v) (z/get k) z/left (newline-before-key indent))))

(defn- assoc-node-nl
  "Like assoc-nl but the value is a pre-parsed rewrite-clj node, so its multi-line
   formatting is preserved (z/assoc of a raw map would collapse it to one line)."
  [zmap k node indent]
  (if (z/get zmap k)
    (-> zmap (z/get k) (z/replace node) z/up)
    (-> zmap (z/assoc k :fireworks/placeholder) (z/get k) (z/replace node)
        z/left (newline-before-key indent))))

;; ---------------------------------------------------------------------------
;; project.clj navigation (a (defproject name version :k v ...) LIST, not a map)
;; ---------------------------------------------------------------------------

(defn- defproject?
  "True when zloc is positioned at a (defproject ...) list."
  [zloc]
  (and (= :list (z/tag zloc))
       (= 'defproject (some-> zloc z/down z/sexpr))))

(defn- up-to-defproject
  "Walk up to the (defproject ...) list loc. z/of-string roots at a :forms wrapper,
   so plain z/up past the list would overshoot; stop at the defproject itself."
  [loc]
  (loop [l loc]
    (if (defproject? l)
      l
      (if-let [u (z/up l)] (recur u) l))))

(defn- project-kv-loc
  "Loc of the value for top-level defproject key k (e.g. :plugins), or nil."
  [list-loc k]
  (some-> list-loc z/down (z/find-value z/right k) z/right))

(defn- find-coord
  "Within a vector of [sym \"version\"] coordinates, the loc of the entry whose first
   element is `sym`, or nil."
  [vec-loc sym]
  (loop [e (z/down vec-loc)]
    (cond
      (nil? e)                          nil
      (= sym (some-> e z/down z/sexpr)) e
      :else                             (recur (z/right e)))))

(defn- append-project-kv
  "Append `:k v-node` as a new top-level entry to the defproject list, on its own
   line at a 2-space body indent, before the closing paren. Returns the list loc."
  [list-loc k v-node]
  (-> list-loc
      z/down z/rightmost
      (z/insert-right* v-node)
      (z/insert-right* (n/spaces 1))
      (z/insert-right* (n/keyword-node k))
      (z/insert-right* (n/spaces 2))
      (z/insert-right* (n/newlines 1))
      up-to-defproject))

(defn- options-map-loc
  "Loc of the options map: the :test-refresh value when zloc is a defproject list
   (lein), else zloc itself (a bare .test-refresh.edn map). May be nil for a lein
   project with no :test-refresh yet."
  [zloc]
  (if (defproject? zloc)
    (project-kv-loc zloc :test-refresh)
    zloc))

;; ---------------------------------------------------------------------------
;; Rendering (create-from-defaults paths produce nicely-formatted text directly)
;; ---------------------------------------------------------------------------

(defn- render-options
  "Render an options map as an aligned multi-line edn string with the values column-
   aligned. Known keys come first in option-order; any extra keys follow. Each line
   after the first is prefixed with `indent` spaces so continuation lines align under
   the first key (indent 1 for a file-root .test-refresh.edn; larger when the map is
   embedded under a defproject :test-refresh key)."
  ([opts] (render-options opts 1))
  ([opts indent]
   (let [ks    (concat (filter #(contains? opts %) option-order)
                       (remove (set option-order) (keys opts)))
         width (apply max (map (comp count str) ks))
         pad   (apply str (repeat indent " "))
         line  (fn [k]
                 (let [ks* (str k)
                       kp  (apply str (repeat (- width (count ks*)) " "))]
                   (str ks* kp " " (pr-str (get opts k)))))
         lines (map line ks)]
     (str "{" (first lines) "\n"
          (str/join "\n" (map #(str pad %) (rest lines)))
          "}\n"))))

(defn- alias-node
  "The managed :test-refresh deps alias as a parsed node, pinned to the given
   versions. Self-sufficient (carries both deps in :extra-deps) so the project's
   top-level :deps is never touched."
  [fireworks-version test-refresh-version]
  (p/parse-string
   (str "{:extra-paths [\"test\"]\n"
        "   :extra-deps  {" test-refresh-dep " {:mvn/version \"" test-refresh-version "\"}\n"
        "                 " fireworks-dep " {:mvn/version \"" fireworks-version "\"}}\n"
        "   :main-opts   [\"-m\" \"com.jakemccrary.test-refresh\"]}")))

;; ---------------------------------------------------------------------------
;; deps.edn
;; ---------------------------------------------------------------------------

(defn ensure-deps-alias
  "Ensure a single managed :test-refresh alias in deps.edn's :aliases, pinned to
   the given versions. text nil/blank => create the whole file. Existing aliases,
   :deps, and comments are left untouched; re-running only updates the managed
   alias value. Returns new text or {:error :unparseable}."
  [text {:keys [fireworks-version test-refresh-version]}]
  (try
    (let [node (alias-node fireworks-version test-refresh-version)]
      (if (str/blank? text)
        (-> (z/of-string "{:aliases {}}")
            (z/get :aliases) (assoc-node-nl :test-refresh node 1) z/root-string
            (str "\n"))
        (let [zloc  (z/of-string text)
              zloc  (if (z/get zloc :aliases)
                      zloc
                      (-> zloc (z/assoc :aliases {}) (z/get :aliases) z/left
                          (newline-before-key 1)))]
          (-> zloc (z/get :aliases) (assoc-node-nl :test-refresh node 2) z/root-string))))
    (catch :default _ {:error :unparseable})))

;; ---------------------------------------------------------------------------
;; project.clj (Leiningen): three scoped edits, each preserving everything else
;; ---------------------------------------------------------------------------

(defn- ensure-plugin
  "Ensure top-level :plugins carries [lein-plugin \"version\"]: add :plugins if
   absent, update the managed plugin's version if present, else append it."
  [list-loc version]
  (let [pv (project-kv-loc list-loc :plugins)]
    (cond
      (nil? pv)
      (append-project-kv list-loc :plugins
                         (p/parse-string (str "[[" lein-plugin " \"" version "\"]]")))

      (find-coord pv lein-plugin)
      (-> (find-coord pv lein-plugin) z/down z/right (z/replace version) up-to-defproject)

      :else
      (up-to-defproject
       (z/append-child pv (p/parse-string (str "[" lein-plugin " \"" version "\"]")))))))

(defn- ensure-test-refresh-map
  "Add a :test-refresh options map (from defaults) if absent. If present it is the
   user's authoritative options (single source of truth) — leave it untouched; the
   mode toggle edits it via set-mode."
  [list-loc options]
  (if (project-kv-loc list-loc :test-refresh)
    list-loc
    ;; continuation lines align under the first key: 2 (body) + len(:test-refresh)
    ;; + 1 space + 1 brace.
    (let [indent (+ 2 (count (str :test-refresh)) 2)]
      (append-project-kv list-loc :test-refresh
                         (p/parse-string (str/trimr (render-options options indent)))))))

(defn- ensure-dev-fireworks
  "Ensure :profiles {:dev {:dependencies [[fireworks \"version\"]]}}, scoped to :dev so
   Fireworks stays out of artifacts. Adds whichever of :profiles / :dev /
   :dependencies / the coordinate is missing; updates the version if present."
  [list-loc version]
  (let [coord (str "[" fireworks-dep " \"" version "\"]")
        profs (project-kv-loc list-loc :profiles)]
    (if (nil? profs)
      (append-project-kv list-loc :profiles
                         (p/parse-string (str "{:dev {:dependencies [" coord "]}}")))
      (let [dev (z/get profs :dev)]
        (if (nil? dev)
          (up-to-defproject
           (assoc-node-nl profs :dev (p/parse-string (str "{:dependencies [" coord "]}")) 1))
          (let [deps (z/get dev :dependencies)]
            (cond
              (nil? deps)
              (up-to-defproject
               (assoc-node-nl dev :dependencies (p/parse-string (str "[" coord "]")) 1))

              (find-coord deps fireworks-dep)
              (-> (find-coord deps fireworks-dep) z/down z/right (z/replace version)
                  up-to-defproject)

              :else
              (up-to-defproject (z/append-child deps (p/parse-string coord))))))))))

(defn ensure-lein-setup
  "Apply the three managed project.clj edits: the :plugins entry, the :test-refresh
   options map (created from defaults only if absent), and the :dev-scoped Fireworks
   dependency. options defaults to the tap-mode map. Unrelated keys, profiles, and
   comments survive; re-running is a fixpoint. Returns new text, {:error :unparseable},
   or {:error :not-defproject} when the text isn't a defproject."
  [text {:keys [fireworks-version test-refresh-version options]}]
  (try
    (let [zloc (z/of-string text)]
      (if-not (defproject? zloc)
        {:error :not-defproject}
        (-> zloc
            (ensure-plugin test-refresh-version)
            (ensure-test-refresh-map (or options (default-options :tap)))
            (ensure-dev-fireworks fireworks-version)
            z/root-string)))
    (catch :default _ {:error :unparseable})))

;; ---------------------------------------------------------------------------
;; .test-refresh.edn (deps) and the lein :test-refresh map share these
;; ---------------------------------------------------------------------------

(defn write-options
  "Merge the managed option keys in options-map into a .test-refresh.edn. text
   nil/blank => render a fresh file. Existing keys/comments survive; only the keys
   in options-map are added or updated. Returns new text or {:error :unparseable}."
  [text options-map]
  (try
    (if (str/blank? text)
      (render-options options-map)
      (z/root-string
       (reduce-kv (fn [zloc k v] (assoc-nl zloc k v 1))
                  (z/of-string text)
                  options-map)))
    (catch :default _ {:error :unparseable})))

(defn read-options
  "Parse the options map to a Clojure map: a bare .test-refresh.edn (deps), or the
   :test-refresh map inside a project.clj (lein). Returns the map, {:error :no-options}
   (lein project with no :test-refresh), or {:error :unparseable}."
  [text]
  (try
    (if-let [m (options-map-loc (z/of-string text))]
      (z/sexpr m)
      {:error :no-options})
    (catch :default _ {:error :unparseable})))

(defn read-mode
  "Read the live-coding mode: :tap when :debug is truthy, else :test (absent :debug or
   absent options => :test). Reads from a bare .test-refresh.edn or a project.clj's
   :test-refresh map. Returns the mode or {:error :unparseable}."
  [text]
  (try
    (let [debug (some-> (options-map-loc (z/of-string text)) (z/get :debug))]
      (if (and debug (z/sexpr debug)) :tap :test))
    (catch :default _ {:error :unparseable})))

(defn set-mode
  "Flip the options to `mode` (:tap | :test): set :debug and swap :banner to the
   matching banner, in place. Other keys (incl. :debug-mode-opts) are left as-is.
   Operates on a bare .test-refresh.edn or a project.clj's :test-refresh map (returns
   the whole file either way). Returns new text, {:error :no-options}, or
   {:error :unparseable}."
  [text mode {:keys [tap-banner test-banner]}]
  (try
    (let [tap?   (= mode :tap)
          banner (if tap? (or tap-banner tap-banner-default)
                          (or test-banner test-banner-default))
          m      (options-map-loc (z/of-string text))]
      (if (nil? m)
        {:error :no-options}
        (-> m
            (assoc-nl :debug tap? 1)
            (assoc-nl :banner banner 1)
            z/root-string)))
    (catch :default _ {:error :unparseable})))

;; ---------------------------------------------------------------------------
;; Seed-from-global: pull only the managed subset out of a global config
;; ---------------------------------------------------------------------------

(defn- coord-version
  "Version (second element) of the [sym \"v\"] entry in `coords` whose first element
   is `sym`, or nil."
  [coords sym]
  (some (fn [c] (when (and (sequential? c) (= sym (first c))) (second c))) coords))

(defn extract-managed
  "Extract the managed values from a global config's text, for seeding project
   files. file-kind:
     :deps             -> {:fireworks-version v|nil :test-refresh-version v|nil}
                          from the global :test-refresh alias's :extra-deps.
     :test-refresh-edn -> {:options <map>} from a global ~/.test-refresh.edn.
     :lein             -> {:fireworks-version v|nil :test-refresh-version v|nil
                           :options <map>|nil} pulled from any profile in a global
                          ~/.lein/profiles.clj (the plugin version, the :dev-style
                          Fireworks dep version, and the :test-refresh options).
   Returns the managed map, {:error :unparseable}, or {:error :unsupported}."
  [text file-kind]
  (try
    (case file-kind
      :deps
      (let [extra-deps (some-> (z/of-string text)
                               (z/get :aliases) (z/get :test-refresh) (z/get :extra-deps))
            ver        (fn [dep] (some-> extra-deps (z/get dep) (z/get :mvn/version) z/sexpr))]
        {:fireworks-version    (ver fireworks-dep)
         :test-refresh-version (ver test-refresh-dep)})

      :test-refresh-edn
      {:options (z/sexpr (z/of-string text))}

      ;; profiles.clj is read as data (we're extracting, not preserving formatting);
      ;; scan every profile so the plugin/dep/options are found wherever they live.
      :lein
      (let [profiles (vals (z/sexpr (z/of-string text)))]
        {:test-refresh-version (some #(coord-version (:plugins %) lein-plugin) profiles)
         :fireworks-version    (some #(coord-version (:dependencies %) fireworks-dep) profiles)
         :options              (some :test-refresh profiles)})

      {:error :unsupported})
    (catch :default _ {:error :unparseable})))
