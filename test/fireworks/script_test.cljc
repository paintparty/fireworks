(ns fireworks.script-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.sample]))

(defn main [& cli-args]
  (? "testing from node or deno script"
     fireworks.sample/array-map-of-everything-cljc))
