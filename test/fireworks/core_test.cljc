(ns fireworks.core-test
  (:require [clojure.string :as string]
            [fireworks.core :refer [? !? ?> !?>]]
            [fireworks.config]
            [fireworks.pp :as pp :refer [?pp]]
            [fireworks.smoke-test :as smoke-test]
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
          (let [ret (? :data {:theme theme} "foo")] #_ #_(pp/pprint "p-data basic") (pp/pprint ret) ret)
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c"
                           :css-styles []},
           :file          "fireworks/core_test.cljc",
           :end-column    51,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:22:21",
           :column        21,
           :line          22,
           :end-line      22,
           :formatted+    {:string     "%cfoo%c  %cfireworks.core-test:22:21%c%c \n%c%c\"foo\"%c",
                           :css-styles ["color:#2e6666;background-color:#e5f1fa;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#2e6666;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
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
                           :css-styles []},
           :file          "fireworks/core_test.cljc",
           :end-column    47,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:45:21",
           :column        21,
           :line          45,
           :end-line      45,
           :formatted+    {:string     "%cmy-label%c  %cfireworks.core-test:45:21%c%c \n%c%c\"foo\"%c",
                           :css-styles ["color:#2e6666;background-color:#e5f1fa;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#2e6666;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
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
               :formatted     {:string     "%c\"foo\"%c"
                               :css-styles []},
               :file          "fireworks/core_test.cljc",
               :end-column    39,
               :ns-str        "fireworks.core-test",
               :file-info-str "fireworks.core-test:71:25",
               :column        25,
               :line          71,
               :end-line      76,
               :formatted+    {:string     "%cmy-label-from-opts%c  %cfireworks.core-test:71:25%c%c \n%c%c\"foo\"%c",
                               :css-styles ["color:#2e6666;background-color:#e5f1fa;text-shadow:0 0 2px #ffffff;font-style:italic;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#2e6666;font-style:italic;padding-inline-start:0ch;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:;margin-block-end:0.5em;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#448C27;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"]}})))

       (deftest p-data-basic-samples
         (is (= 
              (let [ret              (? :data {:label                        "my-label"
                                              :enable-terminal-truecolor?   true
                                              :enable-terminal-italics?     true
                                              :bracket-contrast             "high"
                                              :theme                        theme
                                              :custom-printers              {}
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
                    formatted-string (-> ret :formatted :string)]
                ;; (pp/pprint 'p-data-basic-samples)
                ;; (pp/pprint formatted-string)
                formatted-string)
                "%c{%c%c:abcdefg%c %c{%c%c:boolean%c            %ctrue%c\n           %c:brackets%c           %c[%c%c[%c%c[%c%c[%c%c[%c%c[%c%c]%c%c]%c%c]%c%c]%c%c]%c%c]%c\n           %c:fn%c                 %ccljs.core/juxt%c%c[var_args]%c\n           %c:lamda%c              %cλ%c%c%c%c[%1]%c\n           %c:number%c             %c1234%c\n           %c:record%c             %cFoos%c\n                               %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c\n           %c:regex%c              %c#\"^hi$\"%c\n           %c:string%c             %c\"string\"%c\n           %c:symbol%c             %cmysym%c %c    %c%c^{%c%c:foo%c %c:bar%c%c}%c\n           %c:symbol2%c            %cmysym%c %c    %c%c^{%c%c:foo%c %c[%c%c\"afasdfasf\"%c%c\n                                                 %c%c\"afasdfasf\"%c%c\n                                                 %c%c{%c%c:a%c %c\"foo\"%c%c %c%c:b%c %c[%c%c1%c%c %c%c2%c%c %c%c[%c%c1%c%c %c%c2%c%c %c%c3%c%c %c%c4%c%c]%c%c]%c%c}%c%c\n                                                 %c%c\"afasdfasf\"%c%c\n                                                 %c%c\"afasdfasf\"%c%c]%c%c\n                                           %c%c:bar%c %c\"fooz\"%c%c}%c\n           %c:uuid%c               %c#uuid %c%c\"4fe5d828-6444-11e8-822\"%c...%c%c\n           %c:atom/number%c        %cAtom<%c%c1%c%c>%c\n           %c:atom/record%c        %cAtom<%c%cFoos%c\n                               %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c\n           %c:map/multi-line%c     %c{%c%c:abc%c%c\n                                %c%c\"bar\"%c%c\n                                \n                                %c%c\"asdfasdfa\"%c%c\n                                %c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                \n                                %c%c[%c%c:a%c %c:b%c%c]%c%c\n                                %c%c123444%c%c}%c\n           %c:map/nested-meta%c    %c{%c %c    %c%c^{%c%c:a%c %cfoo%c %c    %c%c^{%c%c:abc%c %cbar%c%c\n                                                    %c%c:xyz%c %c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c}%c%c}%c%c\n                                %c%ca%c %c    %c%c^{%c%c:abc%c %c\"bar\"%c%c %c%c:xyz%c %c\"abc\"%c%c}%c%c\n                                %c%cfoo%c %c    %c%c^{%c%c:abc%c %c\"bar\"%c%c %c%c:xyz%c %c\"abc\"%c%c}%c%c\n                                \n                                %c%c:b%c%c\n                                %c%c2%c%c}%c\n           %c:map/single-line%c    %c{%c%c:a%c %c1%c %c:b%c %c2%c %c:c%c %c\"three\"%c%c}%c\n           %c:set/multi-line%c     %c#{%c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                 %c%c3333333%c%c\n                                 %c%c:22222%c%c}%c\n           %c:set/single-line%c    %c#{%c%c1%c %c\"three\"%c %c:2%c%c}%c\n           %c:vector/multi-line%c  %c[%c%c\"abcdefghijklmnopqrstuvwxyzzz\"%c...%c%c%c\n                                %c%c:22222%c%c\n                                %c%c3333333%c%c]%c\n           %c:vector/single-line%c %c[%c%c1%c %c:2%c %c\"three\"%c%c]%c%c}%c%c}%c")))
       
       (deftest p-data-with-coll-limit
         (is (= 
              (let [ret              (? :data {:label                      "my-label"
                                              :enable-terminal-truecolor? true
                                              :enable-terminal-italics?   true
                                              :coll-limit                 5
                                              :theme                      theme}
                                             [1 2 3 4 5 6 7 8])
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
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
                #_(pp/pprint formatted-string)
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
                #_(pp/pprint formatted-string)
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


       ;; Leave this out until you support custom printers 
       #_(deftest p-data-custom-printers
         (is (= 
              (let [ret              
                    (p-data {:custom-printers {:vector {:pred        (fn [x] (= x [1 2 3 4]))
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
   
   :clj
   (do 
     (deftest  p-data-with-label-from-opts
       (is (= 
            (let [ret              (? :data {:label                      "my-label-from-opts"
                                             :enable-terminal-truecolor? true
                                             :enable-terminal-italics?   true}
                                      "foo")
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;2;68;140;39〠" "\"foo\"" "〠0〠"))))

     (deftest p-data-with-label-from-opts-primitive-terminal-emulator
       (is (= 
            (let [ret              (? :data {:label                      "my-label-from-opts"
                                            :enable-terminal-truecolor? false
                                            :enable-terminal-italics?   false
                                            :theme                      theme}
                                           "foo")
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;5;64〠" "\"foo\"" "〠0〠"))))

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
              (escape-sgr formatted-string))
            '("〠38;5;241〠"
              "["
              "〠0〠"
              "〠38;2;68;140;39〠"
              "\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44\""
              "〠3;38;2;140;140;140〠"
              "..."
              "〠0〠"
              "〠0〠"
              "〠38;5;241〠"
              "]"
              "〠0〠"))))

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
              (escape-sgr formatted-string))
            '("〠38;2;68;140;39〠"
              "\"asdfffaaaaasdfasdfasdfasdfasdfasdfasd\"..."
              "〠0〠"))))

     (deftest p-data-with-coll-limit
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                            :enable-terminal-truecolor? true
                                            :enable-terminal-italics?   true
                                            :coll-limit                 5
                                            :theme                      theme
                                            :custom-printers            {}}
                                           [1 2 3 4 5 6 7 8])
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;5;241〠"
              "["
              "〠0〠"
              "〠38;2;122;62;157〠"
              "1"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "2"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "3"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "4"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "5"
              "〠0〠"
              "〠3;38;2;140;140;140〠"
              " ...+3"
              "〠0〠"
              "〠38;5;241〠"
              "]"
              "〠0〠"))))
     
     (deftest p-data-rainbow-brackets
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                            :enable-terminal-truecolor? true
                                            :enable-terminal-italics?   true
                                            :bracket-contrast           "high"
                                            :theme                      theme}
                                           [[[[[]]]]])
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;5;241〠"
              "["
              "〠0〠"
              "〠38;5;32〠"
              "["
              "〠0〠"
              "〠38;5;208〠"
              "["
              "〠0〠"
              "〠38;5;28〠"
              "["
              "〠0〠"
              "〠38;5;128〠"
              "["
              "〠0〠"
              "〠38;5;128〠"
              "]"
              "〠0〠"
              "〠38;5;28〠"
              "]"
              "〠0〠"
              "〠38;5;208〠"
              "]"
              "〠0〠"
              "〠38;5;32〠"
              "]"
              "〠0〠"
              "〠38;5;241〠"
              "]"
              "〠0〠"))))
     
     (deftest p-data-rainbow-brackets-low-contrast
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                            :enable-terminal-truecolor? true
                                            :enable-terminal-italics?   true
                                            :bracket-contrast           "low"
                                            :theme                      theme}
                                           [[[[[]]]]])
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;5;245〠"
              "["
              "〠0〠"
              "〠38;5;74〠"
              "["
              "〠0〠"
              "〠38;5;179〠"
              "["
              "〠0〠"
              "〠38;5;106〠"
              "["
              "〠0〠"
              "〠38;5;177〠"
              "["
              "〠0〠"
              "〠38;5;177〠"
              "]"
              "〠0〠"
              "〠38;5;106〠"
              "]"
              "〠0〠"
              "〠38;5;179〠"
              "]"
              "〠0〠"
              "〠38;5;74〠"
              "]"
              "〠0〠"
              "〠38;5;245〠"
              "]"
              "〠0〠"))))


     (deftest p-data-record-sample-in-atom
       (is (= 
            (let [ret              (? :data {:label                      "my-label"
                                            :enable-terminal-truecolor? true
                                            :enable-terminal-italics?   true
                                            :bracket-contrast           "high"
                                            :theme                      theme}
                                           (atom smoke-test/record-sample))
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint p-data-record-sample-in-atom)
              ;; (pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠3;38;2;37;101;70;48;2;229;251;229〠"
              "Atom<"
              "〠0〠"
              "〠3;38;2;37;101;70;48;2;229;251;229〠"
              "Foos"
              "〠0〠"
              "\n"
              "〠38;5;241;48;2;229;251;229〠"
              "{"
              "〠0〠"
              "〠38;2;122;62;157〠"
              ":a"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "1"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              ":b"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "2"
              "〠0〠"
              "〠38;5;241;48;2;229;251;229〠"
              "}"
              "〠0〠"
              "〠3;38;2;37;101;70;48;2;229;251;229〠"
              ">"
              "〠0〠"))))

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
              (escape-sgr formatted-string))
            '("〠3;38;2;37;101;70;48;2;229;251;229〠"
              "Foos"
              "〠0〠"
              "\n"
              "〠38;5;241;48;2;229;251;229〠"
              "{"
              "〠0〠"
              "〠38;2;122;62;157〠"
              ":a"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "1"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              ":b"
              "〠0〠"
              " "
              "〠38;2;122;62;157〠"
              "2"
              "〠0〠"
              "〠38;5;241;48;2;229;251;229〠"
              "}"
              "〠0〠"))))

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
              (escape-sgr formatted-string))
            '("〠38;2;77;109;186〠"
              "mysym"
              "〠0〠"
              " "
              "〠38;2;190;85;187;48;2;250;232;253〠"
              "    "
              "〠0〠"
              "〠38;2;190;85;187;48;2;250;232;253〠"
              "^{"
              "〠0〠"
              "〠38;2;190;85;187;1;48;2;250;232;253〠"
              ":foo"
              "〠0〠"
              "〠38;2;190;85;187;48;2;250;232;253〠"
              " "
              "〠0〠"
              "〠38;2;190;85;187;48;2;250;232;253〠"
              ":bar"
              "〠0〠"
              "〠38;2;190;85;187;48;2;250;232;253〠"
              "}"
              "〠0〠"))))))

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
  (deftest p-basic
    (is (= (? {:a   "foo"
               :xyz "bar"})
           {:a   "foo"
            :xyz "bar"})))

  (deftest ?-basic
    (is (= (? {:a   "foo"
               :xyz "bar"})
           {:a   "foo"
            :xyz "bar"})))

  (deftest !?-basic
    (is (= (!? {:a   "foo"
                :xyz "bar"})
           {:a   "foo"
            :xyz "bar"})))

  (deftest ?>-basic
    (is (= (?> {:a   "foo"
                :xyz "bar"})
           {:a   "foo"
            :xyz "bar"})))

  (deftest !?>-basic
    (is (= (!?> {:a   "foo"
                 :xyz "bar"})
           {:a   "foo"
            :xyz "bar"}))))

