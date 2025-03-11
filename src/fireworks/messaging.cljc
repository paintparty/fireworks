(ns fireworks.messaging
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp pprint]]
            [expound.alpha :as expound]
            [bling.core :refer [stack-trace-preview bling point-of-interest callout]]))

(defrecord FireworksThrowable [err err-x err-opts])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))

(defn fw-debug-report-template
  ([s x]
   (fw-debug-report-template s x :info))
  ([s x k]
   (callout {:label       s
             :padding-top 1
             :type        k}
            (with-out-str (pprint x)))))

(defn bad-option-value-warning
  [{:keys [k v spec default header body line column file form]}]
  (callout {:type           :warning
            :label-theme    :marquee
            :padding-top    1
            :padding-bottom 0
            :label          "WARNING: Invalid value"}
           (point-of-interest
            (merge {:type   :warning
                    :form   (or form
                                (let [str? (string? v)]
                                  (symbol (str k
                                               " "
                                               (when str? "\"")
                                               v
                                               (when str? "\"")))))          
                    :line   line
                    :column column
                    :file   file
                    :header (or header "Invalid option value:")
                    :body   (apply bling 
                                   (concat
                                    (when (and form k v)
                                      ["\n"
                                       "Bad value:\n\n"
                                       [:bold k]
                                       " "
                                       [:bold v]
                                       (str "\n"
                                            (string/join (repeat (count (str k)) " "))
                                            " ")
                                       [:bold.warning (string/join (repeat (count (str v)) "^"))]
                                       "\n\n\n"])
                                    [(expound/expound-str spec
                                                          v
                                                          {:print-specs? false})
                                     (when default
                                       (str "\n\n"
                                            "The default value of `"
                                            default
                                            "` will be applied."))
                                     (when body (str "\n\n" body))]))}))))


(defn unable-to-trace [opts]
  (callout opts
           (point-of-interest
            (merge opts
                   {:type
                    :warning

                    :form           
                    (list '?trace  (:form opts))

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


;; TODO - maybe move this to bling?
;; Would have to pass in the regex part
;; version with stack trace
(defn caught-exception
  ([opts]
   (caught-exception nil opts))
  ([err {:keys [k v form body]
         :as   opts}]
   (callout (merge opts
                   {:type        :error
                    :label       #?(:cljs
                                    "ERROR: (Caught)"
                                    :clj
                                    (str "ERROR: "
                                         (string/replace (type err) #"^class " "" )
                                         " (Caught)"))
                    :padding-top 1})
            (point-of-interest
             (merge opts
                    {:type
                     :error

                     :form
                     (or form (symbol (str k " " v)))

                     :body           
                     (if err
                       #?(:cljs
                          ;; TODO - Add stacktrace preview in bling for cljs?
                          body
                          :clj
                          (let [
                                ;;  Leave this stack trace stuff out but maybe put it back in with
                                ;;  a config option for showing full-stacktrace of fireworks errors
                                ;;  maybe call it :fireworks-dev/full-stacktrace? or something.
                                ;; strace (some->> (when #(instance? Exception err)
                                ;;                   err)
                                ;;                 .getStackTrace
                                ;;                 (map str))
                                ]
                            (bling
                             [:italic.subtle.bold "Message from Clojure:"]
                             "\n"
                             (string/replace (.getMessage err) #"\(" "\n(")
                             "\n\n"
                             (stack-trace-preview
                              {:error err
                               :regex #"^fireworks\.|^lasertag\."
                               :depth 12})
                             (some->> body (str "\n\n"))
                             
                            ;;  Leave this stack trace stuff out but maybe put it back in with
                            ;;  a config option for showing full-stacktrace of fireworks errors
                            ;;  maybe call it :fireworks-dev/full-stacktrace? or something.
                            ;;  
                            ;;  (when strace "\n\n")
                            ;;  (when strace [:italic.subtle.bold "Full stacktrace:"])
                            ;;  (when strace "\n")
                            ;;  (with-out-str (some-> strace pprint))
                             )))
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
