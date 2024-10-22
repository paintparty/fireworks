(ns visual-testing.browser
  (:require
   [fireworks.core :refer [? !? ?> !?>]]
   [visual-testing.macros :refer-macros [test-clj]]
   [visual-testing.shared :refer [foo test-suite]]
   ))


;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []

;; To test readfile warnings in fireworks.macros
;; 1) ENV VAR value of:
;;    FIREWORKS_CONFIG="wtf"
;;    This should give a "must be edn file warning"
;; 2) ENV VAR value of:
;;    FIREWORKS_CONFIG="wtf.edn"
;;    Where wtf.edn is not a valid file path, or is unparsable as an edn file, 
;;    This should give a "file cannot be read", or "file cannot be parsed" warning.
  
;; To test bad config value in user config:
;; 1) Change a value, say :print-level, in .edn config from an int to a string
;;    Should give warning
;; 1) Change a value, say :print-level at the callsite, like:
;;    (? {:print-level "foo"} "hi")
;;    This should give a warning about the bad option value
  

;; Bad color value in theme
;; (? {:theme alabaster-light+} "string")
  

;; Bad color value in style-map for sytle override, only used if custom printers
;; are enabled
;; (fireworks.state/with-conformed-colors
;;   {:style-map                {:color 7}
;;    :theme-token              :type-label
;;    :from-custom-badge-style? true})
  

;; Invalid map for :find highlighting
;; (? {:find {:predz #(= 1 %)}} [1 2 3])
  
;; Arbitrary error that will happen in fireworks.core/formatted
;; Make sure to uncomment the bad form in fireworks.core/formatted first
;; (? "force error")
  ;;  (? {:theme "Neutral Light"}
  ;;     {:boolean     true
  ;;      :function    cycle
  ;;      :nil         nil
  ;;      :number      12345
  ;;      :regex       #"^hello$"
  ;;      :string      "hello"
  ;;      :string-xl   "really-long-string-abcdefghijklm"
  ;;      :symbol      'mysymbol
  ;;      :coll/map    {:a [1 2 [:x :y]]}
  ;;      :coll/vector [:a :b :c [1 2 [:x :y]]]
  ;;      :coll/set    #{:a :b :c}
  ;;      :coll/record (->Foo "a" "b")})

  ;; TESTING CODE FOR THEMES & FEATURES

  (js/console.clear)

  ;; This will run test suite of cljc calls to fireworks.core/? in browser dev-console 
  (test-suite)

  ;; This will run same visual test suite in terminal where shadow is running
  (println (test-clj)))



(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  #_(js/console.log "init")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  #_(js/console.log "stop"))

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
                      :metadata2     {:color            "#999ad1"
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#3e4379"}
                      :label         {:color            "#8ba7d5"
                                      :text-shadow      "0 0 2px #000738"
                                      :background-color "#3d4b61"
                                      :font-style       :italic}
                      :eval-label    {:color             "#eaa580"
                                      :text-shadow       "0 0 2px #202288"
                                      :background-color  "#5f331b"
                                      :font-style        :italic}}
            :syntax  {:number        {:color "#afaf87"}
                      :js-object-key {:color "#888888"}}
            :printer {:file-info     {:color                "#eaa580"
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
