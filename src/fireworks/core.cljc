;; Taps and returns the result, no printing
;; ?>
;; !?> (untap)

;; Fireworks formatting
;; ?         eval + file-info + result
;; ?-        file-info + result
;; ?--       result
;; !?        eval + file-info + result (silenced)
;; !?-       file-info + result        (silenced)
;; !?--      result                    (silenced)

;; JS formatting
;; ?log       js/console.log
;; ?log-      js/console.log
;; ?log--     js/console.log
;; !?log      js/console.log (silenced)
;; !?log-     js/console.log (silenced)
;; !?log--    js/console.log (silenced)

;; PP formatting
;; ?pp        pprint 
;; ?pp-       pprint 
;; ?pp--      pprint 
;; !?pp       pprint (silenced)
;; !?pp-      pprint (silenced)
;; !?pp--     pprint (silenced)

;; These print-and-return macros print using core printing fns and return the result
;; ?println   println
;; ?print     print
;; ?prn       prn
;; ?pr        pr
;; !?println  println  (silenced)
;; !?print    print    (silenced)
;; !?prn      prn      (silenced)
;; !?pr       pr       (silenced)


;; Remove old truncate code

;; Fix meta printing on colls (string quotes now)

;; Fix color styling on eval form and meta - All themes

;; look at these bugs
;; (??? (new (.-Color js/window) "hwb" #js[60 30 40]))

;; IndexedSeq showing up as js/Iterable
;; Should be like any other seq
;; check if in babashka

;; Next

;; TODO - Try to eliminate meta-map entries like :js-map-like?, which shadow stuff in :all-tags

;; TODO - Try to eliminate some of the redundant passing keys around in serialize


(ns fireworks.core
  (:require
   [fireworks.pp :as fireworks.pp :refer [?pp] :rename {?pp ff}]
   [clojure.walk :as walk]
   [clojure.set :as set]
   [clojure.data :as data]
   [fireworks.defs :as defs]
   [fireworks.messaging :as messaging]
   [fireworks.profile :as profile]
   [fireworks.printers :as printers]
   [fireworks.serialize :as serialize]
   [fireworks.state :as state]
   [fireworks.tag :as tag]
   [fireworks.truncate :as truncate]
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
            [fireworks.core :refer [?> ? !? ?println]])))

(declare pprint)

(def core-defs 
  (set '(def defn defrecord defstruct defprotocol defmulti deftype defmethod)))

(def core-defs-clj-classes 
  (set '(defrecord deftype)))

(defn formatted*
  ([source]
   (formatted* source nil))
  ([source opts]
   (let [truncated      (truncate/truncate {:depth 0} source)
         ;;             _ (js/console.log truncated)
         ;;             _ (?pp 'truncated (meta truncated))

         custom-printed truncated

         ;; Come back to this custom printing jazz later
         ;;  custom-printed (if (:evaled-form? opts)
         ;;                   truncated
         ;;                   (let [ret (walk/postwalk printers/custom truncated)]
         ;;                     (when (some-> ret meta :fw/truncated :sev?)
         ;;                       (reset! state/top-level-value-is-sev? true))
         ;;                     ret))

         profiled       (walk/prewalk profile/profile custom-printed)

         ;;             _ (js/console.log profiled)
         serialized     (serialize/serialized profiled)
         len            (-> profiled meta :str-len-with-badge)]
     [serialized len])))



;;                               ooo OOO OOO ooo
;;                           oOO                 OOo
;;                       oOO                         OOo
;;                    oOO                               OOo
;;                  oOO                                   OOo
;;                oOO                                       OOo
;;               oOO                                         OOo
;;              oOO                                           OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;              oOO                                           OOo
;;               oOO                                         OOo
;;                oOO                                       OOo
;;                  oOO                                   OOo
;;                    oO                                OOo
;;                       oOO                         OOo
;;                           oOO                 OOo
;;                               ooo OOO OOO ooo



(defn user-label-or-form!
  [{:keys [qf template label]}]
  (let [label (when (= template [:form-or-label :file-info :result])
                (some-> label (tag/tag-entity! :comment)))
        label #?(:cljs label :clj (str label "  "))
        form  (when-not label
                (when qf
                  (reset! state/formatting-form-to-be-evaled?
                          true)
                  (let [shortened (tag/tag-entity! 
                                   (messaging/shortened qf 25)
                                   :eval-form) 
                        ret       shortened]
                    (reset! state/formatting-form-to-be-evaled?
                            false)
                    ret)))]
    [label form]))

(defn formatted
  "Formatted log with file-info, form/comment, fat-arrow and syntax-colored
   pretty-printed result. Used by fireworks.core/? macro."
  [source
   {:keys [form-meta
           qf
           template
           log?
           p-data?
           ns-str]
    :as   opts}] 
  (let [[label form]  (user-label-or-form! opts)
        file-info*    (when (contains?
                             #{[:form-or-label :file-info :result]
                               [:file-info :result]}
                             template)
                        (when-let [{ln  :line
                                    col :column} form-meta]
                          (str ns-str ":" ln ":" col)))
        file-info     (some-> file-info* (tag/tag-entity! :file-info))
        result-header (when-not (or (= template [:result])
                                    log?)
                        (tag/tag-entity! " \n" :result-header))
        [fmt _]       (when-not log? (formatted* source))
        fmt+          (str (or label form)
                           file-info
                           result-header
                           fmt)]
    (if p-data?
      (merge
       {:ns-str        ns-str
        :file-info-str file-info*
        :quoted-form   qf}
       form-meta
       {:formatted+ (merge {:string fmt+}
                           #?(:cljs {:css-styles @state/styles}))
        :formatted  (merge {:string fmt}
                           #?(:cljs {:css-styles (subvec
                                                  @state/styles 
                                                  (count @state/styles))}))})
      {:fmt fmt+})))


#?(:cljs 
   (defn- js-print [opts]
     (let [js-arr (into-array (concat [(:fmt opts)] @state/styles))]
       (.apply (.-log  js/console)
               js/console
               js-arr))))


(defn- reset-user-opt!
  "Validates user option override from call site and updates config."
  [k opts]
  (let [new-val (k opts)
        valid?  (some-> k
                        config/options
                        :spec
                        (s/valid? new-val))]

    #_(when (contains? #{:theme} #_#{:enable-terminal-italics? :display-metadata?} k)
      (println "Current value of" k "is" (k @state/config))
      (println "New value of" k "is valid?:" valid?))
    (when valid?
      (swap! state/config assoc k new-val)
      #_(println "Current value of" k "after swap! is" (k @state/config)))))


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
    (reset! state/styles [])
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
          



;;                               ooo OOO OOO ooo
;;                           oOO                 OOo
;;                       oOO                         OOo
;;                    oOO                               OOo
;;                  oOO                                   OOo
;;                oOO                                       OOo
;;               oOO                                         OOo
;;              oOO                                           OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;              oOO                                           OOo
;;               oOO                                         OOo
;;                oOO                                       OOo
;;                  oOO                                   OOo
;;                    oO                                OOo
;;                       oOO                         OOo
;;                           oOO                 OOo
;;                               ooo OOO OOO ooo



(defn _p 
  "Internal runtime dispatch target for fireworks.core/? and fireworks.core/p.
   Needs to be a public function because fireworks.core/? is a macro.

   Pretty-prints the value with syntax coloring.
   Takes an optional leading argument (custom label or options map).
   Returns the value."

  ([opts x]
   (_p nil opts x))

  ([a opts x]
  
  (let [opts (if (map? a) (merge (dissoc opts x :label) a) opts)]
    (reset-state! opts)
    (let [
          ;; In cljs, if val is data structure but not cljs data structure
          ;; TODO - You could add tag-map to the opts to save a call in truncate
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
             ;; TODO - Add support for native logging in clj
             :clj nil)
          opts          (merge opts native-logging)
          printing-opts (try (formatted x opts)
                             (catch #?(:cljs
                                       js/Object
                                       :clj Exception)
                                    e
                               (messaging/->FireworksThrowable e)))]
      (if (:p-data? opts) 
        printing-opts
        (do 
          (messaging/print-formatted printing-opts
                                     #?(:cljs js-print))

          ;; Fireworks formatting does not happen when:
          ;; - Value being printed is non-cljs or non-clj data-structure
          ;; - fireworks.core/?log is used
          (when (and (not (:fw/log? opts))
                     (:log? opts))
            #?(:cljs (js/console.log x)
               :clj (fireworks.core/pprint x)))

          (reset! state/formatting-form-to-be-evaled?
                  false)
          #_(?pp @state/styles)
          x))))))


(defn- cfg-opts
  "Helper for shaping opts arg to be passed to fireworks.core/_p"
  [{:keys [a] :as m}]
  (let [cfg-opts  (when (map? a) a)
        label     (if cfg-opts (:label cfg-opts) a)
        cfg-opts  (merge (dissoc (or cfg-opts {}) :label)
                         ;; maybe don't need the select-keys, just use m
                         (select-keys m [:template
                                         :log?
                                         :fw/log?
                                         :p-data?
                                         :form-meta] )
                         {:ns-str    (some-> *ns* ns-name str)
                          :label     label
                          :user-opts cfg-opts})]
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
  [defd {:keys [ns-str]}]
  (symbol (str "#'" ns-str "/" defd)))




;;                               ooo OOO OOO ooo
;;                           oOO                 OOo
;;                       oOO                         OOo
;;                    oOO                               OOo
;;                  oOO                                   OOo
;;                oOO                                       OOo
;;               oOO                                         OOo
;;              oOO                                           OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;              oOO                                           OOo
;;               oOO                                         OOo
;;                oOO                                       OOo
;;                  oOO                                   OOo
;;                    oO                                OOo
;;                       oOO                         OOo
;;                           oOO                 OOo
;;                               ooo OOO OOO ooo


(defn ?--
  "Prints the value. The value is formatted with fireworks.core/_p.
   Returns the value."
  ([])
  ([x]
   (?-- nil x))
  ([a x]
   (_p a (cfg-opts {:a a :template [:result]}) x)))

(defmacro ?-
  "Prints the namespace info, and then the value.

   The form (or optional label) and value are
   formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}   
         (helper2 {:x         x
                   :template  [:file-info :result]
                   :form-meta (meta &form)})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p ~cfg-opts
                             (cast-var ~defd ~cfg-opts)))
       `(fireworks.core/_p ~cfg-opts
                           ~x))))

  ([a x]
   (let [{:keys [cfg-opts defd]}  
         (helper2 {:a         a
                   :template  [:file-info :result]
                   :x         x
                   :form-meta (meta &form)})]
     (if
      defd 
       `(do 
          ~x
          (fireworks.core/_p ~a
                             ~cfg-opts
                             (cast-var ~defd ~cfg-opts)))
       `(fireworks.core/_p ~a
                           ~cfg-opts
                           ~x)))))

(defmacro ?
  "Prints the form (or user-supplied label), the namespace info,
   and then the value.
   
   The form (or optional label) and value are
   formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:form-or-label :file-info :result]

                   :form-meta (meta &form)})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                           ~x))))

  ([a x]
   (let [{:keys [cfg-opts defd]}   
         (helper2 {:a         a
                   :template  [:form-or-label :file-info :result]
                   :x         x
                   :form-meta (meta &form)})]
     (if
      defd 
       `(do 
          ~x
          (fireworks.core/_p ~a
                             (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(fireworks.core/_p ~a
                           (assoc ~cfg-opts :qf (quote ~x))
                           ~x)))))


(defmacro ?log
  "Prints the form (or user-supplied label), the namespace info,
   and then logs the value using console.log or pp/pprint
   
   The form (or optional label) and value are
   formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:form-or-label :file-info :result]
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x)))))

  ([a x]
   (let [{:keys [cfg-opts defd]}   
         (helper2 {:a         a
                   :template  [:form-or-label :file-info :result]
                   :x         x
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if
      defd 
       `(do 
          ~x
          (fireworks.core/_p ~a
                             (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p ~a
                               (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x))))))



(defmacro ?log-
  "Prints the namespace info,
   and then logs the value using console.log or pp/pprint
   
   The form (or optional label) and value are
   formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:file-info :result]
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x)))))

  ([a x]
   (let [{:keys [cfg-opts defd]}   
         (helper2 {:a         a
                   :template  [:file-info :result]
                   :x         x
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if
      defd 
       `(do 
          ~x
          (fireworks.core/_p ~a
                             (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p ~a
                               (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x))))))


(defmacro ?log--
  "Prints the namespace info,
   and then logs the value using console.log or pp/pprint
   
   The form (or optional label) and value are
   formatted with fireworks.core/_p.
   
   Returns the value.
   
   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned."

  ([])

  ([x]
   (let [{:keys [cfg-opts defd]}
         (helper2 {:x         x
                   :template  [:result]
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x)))))

  ([a x]
   (let [{:keys [cfg-opts defd]}   
         (helper2 {:a         a
                   :template  [:result]
                   :x         x
                   :form-meta (meta &form)
                   :log?      true
                   :fw/log?   true})]
     (if
      defd 
       `(do 
          ~x
          (fireworks.core/_p ~a
                             (assoc ~cfg-opts :qf (quote ~x))
                             (cast-var ~defd ~cfg-opts)))
       `(do (fireworks.core/_p ~a
                               (assoc ~cfg-opts :qf (quote ~x))
                               ~x)
            (js/console.log ~x))))))


(defmacro p-data
  "Formats the namespace info, then the form (or user-supplied label),
   and then the value. The form (or optional label) and value are
   formatted with fireworks.core/p.

   If value is a list whose first element is a member of
   fireworks.core/core-defs, the value gets evaluated first,
   then the quoted var is printed and returned.

   Returns a map of data with the following shape:

   {:formatted     {:string \"...\" :css-styles [...]}
    :formatted+    {:string \"...\" :css-styles [...]}
    :file          \"sandbox/browser.cljs\"
    :end-column    32
    :ns-str        \"sandbox.browser\"
    :file-info-str \"sandbox.browser:119:22\"
    :column        22
    :line          119
    :end-line      119
    :quoted-value  \"...\"}
   
   The entries [:formatted :string], [:formatted+ :string], and [:quoted-value]
   represent the syntax-colored values. The tokens within these strings are
   wrapped with `%c` escaped codes for browser dev consoles, or sgr codes,
   for terminal emulators."

  ([])

  ([x]
   (let [{:keys [cfg-opts
                 defd]}   (helper2 {:p-data?   true
                                    :x         x
                                    :template  [:form-or-label :file-info :result]
                                    :form-meta (meta &form)})]
     (if defd
       `(do 
          ~x
          (fireworks.core/_p
           (assoc ~cfg-opts :qf (quote ~x))
           (var ~defd)))

       `(fireworks.core/_p 
         (assoc ~cfg-opts :qf (quote ~x))
         ~x))))

  ([a x]
   (let [{:keys [cfg-opts
                 defd]}   (helper2 {:p-data?   true
                                    :a         a
                                    :x         x
                                    :template  [:form-or-label :file-info :result]
                                    :form-meta (meta &form)})]
     (if defd 
       ;; If value is a list whose first element is a member of
       ;; fireworks.core/core-defs, it needs special treatment.
       `(do 
          ~x
          (fireworks.core/_p
           ~a
           (assoc ~cfg-opts :qf (quote ~x))
           (var ~defd)))

       ;; Otherwise, just print the value and return
       `(fireworks.core/_p
         ~a
         (assoc ~cfg-opts :qf (quote ~x))
         ~x)))))




;;                               ooo OOO OOO ooo
;;                           oOO                 OOo
;;                       oOO                         OOo
;;                    oOO                               OOo
;;                  oOO                                   OOo
;;                oOO                                       OOo
;;               oOO                                         OOo
;;              oOO                                           OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;              oOO                                           OOo
;;               oOO                                         OOo
;;                oOO                                       OOo
;;                  oOO                                   OOo
;;                    oO                                OOo
;;                       oOO                         OOo
;;                           oOO                 OOo
;;                               ooo OOO OOO ooo






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



;; ?pp and supporting ns-str are add-on macros for basic print-and-return fn 
;; Intended to be used for development of fireworks itself.
(defn- ns-str
  [form-meta]
  (let [{:keys [line column]} form-meta
        ns-str                (some-> *ns*
                                      ns-name
                                      str
                                      (str ":" line ":" column))
        ns-str                (str "\033[3;34;m" ns-str "\033[0m")]
    ns-str))


;; Dupe this as ?pp* - uses no formatting stuff, suitable for use in fireworks itself.
;; New version should use same formatting convention for label/form and file info as ?.
;; Then add ?pp- and ?pp--
;; Then activate !?pp- and !?pp--
(defmacro ?pp 
  ([x]
   (let [ns-str (ns-str (meta &form))]
     `(do
        (println
         (str ~ns-str
              "\n"
              (with-out-str (fireworks.pp/pprint ~x))))
        ~x)))
  ([label x]
   (let [label (or (:label label) label)
         ns-str (ns-str (meta &form))]
     `(do
        (println
         (str ~ns-str
              "\n"
              ~label
              "\n"
              (with-out-str (fireworks.pp/pprint ~x))))
        ~x))))


(defn- ?p* 
  ([label ret]
   (?p* label ret nil))
  ([label ret f]
   [(< (:non-coll-length-limit @state/config)
       (+ (or (-> label str count) 0)
          (or (-> ret str count) 0)))
    #?(:cljs (or f js/console.log) :clj (or f fireworks.core/pprint))]))


(defn ?> 
  "Passes value to clojure.core/tap> and returns value."
  ([x]
   (do (tap> x)
       x)))


(defmacro ?println
  "Prints the value using clojure.core/println`, and then
   returns the value."
  ([x]
   `(do (clojure.core/println ~x)
        ~x))
  ([label x]
   ;; TODO use label style from theme
   `(let [label#    (or (:label ~label) ~label)
          [nl?# f#] (fireworks.core/?p* ~label ~x clojure.core/println)]
      (if label#
        (if nl?#
          (f# (str label# "\n") ~x)
          (f# label# ~x))
        (f# ~x))
      ~x)))


(defmacro ?print
  "Prints the value using clojure.core/println`, and then
   returns the value."
  ([x]
   `(do
      (clojure.core/print ~x)
      ~x))
  ([label x]
   ;; TODO use label style from theme
   `(let [label#    (or (:label ~label) ~label)
          [nl?# f#] (fireworks.core/?p* ~label ~x clojure.core/print)]
      (if label#
        (if nl?#
          (f# (str label# "\n") ~x)
          (f# label# ~x))
        (f# ~x))
      ~x)))


(defmacro ?prn
  "Prints the value using clojure.core/println`, and then
   returns the value."
  ([x]
   `(do (clojure.core/prn ~x)
        ~x))
  ([label x]
   ;; TODO use label style from theme
   `(let [label#    (or (:label ~label) ~label)
          [nl?# f#] (fireworks.core/?p* ~label ~x clojure.core/prn)]
      (if label#
        (if nl?#
          (f# (str label# "\n") ~x)
          (f# label# ~x))
        (f# ~x))
      ~x)))


(defmacro ?pr
  "Prints the value using clojure.core/println`, and then
   returns the value."
  ([x]
   `(do (clojure.core/pr ~x)
        ~x))
  ([label x]
   ;; TODO use label style from theme
   `(let [label#    (or (:label ~label) ~label)
          [nl?# f#] (fireworks.core/?p* ~label ~x clojure.core/pr)]
      (if label#
        (if nl?#
          (f# (str label# "\n") ~x)
          (f# label# ~x))
        (f# ~x))
      ~x)))




;; Silencers

(defn !?*
  "A no-op function which returns the value. Intended to temporarily silence the
   printing of a form while still keeping it wrapped."
  ([])
  ([x] x)
  ([_ x] x))

(def !?> !?*)

(def !? !?*)
(def !?- !?*)
(def !?-- !?*)

(def !?log !?*)
(def !?log- !?*)
(def !?log-- !?*)

(def !?pp !?*)
;; (def !pp- !?*)
;; (def !pp-- !?*)

(def !?pr      !?*) 
(def !?prn     !?*) 
(def !?print   !?*) 
(def !?println !?*) 
