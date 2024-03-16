(ns fireworks.smoke-test
  (:require [fireworks.core :refer [? !? ?> !?> p p*]]
            [clojure.string :as string] [fireworks.pp :as pp]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))

;; This is example config. If you want to run fireworks.core-test tests locally,
;; replace the config map in your ~/.fireworks/config.edn with this map temporarily.
;; If you don't do this, the tests will break.
;; TODO - Fix the above situation.
{:theme                      "Alabaster Light"
 :mood                       :light
 :line-height                1.45
 :print-level                7
 :non-coll-length-limit          33
 :non-coll-mapkey-length-limit         20
 :coll-limit                 15
 :evaled-form-coll-limit     7
 :display-namespaces?        true
 :metadata-print-level       7
 :display-metadata?          true
 :metadata-position          :inline 
 :enable-rainbow-brackets?   true
 :bracket-contrast           :high
 :enable-terminal-truecolor? true
 :enable-terminal-italics?   true
 :custom-printers            nil
 :find                       nil}


;; This is for tests in core_tests
(def alabaster-light-legacy
  {:name    "Alabaster Light"
   :desc    "Based on @tonsky's Alabaster theme."
   :about   "This is additional documentation. Should support markdown here."
   :url     "url goes here"
   :author  "Author Name"
   :langs   ["Clojure" "ClojureScript" "Babashka"]
   :mood    :light
   ;; :bracket-contrast "high"
   :tokens   {:classes {:background {:background-color "#f7f7f7"}
                        :string     {:color "#448C27"}
                        :comment    {:color      "#AA3731"
                                     :font-style :italic}
                        :constant   {:color "#7A3E9D"}
                        :definition {:color "#4d6dba"}
                        :annotation {:color      "#8c8c8c" 
                                     :font-style :italic}
                        :metadata   {:color            "#2e6666"
                                     :text-shadow      "0 0 2px #ffffff"
                                     :background-color "#e6fafa"}}
             :syntax  {:js-object-key {:color "#888888"}}
             :printer {:eval-fat-arrow {:color "#28cc7d"}
                       :function-args  {:color "#999999"}
                       :atom-wrapper   {:color            "#256546"
                                        :background-color "#d6f5d6"
                                        :text-shadow      "0 0 2px #ffffff"
                                        :font-style       :italic}}}})


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
        :java.math.BigInteger (java.math.BigInteger. "171")}
       )
     ))

(def everything
  {:primitives   {:a                              9999
                  :bbb                            "asdfasdfasdfjasdfasdfasdfasdfa"
                  "really-long-string-abcdefghi"  "really-long-string-abcdefghi"  
                  1                               2
                  "string"                          "hello"
                  :keyword                        :keyword
                  true                            false
                  nil                                nil
                  'symbol                  'symbol
                  #"^hi$"                          #"^hi$"
                  #"really-long-string-abcdefghi" #"really-long-string-abcdefghi"
                  }
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

   :with-meta    {:symbol  (with-meta (symbol (str "foo")) (into {} (map (fn [x y] [x y]) (range 20) (repeat :foo))))
                  :vector  ^{:foo :bar} [:foo :bar :baz]
                  :vector* ^{:foo :bar} [(with-meta (symbol (str "foo")) {:bar :baz})
                                         (with-meta (symbol (str "bar")) {:bar :baz})
                                         (with-meta (symbol (str "baz")) {:bar :baz})]
                  :map     ^{:foo :bar} {:one :two}
                  :map*    ^{:foo :bar} {(with-meta (symbol (str "foo")) {:bar :baz})  (with-meta (symbol (str "bar")) {:bar :baz})

                                         (with-meta (symbol (str "bang")) {:bar :baz}) (with-meta (symbol (str "bop")) {:bar :baz})}}

   :number-types #?(:cljs
                    js-number-types
                    :clj
                    nil)})

(defrecord Foos [a b])

(def record-sample (->Foos 1 2))

(def basic-samples 
  {:string   "string"
   :uuid     #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :number   1234
   :symbol   (with-meta 'mysym {:foo :bar})
   :symbol2  (with-meta 'mysym
               {:foo ["afasdfasf"
                      "afasdfasf"
                      {:a "foo"
                       :b [1 2 [1 2 3 4]]}
                      "afasdfasf"
                      "afasdfasf"]

                :bar "fooz"})
   :boolean  true
   :lamda    #(inc %)
   :fn       juxt
   :regex    #"^hi$"
   :record   record-sample
   :atom2    (atom record-sample)
   :atom1    (atom 1)
   :brackets [[[[[[]]]]]]})


#?(:clj
   (do 
     (do
     ;; DataTypes ------------------------------------------------------
         
         (? "A def of deftype" (deftype MyType [a b]))
         
         (p "A deftype" MyType)
         (def my-data-type (->MyType 2 3))
         (p "Instance of deftype" my-data-type)


     ;; RecordTypes ----------------------------------------------------
         (defrecord MyRecordType [a b c d])
         (p "A record type" MyRecordType)
         (defrecord MySubRecordType [x y])
         (def my-record-type (->MyRecordType 
                              (->MySubRecordType "adfasdfasdfasdfasdf" "y")
                              #_"asdfasdfasdfsafasdfadf"
                              "xyxyxyxyxyxxyxxy"
                              123456
                              juxt))
         (p "Instance of record type" MyRecordType)


     ;; Multimethods ---------------------------------------------------
         (defmulti different-behavior (fn [x] (:x-type x)))
         (defmethod different-behavior :wtf
           [x]
           (str (:name x) " will have a specific behavior"))
         (p "Multimethod" different-behavior)


     ;; Dates ----------------------------------------------------------
         (def my-date (java.util.Date.))
         (p "A java date" my-date)


     ;; Function -------------------------------------------------------
         (defn xy [x y] (+ x y))
         (p "2-arity function" xy)

         (defn xy-variadic [x y & args] (+ x y))
         (p "variadic function" xy-variadic)

     ;; Collections ----------------------------------------------------
         (p "anonymous function" (fn xy-variadic []))
         (p "anonymous function sugared" #(inc %))
         (p "vector"               [1 2 3])
         (p "set"                  #{1 2 3})
         (p "list"                 '(1 2 3))
         (p "seq"                  (range 10))
         (p "record"               my-record-type)
         (p {:coll-limit 5 :label "truncation"} [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23])
         (let [o {:coll-limit 5 :label "truncation with opts var"}]
           (p o [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23]))
         (p "colls-as-keys"        {[1 2]    "afjasljfalsjdalsfk" #{1 2 3} "afsdfasdfsdfasfas"})

     ;; Abstractions ----------------------------------------------------
         (p "Atom" {:atom (atom 1)})

     ;; Java Number Types ----------------------------------------------------
         (p "##NaN" ##NaN)
         (p "##Inf" ##Inf)
         (p "##-Inf" ##-Inf)
         (p "1/3" 1/3)
         (p "byte" (byte 0))
         (p "short" (short 3))
         (p "double" (double 23.44))
         (p "decimal" 1M)
         (p "int" 1)
         (p "float" (float 1.50))
         (p "char" (char 97))
         (p "java.math.BigInteger" (java.math.BigInteger. "171"))
         (p {:label      "Basic samples"
             :coll-limit 15
             :theme      "Alabaster Light"
             :find       {:pred #(= 1234 %)}}
            basic-samples)

         ;; Custom printing
         
         (p {:custom-printers {:vector {:pred        (fn [x] (= x [765 233 444 21]))
                                        :f           (fn [x] (into #{} x))
                                        :badge-text  " Set💩 "
                                        :badge-style {:color            "#000"
                                                      :background-color "lime"
                                                      :border-radius    "999px"
                                                      :text-shadow      "none"
                                                      :font-style       "normal"}}}}
            [765 233 444 21])

         

         
         )))
  

 ;; Testing all the options cljc
 (do 

  ;; :mood
  (p {:label :mood :mood "light" :theme nil}
     (with-meta ["Light"] {:mood :light}))
  (p {:label :mood :mood "dark" :theme nil}
     (with-meta ["Dark"] {:mood :dark}))

  ;; :print-level
  (p {:label :print-level :print-level                7}
     [0 [1 [2 [3 [4 [5 [6 [7 [888 [9]]]]]]]]]])

  ;; :theme
  (p {:label "theme Alabaster Dark" :theme "Alabaster Dark"}
     (atom [[1 2 3] "abcdefghijk"]))
  (p {:label "theme Alabaster Light" :theme "Alabaster Light"}
     (atom [[1 2 3] "abcdefghijk"]))

  ;; :non-coll-length-limit
  (p {:label :non-coll-length-limit :non-coll-length-limit 20}
     "abcdefghijklmnopqrstuvwxyz")


  ;; :non-coll-mapkey-length-limit
  (p {:label :non-coll-mapkey-length-limit :non-coll-mapkey-length-limit         20}
     {"abcdefghijklmnopqrstuvwxyz" [1 2 3] })

  (? {:evaled-form-coll-limit     7}
     [:evaled-form-coll-limit 1 2 3 4 5 6 7 8 9 10])

  (p {:label :display-namespaces? :display-namespaces?        true}
     [juxt my-record-type])

  (p {:label "display-namespaces? false" :display-namespaces?        false}
     [juxt my-record-type])
  
  (p {:label             :metadata-position
      :metadata-position "block"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})
                 (with-meta (symbol "foo") {:my :meta})
                 (with-meta (symbol "foo") {:my :meta})]
       {:mood [1 [2 [3 [4 [5]]]]]})) 

  

  (p {:label                :metadata-print-level
      :metadata-print-level 3
      :metadata-position    "inline"}
     (with-meta [] {:mood [1 [2 [3 [4 [5]]]]]}))

  ;; :display-metadata?
  (p {:label             ":display-metadata? false"
      :display-metadata? false
      :metadata-position "inline"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})] {:mood [1 [2 [3 [4 [5]]]]]}))

  ;; :display-metadata?
  (p {:label             :display-metadata?
      :display-metadata? true
      :metadata-position "inline"}
     (with-meta [(with-meta (symbol "foo") {:my :meta})] {:mood [1 [2 [3 [4 [5]]]]]}))

  (p {:label ":enable-rainbow-brackets? false" :enable-rainbow-brackets?   false}
     [[[[[[]]]]]])

  (p {:label  ":bracket-contrast low" :enable-rainbow-brackets?   true :bracket-contrast "low"}
     [[[[[[]]]]]])

  (p {:label ":bracket-contrast high":enable-rainbow-brackets?   true :bracket-contrast "high"}
     [[[[[[]]]]]])

  (p {:label ":enable-terminal-truecolor? false" :enable-terminal-truecolor? false}
     (atom [[1 2 3] "abcdefghijk"]))

  ;; :enable-terminal-italics?
  (p {:label ":enable-terminal-italics? false"  :enable-terminal-italics?   false :enable-terminal-truecolor? true}
     (atom [[1 2 3] "abcdefghijk"]))
  (p {:label :enable-terminal-italics? :enable-terminal-italics?   true :enable-terminal-truecolor? true}
     (atom [[1 2 3] "abcdefghijk"]))

  (p {:label "Custom theme"
      :theme {:name   "MyCustomTheme Dark" 
              :mood   "dark"     
              :tokens {:classes {:string {:color "#ff0000"}
                                 :comment {:color "#ff00cc"}}
                       :syntax  {:js-object-key {:color "#888888"}}
                       :printer {:function-args {:color "#bb8f44"}}}}}
   {:string "string"})

  (p {:label "Alabaster Dark"
      :theme "Alabaster Dark"}
     {:string "string"})

  (p {:theme "Alabaster Light"}
     {:string "string"})

  #?(:clj
     (p {:label "java.util.ArrayList" :coll-limit 10} (java.util.ArrayList. [1 2 3 4 5 6 7 8 9 10 11 12 13])))
  )  
