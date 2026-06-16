;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.toggle
  "Pure transform logic for the Fireworks toggle commands. Ported from the two
   Joyride scripts (toggle_fireworks.cljs, toggle_fireworks_tap.cljs); the two
   differ only in the wrap symbol, which arrives here as `variant`. Knows nothing
   about VS Code or Calva. Returns an edit plan (kebab-case keywords) or nil for
   the no-op cases. See fireworks-vscode-extension-phase1-design.md."
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

;; ? <-> !?  and  ?> <-> !?>   (toggle silenced/unsilenced)
(def ^:private inverse-macro-sym-by-macro
  {"?" "!?", "!?" "?", "?>" "!?>", "!?>" "?>"})

(defn- macro-sym? [code]
  (contains? inverse-macro-sym-by-macro code))

(defn- wrapped-form?
  "Looks like an existing wrapped fireworks call: ends in `)` and opens with `(`
   followed by a macro symbol. Mirrors the scripts' `fireworks-macro-sym`."
  [code]
  (and (string? code)
       (str/ends-with? code ")")
       (boolean (re-find #"^\((\s*\?|!\?|\?>|!\?>) *" code))))

(defn- indent-continuation
  "Prefix every line but the first with n spaces."
  [code n]
  (->> (str/split code #"\n")
       (map-indexed (fn [i line]
                      (if (zero? i)
                        line
                        (str (apply str (repeat n " ")) line))))
       (str/join "\n")))

(defn- deindent-continuation
  "Drop up to n leading spaces from every line but the first."
  [code n]
  (->> (str/split code #"\n")
       (map-indexed (fn [i line]
                      (if (zero? i)
                        line
                        (subs line (min n (count (re-find #"^ *" line)))))))
       (str/join "\n")))

(defn- reformat-wrapped
  "Wrap-branch reindent: indent continuation lines under the opening `(<sym> ` —
   3 spaces for `?`, 4 for `?>`. Mirrors the scripts' `reformat-code`."
  [code sym]
  (indent-continuation code (+ 2 (count sym))))

(defn- value-node
  "The node to keep when unwrapping. Mirrors the scripts' node-1..node-4 +
   `(last forms)` selection: the last child for forms of 1-3 elements, and for
   4+ elements the script's node-4 (4th child's string, 3rd child's loc) ported
   verbatim — an edge case real fireworks forms don't hit (`(? form)` /
   `(? \"label\" form)` are 2 or 3 elements)."
  [zd n]
  (letfn [(info [z] {:loc (some-> z z/node meta) :string (some-> z z/string)})]
    (case n
      1 (info zd)
      2 (info (z/right zd))
      3 (info (-> zd z/right z/right))
      (let [third (-> zd z/right z/right)]
        {:loc    (some-> third z/node meta)
         :string (some-> third z/right z/string)}))))

(defn- unwrap-plan
  "Unwrap `(? form)` / `(? \"label\" form)` back to the value form, reindenting its
   continuation lines so they re-align at the selection's start column. Ported
   faithfully from the scripts' `unwrap-fireworks-form`; the multiline unwrap tests
   are its specification."
  [code start end]
  (let [sel-start-col (:col start)
        n             (-> code p/parse-string n/sexpr count)
        zd            (-> code z/of-string z/down)
        {last-str :string {:keys [row col]} :loc} (value-node zd n)
        ;; rewrite-clj col is 1-based; the form's column within the selection text
        last-node-column        (dec col)
        last-node-on-same-line? (= 1 row)
        reformatted
        (->> (str/split last-str #"\n")
             (map-indexed
              (fn [i line]
                (if (zero? i)
                  line
                  (let [num-leading-spaces (some-> (re-find #"^( *)" line) last count)
                        indentation        (- num-leading-spaces last-node-column)
                        subs-start-base    (- last-node-column sel-start-col)
                        subs-start         (if last-node-on-same-line?
                                             (if (neg? indentation)
                                               (+ last-node-column indentation)
                                               last-node-column)
                                             subs-start-base)]
                    (subs line subs-start)))))
             (str/join "\n"))]
    {:replace-range {:start start :end end}
     :insert-text   reformatted
     :new-cursor    start
     :reformat?     false}))

(def ^:private discard "#_")

(defn- ignore-plan
  "Toggle the reader-discard prefix #_ on the selected form. `before` is the text
   immediately to the left of the selection, so an already-ignored form is caught
   whether selectCurrentForm includes the #_ in the selection or leaves it just
   outside. #_ shifts the form two columns, so continuation lines are
   re-/de-indented by 2. The cursor lands at the form's resulting start column."
  [text start end before]
  (cond
    ;; #_ is inside the selection -> un-ignore: drop it, shift the form left 2.
    (str/starts-with? text discard)
    {:replace-range {:start start :end end}
     :insert-text   (deindent-continuation (subs text 2) 2)
     :new-cursor    start
     :reformat?     false}

    ;; #_ sits immediately before the selection -> un-ignore by removing it too.
    (and (string? before) (str/ends-with? before discard))
    (let [new-start {:line (:line start) :col (- (:col start) 2)}]
      {:replace-range {:start new-start :end end}
       :insert-text   (deindent-continuation text 2)
       :new-cursor    new-start
       :reformat?     false})

    ;; not ignored -> ignore: prepend #_, shift the form right 2.
    :else
    {:replace-range {:start start :end end}
     :insert-text   (str discard (indent-continuation text 2))
     :new-cursor    start
     :reformat?     false}))

(defn toggle-form
  "Pure dispatch over the selected `text`. opts:
   {:text :start :end :variant :before} where :start/:end are {:line :col}
   (0-based), :variant is \"?\", \"?>\" or \"#_\", and :before is the text just to
   the left of the selection (used only by the #_ variant). Returns an edit plan
   or nil (no-op: blank selection or parse failure)."
  [{:keys [text start end variant before]}]
  (try
    (when-not (str/blank? text)
      (if (= variant discard)
        (ignore-plan text start end before)
        ;; Calva's selectCurrentForm can return the macro symbol plus its argument
        ;; without the enclosing parens (e.g. "? foo"). Normalize that to the symbol
        ;; alone and shrink the replace range to the single symbol char.
        (let [double-form? (boolean (re-find #"^\? .+" text))
              code         (if double-form? "?" text)]
          (cond
            (macro-sym? code)
            ;; Invert: toggle ? <-> !? (or ?> <-> !?>), cursor after the symbol.
            (let [inverse     (inverse-macro-sym-by-macro code)
                  replace-end (if double-form?
                                {:line (:line start) :col (inc (:col start))}
                                end)]
              {:replace-range {:start start :end replace-end}
               :insert-text   inverse
               :new-cursor    {:line (:line start) :col (+ (:col start) (count inverse))}
               :reformat?     true})

            (wrapped-form? code)
            (unwrap-plan code start end)

            :else
            ;; Wrap a bare form as (<variant> form). parse-string validates it first
            ;; so unbalanced/unparseable selections no-op (design overrides the
            ;; scripts here, which would have wrapped them).
            (let [_     (p/parse-string code)
                  code* (if (re-find #"\n" code)
                          (reformat-wrapped code variant)
                          code)]
              {:replace-range {:start start :end end}
               :insert-text   (str "(" variant " " code* ")")
               :new-cursor    start
               :reformat?     false})))))
    (catch :default _ nil)))
