(ns fireworks.profiling
  (:require [fireworks.prof :as p]
            [hifi.sample :as sample]
            [lasertag.core]
            [clojure.pprint :refer [pprint]]
            [fireworks.core :refer [?]]))   ; <- your real entry-point ns

(p/set-width! 150)

(defn profile-macro [{:keys [mode chart start-path] :or {chart :full}}]
  (p/clear!)
  (p/enable! mode)
  (try
    (dotimes [_ 50]
      (p/boundary!)
      (fireworks.core/? {:print-length 33} (get sample/everything* "Collections")#_(:regex sample/everything2)))   ; your real call
    (p/report chart start-path)
    (finally (p/disable!))))

(defn profile-fn [{:keys [mode chart start-path] :or {chart :full}}]
  (p/clear!)
  (p/enable! mode)
  (try
    (dotimes [_ 50]
      (p/boundary!)
      (p/prof 'pprint (pprint (get sample/everything* "Collections") #_(:regex sample/everything2))))   ; your real call
    (p/report chart start-path)
    (finally (p/disable!))))

;; (? {:perf 500} 
;;    (let [s (re-find (:regex sample/everything2) "ataa")]
;;      (str s (+ 2 333))))


#_(profile-macro {:mode  :nested  ;<- :nested or :sequential

                  :chart :mean-bar        ;<- :full or :mean (for report style)
                  ;; :start-path ['formatted]
                  ;; :start-path ['reset-state!]
                  })


#_(profile-fn {:mode :nested :chart :man-bar})
