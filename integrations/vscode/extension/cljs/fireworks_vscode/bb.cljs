
(ns fireworks-vscode.bb
  "Pure reading of a project bb.edn for live coding — the Babashka counterpart to
   fireworks-vscode.deps. Same boundary discipline: text in, data out, no I/O. Reads the
   task names so the TS side can offer them in a picker and launch `bb <task>`. On
   unparseable input returns nil rather than throwing, so TS can abort cleanly.

   bb is a live-coding target only when its bb.edn is explicitly wired for it: a task whose
   body runs (load-file \".fireworks/bb/watch.clj\"). watch-task-names finds those tasks (so a
   bb.edn kept only for build scripts isn't mistaken for a watcher), and watch-template is the
   generic watcher the TS side seeds into .fireworks/bb/watch.clj when missing."
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]))

(defn task-names
  "The task names defined under :tasks in bb.edn `text`, in file order, as a vector of
   strings (e.g. [\"build\" \"test\"]). Task keys are symbols; the special keyword keys
   (:init/:requires/:enter/:leave) configure the runner rather than name a task, so only
   symbol keys are kept. Returns [] when there is no :tasks map, and nil when the text
   won't parse."
  [text]
  (try
    (if-let [tasks (z/get (z/of-string text) :tasks)]
      (->> (z/sexpr tasks) keys (filter symbol?) (mapv str))
      [])
    (catch :default _ nil)))

;; The Fireworks-managed bb watcher path. A bb.edn task is a live-coding watcher when its
;; body load-files this (a leading ./ or other prefix is tolerated — we match on the suffix).
(def watch-path ".fireworks/bb/watch.clj")

(defn- watch-load?
  "True when x is, or deep-contains, a (load-file \"…/.fireworks/bb/watch.clj\") call."
  [x]
  (cond
    (and (seq? x) (= 'load-file (first x)) (string? (second x))
         (str/ends-with? (second x) watch-path)) true
    (coll? x) (boolean (some watch-load? x))
    :else     false))

(defn watch-task-names
  "The task names in bb.edn `text` whose body load-files the Fireworks watcher (watch-path),
   in file order, as a vector of strings. These are the tasks the TS side may run as the bb
   watcher. Returns [] when no task is wired (so bb is not offered as a runtime), and nil when
   the text won't parse."
  [text]
  (try
    (if-let [tasks (z/get (z/of-string text) :tasks)]
      (->> (z/sexpr tasks)
           (filter (fn [[k v]] (and (symbol? k) (watch-load? v))))
           (mapv (comp str key)))
      [])
    (catch :default _ nil)))

(def watch-template
  "The generic Fireworks bb watcher, seeded into .fireworks/bb/watch.clj when absent. Static —
   no per-project substitution; per-project options come from .fireworks/config.edn at runtime.
   On each save it load-files the changed file (re-running its `?` forms, which write inline
   results under .fireworks/results/), with optional -main / test runs driven by config."
  ";; Fireworks Live Code watcher (Babashka), managed by the Fireworks VS Code extension.
;; Invoked by a bb.edn task that runs (load-file \".fireworks/bb/watch.clj\"). On each save it
;; reloads the changed file so its `?` forms re-run and write inline results under
;; .fireworks/results/. Safe to edit; the extension won't overwrite it once present.
;;
;; Optional settings, read from .fireworks/config.edn at runtime (all optional):
;;   {:clear true :banner \"🔥🔥🔥\" :watch-paths [\"src\" \"test\"]
;;    :entry-ns my.app.core :test-ns my.app.core-test :run-entry true :run-tests false}

;; Load the filesystem-watcher pod here, so this works without a :pods entry in bb.edn.
;; load-pod is fine even when bb.edn already declares the pod.
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/fswatcher \"0.0.7\")

(require '[pod.babashka.fswatcher :as fw]
         '[clojure.test :as t]
         '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def config
  (let [f (io/file \".fireworks/config.edn\")]
    (if (.exists f)
      (try (edn/read-string (slurp f)) (catch Throwable _ {}))
      {})))

(def clear       (get config :clear true))
(def banner      (get config :banner \"🔥🔥🔥\"))
(def watch-paths (get config :watch-paths [\"src\" \"test\"]))
(def entry-ns    (some-> (get config :entry-ns) symbol))
(def test-ns     (some-> (get config :test-ns) symbol))
(def run-entry?  (get config :run-entry (boolean entry-ns)))
(def run-tests?  (get config :run-tests false))

(defn show-banner []
  (when clear (print \"\\033[H\\033[2J\")) ; home + clear, preserving scrollback
  (when-not (str/blank? banner) (println banner))
  (flush))

(def debounce-ms 75)
(def last-fired (atom 0))

(defn reload! [path]
  (show-banner)
  (try
    (when (and path (.exists (io/file path)))
      (load-file path))
    (when (and run-entry? entry-ns)
      (require entry-ns :reload-all)
      ((requiring-resolve (symbol (name entry-ns) \"-main\"))))
    (when (and run-tests? test-ns)
      (require test-ns :reload-all)
      (t/run-tests test-ns))
    (catch Throwable e
      (println \"Reload failed:\" (ex-message e)))))

;; fswatcher reports different event types per save style (:chmod / :write / :remove+:create);
;; the debounce collapses a save's burst into a single reload.
(defn on-event [event]
  (let [now (System/currentTimeMillis)]
    (when (> (- now @last-fired) debounce-ms)
      (reset! last-fired now)
      (reload! (:path event)))))

(def watch-opts {:recursive true :delay-ms 50})

(println \"Fireworks: watching\" (str/join \", \" watch-paths)
         \"— edit and save to reload. Ctrl-C to quit.\")
(reload! nil) ; run once at startup
(doseq [p watch-paths]
  (when (.exists (io/file p)) (fw/watch p on-event watch-opts)))
@(promise)
")
