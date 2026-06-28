
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
;; reloads the saved file and runs an entry `-main` so its `?` forms execute — printing to this
;; terminal and, when a .fireworks/results/ dir exists, writing inline results there. The entry
;; is :entry-ns from config, else the first watched file defining -main; it also runs once at
;; startup, pre-loading Fireworks so the first save reloads instantly (no slow double-reload).
;; Safe to edit; the extension won't overwrite it once present.
;;
;; Optional settings, read from .fireworks/config.edn at runtime (all optional):
;;   {:clear true :banner \"🔥\" :watch-paths [\"src\" \"test\"]
;;    :entry-ns my.app.core   ; pin a fixed ns to run instead of the saved file's
;;    :test-ns my.app.core-test :run-entry true :run-tests false}

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
(def banner      (get config :banner \"🔥\"))
(def watch-paths (get config :watch-paths [\"src\" \"test\"]))

(defn- ns-of-file
  \"The namespace symbol declared at the top of the file at `path`, or nil.\"
  [path]
  (try
    (let [form (read-string (slurp path))]
      (when (and (seq? form) (= 'ns (first form))) (second form)))
    (catch Throwable _ nil)))

(defn- detect-entry-ns
  \"The namespace of the first watched .clj(c) file that defines a `-main`, or nil. Used as the
   default entry when :entry-ns isn't set, so the loop runs (and prints) out of the box.\"
  []
  (some (fn [f]
          (when (and (.isFile f)
                     (re-find #\"\\.cljc?$\" (.getName f))
                     (re-find #\"\\(defn-?\\s[^\\n]*-main\" (slurp f)))
            (ns-of-file (.getPath f))))
        (mapcat #(when (.exists (io/file %)) (file-seq (io/file %))) watch-paths)))

(def entry-ns    (or (some-> (get config :entry-ns) symbol) (detect-entry-ns)))
(def test-ns     (some-> (get config :test-ns) symbol))
(def run-entry?  (get config :run-entry true))
(def run-tests?  (get config :run-tests false))

(defn show-banner []
  (when clear (print \"\\033[H\\033[2J\")) ; home + clear, preserving scrollback
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

(defn reload! [path]
  (show-banner)
  (try
    (let [reloaded (when (and path (.exists (io/file path)))
                     (load-file path)
                     (ns-of-file path))
          target   (or entry-ns reloaded)]
      ;; Run the entry's `-main` so its `?` forms execute (load-file only redefines them).
      ;; Targets the saved file's ns, or :entry-ns / the detected entry. The saved file is
      ;; already loaded above; otherwise (startup, or editing a non-entry file) require the
      ;; entry once — which also pre-loads Fireworks so the first save reloads instantly.
      (when (and run-entry? target)
        (when-not (find-ns target) (require target))
        (when-let [main (resolve (symbol (name target) \"-main\"))]
          (main)))
      (when (and run-tests? test-ns)
        (require test-ns :reload-all)
        (t/run-tests test-ns)))
    (catch Throwable e
      (println \"Reload failed:\" (ex-message e)))))

;; fswatcher reports varied event types per save style (:write / :chmod / :remove+:create),
;; sometimes spread over ~1s. content-changed? collapses them to one reload per real edit.
(defn on-event [event]
  (let [path (:path event)]
    (when (and path (.exists (io/file path)) (content-changed? path))
      (reload! path))))

(def watch-opts {:recursive true :delay-ms 50})

(println \"Fireworks: watching\" (str/join \", \" watch-paths)
         \"— edit and save to reload. Ctrl-C to quit.\")
(reload! nil) ; run once at startup
(doseq [p watch-paths]
  (when (.exists (io/file p)) (fw/watch p on-event watch-opts)))
@(promise)
")
