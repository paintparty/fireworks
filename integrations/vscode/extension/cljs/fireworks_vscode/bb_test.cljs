
(ns fireworks-vscode.bb-test
  "Spec for reading task names out of a bb.edn string."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.bb :as bb]
            [rewrite-clj.parser :as p]))

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

(deftest watch-task-names-basic
  (testing "only tasks load-filing the Fireworks watcher are returned, in file order"
    (is (= ["live"]
           (bb/watch-task-names
            "{:tasks {build (println \"build\")
                      live  {:task (load-file \".fireworks/bb/watch.clj\")}}}")))))

(deftest watch-task-names-bare-and-prefixed
  (testing "bare-expression task body, and a ./ prefix on the path, both match"
    (is (= ["dev"]
           (bb/watch-task-names
            "{:tasks {dev (load-file \"./.fireworks/bb/watch.clj\")}}")))))

(deftest watch-task-names-nested
  (testing "a load-file nested inside a do (or other form) is found"
    (is (= ["live"]
           (bb/watch-task-names
            "{:tasks {live {:task (do (println \"starting\")
                                     (load-file \".fireworks/bb/watch.clj\"))}}}")))))

(deftest watch-task-names-ignores-others
  (testing "tasks without the load-file, or load-filing a different path, are ignored"
    (is (= []
           (bb/watch-task-names
            "{:tasks {build (println \"build\")
                      other {:task (load-file \"dev/watch.clj\")}}}")))))

(deftest watch-task-names-no-tasks
  (testing "no :tasks key -> empty vector"
    (is (= [] (bb/watch-task-names "{:paths [\"src\"]}")))
    (is (= [] (bb/watch-task-names "{}")))))

(deftest watch-task-names-unparseable
  (testing "broken EDN -> nil so TS can abort"
    (is (nil? (bb/watch-task-names "{:tasks {live ")))))

(deftest watch-template-parses
  (testing "the seeded watch.clj is valid Clojure (escaping guard) and is the bb watcher"
    (is (string? bb/watch-template))
    (is (str/includes? bb/watch-template "load-file"))
    (is (str/includes? bb/watch-template ".fireworks/config.edn"))
    ;; self-loads the fswatcher pod, so no :pods entry is required in the user's bb.edn
    (is (str/includes? bb/watch-template "load-pod"))
    (is (str/includes? bb/watch-template "org.babashka/fswatcher"))
    ;; clears the saved file's result dir before reload, so unreached `?` forms don't go stale
    (is (str/includes? bb/watch-template "clear-results!"))
    (is (str/includes? bb/watch-template ".fireworks/results"))
    ;; rewrite-clj parses every form without throwing — catches a botched escape
    (is (some? (p/parse-string-all bb/watch-template)))))
