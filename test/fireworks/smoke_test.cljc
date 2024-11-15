;; Unstructured sandbox for smoke testing

(ns fireworks.smoke-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.themes :as themes]
            [bling.core :refer [callout bling ?sgr]]
            [clojure.string :as string]
            [fireworks.pp :as pp]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as walk]
            [fireworks.util :as util]
            [lasertag.core :refer [tag-map tag]]
            ;; [lambdaisland.ansi :as ansi]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))

(def theme themes/alabaster-light)

;; testing label length
;; (? (str "asdfasdfasdfasdfadsfasfasdf" "zzzzzzzzzzzzz" "xxxxxxx"))

;; (array-map "one" 1 "two" 2 "three" 3 "four" 4 "five" 5 "six" 6 "seven" 7 "eight" 8 "nine" 9)

;; This is example config. If you want to run fireworks.core-test tests locally,
;; replace the config map in your ~/.fireworks/config.edn with this map temporarily.
;; If you don't do this, the tests will break.
;; TODO - Fix the above situation.
{:theme                        "Alabaster Light"
 :mood                         :light
 :line-height                  1.45
 :print-level                  7
 :non-coll-length-limit        33
 :non-coll-mapkey-length-limit 20
 :coll-limit                   15
 :evaled-form-coll-limit       7
 :display-namespaces?          true
 :metadata-print-level         7
 :display-metadata?            true
 :metadata-position            :inline 
 :enable-rainbow-brackets?     true
 :bracket-contrast             :high
 :enable-terminal-truecolor?   true
 :enable-terminal-italics?     true
 :custom-printers              nil
 :find                         nil}


;; Formatting

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

#?(:cljs
   (do
     (do
         (def my-date (new js/Date))
         (def my-prom (js/Promise. (fn [x] x)))
         (def prom-ref js/Promise)

         (def cljs-datatypes
           [my-data-type
            my-record-type])

         (def cljs-functions
           [MyType
            MyRecordType
            different-behavior
            xy
            xyv
            xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz
            #(inc %)
            (fn [a] (inc a))
            (fn incr [a] (inc a))
            juxt
            js/Date
            js/Array])

         (def js-number-types
           {##NaN      ##NaN
            ##Inf      ##Inf
            ##-Inf     ##-Inf
            :js/BigInt (js/BigInt 171)
            :int       1
            :float     1.50})))
   :clj
   (do
    (def my-date (java.util.Date.))
     (def number-types
       {:nan                  ##NaN
        :inf                  ##Inf
        :-Inf                 ##-Inf
        1/3                   1/3
        :byte                 (byte 0)
        :short                (short 3)
        :double               (double 23.44)
        :decimal              1M
        :int                  1
        :float                (float 1.50)
        :char                 (char 97)
        :java.math.BigInteger (java.math.BigInteger. "171")})))

(def everything
  {:primitives   {:a                              9999
                  :bbb                            "asdfasdfasdfjasdfasdfasdfasdfa"
                  "really-long-string-abcdefghi"  "really-long-string-abcdefghi"  
                  1                               2
                  "string"                        "hello"
                  :keyword                        :keyword
                  true                            false
                  nil                             nil
                  'symbol                         'symbol
                  #"^hi$"                         #"^hi$"
                  #"really-long-string-abcdefghi" #"really-long-string-abcdefghi"}
   :functions    {(symbol "(fn [])")   #()
                  (symbol "#(+ % %2)") #(+ % %2) 
                  +                    -
                  MyType               MyType
                  my-data-type         my-data-type
                  MyRecordType         MyRecordType
                  :really-long         xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz}
   :collections  {:vector        [1 2 3]
                  :set           #{1 2 3}
                  :list          '(1 2 3)
                  :seq           (range 10)
                  :record        my-record-type
                  ;; :truncation-candidate [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23]
                  :colls-as-keys {[1 2]    "afjasljfalsjdalsfk"
                                  #{1 2 3} "afsdfasdfsdfasfas"}}
   :abstractions {:atom (atom 1)
                  :date my-date}
   :with-meta    {:symbol  (with-meta (symbol (str "foo"))
                             (into {}
                                   (map (fn [x y] [x y])
                                        (range 20)
                                        (repeat :foo))))
                  :vector  ^{:foo :bar} [:foo :bar :baz]
                  :vector* ^{:foo :bar} [(with-meta (symbol (str "foo"))
                                           {:bar :baz})
                                         (with-meta (symbol (str "bar")) 
                                           {:bar :baz})
                                         (with-meta (symbol (str "baz"))
                                           {:bar :baz})]
                  :map     ^{:foo :bar} {:one :two}
                  :map*    ^{:foo :bar} {(with-meta (symbol (str "foo"))
                                           {:bar :baz})
                                         (with-meta (symbol (str "bar"))
                                           {:bar :baz})

                                         (with-meta (symbol (str "bang"))
                                           {:bar :baz})
                                         (with-meta (symbol (str "bop"))
                                           {:bar :baz})}}
   :number-types #?(:cljs js-number-types
                    :clj nil)})

(defrecord Foos [a b])

(def record-sample (->Foos 1 2))

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

(def basic-samples-cljc-theme
  {:string        "string"
   :uuid          #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number        1234
   :symbol-w-meta (with-meta 'mysym {:foo :bar})
   :boolean       true
   :lambda        #(inc %)
   :fn            juxt
   :regex         #"^hi$"
   :record        record-sample
   :atom/number   (atom 1)
   :brackets      [[[[[[]]]]]]
   :map/with-meta (with-meta {:a :foo
                              :b :bar} {:k1 "abcdefghijklmnop"
                                        :k2 "qrstuvwxyz"})
  ;;  :map/nested-meta (with-meta {:a :foo
  ;;                               :b 2}
  ;;                     {:a (with-meta (symbol "foo")
  ;;                           {:abc (symbol "bar")
  ;;                            :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})
   })


;; (?
;;  "Nested metadata, on val"
;;  ^{:a (with-meta (symbol "foo")
;;         {:for  'foo
;;          :desc "meta for foo"})
;;    :b 'bar}
;;  [1 2 3])

;; (?
;;  "Nested metadata, on key"
;;  ^{(with-meta (symbol "a")
;;      {:for  'a
;;       :desc "meta for a"})
;;    'foo

;;    :b
;;    'bar}
;;  [1 2 3])

;; (?
;;  "Nested metadata, on key and val"
;;  ^{(with-meta (symbol "a")
;;      {:for  'a
;;       :desc "meta for a"})
;;    (with-meta (symbol "foo")
;;         {:for  'foo
;;          :desc "meta for foo"})

;;    :b
;;    'bar}
;;  [1 2 3])

#_(with-meta 
  {(with-meta (symbol :a)
     {:abc "bar"
      :xyz "abc"}) (with-meta (symbol "foo")
                     {:abc "bar"
                      :xyz "abc"})
   :b                                               2}
  {:a (with-meta (symbol "foo")
        {:abc (symbol "bar")
         :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})

;; (? record-sample)

;; Testing :non-coll-mapkey-length-limit
;; (? {:non-coll-mapkey-length-limit 40} {"12345678_112345678_212345678_312345678_4" :gold})
;; Testing print level truncation syntax
#_(? {:print-level 1} {:a {:b {:a 1 :b 2}
                         :c #{1 2 3 4 5}}})

;; (? basic-samples-cljc)

(defn co [k commentary]
  (callout {:border-weight :medium
            :type          :magenta
            :label         k
            :margin-bottom 1}
           commentary))

(defn co2 [label form]
  (callout {:type          :subtle
            :border-weight :thin
            :label         (bling [:italic label])
            :margin-bottom 0}
           (bling [:italic.subtle form])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY START
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO - Add more commentary and thorough examples

#_(do 
  (co '? "Basic functionality for `?`")
  (? "? : Default" {:a "foo"})
  (? "? : Default:\nLine1\nLine2" {:a "foo"})
  (? {:label "with options"} (atom {:a "foo"
                                    :b 12}))
  (? (def x1 "x1"))
  (? {:label "def with label from options"} (def x2 "x2"))


  (co :label "Only display label (or form), no file info.")
  (? :label "my label" {:a "foo"})
  (when (= {:a "foo"}
           (? :label
              {:margin-bottom 0}
              {:a "foo"}))
    (println "✓"))
;; (? :l "?l : Default:\nLine1\nLine2" {:a "foo"})
;; (? :l {:label "with options" } (atom {:a "foo" :b 12}))
;; (? :l (def x3 "x3"))
;; (? :l {:label "def with label from options"} (def x4 "x4"))
  
  (co :file "Only display file-info, no label (or form).\nReturns result.")
  (? :file
     {:a "foo"})
  (when (= {:a "foo"}
           (? :file
              {:label         "?i : Just the namespace info"
               :margin-bottom 0}
              {:a "foo"}))
    (println "✓"))
;; (? :i {:a "foo"})
;; (? :i {:theme "Neutral Light"} (atom {:a "foo" :b 12}))
;; (? :i (def x5 "x5"))
;; (? :i {:theme "Neutral Light"} (def x6 "x6"))
  
  (co :result "Only display result.\nReturns result.")
  (? :result {:a "foo"})
  (? :result "? : Default" {:a "foo"})
  (when (= (? :result {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))
;; ;; (? :- "? : Default:\nLine1\nLine2" {:a "foo"})
;; ;; (? :- {:label "with options"} (atom {:a "foo" :b 12}))
;; ;; (? :- (def x1 "x1"))
;; ;; (? :- {:label "def with label from options"} (def x2 "x2"))
  
  (co :comment "Only display comment and file.\nDoes NOT return result.")
  (? :comment "my comment") 
  (? :comment {:margin-bottom 0} "my comment") 
  

  (co :log "Uses js/console.log or pprint.\nReturns result.")
  (? :log "my label" {:a "foo"})
  (when (= (? :log {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))

  ;; ;; (? :log "?log : Default:\nLine1\nLine2" {:a "foo"})
  ;; ;; (? :log {:label "with label from options" } (atom {:a "foo" :b 12}))
  ;; ;; (? :log (def x7 "x7"))
  ;; ;; (? :log {:label "def with label from options"} (def x8 "x8"))
  

  (co :pp "Uses pprint.\nReturns result.")
  (? :pp "my label" {:a "foo"})
  (when (= (? :pp {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))

  (co :log- "Just prints result, with js/console.log or pprint.\nReturns result.")
  (? :log- "my label" {:a "foo"})
  (when (= (? :log- {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))
  ;; (? :log- {:a "foo"})
  ;; ;; (? :log- {:a "foo"})
  ;; ;; (? :log- (atom {:a "foo" :b 12}))
  ;; ;; (? :log- (def x10 "x9"))
  
  (co :pp- "Just prints result, with js/console.log or pprint.\nReturns result.")
  (? :pp- "my label" {:a "foo"})
  (when (= (? :pp- {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))
  
  (co :data "Prints a data representation of the format.\nDoes NOT returns result.")
  (pprint (? :data {:a "foo"}))
  (when (= (? :data {:margin-bottom 0} {:a "foo"})
           {:a "foo"})
    (println "✓"))
  
  (co :trace "Trace form.\nReturns result.")
  (co2 "Default" '(? :trace (-> 1 (+ 3))))

  (? :trace
     (-> 1 (+ 3)))


  (co2 "With user label"
       '(? :trace "My label" (-> 1 (+ 3))))
  (? :trace
     "My label"
     (-> 1 (+ 3)))

  (co2 "With longer threading form"
       '(? :trace
           (-> 1 (+ 3) (repeat 5 5555555))))
  (? :trace
     (-> 1 (+ 3) (cons (repeat 5 5555555))))
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY END
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;





;; (pprint (? :data {:label "my label from opts"} "foo"))
;; (pprint (? :data "my-label" "foo"))
;; (? :label "my-label" "foo")

;; (? "my-label" "foo")

  ;; (println (? :comment "dude")) 
  ;; (println (? :comment {:margin-bottom 0} "dude")) 



;; (? "hiasdfasdfafsadfasdfasdfasdf asdfasdfasdf asdfasdfasdfa fasdfasdfasf" (+ 1 1))



;; (println)

;; (? :trace
;;  (-> 1 (+ 3)))

;; (println (bling [:italic.magenta "with xtra arg"]))

;; (? :trace
;;  "my label"
;;  (-> 1 (+ 3)))

;; (callout {:border-weight :medium :type :magenta}
;;          (bling [:bold.magenta :trace-error]))
;; (?trace 
;;  (let [[a & rest] ["a" "b" "c" "d" "e"]]
;;    [a rest]))


;; (?-- "Commentary")
;; (?-- "Commentary, multiline:\nLine2\nLine3")


;; (?let [[a c] ["4" 5]
;;        b 2]
;;       {:a a :b b :c c})

;; (?let [[a & b] [1 2 3 4 5]]
;;       {:a a :b b})

;; (?let [a                                       (take 5 (range))
;;        {:keys [b c d]
;;         :or   {d 10
;;                b 20
;;                c 30}}                          {:c 50
;;                                                 :d 100}
;;        [e f g &  h]                             ["a" "b" "c" "d" "e"]]
;;       [a b c d e f g h])


;; #?(:clj
;;    (? (java.util.ArrayList. (range 6)))
;;    #_(? (some seq? [(int-array [4 5 6]) (java.util.ArrayList.) (java.util.HashMap.)])))


;; (?trace 
;;  (let [[a & rest] ["a" "b" "c" "d" "e"]]
;;    [a rest]))

;; (?trace 
;;  (-> 1 (+ 3)))


;; (?let [[a b c] ["a" "b" "c" "d" "e"]]
;;       [a b c])

;; #_(?let {:coll-limit 5}
;;       [a (range 10)
;;        b (range 33 200 4)])

;; (? {:print-level ["foo"]} :HI)

(def person
  {:name     "Mark Volkmann"
   :address  {:street "644 Glen Summit"
              :city   "St. Charles"
              :state  "Missouri"
              :zip    63304}
   :employer {:name    "Object Computing, Inc."
              :address {:street "12140 Woodcrest Dr."
                        :city   "Creve Coeur"
                        :state  "Missouri"
                        :zip    63141}}})

#_(? (-> person 
       ?
       :employer
       ?
       :address
       ?
       :city
       ?))

;; (? (?some->> person :employer :address :city .toUpperCase))

;; (?trace {:coll-limit 2} (some-> person :employer :address :city .toUpperCase))

;; (?let [a {:a 12 :b 44 :c "asfdasdfasdfasdfasf" :d 55}
;;        b {:a 12 :b 44 :c "asfdasdfasdfasdfasf" :x 55}]
;;   a)


#?(:clj
   (do 
     #_(do
         ;; DataTypes ------------------------------------------------------
         (? "A def of deftype" (deftype MyType [a b]))
         
         (? "A deftype" MyType)
         (def my-data-type (->MyType 2 3))
         (? "Instance of deftype" my-data-type)


         ;; RecordTypes ----------------------------------------------------
         (defrecord MyRecordType [a b c d])
         (? "A record type" MyRecordType)
         (defrecord MySubRecordType [x y])
         (def my-record-type (->MyRecordType 
                              (->MySubRecordType "adfasdfasdfasdfasdf" "y")
                              #_"asdfasdfasdfsafasdfadf"
                              "xyxyxyxyxyxxyxxy"
                              123456
                              juxt))
         (? "Instance of record type" MyRecordType)


         ;; Multimethods ---------------------------------------------------
         (defmulti different-behavior (fn [x] (:x-type x)))
         (defmethod different-behavior :wtf
           [x]
           (str (:name x) " will have a specific behavior"))
         (? "Multimethod" different-behavior)


         ;; Dates ----------------------------------------------------------
         (def my-date (java.util.Date.))
         (? "A java date" my-date)


         ;; Function -------------------------------------------------------
         (defn xy [x y] (+ x y))
         (? "2-arity function" xy)

         (defn xy-variadic [x y & args] (+ x y))
         (? "variadic function" xy-variadic)


         ;; Collections ----------------------------------------------------
         (? "anonymous function" (fn xy-variadic []))
         (? "anonymous function sugared" #(inc %))
         (? "vector"               [1 2 3])
         (? "set"                  #{1 2 3})
         (? "list"                 '(1 2 3))
         (? "seq"                  (range 10))
         (? "record"               my-record-type)
         (? {:coll-limit 5 :label "truncation"} [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23])
         (let [o {:coll-limit 5 :label "truncation with opts var"}]
           (? o [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23]))
         (? "colls-as-keys"        {[1 2]    "afjasljfalsjdalsfk" #{1 2 3} "afsdfasdfsdfasfas"})


         ;; Abstractions ----------------------------------------------------
         (? "Atom" {:atom (atom 1)})


         ;; Java Number Types -----------------------------------------------
         (? "##NaN" ##NaN)
         (? "##Inf" ##Inf)
         (? "##-Inf" ##-Inf)
         (? "1/3" 1/3)
         (? "byte" (byte 0))
         (? "short" (short 3))
         (? "double" (double 23.44))
         (? "decimal" 1M)
         (? "int" 1)
         (? "float" (float 1.50))
         (? "char" (char 97))
         (? "java.math.BigInteger" (java.math.BigInteger. "171"))
         (? {:label      "Basic samples"
             :coll-limit 15
            ;;  :theme      "Alabaster Light"
             :theme      themes/alabaster-light
             :find       {:pred #(= 1234 %)}}
            basic-samples-cljc)




         ;; Custom printing -- ! Leave this off until feature re-implemented
        
        ;;  (? {:custom-printers {:vector {:pred        (fn [x] (= x [765 233 444 21]))
        ;;                                 :f           (fn [x] (into #{} x))
        ;;                                 :badge-text  " Set💩 "
        ;;                                 :badge-style {:color            "#000"
        ;;                                               :background-color "lime"
        ;;                                               :border-radius    "999px"
        ;;                                               :text-shadow      "none"
        ;;                                               :font-style       "normal"}}}
        ;;      }
        ;;     [765 233 444 21])

         ;; Metadata
         (?
            {:label             "Nested metadata, :block positioning"
              :metadata-position :block}
            (with-meta 
                    {(with-meta (symbol :a)
                        {:abc "bar"
                        :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
                      (with-meta (symbol "foo")
                        {:abc "bar"
                        :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
                      :b {:foo 'bar}}
                    {:a                (with-meta (symbol "foo")
                                          {:abc  (with-meta (symbol "bar") {:a 1 #_(with-meta (symbol "foo") {:a 1})})
                                          :xyz  "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                          ;;  "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
                                          })
                      ;; :xyz              "bar" 
                      ;; "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
                      }))

         
         (?
          "Nested metadata, :inline positioning"
          (with-meta 
            {(with-meta (symbol :a)
               {:abc "bar"
                :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
             (with-meta (symbol "foo")
               {:abc "bar"
                :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
             :b {:foo 'bar}}
            {:a                (with-meta (symbol "foo")
                                 {:abc  (with-meta (symbol "bar") {:a 1 #_(with-meta (symbol "foo") {:a 1})})
                                  :xyz  "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
                                          ;;  "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
                                  })
                      ;; :xyz              "bar" 
                      ;; "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
             }))
         )))
  

 ;; Testing all the stock themes cljc
 #_(do
   (doseq [mood ["Light" #_"Dark"]]
     (doseq [theme ["Neutral"
                    "Alabaster"
                    "Zenburn"
                    "Degas"
                    "Solarized"
                    "Monokai"]]
      (let [x (str theme " " mood)]
        (? {:label x :theme x}
           basic-samples-cljc-theme)))))


 ;; Testing all the options cljc
 #_(do 

  ;; :mood
  (? {:label :mood :mood "light" :theme nil}
     (with-meta ["Light"] {:mood :light}))
  (? {:label :mood :mood "dark" :theme nil}
     (with-meta ["Dark"] {:mood :dark}))

  ;; :print-level
  (? {:label :print-level :print-level                7}
     [0 [1 [2 [3 [4 [5 [6 [7 [888 [9]]]]]]]]]])

  ;; :theme
  (? {:label "theme Alabaster Dark" :theme "Alabaster Dark"}
     (atom [[1 2 3] "abcdefghijk"]))
  (? {:label "theme Alabaster Light" :theme "Alabaster Light"}
     (atom [[1 2 3] "abcdefghijk"]))

  ;; :non-coll-length-limit
  (? {:label :non-coll-length-limit :non-coll-length-limit 20}
     "abcdefghijklmnopqrstuvwxyz")


  ;; :non-coll-mapkey-length-limit
  (? {:label :non-coll-mapkey-length-limit :non-coll-mapkey-length-limit         20}
     {"abcdefghijklmnopqrstuvwxyz" [1 2 3] })

  (? {:evaled-form-coll-limit     7}
     [:evaled-form-coll-limit 1 2 3 4 5 6 7 8 9 10])

  (? {:label :display-namespaces? :display-namespaces?        true}
     [juxt my-record-type])

  (? {:label "display-namespaces? false" :display-namespaces?        false}
     [juxt my-record-type])
  
  (? {:label             :metadata-position
      :metadata-position "block"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})
                 (with-meta (symbol "foo") {:my :meta})
                 (with-meta (symbol "foo") {:my :meta})]
       {:mood [1 [2 [3 [4 [5]]]]]})) 

  

  (? {:label                :metadata-print-level
      :metadata-print-level 3
      :metadata-position    "inline"}
     (with-meta [] {:mood [1 [2 [3 [4 [5]]]]]}))

  ;; :display-metadata?
  (? {:label             ":display-metadata? false"
      :display-metadata? false
      :metadata-position "inline"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})] {:mood [1 [2 [3 [4 [5]]]]]}))

  ;; :display-metadata?
  (? {:label             :display-metadata?
      :display-metadata? true
      :metadata-position "inline"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})] {:mood [1 [2 [3 [4 [5]]]]]}))

  (? {:label ":enable-rainbow-brackets? false" :enable-rainbow-brackets?   false}
     [[[[[[]]]]]])

  (? {:label  ":bracket-contrast low" :enable-rainbow-brackets?   true :bracket-contrast "low"}
     [[[[[[]]]]]])

  (? {:label ":bracket-contrast high":enable-rainbow-brackets?   true :bracket-contrast "high"}
     [[[[[[]]]]]])

  (? {:label ":enable-terminal-truecolor? false" :enable-terminal-truecolor? false}
     (atom [[1 2 3] "abcdefghijk"]))

  ;; :enable-terminal-italics?
  (? {:label ":enable-terminal-italics? false"  :enable-terminal-italics?   false :enable-terminal-truecolor? true}
     (atom [[1 2 3] "abcdefghijk"]))
  (? {:label :enable-terminal-italics? :enable-terminal-italics?   true :enable-terminal-truecolor? true}
     (atom [[1 2 3] "abcdefghijk"]))

  (? {:label "Custom theme"
      :theme {:name   "MyCustomTheme Dark" 
              :mood   "dark"     
              :tokens {:classes {:string {:color "#ff0000"}
                                 :comment {:color "#ff00cc"}}
                       :syntax  {:js-object-key {:color "#888888"}}
                       :printer {:function-args {:color "#bb8f44"}}}}}
   {:string "string"})

  (? {:label "Alabaster Dark"
      :theme "Alabaster Dark"}
     {:string "string"})

  (? {:theme "Alabaster Light"}
     {:string "string"})

  #?(:clj
     (? {:label "java.util.ArrayList" :coll-limit 10} (java.util.ArrayList. [1 2 3 4 5 6 7 8 9 10 11 12 13]))))  


;; Random js data structure tests

;; (let [
;;         ;; itInt8Array  (new js/Int8Array
;;         ;;                   #js[1 2 3 4 5 6 7 8 9])
;;         ;; jsarray  #js[1 2 3 4 5 6 7 8 9]
;;         itmap (new js/Map
;;                    #js[#js["a", 1],
;;                        #js["b", 2]
;;                        #js["c", 3]
;;                        #js["d", 4]])
;;         ;; it #_[1 2 3 [1 2 3 [1 2 3]]]
;;         ;; {:a           [1 3 8 7 5 9 3 99 88]
;;         ;;  :bee         {:a 'foo
;;         ;;                :b {:a 'foo
;;         ;;                    :b [:wtf]}}
;;         ;;  :b           "hello, world"
;;         ;;  :c           (new js/Date) 
;;         ;;  :d           #js {:a "bar"
;;         ;;                    :b "bar"}
;;         ;;  :jsmap       (new js/Map
;;         ;;                    #js[#js["a", 1],
;;         ;;                        #js["b", 2]
;;         ;;                        #js["c", 3]
;;         ;;                        #js["d", 4]])
;;         ;;  :itInt8Array (new js/Int8Array
;;         ;;                    #js[1 2 3 4 5 6 7 8 9])
;;         ;;  :e           #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
;;         ;;  }
;;         ;; pw (truncate 0 it)
;;         ts (with-meta
;;              {"asfasdfasdfasdfasdfasdfasdfasdfasfadf"                          12
;;               :foo                                                           (with-meta #{:a 1}
;;                                                                                {:foo  "on a sev blah blah"
;;                                                                                 :ffoo "adfadsfadsfasfdasdfasdfassasfsdfasd"
;;                                                                                 :baz  "adfadsfadsfasfdasdfasdfassasdfadsfasdf"}) 
;;               #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc" itmap
;;               ;; :itmap itmap
;;               }
;;              {:foo  "adfadsfadsfasfdasdfasdfas"
;;               :ffoo "adfadsfadsfasfdasdfasdfas"
;;               :baz  [123 9898 8989898 89898989 988989]})
;;         ;; tss {:foo "bar"}
;;         ]

;;     (? basic-samples-cljc)

;;     #_(? {:theme monokai-light}
;;      {:a (with-meta (symbol "foo")
;;            {:abc "bar"
;;             :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
;;       :b 1})

;;     #_(? {:theme degas-light} (with-meta 
;;                                 {(with-meta (symbol :a)
;;                                    {:abc "bar"
;;                                     :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"}) (with-meta (symbol "foo")
;;                                                                                              {:abc "bar"
;;                                                                                               :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
;;                                  :b                                                                                         2}
;;                                 {:a (with-meta (symbol "foo")
;;                                       {:abc (with-meta (symbol "bar") {:a   (with-meta (symbol "a")
;;                                                                               {:abc (with-meta (symbol "a")
;;                                                                                       {:abc (with-meta (symbol "a")
;;                                                                                               {:abc "abc"
;;                                                                                                :xyz "xyz"})
;;                                                                                        :xyz "xyz"})
;;                                                                                :xyz "xyz"})
;;                                                                        :xyz "abcdefghijklmnopqrstuv"})
;;                                        :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})}))

;;     ;; (? {[1 2] "afjasljfalsjdalsfk" #{1 2 3} "afsdfasdfsdfasfas"})

;;     #_(? {:a         1234
;;         :abc       3333
;;         :goldenrod "97bda55b-6175-4c39-9e04-7c0205c709dc"})

;;     #_(? {:theme alabaster-lightx}
;;        {:a                 "foo"
;;         :xyz               {:a         1234
;;                             :abc       3333
;;                             :goldenrod "97bda55b-6175-4c39-9e04-7c0205c709dc"}
;;         2222               :wtf
;;         :hi-there-everyone #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"})

;;     #_(? {:theme             alabaster-lightx
;;         :metadata-position :inline}
;;        (with-meta 
;;          {(with-meta (symbol :a)
;;             {:abc "bar"
;;              :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
;;           (with-meta (symbol "foo")
;;             {:abc "bar"
;;              :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})
;;           :b "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
;;           :122222 5555555}
;;          {:a                (with-meta (symbol "foo")
;;                               {:abc  (with-meta (symbol "bar") {:a 1 #_(with-meta (symbol "foo") {:a 1})})
;;                                :xyz  "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"
;;                                ;;  "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
;;                                })
;;           ;; :xyz              "bar" 
;;           ;; "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"
;;           }))


;;     #_(?-- (with-meta [] #_(symbol "foo")
;;          {:xyz "abcdefghijklmnopqrstuvwxyzzzzzzz"}
;;          #_{:xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"}
;;          ))
;;     #_(? {:theme             alabaster-lightx
;;         :metadata-position :inline}
;;        (with-meta
;;          [:b]
;;          {:a                "foo"
;;           :xyz              "bar" 
;;           "hi there everyone" #uuid "97bda55b-6175-4c39-9e04-7c0205c709dc"})))


















(def basic-samples 
  {:string   "string"
   :uuid     #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number   1234
   :boolean  true
   :lambda    #(inc %)
   :fn       juxt
   :regex    #"^hi$"
   :record   record-sample
   :range    (range 40)
   :atom2    (atom record-sample)
   :atom1    (atom 1)
   :brackets [[[[[[]]]]]]})

(def basic-samples-array-map
  (array-map
   :string   "string"
   :uuid     #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number   1234
   :boolean  true
   :lambda    #(inc %)
   :fn       juxt
   :regex    #"^hi$"
   :record   record-sample
   :range    (range 40)
   :atom2    (atom record-sample)
   :atom1    (atom 1)
   :brackets [[[[[[]]]]]] ))

basic-samples

;; (? (meta #'basic-samples))

;; (map #(* % 33) (range 100))

#_(?sgr (-> (? :data {:label                      "my-label-from-opts"
                    :enable-terminal-truecolor? false
                    :enable-terminal-italics?   false
                    :theme                      theme}
             "foo")
          :formatted
          :string))

;; (def themez  "Monokai Light")
;; (? #_{:theme themez} record-sample)
;; (? #_{:theme "Monokai Light"} (with-meta 'foo {:a "a"}))
;; (? "Fix italics" #_(select-keys (:abcdefg basic-samples-cljc) [:uuid :symbol :symbol2]))
;; (? (remove true? [true]))
;; (? :pp (select-keys {:a 1 :b 2} [:f :g :c]))

;; (?  basic-samples-cljc)
;; (!?  #_{} basic-samples-array-map)
;; (!?  #_{} basic-samples-cljc)
;; (!? {:coll-limit 5} (atom (with-meta (range 8) {:foo :bar})))


#_(? :result (atom 1))
#_(? :result (atom [1 2 3]))
;; (? :result (atom {1 2 3 4}))
;; (? (atom "a"))
#_(? {:ababad [1
             2
             (with-meta (symbol "a") {:foo :bar :bang :baz :go :yeah})
             #_(atom (with-meta (symbol "a") {:foo :bar :bang :baz :go :yeah}))
             3]})
;; (? :result [1 2 3 [(atom "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")]])
;; (? :result (atom (range 35)))
#_(? :result [1
            2
            [(atom xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz)]])

#_(println "\n\n")
;; (? :result (volatile! 1))
;; (? :result (volatile! [1 2 3]))
;; (? :result (volatile! {1 2 3 4}))
;; (? :result (volatile! (range 35)))
;; (? :result (volatile! ^{:foo :bar} [1 2 3]))

;; (? (volatile! "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"))
;; (? (volatile! "a"))
;; (? :result [1 2 3 [(volatile! "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")]])
;; (? :result [1
;;             2
;;             [(volatile! xyasldfasldkfaslkjfzzzzzzzzzzzzzzzzzzz)]])


;; (pprint (? :data (volatile! record-sample)))
;; (pprint (? :data (atom record-sample)))

;;  example viewer def -------------------------------
(def myval
  #{{:kind   :rook
     :player :white
     :pos    [5 1]}
    {:kind   :pawn
     :player :white
     :pos    [5 3]}})
;; -----------------------------------------------------------------------------


#?(:cljs
   ()
   :clj
   (do


;;  example viewer start -----------------------------

;; (prn 
;; (-> (? :data myval) :formatted+ :string))
     
;; (pprint
;; (sequence ansi/apply-props
;;           (ansi/token-stream (-> (? :data myval)
;;                                  :formatted+
;;                                  :string))))


;; example viewer start end ----------------------------------

     #_(? (tag-map (fn [a] (inc a))))

     #_(? "Universal Neutral"
          {:theme "Universal Neutral"
           :when  #(not= % 12)}
          basic-samples-cljc-theme)

     (? (tag-map (into-array '(1 2 3))))

     #_(? #{"abcdefghijklmnopqrstuvwxyzzz"
            3333333})

    ;;  (? {:non-coll-length-limit 50} (tag-map (transient [1 2 3 4])))
     
    ;;  (? (transient [1 2 3 4]))
     
    ;;  (let [x (transient #{:a 1})]
    ;;    (? x)
    ;;    (conj! x 11))
     
    ;;  (? (lasertag.core/tag-map 1))
     
    ;;  (? (transient (into [] (range 333))))
     
    ;;  (? (atom (into [] (range 33))))
     
     
    ;;  (? (tag-map (transient [1 2 3 4])))
    ;;  (? (transient (into #{} (range 100))))
    ;;  (? (transient {1 2
    ;;                 3 4}))
     
    ;;  (? (type (transient {1   2
    ;;                       3   4
    ;;                       5   6
    ;;                       7   8
    ;;                       9   10
    ;;                       11  12
    ;;                       13  14
    ;;                       15  16
    ;;                       17  18
    ;;                       191 20
    ;;                       21  22})))
    ;;  (? (type (transient {1   2
    ;;                       3   4
    ;;                       5   6
    ;;                       7   8
    ;;                       9   10
    ;;                       11  12
    ;;                       13  14
    ;;                       15  16
    ;;                       17  18
    ;;                       191 20
    ;;                       21  22})))
     
;;      (!? (instance? java.util.AbstractCollection (java.util.HashMap. {"a" 1
;;                                                                       "b" 2})))
    ;;  (? (java.lang.String. "welcome"))
;;     ;;  (? "welcome")
;;     ;;  (? #{"a" 1 "b" 2})                        
     

    (println "\n:Java HashSet")
    (? (java.util.HashSet. #{"a" 1 "b" 2}))
     
    (println "\n:Java ArrayList")
    (? (java.util.ArrayList. [1 2 3 4 5 6 7]))

    (println "\n:Java HashMap")
    (? (java.util.HashMap. {"a" 1 "b" 2 "c" 3}))

    ;;  (? [1 2 3 4 5 6 7])
    ;;  (? :result 
    ;;     [(to-array '(1 2 3 4 5))
    ;;      (java.util.HashSet. #{"a" "b" "c" "d" "e"})
    ;;      (java.util.ArrayList. [1 2 3 4 5 6 7])
    ;;      (java.util.HashMap. {"a" 1 "b" 2 "c" 3})])
    ;;  (println "\n\n")
    ;;  (pprint [(to-array '(1 2 3 4 5))
    ;;           (java.util.HashSet. #{"a" "b" "c" "d" "e"})
    ;;           (java.util.ArrayList. [1 2 3 4 5])
    ;;           (java.util.HashMap. {"a" 1 "b" 2 "c" 3})]
    ;;          #_{:max-width 20})
    ;;  (println "\n\n")
     
;;     ;;  (? (tag (java.util.HashMap. {"a" 1
;;     ;;                               "b" 2})))
;;     ;;  (? (tag [1 2]))
;;     ;;  (? (tag (list 1 2)))
;;     ;;  (? (tag '(1 2)))
;;     ;;  (? (tag (range 3)))
;;     ;;  (? (tag (map inc (range 3))))
     
;;     ;;  (? (tag (java.util.ArrayList. [1 2 3])))
;;     ;;  (? (tag-map (java.util.ArrayList. [1 2 3])))
;;     ;;  (? (.isArray (.getClass (java.util.ArrayList. [1 2 3]))))
     
      ;;  (? {:non-coll-length-limit 50} (tag-map (transient [1 2 3])))
     
;;     ;;  (!? (tag (to-array '(1 2 3 4))))
     
;;      (? (tag-map (to-array '(1 2 3 4))))
     
;;     ;;  (println (type (to-array '(1 2 3 4))))
     
;;     ;;  (!? (.isArray (.getClass (to-array '(1 2 3 4)))))
     
;;      (? (to-array '(1 2 3 4)))
     

;;      (!? (instance? java.util.ArrayList (java.util.ArrayList. [1 2 3])))
;;      (!? (instance? java.util.ArrayList [1 2 3]))
     









































































     ))

;; #?(:clj
;;    )



