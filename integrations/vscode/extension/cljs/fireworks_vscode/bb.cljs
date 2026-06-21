
(ns fireworks-vscode.bb
  "Pure reading of a project bb.edn for live coding — the Babashka counterpart to
   fireworks-vscode.deps. Same boundary discipline: text in, data out, no I/O. Reads the
   task names so the TS side can offer them in a picker and launch `bb <task>`. On
   unparseable input returns nil rather than throwing, so TS can abort cleanly."
  (:require [rewrite-clj.zip :as z]))

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
