(ns visual-testing.shared
  (:require [fireworks.config]
            [fireworks.themes]
            [fireworks.sample :as sample :refer []]
            [lasertag.core :refer [tag-map]]
            [fireworks.core :refer [? !? ?> !?> pprint]]))

(def everything sample/array-map-of-everything-cljc)

(defn test-suite []
  #?(:cljs
     ;; Move this interop demo code into fireworks.sample
     (do #_(? {:coll-limit 200
               :label      "ClojureScript interop types"}
              sample/interop-types)

         #_(let [buffer      (new js/ArrayBuffer 8)
                 arr         (new js/Int32Array buffer)
                 js-array    (array "a" "b")
                 i8-array    (new js/Int8Array #js[1 2 3])

                 js-map      (new js/Map #js[#js["a", 1], #js["b", 2]])
                 js-set      (new js/Set #js[1 2])
                 js-weak-map (let [wm (js/WeakMap.)
                                   o  #js{:a 1}]
                               (.set wm o 100))
                 js-weak-set (new js/WeakSet #js[#js{"a" 1} #js{"b" 2}])]


             #_(js/console.log buffer)
             #_(pprint (tag-map buffer))
             #_(pprint (tag-map arr))

        ;; (js/console.log js-array)
        ;; (pprint (tag-map js-array))
        ;; (pprint (array? i8-array))
        ;; (pprint (tag-map i8-array))
        ;; (pprint (tag-map js-map))
             
        ;; (pprint (tag-map js-set))
             
        ;; (js/console.log js-weak-map)
        ;; (pprint (tag-map js-weak-map))
        ;; (js/console.log js-weak-set)
        ;; (pprint (tag-map js-weak-set))
             
        ;; (pprint (tag-map [1 2 3]))
             
        ;;  (pprint (tag-map js/JSON
        ;;                     {
        ;;                     ;; :include-js-built-in-object-info? false
        ;;                     ;; :exclude [:function-info]
        ;;                      }))
             )
         

         (? {:coll-limit  200
             :label       "Clojure(Script) values"
             :label-color :blue
             :bold?       true}
            everything)

         #_(? {:label "Clojure(Script) multiline formatting"}
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

  nil)
