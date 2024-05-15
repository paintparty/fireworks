(ns fireworks.themes)


(def neutral-light
  {:name   "Neutral Light"
   :desc   "Neutral dark theme with no syntax coloring"
   :mood   :light
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :tokens {:classes {:comment {:color      "#AA3731"
                                :font-style :italic}}}})
                        
(def neutral-dark
  {:name   "Neutral Dark"
   :desc   "Neutral dark theme with no syntax coloring"
   :mood   :dark
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :tokens {:classes {:comment {:color      "#e0d557"
                                :font-style :italic}}}})

(def alabaster-light
  {:name    "Alabaster Light"
   :desc    "Based on @tonsky's Alabaster theme."
   :about   "This is additional documentation. Should support markdown here."
   :url     "url goes here"
   :author  "Author Name"
   :langs   ["Clojure" "ClojureScript" "Babashka"]
   :mood    :light
   ;; :bracket-contrast "high"
   :tokens   {:classes {:background   {:background-color "#f7f7f7"}
                        :string       {:color "#448C27"}
                        :constant     {:color "#7A3E9D"}
                        :definition   {:color "#4d6dba"}
                        :annotation   {:color      "#8c8c8c" 
                                       :font-style :italic}
                        :metadata     {:color            "#be55bb"
                                       :text-shadow      "0 0 2px #ffffff"
                                       :background-color "#fae8fd"}
                        :metadata-key {:color            "#6349d4"
                                       :text-shadow      "0 0 2px #ffffff"
                                       :background-color "#fae8fd"}
                        :metadata2    {:color            "#be55bb"
                                       :text-shadow      "0 0 2px #ffffff"
                                       :background-color "#e9e5ff"}
                        :metadata-key2 {:color            "#6349d4"
                                       :text-shadow      "0 0 2px #ffffff"
                                       :background-color "#e9e5ff"}
                        :label        {:color            "#256546"
                                       :background-color "#e5fbe5"
                                       :text-shadow      "0 0 2px #ffffff"
                                       :font-style       :italic}}
             :syntax  {:js-object-key      {:color "#888888"}}
             :printer {:file-info          {:color                "#737373" 
                                            :font-style           :italic
                                            :padding-inline-start :0ch}
                       :eval-form          {:color             "#2e6666"
                                            :text-shadow       "0 0 2px #ffffff"
                                            :background-color  "#e5f1fa"
                                            :margin-inline-end :2ch}
                       :comment            {:color             "#2e6666"
                                            :text-shadow       "0 0 2px #ffffff"
                                            :background-color  "#e5f1fa"
                                            :outline           "2px solid #e5f1fa"
                                            :margin-inline-end :2ch
                                            :font-style        :italic}
                       :function-args      {:color "#999999"}
                       :atom-wrapper       {:color            "#256546"
                                            :background-color "#e5fbe5"
                                            :text-shadow      "0 0 2px #ffffff"
                                            :font-style       :italic}}}})


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
                      :metadata      {:color            "#bb7777"
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#351d1d"}
                      :metadata-key  {:color            "#bb7777"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#351d1d"}
                      :metadata2     {:color            "#9773b5"
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#321f42"}
                      :metadata-key2 {:color            "#9773b5"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #003538"
                                      :background-color "#321f42"}
                      :label         {:color            "#5abf8e"
                                      :background-color "#004222"
                                      :text-shadow      "0 0 2px #00381d"
                                      :font-style       :italic}}
            :syntax  {:js-object-key {:color "#b2b2b2"}}
            :printer {:file-info     {:color                "#71ADE7"
                                      :font-style           :italic
                                      :padding-inline-start :0ch}
                      :eval-form     {:color             "#85b7e5"
                                      :font-style        :italic
                                      :text-shadow       "0 0 2px #003d6b"
                                      :background-color  "#00345c"
                                      :margin-inline-end :2ch}
                      :comment       {:color             "#2e6666"
                                      :text-shadow       "0 0 2px #ffffff"
                                      :background-color  "#e5f1fa"
                                      :outline           "2px solid #e5f1fa"
                                      :margin-inline-end :2ch
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
                      :metadata-key  {:color            "#618d98"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e8f3fd"}
                      :metadata2     {:color            "#7065c3"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e4e0ff"}
                      :metadata-key2 {:color            "#7065c3"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#e4e0ff"}
                      :label         {:color            "#c4793b"
                                      :text-shadow      "0 0 2px #ffffff"
                                      :background-color "#f7ece3"
                                      :font-style       :italic}
                      }
           :syntax  {:number {:color "#737373"}
                     :js-object-key {:color "#888888"}}
           :printer {:file-info     {:color                "#4f7878" 
                                     :font-style           :italic
                                     :padding-inline-start :0ch
                                     }
                     :eval-form     {:color             "#4f7878"
                                     :text-shadow       "0 0 2px #ffffff"
                                     :background-color  "#e5f1fa"
                                     :font-style        :italic
                                     :margin-inline-end :2ch
                                     }
                     :comment       {:color             "#4d6f6f"
                                     :text-shadow       "0 0 2px #ffffff"
                                     :background-color  "#e5f1fa"
                                     :outline           "2px solid #e5f1fa"
                                     :margin-inline-end :2ch
                                     :font-style        :italic}
                     :function-args {:color "#999999"}
                     :atom-wrapper  :label }}})


(def degas-dark
  {:name   "Degas Dark"
   :desc   "A dark pastel theme for Clojure"
   :mood   "dark"
   :tokens {:classes {:background    {:background-color "#363f4e"}
                      :foreground    {:color "#bfbfbf"}
                      :string        {:color "#8cc08c"}
                      :constant      {:color "#c0a1bf"}
                      :definition    {:color "#80a3ea"}
                      :comment       {:color      "#e1d084"
                                      :font-style :italic}
                      :annotation    {:color      "#999999"
                                      :font-style :italic}
                      :metadata      {:color            "#b191b0"
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#553f55"}
                      :metadata-key  {:color            "#b191b0"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#553f55"}
                      :metadata2     {:color            "#999ad1"
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#3e4379"}
                      :metadata-key2 {:color            "#999ad1"
                                      :font-weight      :bold
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#3e4379"}
                      :label         {:color            "#8ba7d5"
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#3d4b61"}}
            :syntax  {:number        {:color "#afaf87"}
                      :js-object-key {:color "#888888"}}
            :printer {:file-info     {:color                "#eaa580"
                                      :font-style           :italic
                                      :padding-inline-start :0ch}
                      :eval-form     {:color             "#eaa580"
                                      :text-shadow       "0 0 2px #202288"
                                      :background-color  "#5f331b"
                                      :font-style        :italic
                                      :margin-inline-end :2ch}
                      :comment       {:color             "#2e6666"
                                      :text-shadow       "0 0 2px #ffffff"
                                      :background-color  "#e5f1fa"
                                      :outline           "2px solid #e5f1fa"
                                      :margin-inline-end :2ch
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
                                :metadata-key  {:color            "#618d98"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#e8f3fd"}
                                :metadata2     {:color            "#7065c3"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#e4e0ff"}
                                :metadata-key2 {:color            "#7065c3"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#e4e0ff"}
                                :label         {:color            "#c4793b"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#f7ece3"
                                                :font-style       :italic}
                                }
                      :syntax  {:number        {:color "#3d7a99"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#7b6c3d" 
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                ;; Maybe think of new name for this
                                ;; (covers label) and promote to classes?
                                :eval-form     {:color             "#7b6c3d"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#f5eccc"
                                                :margin-inline-end :2ch
                                                :font-style        :italic}
                                :comment       {:color             "#00ffff"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :margin-inline-end :2ch
                                                :font-style        :italic}
                                :function-args {:color "#9e9e9e"}
                                :atom-wrapper  :label}}})

(def zenburn-dark 
  {:name             "Zenburn Dark"
   :desc             "A dark low-contrast theme for Clojure"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background    {:background-color "#3f3f3f"}
                                :string        {:color "#cc8585"}
                                :constant      {:color "#8cc08c"}
                                :definition    {:color "#cccc7a"}
                                :comment       {:color      "#76d5fe"
                                                :font-style :italic}
                                :annotation    {:color      "#999999"
                                                :font-style :italic}
                                :metadata      {:color            "#b19d8c"
                                                :text-shadow      "0 0 2px #321d06"
                                                :background-color "#524f4c"}
                                :metadata-key  {:color            "#b19d8c"
                                                :text-shadow      "0 0 2px #321d06"
                                                :font-weight      :bold
                                                :background-color "#524f4c"}
                                :metadata2     {:color            "#afb18c"
                                                :text-shadow      "0 0 2px #321d06"
                                                :background-color "#585c2d"}
                                :metadata-key2 {:color            "#afb18c"
                                                :text-shadow      "0 0 2px #321d06"
                                                :font-weight      :bold
                                                :background-color "#585c2d"}
                                :label         {:color            "#b49d5f"
                                                :background-color "#584928"
                                                :text-shadow      "0 0 2px #000000"
                                                :font-style       :italic}}
                      :syntax  {:number        {:color "#8fb8cc"}
                                :js-object-key {:color "#888888"}}
                      :printer {
                                :file-info     {:color                "#bfbf97"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-form     {:color             "#cccc7a"
                                                :text-shadow       "0 0 2px #000000"
                                                :background-color  "#525200"
                                                :font-style        :italic
                                                :margin-inline-end :2ch}
                                :atom-wrapper  {:color            "#83a59c"
                                                :background-color "#475c54"
                                                :text-shadow      "0 0 2px #002916"
                                                :font-style       :italic}}}})


(def solarized-light 
  {:name             "Solarized Light"
   :desc             "A light low-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "low"
   :tokens           {:classes {:background    {:background-color "#fdf6e3"}
                                :foreground    {:color "#666666"}
                                :string        {:color "#4ba6a6"}
                                :definition    {:color "#5270cc"}
                                :comment       {:color      "#AA3731"
                                                :font-style :italic}
                                :annotation    {:color      "#999999"
                                                :font-style :italic}
                                :metadata      {:color            "#bc775c"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#ffe9e0"}
                                :metadata-key  {:color            "#bc775c"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#ffe9e0"}
                                :metadata2     {:color            "#93842f"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#faefbc"}
                                :metadata-key2 {:color            "#93842f"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#faefbc"}
                                :label         {:color            "#7e70a1"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#f0f0fc"
                                                :font-style       :italic}
                                }
                      :syntax  {:number        {:color "#af5f5f"}
                                :js-object-key {:color "#888888"}}
                      :printer {:file-info     {:color                "#497b3d"
                                                :font-style           :italic
                                                :padding-inline-start :0ch}
                                :eval-form     {:color             "#497b3d"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e3fce3"
                                                :margin-inline-end :2ch
                                                :font-style        :italic}
                                :comment       {:color             "#00ffff"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :margin-inline-end :2ch
                                                :font-style        :italic}
                                :function-args {:color "#9e9e9e"}
                                :atom-wrapper  :label}}})


(def solarized-dark 
  {:name             "Solarized Dark"
   :desc             "A dark low-contrast theme for Clojure, based on"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background    {:background-color "#002b36"}
                                :foreground    {:color "#999999"}
                                :bracket       {:color "#808080"}
                                :string        {:color "#33a3a3"}
                                :constant      {:color "#8f8f8f"}
                                :definition    {:color "#5289cc"}
                                :comment       {:color      "#ee63b4"
                                                :font-style :italic}
                                :label         {:color            "#5289cc"
                                                :background-color "#143966"
                                                :text-shadow      "0 0 2px #000000"
                                                :font-style       :italic}
                                :metadata      {:color            "#8a8a8a"
                                                :background-color "#333333"
                                                :text-shadow      "0 0 2px #000000"}
                                :metadata-key  {:color            "#8a8a8a"
                                                :font-weight      :bold
                                                :background-color "#333333"
                                                :text-shadow      "0 0 2px #000000"}
                                :metadata2     {:color            "#ad7b58"
                                                :background-color "#4e321d"
                                                :text-shadow      "0 0 2px #000000"}
                                :metadata-key2 {:color            "#ad7b58"
                                                :font-weight      :bold
                                                :background-color "#4e321d"
                                                :text-shadow      "0 0 2px #000000"}
                                }
                      :syntax  {:number        {:color "#bf6986"}
                                :js-object-key {:color "#888888"}}
                      :printer {
                                :file-info     {:color                "#42ae72"
                                                :font-style           :italic
                                                :padding-inline-start :0ch
                                                }
                                :eval-form     {:color             "#42ae72"
                                                :background-color  "#03401d"
                                                :text-shadow       "0 0 2px #000000"
                                                :font-style        :italic
                                                :margin-inline-end :2ch
                                                }
                                :comment       {:color             "#2e6666"
                                                :text-shadow       "0 0 2px #ffffff"
                                                :background-color  "#e5f1fa"
                                                :outline           "2px solid #e5f1fa"
                                                :margin-inline-end :2ch
                                                :font-style        :italic}
                                :atom-wrapper  {:color            "#42ae72"
                                                :background-color "#03401d"
                                                :text-shadow      "0 0 2px #000000"
                                                :font-style       :italic}}}})

(def monokai-light 
  {:name             "Monokai Light"
   :desc             "A light high-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "high"
   :tokens            {:classes {:background    {:background-color "#fff"}
                                 :string        {:color "#1386bf"}
                                 :constant      {:color "#8545e6"}
                                 :definition    {:color "#178c54"}
                                 :annotation    {:color      "#a6a6a6"
                                                 :font-style :italic}
                                 :comment       {:color      "#e000ca"
                                                 :font-style :italic}
                                 :metadata      {:color            "#9978b0"
                                                 :text-shadow      "0 0 2px #ffffff"
                                                 :background-color "#e7c2ff"}
                                 :metadata-key  {:color            "#9978b0"
                                                 :font-weight      :bold
                                                 :text-shadow      "0 0 2px #ffffff"
                                                 :background-color "#f9f0ff"}
                                 :metadata2     {:color            "#7a65d7"
                                                 :text-shadow      "0 0 2px #ffffff"
                                                 :background-color "#e9e5ff"}
                                 :metadata-key2 {:color            "#7a65d7"
                                                 :font-weight      :bold
                                                 :text-shadow      "0 0 2px #ffffff"
                                                 :background-color "#e9e5ff"}
                                 :label         {:color            "#398962"
                                                 :background-color "#e5fbe5"
                                                 :text-shadow      "0 0 2px #ffffff"
                                                 :font-style       :italic}}
                       :syntax  {:number        {:color "#cc3d9c"}
                                 :js-object-key {:color "#888888"}}
                       :printer {
                                 :file-info     {:color                "#316363"
                                                 :font-style           :italic
                                                 :padding-inline-start :0ch
                                                 }
                                 :eval-form     {:color             "#316363"
                                                 :text-shadow       "0 0 2px #ffffff"
                                                 :font-style        :italic
                                                 :background-color  "#defcfc"
                                                 :margin-inline-end :2ch
                                                 }
                                 :comment       {:color             "#2e6666"
                                                 :text-shadow       "0 0 2px #ffffff"
                                                 :background-color  "#e5f1fa"
                                                 :outline           "2px solid #e5f1fa"
                                                 :margin-inline-end :2ch
                                                 :font-style        :italic}
                                 :function-args {:color "#999999"}
                                 :atom-wrapper  :label} }})


(def monokai-dark 
  {:name             "Monokai Dark"
   :desc             "A dark high-contrast theme for Clojure, based on Monokai"
   :mood             "dark"
   :bracket-contrast "high"
   :tokens           {:classes {:background    {:background-color "#2d2a2e"}
                                :string        {:color "#c1a349"}
                                :constant      {:color "#cc99ff"}
                                :definition    {:color "#4fc94f"}
                                :annotation    {:color      "#999999"
                                                :font-style :italic}
                                :comment       {:color      "#2ef1ff"
                                                :font-style :italic}
                                :metadata      {:color            "#a395b1"
                                                :text-shadow      "0 0 2px #1b002e"
                                                :background-color "#412e47"}
                                :metadata-key  {:color            "#a395b1"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #000000"
                                                :background-color "#412e47"}
                                :metadata2     {:color            "#908de8"
                                                :text-shadow      "0 0 2px #000000"
                                                :background-color "#412e47"}
                                :metadata-key2 {:color            "#8481ea"
                                                :font-weight      :bold
                                                :text-shadow      "0 0 2px #000000"
                                                :background-color "#412e47"}
                                :label         {:color            "#9e9bf8"
                                                :background-color "#373a6d"
                                                :text-shadow      "0 0 2px #000000"
                                                :font-style       :italic}}
                      :syntax  {:number        {:color "#14bcd2"}
                                :js-object-key {:color "#888888"}}
                      :printer {
                                :file-info     {:color                "#e38282"
                                                :font-style           :italic
                                                :padding-inline-start :0ch
                                                }
                                :eval-form     {:color             "#ee7777"
                                                :text-shadow       "0 0 2px #000000"
                                                :font-style        :italic
                                                :background-color  "#500101"
                                                :margin-inline-end :2ch
                                                }
                                :atom-wrapper  {:color            "#81c581"
                                                :background-color "#054305"
                                                :text-shadow      "0 0 2px #152900"
                                                :font-style       :italic}}}})
