(ns visual-testing.shared
 
  (:require [fireworks.config]
            [fireworks.themes]
            [lasertag.core]
            [fireworks.core :refer [? pprint]]))

(defrecord Foo [a b])

(def record-sample (->Foo 1 2))

(def foo
  {:string        "string"
   :uuid          #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number        1234
  ;;  :symbol          (with-meta 'mysym {:foo :bar})
   :symbol        {:foo :bar}
   :boolean       true
   :lambda         #(inc %)
   :fn            juxt
   :regex         #"^hi$"
   :record        record-sample
   :atom/number   (atom 1)
   :brackets      [[[[[[]]]]]]
   :map/with-meta (with-meta {:a :foo :b :bar}
                    {:k1 "abcdefghijklmnop" 
                     :k2 "qrstuvwxyz" })
  ;;  :map/with-meta (with-meta {:a :foo
  ;;                               :b 2}
  ;;                     {:a (with-meta (symbol "foo")
  ;;                           {:abc (symbol "foobarbaz")
  ;;                            :xyz "abcdefghijklmnop"})})
   })

(def basic-samples-cljc 
  {:abcdefg {:string             "string"
             :uuid               #uuid "4fe5d828-6444-11e8-8222-720007e40350"
             :number             1234
             :symbol             (with-meta 'mysym {:foo :bar})
             :symbol2            (with-meta 'mysym
                                   {:foo ["afasdfasf"
                                          "afasdfasf"
                                          {:a "foo"
                                           :b [1 2 [1 2 3 4]]}
                                          "afasdfasf"
                                          "afasdfasf"]

                                    :bar "fooz"})
             :boolean            true
             :lambda              #(inc %)
             :fn                 juxt
             :regex              #"^hi$"
             :record             record-sample
             :atom/record        (atom record-sample)
             :atom/number        (atom 1)
             :brackets           [[[[[[]]]]]]
             :map/nested-meta    (with-meta 
                                   {(with-meta (symbol :a)
                                      {:abc "bar"
                                       :xyz "abc"}) (with-meta (symbol "foo")
                                                      {:abc "bar"
                                                       :xyz "abc"})
                                    :b                                               2}
                                   {:a (with-meta (symbol "foo")
                                         {:abc (symbol "bar")
                                          :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})
             :map/single-line    {:a 1
                                  :b 2
                                  :c "three"}
             :map/multi-line     {:abc      "bar"
                                  "asdfasdfa" "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                  [:a :b]   123444}
             :vector/single-line [1 :2 "three"]
             :vector/multi-line  ["abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                  :22222
                                  3333333]
             :set/single-line    #{1 :2 "three"}
             :set/multi-line     #{"abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                   :22222
                                   3333333}}})

(defn test-suite []
  #?(:cljs
     (do

       #_(? #_{:label                      "my-label"
               :enable-terminal-truecolor? true
               :enable-terminal-italics?   true
               :bracket-contrast           "high"
               :theme                      "Alabaster Light"}
            #js [1 2 3])

       #_(? [1 (new js/Int8Array #js[1 2 3])])

       #_(? (lasertag.core/tag #js {:a 1
                                    :b 2}))
       #_(? [1 #js [1 2 3 4 5]])

       #_(? [1 #js ["a" #js [1 2 3] "b"]])

       #_(? [1 (new js/Set #js[1])])
       #_(? [1 #js {:a 1
                    :b 2}])))

  (? {:theme "Alabaster Light"
      :label "basic-samples-cljc"}
   basic-samples-cljc)

  (? foo)

  (? :default foo)

  (? {:theme "Alabaster Light"
      :label "Alabaster Light"} foo)
  (? {:theme "Monokai Light"
      :label "Monokai Light"} foo)
  (? {:theme "Alabaster Dark"
      :label "Alabaster Dark"} foo)
  (? {:theme "Monokai Dark"
      :label "Monokai Dark"} foo)
  (? {:theme "Universal Neutral"
      :label "Universal Neutral"} foo)


;; OFF-start
;; (? {:theme "Zenburn Light" :label "Zenburn Light"} foo)
;; (? {:theme "Solarized Light" :label "Solarized Light"} foo)
;; (? {:theme "Degas Light" :label "Degas Light"} foo)
;; (? {:theme "Neutral Light" :label "Neutral Light"} foo)
  
;; (? {:theme "Alabaster Dark" :label "Alabaster Dark"} foo)
;; (? {:theme "Zenburn Dark" :label "Zenburn Dark"} foo)
;; (? {:theme "Monokai Dark" :label "Monokai Dark"} foo)
;; (? {:theme "Solarized Dark" :label "Solarized Dark"} foo)
;; (? {:theme "Degas Dark" :label "Degas Dark"} foo)
;; (? {:theme "Neutral Dark" :label "Neutral Dark"} foo)
;; OFF-end
  
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
  (? {:single-line-coll-length-limit 50} (range 20))

  (? {:label                        "my-label"
      :enable-terminal-truecolor?   true
      :enable-terminal-italics?     true
      :bracket-contrast             "high"
      :theme                        fireworks.themes/alabaster-light
      :custom-printers              {}
      :coll-limit                   20
      :non-coll-length-limit        (-> fireworks.config/options
                                        :non-coll-length-limit
                                        :default)
      :display-namespaces?          (-> fireworks.config/options
                                        :display-namespaces?
                                        :default)
      :metadata-position            (-> fireworks.config/options
                                        :metadata-position
                                        :default)
      :metadata-print-level         (-> fireworks.config/options
                                        :metadata-print-level
                                        :default)
      :non-coll-mapkey-length-limit (-> fireworks.config/options
                                        :non-coll-mapkey-length-limit
                                        :default)}
     
     (with-meta 
       {:b 2}
       {:a (with-meta (symbol "foo")
             {:abc (symbol "bar")
              :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})
     #_{:abcdefg {:string             "string"
                  :uuid               #uuid "4fe5d828-6444-11e8-8222-720007e40350"
                  :number             1234
                  :symbol             (with-meta 'mysym {:foo :bar})
                  :symbol2            (with-meta 'mysym
                                        {:foo ["afasdfasf"
                                               "afasdfasf"
                                               {:a "foo"
                                                :b [1 2 [1 2 3 4]]}
                                               "afasdfasf"
                                               "afasdfasf"]

                                         :bar "fooz"})
                  :boolean            true
                  :lambda              #(inc %)
                  :fn                 juxt
                  :regex              #"^hi$"
                  :record             record-sample
                  :atom/record        (atom record-sample)
                  :atom/number        (atom 1)
                  :brackets           [[[[[[]]]]]]
                  :map/nested-meta    (with-meta 
                                        {(with-meta (symbol :a)
                                           {:abc "bar"
                                            :xyz "abc"}) (with-meta (symbol "foo")
                                                           {:abc "bar"
                                                            :xyz "abc"})
                                         :b                                               2}
                                        {:a (with-meta (symbol "foo")
                                              {:abc (symbol "bar")
                                               :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})
                  :map/single-line    {:a 1
                                       :b 2
                                       :c "three"}
                  :map/multi-line     {:abc      "bar"
                                       "asdfasdfa" "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                       [:a :b]   123444}
                  :vector/single-line [1 :2 "three"]
                  :vector/multi-line  ["abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                       :22222
                                       3333333]
                  :set/single-line    #{1 :2 "three"}
                  :set/multi-line     #{"abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                        :22222
                                        3333333}}}
     )

  
  (println "\n:Volatile, record")
  (? {:label                      "my-label"
      :enable-terminal-truecolor? true
      :enable-terminal-italics?   true
      :bracket-contrast           "high"}
     (volatile! record-sample))

  (println "\n:Transient Array")
  (let [x (transient [1 2 3 4 5 6 7 8 9 0])]
    (? {:coll-limit                 7
        :label                      "my-label"
        :enable-terminal-truecolor? true
        :enable-terminal-italics?   true
        :bracket-contrast           "high"}
       x)
    (conj! x 5)) 

  (println "\n:Transient Map")
  (let [x (transient {:a 1
                      :b 2
                      :c 3
                      :d 4
                      :e 5
                      :f 6
                      :g 7
                      :h 8
                      :i 9
                      :j 10 })]
    (? {:coll-limit 7} x)
    (assoc! x :k 11))

  (println "\n:Transient Set")
  (let [x (transient #{1 2 3 4 5 6 7 8 9 0})]
    (? {:label "Transient Set" :coll-limit 7} x)
    (conj! x 11))

  nil)



;; For trying stuff out
#_(defn test-suite []

  (? {:label                        "my-label"
      :enable-terminal-truecolor?   true
      :enable-terminal-italics?     true
      :bracket-contrast             "high"
      :custom-printers              {}
      :coll-limit                   20
      :non-coll-length-limit        (-> fireworks.config/options
                                        :non-coll-length-limit
                                        :default)
      :display-namespaces?          (-> fireworks.config/options
                                        :display-namespaces?
                                        :default)
      :metadata-position            (-> fireworks.config/options
                                        :metadata-position
                                        :default)
      :metadata-print-level         (-> fireworks.config/options
                                        :metadata-print-level
                                        :default)
      :non-coll-mapkey-length-limit (-> fireworks.config/options
                                        :non-coll-mapkey-length-limit
                                        :default)} basic-samples-cljc)
  ;; (println "v")
  ;; (pprint (? :data (volatile! record-sample)))
  
 #_#?(:cljs
     (do 
      ;;  (println "atom")
      ;;  (pprint (:formatted (? :data (atom record-sample))))
      ;;  (? (atom record-sample))
       
      ;;  (println "\n\n")
       
      ;;  (println "Volatile")
       (pprint (:formatted (? :data 
                              {:label                      "my-label"
                               :enable-terminal-truecolor? true
                               :enable-terminal-italics?   true
                               :bracket-contrast           "high"}
                              (volatile! record-sample))))
      ;;  (? (volatile! record-sample))
      ;;  (? (lasertag.core/js-object-instance (transient [1 2 3 4])))
       #_(let [x (transient [1 2 3 4 5 6 7 8 9 0])]
         (? {:coll-limit                 7
             :label                      "my-label"
             :enable-terminal-truecolor? true
             :enable-terminal-italics?   true
             :bracket-contrast           "high"}
          x)
         #_(pprint (? :data {:coll-limit                 7
                           :label                      "my-label"
                           :enable-terminal-truecolor? true
                           :enable-terminal-italics?   true
                           :bracket-contrast           "high"}
                    x))
         (conj! x 5))
       
      ;;  (let [x (transient #{1 2 3 4 5 6 7 8 9 0})]
      ;;    (? "Transient set" {:coll-limit 7} x)
      ;;    (conj! x 11))
       
       #_(? {})

       #_(let [x (transient {:a 1
                             :b 2
                             :c 3
                             :d 4
                             :e 5
                             :f 6
                             :g 7
                             :h 8
                             :i 9
                             :j 10 })]
           (? {:coll-limit 7} x)
           (assoc! x :k 11))
       
       #_(? (volatile! record-sample))

       #_(let [x (transient #{:a 1
                            :b 2
                            :c 3
                            :d 4
                            :e 5
                            :f 6
                            :g 7
                            :h 8
                            :i 9
                            :j 10 })]
         (? {:coll-limit 7} x)
         (conj! x 11))
       
       #_(? (transient {:a 1
                        :b 2
                        :c 3
                        :d 4
                        :e 5
                        :f 6
                        :g 7
                        :h 8
                        :i 9
                        :j 10}))
      ;;  (? (lasertag.core/tag-map 1))
      ;;  (js/console.log lasertag.cljs-interop/js-built-in-objects-by-object-map)
      ;;  (js/console.log lasertag.cljs-interop/js-built-ins-by-category)
       
      ;;  (? (lasertag.core/js-object-instance 1))
       
      ;;  (? (transient [1 2 3 4]))
      ;;  (? (type (transient #{1 2 3 4})))
      ;;  (? (type (transient {1 2 3 4})))
      ;;  (? (type (transient {1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 191 20 21 22})))
      ;;  (? (type (transient {1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 191 20 21 22})))
       
      ;;  (? (atom record-sample))
       )
     :clj
     nil)

  nil)




