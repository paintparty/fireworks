(ns visual-testing.shared
 
  (:require [fireworks.config]
            [fireworks.themes]
            [fireworks.core :refer [?]]))

(defrecord Foo [a b])

(def record-sample (->Foo 1 2))

(def foo
  {:string        "string"
   :uuid          #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number        1234
  ;;  :symbol          (with-meta 'mysym {:foo :bar})
   :symbol        {:foo :bar}
   :boolean       true
   :lamda         #(inc %)
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
             :lamda              #(inc %)
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
  (? {:coll-limit 30} basic-samples-cljc)
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
                  :lamda              #(inc %)
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



  nil
  )





