(ns fireworks.themes)


(def neutral-light
  {:name   "Neutral Light"
   :desc   "Neutral dark theme with no syntax coloring"
   :mood   :light
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :bracket-contrast "low"
   :tokens {:classes {:comment    {:color      "#AA3731"
                                   :font-style :italic}
                      :metadata   {:color            "#be55bb"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#fbf2fc"}
                      :metadata2  {:color            "#8f7ed3"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#f2f0ff"}
                      :label      {:color            "#4a4b5e"
                                   :background-color "#f1f5f8"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :font-style       :italic}
                      :eval-label {:color            "#3e76a8"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#f0fbfe"
                                   :font-style       :italic}}
            :printer {:file-info {:color                "#3e76a8"
                                  :font-style           :italic
                                  :padding-inline-start :0ch}}}})
                        
(def neutral-dark
  {:name   "Neutral Dark"
   :desc   "Neutral dark theme with no syntax coloring"
   :mood   :dark
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :bracket-contrast "low"
   :tokens {:classes {:comment    {:color      "#e0d557"
                                   :font-style :italic}
                      :eval-label {:color            "#85b7e5"
                                   :font-style       :italic
                                   :text-shadow      "0 0 2px #003d6b"
                                   :background-color "#00345c"}
                      :metadata   {:color            "#bb7777"
                                   :text-shadow      "0 0 2px #003538"
                                   :background-color "#2e0a0a"}
                      :label      {:color            "#a9aabc"
                                   :background-color "#212d36"
                                   :text-shadow      "0 0 2px #000000"
                                   :font-style       :italic}
                      :metadata2  {:color            "#9773b5"
                                   :text-shadow      "0 0 2px #003538"
                                   :background-color "#260a3d"}} 
            :printer {:file-info {:color                "#85b7e5"
                                  :font-style           :italic
                                  :padding-inline-start :0ch}
                      :eval-form :eval-label}}})

(def alabaster-light
  {:name   "Alabaster Light"
   :desc   "Based on @tonsky's Alabaster theme."
   :about  "This is additional documentation. Should support markdown here."
   :url    "url goes here"
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :mood   :light
   :tokens {:classes {:background    {:background-color "#f7f7f7"}
                      :string        {:color "#448C27"}
                      :constant      {:color "#7A3E9D"}
                      :definition    {:color "#4d6dba"}
                      :annotation    {:color      "#8c8c8c" 
                                      :font-style :italic}
                      :metadata      {:color            "#be55bb"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#fae8fd"}
                      :metadata2     {:color            "#be55bb"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e9e5ff"}
                      :label         {:color            "#4d6dba"
                                      :background-color "#edf2fc"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :font-style       :italic}
                      :eval-label    {:color            "#4d6dba"
                                      :background-color "#edf2fc"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :font-style       :italic}}
            :syntax  {:js-object-key {:color "#888888"}}
            :printer {:file-info     {:color                "#4d6dba"
                                      :font-style           :italic
                                      :padding-inline-start :0ch}
                      :eval-form     :eval-label
                      :comment       {:color            "#2e6666"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e5f1fa"
                                      :outline          "2px solid #e5f1fa"
                                      :font-style       :italic}
                      :function-args {:color "#999999"}
                      :atom-wrapper  :label}}})


(def alabaster-dark 
  {:name   "Alabaster Dark"
   :desc   "Based on @tonsky's Alabaster Dark theme."
   :about  "This is additional documentation. Should support markdown here."
   :url    "url goes here"
   :mood   :dark
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :tokens {:classes {:background    {:background-color "#0e1415"}
                      :string        {:color "#8cbd7a"}
                      :comment       {:color      "#DFDF8E"
                                      :font-style :italic}
                      :constant      {:color "#b696b5"}
                      :definition    {:color "#71ADE7"}
                      :annotation    {:color      "#DFDF8E"
                                      :font-style :italic}
                      :metadata      {:color            "#ae849b"
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#3a1228"}
                      :metadata2     {:color            "#a08a40"
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#351d1d"}
                      :label         {:color            "#5f9ed8"
                                      :background-color "#162f46"
                                      :text-shadow      "0 0 2px #00381d"
                                      :font-style       :italic}
                      :eval-label    {:color            "#85b7e5"
                                      :font-style       :italic
                                      :text-shadow      "0 0 2px #003d6b"
                                      :background-color "#00345c"}}
            :syntax  {:js-object-key {:color "#b2b2b2"}}
            :printer {:file-info     {:color                "#71ADE7"
                                      :font-style           :italic
                                      :padding-inline-start :0ch}
                      :eval-form     :eval-label
                      :eval-label    :eval-label
                      :comment       {:color             "#2e6666"
                                      :text-shadow       "0 0 2px #ffffff"
                                      :background-color  "#e5f1fa"
                                      :outline           "2px solid #e5f1fa"
                                      :font-style        :italic}
                      :function-args {:color "#999999"}
                      :atom-wrapper  :label}}})


(def degas-light
  {:name  "Degas Light"
   :desc  "A light, low-contrast pastel theme for Clojure"
   :mood  "light"
   :tokens {:classes {:background    {:background-color "#f5f9f9"}
                      :string        {:color "#5c9999"}
                      :constant      {:color "#bf6faf"}
                      :definition    {:color "#5f87d7"}
                      :comment       {:color      "#5728f0"
                                      :font-style :italic}
                      :annotation    {:color      "#9e9e9e"
                                      :font-style :italic}
                      :metadata      {:color            "#618d98"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e8f3fd"}
                      :metadata2     {:color            "#7065c3"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e4e0ff"}
                      :label         {:color            "#c4793b"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#f7ece3"
                                      :font-style       :italic}
                     :eval-label     {:color             "#4f7878"
                                     :text-shadow       "0 0 2px #ffffff"
                                     :background-color  "#e5f1fa"
                                     :font-style        :italic}}
           :syntax  {:number {:color "#737373"}
                     :js-object-key {:color "#888888"}}
           :printer {:file-info     {:color                "#4f7878" 
                                     :font-style           :italic
                                     :padding-inline-start :0ch}
                     :eval-form     :eval-label
                     :eval-label    :eval-label
                     :comment       {:color             "#4d6f6f"
                                     :text-shadow       "0 0 2px #ffffff"
                                     :background-color  "#e5f1fa"
                                     :outline           "2px solid #e5f1fa"
                                     :font-style        :italic}
                     :function-args {:color "#999999"}
                     :atom-wrapper  :label }}})


(def degas-dark
  {:name   "Degas Dark"
   :desc   "A dark pastel theme for Clojure"
   :mood   "dark"
   :bracket-contrast "low"
   :tokens {:classes {:background {:background-color "#363f4e"}
                      :foreground {:color "#bfbfbf"}
                      :string     {:color "#78ba78"}
                      :constant   {:color "#c0a1bf"}
                      :definition {:color "#80a3ea"}
                      :comment    {:color      "#e1d084"
                                   :font-style :italic}
                      :annotation {:color      "#999999"
                                   :font-style :italic}
                      :metadata   {:color            "#cd98cc"
                                   :text-shadow      "0 0 2px #000738"
                                   :background-color "#544054"}
                      :metadata2  {:color            "#9697d9"
                                   :text-shadow      "0 0 2px #000738"
                                   :background-color "#42477b"}
                      :label      {:color            "#bc8b71"
                                   :text-shadow      "0 0 2px #202288"
                                   :background-color "#4d3f38"
                                   :font-style       :italic}
                      :eval-label {:color            "#9ac2d6"
                                   :background-color "#2b4c69"
                                   :text-shadow      "0 0 2px #002916"
                                   :font-style       :italic}}
            :syntax  {:number        {:color "#afaf87"}
                      :js-object-key {:color "#888888"}}
            :printer {:file-info     {:color                "#9ac2d6"
                                      :font-style           :italic
                                      :padding-inline-start :0ch}
                      :eval-form     :eval-label
                      :eval-label    :eval-label
                      :comment       {:color             "#2e6666"
                                      :text-shadow       "0 0 2px #ffffff"
                                      :background-color  "#e5f1fa"
                                      :outline           "2px solid #e5f1fa"
                                      :font-style        :italic}
                      :function-args {:color "#b3b3b3"}}}})


(def zenburn-light
  {:name             "Zenburn Light"
   :desc             "A light low-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "low"
   :tokens           {:classes {:background    {:background-color "#f9f8f5"}
                                :foreground    {:color "#666666"}
                                :string        {:color "#a64b64"}
                                :constant      {:color "#548354"}
                                :definition    {:color "#99770f"}
                                :comment       {:color      "#0060e6"
                                                :font-style :italic}
                                :annotation    {:color      "#999999"
                                                :font-style :italic}
                                :metadata      {:color            "#618d98"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#e8f3fd"}
                                :metadata2     {:color            "#7065c3"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#e4e0ff"}
                                :label         {:color             "#99770f"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#f5eccc"
                                                :font-style        :italic}
                                :eval-label    {:color            "#618d98"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :font-style       :italic
                                                :background-color "#e8f3fd"}}
                      :syntax  {:number        {:color "#3d7a99"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#618d98"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-label    :eval-label
                                :eval-form     :eval-label
                                :comment       {:color             "#00ffff"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :font-style        :italic}
                                :function-args {:color "#9e9e9e"}
                                :atom-wrapper  :label}}})


(def zenburn-dark 
  {:name             "Zenburn Dark"
   :desc             "A dark low-contrast theme for Clojure"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background    {:background-color "#3f3f3f"}
                                :string        {:color "#dc8f8f"}
                                :constant      {:color "#8cc08c"}
                                :definition    {:color "#bfbf69"}
                                :comment       {:color      "#76d5fe"
                                                :font-style :italic}
                                :annotation    {:color      "#999999"
                                                :font-style :italic}
                                :metadata      {:color            "#96a4e8"
                                                :background-color "#353e69"
                                                :text-shadow      "0 0 2px #002916"}
                                :metadata2     {:color            "#b596c0"
                                                :background-color "#4e3257"
                                                :text-shadow      "0 0 2px #002916"}
                                :label         {:color            "#b49d5f"
                                                :background-color "#524019"
                                                :text-shadow      "0 0 2px #000000"
                                                :font-style       :italic}
                                :eval-label    {:color            "#9ac2d6"
                                                :background-color "#2b4c69"
                                                :text-shadow      "0 0 2px #002916"
                                                :font-style       :italic}}
                      :syntax  {:number        {:color "#8fb8cc"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#9ac2d6"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-form     :eval-label
                                :eval-label    :eval-label
                                :atom-wrapper  :label}}})


(def solarized-light 
  {:name             "Solarized Light"
   :desc             "A light low-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#fdf6e3"}
                                ;; :foreground {:color "#666666"}
                                :foreground {:color "#657b83"}
                                :constant   {:color "#657b83"}
                                :string     {:color "#2aa198"}
                                ;; :string     {:color "#859900"}
                                :definition {:color "#268bd2"}
                                ;; :definition {:color "#268bd2"}

                                :comment    {:color      "#AA3731"
                                             :font-style :italic}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :metadata   {:color            "#bc775c"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#ffe9e0"}
                                :metadata2  {:color            "#93842f"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#faefbc"}
                                :label      {:color            "#6c71c4"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#eef3ec"
                                             :font-style       :italic}
                                :eval-label {:color            "#6c71c4"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#eef3ec"
                                             :font-style       :italic}
                                }
                      :syntax  {:number        {:color "#af5f5f"}
                                :boolean       {:color "#b58900"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#6c71c4"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-form     :eval-label
                                :eval-label    :eval-label
                                :comment       {:color             "#00ffff"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :font-style        :italic}
                                :function-args {:color "#9e9e9e"}
                                :atom-wrapper  :label}}})


(def solarized-dark 
  {:name             "Solarized Dark"
   :desc             "A dark low-contrast theme for Clojure, based on"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#002b36"}
                                :foreground {:color "#999999"}
                                :bracket    {:color "#808080"}
                                :string     {:color "#33a3a3"}
                                :constant   {:color "#8f8f8f"}
                                :definition {:color "#5289cc"}
                                :comment    {:color      "#ee63b4"
                                             :font-style :italic}
                                :label      {:color            "#a4862d"
                                             :background-color "#293928"
                                             :text-shadow      "0 0 2px #000000"
                                             :font-style       :italic}
                                :metadata   {:color            "#bf6986"
                                             :background-color "#34273a"
                                             :text-shadow      "0 0 2px #000000"}
                                :metadata2  {:color            "#ad5952"
                                             :text-shadow      "0 0 2px #321d06"
                                             :background-color "#2c2f04"}
                                :eval-label {:color            "#659bdc"
                                             :background-color "#263d5a"
                                             :text-shadow      "0 0 2px #002916"
                                             :font-style       :italic}}
                      :syntax  {:number        {:color "#bf6986"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#659bdc"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-form     :eval-label
                                :eval-label    :eval-label
                                :comment       {:color             "#2e6666"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :font-style        :italic}
                                :atom-wrapper  :label}}})

(def monokai-light 
  {:name             "Monokai Light"
   :desc             "A light high-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "high"
   :tokens            {:classes {:background {:background-color "#fff"}
                                 :string     {:color "#1386bf"}
                                 :constant   {:color "#8545e6"}
                                 :definition {:color "#178c54"}
                                 :annotation {:color      "#a6a6a6"
                                              :font-style :italic}
                                 :comment    {:color      "#e000ca"
                                              :font-style :italic}
                                 :metadata   {:color            "#c256a9"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :background-color "#fcf3fa"}
                                 :metadata2  {:color            "#c57634"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :background-color "#fdf6ed"}
                                 :label      {:color            "#398962"
                                              :background-color "#eefbee"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :font-style       :italic}
                                 :eval-label {:color            "#316363"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :font-style       :italic
                                              :background-color "#defcfc"}}
                       :syntax  {:number        {:color "#cc3d9c"}
                                 :js-object-key {:color "#888888"}}
                       :printer {
                                 :file-info     {:color                "#316363"
                                                 :font-style           :italic
                                                 :padding-inline-start :0ch}
                                 :eval-form     :eval-label
                                 :eval-label    :eval-label
                                 :comment       {:color             "#2e6666"
                                                 :text-shadow       "0 0 2px #ffffff"
                                                 :background-color  "#e5f1fa"
                                                 :outline           "2px solid #e5f1fa"
                                                 :font-style        :italic}
                                 :function-args {:color "#999999"}
                                 :atom-wrapper  :label} }})


(def monokai-dark 
  {:name             "Monokai Dark"
   :desc             "A dark high-contrast theme for Clojure, based on Monokai"
   :mood             "dark"
   :bracket-contrast "high"
   :tokens           {:classes {:background {:background-color "#2d2a2e"}
                                :string     {:color "#ccb43e"}
                                :constant   {:color "#cc99ff"}
                                :definition {:color "#4fc94f"}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :comment    {:color      "#2ef1ff"
                                             :font-style :italic}
                                :metadata   {:color            "#c47878"
                                             :text-shadow      "0 0 2px #003538"
                                             :background-color "#3f2222"}
                                :metadata2  {:color            "#a77ccb"
                                             :text-shadow      "0 0 2px #003538"
                                             :background-color "#3d274f"}
                                :label      {:color            "#cd8923"
                                             :background-color "#3c2a06"
                                             :text-shadow      "0 0 2px #000000"
                                             :font-style       :italic}
                                :eval-label {:color            "#85b7e5"
                                             :font-style       :italic
                                             :text-shadow      "0 0 2px #003d6b"
                                             :background-color "#00345c"}}
                      :syntax  {:number        {:color "#14bcd2"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info    {:color                "#71ADE7"
                                               :font-style           :italic
                                               :padding-inline-start :0ch}
                                :eval-form    :eval-label
                                :eval-label   :eval-label
                                :atom-wrapper :label}}})

(def universal-neutral
  {:name   "Universal Neutral"
   :desc   (str "A neutral syntax theme for Clojure data that works on both light and dark backgrounds."
                "The eval label is printed in blue, metadata is printed in purple, and object labels are printed in green.")
   :mood   "dark"
   :rainbow-brackets {:browser [:neutral "#9e9e9e"
                                :blue    "#00afff"
                                :orange  "#ff8700"
                                :green   "#00d700"
                                :purple  "#ff00ff"]
                      :x-term  [:neutral 247
                                :blue    39
                                :orange  208 
                                :green   40
                                :purple  201]}
   :tokens {:classes {:foreground {:color "#9e9e9e"}
                      :metadata   {:color "#af87ff"}      ;; purple
                      :metadata2  {:color "#87af00"}      ;; yellow
                      :label      {:color       "#ff8700" ;; yellow
                                   :font-style  :italic}
                      :eval-label {:color      "#00afff"
                                   :font-style :italic}}
            :syntax  {:js-object-key {:color "#ff00ff"}}

            :printer {:file-info     {:color      "#9e9e9e" ;; gray
                                      :font-style :italic }
                      :eval-form     :eval-label
                      :eval-label    :eval-label
                      :comment       {:font-style :italic}
                      :function-args {:color "#9e9e9e"}}}})

(def universal
  {:name   "Universal"
   :desc   "A colorized syntax theme for Clojure data that works on both light and dark backgrounds."
   :mood   "dark"
   :rainbow-brackets {:browser [:neutral "#9e9e9e"
                                :blue    "#00afff"
                                :orange  "#ff8700"
                                :green   "#00d700"
                                :purple  "#ff00ff"]
                      :x-term  [:neutral 247
                                :blue    39
                                :orange  208 
                                :green   40
                                :purple  201]}
   :tokens {:classes {:foreground {:color "#9e9e9e"}
                      :string     {:color "#00d700"}
                      :definition {:color "#00afff"}
                      :metadata   {:color "#af87ff"}     ;; purple 
                      :metadata2  {:color "#87af00"}     ;; yellow
                      :label      {:color      "#ff00ff" ;; magenta
                                   :font-style :italic}
                      :eval-label {:color      "#af87ff"
                                   :font-style :italic}}

            :syntax  {:number        {:color "#ff8700"} ;; orange 
                      :js-object-key {:color "#ff00ff"}}

            :printer {:file-info     {:color      "#9e9e9e"
                                      :font-style :italic}
                      :eval-form     :eval-label
                      :eval-label    :eval-label
                      :comment       {:font-style :italic}
                      :function-args {:color "#9e9e9e"}}}})
