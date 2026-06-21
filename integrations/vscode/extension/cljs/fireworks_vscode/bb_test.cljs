
(ns fireworks-vscode.bb-test
  "Spec for reading task names out of a bb.edn string."
  (:require [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.bb :as bb]))

(deftest task-names-basic
  (testing "symbol task keys returned in file order"
    (is (= ["build" "test" "watch"]
           (bb/task-names
            "{:paths [\"src\"]
              :tasks {build (println \"build\")
                      test  (println \"test\")
                      watch {:task (println \"watch\")}}}")))))

(deftest task-names-skips-special-keys
  (testing ":init/:requires/:enter/:leave configure the runner and are not tasks"
    (is (= ["run"]
           (bb/task-names
            "{:tasks {:init (def x 1)
                      :requires ([babashka.fs :as fs])
                      :enter (prn :enter)
                      :leave (prn :leave)
                      run (println \"go\")}}")))))

(deftest task-names-no-tasks
  (testing "no :tasks key -> empty vector"
    (is (= [] (bb/task-names "{:paths [\"src\"]}")))
    (is (= [] (bb/task-names "{}")))))

(deftest task-names-unparseable
  (testing "broken EDN -> nil so TS can abort"
    (is (nil? (bb/task-names "{:tasks {build ")))
    (is (nil? (bb/task-names "}}}not edn")))))
