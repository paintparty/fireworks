(ns example.core-test
  (:require [clojure.test :refer [deftest is]]
            [example.core :as core]))

(deftest greet-test
  (is (= "Hello, Fireworks!" (core/greet "Fireworks"))))

(deftest fib-test
  (is (= [0 1 1 2 3] (map core/fib (range 5)))))
