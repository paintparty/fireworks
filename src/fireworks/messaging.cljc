(ns fireworks.messaging
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp pprint]]
            [fireworks.defs :as defs]
            [expound.alpha :as expound]
            [fireworks.util :as util :refer [maybe]]))

(defrecord FireworksThrowable [err err-x err-opts])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))


(defn bold [s]
  (str defs/bold-tag-open s defs/sgr-tag-close))

(defn italic [s]
  (str defs/italic-tag-open s defs/sgr-tag-close))


(defn block
  [{:keys [header-str block-type body]}] 
  (let [open-tag 
        (case block-type
          :warning defs/orange-tag-open
          :error defs/red-tag-open
          defs/gray-tag-open)

        footer-stripe
        "───────────────────────────────────────────────"

        header-stripe-start
        "══ "

        header-stripe
        (string/join (repeat (- (count footer-stripe)
                                (count header-str)
                                (count header-stripe-start)
                                1)
                             "═"))]
    (str "\n"
         open-tag
         header-stripe-start
         defs/sgr-tag-close
         defs/bold-tag-open
         #_"  " header-str
         defs/sgr-tag-close
         open-tag
         " " header-stripe
         "\n"
         defs/sgr-tag-close
         "\n"
         body
         open-tag
         "\n"
         footer-stripe
         defs/sgr-tag-close
         "\n")))


(defn stack-trace-preview
  "Creates a user-friendly stack-trace preview, limited to the frames which
   contain a match with the supplied regex, up to the `depth` value, if supplied.
   `depth` defaults to 7."
  [{:keys [error regex depth header]}]
  #?(:clj
     (if-let [strace (some->> (maybe error #(instance? Exception %))
                              .getStackTrace
                              seq)]
       (let [strace-len  (count strace)
             depth       (or (maybe depth pos-int?) 7)

             ;; Get a mini-strace, limited to the number of frames that will be
             ;; displayed based on `depth`
             mini-strace (->> strace
                              (take depth)
                              (mapv StackTraceElement->vec))

             ;; If regex is legit, get a list of indexes that match the regex
             ;; passed in by user. Regex will match on ns or filename where
             ;; user's their program lives. Then get the last index of a match
             ;; (within the mini-strace). If regex is not legit, use the depth.
             last-index  (if (= java.util.regex.Pattern (type regex))
                           ;; TODO - perf - use transduction here
                           (some->> mini-strace
                                    (keep-indexed
                                      (fn [i [f]]
                                        (when (re-find regex (str f))
                                          i)))
                                    seq
                                    (take depth)
                                    last)
                           (dec depth))

             ;; Get all the frames up to the last index
             trace*      (when last-index
                           (->> mini-strace (take (inc last-index))))
             len         (when trace* (count trace*))
             with-header [(or header
                              (italic "Stacktrace preview:"))
                          "\n"]
             trace       (some->> trace* (interpose "\n") (into with-header))
             num-dropped (when trace
                           (let [n (- (or strace-len 0) (or len 0))]
                             (some->> (maybe n pos-int?)
                                      (str "\n...+"))))

             ;; Conj num-dropped annotation to mini-strace
             trace       (some-> trace (conj num-dropped))]

         ;; Create and return multiline string
         (apply str trace))

       ;; Print a warning if option args are bad
       (block {:header-str "WARNING"
                 :block-type :warning
                 :body       (str
                              "fireworks.messaging/stack-trace-preview\n\n"
                              "Value of the "
                              (bold :error)
                              " option should be an instance of "
                              (bold 'java.lang.Exception.) 
                              "\n\n"
                              "Value received:\n"
                              (bold (util/shortened error 44)) 
                              "\n\n"
                              "Type of value received:\n"
                              (bold (str (type error))) 
                              )}))))


(defn fw-debug-report-template
  ([s x]
   (fw-debug-report-template s x :info))
  ([s x k]
   (println (block {:header-str s
                    :block-type :info 
                    :body       (with-out-str (pprint x))}))))


(defn unable-to-print-warning
  [s x]
  (println (block {:header-str s
                   :block-type :warning 
                   :body       x})))


(defn warning-or-exception-summary 
  [{:keys [k v form line column file header block-type]}]
  (let [line+sep
        (str line " │ ")]

    (str (when header
           (str (italic "raised by:\n") "  "  header "\n\n\n"))

         (when (and line column file)
           (str (italic "source:\n") 
                "  " file ":" line ":" column "\n\n\n" ))

         (when line 
           (str defs/gray-tag-open line+sep defs/sgr-tag-close))

         defs/bold-tag-open
         (let [bad-form (with-out-str 
                          (pprint (or (some-> form (util/shortened 33) symbol)
                                      (symbol (str k " " v)))))
               bad-form (string/replace bad-form #"\n$" "")]
           (str bad-form "\n"
                (when line+sep (string/join "" (repeat (count line+sep) " ")))
                (case block-type :error defs/red-tag-open defs/orange-tag-open)
                (string/join "" (repeat (count bad-form) "^"))
                defs/sgr-tag-close))
         defs/sgr-tag-close
         "\n\n")))


(defn warning-details 
  [{:keys [k v spec default body form]}]
  (apply str 
         (concat
          (when (and form k v)
            (let [indent "  "]
              ["\n"
               "Bad value:\n\n"
               indent
               (bold k)
               " "
               (bold v)
               (str "\n"
                    (string/join (repeat (count (str k)) " "))
                    " ")
               indent
               defs/orange-tag-open
               (string/join (repeat (count (str v)) "^"))
               defs/sgr-tag-close
               "\n\n\n"]))
          [(expound/expound-str spec v {:print-specs? false})
           (when default
             (str "\n\n"
                  "The default value of `"
                  default
                  "` will be applied."))
           (when body (str "\n\n" body))])))


(defn bad-option-value-warning
  [opts]
  (println 
   (block {:header-str "WARNING: Invalid value"
           :block-type :warning
           :body       (str
                        (warning-or-exception-summary opts)
                        "\n"
                        (warning-details opts))})))


(defn unable-to-trace [opts]
  (let [opts
        (merge opts 
               {:header-str "Unable to trace form"
                :block-type :warning
                :form       (list '?trace (:form opts))
                :body       (str
                             (warning-or-exception-summary opts)    
                             "\n"
                             (str "fireworks.core/?trace will trace forms beginning with:\n"
                                      "  "
                                      (string/join "\n  "
                                                   ['->
                                                    'some->
                                                    '->>
                                                    'some->>])))})]
    (println (block opts))))

(defn indented-string [s]
  (when s
    (string/join "\n"
                 (map #(str "  "  %) 
                      (string/split s
                                    #"\n")))))
(defn exception-info-from-clojure
  [err {:keys [body]}]
  
   (if err
     (let [err-msg-str #?(:cljs
                          nil
                          :bb
                          (str err)
                          :clj
                          (try (string/replace (.getMessage err) #"\(" "\n(")
                               (catch Throwable e
                                 "No message from Clojure was provided.")))]
       #?(:cljs
         ;; TODO - Add stacktrace preview in bling for cljs?
          body
          :clj
          (str defs/italic-tag-open
               "Message from Clojure:"
               defs/sgr-tag-close
               "\n"
               (some-> err-msg-str indented-string)
               "\n\n"
               (when-let [stp          (stack-trace-preview
                                        {:error err
                                         :regex #"^fireworks\.|^lasertag\."
                                         :depth 12})]
                 (let [[fl & rl]    (string/split stp #"\n")
                       section-head (str defs/italic-tag-open
                                         fl
                                         defs/sgr-tag-close)]
                   (str section-head
                        "\n"
                        (indented-string (string/join "\n" rl)))))
               (some->> body (str "\n\n"))))) 
     body))



(defn caught-exception
  ([opts]
   (caught-exception nil opts))
  ([err opts]
   (println 
    (block 
     {:header-str (str "ERROR (Caught): "
                       #?(:clj
                          (string/replace (type err) #"^class " "")
                          :cljs nil))
      :block-type :error
      :body       (str (warning-or-exception-summary (assoc opts :block-type :error))
                       (exception-info-from-clojure err opts)
                       )}))))


(def dispatch 
  {:messaging/bad-option-value-warning bad-option-value-warning
   :messaging/read-file-warning        caught-exception
   :messaging/print-error              caught-exception})

;; Race-condition-free version of clojure.core/println,
;; maybe useful if any weird behavior arises
;; (defn safe-println [& more]
;;   (.write *out* (str (clojure.string/join " " more) "\n")))

