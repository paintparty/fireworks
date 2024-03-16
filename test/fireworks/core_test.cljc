(ns fireworks.core-test
  (:require [clojure.string :as string]
            [fireworks.core :refer [? !? ?> !?> p p*]]
            [fireworks.config]
            [fireworks.pp :as pp :refer [?pp]]
            [fireworks.smoke-test :as smoke-test]
            #?(:cljs [cljs.test :refer [deftest is]])
            #?(:clj [clojure.test :refer :all])))
;; These tests will break if your local ~/.fireworks/config.edn is different from fireworks.smoke-test/example-config. Change it to that temporarily if you want to run these tests locally. This will be fixed in the near future.
;; By design, all cljs tests that test fireworks.core/p* in this namespace will break if the line number that they are on changes!
(def theme smoke-test/alabaster-light-legacy)
(declare escape-sgr)

#?(:cljs
   (deftest p*-basic 
     (is (=
          (let [ret (p* {:theme theme} "foo")] #_(pp/pprint ret) ret)
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c",
                           :css-styles ["color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    46,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:17:21",
           :column        21,
           :line          17,
           :end-line      17,
           :formatted+    {:string     "%cfireworks.core-test:17:21%c\n%c\"foo\"%c %c=>%c %c\"foo\"%c",
                           :css-styles ["color:#8c8c8c;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#28cc7d;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]}}))))
#?(:cljs
   (deftest p*-with-label
     (is (= 
          (let [ret (p* "my-label" "foo")] (pp/pprint ret) ret)
          {:quoted-form   "foo",
           :formatted     {:string     "%c\"foo\"%c",
                           :css-styles ["color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]},
           :file          "fireworks/core_test.cljc",
           :end-column    42,
           :ns-str        "fireworks.core-test",
           :file-info-str "fireworks.core-test:41:21",
           :column        21,
           :line          41,
           :end-line      41,
           :formatted+    {:string     "%cfireworks.core-test:41:21%c\n%cmy-label%c %c\"foo\"%c",
                           :css-styles ["color:#8c8c8c;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#AA3731;font-style:italic;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"
                                        "color:#448C27;line-height:1.45;"
                                        "color:#585858;line-height:1.45;"]}}))))



#?(:cljs
   (do (deftest p*-with-label-from-opts
         (is (= 
              (let [ret (p* {:label             "my-label-from-opts"
                             :theme             theme
                             :non-coll-length-limit (-> fireworks.config/options
                                                    :non-coll-length-limit
                                                    :default)}
                            "foo")]
                #_(pp/pprint ret) ret)
              {:quoted-form   "foo",
               :formatted     {:string     "%c\"foo\"%c",
                               :css-styles ["color:#448C27;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"]},
               :file          "fireworks/core_test.cljc",
               :end-column    35,
               :ns-str        "fireworks.core-test",
               :file-info-str "fireworks.core-test:66:25",
               :column        25,
               :line          66,
               :end-line      71,
               :formatted+    {:string     "%cfireworks.core-test:66:25%c\n%cmy-label-from-opts%c %c\"foo\"%c",
                               :css-styles ["color:#8c8c8c;font-style:italic;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#AA3731;font-style:italic;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"
                                            "color:#448C27;line-height:1.45;"
                                            "color:#585858;line-height:1.45;"]}})))
       (deftest p*-basic-samples
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :bracket-contrast           "high"
                                          :theme                      theme
                                          :custom-printers            {}
                                          :non-coll-length-limit          (-> fireworks.config/options
                                                                          :non-coll-length-limit
                                                                          :default)
                                          :display-namespaces?        (-> fireworks.config/options
                                                                          :display-namespaces?
                                                                          :default)
                                          :metadata-position          (-> fireworks.config/options
                                                                          :metadata-position
                                                                          :default)
                                          :metadata-print-level       (-> fireworks.config/options
                                                                          :metadata-print-level
                                                                          :default)
                                          :non-coll-mapkey-length-limit         (-> fireworks.config/options
                                                                          :non-coll-mapkey-length-limit
                                                                          :default)}
                                         smoke-test/basic-samples)
                    formatted-string (-> ret :formatted :string)]

                ;; (pp/pprint "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                ;; (pp/pprint formatted-string)
                formatted-string)
              "%c{%c%c:uuid%c     %c#uuid%c%c\"4fe5d828-6444-11e8-8222-720007e40350\"%c\n %c:fn%c       %ccljs.core/juxt%c%c[var_args]%c\n %c:brackets%c %c[%c%c[%c%c[%c%c[%c%c[%c%c[%c%c]%c%c]%c%c]%c%c]%c%c]%c%c]%c\n %c:symbol2%c  %cmysym%c%c %c%c    %c%c{:foo%c%c\n                     %c%c [\"afasdfasf\"%c%c\n                     %c%c  \"afasdfasf\"%c%c\n                     %c%c  {:a \"foo\", :b [1 2 [1 2 3 4]]}%c%c\n                     %c%c  \"afasdfasf\"%c%c\n                     %c%c  \"afasdfasf\"],%c%c\n                     %c%c :bar \"fooz\"}%c\n %c:atom1%c    %cAtom<%c%c1%c%c>%c\n %c:regex%c    %c#\"^hi$\"%c\n %c:number%c   %c1234%c\n %c:string%c   %c\"string\"%c\n %c:lamda%c    %cλ%c%c%c%c[%1]%c\n %c:atom2%c    %cAtom<%c%cFoos%c\n           %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c\n %c:symbol%c   %cmysym%c%c %c%c    %c%c{:foo :bar}%c\n %c:boolean%c  %ctrue%c\n %c:record%c   %cFoos%c\n           %c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c}%c")))
       
       (deftest p*-with-coll-limit
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :coll-limit                 5
                                          :theme                      theme}
                                         [1 2 3 4 5 6 7 8])
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
                formatted-string)
              "%c[%c%c1%c %c2%c %c3%c %c4%c %c5%c%c ...+3%c%c]%c")))

       (deftest p*-with-non-coll-level-1-depth-length-limit
         (is (= 
              (let [ret              (p* {:label                         "my-label"
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

       (deftest p*-with-non-coll-length-limit
       (is (= 
            (let [ret              (p* {:label                        "my-label"
                                        :enable-terminal-truecolor?   true
                                        :enable-terminal-italics?     true
                                        :bracket-contrast             "high"
                                        :theme                        theme
                                        :non-coll-result-length-limit 42}
                                       "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
                  formatted-string (-> ret :formatted :string)]
              (pp/pprint formatted-string)
              formatted-string)
            "%c\"asdfffaaaaasdfasdfasdfasdfasdfasdfasd\"%c...%c%c")))

       (deftest p*-rainbow-brackets
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :bracket-contrast           "high"
                                          :theme                      theme}
                                         [[[[[]]]]])
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
                formatted-string)
              "%c[%c%c[%c%c[%c%c[%c%c[%c%c]%c%c]%c%c]%c%c]%c%c]%c")))
       
       (deftest p*-record-sample-in-atom
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :bracket-contrast           "high"
                                          :theme                      theme}
                                         (atom smoke-test/record-sample))
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
                formatted-string)
              "%cAtom<%c%cFoos%c\n%c{%c%c:a%c %c1%c %c:b%c %c2%c%c}%c%c>%c")))
       
       (deftest p*-js-array
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :bracket-contrast           "high"
                                          :theme                      theme}
                                         #js [1 2 3])
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
                formatted-string)
              "%c#js%c%c[%c%c1%c, %c2%c, %c3%c%c]%c")))

       (deftest p*-js-set
         (is (= 
              (let [ret              (p* {:label                      "my-label"
                                          :enable-terminal-truecolor? true
                                          :enable-terminal-italics?   true
                                          :bracket-contrast           "high"
                                          :theme                      theme}
                                         (new js/Set #js[1 2]))
                    formatted-string (-> ret :formatted :string)]
                #_(pp/pprint formatted-string)
                formatted-string)
              "%cjs/Set%c\n%c#{%c%c1%c, %c2%c%c}%c")))
       
       (deftest p*-custom-printers
         (is (= 
              (let [ret              
                    (p* {:custom-printers {:vector {:pred        (fn [x] (= x [1 2 3 4]))
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
     (deftest p*-with-label-from-opts
       (is (= 
            (let [ret              (p* {:label                      "my-label-from-opts"
                                        :enable-terminal-truecolor? true
                                        :enable-terminal-italics?   true}
                                       "foo")
                  formatted-string (-> ret :formatted :string)]
              ;; (pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;2;68;140;39〠" "\"foo\"" "〠0〠"))))

     (deftest p*-with-label-from-opts-primitive-terminal-emulator
       (is (= 
            (let [ret              (p* {:label                      "my-label-from-opts"
                                        :enable-terminal-truecolor? false
                                        :enable-terminal-italics?   false
                                        :theme                      theme}
                                       "foo")
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;5;64〠" "\"foo\"" "〠0〠"))))

     (deftest p*-with-non-coll-level-1-depth-length-limit
       (is (= 
            (let [ret              (p* {:label                         "my-label"
                                        :enable-terminal-truecolor?    true
                                        :enable-terminal-italics?      true
                                        :bracket-contrast              "high"
                                        :theme                         theme
                                        :non-coll-depth-1-length-limit 60}
                                       ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
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

     (deftest p*-with-non-coll-result-length-limit
       (is (= 
            (let [ret              (p* {:label                        "my-label"
                                        :enable-terminal-truecolor?   true
                                        :enable-terminal-italics?     true
                                        :bracket-contrast             "high"
                                        :theme                        theme
                                        :non-coll-result-length-limit 42}
                                       "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;2;68;140;39〠"
              "\"asdfffaaaaasdfasdfasdfasdfasdfasdfasd\""
              "〠3;38;2;140;140;140〠"
              "..."
              "〠0〠"
              "〠0〠"))))

     (deftest p*-with-coll-limit
       (is (= 
            (let [ret              (p* {:label                      "my-label"
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
     
     (deftest p*-rainbow-brackets
       (is (= 
            (let [ret              (p* {:label                      "my-label"
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
     
     (deftest p*-rainbow-brackets-low-contrast
       (is (= 
            (let [ret              (p* {:label                      "my-label"
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


     (deftest p*-record-sample-in-atom
       (is (= 
            (let [ret              (p* {:label                      "my-label"
                                        :enable-terminal-truecolor? true
                                        :enable-terminal-italics?   true
                                        :bracket-contrast           "high"
                                        :theme                      theme}
                                       (atom smoke-test/record-sample))
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠3;38;2;37;101;70;48;2;214;245;214〠"
              "Atom<"
              "〠0〠"
              "〠3;38;2;128;128;128;48;2;237;237;237〠"
              "Foos"
              "〠0〠"
              "\n"
              "〠38;5;241;48;2;237;237;237〠"
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
              "〠38;5;241;48;2;237;237;237〠"
              "}"
              "〠0〠"
              "〠3;38;2;37;101;70;48;2;214;245;214〠"
              ">"
              "〠0〠"))))
     (deftest p*-record-sample
       (is (= 
            (let [ret              (p* {:label                      "my-label"
                                        :enable-terminal-truecolor? true
                                        :enable-terminal-italics?   true
                                        :bracket-contrast           "high"
                                        :theme                      theme}
                                       smoke-test/record-sample)
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠3;38;2;128;128;128;48;2;237;237;237〠"
              "Foos"
              "〠0〠"
              "\n"
              "〠38;5;241;48;2;237;237;237〠"
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
              "〠38;5;241;48;2;237;237;237〠"
              "}"
              "〠0〠"))))
     (deftest p*-symbol-with-meta
       (is (= 
            (let [ret              (p* {:label                      "my-label"
                                        :enable-terminal-truecolor? true
                                        :enable-terminal-italics?   true
                                        :bracket-contrast           "high"
                                        :theme                      theme}
                                       (with-meta 'mysym {:foo :bar}))
                  formatted-string (-> ret :formatted :string)]
              #_(pp/pprint (escape-sgr formatted-string))
              (escape-sgr formatted-string))
            '("〠38;2;77;109;186〠"
              "mysym"
              "〠0〠"
              "〠38;2;88;88;88〠"
              " "
              "〠0〠"
              "〠38;2;46;102;102;48;2;230;250;250〠"
              "    "
              "〠0〠"
              "〠38;2;46;102;102;48;2;230;250;250〠"
              "{:foo :bar}"
              "〠0〠")))) ))

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
(do (deftest p-basic
      (is (= (p {:a   "foo"
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

