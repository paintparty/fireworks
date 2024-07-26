(ns fireworks.messaging
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp]]
            [expound.alpha :as expound]
            [get-rich.core :refer [point-of-interest callout]]))

(defrecord FireworksThrowable [err err-x err-opts])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))


(defn bad-option-value-warning
  [{:keys [k v spec default header body line column file]}]
  (callout {:type           :warning
            ;; :heavy?         true
            ;; :wrap?          true
            :padding-bottom 0
            :label          nil}
           (point-of-interest
            (merge {:squiggly-color :warning
                    :form           (str k " " v)          
                    :line           line
                    :column         column
                    :file           file
                    :header         (or header "Invalid option value:")
                    :body           (str (expound/expound-str spec
                                                              v
                                                              {:print-specs? false})
                                         (when default
                                           (str "\n\n"
                                                "The default value of `"
                                                default
                                                "` will be applied."))
                                         (when body (str "\n\n" body)))}))))


(defn unable-to-trace [opts]
  (callout opts
           (point-of-interest
            (merge opts
                   {:squiggly-color
                    :warning

                    :form           
                    (str "(?trace " (:form opts) ")")          

                    :header         
                    "Unable to trace form."

                    :body           
                    (str "fireworks.core/?trace will trace forms beginning with:\n"
                         (string/join "\n"
                                      ['->
                                       'some->
                                       '->>
                                       'some->>
                                       ;; Add let when you actually support it
                                       ;; 'let
                                       ]))}))))


;; TODO - maybe move this to get-rich?
;; Would have to pass in the regex part
(defn user-friendly-clj-stack-trace [err]
   #?(:clj
      (let [st          (->> err .getStackTrace)
            st-len      (count st)
            mini-st     (->> st (take 10) (map StackTraceElement->vec))
            indexes     (keep-indexed
                         (fn [i [f]]
                           (when (re-find #"^fireworks\.|^lasertag\."
                                          (str f)) 
                             i))
                         mini-st)
            last-index  (when (seq indexes)
                          (->> indexes (take 5) last))
            trace*      (when last-index
                          (->> mini-st (take (inc last-index))))
            len         (when trace* (count trace*)) 
            trace       (some->> trace* (interpose "\n") (into []))
            num-dropped (- (or st-len 0) (or len 0))
            trace       (when trace
                          (conj trace
                                "\n"
                                (symbol (str "..."
                                             (some->> num-dropped
                                                      (str "+"))))))]
        (apply str trace))))


;; version with stack trace
(defn caught-exception
  ([opts]
   (caught-exception nil opts))
  ([err {:keys [k v form body]
         :as   opts}]
   (callout (assoc opts :type :error)
            (point-of-interest
             (merge opts
                    {:squiggly-color
                     :error

                     :form
                     (or form (str k " " v))

                     :label          
                     "CAUGHT EXCEPTION"

                     :body           
                     (if err
                       #?(:cljs
                          nil
                          :clj
                          (conj [[:italic.subtle.bold "Message from Clojure:"]
                                 "\n"
                                 (string/replace (.getMessage err) #"\(" "\n(")
                                 "\n\n"
                                 [:italic.subtle.bold "Stacktrace preview:"]
                                 "\n"
                                 (user-friendly-clj-stack-trace err)]
                                body))
                       body)})))))



;; TODO fix this for new callouts
;; (def dispatch 
;;   {:messaging/bad-option-value-warning bad-option-value-warning
;;    :messaging/read-file-warning        read-file-warning
;;    :messaging/print-error              print-error})

(def dispatch 
  {:messaging/bad-option-value-warning bad-option-value-warning
   :messaging/read-file-warning        caught-exception
   :messaging/print-error              caught-exception})

;; Race-condition-free version of clojure.core/println,
;; maybe useful if any weird behavior arises
;; (defn safe-println [& more]
;;   (.write *out* (str (clojure.string/join " " more) "\n")))
