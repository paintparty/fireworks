;; Unstructured sandbox for smoke testing

(ns fireworks.smoke-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.themes :as themes]
            [fireworks.state]
            [fireworks.color]
            [clojure.string :as string]
            [fireworks.pp :as pp]
            [clojure.pprint :refer [pprint print-table]]
            [clojure.walk :as walk]
            [fireworks.util :as util]
            [fireworks.sample :as sample]
            [fireworks.basethemes :as basethemes]
            [lasertag.core :refer [tag-map tag]]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))


;; let bindings smoke test
#_(? :trace 
   {:theme "Alabaster Dark"} 
   (let [foo                                         1
         bar                                         2
         [a [d e] :as abs]                           [:a [:d :e]]
         #_#_{:keys              [x y]
          {bee :zo
           gee :zz} :z
          :as                m} {:x 1
                                 :y 2
                                 :z {:zo                44
                                     :zz                55
                                     :aafasdfasdfadsfds "asdfsdfsdfasdfsdfkjsadl;fkjsadlfjasdlfjsaldfjsda"}}]
     [a d]
     #_(+ bee gee)
     ))

(def my-map {"foo" "bar"})
(? :pp {:margin-top 0} {:x 1
                        :y 2
                        :z {:zo                44
                            :zz                55
                            :aafasdfasdfadsfds "asdfsdfsdfasdfsdfkjsadl;fkjsadlfjasdlfjsaldfjsda"}})

#_(? :trace
   (with-meta 
     (->> my-map 
          vec
          flatten)
     {:fw/hide-brackets? true}))


;; single-column map smoke tests
#_(do (? {:single-column-maps? true}
       {:foo :bar}))


;; long-fn name smoke test
#_(do (defn abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn [] nil)
    (? {:non-coll-length-limit 33}
       {:a abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn})) 


;; :trace mode smoke testing
#_(do
(def my-map {"foo" "bar"})

(defn ^:public map?->
  "If (map? x), returns x, otherwise nil."
  [x]
  (when (map? x) x))

(println "hn\n---------- testing :trace with ->>")
(? :trace
   (->> my-map 
        vec
        flatten))

(println "\n\n---------- testing :trace with some->>, short-circuting initial")
(? :trace
   (some->> nil
            vec
            flatten))

(println "\n\n---------- testing :trace with some->>, short-circuting second")
(? :trace
   (some->> my-map
            vec
            map?->
            flatten))

(println "\n\n---------- testing :trace with some->>, short-circuting second, 3-arity")
(? :trace
   {}
   (some->> my-map
            vec
            map?->
            flatten))

(println "\n\n---------- testing :trace with ->, 3-arity")
(? :trace
   {:single-column-maps? true}
   (-> my-map
       (assoc :bango :bongo)
       (keys)
       (->> (mapv #(-> % name string/upper-case)))))

(println "\n\n---------- testing :trace with ->")
(? :trace
   (-> my-map
       (assoc :bango :bongo)
       (keys)
       (->> (mapv #(-> % name string/upper-case)))))

(println "\n\n---------- testing :trace with some->")
(? :trace
   (some-> my-map
       (assoc :bango :bongo)
       (keys)
       (->> (mapv #(-> % name string/upper-case)))))

(println "\n\n---------- testing :trace with ->, 3-arity")
(? :trace
   {:single-column-maps? true}
   (-> my-map
       (assoc :bango :bongo)
       (keys)
       (->> (mapv #(-> % name string/upper-case)))))


(println "\n\n---------- testing :trace with normal fn, should not trace")
(? :trace
   (+ 1 1))

(println "\n\n---------- testing :trace with as->, 3-arity, should not trace")
(? :trace
   {:single-column-maps? true}
   (as-> my-map $ 
     (vec $)
     (flatten $)))

(println "\n\ntesting :trace with as->, 3-arity, print-with pprint, should not trace")
(? :trace
   {:single-column-maps? true
    :print-with          pprint}
   (as-> my-map $ 
     (vec $)
     (flatten $)))

(println "\n\ntesting :trace with as->, should not trace")
(? :trace
   #_{:single-column-maps? true
    ;; :print-with          pprint
      }
   #_{}
   (as-> my-map $ 
     (vec $)
     (flatten $)))
)

;; Color level support smoke test
#_(do
    (def my-sample ["string" 1234 :keyword 'sym])
    (? my-sample)
    (? {:supports-color-level 3} my-sample)
    (? {:supports-color-level 2} my-sample)
    (? {:enable-terminal-truecolor? false} my-sample)
    (? {:supports-color-level 1} my-sample)
    #_(? {:legacy-terminal? true} my-sample))


;; Smoke test: red, green, or blue form/label tags at call-site, light themes
#_(let [my-sample (with-meta (symbol "foo") {:metadata :bar})]
    #_(do (println "\n\nAlabaster Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Alabaster Light" :label-color color} my-sample)
                (? :no-file {:theme "Alabaster Light" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nDegas Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Degas Light" :label-color color} my-sample)
                (? :no-file {:theme "Degas Light" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nSolarized Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Solarized Light" :label-color color} my-sample)
                (? :no-file {:theme "Solarized Light" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nZenburn Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Zenburn Light" :label-color color} my-sample)
                (? :no-file {:theme "Zenburn Light" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nMonokai Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Monokai Light" :label-color color} my-sample)
                (? :no-file {:theme "Monokai Light" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nNeutral Light\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Neutral Light" :label-color color :display-metadata? true} my-sample)
                (? :no-file {:theme "Neutral Light" :label-color color :display-metadata? true :label "custom label text"} my-sample))))

    #_(do (println "\n\nUniversal\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Universal" :label-color color :display-metadata? true} my-sample)
                (? :no-file {:theme "Universal" :label-color color :display-metadata? true :label "custom label text"} my-sample))))

    #_(do (println "\n\nUniversal Neutral\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Universal Neutral" :display-metadata? true :label-color color} my-sample)
                (? :no-file {:theme "Universal Neutral" :display-metadata? true :label-color color :label "custom label text"} my-sample)))))


;; Smoke test: red, green, or blue form/label tags at call-site, dark themes
#_(let [my-sample (with-meta (symbol "foo") {:metadata :bar})]

    #_(do (println "\n\nAlabaster Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Alabaster Dark" :label-color color} my-sample)
                (? :no-file {:theme "Alabaster Dark" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nDegas Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Degas Dark" :label-color color} my-sample)
                (? :no-file {:theme "Degas Dark" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nSolarized Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Solarized Dark" :label-color color} my-sample)
                (? :no-file {:theme "Solarized Dark" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nZenburn Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Zenburn Dark" :label-color color} my-sample)
                (? :no-file {:theme "Zenburn Dark" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nMonokai Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Monokai Dark" :label-color color} my-sample)
                (? :no-file {:theme "Monokai Dark" :label-color color :label "custom label text"} my-sample))))

    #_(do (println "\n\nNeutral Dark\n")
          (doseq [color [:red :green :blue]]
            (do (? :no-file {:theme "Neutral Dark" :label-color color :display-metadata? true} my-sample)
                (? :no-file {:theme "Neutral Dark" :label-color color :display-metadata? true :label "custom label text"} my-sample)))))

#?(:clj
   (def sample
     ["\"hi\""
      "hi"

      :hi
      :hi

      "^hi$"
      "^hi$"

      true
      true

      'mysym
      'mysym

      [1 2 3]
      [1 2 3]

      #{1 3 2}
      #{1 3 2}

      {:a 2
       :b 3}
      {:a 2
       :b 3}

      '(map inc (range 3))
      (map inc (range 3))

      '(range 3)
      (range 3)

      '(:a :b :c)
      (:a :b :c)

      '##Inf
      ##Inf

      '##-Inf
      ##-Inf

      '##NaN
      ##NaN

      1/3
      1/3

      '(byte 0)
      (byte 0)

      '(short 3)
      (short 3)

      '(double 23.44)
      (double 23.44)

      '1M
      1M

      1
      1

      '(float 1.5)
      (float 1.5)

      '(char 92)
      (char 92)

      '(java.math.BigInteger. "171")
      (java.math.BigInteger. "171")

      '(java.util.Date.)
      (java.util.Date.)]))

(def lasertag-sample
  #?(:cljs
     ()
     :clj
     (reduce
      (fn [acc [sym v]]
        (conj acc
              {'form              sym
               'lasertag.core/tag (tag v)
               'clojure.core/type (type v)}))
      []
      (partition 2 sample))))


#_(println (table [{:name 'form :title "form"}
                   {:name 'lasertag.core/tag :title "lasertag.core/tag"}
                   {:name 'clojure.core/type :title "clojure.core/type"}]
                  lasertag-sample))

;; This is example config. If you want to run fireworks.core-test tests locally,
;; replace the config map in your ~/.fireworks/config.edn with this map temporarily.
;; If you don't do this, the tests will break.
;; TODO - Fix the above situation.
{:theme                        "Alabaster Light"
 :line-height                  1.45
 :print-level                  7
 :non-coll-length-limit        33
 :non-coll-mapkey-length-limit 20
 :coll-limit                   15
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

;; Todo - the slice samplings such as sample/array-map-of-everything-cljc
;; fns so you can pass ks for get-in an narrowing 
#_(pprint (tag-map 1/3))

(def sample-map
  (array-map
   :string
   "string"
   :regex
   #"myregex"
   :uuid
   #uuid "4fe5d828-6444-11e8-8222-720007e40350"
   :symbol
   'mysym
   :symbol+meta
   (with-meta 'mysym {:foo "bar"})
   :boolean
   true
   :keyword
   :keyword
   :nil
   nil
   :##Nan
   ##NaN
   :##Inf
   ##Inf
   :##-Inf
   ##-Inf
   :map
   {:a 1
    :b 2
    :c "three"}
   :vector
   [1 2 3]
   :vector+meta
   ^{:meta-on-coll 1}
   ['foo
    (with-meta 'bar {:meta-on-sym 2})
    'baz]
   :list
   '(1 2 3)
   :lazy-seq
   (range 10)
   :rainbow
   [[[[[[]]]]]]
   :set
   #{1 :2 "three"}))


#_(println
   (-> (? :data
          {:theme "Universal Neutral"}
          sample-map)
       :formatted+
       :string))


(!? {:coll-limit 100
     :theme      "Alabaster Light"
     :label      "Clojure(Script) Values"}
    sample/array-map-of-multiline-formatting-cljc)

(!? {:coll-limit 100
     :theme      "Alabaster Light"
     :label      "Clojure(Script) Values, with extras"
     :find {:pred #(= :tag %)}}
    (sample/vec-of-everything-cljc-with-extras))

(!? {:coll-limit 100
     :label      "JVM Clojure Values"}
    sample/interop-types)

(!? {:coll-limit 100
     :label      "JVM Clojure Values with extras"}
    sample/vec-of-interop-types-with-extras)



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY START
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO - Add more commentary and thorough examples


(do
  ;; good 
  #_(do
      (co '? nil)
      (? {:a "foo"})
      (? "width custom label" {:a "foo"})
      (? "Custom label line1\nCustom label line2" {:a "foo"})
      (? {:label "Custom label from :label option"} (atom {:a "foo" :b 12}))
      (? (def x1 "x1"))
      (? "def with custom label" (def x2 "x2"))
      (? {:label "def with label from options"} (def x3 "x3")))


  #_(do
      (co :label "Only display label (or form), no file info.")
      (? :label "my label" {:a "foo"})
      (when (= {:a "foo"}
               (? :label
                  {:margin-bottom 0}
                  {:a "foo"}))
        (println "✓"))
      (? :label "Line1\nLine2" {:a "foo"})
      (? :label {:label "Label from options"} (atom {:a "foo" :b 12}))
      (? :label (def x4 "x4"))
      (? :label {:label "def with label from options"} (def x5 "x5"))))


#_(do
    (co :file "Only display file-info, no label (or form).\nReturns result.")
    (? :file
       {:a "foo"})
    (when (= {:a "foo"}
             (? :file
                {:label         "?i : Just the namespace info"
                 :margin-bottom 0}
                {:a "foo"}))
      (println "✓"))
    (? :file {:a "foo"})
    (? :file {:theme "Neutral Light"} (atom {:a "foo"
                                             :b 12}))
    (? :file (def x6 "x6"))
    (? :file {:label "This label should not be displayed"} (def x7 "x7")))


#_(do
    (co :result "Only display result.\nReturns result.")
    (? :result {:a "foo"})
    (? :result "? : Default" {:a "foo"})
    (when (= (? :result {:margin-bottom 0} {:a "foo"})
             {:a "foo"})
      (println "✓"))
    (? :result "Custom label should not be visible" {:a "foo"})
    (? :result {:label "Custom label from opts should not be visible"} (atom {:a "foo" :b 12}))
    (? :result (def x8 "x8"))
    (? :result {:label "Custom label from opts should not be visible"} (def x9 "x9")))

#_(do
    (co :comment "Only display comment and file.\nDoes NOT return result.")
    (? :comment "my comment")
    (? :comment {:margin-bottom 0} "my comment"))

#_(do
    (co :log "Uses js/console.log or pprint.\nReturns result.")
    (? :log "my label" {:a "foo" :abc "bar"})
    (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
             {:a "foo" :abc "bar"})
      (println "✓"))

    (? :log "Line1\nLine2" {:a "foo" :abc "bar"})
    (? :log {:label "With label from options"} (atom {:a "foo" :abc "bar"}))
    (? :log (def x10 "x10"))
    (? :log {:label "def with label from options"} (def x11 "x11")))

#_(do
    (co :pp "Uses pprint.\nReturns result.")
    (? :pp "my label" {:a "foo" :abc "bar"})
    (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
             {:a "foo" :abc "bar"})
      (println "✓"))
    (? :pp "Line1\nLine2" {:a "foo" :abc "bar"})
    (? :pp {:label "With label from options"} (atom {:a "foo" :abc "bar"}))
    (? :pp (def x10 "x10"))
    (? :pp {:label "def with label from options"} (def x11 "x11")))

#_(do
    (co :log- "Uses js/console.log or pprint.\nReturns result.")
    (? :log- "my label" {:a "foo" :abc "bar"})
    (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
             {:a "foo" :abc "bar"})
      (println "✓"))
    (? :log- "Line1\nLine2" {:a "foo" :abc "bar"})
    (? :log- {:label "With label from options"} (atom {:a "foo" :abc "bar"}))
    (? :log- (def x10 "x10"))
    (? :log- {:label "def with label from options"} (def x11 "x11")))

#_(do
    (co :pp- "Uses js/console.log or pprint.\nReturns result.")
    (? :pp- "my label" {:a "foo" :abc "bar"})
    (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
             {:a "foo" :abc "bar"})
      (println "✓"))
    (? :pp- "Line1\nLine2" {:a "foo" :abc "bar"})
    (? :pp- {:label "With label from options"} (atom {:a "foo" :abc "bar"}))
    (? :pp- (def x10 "x10"))
    (? :pp- {:label "def with label from options"} (def x11 "x11")))

#_(do
    (co :data "Prints a data representation of the format.\nDoes NOT returns result.")
    (pprint (? :data {:a "foo"}))
    (when (= (-> (? :data {:margin-bottom 0} {:a "foo"})
                 :formatted
                 :string)
             "\033[38;5;241m{\033[0m\033[38;2;122;62;157m:a\033[0m \033[38;2;68;140;39m\"foo\"\033[0m\033[38;5;241m}\033[0m")
      (println "✓")))

;; Tracing feature, leave off for now
#_(do
    (co :trace "Trace form.\nReturns result.")
    (co2 "Default" '(? :trace (-> 1 (+ 3))))
    (? :trace (-> 1 (+ 3)))


    (co2 "With user label" '(? :trace "My label" (-> 1 (+ 3))))
    (? :trace "My label" (-> 1 (+ 3)))

    (co2 "With longer threading form"
         '(? :trace
             (-> 1 (+ 3) (repeat 5 5555555))))
    (? :trace (-> 1 (+ 3) (cons (repeat 5 5555555))))

    ;; Different let bindings  
    )

;; let-binding trace, leave off for now
#_(do
    ;; (? :let 
    ;;  (let [[a & rest] ["a" "b" "c" "d" "e"]]
    ;;    [a rest]))


    ;; (? :let [[a c] ["4" 5]
    ;;        b 2]
    ;;       {:a a :b b :c c})

    ;; (? :let [[a & b] [1 2 3 4 5]]
    ;;       {:a a :b b})

    ;; (? :let
    ;;     [a                                       (take 5 (range))
    ;;        {:keys [b c d]
    ;;         :or   {d 10
    ;;                b 20
    ;;                c 30}}                          {:c 50
    ;;                                                 :d 100}
    ;;        [e f g &  h]                             ["a" "b" "c" "d" "e"]]
    ;;       [a b c d e f g h])
    )


;; -------------------------------------------------------------
;; TODO - Use the co fn to add some commentary to samples below.
;; -------------------------------------------------------------

;; Testing all the options cljc
#_(do

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
       {"abcdefghijklmnopqrstuvwxyz" [1 2 3]})

    (? {:label :display-namespaces? :display-namespaces?        true}
       [juxt my-record-type])

    (? {:label "display-namespaces? false" :display-namespaces?        false}
       [juxt my-record-type])

    (? {:label             :metadata-position
        :metadata-position "block"}
       (with-meta [(with-meta (symbol "foo") {:my :meta})
                   (with-meta (symbol "foo") {:my :meta})
                   (with-meta (symbol "foo") {:my :meta})]
         {:moo [1 [2 [3 [4 [5]]]]]}))



    (? {:label                :metadata-print-level
        :metadata-print-level 3
        :metadata-position    "inline"}
       (with-meta [] {:moo [1 [2 [3 [4 [5]]]]]}))

    ;; :display-metadata?
    (? {:label             ":display-metadata? false"
        :display-metadata? false
        :metadata-position "inline"}
       (with-meta [(with-meta (symbol "foo") {:my :meta})] {:moo [1 [2 [3 [4 [5]]]]]}))

    ;; :display-metadata?
    (? {:label             :display-metadata?
        :display-metadata? true
        :metadata-position "inline"}
       (with-meta [(with-meta (symbol "foo") {:my :meta})] {:moo [1 [2 [3 [4 [5]]]]]}))

    (? {:label ":enable-rainbow-brackets? false" :enable-rainbow-brackets?   false}
       [[[[[[]]]]]])

    (? {:label  ":bracket-contrast low" :enable-rainbow-brackets?   true :bracket-contrast "low"}
       [[[[[[]]]]]])

    (? {:label ":bracket-contrast high" :enable-rainbow-brackets?   true :bracket-contrast "high"}
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
                :tokens {:classes {:string {:color "#ff0000"}
                                   :comment {:color "#ff00cc"}}
                         :syntax  {:js-object-key {:color "#888888"}}
                         :printer {:function-args {:color "#bb8f44"}}}}}
       {:string "string"}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY END
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


