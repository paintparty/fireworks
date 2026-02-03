;; TODO - only support new named color system

;; TODO - preprocess these to be flat and support ansi-sgr, save compute that happens in state

;; TODO - catch bad values

(ns fireworks.themes 
  (:require
   [fireworks.defs :as defs :refer [bling-css-color]]
   [fireworks.pp :refer [?pp]]))



(def regex-syntax-neutral
  {:regex.character            {:font-weight :bold}
   :regex.quantifier           {}
   :regex.numeric-quantifier   {}
   :regex.group-mods           {}
   :regex.any-of-delimeter     {}
   :regex.group-delimeter      {}
   :regex.not-any-of-delimeter {} 
   :regex.anchor               {}    
   :regex.character-range      {:font-weight :bold}                                        
   :regex.number-range         {:font-weight :bold}                                        
   :regex.special-character    {:font-weight :bold}
   :regex.alternation          {}})

(def regex-syntax-neutral-light
  (assoc regex-syntax-neutral
         :regex.escape-backslash     
         {:color (bling-css-color :light-gray)}))

(def regex-syntax-neutral-dark
  (assoc regex-syntax-neutral
         :regex.escape-backslash     
         {:color (bling-css-color :dark-gray)}))


(def regex-syntax-light
  ;; minimal color
  #_{:regex.character            {}
   :regex.quantifier           {:color (bling-css-color :dark-blue)}
   :regex.numeric-quantifier   {:color (bling-css-color :dark-blue)}
   :regex.group-mods           {}
   :regex.any-of-delimeter     {}
   :regex.group-delimeter      {}
   :regex.not-any-of-delimeter {} 
   :regex.anchor               {:color (bling-css-color :dark-purple)}    
   :regex.character-range      {}                                        
   :regex.number-range         {}                                        
   :regex.special-character    {}
   :regex.escape-backslash     {:color (bling-css-color :light-gray)}
   :regex.alternation          {}
   }

  {:regex.character            {:color nil :font-weight :bold}
   :regex.quantifier           {:color (bling-css-color :dark-blue)}
   :regex.numeric-quantifier   {:color (bling-css-color :dark-blue)}
   :regex.group-mods           {:color (bling-css-color :dark-blue)}
   :regex.any-of-delimeter     {:color (bling-css-color :medium-blue) #_"#b063d4" #_(bling-css-color :medium-purple) }
   :regex.group-delimeter      {:color (bling-css-color :dark-green)}
   :regex.not-any-of-delimeter {:color (bling-css-color :dark-blue)    #_"#b063d4"#_(bling-css-color :medium-purple)} 
   :regex.anchor               {:color (bling-css-color :medium-orange) :font-weight :bold}    
   :regex.character-range      {:font-weight :bold :color (bling-css-color :dark-blue)}                                        
   :regex.number-range         {:font-weight :bold :color (bling-css-color :dark-blue)}                                        
   :regex.special-character    {:color (bling-css-color :dark-purple) :font-weight :bold}
   :regex.escape-backslash     {:color (bling-css-color :light-gray)}
   :regex.alternation          {:color (bling-css-color :medium-magenta)
                                :background-color "#fae7fa"}})


(def regex-syntax-dark
  {:regex.character            {:font-weight :bold}
   :regex.quantifier           {:color (bling-css-color :medium-green)}
   :regex.numeric-quantifier   {:color (bling-css-color :medium-green)}
   :regex.group-mods           {:color (bling-css-color :medium-green)}
   :regex.any-of-delimeter     {:color (bling-css-color :medium-blue)}
   :regex.group-delimeter      {:color (bling-css-color :medium-green) }
   :regex.not-any-of-delimeter {:color (bling-css-color :medium-blue)} 
   :regex.anchor               {:color (bling-css-color :medium-orange)}    
   :regex.character-range      {:color (bling-css-color :medium-blue) :font-weight :bold}                                        
   :regex.number-range         {:color (bling-css-color :medium-blue) :font-weight :bold}                                        
   :regex.special-character    {:color (bling-css-color :medium-purple) :font-weight :bold}
   :regex.escape-backslash     {:color (bling-css-color :dark-gray)}
   :regex.alternation          {:color (bling-css-color :medium-magenta)}})


(def regex-syntax-minimal
  {:regex.quantifier           {:color (bling-css-color :medium-blue)}
   :regex.numeric-quantifier   {:color (bling-css-color :medium-blue)}
   :regex.group-mods           {:color (bling-css-color :medium-blue)}
   :regex.any-of-delimeter     {:color (bling-css-color :medium-blue)}
   :regex.group-delimeter      {:color (bling-css-color :medium-blue)}
   :regex.not-any-of-delimeter {:color (bling-css-color :medium-blue)} 
   :regex.anchor               {:color (bling-css-color :medium-yellow)}    
   :regex.character-range      {:color (bling-css-color :medium-purple)}                                        
   :regex.special-character    {:color (bling-css-color :medium-purple)}                                        
   :regex.escape-backslash     {:color (bling-css-color :medium-gray)}
   :regex.number-range         {:color (bling-css-color :medium-purple)}
   :regex.alternation          {:color (bling-css-color :medium-magenta)}})


;; Put neutral tokens in all of these?
(defn highlighted-regex-syntax 
  [m]
  (let [{:keys [in-group in-any-of in-group-neutral in-any-of-neutral]}
        (let [neutral-light "#e0e0e0"
              neutral-dark  "#393939"]
          (cond
            (= m regex-syntax-light)
            {:in-group          {:background-color "#d8fbdd"}
             :in-any-of         {:background-color "#d3f3fa"}
             :in-group-neutral  {:background-color neutral-light}
             :in-any-of-neutral {:background-color neutral-light}}

            (= m regex-syntax-neutral-light)
            {:in-group          {:background-color neutral-light}
             :in-any-of         {:background-color neutral-light}
             :in-group-neutral  {:background-color neutral-light}
             :in-any-of-neutral {:background-color neutral-light}}

            (= m regex-syntax-dark)
            {
             :in-group          {:background-color "#003a09"}
             :in-any-of         {:background-color "#053b47"}
             ;;  :in-group          {:background-color "#036a13"}
             ;;  :in-any-of         {:background-color "#005e73"}
             :in-group-neutral  {:background-color neutral-dark}
             :in-any-of-neutral {:background-color neutral-dark}}

            (= m regex-syntax-neutral-dark)
            {:in-group          {:background-color neutral-dark}
             :in-any-of         {:background-color neutral-dark}
             :in-group-neutral  {:background-color neutral-dark}
             :in-any-of-neutral {:background-color neutral-dark}}

            ;; This covers "Universal Neutral"
            :else
            {:in-group          {}
             :in-any-of         {}
             :in-group-neutral  {}
             :in-any-of-neutral {}}))
        with-highlight-styles
        (reduce-kv 
         (fn [acc k v]
           (cond->
            (assoc acc
                   (keyword (str (name k) ".in-group"))
                   (merge v in-group)
                   (keyword (str (name k) ".in-any-of"))
                   (merge v in-any-of)
                   (keyword (str (name k) ".in-group.neutral"))
                   (merge v in-group-neutral)
                   (keyword (str (name k) ".in-any-of.neutral"))
                   (merge v in-any-of-neutral)
                   )

             (= m regex-syntax-light)
             (assoc (keyword (str (name k) ".neutral"))
                    (-> regex-syntax-neutral-light k)

                    (keyword (str (name k) ".in-group.neutral"))
                    (merge (-> regex-syntax-neutral-light k)
                           in-group-neutral)

                    (keyword (str (name k) ".in-any-of.neutral"))
                    (merge (-> regex-syntax-neutral-light k)
                           in-any-of-neutral))

             (= m regex-syntax-dark)
             (assoc (keyword (str (name k) ".neutral"))
                    (-> regex-syntax-neutral-dark k)
                    
                    (keyword (str (name k) ".in-group.neutral"))
                    (merge (-> regex-syntax-neutral-dark k)
                           in-group-neutral)

                    (keyword (str (name k) ".in-any-of.neutral"))
                    (merge (-> regex-syntax-neutral-dark k)
                           in-any-of-neutral))))
         {}
         m)]
    (when (= m regex-syntax-dark) (keys with-highlight-styles))
    (merge m with-highlight-styles)))

(def neutral-light
  {:name   "Neutral Light"
   :desc   "Neutral dark theme with no syntax coloring on data. The label
            above the printed result that displays the evaled form
            (or user-provided label) will be colorized. Metadata will also be
            colorized."
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
                                   :font-style       :italic}
                      :eval-label {:color            "#3e76a8"
                                   :background-color "#f0fbfe"
                                   :font-style       :italic}}
            :syntax  (merge {:js-object-key {:color "#888888"}}
                            (highlighted-regex-syntax regex-syntax-neutral-light))
            :printer {:file-info        :annotation
                      :eval-label       :eval-label
                      :eval-label-red   :eval-label-red
                      :eval-label-green :eval-label-green
                      :eval-label-blue  :eval-label-blue
                      :eval-form        :eval-label
                      :eval-form-red    :eval-label-red
                      :eval-form-green  :eval-label-green
                      :eval-form-blue   :eval-label-blue}}})
                        
(def neutral-dark
  {:name             "Neutral Dark"
   :desc             "Neutral dark theme with no syntax coloring on data. The label
            above the printed result that displays the evaled form
            (or user-provided label) will be colorized. Metadata will also be
            colorized."
   :mood             :dark
   :author           "Author Name"
   :langs            ["Clojure" "ClojureScript" "Babashka"]
   :bracket-contrast "low"
   :tokens           {:classes {:comment    {:color      "#e0d557"
                                             :font-style :italic}
                                :eval-label {:color            "#85b7e5"
                                             :font-style       :italic
                                             :background-color "#00345c"}
                                :metadata   {:color            "#bb7777"
                                             :text-shadow      "0 0 2px #003538"
                                             :background-color "#2e0a0a"}
                                :label      {:color            "#a9aabc"
                                             :background-color "#212d36"
                                             :font-style       :italic}
                                :metadata2  {:color            "#9773b5"
                                             :text-shadow      "0 0 2px #003538"
                                             :background-color "#260a3d"}} 
                      :syntax  (merge {:js-object-key {:color "#888888"}}
                                      (highlighted-regex-syntax regex-syntax-neutral-dark))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                }}})

;; TODO - migrate theme syntax towards this?
#_{:background-hue       :neutral
 :background-lightness 25       ;; optional
 :background-chroma    :medium  ;; optional
 :foreground-hue       :neutral
 :foreground-lightness 600      ;; optional
 :foreground-chroma    :medium  ;; optional
 :bracket-lightness    :600     ;; optional, syncs to foreground-lightness
 :bracket-chroma       :medium  ;; optional, syncs to foreground-saturation, applies only to rainbow or colored brackets 
 :rainbow-brackets?    true     ;; optional, overrides bracket below
 :languages            ["Clojure" "ClojureScript" "Babashka"]         
 :tokens               {:string                {:hue :green}
                        :annotation            {:hue        :neutral
                                                :contrast   :low  ;; -> adjusts both hue and saturation
                                                :font-style :italic}
                        :bracket               {:hue        :blue
                                                :contrast   :xlow
                                                :lightness  300
                                                :chroma     400
                                                :font-style :italic}
                        :reader-macro          :annotation
                        :js-object-key         :foreground
                        :printer/file-info     :annotation
                        :printer/function-args :annotation}}

(def alabaster-light
  {:name   "Alabaster Light"
   :desc   "Based on @tonsky's Alabaster theme."
   :about  "This is additional documentation. Should support markdown here."
   :url    "url goes here"
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   ;; :mood   :light
   :tokens {:classes {:background {:background-color "#f7f7f7"}
                      :string     {:color "#448C27"}
                      :constant   {:color "#7A3E9D"}
                      :definition {:color "#4d6dba"}
                      :annotation {:color      "#8c8c8c" 
                                   :font-style :italic}
                      :metadata   {:color            "#be55bb"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#fcf0ff"}
                      :metadata2  {:color            "#9f60be"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#ebedff"}
                      :label      {:color            "#c76823"
                                   :background-color "#fff9f5"
                                   :font-style       :italic}
                      :eval-label {:color            "#3764cd"
                                   :background-color "#f3f7feff"
                                   :font-style       :italic}}
            :syntax  (merge {:js-object-key {:color "#888888"}}
                            (highlighted-regex-syntax regex-syntax-light))
            :printer {:file-info        :annotation 
                      :eval-label       :eval-label
                      :eval-label-red   :eval-label-red
                      :eval-label-green :eval-label-green
                      :eval-label-blue  :eval-label-blue
                      :eval-form        :eval-label
                      :eval-form-red    :eval-label-red
                      :eval-form-green  :eval-label-green
                      :eval-form-blue   :eval-label-blue
                      :comment          {:color            "#2e6666"
                                         :text-shadow      "0 0 2px #ffffff"
                                         :background-color "#e5f1fa"
                                         :outline          "2px solid #e5f1fa"
                                         :font-style       :italic}
                      :function-args    {:color "#999999"}
                      :atom-wrapper     :label}}})


(def alabaster-dark 
  {:name   "Alabaster Dark"
   :desc   "Based on @tonsky's Alabaster Dark theme."
   :about  "This is additional documentation. Should support markdown here."
   :url    "url goes here"
   :mood   :dark
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :tokens {:classes {:background {:background-color "#0e1415"}
                      :string     {:color "#8cbd7a"}
                      :comment    {:color      "#DFDF8E"
                                   :font-style :italic}
                      :constant   {:color "#b696b5"}
                      :definition {:color "#71ADE7"}
                      :annotation {:color      "#a3a3a3ff"
                                   :font-style :italic}
                      :metadata   {:color            "#ae849b"
                                   :text-shadow      "0 0 2px #003538"
                                   :background-color "#3a1228"}
                      :metadata2  {:color            "#a08a40"
                                   :text-shadow      "0 0 2px #003538"
                                   :background-color "#351d1d"}
                      :label      {:color            "#5f9ed8"
                                   :background-color "#162f46"
                                   :font-style       :italic}
                      :eval-label {:color            "#85b7e5"
                                   :font-style       :italic
                                   :background-color "#00345c"}}
            :syntax  (merge {:js-object-key {:color "#b2b2b2"}
                             :number        {:color "#6392c5"}}
                            (highlighted-regex-syntax regex-syntax-dark))
            :printer {:file-info        :annotation
                      :eval-label       :eval-label
                      :eval-label-red   :eval-label-red
                      :eval-label-green :eval-label-green
                      :eval-label-blue  :eval-label-blue
                      :eval-form        :eval-label
                      :eval-form-red    :eval-label-red
                      :eval-form-green  :eval-label-green
                      :eval-form-blue   :eval-label-blue
                      :comment          {:color            "#2e6666"
                                         :text-shadow      "0 0 2px #ffffff"
                                         :background-color "#e5f1fa"
                                         :outline          "2px solid #e5f1fa"
                                         :font-style       :italic}
                      :function-args    {:color "#999999"}
                      :atom-wrapper     :label}}})


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
                      :metadata   {:color            "#618d98"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#e8f3fd"}
                      :metadata2  {:color            "#7065c3"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#e4e0ff"}
                      :label      {:color            "#c4793b"
                                   :background-color "#f7ece3"
                                   :font-style       :italic}
                      :eval-label {:color            "#4f7878"
                                   :background-color "#e5f1fa"
                                   :font-style       :italic}}
            :syntax  (merge {:number        {:color "#737373"}
                             :js-object-key {:color "#888888"}}
                            (highlighted-regex-syntax regex-syntax-light))
            :printer {:file-info        :annotation
                      :eval-label       :eval-label
                      :eval-label-red   :eval-label-red
                      :eval-label-green :eval-label-green
                      :eval-label-blue  :eval-label-blue
                      :eval-form        :eval-label
                      :eval-form-red    :eval-label-red
                      :eval-form-green  :eval-label-green
                      :eval-form-blue   :eval-label-blue
                      :comment          {:color            "#4d6f6f"
                                         :text-shadow      "0 0 2px #ffffff"
                                         :background-color "#e5f1fa"
                                         :outline          "2px solid #e5f1fa"
                                         :font-style       :italic}
                      :function-args    {:color "#999999"}
                      :atom-wrapper     :label }}})


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
                                   :background-color "#4d3f38"
                                   :font-style       :italic}
                      :eval-label {:color            "#9ac2d6"
                                   :background-color "#2b4c69"
                                   :font-style       :italic}}
            :syntax  (merge {:number        {:color "#afaf87"}
                             :js-object-key {:color "#888888"}}
                            (highlighted-regex-syntax regex-syntax-dark))
            :printer {:file-info        :annotation
                      :eval-label       :eval-label
                      :eval-label-red   :eval-label-red
                      :eval-label-green :eval-label-green
                      :eval-label-blue  :eval-label-blue
                      :eval-form        :eval-label
                      :eval-form-red    :eval-label-red
                      :eval-form-green  :eval-label-green
                      :eval-form-blue   :eval-label-blue
                      :comment          {:color            "#2e6666"
                                         :text-shadow      "0 0 2px #ffffff"
                                         :background-color "#e5f1fa"
                                         :outline          "2px solid #e5f1fa"
                                         :font-style       :italic}
                      :function-args    {:color "#b3b3b3"}}}})


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
                                             :background-color "#e8f3fd"}
                                :metadata2  {:color            "#7065c3"
                                             :text-shadow      "0 0 2px #ffffff"
                                             :background-color "#e4e0ff"}
                                :label      {:color            "#99770f"
                                             :background-color "#f5eccc"
                                             :font-style       :italic}
                                :eval-label {:color            "#618d98"
                                             :font-style       :italic
                                             :background-color "#e8f3fd"}}
                      :syntax  (merge {:number        {:color "#3d7a99"}
                                       :js-object-key {:color "#888888"}}
                                      (highlighted-regex-syntax regex-syntax-light))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                :comment          {:color            "#00ffff"
                                                   :text-shadow      "0 0 2px #ffffff"
                                                   :background-color "#e5f1fa"
                                                   :outline          "2px solid #e5f1fa"
                                                   :font-style       :italic}
                                :function-args    {:color "#9e9e9e"}
                                :atom-wrapper     :label}}})


(def zenburn-dark 
  {:name             "Zenburn Dark"
   :desc             "A dark low-contrast theme for Clojure"
   :mood             "dark"
   :bracket-contrast "low"
   :tokens           {:classes {:background {:background-color "#3f3f3f"}
                                :string     {:color "#dc8f8f"}
                                :constant   {:color "#8cc08c"}
                                :definition {:color "#bfbf69"}
                                :comment    {:color      "#76d5fe"
                                             :font-style :italic}
                                :annotation {:color      "#999999"
                                             :font-style :italic}
                                :metadata   {:color            "#96a4e8"
                                             :background-color "#353e69"
                                             :text-shadow      "0 0 2px #002916"}
                                :metadata2  {:color            "#b596c0"
                                             :background-color "#4e3257"
                                             :text-shadow      "0 0 2px #002916"}
                                :label      {:color            "#b49d5f"
                                             :background-color "#524019"
                                             :font-style       :italic}
                                :eval-label {:color            "#9ac2d6"
                                             :background-color "#2b4c69"
                                             :font-style       :italic}}
                      :syntax  (merge {:number                    {:color "#8fb8cc"}
                                       :nil                       {:color "#a9a9a9"}
                                       :js-object-key             {:color "#a9a9a9"}
                                       :escaped-double-quote-char :string
                                       :escape-char               {:color (bling-css-color :dark-gray)}
                                       :string-delimiter          {:color "#bfbf69"}}
                                      (highlighted-regex-syntax regex-syntax-dark))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                :atom-wrapper     :label}}})


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
                                             :background-color "#eef3ec"
                                             :font-style       :italic}
                                :eval-label {:color            "#6c71c4"
                                             :background-color "#eef3ec"
                                             :font-style       :italic}}
                      :syntax  (merge {:number        {:color "#af5f5f"}
                                       :boolean       {:color "#b58900"}
                                       :js-object-key {:color "#888888"}}
                                      (highlighted-regex-syntax regex-syntax-light))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
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
                                             :background-color "#2c2f04"}
                                :eval-label {:color            "#659bdc"
                                             :background-color "#263d5a"
                                             :font-style       :italic}}
                      :syntax  (merge {:number        {:color "#bf6986"}
                                       :js-object-key {:color "#888888"}}
                                      (highlighted-regex-syntax 
                                       regex-syntax-dark))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                :comment          {:color            "#2e6666"
                                                   :text-shadow      "0 0 2px #ffffff"
                                                   :background-color "#e5f1fa"
                                                   :outline          "2px solid #e5f1fa"
                                                   :font-style       :italic}
                                :atom-wrapper     :label}}})

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
                                              :font-style       :italic}
                                 :eval-label {:color            "#316363"
                                              :font-style       :italic
                                              :background-color "#defcfc"
                                              ;; :background-color "#edfdfdff" ; <- for testing level-2 color support
                                              }}

                       :syntax  (merge {:number        {
                                                        :color "#cc3d9c"
                                                        ;;  :color "#a7ebaaff" ; <- for testing level-2 color support
                                                        }
                                        :js-object-key {:color "#888888"}}
                                       (highlighted-regex-syntax regex-syntax-light))
                       :printer {:file-info        :annotation
                                 :eval-label       :eval-label
                                 :eval-label-red   :eval-label-red
                                 :eval-label-green :eval-label-green
                                 :eval-label-blue  :eval-label-blue
                                 :eval-form        :eval-label
                                 :eval-form-red    :eval-label-red
                                 :eval-form-green  :eval-label-green
                                 :eval-form-blue   :eval-label-blue
                                 :comment          {:color            "#2e6666"
                                                    :text-shadow      "0 0 2px #ffffff"
                                                    :background-color "#e5f1fa"
                                                    :outline          "2px solid #e5f1fa"
                                                    :font-style       :italic}
                                 :function-args    {:color "#999999"}
                                 :atom-wrapper     :label} }})


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
                                             :font-style       :italic}
                                :eval-label {:color            "#85b7e5"
                                             :font-style       :italic
                                             :background-color "#00345c"}}
                      :syntax  (merge {:number        {:color "#14bcd2"}
                                       :js-object-key {:color "#888888"}}
                                      (highlighted-regex-syntax regex-syntax-dark))
                      :printer {:file-info        :annotation
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                :atom-wrapper     :label}}})

(def universal-neutral
  {:name             "Universal Neutral"
   :desc             (str "A neutral syntax theme for Clojure data that works on both light and dark backgrounds."
                          "The eval label is printed in blue, metadata is printed in purple, and object labels are printed in green.")
   :mood             "universal"
   :tokens           {:syntax  (merge {:js-object-key {:color "#888888"}}
                                      regex-syntax-neutral
                                      #_(highlighted-regex-syntax regex-syntax-neutral))
                      :printer {:file-info     {:font-style :italic }
                                ;; TODO maybe these values should be :classes/eval-label
                                :eval-label       :eval-label
                                :eval-label-red   :eval-label-red
                                :eval-label-green :eval-label-green
                                :eval-label-blue  :eval-label-blue
                                :eval-form        :eval-label
                                :eval-form-red    :eval-label-red
                                :eval-form-green  :eval-label-green
                                :eval-form-blue   :eval-label-blue
                                :comment       {:font-style :italic}
                                :function-args {:color "#9e9e9e"}}}})

(def universal
  {:name   "Universal"
   :desc   "A colorized syntax theme for Clojure data that works on both light
            and dark backgrounds. The label above the printed result that
            displays the evaled form (or user-provided label) will not be
            colorized."
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
   :tokens {:classes {:string     {:color "#00d700"}
                      :definition {:color "#00afff"}
                      :metadata   {:color "#af87ff"}     ;; purple 
                      :metadata2  {:color "#87af00"}     ;; yellow
                      :label      {:color      "#ff00ff" ;; magenta
                                   :font-style :italic}}

            :syntax  (merge {:number        {:color "#ff8700"} ;; orange 
                             :js-object-key {:color "#ff00ff"}}
                            (highlighted-regex-syntax regex-syntax-minimal))

            :printer {:file-info     :annotation
                      :comment       {:font-style :italic}
                      :function-args {:color "#9e9e9e"}}}})

(def alabaster-light-legacy
  {:name   "Alabaster Light"
   :desc   "Based on @tonsky's Alabaster theme."
   :about  "This is additional documentation. Should support markdown here."
   :url    "url goes here"
   :author "Author Name"
   :langs  ["Clojure" "ClojureScript" "Babashka"]
   :tokens {:classes {:background {:background-color "#f7f7f7"}
                      :string     {:color "#448C27"}
                      :constant   {:color "#7A3E9D"}
                      :definition {:color "#4d6dba"}
                      :annotation {:color      "#8c8c8c" 
                                   :font-style :italic}
                      :metadata   {:color            "#be55bb"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#fae8fd"}
                      :metadata2  {:color            "#9f60be"
                                   :text-shadow      "0 0 2px #ffffff"
                                   :background-color "#e9e5ff"}
                      :label      {:color            "#398962"
                                   :background-color "#eefbee"
                                  ;;  :color            "#619488"
                                  ;;  :background-color "#e7f9f5"
                                   :font-style       :italic}
                      :eval-label {:color            "#4d6dba"
                                   :background-color "#edf2fc"
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
