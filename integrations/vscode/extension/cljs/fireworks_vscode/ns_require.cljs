;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.ns-require
  "Pure logic for the \"Add Fireworks Require\" command. Given a whole-document
   string, inserts `[fireworks.core :refer [? !? ?> !?>]]` into the namespace's
   `ns` form — appended to an existing `:require` (aligned under its libspecs) or
   creating the `:require` if there is none. Knows nothing about VS Code or Calva.
   Returns an edit plan (kebab-case keywords) confined to the `ns` form, or nil for
   the no-op cases (no `ns` form, or Fireworks already required)."
  (:require [clojure.string :as str]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [rewrite-clj.zip :as z]))

(def ^:private libspec-str "[fireworks.core :refer [? !? ?> !?>]]")
(def ^:private require-str (str "(:require " libspec-str ")"))

;; Parse the literals into nodes so their internal whitespace is preserved exactly
;; (n/coerce would drop the separators).
(defn- libspec-node [] (p/parse-string libspec-str))
(defn- require-node [] (p/parse-string require-str))

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
      (nil? loc)            nil
      (list-head= loc sym)  loc
      :else                 (recur (z/right loc)))))

(defn- find-child-list
  "Walk the children of `parent-loc`, returning the first child list whose head
   sexpr is `sym`, or nil."
  [parent-loc sym]
  (loop [loc (z/down parent-loc)]
    (cond
      (nil? loc)            nil
      (list-head= loc sym)  loc
      :else                 (recur (z/right loc)))))

(defn- append-on-new-line
  "Insert `node` as a new child of the list at `anchor-loc`'s parent, on its own
   line indented to `col` (1-based). `anchor-loc` is the sibling the new node
   follows. The three raw inserts land directly to the right of `anchor-loc`, so
   placing them newline-last yields `anchor\\n<spaces>node`."
  [anchor-loc col node]
  (-> anchor-loc
      (z/insert-right* node)
      (z/insert-right* (n/spaces (dec col)))
      (z/insert-right* (n/newlines 1))))

(defn- add-to-require
  "Append the Fireworks libspec under the existing `:require`, aligned to the
   column of its last libspec. Returns the ns-form loc."
  [require-loc]
  (let [last-spec (-> require-loc z/down z/rightmost) ; last non-ws child of (:require ...)
        [_ col]   (z/position last-spec)]
    (-> (append-on-new-line last-spec col (libspec-node))
        z/up   ; -> (:require ...)
        z/up))) ; -> (ns ...)

(defn- create-require
  "Append a fresh `(:require ...)` to the ns form, indented two spaces. Returns the
   ns-form loc (append-child* leaves the loc at the parent)."
  [ns-loc]
  (-> ns-loc
      (z/append-child* (n/newlines 1))
      (z/append-child* (n/spaces 2))
      (z/append-child* (require-node))))

(defn add-fireworks-require
  "Pure transform over the whole document `text`. Returns
   {:replace-range {:start {:line :col} :end {:line :col}} :insert-text \"…\"}
   (0-based line/col, end col exclusive) confining the edit to the ns form, or nil
   (no ns form, Fireworks already required, or a parse failure)."
  [text]
  (try
    (let [ns-loc (-> (z/of-string text {:track-position? true})
                     (find-top-level 'ns))]
      (when (and ns-loc
                 (not (str/includes? (z/string ns-loc) "fireworks.core")))
        (let [[[sr sc] [er ec]] (z/position-span ns-loc) ; original span, ones-based
              require-loc       (find-child-list ns-loc :require)
              edited            (if require-loc
                                  (add-to-require require-loc)
                                  (create-require ns-loc))]
          {:replace-range {:start {:line (dec sr) :col (dec sc)}
                           :end   {:line (dec er) :col (dec ec)}}
           :insert-text   (z/string edited)})))
    (catch :default _ nil)))
