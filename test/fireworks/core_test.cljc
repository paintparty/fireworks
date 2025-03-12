(ns fireworks.core-test
  (:require 
   [clojure.string :as string]
   [fireworks.core :refer [? !? ?> !?>]]
   [fireworks.config]
   [fireworks.pp :as pp :refer [?pp !?pp]]
   [lasertag.core :refer [tag-map]]
   [fireworks.demo]
   [fireworks.themes :as themes] 
   [fireworks.sample :as sample] 
   #?(:cljs [cljs.test :refer [deftest is]])
   #?(:clj [clojure.test :refer :all])
   [clojure.spec.alpha :as s]))

;; These tests will break if your local ~/.fireworks/config.edn is different from fireworks.smoke-test/example-config.
;; Change it to that temporarily if you want to run these tests locally. This will be fixed in the near future.
;; By design, all cljs tests that test fireworks.core/p-data in this namespace will break if the line number that they are on changes!


(def theme themes/alabaster-light-legacy)
(declare escape-sgr)

#?(:cljs
   (deftest p-data-basic 
     (is (=
          (let [ret (? :data {:theme theme} "foo")] (?pp 'p-data-basic ret))
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c",
                           :css-styles ["color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    51,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:24:21",
           :column        21,
           :line          24,
           :end-line      24,
           :formatted+    {:string     "%cfoo%c  %cfireworks.core-test:24:21%c%c \n%c%c\"foo\"%c",
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
          (let [ret (? :data "my-label" "foo")] (?pp 'p-data-with-label ret))
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c",
                           :css-styles ["color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    47,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:48:21",
           :column        21,
           :line          48,
           :end-line      48,
           :formatted+    {:string     "%cmy-label%c  %cfireworks.core-test:48:21%c%c \n%c%c\"foo\"%c",
                           :css-styles ["color:#3764cd;background-color:#edf2fc;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#3764cd;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:;margin-block-end:0.5em;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]}}))))


#?(:cljs
   nil

   :clj
   (do 
     #_(deftest  p-data-with-label-from-opts ;; line+column dependant
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

     #_(deftest p-data-with-label-from-opts-primitive-terminal-emulator ;; line+column dependant
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
                                      (atom sample/my-record-type))
                  formatted-string (-> ret :formatted :string)
                  escaped          (string/join (escape-sgr formatted-string))]
              #_(?pp 'p-data-record-sample-in-atom escaped)
              escaped)
            "〠3;38;2;57;137;98;48;2;238;251;238〠Atom<〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠MyRecordType〠0〠\n〠38;5;241;48;2;238;251;238〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;68;140;39〠\"a\"〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;238;251;238〠}〠0〠〠3;38;2;57;137;98;48;2;238;251;238〠>〠0〠")))

     (deftest p-data-record-sample
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true
                                             :bracket-contrast           "high"
                                             :theme                      theme}
                                      sample/my-record-type)
                  formatted-string (-> ret :formatted :string)
                  escaped          (string/join (escape-sgr formatted-string))]
              #_(?pp 'p-data-record-sample escaped)
              escaped)
            "〠3;38;2;57;137;98;48;2;238;251;238〠MyRecordType〠0〠\n〠38;5;241;48;2;238;251;238〠{〠0〠〠38;2;122;62;157〠:a〠0〠 〠38;2;68;140;39〠\"a\"〠0〠 〠38;2;122;62;157〠:b〠0〠 〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;238;251;238〠}〠0〠")))

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
            (str "〠3;38;2;57;137;98;48;2;238;251;238〠" "java.util.ArrayList" "〠0〠" "\n" "〠38;5;241;48;2;238;251;238〠" "[" "〠0〠" "〠38;2;122;62;157〠" "1" "〠0〠" " " "〠38;2;122;62;157〠" "2" "〠0〠" " " "〠38;2;122;62;157〠" "3" "〠0〠" "〠38;5;241;48;2;238;251;238〠" "]" "〠0〠"))))

     (deftest java-interop-types
       (is (= 
            (let [ret              (? :data
                                      {:coll-limit 100
                                       :label      "JVM Clojure Values"}
                                      sample/interop-types)
                  formatted-string (-> ret :formatted :string)
                  escaped          (string/join (escape-sgr formatted-string))]
              ;; (pp/pprint 'java-interop-types)
              ;; (pp/pprint (escape-sgr formatted-string))
              (!?pp 'java-interop-types escaped))
            "〠38;5;241〠{〠0〠〠38;2;68;140;39〠\"Java collection types\"〠0〠 〠38;5;32〠{〠0〠〠38;2;77;109;186〠java.util.ArrayList〠0〠 〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n                                              〠38;5;208;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠0〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;122;62;157〠3〠0〠 〠38;2;122;62;157〠4〠0〠 〠38;2;122;62;157〠5〠0〠〠38;5;208;48;2;255;249;245〠]〠0〠\n                          〠38;2;77;109;186〠java.util.HashMap〠0〠   〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n                                              〠38;5;208;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"a\"〠0〠 〠38;2;122;62;157〠1〠0〠 〠38;2;68;140;39〠\"b\"〠0〠 〠38;2;122;62;157〠2〠0〠〠38;5;208;48;2;255;249;245〠}〠0〠\n                          〠38;2;77;109;186〠java.util.HashSet〠0〠   〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n                                              〠38;5;208;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;68;140;39〠\"a\"〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;68;140;39〠\"b\"〠0〠〠38;5;208;48;2;255;249;245〠}〠0〠\n                          〠38;2;77;109;186〠java.lang.String〠0〠    〠38;2;68;140;39〠\"welcome\"〠0〠\n                          〠38;2;77;109;186〠array〠0〠               〠3;38;2;199;104;35;48;2;255;249;245〠Ljava.lang.Object〠0〠\n                                              〠38;5;208;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠 〠38;2;122;62;157〠2〠0〠 〠38;2;122;62;157〠3〠0〠 〠38;2;122;62;157〠4〠0〠 〠38;2;122;62;157〠5〠0〠〠38;5;208;48;2;255;249;245〠]〠0〠〠38;5;32〠}〠0〠\n 〠38;2;68;140;39〠\"Java numbers\"〠0〠          〠38;5;32〠{〠0〠〠38;2;122;62;157〠:ratio〠0〠               〠38;2;122;62;157〠1/3〠0〠\n                          〠38;2;122;62;157〠:byte〠0〠                〠38;2;122;62;157〠0〠0〠\n                          〠38;2;122;62;157〠:short〠0〠               〠38;2;122;62;157〠3〠0〠\n                          〠38;2;122;62;157〠:double〠0〠              〠38;2;122;62;157〠23.44〠0〠\n                          〠38;2;122;62;157〠:decimal〠0〠             〠38;2;122;62;157〠1M〠0〠\n                          〠38;2;122;62;157〠:int〠0〠                 〠38;2;122;62;157〠1〠0〠\n                          〠38;2;122;62;157〠:float〠0〠               〠38;2;122;62;157〠1.5〠0〠\n                          〠38;2;122;62;157〠:char〠0〠                〠38;2;88;88;88〠a〠0〠\n                          〠38;2;122;62;157〠:java.math.BigInt〠3;38;2;140;140;140〠...〠0〠〠0〠 〠38;2;122;62;157〠171〠0〠〠38;5;32〠}〠0〠〠38;5;241〠}〠0〠")))
     ))

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
  ;; (deftest ?-par-result
  ;;   (is (= (? :result "par?") "par?")))
  ;; (deftest ?-par
  ;;   (is (= (? "par?") "par?")))
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
                       sample/array-map-of-everything-cljc)

                    formatted-string 
                    #?(:cljs
                       (-> ret :formatted :string)
                       :clj
                       (string/join (escape-sgr (-> ret :formatted :string))))]
                #?(:clj (do (!?pp 'p-data-basic-samples formatted-string))
                   :cljs (do (!?pp 'p-data-basic-samples formatted-string))))
              #?(:clj
                 "〠38;5;241〠{〠0〠〠38;2;122;62;157〠:string〠0〠           〠38;2;68;140;39〠\"string\"〠0〠\n 〠38;2;122;62;157〠:regex〠0〠            〠38;2;68;140;39〠#\"myregex\"〠0〠\n 〠38;2;122;62;157〠:uuid〠0〠             〠3;38;2;57;137;98;48;2;238;251;238〠#uuid 〠0〠〠38;2;68;140;39〠\"4fe5d828-6444-11e8-8222\"〠3;38;2;140;140;140〠...〠0〠〠0〠\n 〠38;2;122;62;157〠:symbol〠0〠           〠38;2;77;109;186〠mysym〠0〠\n 〠38;2;122;62;157〠:symbol+meta〠0〠      〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;250;232;253〠    〠0〠〠38;2;190;85;187;48;2;250;232;253〠^{〠0〠〠38;2;190;85;187;48;2;250;232;253〠:foo〠0〠〠38;2;190;85;187;48;2;250;232;253〠 〠0〠〠38;2;190;85;187;48;2;250;232;253〠\"bar\"〠0〠〠38;2;190;85;187;48;2;250;232;253〠}〠0〠\n 〠38;2;122;62;157〠:boolean〠0〠          〠38;2;122;62;157〠true〠0〠\n 〠38;2;122;62;157〠:keyword〠0〠          〠38;2;122;62;157〠:keyword〠0〠\n 〠38;2;122;62;157〠:nil〠0〠              〠38;2;122;62;157〠nil〠0〠\n 〠38;2;122;62;157〠:##Nan〠0〠            〠38;2;88;88;88〠NaN〠0〠\n 〠38;2;122;62;157〠:##Inf〠0〠            〠38;2;88;88;88〠Infinity〠0〠\n 〠38;2;122;62;157〠:##-Inf〠0〠           〠38;2;88;88;88〠-Infinity〠0〠\n 〠38;2;122;62;157〠:int〠0〠              〠38;2;122;62;157〠1234〠0〠\n 〠38;2;122;62;157〠:float〠0〠            〠38;2;122;62;157〠3.33〠0〠\n 〠38;2;122;62;157〠:lambda〠0〠           〠3;38;2;57;137;98;48;2;238;251;238〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:lambda-2-args〠0〠    〠3;38;2;57;137;98;48;2;238;251;238〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:core-fn〠0〠          〠38;2;77;109;186〠clojure.core/juxt〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:date-fn〠0〠          〠38;2;77;109;186〠java.util/Date〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:datatype-class〠0〠   〠38;2;77;109;186〠fireworks.sample/MyType〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:recordtype-class〠0〠 〠38;2;77;109;186〠fireworks.sample/MyRecordType〠0〠〠38;2;153;153;153〠[]〠0〠\n 〠38;2;122;62;157〠:really-long-fn〠0〠   〠38;2;77;109;186〠xyasldfasldkfaslkjfzzzzzzzzzz〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;153;153;153〠[]〠0〠〠3;38;2;140;140;140〠\n ...               ...+14〠0〠〠38;5;241〠}〠0〠"
                 :cljs
                 "%c{%c%c:string%c           %c\"string\"%c\n %c:regex%c            %c#\"myregex\"%c\n %c:uuid%c             %c#uuid %c%c\"4fe5d828-6444-11e8-8222\"%c...%c%c\n %c:symbol%c           %cmysym%c\n %c:symbol+meta%c      %cmysym%c %c    %c%c^{%c%c:foo%c %c\"bar\"%c%c}%c\n %c:boolean%c          %ctrue%c\n %c:keyword%c          %c:keyword%c\n %c:nil%c              %cnil%c\n %c:##Nan%c            %cNaN%c\n %c:##Inf%c            %cInfinity%c\n %c:##-Inf%c           %c-Infinity%c\n %c:int%c              %c1234%c\n %c:float%c            %c3.33%c\n %c:lambda%c           %cλ%c%c%c%c[]%c\n %c:lambda-2-args%c    %cλ%c%c%c%c[%1 %2]%c\n %c:core-fn%c          %ccljs.core/juxt%c%c[var_args]%c\n %c:date-fn%c          %cjs/Date%c%c[]%c\n %c:datatype-class%c   %cfireworks.sample/MyType%c%c[a b]%c\n %c:recordtype-class%c %cfireworks.sample/MyRecordType%c%c[a b]%c\n %c:really-long-fn%c   %cxyasldfasldkfaslkjfzzzzzzz%c...%c%c%c[x y]%c%c\n ...               ...+14%c%c}%c")))))


;; TODO - Add tests for:
;; :when pred option for selective printing
;; correct margins when using margin options
;; correct margins when using :result flag
;; Java Objects from java.util.*
;; More native js Objects

;; Other transient ClojureScript types (strange in node testing)

;; Add :use-default-config option which bypasses config.edn and just always uses
;; "Universal Default" theme, as well as default config options.

;; TODO - this should not print multiline, it is b/c label is being counted in string length

(? {1M :foo
    1 :bar})
