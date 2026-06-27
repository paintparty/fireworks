(ns fireworks.profiling
  (:require [fireworks.prof :as p]
            [fireworks.sample :as sample]
            [fireworks.core :refer [?]]))   ; <- your real entry-point ns

(p/set-width! 150)

(defn run-profile [{:keys [mode chart start-path] :or {chart :full}}]
  (p/clear!)
  (p/enable! mode)
  (try
    (dotimes [_ 50]
      (p/boundary!)
      (fireworks.core/? {:data? true} :foo))   ; your real call
    (p/report chart start-path)
    (finally (p/disable!))))


;; (run-profile {:mode :nested  ;<- :nested or :sequential
;;               :chart :mean        ;<- :full or :mean (for report style)
;;               })
(run-profile {:mode       :nested  ;<- :nested or :sequential
              :chart      :mean-bar        ;<- :full or :mean (for report style)
              :start-path ['formatted]}
             )
;; truncated                         profiled                             serialized                                
;; 80ms                              50ms                                 30ms
;; ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒

;;                                   sweetner              milk                      
;;                                   70ms                  20ms
;;                                   ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒▒▒▒▒▒ 

;;                                   sugar     water
;;                                   30ms      25ms
;;                                   ▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒ 

;; truncated                         profiled                             serialized                                
;; 80ms                              50ms                                 30ms
