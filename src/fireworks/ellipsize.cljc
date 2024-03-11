(ns ^:dev/always fireworks.ellipsize
(:require
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.pp :refer [?pp]]
   [fireworks.state :as state]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])))

(defn- len
  [x]
  (some-> x str count))

(defn- budge-diff
  [limit atom-wrap-count a b]
  (- (+ (len a)
        (len b)
        atom-wrap-count)
     limit))


(defn- fn-args*
  [fn-args]
  (if (contains? 
       #{:typetag/unknown-function-signature-on-js-built-in-method
         :typetag/unknown-function-signature-on-clj-function
         :typetag/unknown-function-signature-on-java-class
         :typetag/multimethod}
       fn-args)
    defs/mysterious-fn-args
    fn-args))


(defn- ellipsized-char-count 
  [badge s num-chars-dropped]
  (+ (or (some-> badge count) 0)
     (or (count s) 0)
     (or (when num-chars-dropped defs/ellipsis-count) 0)))


(defn- pre-truncate-function-name
  "Potentially shortens, or drops, one or more of the following parts of
   a function's (or function-like object's) display value:
   - The args vec
   - The fully-qualified namespace
   - The function nam "
  [limit
   {:keys [fn-ns
           fn-name
           fn-args
           js-built-in-function?
           js-built-in-method-of
           badge
           lamda?
           atom?]
    :as   m}]
  (let [;; Create `budge-diff` partial, which will be used to calculate
        ;; the difference between the length of the fn name (at a given
        ;; stage of shortening) and the (:value-width-limit @state/config).
        atom-wrap-count (or (when atom? defs/atom-wrap-count) 0)
        budge-diff      (partial budge-diff limit atom-wrap-count)
        

        ;; Construct the fn display name with proper js/built-in prefix
        ;; and optional namespace + possibly shortened fn-args vector.
        ;; Then use budge-diff to see if it is over budget.
        interop-prefix  (if js-built-in-function? "js/" nil)
        nm              (if js-built-in-function?
                          (str interop-prefix 
                               (some-> js-built-in-method-of
                                       (str "."))
                               fn-name)
                          (str (when (:display-namespaces? @state/config)
                                 (some-> fn-ns (str "/")))
                               fn-name))
        fn-args         (fn-args* fn-args)
        diff            (budge-diff nm fn-args)


        ;; If over budget, truncate the fn-args vector and then check
        ;; again to see if still over budget.
        trunc-args?     (pos? diff)
        fn-args         (if (and trunc-args?
                                 (> (count (str fn-args))
                                    (count (str defs/truncated-fn-args))))
                          defs/truncated-fn-args
                          fn-args)
        diff            (budge-diff nm fn-args)


        ;; If still over budget, drop the namespace and check again to
        ;; see if still over budget.
        drop-ns?        (pos? diff)
        nm              (if drop-ns? 
                          (symbol
                           (str interop-prefix
                                (when (some->
                                       js-built-in-method-of
                                       str
                                       count 
                                       (< defs/js-built-in-method-nm-limit))
                                  (str js-built-in-method-of "."))
                                fn-name))
                          nm)
        diff            (budge-diff nm fn-args)


        ;; If still over budget, truncate (ellipsize) the function name.
        trunc-name?     (pos? diff)
        fn-display-name (if trunc-name? 
                          (->> nm
                               str
                               (drop-last (+ diff defs/ellipsis-count)) 
                               string/join 
                               symbol)
                          nm)
        fn-display-name (when-not lamda?
                          (when-not (string/blank? (str fn-display-name))
                            fn-display-name))


        ;; Finally, calculate the final ellipsized-char-count. This count
        ;; should never exceed the :value-width-limit, or the
        ;; :map-key-width-limit, from @state/config.
        ecc             (+ (ellipsized-char-count badge
                                                  (str fn-display-name)
                                                  trunc-name?)
                           (or (count (str fn-args)) 0))]

    (merge (keyed [fn-args fn-display-name drop-ns?])
           {:ellipsized-char-count ecc
            :truncate-fn-name?     trunc-name?
            :truncate-fn-args?     trunc-args?})))


(defn- inst-str
  [x]
  (subs (with-out-str (print x))
        (count "#inst ")))


(defn- qv
  [s* k]
  (let [q (or (k defs/quoting-chars) \")] (str q s* q)))


(defn- regex-display
  [x]
  #?(:cljs (-> x
               str
               (subs 1)
               drop-last
               string/join)
     :clj (str x)))


(defn- stringified 
  "Stringifies self-evaluating values (non-colls). Wraps in appropriate quotes,
   when appropriate."
  [x t m]
  (let [s* (case t
             :nil        "nil"
             :regex      (regex-display x)
             :js/Promise "{}"
             (if (contains? (:all-tags m) :inst)
               (inst-str x)
               ;; for custom js-types
               (str x)))
        s (cond 
            (= t :string)     (qv s* :string)
            (= t :regex)      (str "#" (qv s* :regex))
            (= t :object-key) (qv s* :object-key)
            (= t :uuid)       (qv s* :uuid)
            :else             s*)]
    s))


(defn- ellipsized-sev
  "Removes exact number of chars from self-evaluating values, so that when
   ellipsis chars are added in serialization phase, the resulting string does
   not exceed the value width limit."
  [{:keys [t
           stringified
           stringified-len
           num-chars-over]}]
  (let [num-to-remove
        (+ num-chars-over (or defs/ellipsis-count 0))

        value-is-wrapped-in-some-kind-of-quotes?
        (contains? defs/values-wrapped-in-quotes t)

        ret          
        (if value-is-wrapped-in-some-kind-of-quotes?
          (let [end     (dec (- stringified-len num-to-remove))
                spliced (subs stringified 0 end)]
            (str spliced (get defs/quoting-chars t \")))
          stringified)]
    ret))


(defn ellipsized
  "Ellipsizes longer-than acceptable self-evaluating values such as strings,
   regexes, keywords, #insts, fns, etc. Truncation is based on the following:
   - :mapkey-width-limit or :value-width-limit values from config
   - Optional inline badge length e.g `#js`
   - Optional atom encapsulation e.g. `Atom<42>`"
  [x 
   {:keys [t limit key? badge inline-badge? atom?]
    :as   m}]
  (let [{:keys [mapkey-width-limit
                value-width-limit]}
        @state/config

        limit
        (if @state/top-level-value-is-sev?
          ;; TODO - make this a config value with upper-bound
          ;; maybe call it top-level-scalar-value-length-limit
          ;; maybe change `width-limit*` to length-limit ?
          500
          (max (if key?
                 mapkey-width-limit
                 value-width-limit)
               (or limit 0)))]
    (if (:ellipsized-char-count m)
      x
      (if (contains? #{:function :defmulti :java.lang.Class} t)
        (let [ret (pre-truncate-function-name limit m)]
          ret)
        (let [stringified           (stringified x t m)
              atom-wrap-count       (or (when atom? defs/atom-wrap-count) 0)
              inline-badge-count    (or (when (and badge inline-badge?)
                                          (len badge))
                                        0)
              stringified-len       (count stringified)
              char-len              (+ (or stringified-len 0)
                                       atom-wrap-count
                                       inline-badge-count)
              num-chars-over        (or (- char-len limit) 0)
              exceeds?              (pos? num-chars-over)
              s                     (cond
                                      (= t :nil) "nil"
                                      exceeds?   (ellipsized-sev
                                                  (keyed [t
                                                          stringified
                                                          stringified-len
                                                          num-chars-over]))
                                      :else       stringified)
              num-chars-dropped     (when exceeds? num-chars-over)
              ellipsized-char-count (if-not exceeds?
                                      char-len
                                      (count s))
              ret                   (merge 
                                     (keyed [s
                                             stringified
                                             ellipsized-char-count
                                             exceeds?             
                                             num-chars-dropped]))]

      ;;  (when-not @state/formatting-form-to-be-evaled?
      ;;    (?pp stringified)
      ;;    (?pp (keyed [#_m
      ;;                 t
      ;;                 stringified
      ;;                 stringified-len
      ;;                 num-chars-over
      ;;                 inline-badge-count
      ;;                 s
      ;;                 atom-wrap-count
      ;;                 ellipsized-char-count])))
          ret)))))
