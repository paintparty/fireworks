(ns fireworks.core-test
  (:require 
   [clojure.string :as string]
   [fireworks.core :refer [?]]
   [fireworks.test-util :refer [escape-sgr]]
   [fireworks.config]
   [fireworks.pp :as pp :refer [?pp !?pp pprint]]
   [fireworks.demo]
   [fireworks.sample :as sample] 
   [fireworks.smoke-test] 
   [fireworks.config :as config]
   [clojure.test :refer [deftest is]]))


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

(declare visual-mode?)
(declare theme)

(defn abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn [] nil)


(deftest long-fn-name
  (is (= 
       (let [ret              (? :data
                                 {:non-coll-length-limit 33
                                  :theme                 theme} {:a abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn})
             formatted-string (-> ret :formatted :string)]
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;77;109;186〠abcdefghijklmnopqrstuvwxyz-a〠0〠〠3;38;2;140;140;140〠...〠0〠〠38;2;153;153;153〠[]〠0〠〠38;5;102〠}〠0〠")))

(deftest transient-set
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
                                                     :j 10 })
                                    ret (? :data {:theme theme} x)]
                                (when visual-mode? 
                                  (? 'transient-set {:theme theme} x))
                                (conj! x 11)
                                ret)
             formatted-string (-> ret :formatted :string)]
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠3;38;2;199;104;35;48;2;255;249;245〠TransientHashSet〠0〠\n〠38;5;102;48;2;255;249;245〠#{〠0〠〠3;38;2;140;140;140〠〠〠...+20〠0〠〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))
     

(deftest transient-map
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
                                                    :j 10 })
                                    ret (? :data {:theme theme} x)]
                                (when visual-mode? 
                                  (? 'transient-map {:theme theme} x))
                                (assoc! x :k 11)
                                ret)
             formatted-string (-> ret :formatted :string)]
         (!?pp (string/join (escape-sgr formatted-string))))
       "〠3;38;2;199;104;35;48;2;255;249;245〠TransientHashMap〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140〠 〠〠......+10〠0〠〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest transient-vector
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
       "〠3;38;2;199;104;35;48;2;255;249;245〠TransientVector〠0〠\n〠38;5;102;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠0〠0〠〠38;5;102;48;2;255;249;245〠]〠0〠")))



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
                           ~v) 
               })
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

(def basic-tapping-macros-tests
  (with-out-str 
    (pprint '(do (deftest !?-par
                   (is (= (!? "foo") "foo")))
                 (deftest ?>-par
                   (is (= (?> "foo") "foo")))
                 (deftest !?>-par
                   (is (= (!?> "foo") "foo")))))))

(defn- spit-test-ns! [path ns-form deftests-string]
  (spit path 
        (str ns-form
             "\n\n"
             basic-tapping-macros-tests
             "\n\n"
             deftests-string
             )
        :append false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Options
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Toggle this true / false to generate tests 

(def write-tests? false)
;; (def write-tests? true)

;; Toggle this true / false to see the output
;; Visual mode does not yet work when running bb tests
(def visual-mode? false)
;; (def visual-mode? true)

;; Change the theme here (probably don't want to do this)
(def theme "Alabaster Light")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn write-tests-ns!
  "This updates/generates 2 test suite namespaces and writes them to
   test/fireworks/test_suite.cljc and testbb/fireworks/bb_test.cljc. 
      
   Used ocasionally during dev, when tests are modified or added, or
   functionality changes.
   
   To update/generate the above mentioned test namespaces with this function,
   set `fireworks.core-test/write-tests?` to `true`, save this file, then run
   `lein test`. Then set `fireworks.core-test/write-tests?` back to `false`."
  []
  (let [cljc-test-ns-form
        (with-out-str 
          (pprint '(ns fireworks.test-suite 
                     (:require 
                      [clojure.string :as string]
                      [fireworks.test-util :refer [escape-sgr]]
                      [fireworks.core :refer [? !? ?> !?>]]
                      [fireworks.config]
                      [fireworks.demo]
                      [fireworks.sample :as sample] 
                      [fireworks.smoke-test] 
                      [clojure.test :refer [deftest is]]))))

        bb-test-ns-form
        (with-out-str 
          (pprint '(ns fireworks.bb-test
                     (:require
                      [clojure.string :as string]
                      [fireworks.test-util :refer [escape-sgr]]
                      [fireworks.core :refer [? !? ?> !?>]]
                      [fireworks.sample :as sample]
                      [clojure.test :refer [deftest is]]) )))

        s
        (deftests-str)]

   (spit-test-ns! "./test/fireworks/test_suite.cljc" cljc-test-ns-form s) 
   (spit-test-ns! "./testbb/fireworks/bb_test.cljc" bb-test-ns-form s)))

(deftest+ custom-vector-datatype
  {:theme          theme
   :coll-limit     40
   :elide-branches #{:bb}}
  sample/custom-vector-datatype)

(deftest+ custom-map-dataype
  {:theme          theme
   :coll-limit     40
   :elide-branches #{:bb}}
  sample/custom-map-datatype)

(deftest+ custom-map-dataype
  {:theme          theme
   :coll-limit     40
   :elide-branches #{:bb}}
  sample/vector-with-custom-datatypes)

(deftest+ user-fn-names 
  {:theme          theme
   :coll-limit     40
   :elide-branches #{:bb}}
  sample/user-fn-names)

(deftest+ basic-samples
  {:theme      theme
   :coll-limit 40}
  sample/array-map-of-everything-cljc)

(deftest+ no-truncation
  {:theme      theme
   :truncate?  false
   :coll-limit 40}
  (cons "adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx"
         (range 50)))

(deftest+ bolded
  {:theme      theme
   :bold?      true
   :coll-limit 40}
  sample/array-map-of-everything-cljc)

(deftest+ single-line-coll-length-limit-50-19
  {:theme theme
   :single-line-coll-length-limit 50}
  (range 14))

(deftest+ single-line-coll-length-limit-50-20
  {:theme theme
   :single-line-coll-length-limit 50}
  (range 15))


(deftest+ array-map-order
  {:theme theme}
  (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))

(deftest+ record-sample-in-atom
  {:theme theme}
  (atom sample/my-record-type))

(deftest+ record-sample
  {:theme theme}
  sample/my-record-type)

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

(deftest+ with-non-coll-level-1-depth-length-limit
  {:theme                         theme
   :non-coll-depth-1-length-limit 60}
  ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])

(deftest+ non-coll-result-length-limit
  {:theme                         theme
   :non-coll-result-length-limit 44}
  "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")



;; JVM
(do 
  (deftest+ java-interop-types
    {:theme      theme
     :coll-limit 100}
    sample/interop-types )

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

;; Call this from script or uncomment here to regenerate test suite
(when write-tests? 
  (println "--- Writing new fireworks.test_suite namespace --------------------")
  (write-tests-ns!))
