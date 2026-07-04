(ns ^:dev/always fireworks.ellipsize
(:require
 [clojure.string :as str]
 [fireworks.defs :as defs]
 [fireworks.state :as state]
 [fireworks.specs.config :as specs.config]
 #?(:cljs [fireworks.macros :refer-macros [keyed]])
 #?(:clj [fireworks.macros :refer [keyed]])
 ))

(defn- len
  [x]
  (some-> x str count))

(defn- budge-diff
  [limit a]
  (- (+ (or (len a) 0))
     (or limit 0)))

(defn- ellipsized-char-count 
  [badge s num-chars-dropped]
  (+ (or (some-> badge count) 0)
     (or (count s) 0)
     (or (when num-chars-dropped defs/ellipsis-count) 0)))

(defn- pre-truncate-function-name
  "Potentially shortens, or drops, one or more of the following parts of
   the display value of a function (or function-like object such as a java
   class, JS class, or defmulti):
   - The fully-qualified namespace
   - The function name"
  [limit
   {:keys [fn-ns
           fn-name
           js-built-in-function?
           js-built-in-method-of
           java-lang-class?
           badge
           lambda?
           classname]
    :as   m}]
  (let [result-map      (fn [nm ecc trunc?]
                          {:fn-display-name       nm
                           :drop-ns?              false ;; <- krft?
                           :fn-args               nil   ;; <- krft? 
                           :ellipsized-char-count ecc
                           :truncate-fn-name?     trunc?} )

        ;; Create `budge-diff` partial, which will be used to calculate
        ;; the difference between the length of the fn name (at a given
        ;; stage of shortening) and the current limit.
        
        budge-diff      (partial budge-diff limit)
        

        ;; Construct the function display name with proper js/built-in prefix
        ;; and optional namespace + possibly shortened function args fn-args vector.
        ;; Then use budge-diff to see if it is over budget.
        interop-prefix  (if js-built-in-function? "js/" nil)
        nm              (if js-built-in-function?
                          (str interop-prefix 
                               (some-> js-built-in-method-of
                                       (str "."))
                               fn-name)
                          (str (when (:display-namespaces? @state/config)
                                 (some-> fn-ns
                                         (str (if java-lang-class? "." "/"))))
                               fn-name))
        nm              (if lambda?
                          (str nm 
                               (if (= classname "Function")
                                 defs/lambda-badge
                                 (or (some-> classname
                                             (str/split #"\$fn__")
                                             last
                                             (->> (str "fn__")))
                                     badge)))
                          nm)
        diff            (budge-diff nm)


        ;; If still over budget, truncate (ellipsize) the function name.
        trunc-name?     (pos? diff)
        fn-display-name (if trunc-name? 
                          (->> nm
                               str
                               (drop-last (+ diff defs/ellipsis-count)) 
                               str/join 
                               symbol)
                          nm)
        ;; _               (when lambda? (? m))
        fn-display-name (when-not (str/blank? (str fn-display-name))
                          fn-display-name)


        ;; Finally, calculate the final ellipsized-char-count. This count
        ;; should never exceed the :scalar-max-length, or the
        ;; :map-key-print-length, from @state/config.
        ecc             (ellipsized-char-count badge
                                               (str fn-display-name)
                                               trunc-name?)]

    (result-map fn-display-name ecc trunc-name?)))


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
               str/join)
     :clj (str x)))


(defn- stringified 
  "Stringifies self-evaluating values (scalars). 
   Wraps in appropriate quotes, when appropriate."
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
           num-chars-over
           key?]}]
  (let [num-to-remove
        (+ num-chars-over (or defs/ellipsis-count 0))

        value-is-wrapped-in-some-kind-of-quotes?
        (contains? defs/values-wrapped-in-quotes t)

        ret          
        (if value-is-wrapped-in-some-kind-of-quotes?
          (let [end     (dec (- stringified-len num-to-remove))
                spliced (subs stringified 0 end)]
            (str spliced (get defs/quoting-chars t \")))
          (let [end     (- stringified-len num-to-remove)
                spliced (subs stringified 0 end)]
            spliced)
          )]
    #_(when key? 
        (? (keyed [num-to-remove
                   value-is-wrapped-in-some-kind-of-quotes?
                   ret])))
    ret))


(defn ellipsized
  "Ellipsizes longer-than acceptable self-evaluating values such as strings,
   regexes, keywords, #insts, fns, etc.
   
   Truncation is based on the following:
   - `:scalar-mapkey-max-length `or `:scalar-max-length` from config
   - Optional inline badge length e.g `#js `
   - Optional atom or volatile encapsulation e.g. `Atom<42>`"
  [x 
   {:keys [t 
           key?
           sev?
           depth
           limit
           badge
           map-value?
           inline-badge?
           top-level-sev?
           multi-line-string-line?]
    :as   m}]
  (let [{:keys [scalar-depth-1-max-length
                scalar-result-max-length
                scalar-mapkey-max-length
                scalar-max-length
                truncate?]}
        @state/config

        no-truncation?
        (false? truncate?)

        scalar-max-length
        (if no-truncation?
          specs.config/scalar-max-length
          scalar-max-length)

        scalar-depth-1-max-length
        (if no-truncation?
          specs.config/scalar-max-length
          scalar-depth-1-max-length)

        scalar-result-max-length
        (if no-truncation? 
          specs.config/scalar-max-length 
          scalar-result-max-length)

        scalar-mapkey-max-length
        (if no-truncation? 
          specs.config/scalar-max-length 
          scalar-mapkey-max-length)


        limit
        (if multi-line-string-line?
         scalar-result-max-length
         (if-let [level-k (and (not (or key? map-value?))
                               (cond top-level-sev?
                                     :level-0-sev
                                     (and sev? (< depth 2))
                                     :level-1-sev))]
           (case level-k
             :level-0-sev scalar-result-max-length
             :level-1-sev scalar-depth-1-max-length)
           (max (if key?
                  scalar-mapkey-max-length
                  scalar-max-length)
                (or limit 0))))]
    
    (if (:ellipsized-char-count m)
      x
      (if (contains? #{:function :defmulti :class} t)
        (let [ret (pre-truncate-function-name limit m)]
          ret)
        (let [stringified           (stringified x t m)
              inline-badge-count    (or (when (and badge inline-badge?)
                                          (len badge))
                                        0)
              stringified-len       (count stringified)

              ;; If we add the escape chars to string at an early stage, maybe we can
              ;; remove this
              num-double-quotes     (when (= t :string)
                                      (some->> stringified
                                               (re-seq #"\"")
                                               count
                                               (- 2)
                                               #?(:cljs
                                                  js/Math.abs
                                                  :clj
                                                  Math/abs)))
              stringified-len       (+ stringified-len (or num-double-quotes 0))
              char-len              (+ (or stringified-len 0)
                                       inline-badge-count)
              num-chars-over        (or (- char-len limit) 0)
              exceeds?              (pos? num-chars-over)
              s                     (cond
                                      (= t :nil) "nil"
                                      exceeds?   (ellipsized-sev
                                                  (keyed [t
                                                          stringified
                                                          stringified-len
                                                          num-chars-over
                                                          key?]))
                                      :else       stringified)
              num-chars-dropped     (when exceeds? num-chars-over)
              ellipsized-char-count (if-not exceeds?
                                      char-len
                                      (ellipsized-char-count badge
                                                             s
                                                             num-chars-over))
              ret                   (merge 
                                     (keyed [s
                                             stringified
                                             ellipsized-char-count
                                             exceeds?             
                                             num-chars-dropped]))]



          ;;  (when key?
          ;;    (? stringified)
          ;;    (? (keyed [#_m
          ;;                 t
          ;;                 stringified
          ;;                 stringified-len
          ;;                 num-chars-over
          ;;                 exceeds?
          ;;                 inline-badge-count
          ;;                 s
          ;;                 ellipsized-char-count])))
          ret)))))
