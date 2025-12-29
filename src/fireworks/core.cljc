(ns fireworks.core
  (:require
   [clojure.set :as set]
   [clojure.data :as data]
   [clojure.spec.alpha :as s]
   [fireworks.browser]
   [fireworks.pp :as fireworks.pp]
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
   [clojure.string :as string]
   [fireworks.config :as config]
   [fireworks.util :as util] 
   [lasertag.core :as lasertag]
   [fireworks.defs :as defs]
   [clojure.walk :as walk])
  #?(:cljs (:require-macros 
            [fireworks.core :refer [? !? ?> !?>]])))

(declare pprint)

(def core-defs 
  (set '(def defn defrecord defstruct defprotocol defmulti deftype defmethod)))


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


(defn resolve-label-length [label-length-limit]
  (or (when (s/valid? ::specs.config/label-length-limit
                      label-length-limit)
        label-length-limit)
      (:label-length-limit @state/config)
      (-> config/options
          :label-length-limit
          :default)))

(defn- user-label-or-form!
  [{:keys [qf template label mll?]
   {:keys [label-length-limit]} :user-opts
    :as opts}]
  (let [indent-spaces
        (or (some-> @state/margin-inline-start
                    util/spaces)
            nil)

        label
        (when (contains?
               #{[:form-or-label :file-info :result]
                 [:file-info :form-or-label :result]
                 [:form-or-label :result]
                 [:form-or-label :file-info]}
               template)
          (when label
            (let [label-entity-tag
                  (case (some-> opts :label-color util/as-str)
                    "green"
                    :eval-label-green
                    "blue"
                    :eval-label-blue
                    "red"
                    :eval-label-red
                    :eval-label)

                  label
                  (if mll?
                    (string/join
                     "\n"
                     (map
                      #(str (tag/tag-entity!
                             (str indent-spaces %)
                             label-entity-tag)
                            (tag/tag-reset!))
                      (string/split label #"\n")))

                    (tag/tag-entity!
                     (util/shortened label
                                     (resolve-label-length label-length-limit))
                     label-entity-tag))]
              (str indent-spaces label))))

        form
        (when-not label
          (when qf
            ;; TODO - Confirm that toggling this state doesn't matter, remove it
            (reset! state/formatting-form-to-be-evaled? true)
            (let [form-entity-tag
                  (case (some-> opts :label-color util/as-str)
                    "green"
                    :eval-form-green
                    "blue"
                    :eval-form-blue
                    "red"
                    :eval-form-red
                    :eval-form)
                  
                  shortened
                  (tag/tag-entity! 
                   (if (:format-label-as-code? @state/config)
                     (-> qf
                         (pprint {:max-width 33})
                         with-out-str
                         (string/replace #"\n+$" ""))
                     (util/shortened qf
                                     (resolve-label-length label-length-limit)))
                   form-entity-tag) 
                  ret       shortened]
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
                           (tag/tag-entity! :file-info))]
   [file-info* file-info]))


(defn- file-info-and-eval-label
  "file-info and eval lable get tagged, so order matters."
  [{:keys [file-info-first? label?] :as opts}]
  (if file-info-first?
    (let [[file-info* file-info] (file-info opts)
          [label form]           (when label? (user-label-or-form! opts))]
      (keyed [label
              form
              file-info*
              file-info]))
    (let [[label form]           (when label? (user-label-or-form! opts))
          [file-info* file-info] (file-info opts)]
      (keyed [label
              form
              file-info*
              file-info]))))

(defn- fmt+
  [{:keys [mll? 
           log?
           opts
           label
           source
           template
           truncate?
           threading?
           user-print-fn
           file-info-first?]}]
  (let [just-result?
        (and log? (= template [:result]))

        label?
        (not (or just-result? (= template [:file-info :result])))

        {:keys [form
                label
                file-info
                file-info*]}
        (when-not just-result?
          (file-info-and-eval-label (merge opts
                                           (keyed [file-info-first?
                                                   mll? 
                                                   label
                                                   label?]))))

        result-header
        (when-not (or (contains? #{[:result] [:form-or-label :file-info]}
                                 template)
                      log?
                      threading?)
          ;; TODO - is the space before the newline necessary?
          (tag/tag-entity! " \n" :result-header))

        ;; TODO - Remove if post-replace works - cljs stuff below
        css-count*
        (count @state/styles)

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


        fmt+          
        (when-not just-result?
          (if file-info-first?
            (str 
             file-info
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
                 fmt)))]

     {:fmt        fmt
      :fmt+       fmt+
      :file-info* file-info*
      :css-count* css-count*}))


(defn- margin-block-str
  [{:keys [template user-opts mode]}
   k]
  (cond
    (and (= template [:result])
         (not (pos-int? (k user-opts))))
    ""
    (contains? #{:log :pp :js} mode)
    "\n"
    :else
    (string/join (repeat (get @state/config k 0) "\n")))

  ;; (ff k (get @state/config k 0))
  ;; (string/join (repeat (get @state/config k 0) "\n"))
  ;; (if-let [mb (ff (k user-opts))]
  ;;     (if (or (zero? mb) (pos-int? mb))
  ;;       (string/join (repeat mb "\n"))
  ;;       "\n")
  ;;     "")
  )



(defn- formatted
  "Formatted log with file-info, form/comment, fat-arrow and syntax-colored
   pretty-printed result. Used by fireworks.core/? macro."
  [source
   {:keys [qf
           log?
           mode
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

        label
        (if (coll? label)
          (with-out-str (pprint label))
          label)

        mll?
        (when (string? label) (re-find #"\n" label))

        ml-qf?
        (when (string? qf) (re-find #"\n" qf))

        file-info-first?
        (or (contains?
             #{[:file-info :result]
               [:file-info :form-or-label]
               [:file-info :form-or-label :result]}
             template)
            (and label
                 (contains? #{[:form-or-label :file-info]
                              [:form-or-label :file-info :result]}
                            template)
                 (or mll? (< 44 (count (str label)))))
            (and (= template [:form-or-label :file-info :result])
                 (or ml-qf? (:format-label-as-code? opts))))

        {:keys [fmt+ fmt file-info* css-count*]}
        (fmt+  (keyed [file-info-first? 
                       user-print-fn
                       threading?
                       truncate?
                       template
                       source
                       label
                       opts
                       mll?
                       log?]))]
    
    ;; TODO Change this to (= mode :data)
    ;; TODO - Change if post-replace works - cljs stuff below
    (if  p-data?
      ;; If p-data, return a map of preformatted values
      (merge
       {:ns-str        ns-str
        :quoted-form   qf
        :file-info-str file-info*
        :formatted+    {:string fmt+}
        :formatted     {:string fmt}}
       form-meta)

      ;; Else if print-and-return fns, return printing opts
      {:fmt           fmt+
       :log?          log?

       ;; Defaults to {:margin-bottom 1 :margin-top 0}
       ;; If :result flag is used it will be {:margin-bottom 0 :margin-top 0}
       ;; If :result flag is used w call-site opts for :margin-*, those will win
       :margin-bottom (margin-block-str opts :margin-bottom)
       :margin-top    (margin-block-str opts :margin-top)
       :template      template})))


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
    (reset! state/margin-inline-start (:margin-inline-start opts))

    ;; TODO - lose if post-replace works
    (reset! state/styles [])
    (reset! state/*formatting-meta-level 0)
    (reset! state/rainbow-level 0)
    (reset! state/top-level-value-is-sev? false)
    (reset! messaging/warnings-and-errors [])

    ;; Resetting config to user's config.edn merged with defaults
    (do (when state/debug-config?
          (messaging/fw-debug-report-template
           "Resetting fireworks.state/config atom to"
           (state/merged-config)
           :magenta))
        (reset! state/config (state/merged-config)))

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
  ;; It may pull hightlight style from merged theme.
  (reset! state/highlight
          (some->> find-vals 
                   state/highlight-style))
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


(defn- print-formatted
  ([x]
   (print-formatted x nil))
  ([{:keys [fmt log? err err-x err-opts] :as x}
    js-printing-fn]
   (if (instance? fireworks.messaging.FireworksThrowable x)
     (let [{:keys [line column file]} (:form-meta err-opts)
           ns-str                     (:ns-str err-opts)] 

       (messaging/caught-exception err
                                   {:value  err-x
                                    :type   :error
                                    :form   (:quoted-fw-form err-opts)
                                    :header (or (some-> err-opts :header)
                                                (some-> err-opts :fw-fnsym))
                                    :line   line
                                    :column column
                                    :file   (or file ns-str)})
       (println 
        (str "\nFalling back to pprint...\n\n"
             (with-out-str (pprint err-x)))))

     (let [termf #(do 
                    ;; If it has been formatted by fireworks, it will print here.
                    (print (:margin-top %))
                    (some-> fmt print)
                    ;; Extra line based on :margin-bottom
                    ;; TODO - not print if fmt is nil or blank string?
                    ((if log? print println) (:margin-bottom x)))  ]

       #?(:cljs (if node? (termf x) (js-printing-fn x))
          :clj (termf x))))))

(defn- try-pp [x]
  (try (pprint x)
       #?(:clj
          (catch Throwable e
            (let [{:keys [coll-size classname]} (-> x lasertag/tag-map)]
              (messaging/unable-to-print-warning
               "fireworks.core/?"
               (str (messaging/italic "Problem:\n")
                    "  " 
                    "Unable to print value with pprint."
                    "\n\n\n"
                    (messaging/italic "Cause:\n")
                    "  "
                    coll-size
                    "\n\n\n"
                    (messaging/italic "Value class:\n")
                    "  "
                    classname)))))))

(defn- _pp* [user-opts x]
  (print (margin-block-str user-opts :margin-top))
  (try-pp x)
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
     (let [{:keys [coll-type? carries-meta? t transient? classname]
            :as   m} 
           (util/tag-map* x
                          {:include-function-info?           false
                           :include-js-built-in-object-info? false})]

       (when (or (and coll-type?
                      (not carries-meta?)
                      (not= t :cljs.core/Atom)
                      (not= t :cljs.core/Volatile)
                      (not transient?))
                 ;; Specific to HTML DOM Collections, maybe swap this out for
                 ;; something else, after adding support to lasertag
                 (when (= t :iterable) 
                   (or (contains? defs/html-collection-types-primary classname)
                       (contains? defs/html-collection-types-secondary classname))))
         {:log? true}))

     :clj
     nil))


(defn- fw-throwable [e x opts]
  (messaging/->FireworksThrowable
   e
   x
   (assoc opts
          :header
          "fireworks.core/formatted")))


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


(defn ^{:public true}
  as-data
  [printing-opts]
  (assoc (dissoc printing-opts :formatted :formatted+)
         :formatted
         (tagged-string-data printing-opts :formatted)
         :formatted-with-header
         (tagged-string-data printing-opts :formatted+)))


(defn ^{:public true}
  _p 
  "Internal runtime dispatch target for fireworks macros.

   Pretty-prints the value with syntax coloring.
   Takes an optional leading argument (custom label or options map).
   Returns the value."

  ([opts x]
   (_p nil opts x))

  ([a opts x]
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
          ;; - :log or :log- is used (deprecated)
          ;; - :js or :js- is used
          ;; - :pp or :pp- is used
          (when (and (not (:fw/log? opts))
                     (:log? opts))        ; <- This is for something like (? #js[1 2 3])
            #?(:cljs (if node?
                       (fireworks.core/pprint x) ; <- In node, do you want pprint here, or just js/console.log ?
                       (js/console.log x))
               :clj (fireworks.core/pprint x)))

          (reset! state/formatting-form-to-be-evaled? false)
          (when return-result? x)))))))


(defn ^{:public true} _p2 
  "Internal runtime dispatch target for fireworks macros.

   Takes an optional leading argument (custom label or options map).

   Pretty-prints the value with syntax coloring.
   
   Returns the value.
   
   Will not use fireworks formatting if:

   - In ClrjureScript, if value is native data structure.
     Value will be printed with js/console.log or fireworks.core/pprint.
   
   - A native printing flag is supplied at callsite to fireworks.core/?
     Flag must be a leading keyword, one of:
     `#{:pp, :pp-, :js, :js- :log, or :log-}`
     Example: `(? :pp (+ 1 1))`
     
   - A valid `:print-with` option is supplied at callsite to fireworks.core/?
     Value must be one of the following functions from `clojure.core`:
     `#{pr pr-str prn prn-str print println}`
     Example: `(? {:print-with prn} (+ 1 1))`
   "
  [opts x]
  ;; (ff opts)
  (let [debug-config? (or state/debug-config?
                          (-> opts :user-opts :fw/debug-config? true?)) 
        config-before (when debug-config? @state/config)]

    (reset-state! opts)

    (let [native-logging (try (native-logging* x opts)
                              (catch #?(:cljs js/Object :clj Throwable)
                                     e
                                (fw-throwable e x opts)))
          ;; TODO - Need a better way to do the hack for threading 
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

      ;; TODO Change this to (= (:mode opts) :data)
      (if (:p-data? opts) 
        (as-data printing-opts)
        

        (let [print? (if (contains? opts :when) (:when opts) true)]
          (when print?
            (print-formatted printing-opts 
                             #?(:cljs (when-not node? js-print))))
          ;; Fireworks formatting and printing of does not happen when:
          ;; - Value being printed is non-cljs or non-clj data-structure
          ;; - :log :log- is used
          ;; - :pp or :pp- is used
          (when (and print?
                     (not (:fw/log? opts))
                     (:log? opts))
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

(def modes #{
             :comment
             :data
             :log
             :log-
             :pp
             :pp-
             :js
             :js-
             :no-truncation

             ;; deprecated in favor of alias :no-label, :no-file, etc.
             :label
             :file
             :result

             ;; unsupported / experimental
             :trace
             })

(def alias-modes 
  {
   ;; sugar for consuming this the formatted string
   :string       :data

   ;; sugar for changing what extra stuff is displayed
   :no-label     :file
   :no-file      :label
   :-            :result 
   :+            :no-truncation
   :log/-        :log-
   :js/-         :js-
   :pp/-         :pp-
   :log/no-label :log
   :log/no-file  :log
   :js/no-label  :js
   :js/no-file   :js
   :pp/no-label  :pp
   :pp/no-file   :pp 
   })


(defn- _p2-call [og-x m x+ i]
  (list
   'do 
   (list 
    'when
     true #_(if (zero? i) true x+)
    (list 'fireworks.core/_p2 
          (merge {:qf                  (string/replace (str og-x)
                                                       #"p[0-9]+__[0-9]+#" "%")
                  :margin-inline-start 2
                  ;;  :label-color         :green
                  :template            [:form-or-label :result]}
                 (when (map? m)
                   {:user-opts m}))
          (list 'if (list 'nil? x+)
                ;; TODO - figure out annotation styling here
                (list 'symbol "nil  \033[38;5;244;3m; <- short circuited\033[m")
                x+)
          ))
   x+))

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


(defn- thread-form->let-binding [thread-sym m i x]
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
     (_p2-call og-x m x+ i))])


(defn- threading-form? [%]
  (and (list? %) 
       (contains? #{'-> '->> 'some-> 'some->>}
                  (first %))))

(defn- let-form? [%]
  (and (list? %) 
       (contains? #{'let}
                  (first %))))

(defn- as-let* [thread-sym rest-of-threading-form a]
  (let [as-let* (map-indexed (partial thread-form->let-binding thread-sym a)
                             rest-of-threading-form)
        as-let  (list 
                 'let
                 (vec (apply concat as-let*))
                 (symbol 
                  (str "_"
                       (dec (count rest-of-threading-form)))))]
    as-let))


(defn- resolve-tracing-form [&form mode]
  (let [tracing-form                          
        (when (= mode :trace)
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

        mode                                  
        (if (= mode :trace)
          (when (some->> sym (contains? #{'-> '->> 'some-> 'some->> 'let}))
            :trace)
          mode)]
    {:thread-sym             thread-sym  
     :rest-of-threading-form rest-of-threading-form  
     :let-bindings           let-bindings    
     :tracing-form           tracing-form  
     :mode                   mode}))


(defn- mode+template [a &form]
  (let [alias-mode
        (if (contains? alias-modes a) a nil)
        
        a
        (get alias-modes a a)

        mode
        (if (contains? modes a) a nil)

        template
        (get 
         {:label      [:form-or-label :result]
          :file       [:file-info :result]
          :result     [:result]
          :comment    [:form-or-label :file-info]
          ;; default-case nix?
          :data       [:form-or-label :file-info :result]
          ;; default-case nix?
          :log        [:form-or-label :file-info :result]
          :log-       nil
          ;; default-case nix?
          :js         [:form-or-label :file-info :result]
          :js-        nil
          ;; default-case nix?
          :pp         [:form-or-label :file-info :result]
          :pp-        nil
          ;; default-case nix?
          :trace      [:form-or-label :file-info :result]}
         mode
         [:form-or-label :file-info :result])
        
        m
        (resolve-tracing-form &form mode)]
    (merge {:mode       mode
            :alias-mode alias-mode
            :template   template}
           m)))

#?(:clj
   (defn- ?2-helper
     [{:keys [cfg-opts* alias-mode mode template label &form x truncation-flag]}]
     (let [defd           (defd x)

           log?*          (contains? #{:log :pp :js} mode)

           results-mode?  (contains? #{:result :log- :pp- :js-} mode)

           quoted-fw-form (if results-mode?
                            (list 'quote nil)
                            (list 'quote &form))

           fw-fnsym       (when-not results-mode?
                            "fireworks.core/?")

           form-meta      (when-not results-mode?
                            (meta &form))

           qf-nil?        (contains? #{:comment :result} mode)

           user-opts     (merge @state/config-overrides
                                cfg-opts*
                                (when (false? truncation-flag)
                                  {:truncate? false}))

           template       (or (:template user-opts) template)
           cfg-opts       (merge (dissoc (or cfg-opts* {}) :label)

                                 {:mode           mode
                                  :alias-mode     alias-mode
                                  :label          label
                                  :template       template
                                  :ns-str         (some-> *ns* ns-name str)
                                  :user-opts      user-opts
                                  :quoted-fw-form quoted-fw-form
                                  :fw-fnsym       fw-fnsym}

                                 (when (false? truncation-flag) 
                                   {:truncate? false})

                                 (when form-meta
                                   {:form-meta form-meta})

                                 (when log?*
                                   {:log?    log?*
                                    :fw/log? log?*})

                                 (when (= mode :data)
                                   {:p-data? true}))]

       (keyed [defd x qf-nil? cfg-opts log?*]))))

                                                               
 
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


(defmacro ?
  ([])
  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:form-or-label :file-info :result]
                   :&form     &form
                   :form-meta (meta &form)})]
     `(do 
        (when ~defd ~x)
        (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                           (if ~defd (cast-var ~defd ~cfg-opts) ~x)))))
  ;; 2-arity, one of:
  ;; (? :flag x)
  ;; (? "mylabel" x)
  ;; (? {:option ...} x)
  ([a x]
   (let [{:keys [template
                 thread-sym
                 rest-of-threading-form 
                 let-bindings
                 tracing-form
                 mode
                 alias-mode]
          :as   _m}
         (mode+template a &form)]
     (case mode
       :trace
       (let [form-meta (meta &form)
             ns-str    (some-> *ns* ns-name str)
             bindings? (boolean let-bindings)
             as-let    (if bindings?
                         (prep-bindings let-bindings a)
                         (as-let* thread-sym rest-of-threading-form a))]

         `(do 
            (fireworks.core/_p2 {:form-meta ~form-meta
                                 :ns-str    ~ns-str
                                 :qf        (quote ~tracing-form) 
                                 :template  [:file-info :form-or-label :result]}
                                (symbol (str "\n  "
                                             (if ~bindings?
                                               "let bindings"
                                               (str "tracing " (quote ~thread-sym))))))
            ~as-let
            (println)
            (fireworks.core/_p2 {:template  [:result] 
                                 :user-opts {:margin-bottom 1}}
                                ~x)
            ~x))
       :log-
       `(do #?(:cljs (if node?
                       (fireworks.core/pprint ~x)
                       (js/console.log ~x))
               :clj (fireworks.core/pprint ~x))
            ~x)

       :js-
       `(do #?(:cljs (if node?
                       (fireworks.core/pprint ~x)
                       (js/console.log ~x))
               :clj (fireworks.core/pprint ~x))
            ~x)

       :pp-
       `(do (fireworks.core/pprint ~x)
            ~x)

       (let [cfg-opts*                             
             (when (map? a) a)

             label                                 
             (or (:label cfg-opts*)
                 (cond (= mode :comment) x
                       mode              nil
                       :else             (when-not cfg-opts* a)))

             truncation-flag                       
             (when (= mode :no-truncation) false)

             {:keys [log?* defd qf-nil? cfg-opts]} 
             (?2-helper (keyed [mode
                                alias-mode
                                template 
                                cfg-opts* 
                                label
                                truncation-flag
                                &form
                                x]))]

         ;;  #_(ff "?, 2-arity, cfg-opts" cfg-opts)
         `(let [cfg-opts# (assoc ~cfg-opts :qf (if ~qf-nil? nil (quote ~x)))
                ret#      (if ~defd (cast-var ~defd ~cfg-opts) ~x)]
            (when ~defd ~x)
            (if ~log?*
              (do 
                (fireworks.core/_p2 cfg-opts# ret#)
                ((if (or (= ~mode :log) (= ~mode :js))
                   fireworks.core/_log
                   fireworks.core/_pp)
                 ~cfg-opts
                 (if ~defd (cast-var ~defd ~cfg-opts) ~x)))
              (fireworks.core/_p2 cfg-opts# ret#)))))))
   
  ;; 3-arity, one of:
  ;; (? :flag "mylabel" x)
  ;; (? "mylabel" {:option ...} x)
  ;; (? :flag {:option ..} x)

  ([mode-or-label a x]
   (let [{:keys [template
                 thread-sym
                 rest-of-threading-form 
                 let-bindings
                 tracing-form
                 mode]}
         (mode+template mode-or-label &form)]
     (case mode
       :trace
       (let [form-meta (meta &form)
             ns-str    (some-> *ns* ns-name str)
             bindings? (boolean let-bindings)
             as-let    (if bindings?
                         (prep-bindings let-bindings a)
                         (as-let* thread-sym rest-of-threading-form a))]

         `(do 
            (fireworks.core/_p2 {:form-meta ~form-meta
                                 :ns-str    ~ns-str
                                 :qf        (quote ~tracing-form) 
                                 :template  [:file-info :form-or-label :result]}
                                (symbol (str "\n  "
                                             (if ~bindings?
                                               "let bindings"
                                               (str "tracing "
                                                    (quote ~thread-sym))) )))
            ~as-let
            (println)
            (fireworks.core/_p2 {:template  [:result] 
                                 :user-opts {:margin-bottom 1}}
                                ~x)
            ~x))      

       ;; Remove this? or should it be js?
       :log-
       `(do #?(:cljs (if node?
                       (fireworks.core/pprint ~x)
                       (js/console.log ~x))
               :clj (fireworks.core/pprint ~x))
            ~x)

       :pp-
       `(do (fireworks.core/pprint ~x)
            ~x)

       (let [cfg-opts*                             
             (when (map? a) a)

             label                                 
             (or (:label cfg-opts*)
                 (cond
                   (= mode :comment) x
                   (not cfg-opts*)   a
                   :else             (when-not mode mode-or-label)))

             truncation-flag                       
             (when (= mode :no-truncation) false)

             {:keys [log?* defd qf-nil? cfg-opts]} 
             (?2-helper (keyed [truncation-flag
                                cfg-opts* 
                                template 
                                label
                                &form 
                                mode
                                x]))]

        ;;  (ff "?, 3-arity" cfg-opts)
         `(let [cfg-opts# (assoc ~cfg-opts :qf (if ~qf-nil? nil (quote ~x)))
                ret#      (if ~defd (cast-var ~defd ~cfg-opts) ~x)] 
            (when ~defd ~x)
            (if ~log?*
              (do 
                (fireworks.core/_p2 cfg-opts# ret#)
                ((if (or (= ~mode :log) (= ~mode :js))
                   fireworks.core/_log
                   fireworks.core/_pp)
                 ~cfg-opts
                 (if ~defd (cast-var ~defd ~cfg-opts) ~x)))
              (fireworks.core/_p2 cfg-opts# ret#))))))))


;; TODO - Add to docs/readme
(defmacro ^{:public true} ?flop
  "Prints the form (or user-supplied label), the namespace info,
   and then the value. Same as fireworks.core/?, but order of arguments
   is reversed in the case of 2 arities. Inteded for use both 
   internal to fireworks (tracing macros), and also for users wanting
   to drop a `?` call into a thread-first form and also pass an options
   map.
   
   The form (or optional label) is formatted with pprint.
   The value is formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:form-or-label :file-info :result]
                   :&form     &form
                   :form-meta (meta &form)})]
     `(do 
        (when ~defd ~x)
        (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                           (if ~defd (cast-var ~defd ~cfg-opts) ~x)))))

  ([x a]
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
                           (if ~defd (cast-var ~defd ~cfg-opts) ~x))))))



;; Silencers

(defmacro !?> [x]
  `~x)

(defmacro !?
  ([])
  ([x] `~x)
  ([_ x] `~x)
  ([_ _ x] `~x))

(defmacro !?flop
  ([])
  ([x] `~x)
  ([x _] `~x))

                                                                                                                     
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
