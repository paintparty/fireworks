(ns visual-testing.shared
  (:require [fireworks.config]
            [fireworks.themes]
            [fireworks.sample :as sample :refer []]
            [bling.core :refer [print-bling bling ?sgr callout]]
            [bling.hifi :refer [hifi print-hifi]]
            [lasertag.core :refer [tag-map]]
            [taoensso.tufte :as tufte :refer [p profile]]
            [fireworks.core :refer [? !? ?> !?> pprint]]))

(def everything sample/array-map-of-everything-cljc)

;; (tufte/add-handler! :my-console-handler (tufte/handler:console))

(defn test-suite []
  #?(:cljs
     ;; Move this interop demo code into fireworks.sample
     (do 

       (js/console.clear)
      ;;  (js/console.log (? :data {:a "foo"
      ;;                            :b 2
      ;;                            :c 3}))
       
       #_(print-hifi {:a "foo"
                      :b 2
                      :c 3})
      ;;  (println (hifi {:a "foo"
      ;;                  :b 2
      ;;                  :c 3}))
       
       
       (callout {:type        :error
                 :label-theme :pipe}
                (bling [:p [:red "This is red" [:bold " and bold."]]]
                       [:p "Line two"]
                       (hifi {:a "foo"
                              :b 2
                              :c 3})))

       
       #_(profile ; Profile any `p` forms called during body execution
          {} ; Profiling options; we'll use the defaults for now
          (dotimes [_ 100]
            #_(p :nested
                 (bling [:p "First paragraph"]
                        [:p [:bold
                             "Bold, "
                             [:italic "bold italic, "
                              [:red "bold italic red, "]]
                             "bold."]]
                        "Last line"))
            (p :mock-node
               (? {:string  "string"
                   :regex   #"myregex"
                   :uuid    #uuid "4fe5d828-6444-11e8-8222-720007e40350"
                   :symbol  'mysym
                   :nesting [[[[[[]]]]]]
                   :vector  (with-meta 
                              ['foo
                               (with-meta 'bar {:meta-on-sym "xyz"})
                               'baz]
                              {:meta-on-coll "abc"})})
               #_(? everything))))

       #_(pprint (:formatted+ (? {:string "string"})))
       #_(print-bling [{:font-weight           :bold
                        :font-style            :italic
                        :color                 :red
                        ;; :background-color :yellow
                        :text-decoration-style :wavy
                        :text-decoration       :underline
                        }
                       "bang"])

       #_(callout {:label       (bling [:bold.black-bg.white " whoa "])
                   :label-theme :pipe
                   :type        :warning}
                  (bling [:p "First paragraph"]
                         [:p [:bold
                              "Bold, "
                              [:italic "bold italic, "
                               [:red "bold italic red, "]]
                              "bold."]]
                         "Last line")
                  #_(bling "First paragraph"
                           "\n\n"
                           [:bold "Bold, "] 
                           [:bold.italic "bold italic, "] 
                           [:bold.red.italic "bold italic red, "] 
                           [:bold.red.italic.yellow-bg "bold italic red, yellow bg"] 
                           [:bold " bold."]
                           "\n\n"
                           [{:href "www.pets.com"} "Pets.com"]
                           "\n\n"
                           "Last line with " [:wavy-underline "wavy-underline"]))
       
       
       
       
       
       
       
       #_(? {:coll-limit 200
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
       

       #_(? {:coll-limit  200
             :label       "Clojure(Script) values"
             :label-color :blue
             ;;  :bold?       true
             }
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
