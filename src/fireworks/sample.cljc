(ns fireworks.sample
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.themes :as themes]
            [bling.core :refer [callout bling ?sgr]]
            [clojure.string :as string]
            [fireworks.pp :as pp :refer [?pp]]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as walk]
            [fireworks.util :as util]
            [lasertag.core :refer [tag-map tag]]
            #?(:cljs [lasertag.cljs-interop])
            #?(:cljs [cljs.js])
            ;; [lambdaisland.ansi :as ansi]
            )
            
  #?(:cljs
     (:require-macros [fireworks.sample :refer [qc]])
     ))

;; Definitions to use in samples -----------------------------------------------

(deftype MyType [a b])
(def my-data-type (->MyType 2 3))
(defrecord MyRecordType [a b])
(def my-record-type (->MyRecordType "a" "b"))
(defmulti different-behavior (fn [x] (:x-type x)))
(defmethod different-behavior :wolf
  [x]
  (str (:name x) " will have a specific behavior"))
(defn xy [x y] (+ x y))
(defn xyv ([x y] (+ x y)) ([x y v] (+ x y v)))
(defn xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz [x y] (+ x y))
(def my-date (new #?(:cljs js/Date :clj java.util.Date)))

;;  (def my-prom (js/Promise. (fn [x] x)))
;;  (def prom-ref js/Promise)

;; For producing example :call field in metadata of examples
(defrecord QuotedCall [qx x])

#?(:clj
   (defmacro qc
     "Wrap a call in a record:
      (qc (+ 1 1))
      =>
      #fireworks.sample/QuotedCall{:qx '(+ 1 1) :x 2}"
     [coll]
     `(do (->QuotedCall (quote ~coll) ~coll))))



;; Interop data ----------------------------------------------------------------

#?(:cljs
    (defn cljs-interop-classes []
       (let [ks (keys lasertag.cljs-interop/js-built-ins-by-category)]
         (reduce
          (fn [acc [k v]]
            (assoc acc
                   k
                   (apply array-map
                          (reduce (fn [acc [jsf {:keys [sym
                                                        demo
                                                        args
                                                        not-a-constructor?]}]]
                                    (conj acc
                                          sym
                                          (if (and (not not-a-constructor?)
                                                   demo)
                                            (let [res (volatile! nil)]
                                              (cljs.js/eval-str
                                               (cljs.js/empty-state)
                                               demo
                                               nil
                                               {:eval       cljs.js/js-eval    
                                                :source-map true
                                                :context    :expr}
                                               (fn [x]
                                                 (vreset! res (:value x))))
                                              @res)
                                            ::no-demo
                                            )))
                                  []
                                  v))))
          {}
          lasertag.cljs-interop/js-built-ins-by-category
          #_(do 
            (select-keys
               lasertag.cljs-interop/js-built-ins-by-category
               [
                ;; "Fundamental"
                ;; "objects"
                ;; "Numbers and dates"
                ;; "Value properties"
                ;; "Control abstraction objects"
                ;; "Error objects"
                ;; "Text processing"
                ;; "Function properties"
                ;; "Keyed collections" 
                "Indexed collections"
                ;; "Structured data"
                ;; "Internationalization"
                ;; "Managing memory"
                ;; "Reflection"
                ]))))

       #_(doseq [[k v] lasertag.cljs-interop/js-built-in-objects-by-object-map]
           (? :result k)
           (? :result v)
           ))
    :clj
    ())

(def interop-types
#?(:cljs ;; we get these from lasertag.cljs-interop
   (cljs-interop-classes)
   :clj ;; TODO - maybe move these into a new cljc lasertag.interop ns
   (array-map 

    "Java collection types"
    {:java.util.ArrayList (java.util.ArrayList. (range 6))
     :java.util.HashMap (java.util.HashMap. {"a" 1 "b" 2})
     :java.util.HashSet (java.util.HashSet. #{"a" 1 "b" 2})
     :java.lang.String (java.lang.String. "welcome")
     :array (to-array '(1 2 3 4 5))}

    "Java numbers"
    (array-map
      :ratio               1/3
      :byte                (byte 0)
      :short               (short 3)
      :double              (double 23.44)
      :decimal             1M
      :int                 1
      :float               (float 1.50)
      :char                (char 97)
      :java.math.BigInteger (java.math.BigInteger. "171")))))



;; Sample data -----------------------------------------------------------------

(def everything*
  (array-map

   "Primitives"
   (array-map
    :string   "string"
    :regex    #"myregex"
    :uuid     (qc #uuid "4fe5d828-6444-11e8-8222-720007e40350")
    :symbol   'mysym
    :symbol+meta   (with-meta 'mysym {:foo "bar"})
    :boolean  true
    :keyword  :keyword
    :nil      nil
    :##Nan    ##NaN
    :##Inf    ##Inf
    :##-Inf   ##-Inf
    )


   "Number types"
   (array-map
    :int      1234
    :float    3.33)


   "Functions"    
   (array-map
    :lambda            #()
    :lambda-2-args     #(+ % %2) 
    :core-fn           juxt 
    :date-fn   #?(:cljs
                  js/Date
                  :clj
                  java.util.Date)
    :datatype-class   MyType
    :recordtype-class MyRecordType
    :really-long-fn   xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz)

   "Collections"  
   (array-map
    :map
    {:a 1
     :b 2
     :c "three"}

    :rainbow
    [1 2 3]

    :vector    
    [1 2 3]

    :vector+meta
    ^{:meta-on-coll "bar"}
    ['foo
     (with-meta 'bar {:meta-on-sym "bar"})
     'baz]

    :set
    #{1 2 3}

    :list
    '(1 2 3)

    :lazy-seq
    (range 10)

    :record
    my-record-type

    ;; Leave off until you fix this
    ;; :datatype
    ;; my-data-type

    :set
    #{1 :2 "three"})


   "Multi-line collections"
   (array-map
    :map/multi-line
    {"asdfasdfa" "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
     [:a :b]   123444}

    :array-map
    (apply array-map
           (mapcat (fn [x y] [x y])
                   (range 12)
                   ["a" "b" "c" "d" "e" "h" "i" "j" "k" "l" "m" "n"]))

    :vector/multi-line  
    ["abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
     :22222
     3333333]

    :lazy-seq
    (range 20)

    :truncation-candidate
    [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23]
    
    :set/multi-line
    #{"abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
      :22222
      3333333})


   "Map keys"
   (array-map
    +                              "core function"
    "really-long-string-abcdefghi" "really-long-string-abcdefghi"
    [1 2]                          "vector"
    #{1 2 3}                       "set"
    'symbol                        "symbol"
    (with-meta 'mysym
      {:a "foo"
       :b [1 2 [1 2 3 4]]})        "symbol with meta"
    #"^hi$"                        "regex"
    #"really-long-regex-abcdefghi" "long regex"
    1                              "number" 
    nil                            "nil" 
    ;; MyType               MyType
    ;; my-data-type         my-data-type
    ;; MyRecordType         MyRecordType
    ;; :really-long         xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz
    )


   "Abstractions"
   (array-map
    :atom             (qc (atom 1))
    :date             my-date
    :volatile!        (volatile! 1)
    :transient-vector (transient [1 2 3 4])
    :transient-set    (transient #{:a 1})
    :transient-map    (transient {1 2 3 4})
    )


   "With meta"
   (array-map
    :nested-meta/sym (with-meta (symbol (str "foo"))
                          (with-meta {:l1-k1 (with-meta 'a
                                               {:l2-k1 'a
                                                :l2-k2 'b})
                                      :l1-k2 'b}
                            {:l1-coll "key"}))
    :meta/sym  (with-meta (symbol (str "foo"))
                    (apply array-map
                           (mapcat (fn [x y] [x y])
                                   (range 3)
                                   (repeat :foo))))
    :meta/vector  ^{:foo :bar} [:foo :bar :baz]
    :meta/vector* ^{:foo :bar} [(with-meta (symbol (str "foo"))
                                  {:bar :baz})
                                (with-meta (symbol (str "bar")) 
                                  {:bar :baz})
                                (with-meta (symbol (str "baz"))
                                  {:bar :baz})]
    :meta/map     ^{:foo :bar} {:one :two}
    :meta/map*    ^{:foo :bar} {(with-meta (symbol (str "foo"))
                                  {:bar :baz})
                                (with-meta (symbol (str "bar"))
                                  {:bar :baz})

                                (with-meta (symbol (str "bang"))
                                  {:bar :baz})
                                (with-meta (symbol (str "bop"))
                                  {:bar :baz})})))


;; Macros and functions to produce samples -------------------------------------

(defn tt*
  [x
   {:keys [show-extras?
           show-extras-as-meta?
           extras-keys]}]
  (let [[x call] (if (instance? QuotedCall x)
                   [(:x x) (:qx x)]
                   [x nil])
        extras   (when show-extras?
                   (merge (tag-map x)
                          (when call {:call call})))
        extras   (or (some->> extras-keys
                              seq
                              (select-keys extras))
                     extras)
        x        (if (and show-extras? show-extras-as-meta?)
                   (if (util/carries-meta? x)
                     x
                     (-> x str symbol))
                   x)]
    (cond (and show-extras?
               show-extras-as-meta?)
          (with-meta x extras)

          show-extras?
          [x extras]

          :else
          x)))


(defn everything-with-extras
  [coll
   {:keys [show-extras?
           extras-keys]
    :as   opts}]
  (let [coll (for [x coll] (tt* x opts))]
    (if show-extras?
      (apply array-map
             (reduce (fn [acc [k v]] (conj acc k v))
                     []
                     coll))
      (into [] coll))))


(defn show-everything 
  [coll*
   cats
   {:keys [as-vec? conj-to-vec-ks]
    :as   opts}]
  (let [opts (when as-vec? opts)
        coll (everything-with-extras
              (apply concat
                     (for [k cats]
                       (if as-vec?
                         (some-> (get coll* k nil) vals)
                         (get coll* k nil))))
              opts)]
    (if as-vec?
      (if (seq conj-to-vec-ks)
        (apply conj
               coll
               (for [k conj-to-vec-ks]
                 (get coll* k nil)))
        (do coll))
      (apply array-map
             (reduce (fn [acc [k v]]
                       (conj acc
                             k
                             (if (instance? QuotedCall v)
                               (:x v)
                               v)))
                     [] 
                     coll)))))


;; TODO - finish making all these into callable functions
;;      - version of one with categories as keys
(def array-map-of-everything-cljc
  (show-everything everything*
                   [
                    "Primitives"
                    "Number types"
                    "Functions"
                    "Collections"
                    ;; "Multi-line collections"
                    ;; "Map keys"
                    "Abstractions"
                    ;; "With meta"
                    ] 
                   ;; TODO - maybe blank map or no map?
                   {:as-vec?              false
                    :show-extras?         false
                    :show-extras-as-meta? false
                    :extras-keys          [:tag :call]}))

(def array-map-of-multiline-formatting-cljc
  (show-everything everything*
                   [
                    "Multi-line collections"
                    "With meta"
                    ] 
                   ;; TODO - maybe blank map or no map?
                   {:as-vec?              false
                    :show-extras?         false
                    :show-extras-as-meta? false
                    :extras-keys          [:tag :call]}))


(def vec-of-everything-cljc
  (show-everything everything*
                   [
                    "Primitives"
                    "Number types"
                    "Functions"
                    "Collections"
                    "Map keys"
                    "Abstractions"
                    "With metadata"
                    ]
                   {:as-vec?              true
                    ;; :conj-to-vec-ks       [:map-keys]
                    :show-extras?         false
                    :extras-keys          [:tag :call]}))

(defn vec-of-everything-cljc-with-extras []
  (show-everything everything*
                   [
                    ;; "Primitives"
                    ;; "Number types"
                    ;; "Functions"
                    ;; "Collections"
                    ;; "Map keys"
                    "Abstractions"
                    ;; "With metadata"
                    ] 
                   {:as-vec?              true
                    :show-extras?         true
                    :extras-keys          [:tag :call :all-tags]}))

(def vec-of-interop-types-with-extras
  (show-everything interop-types
                   #?(:cljs
                      []
                      :clj
                      [
                      ;;  "Java collection types"
                       "Java numbers"
                       ])
                    
                   {:as-vec?              true
                    :show-extras?         true
                    :extras-keys          [:tag :call :all-tags]}))
