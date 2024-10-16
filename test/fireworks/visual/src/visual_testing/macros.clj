(ns visual-testing.macros
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [visual-testing.shared :refer [test-suite]]))

(defmacro test-clj []
  (test-suite)
  ;; TODO add clj-specific test
  nil)
