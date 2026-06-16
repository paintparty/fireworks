;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.toggle-test
  "Spec for the pure toggle logic. Each case feeds toggle-form an opts map and
   asserts the full returned plan (kebab-case keywords) or nil. The multiline
   unwrap cases are the specification for the reindentation algorithm.

   Coordinate note: the `text` for a multiline selection is what Calva's
   selectCurrentForm yields via getText — line 0 starts at the selection's start
   column, but continuation lines are buffer-absolute (start at column 0). The
   sample texts below reflect that."
  (:require [cljs.test :refer [deftest is testing]]
            [clojure.string :as str]
            [fireworks-vscode.toggle :as toggle]))

(defn- opts [text [sl sc] [el ec] variant]
  {:text text :start {:line sl :col sc} :end {:line el :col ec} :variant variant})

(defn- pos [l c] {:line l :col c})

;; ---------------------------------------------------------------------------
;; Wrap
;; ---------------------------------------------------------------------------

(deftest wrap-single-line
  (testing "? wraps a bare form, cursor at start, no reformat"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 7)}
            :insert-text   "(? (+ 1 1))"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (opts "(+ 1 1)" [0 0] [0 7] "?")))))
  (testing "?> wraps a bare form"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 7)}
            :insert-text   "(?> (+ 1 1))"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (opts "(+ 1 1)" [0 0] [0 7] "?>"))))))

(deftest wrap-multiline
  (testing "? indents continuation lines by 3 so they align under (? "
    (is (= "(? (+ 1\n      2))"
           (:insert-text (toggle/toggle-form (opts "(+ 1\n   2)" [0 0] [1 5] "?"))))))
  (testing "?> indents continuation lines by 4 so they align under (?> "
    (is (= "(?> (+ 1\n       2))"
           (:insert-text (toggle/toggle-form (opts "(+ 1\n   2)" [0 0] [1 5] "?>"))))))
  (testing "wrap is start-column independent (continuation lines are buffer-absolute)"
    (is (= "(? (+ 1\n        2))"
           (:insert-text (toggle/toggle-form (opts "(+ 1\n     2)" [0 2] [1 7] "?")))))))

;; ---------------------------------------------------------------------------
;; Invert (toggle silenced/unsilenced). variant is irrelevant here — the symbol
;; is detected from the selection — so each is asserted once.
;; ---------------------------------------------------------------------------

(deftest invert-macro-symbol
  (testing "? -> !? , cursor after the 2-char inverse, reformat true"
    (is (= {:replace-range {:start (pos 2 4) :end (pos 2 5)}
            :insert-text   "!?"
            :new-cursor    (pos 2 6)
            :reformat?     true}
           (toggle/toggle-form (opts "?" [2 4] [2 5] "?")))))
  (testing "!? -> ?"
    (is (= {:replace-range {:start (pos 2 4) :end (pos 2 6)}
            :insert-text   "?"
            :new-cursor    (pos 2 5)
            :reformat?     true}
           (toggle/toggle-form (opts "!?" [2 4] [2 6] "?")))))
  (testing "?> -> !?>"
    (is (= {:replace-range {:start (pos 2 4) :end (pos 2 6)}
            :insert-text   "!?>"
            :new-cursor    (pos 2 7)
            :reformat?     true}
           (toggle/toggle-form (opts "?>" [2 4] [2 6] "?>")))))
  (testing "!?> -> ?>"
    (is (= {:replace-range {:start (pos 2 4) :end (pos 2 7)}
            :insert-text   "?>"
            :new-cursor    (pos 2 6)
            :reformat?     true}
           (toggle/toggle-form (opts "!?>" [2 4] [2 7] "?>"))))))

;; ---------------------------------------------------------------------------
;; Unwrap
;; ---------------------------------------------------------------------------

(deftest unwrap-single-line
  (testing "(? (+ 1 1)) -> (+ 1 1), cursor back to start, no reformat"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 11)}
            :insert-text   "(+ 1 1)"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (opts "(? (+ 1 1))" [0 0] [0 11] "?")))))
  (testing "label/options node is dropped: (? \"label\" form) -> form"
    (is (= "(+ 1 1)"
           (:insert-text (toggle/toggle-form
                          (opts "(? \"label\" (+ 1 1))" [0 0] [0 19] "?"))))))
  (testing "tap form unwraps the same way"
    (is (= "(+ 1 1)"
           (:insert-text (toggle/toggle-form (opts "(?> (+ 1 1))" [0 0] [0 12] "?>")))))))

(deftest unwrap-multiline
  (testing "kept form reindents to the selection start column (col 0)"
    (is (= "(+ 1\n   2)"
           (:insert-text (toggle/toggle-form
                          (opts "(? (+ 1\n      2))" [0 0] [1 9] "?"))))))
  (testing "kept form reindents to the selection start column (col 2)"
    (is (= "(+ 1\n     2)"
           (:insert-text (toggle/toggle-form
                          (opts "(? (+ 1\n        2))" [0 2] [1 11] "?"))))))
  (testing "wrap then unwrap at the same column round-trips"
    (let [wrapped (:insert-text (toggle/toggle-form (opts "(+ 1\n     2)" [0 2] [1 7] "?")))]
      (is (= "(+ 1\n     2)"
             (:insert-text (toggle/toggle-form
                            (opts wrapped [0 2] [1 (count (last (str/split-lines wrapped)))] "?"))))))))

;; ---------------------------------------------------------------------------
;; double-form normalization: selection "? <arg>" without enclosing parens.
;; Shrink the replace range to the single "?" char and invert it.
;; ---------------------------------------------------------------------------

(deftest double-form-normalization
  (testing "\"? foo\" shrinks to the symbol and inverts to !?"
    (is (= {:replace-range {:start (pos 3 2) :end (pos 3 3)}
            :insert-text   "!?"
            :new-cursor    (pos 3 4)
            :reformat?     true}
           (toggle/toggle-form (opts "? foo" [3 2] [3 7] "?"))))))

;; ---------------------------------------------------------------------------
;; Ignore / un-ignore (#_ reader discard). variant "#_".
;; ---------------------------------------------------------------------------

(deftest ignore-bare-form
  (testing "(+ 1 1) -> #_(+ 1 1), cursor at start, no reformat"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 7)}
            :insert-text   "#_(+ 1 1)"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (opts "(+ 1 1)" [0 0] [0 7] "#_")))))
  (testing "multiline ignore shifts continuation lines right 2 to stay aligned"
    (is (= "#_(+ 1\n     2)"
           (:insert-text (toggle/toggle-form (opts "(+ 1\n   2)" [0 0] [1 5] "#_")))))))

(deftest unignore-discard-in-selection
  (testing "#_(+ 1 1) -> (+ 1 1) when selectCurrentForm includes the discard"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 9)}
            :insert-text   "(+ 1 1)"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (assoc (opts "#_(+ 1 1)" [0 0] [0 9] "#_") :before "")))))
  (testing "multiline un-ignore shifts continuation lines left 2"
    (is (= "(+ 1\n   2)"
           (:insert-text (toggle/toggle-form (opts "#_(+ 1\n     2)" [0 0] [1 6] "#_")))))))

(deftest unignore-discard-before-selection
  (testing "discard just left of the selection is removed; range/cursor shift left 2"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 9)}
            :insert-text   "(+ 1 1)"
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form
            (assoc (opts "(+ 1 1)" [0 2] [0 9] "#_") :before "#_")))))
  (testing "ignore is not falsely triggered by unrelated preceding text"
    (is (= "#_(+ 1 1)"
           (:insert-text (toggle/toggle-form
                          (assoc (opts "(+ 1 1)" [0 3] [0 10] "#_") :before "(")))))))

;; ---------------------------------------------------------------------------
;; No-ops
;; ---------------------------------------------------------------------------

(deftest no-ops
  (testing "empty selection -> nil"
    (is (nil? (toggle/toggle-form (opts "" [0 0] [0 0] "?")))))
  (testing "whitespace-only selection -> nil"
    (is (nil? (toggle/toggle-form (opts "   " [0 0] [0 3] "?")))))
  (testing "unbalanced / unparseable selection -> nil (no throw)"
    (is (nil? (toggle/toggle-form (opts "(+ 1 1" [0 0] [0 6] "?"))))))
