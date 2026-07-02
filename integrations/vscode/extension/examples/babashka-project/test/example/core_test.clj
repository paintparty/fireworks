(ns example.core-test
  (:require [clojure.test :refer [deftest is]]
            [example.core :as core]))

(deftest fizzbuzz-test
  (is (= ["FizzBuzz" "Buzz" "Fizz" 7]
         (map core/fizz-buzz [15 5 3 7]))))
