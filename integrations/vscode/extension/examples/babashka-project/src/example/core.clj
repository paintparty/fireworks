(ns example.core
  (:require [fireworks.core :refer [? !? ?> !?>]]))

(defn fizz-buzz [n]
  (cond
    (? (zero? (mod n (* 5 3))))
    "FizzBuzz"

    (? (zero? (mod n 5)))
    "Buzz"

    (? (zero? (mod n 3)))
    "Fizz"

    :else n))


;; With Fireworks Live Code running, save this file.
;; Each top-level `?` form re-runs and its value paints inline.

;; Toggle a `?` wrap with `cmd/ctrl + '`
;; If caret is on the  `?` form itself, the command toggles `?` ↔ `!?` (loud ↔ silent)
;; Change the default keybinding to suit your needs.

(? (fizz-buzz 15))

;; Change the arg in the above call to fizz-buzz from `7` to `15`,
;; the inline results up in fizz-buzz defn should change as well.


;;----------------------

;; An example of using `(?)` to display inline results within trace form
;; Bonus: `cmd/ctrl + ;` (command + semicolon). This will toggle `#_` or form.
#_(? (->> (range 1 20)
          (?)
          (filter even?)
          (?)
          (reduce +)))
