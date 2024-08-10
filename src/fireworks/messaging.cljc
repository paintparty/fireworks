(ns fireworks.messaging
  (:require [clojure.string :as string]
            [expound.alpha :as expound]
            [get-rich.core :as get-rich :refer [point-of-interest callout]]))

(defrecord FireworksThrowable [err err-x err-opts])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))


(defn bad-option-value-warning
  [{:keys [k v spec default header body line column file form]}]
  (callout {:type           :warning
            :padding-top    1
            :padding-bottom 0
            :label          nil}
           (point-of-interest
            (merge {:type   :warning
                    :form   (or form (symbol (str k " " v)))          
                    :line   line
                    :column column
                    :file   file
                    :header (or header "Invalid option value:")
                    :body   (str (expound/expound-str spec
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
                   {:type
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
;; version with stack trace
(defn caught-exception
  ([opts]
   (caught-exception nil opts))
  ([err {:keys [k v form body]
         :as   opts}]
   (callout (merge opts
                   {:type        :error
                    :padding-top 1})
            (point-of-interest
             (merge opts
                    {:type
                     :error

                     :form
                     (or form (symbol (str k " " v)))

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
                                 (get-rich/stack-trace-preview
                                  {:error err
                                   :regex #"^fireworks\.|^lasertag\."
                                   })]
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
