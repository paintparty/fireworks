(ns fireworks.core
  (:require
   [fireworks.pp :as fireworks.pp :refer [?pp] :rename {?pp ff}]
   [clojure.set :as set]
   [clojure.data :as data]
   [fireworks.messaging :as messaging]
   [fireworks.serialize :as serialize]
   [fireworks.state :as state]
   [fireworks.tag :as tag]
   #?(:cljs [fireworks.macros
             :refer-macros
             [keyed
              compile-time-warnings-and-errors]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [clojure.string :as string]
   [fireworks.config :as config]
   [clojure.spec.alpha :as s]
   [lasertag.core :as lasertag]
   [fireworks.util :as util])
  #?(:cljs (:require-macros 
            [fireworks.core :refer [? !? ?> !?>]])))

(declare pprint)

(def core-defs 
  (set '(def defn defrecord defstruct defprotocol defmulti deftype defmethod)))

(def core-defs-clj-classes 
  (set '(defrecord deftype)))


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


(defn- margin-bottom-str [user-opts]
  (if-let [mb (:margin-bottom user-opts)]
    (if (or (zero? mb) (pos-int? mb))
      (string/join (repeat mb "\n"))
      "\n")
    "\n"))


(defn- user-label-or-form!
  [{:keys [qf template label mll?]
    :as opts}]
  (let [indent-spaces
        (or (some-> @state/margin-inline-start
                    util/spaces)
            nil)

        label
        (when (contains?
               #{[:form-or-label :file-info :result]
                 [:form-or-label :result]
                 [:form-or-label :file-info]}
               template)
          (when label
            (let [label (if mll?
                          (string/join
                           "\n"
                           (map
                            #(str (tag/tag-entity!
                                   (str indent-spaces %)
                                   :eval-label)
                                  (tag/tag-reset!))
                            (string/split label #"\n")))
                          (tag/tag-entity! (util/shortened label 25)
                                           :eval-label))]
              (str indent-spaces label))))
        form  (when-not label
                (when qf
                  (reset! state/formatting-form-to-be-evaled?
                          true)
                  (let [shortened (tag/tag-entity! 
                                   (util/shortened qf 25)
                                   :eval-form) 
                        ret       shortened]
                    (reset! state/formatting-form-to-be-evaled?
                            false)
                    (str indent-spaces ret))))]
    [label form]))


(defn- file-info
  [{:keys [form-meta
           template
           ns-str]}]
  (let [file-info* (when (contains?
                          #{[:form-or-label :file-info :result]
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
          (tag/tag-entity! " \n" :result-header))

        fmt           
        (when-not (or log?
                      threading?
                      (= template [:form-or-label :file-info]))
          (if (and user-print-fn
                   (contains? #{pr pr-str prn prn-str print println} 
                              user-print-fn))
            (with-out-str (user-print-fn source))
            (serialize/formatted* source)))

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
      :file-info* file-info*}))

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
           threading?]
    :as   opts}] 

  ;; Test error messaging
  ;; (when (= "force error" qf)
  ;;   #?(:cljs
  ;;      (throw (js/Error. "Forced error from fireworks.core/formatted"))
  ;;      :clj
  ;;      (+ 1 true)))

  (let [user-print-fn
        (:print-with user-opts)

        label
        (if (coll? label)
          (with-out-str (pprint label))
          label)

        mll?
        (when (string? label) (re-find #"\n" label))

        file-info-first?
        (or (contains?
             #{[:file-info :result]}
             template)
            (and label
                 (contains? #{[:form-or-label :file-info]
                              [:form-or-label :file-info :result]}
                            template)
                 (or mll?
                     (< 44 (count (str label))))))

        {:keys [fmt+ fmt file-info*]}
        (fmt+ (keyed [file-info-first? 
                      user-print-fn
                      threading?
                      template
                      source
                      label
                      opts
                      mll?
                      log?]))]
    
    ;; TODO Change this to (= mode :data)
    (if p-data?
      ;; If p-data, return a map of preformatted values
      (merge
       {:ns-str        ns-str
        :quoted-form   qf
        :file-info-str file-info*}
       form-meta
       {:formatted+ (merge {:string fmt+}
                           #?(:cljs {:css-styles @state/styles}))
        :formatted  (merge {:string fmt}
                           #?(:cljs {:css-styles (subvec
                                                  @state/styles 
                                                  (count @state/styles))}))})

      ;; Else if print-and-return fns, return printing opts
      {:fmt           fmt+
       :log?          log?
       ;; Trailing line for read-ability.
       ;; Single line is default, can be overridden at call-site.
       ;; If using :log or :pp, this should be zero, and margin-bottom
       ;; needs to be set in fireworks.core/_log or fireworks.core/_pp
       :margin-bottom (if (contains? #{:log :pp} mode)
                        "\n"
                        (margin-bottom-str user-opts))})))


#?(:cljs 
   (defn- js-print [opts]
     (let [js-arr (into-array (concat [(:fmt opts)] @state/styles))]
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
  (let [new-val (k opts)
        valid?  (some-> k
                        config/options
                        :spec
                        (s/valid? new-val))]
    (if valid?
      (swap! state/config assoc k new-val)
      (messaging/bad-option-value-warning
       (let [m                (k config/options)
             {:keys [line 
                     column]} (:form-meta opts)]
         (merge m 
                (keyed [k line column])
                {:header (:fw-fnsym opts)
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
   They will be reset only if they are different from current value."
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
  (let [opts-to-reset (let [ks  (opts-to-reset user-opts)]
                        (doseq [k ks]
                          (reset-user-opt! k opts))
                        ks)]
    (when (or (diff-call-site-theme-option-keys opts-to-reset)
              (diff-from-merged-user-config config-before))
      (let [{:keys [with-style-maps
                    with-serialized-style-maps]} (state/merged-theme* :reset)]
        (reset! state/merged-theme
                with-serialized-style-maps)
        (reset! state/merged-theme-with-unserialized-style-maps
                with-style-maps)))))


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

    (reset! state/styles [])
    (reset! state/*formatting-meta-level 0)
    (reset! state/rainbow-level 0)
    (reset! state/top-level-value-is-sev? false)
    (reset! messaging/warnings-and-errors [])
    ;; Resetting config to user's config.edn merged with defaults
    (reset! state/config (state/config*))
    ;; Reset config & potentially reset/remerging the theme
    (reset-config+theme! config-before user-opts opts))
  
  ;; Reset the highlight state.
  ;; It may pull hightlight style from merged theme.
  (some->> find-vals
           state/highlight-style
           (reset! state/highlight)))
          


;                         PPPPPPPPPPPPPPPPP
;                         P::::::::::::::::P
;                         P::::::PPPPPP:::::P
;                         PP:::::P     P:::::P
;                           P::::P     P:::::P
;                           P::::P     P:::::P
;                           P::::PPPPPP:::::P
;                           P:::::::::::::PP
;                           P::::PPPPPPPPP
;                           P::::P
;                           P::::P
;                           P::::P
;                         PP::::::PP
;                         P::::::::P
;                         P::::::::P
;                         PPPPPPPPPP


(defn- print-formatted
  ([x]
   (print-formatted x nil))
  ([{:keys [fmt log? err err-opts] :as x} f]
   (if (instance? fireworks.messaging.FireworksThrowable x)
     (let [{:keys [line column file]} (:form-meta err-opts)
           ns-str                     (:ns-str err-opts)] 
       (messaging/caught-exception err
                                   {:type   :error
                                    :form   (:quoted-fw-form err-opts)
                                    :header (or (some-> err-opts :header)
                                                (some-> err-opts :fw-fnsym))
                                    :line   line
                                    :column column
                                    :file   (or file ns-str)}))
     #?(:cljs
        (f x)
        :clj
        (do 
          (some-> fmt print)
          ((if log? print println) (:margin-bottom x)))))))

(defn ^{:public true}
  _pp
  "Called internally."
  [{:keys [user-opts]} x]
  #?(:cljs
     (do (pprint x)
         x)
     :clj
     (do
       (pprint x)
       ;; Extra line after pprint result
       (print (margin-bottom-str user-opts))
       x)))

(defn ^{:public true} _log 
  [{:keys [user-opts]} x]
  #?(:cljs
     (do (js/console.log x)
         x)
     :clj
     (do
       (pprint x)
       ;; Extra line after pprint result
       (print (margin-bottom-str user-opts))
       x)))

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
               opts)]
    (reset-state! opts)
    (let [
          ;; In cljs, if val is data structure but not cljs data structure
          ;; TODO - Mabye add tag-map to the opts to save a call in truncate
          native-logging 
          #?(:cljs (let [{:keys [coll-type? carries-meta? tag]
                          :as   tag-map} 
                         ;; Maybe add :exclude-all-extra-info? true
                         ;; override opt to lasertag?
                         (lasertag/tag-map 
                          x
                          {:include-function-info?           false
                           :include-js-built-in-object-info? false})]
                     (when (and coll-type?
                                (not carries-meta?)
                                (not= tag :cljs.core/Atom))
                       {:log? true}))
             :clj nil)
          opts          (merge opts native-logging)
          printing-opts (try (formatted x opts)
                             (catch #?(:cljs js/Object :clj Exception)
                                    e
                               (messaging/->FireworksThrowable
                                e
                                x
                                (assoc opts
                                       :header
                                       "fireworks.core/formatted"))))
          return-result?
          (when-not (= (:template opts)
                       [:form-or-label :file-info])
            x)]

      ;; TODO Change this to (= (:mode opts) :data)
      (if (:p-data? opts) 
        printing-opts
        (do 
          (print-formatted printing-opts #?(:cljs js-print))

          ;; Fireworks formatting and printing  does not happen when:
          ;; - Value being printed is non-cljs or non-clj data-structure
          ;; - fireworks.core/?log or fireworks.core/?log- is used
          ;; - fireworks.core/?pp or fireworks.core/?pp- is used
          
          (when (and (not (:fw/log? opts)) (:log? opts))
            #?(:cljs (js/console.log x)
               :clj (fireworks.core/pprint x)))

          (reset! state/formatting-form-to-be-evaled? false)
          (when return-result? x)))))))


(defn ^{:public true}
  _p2 
  "Internal runtime dispatch target for fireworks macros.

   Pretty-prints the value with syntax coloring.
   Takes an optional leading argument (custom label or options map).
   Returns the value."

  [opts x]
  (reset-state! opts)
  (let [
        ;; In cljs, if val is data structure but not cljs data structure
        ;; TODO - Mabye add tag-map to the opts to save a call in truncate
        native-logging
        #?(:cljs (let [{:keys [coll-type? carries-meta? tag]
                        :as   tag-map} 
                       ;; Maybe add {:exclude-all-extra-info? true}
                       ;; as an override opt to lasertag?
                       (lasertag/tag-map 
                        x
                        {:include-function-info?           false
                         :include-js-built-in-object-info? false})]
                   (when (and coll-type?
                              (not carries-meta?)
                              (not= tag :cljs.core/Atom))
                     {:log? true}))
           :clj nil)

        opts           
        (merge opts
               native-logging
               ;; Hack for threading
               ;; TODO - Think about a better way to do it
               (when (some-> opts :label :threading?)
                 {:label (or (some-> opts :label :label)
                             "threading:")}))

        printing-opts  
        (try (formatted x opts)
             (catch #?(:cljs js/Object :clj Exception)
                    e
               (messaging/->FireworksThrowable
                e
                x
                (assoc opts
                       :header
                       "fireworks.core/formatted"))))

        return-result?
        (when-not (= (:template opts)
                     [:form-or-label :file-info])
          x)]

    ;; TODO Change this to (= (:mode opts) :data)
    (if (:p-data? opts) 
      printing-opts
      (do 
        (print-formatted printing-opts #?(:cljs js-print))

          ;; Fireworks formatting and printing of formatted does not happen when:
          ;; - Value being printed is non-cljs or non-clj data-structure
          ;; - fireworks.core/?log or fireworks.core/?log- is used
          ;; - fireworks.core/?pp or fireworks.core/?pp- is used
        
        (when (and (not (:fw/log? opts)) (:log? opts))
          #?(:cljs (js/console.log x)
             :clj (fireworks.core/pprint x)))

        (reset! state/formatting-form-to-be-evaled? false)
        (when return-result? x)))))


(defn- cfg-opts
  "Helper for shaping opts arg to be passed to fireworks.core/_p"
  [{:keys [a] :as m}]
  (let [cfg-opts  (when (map? a) a)
        label     (if cfg-opts (:label cfg-opts) a)
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
                    :user-opts      cfg-opts
                    :quoted-fw-form (list 'quote (:&form m))
                    :fw-fnsym       (some-> m
                                            :&form
                                            (nth 0 nil)
                                            (->> (str "fireworks.core/")))})]
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
  (let [cfg-opts (cfg-opts m)
        defd     (defd (:x m))]
    (keyed [cfg-opts defd])))



(defn cast-var
  "Needs to be public, used at runtime by macros"
  [defd {:keys [ns-str]}]
  (symbol (str "#'" ns-str "/" defd)))

(defn- mode+template [a]
  (let [mode
        (if (contains? #{:label
                         :file
                         :result
                         :comment
                         :data
                         :log
                         :log-
                         :pp
                         :pp-
                         :trace}
                       a)
          a
          nil)

        template
        (get 
         {:label   [:form-or-label :result]
          :file    [:file-info :result]
          :result  [:result]
          :comment [:form-or-label :file-info]
          :data    [:form-or-label :file-info :result]
          :log     [:form-or-label :file-info :result]
          :log-    nil
          :pp      [:form-or-label :file-info :result]
          :pp-     nil
          :trace   [:form-or-label :file-info :result]}
         mode
         [:form-or-label :file-info :result])]
    {:mode     mode
     :template template}))

(defn- ?2-helper
  [{:keys [cfg-opts* mode template label &form x]}]
  (let [defd
        (defd x)

        log?*
        (contains? #{:log :pp} mode)

        quoted-fw-form
        (if (contains? #{:result :log- :pp-} mode)
         (list 'quote nil)
         (list 'quote &form))

        fw-fnsym
        (when-not (contains? #{:result :log- :pp-} mode)
         "fireworks.core/?")

        form-meta
        (when-not (contains? #{:result :log- :pp-} mode)
          (meta &form))

        qf-nil?
        (contains? #{:comment :result} mode)

        cfg-opts
        (merge (dissoc (or cfg-opts* {}) :label)
               {:mode           mode
                :label          label
                :template       template
                :ns-str         (some-> *ns* ns-name str)
                :user-opts      cfg-opts*
                :quoted-fw-form quoted-fw-form
                :fw-fnsym       fw-fnsym}
                (when form-meta
                  {:form-meta form-meta})
                (when log?*
                  {:log?    log?*
                   :fw/log? log?*})
                (when (= mode :data)
                  {:p-data? true}))]
   (keyed [defd x qf-nil? cfg-opts log?*])))
                                                               
                                                               
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
                                                              
          
(defmacro ?> 
  "Passes value to clojure.core/tap> and returns value."
  ([x] `(do (tap> ~x) ~x)))


(declare print-thread)
(declare thread-helper)
(declare threading-sym)

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
  ([a x]
   (let [{:keys [mode template]}
         (mode+template a)]
     (case mode
       :trace
       (let [form-meta (meta &form)]
         (if-let [[thread-sym forms] (threading-sym x)]
           (let [[cfg-opts call]
                 (thread-helper (assoc (keyed [forms
                                               form-meta
                                               thread-sym])
                                       :&form
                                       &form))]
             `(do (fireworks.core/print-thread ~cfg-opts
                                               (quote ~forms)
                                               (str (quote ~thread-sym)))
                  ~call))
           `(do
              (messaging/unable-to-trace
               (merge
                ~form-meta
                {:type :warning
                 :form (quote ~x)}))
              ~x)))

       :log-
       `(do #?(:cljs (js/console.log ~x)
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

             {:keys [log?* defd qf-nil? cfg-opts]}
             (?2-helper (keyed [mode template cfg-opts* label &form x]))]

        ;;  (ff "?, 2-arity, cfg-opts" cfg-opts)
         `(let [cfg-opts# (assoc ~cfg-opts :qf (if ~qf-nil? nil (quote ~x)))
                ret#      (if ~defd (cast-var ~defd ~cfg-opts) ~x)]
            (when ~defd ~x)
            (if ~log?*
              (do 
                (fireworks.core/_p2 cfg-opts# ret#)
                ((if (= ~mode :log)
                   fireworks.core/_log
                   fireworks.core/_pp)
                 ~cfg-opts
                 (if ~defd (cast-var ~defd ~cfg-opts) ~x)))
              (fireworks.core/_p2 cfg-opts# ret#)))))))
   
  ([mode a x]
   (let [{:keys [mode template]}
         (mode+template mode)]
     (case mode
       :trace
       (let [form-meta (meta &form)]
         (if-let [[thread-sym forms] (threading-sym x)]
           (let [[cfg-opts call]
                 (thread-helper (assoc (keyed [forms
                                               form-meta
                                               thread-sym
                                               a])
                                       :&form
                                       &form))]
             `(do (fireworks.core/print-thread ~cfg-opts
                                               (quote ~forms)
                                               (str (quote ~thread-sym)))
                  ~call))
           `(do
              (messaging/unable-to-trace
               (merge
                ~form-meta
                {:type :warning
                 :form (quote ~x)}))
              ~x)))      

       :log-
       `(do #?(:cljs (js/console.log ~x) :clj (fireworks.core/pprint ~x))
            ~x)

       :pp-
       `(do (fireworks.core/pprint ~x)
            ~x)
       (let [cfg-opts*                             
             (when (map? a) a)

             label                                 
             (or (:label cfg-opts*)
                 (cond (= mode :comment) x
                       :else             (when-not cfg-opts* a)))

             {:keys [log?* defd qf-nil? cfg-opts]}
             (?2-helper (keyed [mode template cfg-opts* label &form x]))]

         ;; (ff "?, 3-arity" cfg-opts)
         `(let [cfg-opts# (assoc ~cfg-opts :qf (if ~qf-nil? nil (quote ~x)))
                ret#      (if ~defd (cast-var ~defd ~cfg-opts) ~x)] 
            (when ~defd ~x)
            (if ~log?*
              (do 
                (fireworks.core/_p2 cfg-opts# ret#)
                ((if (= ~mode :log)
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


;; Threading 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO - add cond


(defn ^{:public true}
  print-thread
  [{:keys [label] :as cfg-opts} quoted-forms op]
  (?
   :comment
   (merge cfg-opts
          {:threading? true
           :label      (or label
                           (str "("
                                (string/join
                                 (str "\n" (util/spaces (-> op count (+ 2))))
                                 (cons (symbol (str op
                                                    " "
                                                    (first quoted-forms)))
                                       (rest quoted-forms)))
                                ")"))})))


(defn- thread-helper
  [{:keys [forms form-meta thread-sym user-opts a] :as m}]
  (let [?-call-sym         (if (contains? #{'-> 'some->} thread-sym)
                             'fireworks.core/?flop 
                             'fireworks.core/?)
        user-opts          (when (map? user-opts) user-opts)
        opts               (for [frm forms]
                             (list ?-call-sym
                                   (merge user-opts
                                          {:label               (str frm)
                                           :margin-inline-start 4})))
        fms                (interleave forms opts)
        call               (cons thread-sym fms)
        {:keys [cfg-opts]} (helper2 {:x         nil
                                     :a         a
                                     :&form     (:&form m)
                                     :form-meta form-meta})]
    [cfg-opts call]))


(defn- threading-sym [x]
  (and (list? x)
       (< 1 (count x))
       (let [[sym & forms] x]
         (when (contains? #{'-> '->> 'some-> 'some->>} sym)
           [sym forms]))))


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
        (let [writer (fireworks.pp/count-keeping-writer writer
                                                        {:max-width max-width})]
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
