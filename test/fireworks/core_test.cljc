(ns fireworks.core-test
  (:require [clojure.string :as string]
            [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.config]
            [fireworks.pp :as pp :refer [?pp]]
            [fireworks.smoke-test :as smoke-test]
            [fireworks.demo-tour]
            [fireworks.themes :as themes]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))

;; These tests will break if your local ~/.fireworks/config.edn is different from fireworks.smoke-test/example-config.
;; Change it to that temporarily if you want to run these tests locally. This will be fixed in the near future.
;; By design, all cljs tests that test fireworks.core/p-data in this namespace will break if the line number that they are on changes!


(def theme themes/alabaster-light)
(declare escape-sgr)

#?(:cljs
   (deftest p-data-basic 
     (is (=
          (let [ret (? :data {:theme theme} "foo")] #_(pp/pprint "p-data basic") #_(pp/pprint ret) ret)
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c"
                           :css-styles ["color:#448C27;line-height:1.45;" "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    51,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:22:21",
           :column        21,
           :line          22,
           :end-line      22,
           :formatted+    {:string     "%cfoo%c  %cfireworks.core-test:22:21%c%c \n%c%c\"foo\"%c",
                           :css-styles ["color:#4d6dba;background-color:#edf2fc;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#4d6dba;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:;margin-block-end:0.5em;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]}}))))
#?(:cljs
   (deftest p-data-with-label
     (is (= 
          (let [ret (? :data "my-label" "foo")] #_(pp/pprint "p-data-with-label") #_(pp/pprint ret) ret)
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c"
                           :css-styles ["color:#448C27;line-height:1.45;" "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    47,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:45:21",
           :column        21,
           :line          45,
           :end-line      45,
           :formatted+    {:string     "%cmy-label%c  %cfireworks.core-test:45:21%c%c \n%c%c\"foo\"%c",
                           :css-styles ["color:#4d6dba;background-color:#edf2fc;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#4d6dba;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:;margin-block-end:0.5em;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]}}))))


#?(:cljs
   (do (deftest p-data-with-label-from-opts
         (is (= 
              (let [ret (? :data {:label                 "my-label-from-opts"
                                  :theme                 theme
                                  :non-coll-length-limit (-> fireworks.config/options
                                                             :non-coll-length-limit
                                                             :default)}
                           "foo")]
                ;; (pp/pprint 'p-data-with-label-from-opts)
                ;; (pp/pprint ret)
                ret)
              {:quoted-form   "foo",
               :formatted     {:string     "%c\"foo\"%c",
                               :css-styles ["color:#448C27;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"]},
               :file          "fireworks/core_test.cljc",
               :end-column    34,
               :ns-str        "fireworks.core-test",
               :file-info-str "fireworks.core-test:70:25",
               :column        25,
               :line          70,
               :end-line      75,
               :formatted+    {:string     "%cmy-label-from-opts%c  %cfireworks.core-test:70:25%c%c \n%c%c\"foo\"%c",
                               :css-styles ["color:#4d6dba;background-color:#edf2fc;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#4d6dba;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:;margin-block-end:0.5em;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#448C27;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"]}})))

       
       
       (deftest p-data-with-coll-limit
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :coll-limit                 5
                                               :theme                      theme}
                                        [1 2 3 4 5 6 7 8])
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%c[%c%c1%c %c2%c %c3%c %c4%c %c5%c%c ...+3%c%c]%c")))

       (deftest p-data-with-non-coll-level-1-depth-length-limit
         (is (= 
              (let [ret              (? :data {:label                         "my-label"
                                               :enable-terminal-truecolor?    true
                                               :enable-terminal-italics?      true
                                               :bracket-contrast              "high"
                                               :theme                         theme
                                               :non-coll-depth-1-length-limit 60}
                                        ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%c[%c%c\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44\"%c...%c%c%c]%c")))

       (deftest p-data-with-non-coll-length-limit
         (is (= 
              (let [ret              (? :data {:label                        "my-label"
                                               :enable-terminal-truecolor?   true
                                               :enable-terminal-italics?     true
                                               :bracket-contrast             "high"
                                               :theme                        theme
                                               :non-coll-result-length-limit 42}
                                        "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-with-non-coll-length-limit)
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%c\"asdfffaaaaasdfasdfasdfasdfasdfasdfasd\"...%c")))

       (deftest p-data-rainbow-brackets
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :bracket-contrast           "high"
                                               :theme                      theme}
                                        [[[[[]]]]])
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%c[%c%c[%c%c[%c%c[%c%c[%c%c]%c%c]%c%c]%c%c]%c%c]%c")))
       
       (deftest p-data-record-sample-in-atom
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :bracket-contrast           "high"
                                               :theme                      theme}
                                        (atom smoke-test/record-sample))
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-record-sample-in-atom)
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%cAtom<%c%cFoos%c\n%c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c")))
       

       ;; Leave this out until you support native logging
       (deftest p-data-js-array
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :bracket-contrast           "high"
                                               :theme                      theme}
                                        #js [1 2 3])
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-js-array)
                ;; (pp/pprint formatted-string)
                formatted-string)
              nil)))

       (deftest p-data-js-object
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :bracket-contrast           "high"
                                               :theme                      theme}
                                        #js {:a 1 :b 2})
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-js-object)
                ;; (pp/pprint formatted-string)
                formatted-string)
              nil)))
       
       (deftest p-data-record-sample-in-volatile
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                               :enable-terminal-truecolor? true
                                               :enable-terminal-italics?   true
                                               :bracket-contrast           "high"
                                               :theme                      theme}
                                        (volatile! smoke-test/record-sample))
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-record-sample-in-volatile)
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%cVolatile<%c%cFoos%c\n%c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c")))

       

       ;; Leave this out until you figure out what is going on with transients in node-based testing
       #_(deftest transient-vector
           (is (= 
                (let [ret              (let [x   (transient [1 2 3 4 5 6 7 8 9 0])
                                             ret (? :data {:coll-limit                 7
                                                           :label                      "my-label"
                                                           :enable-terminal-truecolor? true
                                                           :enable-terminal-italics?   true
                                                           :bracket-contrast           "high"
                                                           :theme                      theme}
                                                    x)]
                                         (conj! x 5)
                                         ret)

                      _                (do (println "FUUUU")
                                           (pp/pprint ret))
                      formatted-string (-> ret :formatted :string)]
                  (pp/pprint ret)
                  (pp/pprint 'transient-vector)
                  (pp/pprint formatted-string)
                  formatted-string)
                "%cVolatile<%c%cFoos%c\n%c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c")))


       ;; Leave this out until you support custom printers 
       #_(deftest p-data-custom-printers
           (is (= 
                (let [ret              (p-data {:custom-printers {:vector {:pred        (fn [x] (= x [1 2 3 4]))
                                                                           :f           (fn [x] (into #{} x))
                                                                           :badge-text  " Set💩 "
                                                                           :badge-style {:color            "#000"
                                                                                         :background-color "lime"
                                                                                         :border-radius    "999px"
                                                                                         :text-shadow      "none"
                                                                                         :font-style       "normal"}}}}
                                               [1 2 3 4])
                      formatted-string (-> ret :formatted :string)]
                  (pp/pprint formatted-string)
                  formatted-string)
                "%c Set💩 %c
%c#{%c%c1%c %c2%c %c3%c %c4%c%c}%c")))
       )
 


   :clj #_nil
   (do 
     (deftest  p-data-with-label-from-opts ;; line+column dependant
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      "foo")
                  formatted-string (-> ret :formatted+ :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠3;38;2;77;109;186;48;2;237;242;252〠my-label〠0〠  〠3;38;2;77;109;186〠fireworks.core-test:266:36〠0〠〠〠 \n〠0〠〠38;2;68;140;39〠\"foo\"〠0〠")))

     (deftest p-data-with-label-from-opts-primitive-terminal-emulator ;; line+column dependant
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? false
                                             :enable-terminal-italics?   false
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      "foo")
                  formatted-string (-> ret :formatted+ :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠38;5;61;48;5;255〠my-label〠0〠  〠38;5;61〠fireworks.core-test:279:36〠0〠〠〠 \n〠0〠〠38;5;64〠\"foo\"〠0〠")))

     (deftest p-data-with-non-coll-level-1-depth-length-limit
       (is (= 
            (let [ret              (? :data {:label                         "my-label"
                                             :enable-terminal-truecolor?    true
                                             :enable-terminal-italics?      true
                                             :bracket-contrast              "high"
                                             :theme                         theme

                                             :non-coll-depth-1-length-limit 60}
                                      ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
                  formatted-string (-> ret :formatted :string)]
              (string/join (escape-sgr formatted-string)))
            (str "〠38;5;241〠" "[" "〠0〠" "〠38;2;68;140;39〠" "\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44\"" "〠3;38;2;140;140;140〠" "..." "〠0〠" "〠0〠" "〠38;5;241〠" "]" "〠0〠"))))

     (deftest p-data-with-non-coll-result-length-limit
       (is (= 
            (let [ret              (? :data {:label                        "my-label"
                                             :enable-terminal-truecolor?   true
                                             :enable-terminal-italics?     true
                                             :bracket-contrast             "high"
                                             :theme                        theme
                                             :non-coll-result-length-limit 42}
                                      "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'p-data-with-non-coll-result-length-limit)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;2;68;140;39〠" "\"asdfffaaaaasdfasdfasdfasdfasdfasdfasd\"..." "〠0〠"))))

     (deftest p-data-with-coll-limit
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme

                                             :coll-limit                 5}
                                      [1 2 3 4 5 6 7 8])
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;5;241〠" "[" "〠0〠" "〠38;2;122;62;157〠" "1" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" " " "〠38;2;122;62;157〠" "3" "〠0〠" " " "〠38;2;122;62;157〠" "4" "〠0〠" " " "〠38;2;122;62;157〠" "5" "〠0〠" "〠3;38;2;140;140;140〠" " ...+3" "〠0〠" "〠38;5;241〠" "]" "〠0〠"))))
     
     (deftest p-data-rainbow-brackets
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      [[[[[]]]]])
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;5;241〠" "[" "〠0〠" "〠38;5;32〠" "[" "〠0〠" "〠38;5;208〠" "[" "〠0〠" "〠38;5;28〠" "[" "〠0〠" "〠38;5;128〠" "[" "〠0〠" "〠38;5;128〠" "]" "〠0〠" "〠38;5;28〠" "]" "〠0〠" "〠38;5;208〠" "]" "〠0〠" "〠38;5;32〠" "]" "〠0〠" "〠38;5;241〠" "]" "〠0〠"))))
     
     (deftest p-data-rainbow-brackets-low-contrast
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "low"
                                             :theme                      theme}
                                      [[[[[]]]]])
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;5;245〠" "[" "〠0〠" "〠38;5;74〠" "[" "〠0〠" "〠38;5;179〠" "[" "〠0〠" "〠38;5;106〠" "[" "〠0〠" "〠38;5;177〠" "[" "〠0〠" "〠38;5;177〠" "]" "〠0〠" "〠38;5;106〠" "]" "〠0〠" "〠38;5;179〠" "]" "〠0〠" "〠38;5;74〠" "]" "〠0〠" "〠38;5;245〠" "]" "〠0〠"))))


     (deftest p-data-record-sample-in-atom
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      (atom smoke-test/record-sample))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'p-data-record-sample-in-atom)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠3;38;2;57;137;98;48;2;238;251;238〠Atom<〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠Foos〠0〠\n〠38;5;241;48;2;238;251;238〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;122;62;157〠2〠0〠〠38;5;241;48;2;238;251;238〠}〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠>〠0〠")))

     (deftest p-data-record-sample
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      smoke-test/record-sample)
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'p-data-record-sample)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "Foos" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "{" "〠0〠" "〠38;2;122;62;157〠" ":a" "〠0〠" " " "〠38;2;122;62;157〠" "1" "〠0〠" " " "〠38;2;122;62;157〠" ":b" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "}" "〠0〠"))))

     (deftest p-data-symbol-with-meta
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      (with-meta 'mysym {:foo :bar}))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'p-data-symbol-with-meta)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;2;77;109;186〠" "mysym" "〠0〠" " " "〠38;2;190;85;187;48;2;250;232;253〠" "    " "〠0〠" "〠38;2;190;85;187;48;2;250;232;253〠" "^{" "〠0〠" "〠38;2;190;85;187;48;2;250;232;253〠" ":foo" "〠0〠" "〠38;2;190;85;187;48;2;250;232;253〠" " " "〠0〠" "〠38;2;190;85;187;48;2;250;232;253〠" ":bar" "〠0〠" "〠38;2;190;85;187;48;2;250;232;253〠" "}" "〠0〠"))))

     (deftest transient-vector
       (is (= 
            (let [ret              (let [x   (transient [1 2 3 4 5 6 7 8 9 0])
                                         ret (? :data {:coll-limit                 7
                                                       :label                      "my-label"
                                                       :enable-terminal-truecolor? true
                                                       :enable-terminal-italics?   true
                                                       :bracket-contrast           "high"
                                                       :theme                      theme}
                                                x)]
                                     (conj! x 5)
                                     ret)
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'transient-vector)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠3;38;2;57;137;98;48;2;238;251;238〠TransientVector〠0〠\n〠38;5;241;48;2;238;251;238〠[〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;122;62;157〠3〠0〠 〠38;2;122;62;157〠4〠0〠 〠38;2;122;62;157〠5〠0〠 〠38;2;122;62;157〠6〠0〠 〠38;2;122;62;157〠7〠0〠〠3;38;2;140;140;140〠 ...+3〠0〠〠38;5;241;48;2;238;251;238〠]〠0〠")))
     
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
                                         ret (? :data {:coll-limit                 7
                                                       :label                      "my-label"
                                                       :enable-terminal-truecolor? true
                                                       :enable-terminal-italics?   true
                                                       :bracket-contrast           "high"
                                                       :theme                      theme}
                                                x)]
                                     (conj! x 11)
                                     ret)
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'transient-set)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "TransientHashSet" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "#{" "〠0〠" "〠3;38;2;140;140;140〠" "...+20" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "}" "〠0〠"))))
     
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
                                         ret (? :data {:coll-limit                 7
                                                       :label                      "my-label"
                                                       :enable-terminal-truecolor? true
                                                       :enable-terminal-italics?   true
                                                       :bracket-contrast           "high"
                                                       :theme                      theme}
                                                x)]
                                     (assoc! x :k 11)
                                     ret)
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'transient-set)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "TransientHashMap" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "{" "〠0〠" "〠3;38;2;140;140;140〠" " ......+10" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "}" "〠0〠"))))
     
     (deftest array-map-order
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor? true
                                       :enable-terminal-italics?   true
                                       :bracket-contrast           "high"
                                       :theme                      theme}
                                      (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'array-map-order)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠38;5;241〠" "{" "〠0〠" "〠38;2;122;62;157〠" ":a" "〠0〠" " " "〠38;2;122;62;157〠" "1" "〠0〠" "\n " "〠38;2;122;62;157〠" ":b" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" "\n " "〠38;2;122;62;157〠" ":c" "〠0〠" " " "〠38;2;122;62;157〠" "3" "〠0〠" "\n " "〠38;2;122;62;157〠" ":d" "〠0〠" " " "〠38;2;122;62;157〠" "4" "〠0〠" "\n " "〠38;2;122;62;157〠" ":e" "〠0〠" " " "〠38;2;122;62;157〠" "5" "〠0〠" "\n " "〠38;2;122;62;157〠" ":f" "〠0〠" " " "〠38;2;122;62;157〠" "6" "〠0〠" "\n " "〠38;2;122;62;157〠" ":g" "〠0〠" " " "〠38;2;122;62;157〠" "7" "〠0〠" "\n " "〠38;2;122;62;157〠" ":h" "〠0〠" " " "〠38;2;122;62;157〠" "8" "〠0〠" "\n " "〠38;2;122;62;157〠" ":i" "〠0〠" " " "〠38;2;122;62;157〠" "9" "〠0〠" "\n " "〠38;2;122;62;157〠" ":j" "〠0〠" " " "〠38;2;122;62;157〠" "10" "〠0〠" "〠38;5;241〠" "}" "〠0〠"))))
     
     (deftest single-line-coll-length-limit-50-19
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor?    true
                                       :enable-terminal-italics?      true
                                       :bracket-contrast              "high"
                                       :single-line-coll-length-limit 50
                                       :theme                         theme}
                                      (range 19))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'single-line-coll-length-limit-50-19)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠38;5;241〠(〠0〠〠38;2;122;62;157〠0〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;122;62;157〠3〠0〠 〠38;2;122;62;157〠4〠0〠 〠38;2;122;62;157〠5〠0〠 〠38;2;122;62;157〠6〠0〠 〠38;2;122;62;157〠7〠0〠 〠38;2;122;62;157〠8〠0〠 〠38;2;122;62;157〠9〠0〠 〠38;2;122;62;157〠10〠0〠 〠38;2;122;62;157〠11〠0〠 〠38;2;122;62;157〠12〠0〠 〠38;2;122;62;157〠13〠0〠 〠38;2;122;62;157〠14〠0〠 〠38;2;122;62;157〠15〠0〠 〠38;2;122;62;157〠16〠0〠 〠38;2;122;62;157〠17〠0〠 〠38;2;122;62;157〠18〠0〠〠38;5;241〠)〠0〠")))
     
     (deftest single-line-coll-length-limit-50-20
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor?    true
                                       :enable-terminal-italics?      true
                                       :bracket-contrast              "high"
                                       :single-line-coll-length-limit 50
                                       :theme                         theme}
                                      (range 20))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'array-map-order)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠38;5;241〠(〠0〠〠38;2;122;62;157〠0〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠1〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠2〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠3〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠4〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠5〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠6〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠7〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠8〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠9〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠10〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠11〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠12〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠13〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠14〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠15〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠16〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠17〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠18〠0〠〠38;2;88;88;88〠\n 〠0〠〠38;2;122;62;157〠19〠0〠〠38;5;241〠)〠0〠")))

     (deftest java-util-hashset
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor? true
                                       :enable-terminal-italics?   true
                                       :bracket-contrast           "high"
                                       :theme                      theme}
                                      (java.util.HashSet. #{"a" 1
                                                            "b" 2}))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'java-util-hashset)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            "〠3;38;2;57;137;98;48;2;238;251;238〠java.util.HashSet〠0〠\n〠38;5;241;48;2;238;251;238〠#{〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;68;140;39〠\"a\"〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;238;251;238〠}〠0〠")))
     
     (deftest java-util-hashmap
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor? true
                                       :enable-terminal-italics?   true
                                       :bracket-contrast           "high"
                                       :theme                      theme}
                                      (java.util.HashMap. {"a" 1
                                                           "b" 2}))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'java-util-hashmap)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "java.util.HashMap" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "{" "〠0〠" "〠38;2;68;140;39〠" "\"a\"" "〠0〠" " " "〠38;2;122;62;157〠" "1" "〠0〠" " " "〠38;2;68;140;39〠" "\"b\"" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "}" "〠0〠"))))
     
     (deftest java-util-arraylist
       (is (= 
            (let [ret              (? :data 
                                      {:enable-terminal-truecolor? true
                                       :enable-terminal-italics?   true
                                       :bracket-contrast           "high"
                                       :theme                      theme}
                                      (java.util.ArrayList. [1 2 3]))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint 'java-util-arraylist)
              ;; (pp/pprint (escape-sgr formatted-string))
              (string/join (escape-sgr formatted-string)))
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "java.util.ArrayList" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "[" "〠0〠" "〠38;2;122;62;157〠" "1" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" " " "〠38;2;122;62;157〠" "3" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "]" "〠0〠"))))))

(defn- escape-sgr
  "Escape sgr codes so we can test clj output."
  [s]
  (let [_split   "✂"
        _sgr     "〠"
        replaced (string/replace s
                                 #"\u001b\[([0-9;]*)[mK]"
                                 (str _split _sgr "$1" _sgr _split))
        split    (string/split replaced
                               (re-pattern _split))
        ret      (filter seq split)]
    ret))

;; Basic print-and-return tests, cljc
(do
  (deftest ?-par-result
    (is (= (? :result "par?") "par?")))
  (deftest ?-par
    (is (= (? "par?") "par?")))
  (deftest !?-par
    (is (= (!? "par?") "par?")))
  (deftest ?>-par
    (is (= (?> "par?") "par?")))
  (deftest !?>-par
    (is (= (!?> "par?") "par?")))
  (deftest p-data-basic-samples
         (is (= 
              (let [ret              
                    (? :data 
                       {:label                        "my-label"
                        :enable-terminal-truecolor?   true
                        :enable-terminal-italics?     true
                        :bracket-contrast             "high"
                        :theme                        theme
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
                       smoke-test/basic-samples-cljc)

                    formatted-string 
                    #?(:cljs
                       (-> ret :formatted :string)
                       :clj
                       (string/join (escape-sgr (-> ret :formatted :string))))]
                #?(:clj (do 
                          ;; (pp/pprint 'p-data-basic-samples)
                          ;; (pp/pprint formatted-string)
                          formatted-string)
                   :cljs (do 
                          ;;  (pp/pprint 'p-data-basic-samples)
                          ;;  (pp/pprint formatted-string)
                           formatted-string)))
              #?(:clj
                 "〠38;5;241〠{〠0〠〠38;2;122;62;157〠:abcdefg〠0〠 〠38;5;32〠{〠0〠〠38;2;122;62;157〠:boolean〠0〠            〠38;2;122;62;157〠true〠0〠\n           〠38;2;122;62;157〠:brackets〠0〠           〠38;5;208〠[〠0〠〠38;5;28〠[〠0〠〠38;5;128〠[〠0〠〠38;5;241〠[〠0〠〠38;5;32〠[〠0〠〠38;5;208〠[〠0〠〠38;5;208〠]〠0〠〠38;5;32〠]〠0〠〠38;5;241〠]〠0〠〠38;5;128〠]〠0〠〠38;5;28〠]〠0〠〠38;5;208〠]〠0〠\n           〠38;2;122;62;157〠:fn〠0〠                 〠38;2;77;109;186〠clojure.core/juxt〠0〠〠38;2;153;153;153〠[]〠0〠\n           〠38;2;122;62;157〠:lambda〠0〠             〠3;38;2;57;137;98;48;2;238;251;238〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠\n           〠38;2;122;62;157〠:number〠0〠             〠38;2;122;62;157〠1234〠0〠\n           〠38;2;122;62;157〠:record〠0〠             〠3;38;2;57;137;98;48;2;238;251;238〠Foos〠0〠\n                               〠38;5;208;48;2;238;251;238〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;122;62;157〠2〠0〠〠38;5;208;48;2;238;251;238〠}〠0〠\n           〠38;2;122;62;157〠:regex〠0〠              〠38;2;68;140;39〠#\"^hi$\"〠0〠\n           〠38;2;122;62;157〠:string〠0〠             〠38;2;68;140;39〠\"string\"〠0〠\n           〠38;2;122;62;157〠:symbol〠0〠             〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:foo〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠:bar〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠\n           〠38;2;122;62;157〠:symbol2〠0〠            〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:foo〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠[〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"afasdfasf\"〠0〠〠38;2;88;88;88〠\n                                                 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"afasdfasf\"〠0〠〠38;2;88;88;88〠\n                                                 〠0〠〠38;2;190;85;187;48;2;250;232;253〠{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:a〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"foo\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠:b〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠[〠0〠〠38;2;190;85;187;48;2;250;232;253〠1〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠2〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠[〠0〠〠38;2;190;85;187;48;2;250;232;253〠1〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠2〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠3〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠4〠0〠〠38;2;190;85;187;48;2;250;232;253〠]〠0〠〠38;2;190;85;187;48;2;250;232;253〠]〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠〠38;2;88;88;88〠\n                                                 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"afasdfasf\"〠0〠〠38;2;88;88;88〠\n                                                 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"afasdfasf\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠]〠0〠〠38;2;88;88;88〠\n                                           〠0〠〠38;2;190;85;187;48;2;250;232;253〠:bar〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"fooz\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠\n           〠38;2;122;62;157〠:uuid〠0〠               〠3;38;2;57;137;98;48;2;238;251;238〠#uuid 〠0〠〠38;2;68;140;39〠\"4fe5d828-6444-11e8-822\"〠3;38;2;140;140;140〠...〠0〠〠0〠\n           〠38;2;122;62;157〠:atom/number〠0〠        〠3;38;2;57;137;98;48;2;238;251;238〠Atom<〠0〠〠38;2;122;62;157〠1〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠>〠0〠\n           〠38;2;122;62;157〠:atom/record〠0〠        〠3;38;2;57;137;98;48;2;238;251;238〠Atom<〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠Foos〠0〠\n                               〠38;5;208;48;2;238;251;238〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;122;62;157〠2〠0〠〠38;5;208;48;2;238;251;238〠}〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠>〠0〠\n           〠38;2;122;62;157〠:map/multi-line〠0〠     〠38;5;208〠{〠0〠〠38;2;122;62;157〠:abc〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;68;140;39〠\"bar\"〠0〠〠38;2;88;88;88〠\n                                \n                                〠0〠〠38;2;68;140;39〠\"asdfasdfa\"〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuvwxyzzz\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;88;88;88〠\n                                \n                                〠0〠〠38;5;28〠[〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;122;62;157〠:b〠0〠〠38;5;28〠]〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;122;62;157〠123444〠0〠〠38;5;208〠}〠0〠\n           〠38;2;122;62;157〠:map/nested-meta〠0〠    〠38;5;208;48;2;250;232;253〠{〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:a〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠foo〠0〠 〠38;2;159;96;190;48;2;233;229;255〠    〠0〠〠38;2;159;96;190;48;2;233;229;255〠^{〠0〠〠38;2;159;96;190;48;2;233;229;255〠:abc〠0〠〠38;2;159;96;190;48;2;233;229;255〠 〠0〠〠38;2;159;96;190;48;2;233;229;255〠bar〠0〠〠38;2;88;88;88〠\n                                                    〠0〠〠38;2;159;96;190;48;2;233;229;255〠:xyz〠0〠〠38;2;159;96;190;48;2;233;229;255〠 〠0〠〠38;2;159;96;190;48;2;233;229;255〠\"abcdefghijklmnopqrstuvwxyzzzz\"〠38;2;159;96;190;48;2;233;229;255〠...〠0〠〠0〠〠38;2;159;96;190;48;2;233;229;255〠}〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;77;109;186〠a〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:abc〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"bar\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠:xyz〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"abc\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;77;109;186〠foo〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:abc〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"bar\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠:xyz〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"abc\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠〠38;2;88;88;88〠\n                                \n                                〠0〠〠38;2;122;62;157〠:b〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;208;48;2;250;232;253〠}〠0〠\n           〠38;2;122;62;157〠:map/single-line〠0〠    〠38;5;208〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;122;62;157〠:c〠0〠 〠38;2;68;140;39〠\"three\"〠0〠〠38;5;208〠}〠0〠\n           〠38;2;122;62;157〠:set/multi-line〠0〠     〠38;5;208〠#{〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuvwxyzzz\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;88;88;88〠\n                                 〠0〠〠38;2;122;62;157〠3333333〠0〠〠38;2;88;88;88〠\n                                 〠0〠〠38;2;122;62;157〠:22222〠0〠〠38;5;208〠}〠0〠\n           〠38;2;122;62;157〠:set/single-line〠0〠    〠38;5;208〠#{〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;68;140;39〠\"three\"〠0〠 〠38;2;122;62;157〠:2〠0〠〠38;5;208〠}〠0〠\n           〠38;2;122;62;157〠:vector/multi-line〠0〠  〠38;5;208〠[〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuvwxyzzz\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;122;62;157〠:22222〠0〠〠38;2;88;88;88〠\n                                〠0〠〠38;2;122;62;157〠3333333〠0〠〠38;5;208〠]〠0〠\n           〠38;2;122;62;157〠:vector/single-line〠0〠 〠38;5;208〠[〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠:2〠0〠 〠38;2;68;140;39〠\"three\"〠0〠〠38;5;208〠]〠0〠〠38;5;32〠}〠0〠〠38;5;241〠}〠0〠"
                 :cljs
                 "%c{%c%c:abcdefg%c %c{%c%c:boolean%c            %ctrue%c\n           %c:brackets%c           %c[%c%c[%c%c[%c%c[%c%c[%c%c[%c%c]%c%c]%c%c]%c%c]%c%c]%c%c]%c\n           %c:fn%c                 %ccljs.core/juxt%c%c[var_args]%c\n           %c:lambda%c             %cλ%c%c%c%c[%1]%c\n           %c:number%c             %c1234%c\n           %c:record%c             %cFoos%c\n                               %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c\n           %c:regex%c              %c#\"^hi$\"%c\n           %c:string%c             %c\"string\"%c\n           %c:symbol%c             %cmysym%c %c    %c%c^{%c%c:foo%c %c:bar%c%c}%c\n           %c:symbol2%c            %cmysym%c %c    %c%c^{%c%c:foo%c %c[%c%c\"afasdfasf\"%c%c\n                                                 %c%c\"afasdfasf\"%c%c\n                                                 %c%c{%c%c:a%c %c\"foo\"%c%c %c%c:b%c %c[%c%c1%c%c %c%c2%c%c %c%c[%c%c1%c%c %c%c2%c%c %c%c3%c%c %c%c4%c%c]%c%c]%c%c}%c%c\n                                                 %c%c\"afasdfasf\"%c%c\n                                                 %c%c\"afasdfasf\"%c%c]%c%c\n                                           %c%c:bar%c %c\"fooz\"%c%c}%c\n           %c:uuid%c               %c#uuid %c%c\"4fe5d828-6444-11e8-822\"%c...%c%c\n           %c:atom/number%c        %cAtom<%c%c1%c%c>%c\n           %c:atom/record%c        %cAtom<%c%cFoos%c\n                               %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c\n           %c:map/multi-line%c     %c{%c%c:abc%c%c\n                                %c%c\"bar\"%c%c\n                                \n                                %c%c\"asdfasdfa\"%c%c\n                                %c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                \n                                %c%c[%c%c:a%c %c:b%c%c]%c%c\n                                %c%c123444%c%c}%c\n           %c:map/nested-meta%c    %c{%c %c    %c%c^{%c%c:a%c %cfoo%c %c    %c%c^{%c%c:abc%c %cbar%c%c\n                                                    %c%c:xyz%c %c\"abcdefghijklmnopqrstuvwxyzzzz\"%c...%c%c%c}%c%c}%c%c\n                                %c%ca%c %c    %c%c^{%c%c:abc%c %c\"bar\"%c%c %c%c:xyz%c %c\"abc\"%c%c}%c%c\n                                %c%cfoo%c %c    %c%c^{%c%c:abc%c %c\"bar\"%c%c %c%c:xyz%c %c\"abc\"%c%c}%c%c\n                                \n                                %c%c:b%c%c\n                                %c%c2%c%c}%c\n           %c:map/single-line%c    %c{%c%c:a%c %c1%c %c:b%c %c2%c %c:c%c %c\"three\"%c%c}%c\n           %c:set/multi-line%c     %c#{%c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                 %c%c3333333%c%c\n                                 %c%c:22222%c%c}%c\n           %c:set/single-line%c    %c#{%c%c1%c %c\"three\"%c %c:2%c%c}%c\n           %c:vector/multi-line%c  %c[%c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                %c%c:22222%c%c\n                                %c%c3333333%c%c]%c\n           %c:vector/single-line%c %c[%c%c1%c %c:2%c %c\"three\"%c%c]%c%c}%c%c}%c")))))


;; TODO - Add tests for:
;; :when pred option for selective printing
;; correct margins when using margin options
;; correct margins when using :result flag
;; Java Objects from java.util.*
;; More native js Objects

;; Other transient ClojureScript types (strange in node testing)

;; Add :use-default-config option which bypasses config.edn and just always uses
;; "Universal Default" theme, as well as default config options.
