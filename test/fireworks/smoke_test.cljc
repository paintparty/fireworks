;; Unstructured sandbox for smoke testing

(ns fireworks.smoke-test
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.themes :as themes]
            [doric.core :refer [table]]
            [bling.core :refer [callout bling bling! ?sgr]]
            [bling.sample]
            [bling.banner]
            [bling.fonts]
            [bling.fontlib]
            
            [clojure.string :as string]
            [fireworks.pp :as pp]
            [clojure.pprint :refer [pprint print-table]]
            [clojure.walk :as walk]
            [fireworks.util :as util]
            [fireworks.sample :as sample]
            [lasertag.core :refer [tag-map tag]]
            ;; [lambdaisland.ansi :as ansi]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))


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
    (java.util.Date.)
   ])

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



;; (? (color-rows gr 9))

#_(println (table [{:name 'form :title "form" }
                 {:name 'lasertag.core/tag :title "lasertag.core/tag"}
                 {:name 'clojure.core/type :title "clojure.core/type"}]
                lasertag-sample))

;; (? (bling.fonts/banner-font-array-map "Big Money-nw"))
;; (? bling.fonts/ascii-chars-by-index-map)
;; (? bling.fonts/ascii-indices-by-chars)
                                                                             
;; Do a grid generator thing
#_(doseq [grd #_[
            ;;  "to top, green, blue"
               "to bottom, green, blue"
              ;;  "to right, green, blue"
            ;;  "to top, red, magenta"
               "to bottom, red, magenta"
              ;;  "to right, red, magenta"
            ;;  "to top, orange, purple"
               "to bottom, orange, purple"
              ;;  "to right, orange, purple"
            ;;  "to top, yellow, purple"
              ;;  "to bottom, dark-yellow, dark-purple"
              ;;  "to right, dark-yellow, dark-purple"
               "to right, yellow, purple"
              ;;  "to right, yellow, purple"
              ;;  "to bottom, light-yellow, light-purple"
              ;;  "to right, light-yellow, light-purple"
               ]
        [
         "to top, red, magenta"
        ;;  "to top, green, blue"
         ]
        ]
  (let [opts {
              ;; :font           "Big Money-nw"
              ;; :font           "Drippy"
              :font            "Big"
              ;; :font           "Miniwi"
              ;; :font        "ANSI Shadow"
              ;; :dev/print-font! true
              :font-weight     :bold
              :text            "go\ngo"#_(? (->> bling.fonts/ascii-chars
                                       (partition 16)
                                       (map string/join)
                                       (take 10)
                                       (string/join "\n")
                                   ))
              :letter-spacing  0
              :margin-top      1
              :margin-left     0
              :margin-bottom   1
              :gradient        grd
              ;; :contrast       :medium
              }]

    (? :-
       {:margin-top 1}
       (dissoc opts :margin-right :margin-top :margin-left :margin-bottom))

    (bling! (bling.banner/banner opts))))

#_(bling! (bling.banner/banner 
             {
              :dev/display-missing-chars? true
              :font                       "Drippy"
              :font-weight                :bold
              :text                       "(+ a b)"
              :gradient                   "to bottom, green, blue"
              ;; :contrast       :medium
              }))

;; (? (nth ["3" "4" "5"] 2))

#_(doseq 
 ;; for realease
 [font [
        ;; bling.fonts/miniwi
        bling.fonts/ansi-shadow
        ;; bling.fonts/drippy
        ;; bling.fonts/big
        ;; bling.fonts/big-money
        ;; bling.fonts/rounded
        ;; bling.fonts/isometric-1
        ]]

 ;; for dev, creating fonts dynamically from maps in bling.fontlib, with raw
 ;; figlet strings.
 #_[font [(get bling.fontlib/fonts-by-sym 'miniwi)
        (get bling.fontlib/fonts-by-sym 'ansi-shadow)
        (get bling.fontlib/fonts-by-sym 'drippy)
        (get bling.fontlib/fonts-by-sym 'big)
        (get bling.fontlib/fonts-by-sym 'big-money)
        (get bling.fontlib/fonts-by-sym 'rounded)
        (get bling.fontlib/fonts-by-sym 'isometric-1)]]

 (doseq [gradient ["to top, green, blue"
                   "to right, warm, cool"]]
   (bling!
    "\n"
    "\n"
    (:font-name font)
    "\n"
    (bling.banner/banner 
     {:font            font
      :font-weight     :bold
      :text            "Bling"
      :gradient        gradient 
      :margin-top      1
      :dev/print-font! true
   ;; :contrast       :medium
   ;; :display-missing-chars? true
      })))
#_(bling!
 "\n"
 "\n"
 (:font-name font)
 "\n"
 (bling.banner/banner 
  {:font            font
   :font-weight     :bold
   :text            "Bling"
   :gradient        "to left, red, magenta"
   :margin-top      1
   :dev/print-font! true
   ;; :contrast       :medium
   ;; :display-missing-chars? true
   }))

  #_(doseq [ln (->> bling.fonts/ascii-chars
                  (partition 16)
                  ;; (take 1)
                  (map string/join)
                  (take 10))]
    (bling! (bling.banner/banner 
             {:font        font
              :font-weight :bold
              :text        ln 
              :gradient    "to bottom, green, blue"
              ;; :contrast       :medium
              ;; :display-missing-chars? true
              }))))


;; (? (count ascii-chars-partitioned-6-rows-str))


#_(doseq [font [
              (get bling.fontlib/fonts-by-sym 'miniwi)
              (get bling.fontlib/fonts-by-sym 'ansi-shadow)
              (get bling.fontlib/fonts-by-sym 'drippy)
              (get bling.fontlib/fonts-by-sym 'big)
              (get bling.fontlib/fonts-by-sym 'big-money)
              (get bling.fontlib/fonts-by-sym 'rounded)
              (get bling.fontlib/fonts-by-sym 'isometric-1)
              ]
        :let [gs 3
              ;; fw "normal"
              fw "bold"]]
  (doseq [row ascii-chars-partitioned-6-rows-str
          :let [t row]]
    (bling! 
     (bling.banner/banner 
      {
       :font           font
       :text           t
       :font-weight    fw
       :gradient       "to right, cool, warm"
       :gradient-shift gs
   ;; :contrast       :medium
   ;; :display-missing-chars? true
       })
     "\n"
     (bling.banner/banner 
        {
         :font           font
         :text           t
         :font-weight    fw
         :gradient       "to bottom, cool, warm"
         :gradient-shift gs
   ;; :contrast       :medium
   ;; :display-missing-chars? true
         }))))

#_(doseq [grd
        (flatten
         (concat (mapv (fn [[[c1 c2]]]
                         (mapv #(string/join ", " [(str "to " %) c1 c2])
                               ["right" "left" "top" "bottom"]))
                       bling.banner/gradient-ranges)
                 (mapv (fn [[[c1 c2]]]
                         (mapv #(string/join ", " [(str "to " %) c1 c2])
                               ["right" "left" "top" "bottom"]))
                       bling.banner/gradient-ranges-cool-warm)))]
  (bling! (bling.banner/banner 
             {:font        bling.fonts/miniwi
              :font-weight :bold
              :text        "ABCDEFG"
              :gradient    "to bottom, green, blue"
              ;; :contrast       :medium
              ;; :display-missing-chars? true
              })))


;; Dark and light variants
;; (print (bling [:bold.red "gogogogo"]))
;; (print (bling [{:color 124 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 203 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.orange "gogogogo"]))
;; (print (bling [{:color 166 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 214 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.yellow "gogogogo"]))
;; (print (bling [{:color 136 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 220 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.olive "gogogogo"]))
;; (print (bling [{:color 100 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 143 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.green "gogogogo"]))
;; (print (bling [{:color 28 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 82 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.blue "gogogogo"]))
;; (print (bling [{:color 26 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 81 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.purple "gogogogo"]))
;; (print (bling [{:color 129 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 147 :font-weight :bold} "gogogogo"]))

;; (print (bling [:bold.magenta "gogogogo"]))
;; (print (bling [{:color 163 :font-weight :bold} "gogogogo"]))
;; (println (bling [{:color 213 :font-weight :bold} "gogogogo"]))

;; (println (bling [:bold.magenta "gogogogosss"]))
;; (print (bling [:bold.dark-magenta "gogogogo"]))
;; (println (bling [:bold.light-magenta "gogogogo"]))

;; (println (bling [:bold.magenta.magenta-bg "gogogogo"]))
;; (println (bling [{:color :magenta :background-color :magenta :font-weight :bold} "gogogogo"]))
 
;; (bling.sample/all-the-colors)
;; (println)

;; (bling! [:white.bold.red-bg "goo"])


;; (bling.sample/all-the-colors {:variant "dark" :label "dark variants"})
;; (bling.sample/all-the-colors {:variant "medium" :label "medium variants"})
;; (bling.sample/all-the-colors {:variant "light" :label "light variants"})
;; (bling.sample/all-the-colors {:label (str "with user theme\n"
;;                                           "user env var: BLING_THEME=\"" bling.core/BLING_THEME "\"\n"
;;                                           "bling.core/bling-theme resolves to: \"" bling.core/bling-theme "\""
;;                                           )})

;; (bling! [:dark-red "must have changed"])
;; (bling! [:light-red "must have changed"])
;; (bling! [:medium-red "must have changed"])
;; (bling! [:red "must have changed"])

;; (prn bling.fonts/ansi-shadow-str)


;; (println (?sgr (bling [:bold.medium-orange "^^^^"])))

;; (callout 1)

;; (println)


;; (println (bling [{:background-color :red :color :white :font-weight :bold} "gogogogo"]))

;; (bling.sample/sample)

;; (bling.sample/example-custom-callout-foo)

;; (println (bling [{:background-color :green :font-weight :bold} "gogogogo"]))
;; (println (bling [{:background-color :green :font-weight :bold} "gogogogo"]))

;; (bling! [:light-red "light-red"])
;; (bling! [:medium-red "cool"])

;; (println (?sgr (bling [:subtle.italic "cool"])))
;; (println (?sgr (bling [:subtle "cool"])))

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

#_(bling.sample/sample)




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY START
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO - Add more commentary and thorough examples

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
    (? :label {:label "Label from options" } (atom {:a "foo" :b 12}))
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
      (? :log {:label "With label from options" } (atom {:a "foo" :abc "bar"}))
      (? :log (def x10 "x10"))
      (? :log {:label "def with label from options"} (def x11 "x11")))
  
   #_(do 
      (co :pp "Uses pprint.\nReturns result.")
      (? :pp "my label" {:a "foo" :abc "bar"})
      (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
               {:a "foo" :abc "bar"})
        (println "✓"))
      (? :pp "Line1\nLine2" {:a "foo" :abc "bar"})
      (? :pp {:label "With label from options" } (atom {:a "foo" :abc "bar"}))
      (? :pp (def x10 "x10"))
      (? :pp {:label "def with label from options"} (def x11 "x11")))

   #_(do 
      (co :log- "Uses js/console.log or pprint.\nReturns result.")
      (? :log- "my label" {:a "foo" :abc "bar"})
      (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
               {:a "foo" :abc "bar"})
        (println "✓"))
      (? :log- "Line1\nLine2" {:a "foo" :abc "bar"})
      (? :log- {:label "With label from options" } (atom {:a "foo" :abc "bar"}))
      (? :log- (def x10 "x10"))
      (? :log- {:label "def with label from options"} (def x11 "x11")))

   #_(do 
      (co :pp- "Uses js/console.log or pprint.\nReturns result.")
      (? :pp- "my label" {:a "foo" :abc "bar"})
      (when (= (? :log {:margin-bottom 0} {:a "foo" :abc "bar"})
               {:a "foo" :abc "bar"})
        (println "✓"))
      (? :pp- "Line1\nLine2" {:a "foo" :abc "bar"})
      (? :pp- {:label "With label from options" } (atom {:a "foo" :abc "bar"}))
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
     {"abcdefghijklmnopqrstuvwxyz" [1 2 3] })

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
              :tokens {:classes {:string {:color "#ff0000"}
                                 :comment {:color "#ff00cc"}}
                       :syntax  {:js-object-key {:color "#888888"}}
                       :printer {:function-args {:color "#bb8f44"}}}}}
   {:string "string"})


  )  


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; SMOKE TESTS WITH COMMENTARY END
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


