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

