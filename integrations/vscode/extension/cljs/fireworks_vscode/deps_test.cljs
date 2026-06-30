
(ns fireworks-vscode.deps-test
  "Spec for reading alias names out of a deps.edn string."
  (:require [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.deps :as deps]))

(deftest alias-names-basic
  (testing "alias keys returned in file order, colon dropped"
    (is (= ["test" "test-refresh"]
           (deps/alias-names
            "{:paths [\"src\"]
              :deps {}
              :aliases {:test {:extra-paths [\"test\"]}
                        :test-refresh {:extra-deps {}}}}")))))

(deftest alias-names-qualified
  (testing "namespaced alias keys keep their namespace"
    (is (= ["build" "my/runner"]
           (deps/alias-names
            "{:aliases {:build {} :my/runner {}}}")))))

(deftest alias-names-no-aliases
  (testing "no :aliases key -> empty vector (parses fine, just nothing to pick)"
    (is (= [] (deps/alias-names "{:paths [\"src\"] :deps {}}")))
    (is (= [] (deps/alias-names "{}")))))

(deftest alias-names-unparseable
  (testing "broken EDN -> nil so TS can abort"
    (is (nil? (deps/alias-names "{:aliases {:test ")))
    (is (nil? (deps/alias-names "}}}not edn")))))
