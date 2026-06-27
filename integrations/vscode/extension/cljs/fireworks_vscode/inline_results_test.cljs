
(ns fireworks-vscode.inline-results-test
  "Spec for the pure inline-results analysis. Pins the extracted namespace and the
   \"<line>:<col>\" positions (1-based, at the opening `(`) of every `(? …)` call,
   plus the fast path and the never-throws contract."
  (:require [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.inline-results :as ir]))

(deftest namespace-extraction
  (testing "reads the ns symbol from the ns form"
    (is (= "my.app.core"
           (:namespace (ir/analyze "(ns my.app.core\n  (:require [x]))\n(? 1)")))))
  (testing "nil namespace when there is no ns form"
    (is (nil? (:namespace (ir/analyze "(defn f [] (? 1))"))))))

(deftest single-position
  (is (= [{:key "3_1" :row 3}]
         (:positions (ir/analyze "(ns my.ns)\n\n(? (+ 1 2))")))))

(deftest multiple-positions
  (let [text "(ns my.ns)\n(? :a)\n(def x (? (inc 1)))\n"]
    (is (= [{:key "2_1" :row 2} {:key "3_8" :row 3}]
           (:positions (ir/analyze text))))))

(deftest multi-line-form-anchors-on-end-row
  (testing ":key is the start (file key); :row is the form's last row"
    (is (= [{:key "2_1" :row 4}]
           (:positions (ir/analyze "(ns my.ns)\n(?\n  (+ 1\n     2))"))))))

(deftest nested-positions
  (testing "a `?` nested inside another form is collected"
    (is (= [{:key "1_1" :row 1} {:key "1_4" :row 1}]
           (:positions (ir/analyze "(? (? 1))"))))))

(deftest only-question-mark-matched
  (testing "?>, !? and !?> are not treated as `?`"
    (let [text "(ns my.ns)\n(?> :tap)\n(!? :p)\n(!?> :pt)\n(? :hit)"]
      (is (= [{:key "5_1" :row 5}] (:positions (ir/analyze text))))))
  (testing "a symbol merely starting with ? is not `?`"
    (is (= [] (:positions (ir/analyze "(?foo 1)"))))))

(deftest fast-path-no-question
  (testing "no `(?` substring -> empty, no parse"
    (is (= {:namespace nil :positions []}
           (ir/analyze "(ns my.ns)\n(defn f [] (+ 1 2))")))))

(deftest never-throws
  (testing "malformed source yields empty rather than throwing"
    (is (= {:namespace nil :positions []}
           (ir/analyze "(ns my.ns) (? (((")))))
