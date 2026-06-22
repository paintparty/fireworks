
;; TODO - break the sample/array-map of everything tests (bolded, basic-samples) into smaller pieces

;; TODO - augment write-test-ns function to also write to testbb dir, but without
;; the problematic namespaces

;; TODO - Make this write distinct clojurescript tests?

;; TODO - Add tests for:
;; - label display variations
;; - different :mode templates
;; - :when pred option for selective printing
;; - correct margins when using margin options
;; - correct margins when using :result flag
;; - Java Objects from java.util.*
;; - More native js Objects
;; - Other transient ClojureScript types (strange in node testing)
;; - Add :use-default-config option which bypasses config.edn and just always uses
;; - "Universal Default" theme, as well as default config options.
;; - :find highlighting with :pred in all highlighting variations
;; - :find highlighting with :path in all highlighting variations

(ns fireworks.core-test
  (:require
   [clojure.string :as string]
   [fireworks.core :refer [?]]
   [fireworks.test-util :refer [escape-sgr visual-mode?]]
   [fireworks.config]
   [fireworks.pp :as pp :refer [?pp !?pp pprint]]
   [fireworks.demo]
   [fireworks.sample :as sample]
   [fireworks.smoke-test]
   [fireworks.config :as config]
   [clojure.test :refer [deftest is]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Options
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Toggle this true / false to generate tests 

(def write-tests? false)
;; (def write-tests? true)

;; To see printed output with tests set fireworks.test-util/visual-mode? to true
;; Note there are 2 fireworks.test-util namespaces, one for clj tests and one
;; for bb tests, so are set independently for visual-mode:
;; ./test/fireworks/test_util.cljc
;; ./testbb/fireworks/test_util.cljc

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def theme "Alabaster Light")

(defn abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn
  []
  nil)


(deftest long-fn-name
  (is (=
       (let [ret              (? :data
                                 {:scalar-max-length 33
                                  :theme             theme}
                                 {:a abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn})
             formatted-string (-> ret :formatted :string)]
         (!?pp  (string/join (escape-sgr formatted-string))))
       "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.core-test/abcdefghij〠0〠〠3;38;2;140;140;140〠...〠0〠〠38;5;102〠}〠0〠")))

#_(deftest transient-set
  (is (=
       (let [ret              (let [x   (transient #{:a 1
                                                     :b 2
                                                     :c 3
                                                     :d 4
                                                     :e 5
                                                     :f 6
                                                     :g 7
                                                     :h 8
                                                     :i 9
                                                     :j 10})
                                    ret (? :data {:theme theme} x)]
                                (when visual-mode?
                                  (? 'transient-set {:theme theme} x))
                                (conj! x 11)
                                ret)
             formatted-string (-> ret :formatted :string)]
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠3;38;2;182;168;66;48;2;58;41;0〠TransientHashSet〠0〠\n〠38;5;250;48;2;58;41;0〠#{〠0〠〠38;2;210;140;109〠〠〠...+20〠0〠〠0〠〠38;5;250;48;2;58;41;0〠}〠0〠")))


#_(deftest transient-map
  (is (=
       (let [ret              (let [x   (transient {:a 1
                                                    :b 2
                                                    :c 3
                                                    :d 4
                                                    :e 5
                                                    :f 6
                                                    :g 7
                                                    :h 8
                                                    :i 9
                                                    :j 10})
                                    ret (? :data {:theme theme} x)]
                                (when visual-mode?
                                  (? 'transient-map {:theme theme} x))
                                (assoc! x :k 11)
                                ret)
             formatted-string (-> ret :formatted :string)]
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠3;38;2;182;168;66;48;2;58;41;0〠TransientHashMap〠0〠\n〠38;5;250;48;2;58;41;0〠{〠0〠〠38;2;210;140;109〠 〠〠...+10〠0〠〠0〠〠38;5;250;48;2;58;41;0〠}〠0〠")))


#_(deftest transient-vector
  (is (=
       (let [ret              (let [x   (transient [1 2 3 4 5 6 7 8 9 0])
                                    ret (? :data {:theme theme} x)]
                                (when visual-mode?
                                  (? 'transient-vector {:theme theme} x))
                                (conj! x 5)
                                ret)
             formatted-string (-> ret :formatted :string)]
         ;; (pp/pprint 'transient-vector)
         ;; (pp/pprint (escape-sgr formatted-string))
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠3;38;2;182;168;66;48;2;58;41;0〠TransientVector〠0〠\n〠38;5;250;48;2;58;41;0〠[〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠〠 〠0〠〠38;2;110;171;237〠6〠0〠〠〠 〠0〠〠38;2;110;171;237〠7〠0〠〠〠 〠0〠〠38;2;110;171;237〠8〠0〠〠〠 〠0〠〠38;2;110;171;237〠9〠0〠〠〠 〠0〠〠38;2;110;171;237〠0〠0〠〠38;5;250;48;2;58;41;0〠]〠0〠")))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TEST GENERATION                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-options-map
  (reduce-kv (fn [m k v] (assoc m k (:default v))) {} config/options))

(def tests (atom []))

(defn hifi-impl [x user-opts]
  (->> x
       (fireworks.core/_p2 (merge user-opts
                                  {:user-opts user-opts
                                   :mode      :data
                                   :p-data?   true
                                   :template  [:result]}))
       :formatted
       :string))

(defmacro deftest+
  [sym opts v]
  (let [sym? (symbol? v)
        list? (list? v)]
    `(do
       (swap! tests
              conj
              {:sym  (quote ~sym)
               :opts ~opts
               :v    ~v
               :qv   (cond ~sym? (quote ~v)
                           ~list? (quote ~v)
                           :else
                           ~v)})
       (when visual-mode?
         (? (quote ~sym) ~opts ~v)))))

;; TODO - if elide branches, just don't write bb
;; generate a separate bb script that is just test-runner
(defn deftests-str
  "This creates a string of all of the generated deftests"
  []
  (string/join
   "\n\n"
   (mapv
    (fn [{:keys [sym opts v qv #_escaped-str]}]
      (let [deftest-str
            (with-out-str
              (pprint
               (list
                'deftest sym
                (list 'is
                      (let [merged-opts (dissoc (merge default-options-map opts)
                                                :when)]
                        (list '=
                              (concat (list '->
                                            (list '? :data merged-opts qv)
                                            :formatted
                                            :string)
                                      '[escape-sgr string/join])
                              (-> (hifi-impl v merged-opts)
                                  escape-sgr
                                  string/join)))))))]
        (if-let [elide-branches (:elide-branches opts)]
          (str
           "#?("
           (reduce (fn [acc k] (str acc k " nil\n   ")) "" elide-branches)
           ":clj\n   "
           (string/join "\n   " (string/split deftest-str #"\n"))
           ")")
          deftest-str)))
    @tests)))


(defn bb-visual-mode
  "This creates a string to print the result for each test."
  []
  (string/join
   "\n\n"
   (keep
    (fn [{:keys [opts qv]}]
      (when-not (= (:elide-branches opts)
                   #{:bb})
        (with-out-str
          (pprint
           (let [merged-opts 
                 (assoc (merge default-options-map opts)
                        :when
                        'visual-mode?)]
             (list '? merged-opts qv))))))
    @tests)))


(def basic-tapping-macros-tests
  (with-out-str
    (pprint '(do (deftest !?-par
                   (is (= (!? "foo") "foo")))
                 (deftest ?>-par
                   (is (= (?> "foo") "foo")))
                 (deftest !?>-par
                   (is (= (!?> "foo") "foo")))))))


(defn- spit-test-ns! [path ns-form & strs]
  (spit path
        (str ";; This namespace is automatically generated in `fireworks.core-test`.\n"
             "\n"
             ";; Do not manually add anything to this namespace.\n"
             "\n"
             ";; To regenerate, follow the instructions in the docstring of `fireworks.core-test/write-tests-ns!`"
             "\n"
             ";; If you want do any experimentation use `fireworks.smoke-test`\n"
             "\n\n"
             ns-form
             "\n\n"
             basic-tapping-macros-tests
             "\n\n"
             (string/join "\n\n" strs))
        :append false))


(defn write-tests-ns!
  "This updates/generates 2 test suite namespaces and writes them to
   test/fireworks/test_suite.cljc and testbb/fireworks/bb_test.cljc. 
      
   Used ocasionally during dev, when tests are modified or added, or
   functionality changes.
   
   To update/generate the above mentioned test namespaces with this function,
   set `fireworks.core-test/write-tests?` to `true`, save this file, then run
   `lein test`. Then set `fireworks.core-test/write-tests?` back to `false`.
   
   To see printed output w/tests, set fireworks.test-util/visual-mode? to true.
   Note there are 2 fireworks.test-util namespaces, one for clj tests and one
   for bb tests, so visual-mode? is set independently:
   ./test/fireworks/test_util.cljc
   ./testbb/fireworks/test_util.cljc
   
   With visual output, keep in mind that the tests, by default, force the  
   `\"Alabaster Dark\"` theme, and will look strange in a light-themed terminal.
   You may want to setup your terminal to use a foreground color of #cecece, 
   and a background color of #0e1415."
  []
  (let [cljc-test-ns-form (with-out-str
                            (pprint '(ns fireworks.test-suite
                                       (:require
                                        [clojure.string :as string]
                                        [fireworks.test-util :refer [escape-sgr visual-mode?]]
                                        [fireworks.core :refer [? !? ?> !?>]]
                                        [fireworks.sample :as sample]
                                        [clojure.test :refer [deftest is]]))))

        bb-test-ns-form   (with-out-str
                            ;; use cons
                            (pprint '(ns fireworks.bb-test
                                       (:require
                                        [clojure.string :as string]
                                        [fireworks.test-util :refer [escape-sgr visual-mode?]]
                                        [fireworks.core :refer [? !? ?> !?>]]
                                        [fireworks.config]
                                        [fireworks.sample :as sample]
                                        [clojure.test :refer [deftest is]]))))

        all-deftests      (deftests-str)
        bb-visual-mode    (bb-visual-mode)]

    (println "--- Writing ./testbb/fireworks/bb_test.cljc --------------------")
    (spit-test-ns! "./testbb/fireworks/bb_test.cljc" 
                   bb-test-ns-form 
                   bb-visual-mode
                   all-deftests)

    (println "--- Writing ./test/fireworks/test_suite.cljc --------------------")
    (spit-test-ns! "./test/fireworks/test_suite.cljc" 
                   cljc-test-ns-form 
                   all-deftests)))

(deftest+ custom-vector-datatype
  {:theme          theme
   :print-length     40
   :elide-branches #{:bb}}
  sample/custom-vector-datatype)

(deftest+ custom-map-datatype
  {:theme          theme
   :print-length     40
   :elide-branches #{:bb}}
  sample/custom-map-datatype)

(deftest+ vector-with-custom-datatype
  {:theme          theme
   :print-length     40
   :elide-branches #{:bb}}
  sample/vector-with-custom-datatypes)

(deftest+ user-fn-names
  {:theme          theme
   :print-length     40
   :elide-branches #{:bb}}
  sample/user-fn-names)

;; (deftest+ basic-samples
;;   {:theme      theme
;;    :print-length 40}
;;   sample/array-map-of-everything-cljc)

(deftest+ no-truncation
  {:theme      theme
   :truncate?  false
   :print-length 40}
  (cons "adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx"
        (range 50)))

(deftest+ string-value
  {:theme theme}
  "string")

(deftest+ regex-value
  {:theme theme :scalar-max-length 100}
  #"^(?:abc\\\(\[\d)+[^a-z0-9\w]*$|^foobar{1}s?$")

(deftest+ uuid-value
  {:theme theme  :scalar-max-length 100}
  #uuid "4fe5d828-6444-11e8-8222-720007e40350")

(deftest+ symbol-value
  {:theme theme}
  (symbol "mysym"))

(deftest+ symbol+meta-value
  {:theme             theme
   :display-metadata? true}
  (with-meta (symbol "mysym")
    {:foo "bar"}))

(deftest+ boolean-value
  {:theme theme}
  true)

(deftest+ keyword-value
  {:theme theme}
  :keyword)

(deftest+ nil-value
  {:theme theme}
  nil)

(deftest+ Nan-value
  {:theme theme}
  ##NaN)

(deftest+ Inf-value
  {:theme theme}
  ##Inf)

(deftest+ -Inf-value
  {:theme theme}
  ##-Inf)

(deftest+ int-value
  {:theme theme}
  1234)

(deftest+ float-value
  {:theme theme}
  3.33)

;; Can't test this because it is dynamic
;; Is there a way to test with regex?
#_(deftest+ lambda
  {:theme theme}
  (fn []))

(deftest+ core-fn
  {:theme theme}
  juxt)

(deftest+ date-fn
  {:theme theme}
  java.util.Date)



(deftest+ map-value
  {:theme theme}
  {:a 1 :b 2 :c "three"})

(deftest+ multiline-map
  {:theme theme}
  {:a     "abcdefghijklmnopqrstuv"
   :ab    "abcdefghijklmnopqrstuv12345"
   :abcde "xyz"})

(deftest+ rainbow-brackets
  {:theme theme} [[[[[]]]]])

(deftest+ vector-value
  {:theme theme}     [1 2 3])

(deftest+ vector+meta-value
  {:theme             theme
   :display-metadata? true}
  (with-meta [:foo :baz] {:meta-on-coll 1}))

(deftest+ set-value
  {:theme theme}
  #{1 "three" :2})

(deftest+ list-value
  {:theme theme}
  (list 1 2 3))

(deftest+ lazy-seq-value
  {:theme theme}
  (range 10))

(deftest+ atom-value
  {:theme theme}
  (atom
   {:black   16
    :blue    75
    :gray    247
    :green   76
    :magenta 171
    :olive   106
    :orange  172
    :purple  141
    :red     196
    :white   231
    :yellow  178}))

(deftest+ volatile!-value
  {:theme theme}
  (volatile! 1))

;;  :transient-vector TransientVector
;;                    [1 2 3 4]
;;  :transient-set    TransientHashSet
;;                    #{...+2}
;;  :transient-map    TransientArrayMap
;;                    { ...+2}



;; (deftest+ bolded
;;   {:theme      theme
;;    :bold?      true
;;    :print-length 40}
;;   sample/array-map-of-everything-cljc)

(deftest+ volatile
  {:theme theme}
  (volatile! 1))

(deftest+ transient-vector2
  {:theme theme}
  (transient [1 2 3 4]))

(deftest+ transient-set2
  {:theme theme}
  (transient #{:a 1}))

(deftest+ transient-map2
  {:theme theme}
  (transient {1 2 3 4}))

(deftest+ single-line-coll-print-length-50-19
  {:theme theme
   :single-line-coll-max-length 50}
  (range 14))

(deftest+ single-line-coll-print-length-50-20
  {:theme theme
   :single-line-coll-max-length 50}
  (range 15))


(deftest+ array-map-order
  {:theme theme}
  (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))

(deftest+ symbol-with-meta
  {:theme theme}
  (with-meta 'mysym {:foo :bar}))

(deftest+ rainbow-brackets
  {:theme theme}
  [[[[[]]]]])

(deftest+ rainbow-brackets-low-contrast
  {:theme            theme
   :bracket-contrast "low"}
  [[[[[]]]]])

(deftest+ with-scalar-level-1-depth-print-length
  {:theme                         theme
   :scalar-depth-1-max-length 60}
  ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])

(deftest+ scalar-result-max-length
  {:theme                         theme
   :scalar-result-max-length 44}
  "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")


(deftest+ datatype-value
  {:theme theme}
  fireworks.sample/my-data-type)


(deftest+ record-value
  {:theme theme}
  fireworks.sample/my-record-type)



;; JVM
(do
  (deftest+ java-interop-types
    {:theme      theme
     :print-length 100}
    sample/interop-types)

  (deftest+ java-util-hashmap
    {:theme theme}
    (java.util.HashMap. {"a" 1
                         "b" 2}))

  (deftest+ java-util-arraylist
    {:theme theme}
    (java.util.ArrayList. [1 2 3]))

  (deftest+ java-util-hashset
    {:theme theme}
    (java.util.HashSet. #{"a" 1 "b" 2})))


(when write-tests?
  (write-tests-ns!))
