(ns example.hello-test
  (:require [example.hello :as hello]
            [clojure.test :refer [deftest is]]))

(deftest greets-you
  (is (= "Hello There" (hello/greet "There"))))

(deftest equals
  (is (= 1 2)))
