
(ns fireworks-vscode.deps
  "Pure reading of a project deps.edn for live coding. Same boundary discipline as the
   other fireworks-vscode namespaces: no VS Code, no Calva, no file I/O — text in, data
   out. The user owns deps.edn; the extension never writes it. This namespace only reads
   the alias names so the TS side can offer them in a picker and launch `clojure -M:<alias>`.
   On unparseable input a function returns nil rather than throwing, so TS can abort cleanly."
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
