
(ns fireworks-vscode.inline-results
  "Pure logic for the inline-results feature. Given a whole-document string,
   returns the document's namespace name and one entry per Fireworks `?` call —
   the `(? …)` lists whose head symbol is exactly `?`. Each entry is
   {:key \"<start-row>:<start-col>\" :row <end-row>} (all 1-based): `:key` is the
   opening `(`, which is the filename the `?` macro writes under
   .fireworks/results/<ns>/; `:row` is the form's last row, where the TS side
   anchors the inline decoration (so multi-line forms show their value at the end).
   Knows nothing about VS Code.

   Returns {:namespace \"my.ns\" | nil :positions [{:key \"12:3\" :row 14} …]}.
   Never throws — malformed source yields {:namespace nil :positions []}."
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]))

(defn- list-head=
  "True when `loc` is a list whose first child sexpr equals `sym`."
  [loc sym]
  (and (= :list (z/tag loc))
       (= sym (try (some-> loc z/down z/sexpr) (catch :default _ nil)))))

(defn- find-top-level
  "Walk the top-level siblings starting at `loc`, returning the first list whose
   head sexpr is `sym`, or nil."
  [loc sym]
  (loop [loc loc]
    (cond
      (nil? loc)           nil
      (list-head= loc sym) loc
      :else                (recur (z/right loc)))))

(defn- ns-sym-name
  "The namespace symbol's string from the ns-form loc, or nil."
  [ns-loc]
  (when ns-loc
    (try (some-> ns-loc z/down z/right z/sexpr str) (catch :default _ nil))))

(defn- collect-positions
  "Depth-first walk from `zloc`, collecting {:key \"<start-row>:<start-col>\"
   :row <end-row>} (1-based) for every `(? …)` list."
  [zloc]
  (loop [loc zloc
         acc []]
    (if (or (nil? loc) (z/end? loc))
      acc
      (recur (z/next loc)
             (if (list-head= loc '?)
               (let [[[sr sc] [er _ec]] (z/position-span loc)]
                 (conj acc {:key (str sr ":" sc) :row er}))
               acc)))))

(defn analyze
  "See ns docstring. Fast-paths to empty when the buffer has no `(?` substring."
  [text]
  (if-not (str/index-of text "(?")
    {:namespace nil :positions []}
    (try
      (let [zloc (z/of-string text {:track-position? true})]
        {:namespace (ns-sym-name (find-top-level zloc 'ns))
         :positions (collect-positions zloc)})
      (catch :default _ {:namespace nil :positions []}))))
