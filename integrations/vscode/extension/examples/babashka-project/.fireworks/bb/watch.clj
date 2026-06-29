;; Fireworks Live Code watcher (Babashka), managed by the Fireworks VS Code extension.
;; Invoked by a bb.edn task that runs (load-file ".fireworks/bb/watch.clj"). On each save it
;; reloads the saved file so its top-level `?` forms re-run, printing to this terminal and,
;; when a .fireworks/results/ dir exists, writing inline results there. Reload-only: running an
;; entry `-main` and tests are opt-in (:run-entry / :run-tests, both off by default). At startup
;; it loads the entry ns once, pre-loading Fireworks so the first save is instant and the entry's
;; top-level `?` forms paint on launch. The entry is :entry-ns, else the first watched file with a
;; -main. NOTE: -main runs synchronously on the watch thread, so a long-running -main blocks
;; reloads. Keep it short, or leave :run-entry false and drive `?` from top-level forms.
;; Before each reload it clears the saved file's .fireworks/results/<ns>/ dir, so a `?` in an
;; unreached branch (e.g. a cond arm not taken this run) doesn't keep showing a stale value, and
;; results for moved/deleted `?` forms don't linger. Opt out with :clear-stale-results false.
;; Safe to edit; the extension won't overwrite it once present.
;;
;; Optional settings, read from .fireworks/config.edn at runtime (all optional):
;;   {:clear true :banner "🔥" :watch-paths ["src" "test"]
;;    :entry-ns my.app.core   ; pin a fixed ns to run instead of the saved file's
;;    :test-ns my.app.core-test :run-entry false :run-tests false
;;    :clear-stale-results true}

;; Load the filesystem-watcher pod here, so this works without a :pods entry in bb.edn.
;; load-pod is fine even when bb.edn already declares the pod.
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/fswatcher "0.0.7")

(require '[pod.babashka.fswatcher :as fw]
         '[clojure.test :as t]
         '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def config
  (let [f (io/file ".fireworks/config.edn")]
    (if (.exists f)
      (try (edn/read-string (slurp f)) (catch Throwable _ {}))
      {})))

(def clear       (get config :clear true))
(def banner      (get config :banner "🔥"))
(def watch-paths (get config :watch-paths ["src" "test"]))

(defn- ns-of-file
  "The namespace symbol declared at the top of the file at `path`, or nil."
  [path]
  (try
    (let [form (read-string (slurp path))]
      (when (and (seq? form) (= 'ns (first form))) (second form)))
    (catch Throwable _ nil)))

(defn- clear-results!
  "Delete .fireworks/results/<ns>/ for `nsym` so only `?` forms evaluated in the next reload
   repopulate it. Stale results from unreached branches (a cond arm not taken this run) and from
   moved/deleted `?` forms are removed. No-op when the dir is absent; never throws."
  [nsym]
  (when (and (get config :clear-stale-results true) nsym)
    (try
      (let [dir (io/file ".fireworks/results" (str nsym))]
        (when (.isDirectory dir)
          ;; reverse file-seq = depth-first, so children are deleted before their parent dir.
          (doseq [f (reverse (file-seq dir))] (.delete f))))
      (catch Throwable _ nil))))

(defn- detect-entry-ns
  "The namespace of the first watched .clj(c) file that defines a `-main`, or nil. Used as the
   default entry when :entry-ns isn't set, so the loop runs (and prints) out of the box."
  []
  (some (fn [f]
          (when (and (.isFile f)
                     (re-find #"\.cljc?$" (.getName f))
                     (re-find #"\(defn-?\s[^\n]*-main" (slurp f)))
            (ns-of-file (.getPath f))))
        (mapcat #(when (.exists (io/file %)) (file-seq (io/file %))) watch-paths)))

(def entry-ns    (or (some-> (get config :entry-ns) symbol) (detect-entry-ns)))
(def test-ns     (some-> (get config :test-ns) symbol))
(def run-entry?  (get config :run-entry false))
(def run-tests?  (get config :run-tests false))

(defn show-banner []
  ;; Header: clear the screen, then print the banner (with a couple of blank lines above it for
  ;; breathing room) BEFORE the reload's `?` output. With more than a screenful of output the
  ;; banner can scroll up into scrollback; keep per-reload output short to keep it pinned on top.
  (when clear (println "\033[H\033[2J")) ; home + clear, preserving scrollback
  (when-not (str/blank? banner) (println banner))
  (flush))

;; A single save can surface as several fswatcher events — e.g. a :write followed by a metadata
;; :chmod up to ~1s later — which a short time-debounce can't coalesce. Dedup on content instead:
;; reload only when the file's bytes actually changed since we last reloaded it, so a trailing
;; :chmod (or a re-save of identical content) doesn't fire a second reload.
(def last-hash (atom {}))

(defn- content-changed? [path]
  (let [h (hash (slurp path))]
    (when (not= h (get @last-hash path))
      (swap! last-hash assoc path h)
      true)))

(defn- run-extras! [target]
  ;; Opt-in, config-driven work after a reload: run an entry -main and/or tests. Both are OFF
  ;; by default. -main runs synchronously on the watch thread, so a long-running -main blocks
  ;; reloads. Keep it short, or leave :run-entry false and drive `?` from top-level forms.
  (when (and run-entry? target)
    (when-not (find-ns target) (require target))
    (when-let [main (resolve (symbol (name target) "-main"))]
      (main)))
  (when (and run-tests? test-ns)
    (require test-ns :reload-all)
    (t/run-tests test-ns)))

(defn reload! [path]
  ;; Reload the saved file so its top-level `?` forms re-run (writing inline results), then any
  ;; opt-in extras. load-file redefines the file's vars; its top-level forms are what execute the
  ;; `?` calls. A function's inner `?` only writes when top-level code (or -main) calls it.
  (show-banner)
  (try
    (let [reloaded (when (and path (.exists (io/file path)))
                     (let [nsym (ns-of-file path)]
                       ;; Clear before load-file: only `?` forms run this reload repopulate the cache.
                       (clear-results! nsym)
                       (load-file path)
                       nsym))]
      (run-extras! (or entry-ns reloaded)))
    (catch Throwable e
      (println "Reload failed:" (ex-message e)))))

(defn warmup! []
  ;; Startup pass: load the entry ns once so Fireworks is pre-loaded (first save is instant) and
  ;; the entry's top-level `?` forms paint on launch. Reload-only: no -main unless :run-entry.
  (show-banner)
  (try
    (when (and entry-ns (not (find-ns entry-ns))) (require entry-ns))
    (run-extras! entry-ns)
    (catch Throwable e
      (println "Startup failed:" (ex-message e)))))

;; fswatcher reports varied event types per save style (:write / :chmod / :remove+:create),
;; sometimes spread over ~1s. content-changed? collapses them to one reload per real edit.
(defn on-event [event]
  (let [path (:path event)]
    (when (and path (.exists (io/file path)) (content-changed? path))
      (reload! path))))

(def watch-opts {:recursive true :delay-ms 50})

(println "Fireworks: watching" (str/join ", " watch-paths)
         ". Edit and save to reload. Ctrl-C to quit.")
;; Establish the watch BEFORE the startup warmup, so no save during startup is missed.
(doseq [p watch-paths]
  (when (.exists (io/file p)) (fw/watch p on-event watch-opts)))
(warmup!)
@(promise)
