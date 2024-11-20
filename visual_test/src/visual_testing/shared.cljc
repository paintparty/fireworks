(ns visual-testing.shared
  (:require [fireworks.config]
            [fireworks.themes]
            [fireworks.sample :as sample :refer []]
            [bling.sample]
            [lasertag.core :refer [tag-map]]
            [fireworks.core :refer [? !? ?> !?> pprint]]))

(def everything sample/array-map-of-everything-cljc)

(defn test-suite []
  #?(:cljs
     ;; Move this interop demo code into fireworks.sample
     (do #_(? {:coll-limit 200
             :label      "ClojureScript interop types"}
            sample/interop-types)
         
         (? {:coll-limit 200
             :label      "Clojure(Script) values"}
            everything)

         (!? {:label      "Clojure(Script) multiline formatting"}
            sample/array-map-of-multiline-formatting-cljc)))

  #_(do 
    (? {:theme "Alabaster Light"
        :label "Alabaster Light"} everything)
    (? {:theme "Monokai Light"
        :label "Monokai Light"} everything)
    (? {:theme "Alabaster Dark"
        :label "Alabaster Dark"} everything)
    (? {:theme "Monokai Dark"
        :label "Monokai Dark"} everything)
    (? {:theme "Universal Neutral"
        :label "Universal Neutral"} everything))
  
  #_(do 
    (println "\n:label-length-limit of 10")
    (? {:label-length-limit 10} (str "1234567890" "abcdefghijklmnopqrstuvwxyz"))

    (println "\n:label-length-limit of 50")
    (? {:label-length-limit 50} (str "1234567890"
                                     "abcdefghijklmnopqrstuvwxyz"
                                     "ABCDEFGHIJKLMNOP"))

    (println "\n:single-line-coll-length-limit of 10")
    (? {:single-line-coll-length-limit 10} (range 4))

    (println "\n:single-line-coll-length-limit of 10")
    (? {:single-line-coll-length-limit 10} (range 5))

    (println "\n:single-line-coll-length-limit of 50")
    (? {:single-line-coll-length-limit 50} (range 19))

    (println "\n:single-line-coll-length-limit of 50")
    (? {:single-line-coll-length-limit 50} (range 20)))

  ;; Test bling
  (bling.sample/sample)
  nil)
