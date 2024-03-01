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
   :tokens   {:classes {:background {:background-color "#f7f7f7"}
                        :string     {:color "#448C27"}
                        :comment    {:color      "#AA3731"
                                     :font-style :italic}
                        :constant   {:color "#7A3E9D"}
                        :definition {:color "#4d6dba"}
                        :annotation {:color      "#8c8c8c" 
                                     :font-style :italic}
                        :metadata   {:color            "#2e6666"
                                     :text-shadow      "0 0 2px #ffffff"
                                     :background-color "#e6fafa"}}
             :syntax  {:js-object-key {:color "#888888"}}
             :printer {:eval-fat-arrow {:color "#28cc7d"}
                       :function-args  {:color "#999999"}
                       :atom-wrapper   {:color            "#256546"
                                        :background-color "#e5fbe5"
                                        :text-shadow      "0 0 2px #ffffff"
                                        :font-style       :italic}}}})


(def alabaster-dark
  {:name    "Alabaster Dark"
   :desc    "Based on @tonsky's Alabaster Dark theme."
   :about   "This is additional documentation. Should support markdown here."
   :url     "url goes here"
   :mood    :dark
   :author  "Author Name"
   :langs   ["Clojure" "ClojureScript" "Babashka"]
   :tokens   {:classes {:background      {:background-color "#0e1415"}
                        :string          {:color "#95CB82"}
                        :comment         {:color      "#DFDF8E"
                                          :font-style :italic}
                        :constant        {:color "#c0a1bf"}
                        :definition      {:color "#71ADE7"}
                        :annotation      {:color      "#DFDF8E"
                                          :font-style :italic}
                        :metadata        {:color            "#77a6a6"
                                          :text-shadow      "0 0 2px #003538"
                                          :background-color "#083f3f"}
                        :metadata-offset {:color "#207474"}}
              :syntax  {:js-object-key {:color "#b2b2b2"}}
              ;; :printer {:literal-label {:color      "#DFDF8E"
              ;;                           :font-style :italic}}
              }})


(def degas-light
  {:name  "Degas Light"
   :desc  "A light, low-contrast pastel theme for Clojure"
   :mood  "light"
   :tokens {:classes {:background {:background-color "#f5f9f9"}
                      :string     {:color "#5c9999"}
                      :constant   {:color "#bf6faf"}
                      :definition {:color "#5f87d7"}
                      :comment    {:color      "#5728f0"
                                   :font-style :italic}
                      :annotation {:color      "#9e9e9e"
                                   :font-style :italic}
                      :metadata   {:color            "#4b8eaf"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#e6fafa"}}
           :syntax  {:number {:color "#737373"}
                     :js-object-key {:color "#888888"}}
           :printer {:function-args {:color "#9e9e9e"}
                     :atom-wrapper  {:color            "#256546"
                                     :background-color "#d6f5d6"
                                     :text-shadow      "0 0 2px #ffffff"
                                     :font-style       :italic}}}})


(def degas-dark
  {:name  "Degas Dark"
   :desc  "A dark pastel theme for Clojure"
   :mood  "dark"
   :tokens {:classes {:background {:background-color "#363f4e"}
                      :foreground {:color "#bfbfbf"}
                      :string     {:color "#8cc08c"}
                      :constant   {:color "#c0a1bf"}
                      :definition {:color "#80a3ea"}
                      :comment    {:color      "#e1d084"
                                   :font-style :italic}
                      :annotation {:color      "#999999"
                                   :font-style :italic}
                      :metadata   {:color            "#b191b0"
                                   :text-shadow      "0 0 2px #000738"
                                   :background-color "#4a3f4a"}
                      :label      {:color            "#8ba7d5"
                                   :text-shadow      "0 0 2px #000738"
                                   :background-color "#3d4b61"}}
           :syntax  {:number {:color "#afaf87"}
                     :js-object-key {:color "#888888"}}
           :printer {:function-args {:color "#b3b3b3"}}}})


(def zenburn-light 
  {:name             "Zenburn Light"
   :desc             "A light low-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#f9f8f5"}
                                :foreground {:color "#666666"}
                                :string     {:color "#a64b64"}
                                :constant   {:color "#548354"}
                                :definition {:color "#99770f"}
                                :comment    {:color      "#0060e6"
                                             :font-style :italic}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :metadata   {:color            "#618d98"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#e8f3fd"}}
                      :syntax  {:number        {:color "#3d7a99"}
                                :js-object-key {:color "#888888"}}
                      :printer {:function-args {:color "#9e9e9e"}
                                :atom-wrapper  {:color            "#c4793b"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :background-color "#f7ece3"
                                                :font-style       :italic}}}})

(def zenburn-dark 
  {:name             "Zenburn Dark"
   :desc             "A dark low-contrast theme for Clojure"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#3f3f3f"}
                                :string     {:color "#cc8585"}
                                :constant   {:color "#8cc08c"}
                                :definition {:color "#cccc7a"}
                                :comment    {:color      "#76d5fe"
                                             :font-style :italic}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :metadata   {:color            "#b19d8c"
                                             :text-shadow      "0 0 2px #321d06"
                                             :background-color "#524f4c"}
                                :label      {:color            "#b49d5f"
                                             :background-color "#584928"
                                             :text-shadow      "0 0 2px #000000"
                                             :font-style       :italic}}
                      :syntax  {:number        {:color "#8fb8cc"}
                                :js-object-key {:color "#888888"}}
                      :printer {:atom-wrapper {:color            "#83a59c"
                                               :background-color "#475c54"
                                               :text-shadow      "0 0 2px #002916"
                                               :font-style       :italic}}}})



(def solarized-light 
  {:name             "Solarized Light"
   :desc             "A light low-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#fdf6e3"}
                                :foreground {:color "#666666"}
                                :string     {:color "#4ba6a6"}
                                :definition {:color "#5270cc"}
                                :comment    {:color      "#AA3731"
                                             :font-style :italic}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :metadata   {:color            "#6f7cae"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#ecf0fd"}
                                :label      {:color            "#808080"
                                             :background-color "#f6e8c1"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :font-style       :italic}}
                      :syntax  {:number        {:color "#af5f5f"}
                                :js-object-key {:color "#888888"}}
                      :printer {:atom-wrapper {:color            "#b65da7"
                                               :text-shadow      "0 0 2px #ffffff"
                                               :background-color "#f5e1f2"
                                               :font-style       :italic}}}})


(def solarized-dark 
  {:name             "Solarized Dark"
   :desc             "A dark low-contrast theme for Clojure, based on"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#002b36"}
                                :foreground    {:color "#999999"}
                                :bracket    {:color "#808080"}
                                :string     {:color "#33a3a3"}
                                :constant   {:color "#8f8f8f"}
                                :definition {:color "#5289cc"}
                                :comment    {:color      "#ee63b4"
                                             :font-style :italic}
                                :label      {:color            "#5289cc"
                                             :background-color "#143966"
                                             :text-shadow      "0 0 2px #000000"
                                             :font-style       :italic}
                                :metadata   {:color            "#8a8a8a"
                                             :background-color "#333333"
                                             :text-shadow      "0 0 2px #000000"}}
                      :syntax  {:number        {:color "#bf6986"}
                                :js-object-key {:color "#888888"}}
                      :printer {:atom-wrapper {:color            "#42ae72"
                                               :background-color "#03401d"
                                               :text-shadow      "0 0 2px #000000"
                                               :font-style       :italic}}}})


(def monokai-light 
  {:name             "Monokai Light"
   :desc             "A light high-contrast theme for Clojure"
   :mood             "light"
   :bracket-contrast "high"
   :tokens            {:classes {:background {:background-color "#fff"}
                                 :string     {:color "#1386bf"}
                                 :constant   {:color "#8545e6"}
                                 :definition {:color "#369e36"}
                                 :annotation {:color      "#a6a6a6"
                                              :font-style :italic}
                                 :comment    {:color      "#e000ca"
                                              :font-style :italic}
                                 :metadata   {:color            "#9978b0"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :background-color "#f9f0ff"}
                                 :label      {:color            "#6b6b6b"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :background-color "#f5f5f5"}}
                       :syntax  {:number        {:color "#cc3d9c"}
                                 :js-object-key {:color "#888888"}}
                       :printer {:atom-wrapper {:color            "#39b139"
                                                :background-color "#e3fce3"
                                                :text-shadow      "0 0 2px #ffffff"
                                                :font-style       :italic}} }})


(def monokai-dark 
  {:name             "Monokai Dark"
   :desc             "A dark high-contrast theme for Clojure, based on Monokai"
   :mood             "dark"
   :bracket-contrast "high"
   :tokens            {:classes {:background {:background-color "#2d2a2e"}
                                 :string     {:color "#c1a349"}
                                 :constant   {:color "#cc99ff"}
                                 :definition {:color "#9adb9a"}
                                 :annotation {:color      "#999999"
                                              :font-style :italic}
                                 :comment    {:color      "#2ef1ff"
                                              :font-style :italic}
                                 :metadata   {:color            "#a395b1"
                                              :text-shadow      "0 0 2px #1b002e"
                                              :background-color "#412e47"}
                                 :label      {:color            "#8f90d1"
                                              :background-color "#373a6d"
                                              :text-shadow      "0 0 2px #000000"
                                              :font-style       :italic}}
                       :syntax  {:number        {:color "#35cfe3"}
                                 :js-object-key {:color "#888888"}}
                       :printer {:atom-wrapper {:color            "#81c581"
                                                :background-color "#054305"
                                                :text-shadow      "0 0 2px #152900"
                                                :font-style       :italic}}}})
