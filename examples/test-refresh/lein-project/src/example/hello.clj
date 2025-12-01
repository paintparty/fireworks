(ns example.hello
  (:require [fireworks.core :refer [? !?]]
            [fireworks.sample :as sample]))

;; Fireworks configuration
;; -----------------------------------------------------------------------------

;; The default theme is "Universal Neutral", which is suitable for both light
;; and dark backgrounds

;; You can choose a colorized theme in a light or dark variant, from the list of
;; stock themes below, to match your console environment.

;; See the Options section of the readme for info about all the options.

(fireworks.core/config!
 {
  ;; :theme "Universal Neutral"
  :theme "Alabaster Light"
  ;; :theme "Alabaster Dark"
  ;; :theme "Neutral Light"     
  ;; :theme "Neutral Dark"      
  ;; :theme "Degas Light"       
  ;; :theme "Degas Dark"        
  ;; :theme "Zenburn Light"     
  ;; :theme "Zenburn Dark"      
  ;; :theme "Solarized Light"   
  ;; :theme "Solarized Dark"    
  ;; :theme "Monokai Light"     
  ;; :theme "Monokai Dark"      

  :display-metadata? true
  })


(defn greet [name]
  (str "Hello " name))


;; A few examples
;; -----------------------------------------------------------------------------

;; `fireworks.core/?` is a macro that prints a form and returns it.

;; Simple example
(? (greet "there 1"))

;; With custom label
(? "My label" (greet "there 2"))

;; Use the :no-label flag to omit label/form 
(? :no-label (greet "there 3"))

;; Use the :no-file flag to omit file-info 
(? :no-file (greet "there 4"))

;; Use the :- to omit the label/form and file-info 
(? :- (greet "there 5"))


;; `fireworks.core/!?` is a macro that just returns the form, when you want to
;; temporarily silence a form wrapped with `?` 
;; To run the examples below, change the `!?` calls to `?`.
;; If available for your editor, you can use an integration to toggle this:
;; https://github.com/paintparty/fireworks?tab=readme-ov-file#editor-integrations

;; Default truncation of colls is 33
(!? (range 50))

;; Adjust truncation of colls
(!? {:coll-limit 100} (range 50))

;; Flag to disable all truncation of colls and long strings
(!? :+ (range 50))

;; Print the form-to-be-evaled as formatted code.
(!? {:format-label-as-code? true}
    (mapv (fn [i] (str "id-" i))
          (range 20)))

;; Change the label color (not visible in the "Universal Default" theme)
;; Useful if you are printing a few different things from different parts of
;; codebase and want a quick way to distinguish between them.
(!? {:label-color :red} (greet "Red"))
(!? {:label-color :green} (greet "Green"))
(!? (greet "Blue"))


;; Kitchen sink example of different value types and data structures
(!? sample/array-map-of-everything-cljc)

;; Print above with me.flowthing.pp/pprint
(!? :pp sample/array-map-of-everything-cljc)

;; You can also print with other fns such as prn (will print escaped linebreaks) 
(!? {:print-with prn} "Line One\nLine Two")
(!? "Line One\nLine Two")
