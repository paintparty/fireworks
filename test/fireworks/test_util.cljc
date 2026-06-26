(ns fireworks.test-util
  (:require [clojure.string :as string]))

;; Toggle this true / false to generate tests 
(def write-tests? false)
;; (def write-tests? true)

;; Toggle this true / false to see tests
;; (def visual-mode? true)
(def visual-mode? false)

;; If this is populated, only those tests will be shown, when visual mode is active
(def filter-tests
  #{}
  ;; #{'datatype-value 'java-util-hashmap}
  )


(defn escape-sgr
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
