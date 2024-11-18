(ns fireworks.script-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.smoke-test :as smoke-test]))

(defn main [& cli-args]
  (? "testing from node or deno script"
     smoke-test/basic-samples-cljc))
