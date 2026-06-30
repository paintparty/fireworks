
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
           (:insert-text (toggle/toggle-form (opts "(?> (+ 1 1))" [0 0] [0 12] "?>"))))))
  (testing "empty wrap (no value form, e.g. a thread-tap) -> removed entirely"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 3)}
            :insert-text   ""
            :new-cursor    (pos 0 0)
            :reformat?     false}
           (toggle/toggle-form (opts "(?)" [0 0] [0 3] "?"))))
    (is (= "" (:insert-text (toggle/toggle-form (opts "(!?)" [0 0] [0 4] "?")))))
    (is (= "" (:insert-text (toggle/toggle-form (opts "(?>)" [0 0] [0 4] "?>")))))
    (is (= "" (:insert-text (toggle/toggle-form (opts "(!?>)" [0 0] [0 5] "?>")))))))

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
;; Bulk unwrap: strip every wrap inside a user selection. No variant; all four
;; macro symbols are stripped. nil when nothing is wrapped.
;; ---------------------------------------------------------------------------

(defn- uopts [text [sl sc] [el ec]]
  {:text text :start {:line sl :col sc} :end {:line el :col ec}})

(deftest unwrap-all-basics
  (testing "a single wrap: whole selection replaced, cursor at start, reformat true"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 11)}
            :insert-text   "(+ 1 1)"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/unwrap-all (uopts "(? (+ 1 1))" [0 0] [0 11])))))
  (testing "all four macro variants are stripped"
    (is (= "(a) (b) (c) (d)"
           (:insert-text (toggle/unwrap-all
                          (uopts "(? (a)) (!? (b)) (?> (c)) (!?> (d))" [0 0] [0 34]))))))
  (testing "the label/options arg is dropped"
    (is (= "(+ 1 1)"
           (:insert-text (toggle/unwrap-all (uopts "(? \"lbl\" (+ 1 1))" [0 0] [0 17]))))))
  (testing "nested wraps fully unwrap"
    (is (= "x"
           (:insert-text (toggle/unwrap-all (uopts "(? (!? x))" [0 0] [0 10]))))))
  (testing "wraps are stripped, untouched siblings and whitespace preserved"
    (is (= "(foo)\n(bar)\n(baz)"
           (:insert-text (toggle/unwrap-all (uopts "(foo)\n(? (bar))\n(baz)" [0 0] [2 5]))))))
  (testing "empty wrap (no value form, e.g. a thread-tap) is removed entirely"
    (is (= "(-> 1 (+ 3) (+ 6))"
           (:insert-text (toggle/unwrap-all (uopts "(-> 1 (+ 3) (?) (+ 6))" [0 0] [0 22])))))
    (is (= "(foo) (bar)"
           (:insert-text (toggle/unwrap-all (uopts "(foo) (!?) (bar)" [0 0] [0 16])))))
    (is (= ""
           (:insert-text (toggle/unwrap-all (uopts "(?)" [0 0] [0 3])))))))

(deftest unwrap-all-no-ops
  (testing "no wrapped forms -> nil"
    (is (nil? (toggle/unwrap-all (uopts "(+ 1 1)" [0 0] [0 7])))))
  (testing "predicate `?` suffixes are not wraps -> nil (short-circuits, no parse)"
    (is (nil? (toggle/unwrap-all
               (uopts "(when (even? x) (string? y) (seq? z)) " [0 0] [0 37])))))
  (testing "blank selection -> nil"
    (is (nil? (toggle/unwrap-all (uopts "   " [0 0] [0 3])))))
  (testing "unparseable selection -> nil (no throw)"
    (is (nil? (toggle/unwrap-all (uopts "(? (+ 1" [0 0] [0 7]))))))

(deftest maybe-wrapped-pre-check
  (testing "true for every wrap variant (incl. leading ws and empty wraps)"
    (is (every? toggle/maybe-wrapped?
                ["(? x)" "(!? x)" "(?> x)" "(!?> x)" "(?)" "( ? x)"
                 "(foo (? x))" "[a (!?> b)]"])))
  (testing "false for predicate-heavy code with no wraps, blanks, and nil"
    (is (not-any? toggle/maybe-wrapped?
                  ["(even? x)" "(string? (foo? y))" "(+ 1 2)" "" "   " nil]))))

;; ---------------------------------------------------------------------------
;; Bulk silence master-toggle: any loud -> silence all; all silent -> make all loud.
;; ---------------------------------------------------------------------------

(defn- silenced [text [sl sc] [el ec]]
  (:insert-text (toggle/toggle-all-silent (uopts text [sl sc] [el ec]))))

(deftest toggle-all-silent-master
  (testing "any loud -> silence all (tap distinction preserved)"
    (is (= "(!? a) (!?> b)" (silenced "(? a) (?> b)" [0 0] [0 12]))))
  (testing "a mix counts as 'any loud' -> silence the lot"
    (is (= "(!? a) (!? b)" (silenced "(? a) (!? b)" [0 0] [0 12]))))
  (testing "all already silent -> make all loud"
    (is (= "(? a) (?> b)" (silenced "(!? a) (!?> b)" [0 0] [0 14]))))
  (testing "nested wraps retag too"
    (is (= "(!? (!? x))" (silenced "(? (!? x))" [0 0] [0 10]))))
  (testing "empty wraps toggle: (?) -> (!?), and back when all silent"
    (is (= "(!?)" (silenced "(?)" [0 0] [0 3])))
    (is (= "(?)" (silenced "(!?)" [0 0] [0 4]))))
  (testing "full plan: whole region replaced, cursor at start, reformat true"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 5)}
            :insert-text   "(!? a)"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/toggle-all-silent (uopts "(? a)" [0 0] [0 5]))))))

(deftest toggle-all-silent-no-ops
  (testing "no wraps -> nil (short-circuits)"
    (is (nil? (toggle/toggle-all-silent (uopts "(even? x) (+ 1 2)" [0 0] [0 17])))))
  (testing "blank -> nil"
    (is (nil? (toggle/toggle-all-silent (uopts "   " [0 0] [0 3])))))
  (testing "unparseable -> nil (no throw)"
    (is (nil? (toggle/toggle-all-silent (uopts "(? (+ 1" [0 0] [0 7]))))))

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

;; ---------------------------------------------------------------------------
;; Set option ("? with option"). Each case asserts the full plan or nil. The
;; option only changes line 0's width, so multiline continuation lines shift by
;; that delta; :reformat? is always true (Calva confirms the realignment).
;; ---------------------------------------------------------------------------

(defn- sfo [text [sl sc] [el ec] option]
  {:text text :start {:line sl :col sc} :end {:line el :col ec} :option option})

(deftest set-option-insert
  (testing "adds an option to a wrap that has none, cursor at start, reformat true"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 15)}
            :insert-text   "(? :+ (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/set-form-option (sfo "(? (range 100))" [0 0] [0 15] ":+")))))
  (testing "a lone keyword is the printed value, so the option is inserted before it"
    (is (= "(? :+ :foo)"
           (:insert-text (toggle/set-form-option (sfo "(? :foo)" [0 0] [0 8] ":+")))))))

(deftest set-option-replace
  (testing "replaces an existing leading keyword option"
    (is (= "(? :- (range 100))"
           (:insert-text (toggle/set-form-option (sfo "(? :+ (range 100))" [0 0] [0 18] ":-"))))))
  (testing "replacing with a longer option keeps a single space"
    (is (= "(? :no-file (range 100))"
           (:insert-text (toggle/set-form-option (sfo "(? :+ (range 100))" [0 0] [0 18] ":no-file")))))))

(deftest set-option-remove
  (testing "strips the leading keyword option"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 18)}
            :insert-text   "(? (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/set-form-option (sfo "(? :+ (range 100))" [0 0] [0 18] nil)))))
  (testing "remove on a wrap with no option -> nil"
    (is (nil? (toggle/set-form-option (sfo "(? (range 100))" [0 0] [0 15] nil)))))
  (testing "remove on a bare form -> nil"
    (is (nil? (toggle/set-form-option (sfo "(range 100)" [0 0] [0 11] nil))))))

(deftest set-option-variants
  (testing "?> head preserved"
    (is (= "(?> :pp (range 100))"
           (:insert-text (toggle/set-form-option (sfo "(?> (range 100))" [0 0] [0 16] ":pp"))))))
  (testing "!? head preserved"
    (is (= "(!? :+ x)"
           (:insert-text (toggle/set-form-option (sfo "(!? x)" [0 0] [0 6] ":+"))))))
  (testing "!?> head preserved"
    (is (= "(!?> :trace x)"
           (:insert-text (toggle/set-form-option (sfo "(!?> x)" [0 0] [0 7] ":trace")))))))

(deftest set-option-wrap-then-add
  (testing "a bare form is wrapped with ? and the option added"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 11)}
            :insert-text   "(? :pp (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/set-form-option (sfo "(range 100)" [0 0] [0 11] ":pp")))))
  (testing "multiline bare form: continuation indents under the value"
    (is (= "(? :+ (+ 1\n         2))"
           (:insert-text (toggle/set-form-option (sfo "(+ 1\n   2)" [0 0] [1 5] ":+")))))))

(deftest set-option-multiline
  (testing "insert shifts continuation lines right by the option width"
    (is (= "(? :+ (+ 1\n         2))"
           (:insert-text (toggle/set-form-option (sfo "(? (+ 1\n      2))" [0 0] [1 9] ":+"))))))
  (testing "remove shifts continuation lines left by the option width"
    (is (= "(? (+ 1\n      2))"
           (:insert-text (toggle/set-form-option (sfo "(? :+ (+ 1\n         2))" [0 0] [1 12] nil)))))))

(deftest set-option-no-ops
  (testing "blank selection -> nil"
    (is (nil? (toggle/set-form-option (sfo "   " [0 0] [0 3] ":+")))))
  (testing "unparseable selection -> nil (no throw)"
    (is (nil? (toggle/set-form-option (sfo "(? (range" [0 0] [0 9] ":+"))))))

;; ---------------------------------------------------------------------------
;; Toggle :trace. Add when absent / remove when present, preserving any other
;; leading flags. A bare form is wrapped with ? and :trace added. Like the option
;; cases, the flag only changes line 0's width, so :reformat? is always true.
;; ---------------------------------------------------------------------------

(defn- tt [text [sl sc] [el ec]]
  {:text text :start {:line sl :col sc} :end {:line el :col ec}})

(deftest toggle-trace-bare-form
  (testing "a bare form is wrapped as (? :trace form), cursor at start, reformat true"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 11)}
            :insert-text   "(? :trace (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/toggle-trace (tt "(range 100)" [0 0] [0 11])))))
  (testing "multiline bare form: continuation indents under the value"
    (is (= "(? :trace (+ 1\n             2))"
           (:insert-text (toggle/toggle-trace (tt "(+ 1\n   2)" [0 0] [1 5])))))))

(deftest toggle-trace-add
  (testing "adds :trace to a wrap that has no flags"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 15)}
            :insert-text   "(? :trace (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/toggle-trace (tt "(? (range 100))" [0 0] [0 15])))))
  (testing "adds :trace ahead of an existing flag, which is preserved"
    (is (= "(? :trace :pp (range 100))"
           (:insert-text (toggle/toggle-trace (tt "(? :pp (range 100))" [0 0] [0 19]))))))
  (testing "head is preserved (?>, !?, !?>)"
    (is (= "(?> :trace x)" (:insert-text (toggle/toggle-trace (tt "(?> x)" [0 0] [0 6])))))
    (is (= "(!? :trace x)" (:insert-text (toggle/toggle-trace (tt "(!? x)" [0 0] [0 6])))))
    (is (= "(!?> :trace x)" (:insert-text (toggle/toggle-trace (tt "(!?> x)" [0 0] [0 7])))))))

(deftest toggle-trace-remove
  (testing "removes :trace, form stays wrapped, cursor at start, reformat true"
    (is (= {:replace-range {:start (pos 0 0) :end (pos 0 22)}
            :insert-text   "(? (range 100))"
            :new-cursor    (pos 0 0)
            :reformat?     true}
           (toggle/toggle-trace (tt "(? :trace (range 100))" [0 0] [0 22])))))
  (testing "removes only :trace, leaving other flags intact"
    (is (= "(? :pp (range 100))"
           (:insert-text (toggle/toggle-trace (tt "(? :trace :pp (range 100))" [0 0] [0 26])))))
    (is (= "(? :pp (range 100))"
           (:insert-text (toggle/toggle-trace (tt "(? :pp :trace (range 100))" [0 0] [0 26]))))))
  (testing "head is preserved on remove"
    (is (= "(?> x)" (:insert-text (toggle/toggle-trace (tt "(?> :trace x)" [0 0] [0 13])))))))

(deftest toggle-trace-multiline
  (testing "add shifts continuation lines right by the :trace width"
    (is (= "(? :trace (+ 1\n             2))"
           (:insert-text (toggle/toggle-trace (tt "(? (+ 1\n      2))" [0 0] [1 9]))))))
  (testing "remove shifts continuation lines left by the :trace width"
    (is (= "(? (+ 1\n      2))"
           (:insert-text (toggle/toggle-trace (tt "(? :trace (+ 1\n             2))" [0 0] [1 16])))))))

(deftest toggle-trace-no-ops
  (testing "blank selection -> nil"
    (is (nil? (toggle/toggle-trace (tt "   " [0 0] [0 3])))))
  (testing "unparseable selection -> nil (no throw)"
    (is (nil? (toggle/toggle-trace (tt "(? (range" [0 0] [0 9]))))))

;; ---------------------------------------------------------------------------
;; The other flag toggles (:-, :+, :pp) share toggle-flag with :trace (covered
;; above), so these assert the per-flag wrappers wire the right keyword and the
;; add/remove/preserve behavior holds for each.
;; ---------------------------------------------------------------------------

(deftest toggle-minus-flag
  (testing "wraps a bare form as (? :- form)"
    (is (= "(? :- (range 100))"
           (:insert-text (toggle/toggle-minus (tt "(range 100)" [0 0] [0 11]))))))
  (testing "adds :- ahead of an existing flag, preserved"
    (is (= "(? :- :pp (range 100))"
           (:insert-text (toggle/toggle-minus (tt "(? :pp (range 100))" [0 0] [0 19]))))))
  (testing "removes :-, leaving other flags intact, form stays wrapped"
    (is (= "(? :pp (range 100))"
           (:insert-text (toggle/toggle-minus (tt "(? :- :pp (range 100))" [0 0] [0 22])))))))

(deftest toggle-plus-flag
  (testing "wraps a bare form as (? :+ form)"
    (is (= "(? :+ (range 100))"
           (:insert-text (toggle/toggle-plus (tt "(range 100)" [0 0] [0 11]))))))
  (testing "adds :+ to a wrap that has none"
    (is (= "(? :+ (range 100))"
           (:insert-text (toggle/toggle-plus (tt "(? (range 100))" [0 0] [0 15]))))))
  (testing "removes :+ (head preserved)"
    (is (= "(?> x)"
           (:insert-text (toggle/toggle-plus (tt "(?> :+ x)" [0 0] [0 9])))))))

(deftest toggle-pp-flag
  (testing "wraps a bare form as (? :pp form)"
    (is (= "(? :pp (range 100))"
           (:insert-text (toggle/toggle-pp (tt "(range 100)" [0 0] [0 11]))))))
  (testing "adds :pp ahead of an existing flag, preserved"
    (is (= "(? :pp :trace (range 100))"
           (:insert-text (toggle/toggle-pp (tt "(? :trace (range 100))" [0 0] [0 22]))))))
  (testing "removes :pp, leaving other flags intact"
    (is (= "(? :trace (range 100))"
           (:insert-text (toggle/toggle-pp (tt "(? :pp :trace (range 100))" [0 0] [0 26])))))))

(deftest toggle-flag-no-ops
  (testing "blank selection -> nil for each flag"
    (is (nil? (toggle/toggle-minus (tt "   " [0 0] [0 3]))))
    (is (nil? (toggle/toggle-plus (tt "   " [0 0] [0 3]))))
    (is (nil? (toggle/toggle-pp (tt "   " [0 0] [0 3]))))))
