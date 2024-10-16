(ns visual-testing.shared
  (:require [fireworks.core :refer [? !? ?> !?>]]))

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

(defn test-suite []
  (? foo)
  (? :default foo)
  (? {:theme "Alabaster Light"
      :label "Alabaster Light"} foo)
  (? {:theme "Monokai Light"
      :label "Monokai Light"} foo)

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
  nil)
