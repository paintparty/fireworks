(ns fireworks.messaging
  (:require [clojure.string :as string]
            [clojure.main]
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
        "╚══════════════════════════════════════════════"

        header-stripe-start
        "╔═ "

        header-stripe
        (string/join (repeat (- (count footer-stripe)
                                (count header-str)
                                (count header-stripe-start)
                                1)
                             "═"))]
    (str "\n"
         open-tag
         header-stripe-start
         header-str
         " " header-stripe
         "\n"
         defs/sgr-tag-close
         "\n"
         (str " " (string/join "\n " (string/split body #"\n")))
         open-tag
         "\n\n"
         footer-stripe
         defs/sgr-tag-close
         "\n")))


#?(:clj
   (do 
     (defn- bad-options-to-stack-trace-preview-warning [error]
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
                            (bold (str (type error))))}))


     (defn- last-index-of-relevant-stack-trace
       "If regex is legit, get a list of indexes that match the regex passed in
        by user. Regex will match on ns or filename where user's program lives.
        Then get the last index of a match (within the mini-strace). If regex is
        not legit, use the depth."
       [regex depth mini-strace]
       (if (= java.util.regex.Pattern (type regex))
         ;; TODO - perf - use transduction here
         (some->> mini-strace
                  (keep-indexed
                   (fn [i [f]]
                     (when (re-find regex (str f))
                       i)))
                  seq
                  (take depth)
                  last)
         (dec depth)))
     
     (defn- first-index-of-relevant-stack-trace
       "If regex is legit, get the index of the first frame that features a
        match"
       [regex depth mini-strace]
       (if (= java.util.regex.Pattern (type regex))
         ;; TODO - perf - use transduction here
         (first (keep-indexed
                 (fn [i [f]]
                   (when (re-find regex (str f))
                     i))
                 mini-strace))
         (dec depth)))

     (defn- mini-trace 
       "Get a mini-strace, limited to the number of frames that will be
        displayed based on `depth`."
       [depth strace]
       (->> strace
            (take depth)
            (mapv StackTraceElement->vec)
            (mapv (fn [[fqns wtf _ loc]]
                    [(-> fqns str clojure.main/demunge)
                     wtf
                     loc]))))
     
     (defn- clean-up-frames [trace first-index-of-relevant-stack-trace]
       (map-indexed
        (fn [i x]
          (if (vector? x)
            (let [[fqn wtf loc]   x
                  [fn-ns fn-name] (-> fqn
                                      str
                                      clojure.main/demunge
                                      (string/split #"/"))]
              (string/join
               " "
               (remove
                nil?
                [#_(when (-> x meta :target?)
                   (str defs/red-tag-open ">>>" defs/sgr-tag-close))
                 (str defs/gray-tag-open
                      fn-ns 
                      (when fn-name "/")
                      defs/sgr-tag-close
                      fn-name)
                 (when-not (contains? '#{invoke invokeStatic} wtf) wtf)
                 (str defs/gray-tag-open
                      loc
                      defs/sgr-tag-close)
                 (when (-> x meta :target?)
                   (str defs/red-tag-open "<- Probably here" defs/sgr-tag-close))])))
            x))
        trace))))

(defn summary-header-blue-italic [s]
  (str defs/blue-tag-open
       (italic (str s "\n"))
       defs/sgr-tag-close))

(defn summary-header-bold-gray-italic [s]
  (str defs/bold-italic-gray-tag-open
       (italic (str s "\n"))
       defs/sgr-tag-close))

(defn summary-header [s]
  (str defs/italic-gray-tag-open
       (italic (str s "\n"))
       defs/sgr-tag-close))

(defn stack-trace-preview
  "Creates a user-friendly stack-trace preview, limited to the frames which
   contain a match with the supplied regex, up to the `depth` value, if
   supplied. `depth` defaults to 7."
  [{:keys [error regex depth header]}]
  #?(:clj
     (let [strace 
           (some->> (maybe error #(instance? Exception %)) .getStackTrace seq)

           formatted-string
           (if-not strace

             ;; Str for warning block, if bad options to stack-track-preview are
             (bad-options-to-stack-trace-preview-warning error)

             ;; Create a formatted string of of the stack trace, clean it up and
             ;; make it easier to read
             (let [strace-len   (count strace)
                   depth        (or (maybe depth pos-int?) 7)
                   mini-strace  (mini-trace depth strace)
                   last-index   (last-index-of-relevant-stack-trace regex
                                                                    depth 
                                                                    mini-strace)
                   first-index  (first-index-of-relevant-stack-trace regex
                                                                     depth 
                                                                     mini-strace)
                   ;; Get all the frames up to the last index
                   trace*       (when last-index
                                  (->> mini-strace
                                       (take (inc last-index))
                                       (into [])))
                   trace*       (if first-index
                                  (assoc trace*
                                         first-index
                                         (with-meta (nth trace* first-index)
                                           {:target? true}))
                                  trace*)
                   len          (when trace* (count trace*))
                   with-header  [(or header
                                     (summary-header "Stacktrace preview:"))]
                   trace        (some->> trace*
                                         (interpose "\n") 
                                         (into with-header))
                   num-dropped  (when trace
                                  (let [n (- (or strace-len 0) (or len 0))]
                                    (some->> (maybe n pos-int?)
                                             (str "\n...+"))))
                   trace        (some-> trace (conj num-dropped))
                   trace2       (clean-up-frames trace first-index)]
               (apply str trace2)))]
       {:formatted-string formatted-string
        :stack-trace-seq  strace})))


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
  [{:keys [k v form line column file header block-type hint lb]}]
  (let [line+sep (str line " │ ")]
    (str (when hint
           (str (summary-header "Hint:")
                "  "
                hint
                lb))
         
         (when header
           (str (summary-header "Raised by:")
                "  "
                header
                lb))

         (when (and line column file)
           (str (str (summary-header "Source:") "  " file ":" line ":" column)
                "\n\n"
                (when line 
                  (str "  " defs/gray-tag-open line+sep defs/sgr-tag-close))
                (let [bad-form (with-out-str 
                                 (pprint (or (some-> form (util/shortened 33) symbol)
                                             (symbol (str k " " v)))))
                      bad-form (string/replace bad-form #"\n$" "")]
                  (str bad-form "\n"
                       (when line+sep (string/join "" (repeat (count line+sep) " ")))
                       (case block-type :error defs/gray-tag-open defs/orange-tag-open)
                       "  " (string/join "" (repeat (count bad-form) "^"))
                       defs/sgr-tag-close))))
         lb)))


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


(def hints
  #?(:cljs
     ()
     :clj
     {"Cannot invoke \"Object.getClass()\" because \"x\" is null" 
      {
       [["clojure.lang.Numbers" "ops"]
        ["clojure.lang.Numbers" "lt"]]
       "clojure.core/> received a nil arg. Expected a number."
       
       [[clojure.lang.Numbers 'ops "Numbers.java"]
        [clojure.lang.Numbers 'gt "Numbers.java"]] 
       "clojure.core/< received a nil arg. Expected a number."
      }})
  )

(defn exception-clues
  [err {:keys [body]}]
  (if err
    (let [err-msg-str #?(:cljs
                         nil
                         :bb
                         (str err)
                         :clj
                         (try #_(string/replace (.getMessage err) #"\(" "\n(")
                          (.getMessage err)
                              (catch Throwable e
                                "No message from Clojure was provided.")))]
      #?(:cljs
         ;; TODO - Add stacktrace preview in bling for cljs?
         body
         :clj
         (let [{:keys [formatted-string stack-trace-seq]}
               (stack-trace-preview
                {:error err
                 :regex #"^fireworks\.|^lasertag\."
                 :depth 12})
               
               hint
               (let [hints-by-error-message
                     (->> err-msg-str (get hints))

                     hint-by-st-frames
                     (get hints-by-error-message
                          [[(-> stack-trace-seq first (.getClassName))
                            (-> stack-trace-seq first (.getMethodName))]
                           [(-> stack-trace-seq second (.getClassName))
                            (-> stack-trace-seq second (.getMethodName))]])]
                 hint-by-st-frames)

               message-from-clojure
               (str (summary-header "Message from Clojure:")
                    (some-> err-msg-str indented-string))

               stack-trace-preview-str
               (str (when formatted-string
                      (let [[fl & rl]    (string/split formatted-string #"\n")
                            section-head (str defs/italic-tag-open
                                              fl
                                              defs/sgr-tag-close)]
                        (str section-head
                             "\n"
                             (indented-string (string/join "\n" rl)))))
                    (some->> body (str "\n\n")))]

           {:hint                    hint                    
            :message-from-clojure    message-from-clojure
            :stack-trace-preview-str stack-trace-preview-str})))
    body))



(defn caught-exception
  ([opts]
   (caught-exception nil opts))
  ([err opts]
   (println 
    (block 
     {:header-str "ERROR (Caught)" 
      :block-type :error
      :body       (let [{:keys [hint 
                                message-from-clojure 
                                stack-trace-preview-str]} 
                        (exception-clues err opts)

                        error-type                                                  
                        #?(:clj
                           (some-> err
                                   type
                                   (string/replace #"^class " "")
                                   (->> indented-string
                                        (str (summary-header "Error type:"))))
                           :cljs nil)

                        lb
                        "\n\n\n"]

                    (str (warning-or-exception-summary
                          (assoc opts
                                 :block-type :error
                                 :hint hint
                                 :lb "\n\n\n"))
                         (string/join 
                          lb
                          (remove nil? [message-from-clojure
                                        error-type
                                        stack-trace-preview-str]))))}))))


(def dispatch 
  {:messaging/bad-option-value-warning bad-option-value-warning
   :messaging/read-file-warning        caught-exception
   :messaging/print-error              caught-exception})

;; Race-condition-free version of clojure.core/println,
;; maybe useful if any weird behavior arises
;; (defn safe-println [& more]
;;   (.write *out* (str (clojure.string/join " " more) "\n")))

