
;; This is reqd by specs
(ns fireworks.basethemes
  (:require
   [fireworks.defs :as defs]
   [fireworks.themes :as themes]))


;; Cheat sheet for ansi colors -- https://www.ditig.com/256-colors-cheat-sheet 

(def rainbow-brackets-colorscale
  {:light {:x-term  {:high (apply array-map 
                                  [:neutral 241
                                   :blue    32
                                   :orange  208
                                   :green   28
                                   :purple  128])
                     :low  (apply array-map 
                                  [:neutral 245
                                   :blue    74
                                   :orange  179
                                   :green   106
                                   :purple  177])} 
           :browser {:high (apply array-map 
                                  [:neutral "#888888"
                                   :blue    "#0098e6"
                                   :orange  "#ce7e00"
                                   :green   "#3fa455"
                                   :purple  "#c968e6"])
                     :low  (apply array-map 
                                  [:neutral "#8a8a8a"
                                   :blue    "#5fafd7"
                                   :orange  "#d7af5f"
                                   :green   "#87af00"
                                   :purple  "#d787ff"])}}
   :dark  {:x-term  {:high (apply array-map 
                                  [:neutral 253
                                   :blue    81
                                   :orange  208
                                   :green   35
                                   :purple  201])
                     :low  (apply array-map 
                                  [:neutral 245
                                   :blue    74
                                   :orange  179
                                   :green   106
                                   :purple  177])} 
           :browser {:high (apply array-map 
                                  [:neutral "#b9bab5"
                                   :blue    "#24b3cc"
                                   :orange  "#baa621"
                                   :green   "#62c355"
                                   :purple  "#da72c5"])
                     :low  (apply array-map 
                                  [:neutral "#8a8a8a"
                                   :blue    "#3986ac"
                                   :orange  "#b0893b"
                                   :green   "#4d9900"
                                   :purple  "#ad5dd5"])}}})

;; Experimental, unused for now
(def base-theme-editor-map
  {:viewport                   {:background-color :#ffffff}
   :active-tab                 :foreground
   :tab                        :foreground
   :breadcrumbs                :foreground
   :line-numbers               :foreground
   :active-line-number         :foreground
   :active-line                :foreground
   :explorer                   :foreground
   :explorer-subsection        :foreground
   :explorer-subsection-header :foreground
   :status-bar                 :foreground
   :find                       :highlight
   :find-active                {:background-color :#ffffff}
   :find-active-line           {:background-color :#ffd83c}
   :selection                  {:background-color :#9ff7ff}})


;; This base-theme is essentially monochrome/neutral, and provides a
;; base for building other themes.

;; The only things that should be specified here are:
;; :foreground, :bracket, :comment, :annotation, :highlight.

(def base-theme-light*
  ;; Any single keywords (as vals) in all the maps below refer
  ;; to the values defined in the :classes entry 
  {:classes          (merge defs/base-classes
                            {:foreground     {:color "#585858"}
                             :bracket        {:color "#888888"}
                             :comment        :annotation
                             :annotation     {:color      "#9f9f9f"
                                              :font-style :italic}
                             :highlight      {:background-color "#ffee00"}
                             ;; TODO - change this to badge?
                             :label          {:color            "#808080"
                                              :background-color "#ededed"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :font-style       :italic}
                             :eval-label-red {:color            "#af5a5a"
                                              :background-color "#fdf2f2"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :font-style       :italic}
                             :eval-label-green {:color            "#309738"
                                              :background-color "#f2fdf2"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :font-style       :italic}
                             :eval-label-blue {:color            "#309738"
                                               :background-color "#f2fdf2"
                                               :text-shadow      "0 0 2px #ffffff"
                                               :font-style       :italic}
                             :metadata       {:color            "#808080"
                                              :text-shadow      "0 0 2px #ffffff"
                                              :background-color "#e6fafa"}})

   ;; TODO - I don't think these currently merge if we were to use maps
   :syntax           defs/base-syntax-tokens
   :printer          defs/base-printer-tokens})


(def base-theme-dark*
  ;; Any single keywords (as vals) in all the maps below refer
  ;; to the values defined in the :classes entry 
  {:classes {:foreground    {:color "#cecece"}
             :bracket    {:color "#b2b2b2"}
             :comment    :annotation
             :annotation {:color      "#a8a8a8"
                          :font-style :italic}
             :metadata   {:color       "#999999"
                          :text-shadow "0 0 2px #003538"}
             :highlight  {:color            "#ffffff"
                          :background-color "#5f5fff"}
             :label      {:color            "#a3a3a3"
                          :background-color "#333333"
                          :text-shadow      "0 0 2px #000000"
                          :font-style       :italic}}
   :syntax  defs/base-syntax-tokens
   :printer defs/base-printer-tokens})


(def stock-themes
  {"Alabaster Light"   themes/alabaster-light
   "Alabaster Dark"    themes/alabaster-dark
   "Neutral Light"     themes/neutral-light
   "Neutral Dark"      themes/neutral-dark
   "Degas Light"       themes/degas-light
   "Degas Dark"        themes/degas-dark
   "Zenburn Light"     themes/zenburn-light
   "Zenburn Dark"      themes/zenburn-dark
   "Solarized Light"   themes/solarized-light
   "Solarized Dark"    themes/solarized-dark
   "Monokai Light"     themes/monokai-light
   "Monokai Dark"      themes/monokai-dark
   "Universal"         themes/universal
   "Universal Neutral" themes/universal-neutral})
