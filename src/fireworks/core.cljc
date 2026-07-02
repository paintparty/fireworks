(ns fireworks.core
  (:require
   [clojure.set :as set]
   [clojure.data :as data]
   [clojure.spec.alpha :as s]
   [fireworks.browser]
   [fireworks.fs]
   [fireworks.messaging :as messaging]
   [fireworks.serialize :as serialize]
   [fireworks.specs.config :as specs.config]
   #?(:clj [fireworks.state :as state]
      :cljs [fireworks.state :as state :refer [node?]])
   [fireworks.tag :as tag]
   #?(:cljs [fireworks.macros
             :refer-macros
             [keyed
              compile-time-warnings-and-errors]])
   #?(:clj [fireworks.macros :refer [keyed get-user-config-edn-dynamic]])
   #?(:clj [clojure.java.io :as io])
   [clojure.string :as string]
   [fireworks.config :as config]
   [fireworks.util :as util] 
   [lasertag.core :as lasertag]
   [fireworks.defs :as defs]
   [clojure.walk :as walk]
   )
  #?(:cljs (:require-macros 
            [fireworks.core :refer [? !? ?> !?>]])))

(declare pprint)

(def core-defs 
  (set '(def defn defrecord defstruct defprotocol defmulti deftype defmethod)))


;; Helpers for :perf feature ---------------------------------------------------

;; timer: milliseconds, cross-platform
(def ^:private now-ms
  #?(:clj  (fn [] (/ (double (System/nanoTime)) 1e6))   ; JVM + Babashka
     :cljs (fn [] (system-time))))                      ; → performance.now()

;; formatting
(defn- fmt-dur [ms]
  (let [nanos (double (* ms 1e6))]
    (cond
      (>= nanos 1e9) (str (Math/round (/ nanos 1e9)) "s")
      (>= nanos 1e6) (str (Math/round (/ nanos 1e6)) "ms")
      (>= nanos 1e3) (str (Math/round (/ nanos 1e3)) "µs")
      :else          (str (Math/round nanos) "ns"))))

;; the bench
(def ^:private ^:const budget-ms 1.0)    ; cheap ops self-scale up to this much work
(def ^:private ^:const cap-ms  100.0)    ; one call over this → :too-slow

(def ^:private sink (volatile! nil))     ; defeats dead-code elimination

(defn quick-mean
  "Mean ms/call of thunk f. Cheap ops self-scale their iteration count up to
   ~budget-ms of total work; a single call exceeding cap-ms returns :too-slow.
   Rough by design."
  [f]
  (loop [k 1]
    (let [t0 (now-ms)
          _  (dotimes [_ k] (vreset! sink (f)))   ; sink result so JIT can't elide
          dt (- (now-ms) t0)]
      (cond
        (>= dt cap-ms) :too-slow
        (< dt 0.25)    (if (>= k (* 1024 1024)) (/ dt k) (recur (* k 8)))
        :else
        (let [per  (/ dt k)
              want (long (/ budget-ms per))]
          (if (<= want k)
            per                                    ; pilot already did enough
            (let [t1 (now-ms)]
              (dotimes [_ want] (vreset! sink (f)))
              (/ (- (now-ms) t1) want))))))))

;; label for the ? tap header
(defn perf-label
  "String for the :perf line, e.g. \"2ns\", \"840µs\", or \">100ms\"."
  [f]
  (let [m (quick-mean f)]
    (if (= m :too-slow)
      (str ">" (fmt-dur cap-ms))                  ; sentinel tracks the configured cap
      (fmt-dur m))))


;   FFFFFFFFFFFFFFFFFFFFFF
;   F::::::::::::::::::::F
;   F::::::::::::::::::::F
;   FF::::::FFFFFFFFF::::F
;     F:::::F       FFFFFF
;     F:::::F             
;     F::::::FFFFFFFFFF   
;     F:::::::::::::::F   
;     F:::::::::::::::F   
;     F::::::FFFFFFFFFF   
;     F:::::F             
;     F:::::F             
;   FF:::::::FF           
;   F::::::::FF           
;   F::::::::FF           
;   FFFFFFFFFFF           


(defn resolve-label-length [label-max-length]
  (or (when (s/valid? ::specs.config/label-max-length
                      label-max-length)
        label-max-length)
      (:label-max-length @state/config)
      (-> config/options
          :label-max-length
          :default)))

(defn- label-entity-tag* [opts]
  (case (some-> opts :label-color util/as-str)
    "green"
    :eval-label-green
    "blue"
    :eval-label-blue
    "red"
    :eval-label-red
    :eval-label))

(defn- deref->at-syntax [qf]
  (if (and (list? qf)
           (= 2 (count qf))
           (contains?
            #{'cljs.core/deref 'clojure.core/deref}
            (first qf)))
    (symbol (str "@" (second qf)))
    qf))

(defn- user-label-or-form!
  [{:keys [qf template label mll?]
   {:keys [label-max-length]} :user-opts
    :as opts}]
  (let [indent-spaces
        (or (some-> @state/margin-inline-start
                    util/spaces)
            nil)

        label
        (when (contains?
               #{
                 [:file-info :form-or-label :result]
                 [:form-or-label :result]
                 #_[:form-or-label :file-info]}
               template)
          (when label
            (let [label-entity-tag (label-entity-tag* opts)

                  label            (if mll?
                                     (string/join
                                      "\n"
                                      (map
                                       #(str (tag/tag-entity
                                              (str indent-spaces %)
                                              label-entity-tag)
                                             (tag/reset-tag))
                                       (string/split label #"\n")))
                                     
                                     (tag/tag-entity
                                      (util/shortened label
                                                      (resolve-label-length label-max-length))
                                      label-entity-tag))]
              (str indent-spaces label))))

        form
        (when-not label
          (when qf
            ;; TODO - Confirm that toggling this state doesn't matter, remove it
            (reset! state/formatting-form-to-be-evaled? true)
            (let [form-entity-tag (label-entity-tag* opts)
                  format-anons    #(string/replace %
                                                   #"p([0-9])__[0-9]+\#" 
                                                   (fn [[_ n]] (str "%" n)))
                  qf              (deref->at-syntax qf)
                  shortened       (if (:format-label-as-code? @state/config)
                                    (-> qf
                                        (pprint {:max-width 33})
                                        with-out-str
                                        (string/replace #"\n+$" "")
                                        format-anons)
                                    (util/shortened (-> qf str format-anons)
                                                    (resolve-label-length
                                                     label-max-length)))
                  tagged          (tag/tag-entity shortened form-entity-tag) 
                  ret             tagged]
              ;; TODO - Confirm that toggling this state doesn't matter, remove it
              (reset! state/formatting-form-to-be-evaled? false)
              (str indent-spaces ret))))]
    [label form]))


(defn- file-info
  [{:keys [form-meta
           template
           ns-str]}]
  (let [file-info* (when (contains?
                          #{[:form-or-label :file-info :result]
                            [:file-info :form-or-label :result]
                            [:file-info :result]
                            [:form-or-label :file-info]}
                          template)
                     (when-let [{ln  :line
                                 col :column} form-meta]
                       (str ns-str ":" ln ":" col)))
        file-info  (some-> file-info*
                           (tag/tag-entity :file-info))]
   [file-info* file-info]))


(defn- margin-block-str
  [{:keys [template user-opts mode]}
   k]
  (cond
    (and (= template [:result])
         (not (pos-int? (k user-opts))))
    ""
    ;; Require margin on the top when using native printing  
    (contains? #{:log :pp :js} mode)
    "\n"

    :else
    (string/join (repeat (get @state/config k 0) "\n"))))



(defn- formatted
  "Formatted log with file-info, form/comment, fat-arrow and syntax-colored
   pretty-printed result. Used by fireworks.core/? macro."
  [source
   {:keys [qf
           log?
           label
           ns-str
           p-data?
           template
           form-meta
           user-opts
           truncate?
           threading?]
    :as   opts}] 
  ;; Test error messaging
  (when (= :_fireworks-dev/force-error_ qf)
    #?(:cljs
       (throw (js/Error. "Forced error from fireworks.core/formatted"))
       :clj
       (+ 1 true)))

  (let [user-print-fn
        (:print-with user-opts)

        label*
        label

        label
        (if (coll? label)
          (with-out-str (pprint label))
          label)

        mll?
        (when (string? label) (re-find #"\n" label))

        #_ml-qf?
        #_(when (string? qf) (re-find #"\n" qf))

        file-info-first?
        (or 
         ;; NEW - just go first line if :file info in there at all
         (contains? (into #{} template) :file-info))

        just-result?
        (and log? (= template [:result]))

        label?
        (not (or just-result? (= template [:file-info :result])))

        {:keys [form
                label
                file-info
                file-info*]}
        (when-not just-result?
          (let [[label form]           
                (when label? (user-label-or-form! 
                              (merge opts
                                     (keyed [
                                             ;;  file-info-first?
                                             mll? 
                                             label
                                             label?]))))

                [file-info* file-info] 
                (file-info opts)]
            (keyed [label
                    form
                    file-info*
                    file-info])))

        result-header
        (when-not (or (contains? #{[:result] [:form-or-label :file-info]}
                                 template)
                      log?
                      threading?)
          ;; TODO - is the space before the newline necessary?
          (tag/tag-entity " \n" :result-header))

        fmt           
        (if (:lasertag.core/unknown-coll-size opts)
          (with-out-str (pprint source))
          (when-not (or log?
                        threading?
                        (= template [:form-or-label :file-info]))
            (if (fn? user-print-fn)
              (with-out-str (user-print-fn source))

              ;; This is where you feed the source to the formatting engine
              (let [user-opts (merge (or (some-> opts :user-opts)
                                         {})
                                     (when (false? truncate?)
                                       {:truncate? false}))]
                (serialize/formatted* source user-opts)))))

        label-or-form
        (or label form)

        perf
        (some-> opts :perf (tag/tag-entity :eval-label-green))

        fmt+          
        (when-not just-result?
          (if file-info-first?
            (str 
             file-info
             (when perf "\n")
             perf
             (when label-or-form "\n")
             label-or-form
             result-header
             fmt)
            (str label-or-form
                 (when label-or-form
                   (when-not (re-find #"\n" label-or-form) "  "))
                 (when (and mll? file-info)
                   "\n")
                 file-info
                 result-header
                 fmt)))
        ]
    
    ;; TODO Change this to (= mode :data)
    ;; TODO - Change if post-replace works - cljs stuff below
    (if  p-data?
      ;; If p-data, return a map of preformatted values
      (merge
       {:ns-str              ns-str
        :user-supplied-label label*
        :display-label       (or label form)
        :template            template
        :quoted-form         qf
        :file-info-str       file-info*
        :formatted+          {:string fmt+}
        :formatted           {:string fmt}
        :threading?          threading?
        :truncate?           truncate?}
       form-meta)

      ;; Else if print-and-return fns, return printing opts
      (merge 
       {:ns-str ns-str} ;; <-new
       
       {:fmt           fmt+
        :file-info-str file-info*
        :log?          log?

        ;; Defaults to {:margin-bottom 1 :margin-top 0}
        ;; If :result flag is used it will be {:margin-bottom 0 :margin-top 0}
        ;; If :result flag is used w call-site opts for :margin-*, those will win
        :margin-bottom (margin-block-str opts :margin-bottom)
        :margin-top    (margin-block-str opts :margin-top)
        :template      template}))))


#?(:cljs 
   (defn- js-print [opts]
     (let [js-arr 
           (-> opts
               :fmt
               fireworks.browser/ansi-sgr-string->browser-dev-console-array)

           template-count
           (-> opts :template count)

           index-to-insert-margin-block-end
           (case template-count 3 5 2 3 nil)]
       
       ;; When printing to a browser dev console, use margin-block-end at the
       ;; right index, to create a 1/2 line of space between the header (aka the
       ;; label / file-info), but only if there is a header.
       (when-not (:log? opts)
         (when-let [i index-to-insert-margin-block-end]
           (aset js-arr i (str (aget js-arr i) "; margin-block-end: 0.5em"))))
       (.apply (.-log  js/console)
               js/console
               js-arr))))



;        SSSSSSSSSSSSSSS TTTTTTTTTTTTTTTTTTTTTTT
;      SS:::::::::::::::ST:::::::::::::::::::::T
;     S:::::SSSSSS::::::ST:::::::::::::::::::::T
;     S:::::S     SSSSSSST:::::TT:::::::TT:::::T
;     S:::::S            TTTTTT  T:::::T  TTTTTT
;     S:::::S                    T:::::T        
;      S::::SSSS                 T:::::T        
;       SS::::::SSSSS            T:::::T        
;         SSS::::::::SS          T:::::T        
;            SSSSSS::::S         T:::::T        
;                 S:::::S        T:::::T  
;                 S:::::S        T:::::T  
;     SSSSSSS     S:::::S      TT:::::::TT
;     S::::::SSSSSS:::::S      T:::::::::T
;     S:::::::::::::::SS       T:::::::::T
;      SSSSSSSSSSSSSSS         TTTTTTTTTTT

 
(defn- reset-user-opt!
  "Validates user option override from call site and updates config."
  [k opts]
  (let [new-val (or (k opts)
                    (k (:user-opts opts)))
        valid?  (some-> k
                        config/options
                        :spec
                        (s/valid? new-val))]
    (if valid?
      (swap! state/config assoc k new-val)
      (messaging/bad-option-value-warning
       (let [m                     (k config/options)
             {:keys [line column]} (:form-meta opts)]
         (merge m 
                (keyed [k line column])
                {:header (:fw-fnsym opts)
                 :k      k
                 :v      new-val
                 :form   (:quoted-fw-form opts) 
                 :file   (:ns-str opts)}))))))


(defn- opt-to-reset [opts k]
  (let [a      (k @state/config)
        b      (k opts)
        diff?  (not= a b)
        reset? (and diff? (not (nil? b)))] 
    (when reset? k)))


(defn- opts-to-reset
  "Get a set of overrides the user has passed at call site.

   Merge these on top of the value of @state/config-overrides, which are 
   config options the user may have set globally, at runtime, in their program
   via fireworks.core/config!.

   Opts will be reset only if they are different from current value."
  [user-opts]
  (some->> user-opts
           seq
           keys
           (into #{})
           (set/intersection config/option-keys)
           (keep (partial opt-to-reset user-opts))
           (into #{})))


(defn- some-option-keys-that-update-theme [x]
  (some-> x
          (set/intersection
           config/option-keys-that-update-theme)
          seq))


(defn- diff-from-merged-user-config
  [config-before]
  (let [[a b]           (data/diff config-before @state/config)
        keys-in-a-and-b (into #{} (concat (keys a) (keys b)))]
    (some-option-keys-that-update-theme
     keys-in-a-and-b)))


(defn- diff-call-site-theme-option-keys
  [opts-to-reset]
  (when (seq opts-to-reset) 
    (some-option-keys-that-update-theme
     opts-to-reset)))


(defn- reset-config+theme!
  [config-before user-opts opts]
  (let [ks               (opts-to-reset user-opts)
        opts-to-reset    (doseq [k ks] (reset-user-opt! k opts))]
    (when (or (diff-call-site-theme-option-keys opts-to-reset)
              (diff-from-merged-user-config config-before))
      (let [{:keys [with-style-maps
                    with-serialized-style-maps]} 
            (state/merged-theme* :reset)]
        (reset! state/merged-theme
                with-serialized-style-maps)
        (reset! state/merged-theme-with-unserialized-style-maps
                with-style-maps)))))


(defn- next-row
  [prev cur other-seq]
  (reduce
    (fn [row [diag above other]]
      (let [update-val (if (= other cur)
                          diag
                          (inc (min diag above (peek row))))]
        (conj row update-val)))
    [(inc (first prev))]
    (map vector prev (next prev) other-seq)))


(defn distance
  "Given two words, computes levenshtein distance."
  [seq1 seq2]
  (cond
    (and (empty? seq1) (empty? seq2)) 0
    (empty? seq1) (count seq2)
    (empty? seq2) (count seq1)
    :else (peek
            (reduce (fn [prev cur] (next-row prev cur seq2))
                    (map #(identity %2) (cons nil seq2) (range))
                    seq1))))


(defn- spelling-distances [k]
  (reduce (fn [acc opt] 
            (let [n (distance (name k)
                              (name opt))]
              (update acc n conj opt)))
          {}
          config/option-keys))


(defn- unknown-option-warning-opts [opts k]
  (let [{:keys [line column]} (:form-meta opts)
        distances             (spelling-distances k)
        lt-5?->               #(when (<  % 5) %)
        misspellings          (some->> distances
                                       keys
                                       (apply min)
                                       lt-5?->
                                       (get distances)
                                       (mapv str)
                                       (string/join "\n  "))]
    (merge (keyed [k line column])
           {:header     (:fw-fnsym opts)
            :hint       misspellings
            :hint-label "Maybe you meant:"
            :k          k
            :form       (:quoted-fw-form opts) 
            :file       (:ns-str opts)})))


(defn- reset-state!
  [{find-vals :find
    user-opts :user-opts
    :as       opts}]

  ;; Surfacing of compile-time warnings and errors to cljs
  ;; These will primarily have to do with fireworks.state/user-config,
  ;; which calls a macro that gets the user-config from a .edn file.
  #?(:cljs
     (doseq [x (compile-time-warnings-and-errors)]
       (when-let [k (when (vector? x) (nth x 0 nil))]
         (let [f (get messaging/dispatch k nil)]
           (when (fn? f)
             (when-let [opts (second x)]
               (f opts)))))))
  

  (let [config-before @state/config]

    ;; TODO - add some observability here
    (reset! state/let-bindings? (:let-bindings? opts))
    (reset! state/margin-inline-start (or (:margin-inline-start opts)
                                          (:margin-left opts)))

    ;; TODO - lose if post-replace works
    (reset! state/styles [])
    (reset! state/*formatting-meta-level 0)
    (reset! state/rainbow-level 0)
    (reset! state/top-level-value-is-sev? false)
    (reset! messaging/warnings-and-errors [])

    ;; Resetting config to user's config.edn merged with defaults
    (when state/debug-config?
      (messaging/fw-debug-report-template
       "Resetting fireworks.state/config atom to"
       (state/merged-config)
       :magenta))

    ;; Reset config & potentially reset/remerge the theme
    (reset-config+theme! config-before user-opts opts)

    ;; Maybe print detected color
    (when (:print-detected-color-level? @state/config)
      (println (str "\n"
                    "fireworks.state/detected-color-level => "
                    fireworks.state/detected-color-level
                    "\n")))

    ;; Warn user if a non-existant config option is passed
    (doseq [k (some-> opts keys seq )]
      (when-not (contains? config/option-keys k)
        (when-not (contains? config/undocumented-option-keys k)
          (messaging/unknown-option-warning
           (unknown-option-warning-opts opts k))))))
  
  ;; Reset the highlight state.
  ;; It may pull highlight style from merged theme.
  (reset! state/highlight (some->> find-vals state/highlight-style))
  (reset! state/highlight-target-path nil)

  #_(reset! state/rewind-counter 0))
          


;  PPPPPPPPPPPPPPPPP
;  P::::::::::::::::P
;  P::::::PPPPPP:::::P
;  PP:::::P     P:::::P
;    P::::P     P:::::P
;    P::::P     P:::::P
;    P::::PPPPPP:::::P
;    P:::::::::::::PP
;    P::::PPPPPPPPP
;    P::::P
;    P::::P
;    P::::P
;  PP::::::PP
;  P::::::::P
;  P::::::::P
;  PPPPPPPPPP

(defn- maybe-unable-to-print-warning [x]
  (let [{:keys [coll-size classname]} (-> x lasertag/tag-map)
        cause (if (= coll-size :lasertag.core/unknown-coll-size)
                "Cannot determine collection size"
                "Unknown")]
    (messaging/unable-to-print-warning
     "fireworks.core/?"
     (str (messaging/italic "  Problem:\n")
          "    " 
          "Unable to print value."
          "\n\n"
          (messaging/italic "  Cause:\n")
          "    "
          cause
          "\n\n"
          (messaging/italic "  Value class:\n")
          "    "
          classname))))

(defn- print-formatted
  ([x]
   (print-formatted x nil))
  ([{:keys [fmt log? err err-x err-opts]
     :as   x}
    js-printing-fn]
   (if (instance? fireworks.messaging.FireworksThrowable x)
     (if (= (:coll-size err-opts) :lasertag.core/unknown-coll-size)
       (maybe-unable-to-print-warning err-x)
       (let [{:keys [line column file]} (:form-meta err-opts)
             ns-str                     (:ns-str err-opts)] 

         (messaging/caught-exception 
          err
          {:value  err-x
           :type   :error
           :form   (:quoted-fw-form err-opts)
           :header (or (some-> err-opts :header)
                       (some-> err-opts :fw-fnsym))
           :line   line
           :column column
           :file   (or file ns-str)
           :regex  #"^fireworks\.|^lasertag\."})
         (println 
          (str "\nFalling back to pprint...\n\n"
               (with-out-str (pprint err-x))))))

     (let [termf #(do 
                    ;; If it has been formatted by fireworks, it will print here.
                    (print (:margin-top %))
                    (some-> fmt print)
                    ;; Extra line based on :margin-bottom
                    ;; TODO - not print if fmt is nil or blank string?
                    ((if log? print println) (:margin-bottom x)))  ]

       #?(:cljs (if node? (termf x) (js-printing-fn x))
          :clj (termf x))))))

(defn- try-pp 
  ([x]
   (try-pp x nil))
  ([x opts]
   (try (pprint x opts)
        #?(:clj
           ;; TODO - assess whether you need this and change messaging
           (catch Throwable e
             (maybe-unable-to-print-warning x))))))

(defn- _pp* [user-opts x]
  #_(println "_pp*" user-opts)
  (print (margin-block-str user-opts :margin-top))
  (try-pp x user-opts) 
  ;; Extra line after pprint result
  (print (margin-block-str user-opts :margin-bottom))
  x)

(defn ^{:public true}
  _pp
  "Called internally."
  [{:keys [user-opts]} x]
  #?(:cljs
     (if node? 
       (_pp* user-opts x)
       (do (try-pp x) x))
     :clj
     (_pp* user-opts x)))

(defn ^{:public true} _log 
  [{:keys [user-opts]} x]
  #?(:cljs
     (if node? 
       (_pp* user-opts x)
       (do (js/console.log x) x))
     :clj (_pp* user-opts x)))


(defn- fw-debug-report [config-before opts fname]
  (let [reports 
        [
         #_["Options from user config.edn file"
          state/user-config-edn
          :magenta]

         #_["fireworks.state/config, before reset"
          config-before
          :magenta]
         
         #_["Options from user config.edn that will override defaults"
          (into {}
                (keep (fn [[k v]]
                        (when (and (not= v (get config-before k nil))
                                   (not= :path-to-user-config k))
                          [k v]))
                      state/user-config-edn))
          :magenta]

        ;;  ["fireworks.state/config, after reset"
        ;;   @state/config]
         
         #_["Options that were overidden by user options"
          (into {}
                (keep (fn [[k v]]
                        (when (and (not= v (get config-before k nil))
                                   (not= :path-to-user-config k))
                          [k v]))
                       @state/config))]

        ;;  ["fireworks.state/config, diff / user-supplied overrides"
        ;;   (nth (data/diff config-before @state/config) 1 nil)]

        ;;  [(str fname ", opts")
        ;;   opts ]

        ;;  ["Selected fireworks.state atom values"
        ;;   {:state/margin-inline-start     state/margin-inline-start
        ;;    :state/top-level-value-is-sev? state/top-level-value-is-sev?
        ;;    :state/highlighting            state/highlight}]
         ]]

    (doseq [[label v k] reports]
      (messaging/fw-debug-report-template label v k))))

;; TODO does this get surfaced in cljs-browser?
;; Use generic callout here?

(defn- fw-config-report []
  (println
   (messaging/block 
    {:header-str "fireworks.state/config "
     :block-type :info
     :body       (str "Result of merging options from user's"
                      "\n"
                      "config.edn file with defaults, and then"
                      "\n"
                      "merging optional call-site overrides:"
                      "\n"
                      "\n"
                      (with-out-str (pprint @state/config)))})))

(defn- native-logging*
  [x opts]
  #?(:cljs 
     (let [{:keys [coll-type? carries-meta? t transient? classname all-tags]
            :as   m} 
           (util/tag-map* x
                          {:include-function-info?           false
                           :include-js-built-in-object-info? false})]
       (when (or (and coll-type? 
                      (not (record? x))
                      (or (contains? all-tags :js)
                          (not (string/starts-with? classname "cljs.core"))))

                 ;; Specific to HTML DOM Collections, maybe swap this out for
                 ;; something else, after adding support to lasertag
                 (when (= t :iterable) 
                   (contains? defs/html-collection-types classname)))
         {:log? true}))

     :clj
     nil))


(defn- fw-throwable [e x opts]
  (messaging/->FireworksThrowable
   e
   x
   (let [{:keys [coll-size all-tags]} (-> x lasertag/tag-map)]
     (assoc opts
            :header
            "fireworks.core/formatted"
            :coll-size 
            coll-size
            :all-tags 
            all-tags))))


(defn ^{:public true}
  tagged-string-data
  [printing-opts k]
  (let [string                        
        (->> printing-opts k :string)

        [string-with-format-specifier-tags & css-styles] 
        (->> string
             fireworks.browser/ansi-sgr-string->browser-dev-console-array
             vec)]

    (keyed [string
            string-with-format-specifier-tags 
            css-styles])))


;; TODO - analyze perf of the tagged-string functions
(defn ^{:public true}
  as-data
  [printing-opts]
  (assoc (dissoc printing-opts :formatted :formatted+)
         :formatted
         (tagged-string-data printing-opts :formatted)
         :formatted-with-header
         (tagged-string-data printing-opts :formatted+)
         ))


(defn- safe-str*
  "Calls util/safe-str with options specific to Fireworks Live Code inline
   results."
  [x]
  (util/safe-str x {:start        0
                    :end          1000 ;; <- max string length
                    :print-length (:print-length-inline-results @state/config)
                    :print-level  (:print-level-inline-results @state/config)
                    :ellipsis?    true}))


(def hidden-dir
  "This is the name of the hidden dir in the root of the user's project folder.
   It must be present in order to utilize tooling or extensions which implement
   the Fireworks Live Code pattern."
  ".fireworks")


(def results-subdir 
  "The subdir within the hidden dir, where the truncated results of each eval
   are stored."
  "results")


(defn write-to-store 
  [x
   {:keys [line column ns-str perf] 
    :as   m}]

  ;; (? results-subdir)
  ;; (? ns-str) 
  ;; (? (str line "_" column))


  (when (fireworks.fs/path-exists? hidden-dir)
    (let [fpath
          (str hidden-dir "/" results-subdir "/" (some-> ns-str (str "/" )) line "_" column)
          #_(fireworks.fs/join-path (? hidden-dir)
                                    (? results-subdir)
                                    (? ns-str) 
                                    (? (str line "_" column)))]
      (fireworks.fs/ensure-dir! fpath)
      (fireworks.fs/write-file! fpath 
                                (str (when perf (str "(" perf")  "))
                                     (safe-str* x))))))


;; TODO - maybe we can remove this and just use _p2
(defn ^{:public true :no-doc true}
  _p 
  "Internal runtime dispatch target for fireworks macros.

   Pretty-prints the value with syntax coloring.
   Takes an optional leading argument (custom label or options map).
   Returns the value."

  ([opts x]
   (_p nil opts x))

  ([a opts x]
   (write-to-store x (assoc (:form-meta opts) :ns-str (:ns-str opts)))
   (let [opts (if (map? a)
                (merge (dissoc opts x :label) a)
                opts)
         
         debug-config?
         (or state/debug-config?
             (-> opts :user-opts :fw/debug-config? true?)) 

         config-before
         (when debug-config? @state/config)]

     (reset-state! opts)

     (let [
           ;; In cljs, if val is data structure but not cljs data structure
           ;; TODO - Mabye add tag-map to the opts to save a call in truncate
           native-logging (native-logging* x opts)
           opts           (merge opts native-logging)
           printing-opts  (try (formatted x opts)
                               (catch #?(:cljs js/Object :clj Exception)
                                      e
                                 (fw-throwable e x opts)))
           return-result? (when-not (= (:template opts)
                                       [:form-or-label :file-info])
                            x)]

       (when debug-config?
         (fw-debug-report config-before opts "fireworks.core/_p2")) 

       (when (or state/print-config?
                 (-> opts :user-opts :fw/print-config? true?))
         (fw-config-report))


       ;; TODO Change this to (= (:mode opts) :data)
       (if (:p-data? opts) 
         (as-data printing-opts)

         (do 
           (print-formatted printing-opts
                            #?(:cljs (when-not node? js-print)))

           ;; Fireworks formatting and printing of does not happen when:
           ;; - Value being printed is non-cljs or non-clj data-structure
           ;; - :log flag or :log? (deprecated)
           ;; - :pp, :prn, :println, :print flags or :print-with is function
           (when (and (not (:fw/log? opts))
                      (:log? opts))        ; <- This is for something like (? #js[1 2 3])
             #?(:cljs (if node?
                        (fireworks.core/pprint x) ; <- In node, do you want pprint here, or just js/console.log ?
                        (js/console.log x))
                :clj (fireworks.core/pprint x)))

           (reset! state/formatting-form-to-be-evaled? false)
           (when return-result? x) ))))))


(defn ^{:public true 
        :no-doc true} _p2 
  "Internal runtime dispatch target for fireworks macros.

   Takes an optional leading argument (custom label or options map).

   Pretty-prints the value with syntax coloring.
   
   Returns the value.
   
   Will not use fireworks formatting if:

   - In ClojureScript, if value is native data structure.
     Value will be printed with js/console.log or fireworks.core/pprint.
   
   - A native printing flag is supplied at callsite to fireworks.core/?
     Flag must be a leading keyword, one of:
     `#{:pp, :pp-, :js, :js- :log, or :log-}`
     Example: `(? :pp (+ 1 1))`
     
   - A custom `:print-with` function is supplied at callsite to fireworks.core/?
     Example: `(? {:print-with prn} (+ 1 1))`
   "
  [opts x]
  (let [debug-config? (or state/debug-config?
                          (-> opts :user-opts :fw/debug-config? true?)) 
        config-before (when debug-config? @state/config)]
    ;; Maybe write truncated string representation to .fireworks/results
    ;; This is opt-in by user, based on the existance of that path,
    ;; relative to the root of the project  
    (write-to-store x
                    (assoc (:form-meta opts)
                           :ns-str
                           (:ns-str opts)
                           :perf
                           (:perf opts)))
    
    ;; Reset the state if user is passing options that override defaults
    ;; in state.
    (reset-state! opts)

    (let [native-logging (try (native-logging* x opts)
                              (catch #?(:cljs js/Object :clj Throwable)
                                     e
                                (fw-throwable e x opts)))

          ;; TODO - Need a better way to do threading 
          opts           (merge opts
                                native-logging
                                (when (some-> opts :label :threading?)
                                  {:label (or (some-> opts :label :label)
                                              "threading:")}))

          printing-opts  (try (formatted x opts)
                              (catch #?(:cljs js/Object :clj Throwable)
                                     e
                                (fw-throwable e x opts)))

          return-result? (when-not (= (:template opts)
                                      [:form-or-label :file-info])
                           x)]

      (when debug-config? 
        (fw-debug-report config-before opts "fireworks.core/_p2")) 

      (when (or state/print-config?
                (-> opts :user-opts :fw/print-config? true?))
        (fw-config-report))

      (if (:p-data? opts) 
        ;; user asked for data, don't print
        (as-data printing-opts)
        
        (let [print? (if (contains? opts :when) (:when opts) true)]
          (when print?
            #_(println "_p2, normal printing brach")
            (print-formatted printing-opts 
                             #?(:cljs (when-not node? js-print))))
          
          ;; WHEN DOES THIS BRANCH GET CALLED? 
          ;; TODO should we reverse the order here, and put in a cond
          ;; Fireworks formatting and printing of does not happen when:
          ;; - Value being printed is non-cljs or non-clj data-structure
          ;; - :log :log- is used
          ;; - :pp or :pp- is used
          (when (and print?
                     (not (:fw/log? opts))
                     (:log? opts))
            #_(println "_p2, logging-branch")
            #?(:cljs (if node?
                       (fireworks.core/pprint x)
                       (js/console.log x))
               :clj (fireworks.core/pprint x)))

          (reset! state/formatting-form-to-be-evaled? false)
          (when return-result? x))))))


(defn- cfg-opts
  "Helper for shaping opts arg to be passed to fireworks.core/_p"
  [{:keys [a] :as m}]
  (let [cfg-opts  (when (map? a) a)
        label     (if cfg-opts (:label cfg-opts) a)
        user-opts (merge @state/config-overrides cfg-opts)
        cfg-opts  (merge
                   (dissoc (or cfg-opts {}) :label)
                   ;; maybe don't need the select-keys, just use m
                   (select-keys m [:template
                                   :log?
                                   :fw/log?
                                   :p-data?
                                   :form-meta])
                   {:ns-str         (some-> *ns* ns-name str)
                    :label          label
                    :user-opts      user-opts
                    :quoted-fw-form (list 'quote (:&form m))
                    :fw-fnsym       (some-> m
                                            :&form
                                            (nth 0 nil)
                                            (->> (str "fireworks.core/")))}
                   (some->> user-opts :template (hash-map :template)))]
    cfg-opts))


(defn- defd [x]
  (when (list? x)
    (when-let [sym (nth x 0 nil)]
      (let [defd (when (contains? core-defs sym)
                   (nth x 1 nil))]
        (some-> defd str)))))

(defn- helper2
  "Extracts the :label entry when present fireworks config opts"
  [m]
  ;;  (ff 'helper2 (get-user-config-edn-dynamic))
  (let [cfg-opts (cfg-opts m)
        defd     (defd (:x m))]
    (keyed [cfg-opts defd])))



(defn cast-var
  "Needs to be public, used at runtime by macros"
  [defd {:keys [ns-str]}]
  (symbol (str "#'" ns-str "/" defd)))


(defn- _p2-call
  "For tracing macro construction"
  [og-x m x+ i &env]
  (list 'try
        (list 'do
              (list 'when
                    true #_(if (zero? i) true x+)
                    (list 'fireworks.core/_p2
                          (merge {:qf                  (string/replace 
                                                        (str og-x)
                                                        #"p[0-9]+__[0-9]+#" "%")
                                  :margin-inline-start 2
                                  :template            [:form-or-label :result]}
                                 (when (map? m)
                                   {:user-opts m}))
                          (list 'if (list 'nil? x+)
                                ;; TODO - figure out annotation styling here
                                (list 'symbol "nil  \033[38;5;244;3m; <- short circuited\033[m")
                                x+)))
              x+)
        (list 'catch (if (:ns &env) 'js/Object 'Exception)
              'e
              (list 'fireworks.core/_p2
                    (merge {:qf                  (string/replace (str og-x)
                                                                 #"p[0-9]+__[0-9]+#" "%")
                            :margin-inline-start 2
                            :template            [:form-or-label :result]}
                           (when (map? m)
                             {:user-opts m}))
                    (list 'symbol "nil  \033[38;5;244;3m; <- short circuited\033[m")))))


;; cc-destructure fn from Snitch - https://github.com/AbhinavOmprakash/snitch
(defn- cc-destructure
  "A slightly modified version of clj and cljs' destructure to
  work with clj and cljs.

  This is used to help setup let-bindings for tracing with the :trace flag"
  [bindings]
  (let [bents (partition 2 bindings)
        pb (fn pb
             [bvec b v]
             (let [pvec
                   (fn [bvec b val]
                     (let [gvec (gensym "vec__")
                           gseq (gensym "seq__")
                           gfirst (gensym "first__")
                           has-rest (some #{'&} b)]
                       (loop [ret (let [ret (conj bvec gvec val)]
                                    (if has-rest
                                      (conj ret gseq (list `seq gvec))
                                      ret))
                              n 0
                              bs b
                              seen-rest? false]
                         (if (seq bs)
                           (let [firstb (first bs)]
                             (cond
                               (= firstb '&) (recur (pb ret (second bs) gseq)
                                                    n
                                                    (nnext bs)
                                                    true)
                               (= firstb :as) (pb ret (second bs) gvec)
                               :else (if seen-rest?
                                       (throw #?(:clj (new Exception "Unsupported binding form, only :as can follow & parameter")
                                                 :cljs (new js/Error "Unsupported binding form, only :as can follow & parameter")))
                                       (recur (pb (if has-rest
                                                    (conj ret
                                                          gfirst `(~'first ~gseq)
                                                          gseq `(~'next ~gseq))
                                                    ret)
                                                  firstb
                                                  (if has-rest
                                                    gfirst
                                                    (list `~'nth gvec n nil)))
                                              (inc n)
                                              (next bs)
                                              seen-rest?))))
                           ret))))
                   pmap
                   (fn [bvec b v]
                     (let [gmap (gensym "map__")
                           gmapseq (with-meta gmap {:tag 'clojure.lang.ISeq})
                           defaults (:or b)]
                       (loop [ret (-> bvec (conj gmap) (conj v)
                                      (conj gmap) (conj `(~'if (~'seq? ~gmap)
                                                               (~'apply ~'hash-map (~'seq ~gmapseq))
                                                               ~gmap))
                                      ((fn [ret]
                                         (if (:as b)
                                           (conj ret (:as b) gmap)
                                           ret))))
                              bes (let [transforms
                                        (reduce
                                          (fn [transforms mk]
                                            (if (keyword? mk)
                                              (let [mkns (namespace mk)
                                                    mkn (name mk)]
                                                (cond (= mkn "keys") (assoc transforms mk #(keyword (or mkns (namespace %)) (name %)))
                                                      (= mkn "syms") (assoc transforms mk #(list `quote (symbol (or mkns (namespace %)) (name %))))
                                                      (= mkn "strs") (assoc transforms mk str)
                                                      :else transforms))
                                              transforms))
                                          {}
                                          (keys b))]
                                    (reduce
                                      (fn [bes entry]
                                        (reduce #(assoc %1 %2 ((val entry) %2))
                                                (dissoc bes (key entry))
                                                ((key entry) bes)))
                                      (dissoc b :as :or)
                                      transforms))]
                         (if (seq bes)
                           (let [bb (key (first bes))
                                 bk (val (first bes))
                                 local (if #?(:clj  (instance? clojure.lang.Named bb)
                                              :cljs (implements? INamed bb))
                                         (with-meta (symbol nil (name bb)) (meta bb))
                                         bb)
                                 bv (if (contains? defaults local)
                                      (list 'get gmap bk (defaults local))
                                      (list 'get gmap bk))]
                             (recur (if (ident? bb)
                                      (-> ret (conj local bv))
                                      (pb ret bb bv))
                                    (next bes)))
                           ret))))]
               (cond
                 (symbol? b) (-> bvec (conj b) (conj v))
                 (vector? b) (pvec bvec b v)
                 (map? b) (pmap bvec b v)
                 :else (throw
                         #?(:clj (new Exception (str "Unsupported binding form: " b))
                            :cljs (new js/Error (str "Unsupported binding form: " b)))))))
        process-entry (fn [bvec b] (pb bvec (first b) (second b)))]
    (if (every? symbol? (map first bents))
      bindings
      (if-let [kwbs (seq (filter #(keyword? (first %)) bents))]
        (throw
          #?(:clj (new Exception (str "Unsupported binding key: " (ffirst kwbs)))
             :cljs (new js/Error (str "Unsupported binding key: " (ffirst kwbs)))))
        (reduce process-entry [] bents)))))


(defn ^:public str-keys-vec->syms-array-map 
  [coll]
  (->> coll
       (partition 2)
       (reduce (fn [acc [k v]]
                  (conj acc (symbol k) v)) 
               [])
       (apply array-map)))


(defn- prep-bindings [let-bindings a]
  (let [bindings-to-trace
        (reduce
         (fn [acc [binding value]] 
           (if (->> binding
                    str
                    (re-find #"__[0-9]+$"))
             acc
             (conj acc (str binding) value)))
         []
         (partition 2 let-bindings))]

    (list 'let
          let-bindings
          (list 'fireworks.core/_p2 
                (merge {:margin-inline-start 1
                        :template            [:result]}
                       (when (map? a)
                         {:user-opts (assoc a :let-bindings? true)}))
                (list 'with-meta
                      (list 'fireworks.core/str-keys-vec->syms-array-map 
                            bindings-to-trace)
                      {:fw/hide-brackets? true})))))


(defn- thread-form->let-binding [&env thread-sym m i x ]
  [(symbol (str "_" i))
   (let [og-x x
         x+   (if (pos? i)
                (let [x        (if (list? x) x (list x))
                      [f & rs] x
                      previous-binding (symbol (str "_" (dec i)))]
                  (if (contains? #{'-> 'some->} thread-sym)
                    (concat (list f previous-binding) rs)
                    (list 'when previous-binding (concat x [previous-binding]))))
                x)]
     (_p2-call og-x m x+ i &env))])


(defn- threading-form? [%]
  (and (list? %) 
       (contains? #{'-> '->> 'some-> 'some->>}
                  (first %))))

(defn- let-form? [%]
  (and (list? %) 
       (contains? #{'let}
                  (first %))))

(defn- as-let* [&env thread-sym rest-of-threading-form a]
  (let [as-let* (map-indexed (partial thread-form->let-binding &env thread-sym a)
                             rest-of-threading-form)
        as-let  (list 
                 'let
                 (vec (apply concat as-let*))
                 (symbol 
                  (str "_"
                       (dec (count rest-of-threading-form)))))]
    as-let))
                                                              
 
;                  AAA               PPPPPPPPPPPPPPPPP   IIIIIIIIII
;                 A:::A              P::::::::::::::::P  I::::::::I
;                A:::::A             P::::::PPPPPP:::::P I::::::::I
;               A:::::::A            PP:::::P     P:::::PII::::::II
;              A:::::::::A             P::::P     P:::::P  I::::I  
;             A:::::A:::::A            P::::P     P:::::P  I::::I  
;            A:::::A A:::::A           P::::PPPPPP:::::P   I::::I  
;           A:::::A   A:::::A          P:::::::::::::PP    I::::I  
;          A:::::A     A:::::A         P::::PPPPPPPPP      I::::I  
;         A:::::AAAAAAAAA:::::A        P::::P              I::::I  
;        A:::::::::::::::::::::A       P::::P              I::::I  
;       A:::::AAAAAAAAAAAAA:::::A      P::::P              I::::I  
;      A:::::A             A:::::A   PP::::::PP          II::::::II
;     A:::::A               A:::::A  P::::::::P          I::::::::I
;    A:::::A                 A:::::A P::::::::P          I::::::::I
;   AAAAAAA                   AAAAAAAPPPPPPPPPP          IIIIIIIIII
                                                              


(defmacro config!
  "Resets the value of fireworks.state/config-overrides"
  [m]
  (reset! state/config-overrides m))


(defmacro ?> 
  "Passes value to clojure.core/tap> and returns value."
  ([x] `(do (tap> ~x) ~x)))

(def all-flags
  #{:pp
    :println
    :print 
    :prn
    :+
    :- 
    :log
    :trace
    :data
    :no-file 
    :no-label
    :perf})

#?(:clj
   (defn- elide?
     []
     (or (= "true" (System/getProperty "fireworks.elide"))
         (= "true" (System/getenv "FIREWORKS_ELIDE")))))

(defmacro ?
  [& args]
  (if (elide?)
    (last args)
    (cond
      (= 0 (count args))
      nil

      (= 1 (count args))
      (let [[x]
            args

            {:keys [cfg-opts defd]}
            (helper2 {:x         x
                      :template  [:form-or-label :file-info :result]
                      :&form     &form
                      :form-meta (meta &form)})]
        `(do
           (when ~defd ~x)
           (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                              (if ~defd (cast-var ~defd ~cfg-opts) ~x))))

      :else
      (let [args                   (into [] args)
            x                      (peek args)
            ;;  debug?                 (= x :foo)
            mods                   (pop args)
            single-mod?            (= 1 (count mods))
            last-mod               (peek mods)
            supplied-user-opts     (when (map? last-mod) last-mod)
            mods                   (if supplied-user-opts
                                     (pop mods)
                                     mods)
            flags                  (into #{} (filter keyword? mods))
            label                  (when (seq mods)
                                     (or
                                      ;; The first mod that is not a keyword 
                                      (first (filter #(not (keyword? %)) mods))
                                      ;; Only one mod and it is not a known flag
                                      ;; e.g. (? :wtf (+ 1 1))
                                      (when (= 1 (count mods))
                                        (let [label (nth mods 0)]
                                          (when-not (contains? all-flags label)
                                            label)))
                                      ;; explicit :label option 
                                      ;; e.g. (? {:label "hi"} (+ 1 1))
                                      (some-> supplied-user-opts :label)))

            ;; Sanity check
            ;; _ (when (= x :fooo) 
            ;;     (?pp supplied-user-opts)
            ;;     (?pp mods)
            ;;     (?pp label))
            
            just-results-flag?     (contains? flags :-)
            resolve-option         (fn resolve-option [flag-kw opts-kw pred]
                                     (or (contains? flags flag-kw)
                                         (some-> supplied-user-opts
                                                 opts-kw
                                                 pred)))
            display-file-info?     (if (or just-results-flag?
                                           (resolve-option :no-file
                                                           :display-file-info?
                                                           false?))
                                     false
                                     (get @state/config :display-file-info?))
            display-label-or-form? (if (or just-results-flag?
                                           (resolve-option :no-label
                                                           :display-label-or-form?
                                                           false?))
                                     false
                                     (get @state/config :display-label-or-form?))
            just-results?          (or just-results-flag?
                                       (and (false? display-label-or-form?)
                                            (false? display-file-info?)))

            truncate?              (if (resolve-option :+ :truncate? false?)
                                     false
                                     (get @state/config :truncate?))
            print-with             (cond
                                     (contains? flags :pp)
                                     pprint
                                     (contains? flags :println)
                                     println
                                     (contains? flags :print)
                                     print
                                     (contains? flags :prn)
                                     prn
                                     :else
                                     (let [f (:print-with supplied-user-opts)]
                                       (when (fn? f) f)))
            log?                   (if (or (resolve-option :log :log? true?)
                                           (and just-results? print-with))
                                     true
                                     false)
            trace?                 (if (resolve-option :trace :trace? true?)
                                     true
                                     false)
            perf?                  (if (resolve-option :perf :perf? true?)
                                     true
                                     false)
            data?                  (if (resolve-option :data :data? true?)
                                     true
                                     false)
            template               (:template supplied-user-opts)

            supplied-user-opts-with-flag-overrides
            (assoc supplied-user-opts
                   :display-file-info? display-file-info?
                   :display-label-or-form? display-label-or-form?
                   :truncate? truncate?)

            template
            (or (when (some->> template
                               (contains? #{[:file-info :form-or-label :result]
                                            [:file-info :result]
                                            [:result]}))
                  template)
                (cond
                  just-results?
                  [:result]
                  (false? display-label-or-form?)
                  [:file-info :result]
                  (false? display-file-info?)
                  [:form-or-label :result]
                  :else
                  [:file-info :form-or-label :result]))

            tracing-form
            (when trace?
              (or (first (filter threading-form? &form))
                  (first (filter let-form? &form))))

            [sym]
            tracing-form

            [thread-sym & rest-of-threading-form]
            (when-not (= sym 'let) tracing-form)

            [_ let-bindings]
            (when (= sym 'let) tracing-form)

            let-bindings
            (some-> let-bindings cc-destructure)

            trace+valid-trace-form?
            (when trace?
              (when (some->> sym (contains? #{'-> '->> 'some-> 'some->> 'let}))
                true))]

        (cond
          ;; Special handling for tracing a threading form
          trace+valid-trace-form?
          (let [form-meta (meta &form)
                ns-str    (some-> *ns* ns-name str)
                bindings? (boolean let-bindings)
                as-let    (if bindings?
                            (prep-bindings let-bindings supplied-user-opts)
                            (as-let* &env
                                     thread-sym
                                     rest-of-threading-form
                                     supplied-user-opts))]

            `(do
               (fireworks.core/_p2
                {:form-meta ~form-meta
                 :ns-str    ~ns-str
                 :qf        (quote ~tracing-form)
                 :template  [:file-info :form-or-label :result]}
                (symbol (str "\n  "
                             (if ~bindings?
                               "let bindings"
                               (str "tracing "
                                    (quote ~thread-sym))))))
               ~as-let
               (println)
               (fireworks.core/_p2 {:template  [:result]
                                    :user-opts {:margin-bottom 1}}
                                   ~x)
               ~x))

          ;; No file-info or label annotation and user wants default printing fn 
          log?
          `(do #?(:cljs (if node?
                          (fireworks.core/pprint ~x)
                          (js/console.log ~x))
                  :clj (fireworks.core/pprint ~x))
               ~x)

          ;; No file-info or label annotation and user has supplied printing fn
          (and just-results? print-with)
          `(do (print-with ~x) ~x)

          :else
          (let [{:keys [log?* defd qf-nil? cfg-opts]}
                (let [defd           (defd x)
                      log?*          (or log? (contains? #{pprint} print-with))
                      quoted-fw-form (if just-results?
                                       (list 'quote nil)
                                       (list 'quote &form))
                      ;; what this for?
                      fw-fnsym       (when-not just-results?
                                       "fireworks.core/?")
                      ;; maybe always include?
                      form-meta      (when-not just-results? (meta &form))
                      qf-nil?        (false? display-label-or-form?)
                      user-opts      (merge @state/config-overrides
                                            supplied-user-opts-with-flag-overrides)
                      cfg-opts       (cond->
                                      {:label          label
                                       :template       template
                                       :ns-str         (some-> *ns* ns-name str)
                                       :user-opts      user-opts
                                       :quoted-fw-form quoted-fw-form
                                       :fw-fnsym       fw-fnsym}

                                       (false? truncate?)
                                       (assoc :truncate? false)

                                       form-meta
                                       (assoc :form-meta form-meta)

                                       log?*
                                       (assoc :log? log?* :fw/log? log?*)

                                       data?
                                       (assoc :p-data? true))]

                  (keyed [defd x qf-nil? cfg-opts log?*]))]

            ;;  #_(ff "?, 2-arity, cfg-opts" cfg-opts)
            `(let [perf#     (when ~perf?
                               (fireworks.core/perf-label (fn [] ~x)))
                   cfg-opts# (assoc ~cfg-opts
                                    :qf
                                    (if ~qf-nil? nil (quote ~x))
                                    :perf
                                    perf#)
                   ret#      (if ~defd (cast-var ~defd ~cfg-opts) ~x)]
               (when ~defd ~x)
               (if ~log?*
                 (do
                   (fireworks.core/_p2 cfg-opts# ret#)
                   ((if ~log?
                      fireworks.core/_log
                      fireworks.core/_pp)
                    ~cfg-opts
                    (if ~defd (cast-var ~defd ~cfg-opts) ~x)))
                 (fireworks.core/_p2 cfg-opts# ret#)))))))))


;; TODO - Add to docs/readme
(defmacro ^{:public true} ?flop
  "Prints the form (or user-supplied label), the namespace info,
   and then the value. Same as fireworks.core/?, but order of arguments
   is reversed in the case of a call that is exactly 2 args. Intended for use
   both internal to fireworks (tracing macros), and also for users wanting
   to drop a `?` call into a thread-first form and also pass an options
   map, or a label:
   ```clojure
   (-> \"foo\nbar\"
       str/split-lines
       (?flop \"split on nl\")
       count)
   ```
   
   The form (or optional label) is formatted with pprint.
   The value is formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (if (elide?)
     x
     (let [{:keys [cfg-opts defd]}
           (helper2 {:x         x
                     :template  [:form-or-label :file-info :result]
                     :&form     &form
                     :form-meta (meta &form)})]
       `(do 
          (when ~defd ~x)
          (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                             (if ~defd (cast-var ~defd ~cfg-opts) ~x))))))

  ([x a]
   (if (elide?) 
     x
     (let [{:keys [cfg-opts defd]}   
           (helper2 {:a         a
                     :template  [:form-or-label :file-info :result]
                     :x         x
                     :&form     &form
                     :form-meta (meta &form)})]
       `(do 
          (when ~defd ~x)
          (fireworks.core/_p ~a
                             (assoc ~cfg-opts :qf (quote ~x))
                             (if ~defd (cast-var ~defd ~cfg-opts) ~x)))))))



;; Silencers

(defmacro !?> [x]
  x)

(defmacro !?
  [& args]
  (last args))

(defmacro !?flop
  ([])
  ([x] x)
  ([x _] x))

                                                                                                                     
;; PPPPPPPPPPPPPPPPP   PPPPPPPPPPPPPPPPP   
;; P::::::::::::::::P  P::::::::::::::::P  
;; P::::::PPPPPP:::::P P::::::PPPPPP:::::P 
;; PP:::::P     P:::::PPP:::::P     P:::::P
  ;; P::::P     P:::::P  P::::P     P:::::P  
  ;; P::::P     P:::::P  P::::P     P:::::P  
  ;; P::::PPPPPP:::::P   P::::PPPPPP:::::P   
  ;; P:::::::::::::PP    P:::::::::::::PP    
  ;; P::::PPPPPPPPP      P::::PPPPPPPPP      
  ;; P::::P              P::::P              
  ;; P::::P              P::::P              
  ;; P::::P              P::::P              
;; PP::::::PP          PP::::::PP          
;; P::::::::P          P::::::::P          
;; P::::::::P          P::::::::P          
;; PPPPPPPPPP          PPPPPPPPPP          
                                                                                                                     
                                                                                                                     



;; pprint function below is culled from https://github.com/eerohele/pp
;; check out fireworks.pp ns for complete src

(defn pprint
  "Pretty-print an object.

  Given one arg (an object), pretty-print the object into *out* using
  the default options.

  Given two args (an object and an options map), pretty-print the object
  into *out* using the given options.

  Given three args (a java.io.Writer, a object, and an options map),
  pretty-print the object into the writer using the given options.

  If *print-dup* is true, pprint does not attempt to pretty-print;
  instead, it falls back to default print-dup behavior. ClojureScript
  does not support *print-dup*.

  Options:

    :max-width (long or ##Inf, default: 72)
      Avoid printing anything beyond the column indicated by this
      value.

    :map-entry-separator (string, default: \",\")
      The string to print between map entries. To not print commas
      between map entries, use an empty string."
  ([x]
   (pprint *out* x nil))
  ([x opts]
   (pprint *out* x opts))
  ([writer x {:keys [max-width map-entry-separator]
              :or {max-width 72 map-entry-separator ","}
              :as opts}]
   (assert (or (nat-int? max-width) (= max-width ##Inf))
     ":max-width must be a natural int or ##Inf")

   (letfn
     [(pp [writer]
        (let [writer (fireworks.pp/count-keeping-writer writer {:max-width max-width})]
          (fireworks.pp/-pprint x writer
            (assoc opts
              :map-entry-separator map-entry-separator
              :level 0
              :indentation ""
              :reserve-chars 0))
          (fireworks.pp/nl writer)))]
     #?(:clj
        (do
          (assert (instance? java.io.Writer writer)
            "first arg to pprint must be a java.io.Writer")

          (if *print-dup*
            (do
              (print-dup x writer)
              (.write ^java.io.Writer writer "\n"))
            (pp writer))

          (when *flush-on-newline* (.flush ^java.io.Writer writer)))

        :cljs
        (if writer
          (do
            (assert (satisfies? cljs.core.IWriter writer)
              "first arg to pprint must be a cljs.core.IWriter")

            (pp writer))

          ;; ClojureScript does not have *out* bound by default.
          (let [sb (goog.string.StringBuffer.)
                writer (StringBufferWriter. sb)]
            (pp writer)
            (-> sb str string-print)
            (when *flush-on-newline* (-flush writer))))))))
