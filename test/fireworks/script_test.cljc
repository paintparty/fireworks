(ns fireworks.script-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [hifi.sample]))

(defn main [& cli-args]
  (? "testing from node or deno script"
     hifi.sample/array-map-of-everything-cljc))
