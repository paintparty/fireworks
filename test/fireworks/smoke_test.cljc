(ns fireworks.smoke-test
  (:require [fireworks.core :refer [? !? ?- !?- ?-- !?-- ?> !?> ?i !?i
                                    ?l !?l ?log !?log ?log- !?log- ?pp
                                    !?pp ?pp- !?pp- ?trace !?trace]]
            [fireworks.themes :as themes]
            [clojure.string :as string] [fireworks.pp :as pp]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as walk]
            [fireworks.util :as util]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))


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

(def basic-samples-cljc-theme
  {:string          "string"
   :uuid            #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number          1234
   :symbol          (with-meta 'mysym {:foo :bar})
   :boolean         true
   :lamda           #(inc %)
   :fn              juxt
   :regex           #"^hi$"
   :record          record-sample
   :atom/number     (atom 1)
   :brackets        [[[[[[]]]]]]
   :map/nested-meta (with-meta {:a :foo
                                :b 2}
                      {:a (with-meta (symbol "foo")
                            {:abc (symbol "bar")
                             :xyz "abcdefghijklmnopqrstuvwxyzzzzzzzzzzzzzzzzzzzz"})})})

(def basic-samples 
  {:string   "string"
   :uuid     #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number   1234
   :boolean  true
   :lamda    #(inc %)
   :fn       juxt
   :regex    #"^hi$"
   :record   record-sample
   :atom2    (atom record-sample)
   :atom1    (atom 1)
   :brackets [[[[[[]]]]]]})


;; (? record-sample)

;; Testing :non-coll-mapkey-length-limit
;; (? {:non-coll-mapkey-length-limit 40} {"12345678_112345678_212345678_312345678_4" :gold})
;; Testing print level truncation syntax
#_(? {:print-level 1} {:a {:b {:a 1 :b 2}
                         :c #{1 2 3 4 5}}})

;; (? basic-samples-cljc)

;; (? "? : Default" {:a "foo"})
;; (? "? : Default:\nLine1\nLine2" {:a "foo"})
;; (? {:label "with options"} (atom {:a "foo" :b 12}))
;; (? (def x1 "x1"))
;; (? {:label "def with label from options"} (def x2 "x2"))


;; (?l "?l : Default" {:a "foo"})
;; (?l "?l : Default:\nLine1\nLine2" {:a "foo"})
;; (?l {:label "with options" } (atom {:a "foo" :b 12}))
;; (?l (def x3 "x3"))
;; (?l {:label "def with label from options"} (def x4 "x4"))


;; (?i "?i : Just the namespace info" {:a "foo"})
;; (?i "?i : Default:\nLine1\nLine2" {:a "foo"})
;; (?i {:label "with options" :print-with prn} (atom {:a "foo" :b 12}))
;; (?i (def x5 "x5"))
;; (?i {:label "def with label from options"} (def x6 "x6"))

;; (?log "?log " {:a "foo"})
;; (?log "?log : Default:\nLine1\nLine2" {:a "foo"})
;; (?log {:label "with options" } (atom {:a "foo" :b 12}))
;; (?log (def x7 "x7"))
;; (?log {:label "def with label from options"} (def x8 "x8"))

;; (?log- {:a "foo"})
;; (?log- {:a "foo"})
;; (?log-  (atom {:a "foo" :b 12}))
;; (?log- (def x9 "x9"))

;; (?pp {:f '?pp :b "asfdasdfasfas"})
;; (?pp "pp with label" {:f '?pp :b "asfdasdfasfas"})
;; (?pp "pp with label, def" (def x10 "x10"))
;; (?pp- {:f '?pp :b "asfdasdfasfas"})
;; (?pp- (def x11 "x11"))

;; (?log {:f '?log :b "asfdasdfasfas"})
;; (?log "?log with label" {:f '?log :b "asfdasdfasfas"})

;; (?log- {:f '?log :b "asfdasdfasfas"})
;; (?log- {:f '?log :b "asfdasdfasfas"})



;; (?log {:f '?log :desc "default"})
;; (?log "Label" {:f '?log :desc "with label"})
;; (?log "Label\nLabel Line 2" {:f '?log :desc "with multiline label"})

;; (?log- (def xxx 1))
;; (?log- {:f '?log :b "asfdasdfasfas"})

;; (?pp (p-data {:a "foo"}))
;; (?pp (p-data (def x12 "x12")))
;; (?pp (p-data {:label "def with opts"} (def x12 "x12")))

;; (?pp (p-data "?p-data : Default:\nLine1\nLine2" (atom {:a "foo" :b 12})))
;; (?pp (p-data {:label "?p-data : Default:\nLine1\nLine2" } (atom {:a "foo" :b 12})))


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



#_(? (walk/postwalk 
    (fn [x]
      (if-let [[tag v] (and (vector? x)
                            (let [[tag v] x]
                              (when (and (not (coll? v))
                                         (keyword? tag))
                                [tag v])))]
        (with-meta (symbol (util/as-str v))
          {:color       :red
           :font-weight :bold
           :font-style  :italic})
        x))
    ["neutral"
     [:bold.red.italic "then red "]
     "then neutral"
     [:bold.red.italic "then blue "]
     "then neutral again"
     [:bold.red.italic "then red "]]))


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
