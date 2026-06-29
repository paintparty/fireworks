(ns example.core
  (:require [fireworks.core :refer [? !? ?> !?>]]))

(defn greet
  "Build a greeting string."
  [name]
  (str "Hello, " name "!"))

(defn fib
  "The nth Fibonacci number."
  [n]
  (loop [a 0 b 1 n n]
    (if (zero? n) a (recur b (+ a b) (dec n)))))

;; With Fireworks Live Code running, save this file. Each top-level `?` form
;; re-runs and its value paints inline at the end of the line. Toggle a wrap
;; with cmd/ctrl + '.
(? (greet "Fireworks"))

(? (map fib (range 10)))

(? (->> (range 1 20)
        (filter even?)
        (reduce +)))
