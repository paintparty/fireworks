(ns fireworks.bb-test
  (:require
   [fireworks.core :refer [? !? ?> !?>]]
   [clojure.test :refer [deftest is]]))


;; Basic tapping macros ---------------------------------------------------------

(deftest !?-par (is (= (!? "foo") "foo")))
(deftest ?>-par (is (= (?> "foo") "foo")))
(deftest !?>-par (is (= (!?> "foo") "foo")))


;; ? returns its value and prints without error ---------------------------------

(deftest ?-returns-value
  (is (= {:a 1}
         (let [ret (atom nil)]
           (with-out-str (reset! ret (? {:a 1})))
           @ret))))

(deftest ?-data-mode
  (is (= :fooo (:quoted-form (? {:data? true} :fooo)))))
