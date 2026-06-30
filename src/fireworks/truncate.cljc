(ns ^:dev/always fireworks.truncate
  (:require
  ;;  #?(:cljs [fireworks.debug :refer-macros [?]])
  ;;  #?(:clj [fireworks.debug :refer [?]])
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [clojure.string :as string]
   [fireworks.ellipsize :as ellipsize]
   [fireworks.order :refer [seq->sorted-map]]
   [fireworks.profile :as profile]
   [fireworks.specs.config :as specs.config]
   [fireworks.state :as state]
   [fireworks.util :as util :refer [maybe->]]
   [lasertag.core]
   [lasertag.cached]))

;; The following set of cljs functions optimizes the printing of js objects.
;; This only applies when the js object is nested within a cljs data structure.
#?(:cljs
   (do 

     (defn- inline-style->map 
       "Converts inline css style string to cljs map"
       [v]
       (as-> v $
         (string/split $ #";")
         (mapv #(reduce (fn [acc x]
                         (conj acc (string/trim x)))
                       []
                       (-> % string/trim (string/split #":"))) 
              $)
         (into {} $)))


     (defn- dom-el-attrs-map
       "Converts html attribute map into cljs map"
       [x]
       (when-let [el x]
         ;; Check for when no attrs
         (when (.hasAttributes el)
           (let [attrs (.-attributes el)]
             (into {}  
                   (for [i    (range (.-length attrs))
                         :let [item (.item attrs i)
                               k (.-name item)
                               v (.-value item)]]
                     [k
                      (cond (= k "style") (inline-style->map v)
                            (= k "class") (vec (string/split v " "))
                            :else         v)]))))))

     
     (defn- prune-synthetic-base-event 
       "Removes uneeded properties from React synthetic object"
       [x]
       (doto x
         (js-delete "view")
         (js-delete "nativeEvent")
         (js-delete "target")))))


;; Maybe this could go in Lasertag?
(defn- cljc-atom?
  [x]
  #?(:cljs (= cljs.core/Atom (type x))
     :clj  (= clojure.lang.Atom (type x))))


;; Potential performance gains:
;; Maybe declare truncate new above this and incorporate into kv
;; Maybe use reduce-kv or transducer to speed this up


#?(:cljs 
   (do
     (defn- resolve-js-obj [{:keys [x resolver instance-properties]}]
       (cond resolver
             (or (resolver x) x)
             (seq instance-properties)
             (->> instance-properties
                  (reduce (fn [m k] 
                            (assoc m
                                   ;; Need a way to attach metadata to this so that it gets the correct syntax coloring token from theme
                                   k 
                                   (let [v (aget x k)]
                                     (if (undefined? v)
                                       "____js/undefined____"
                                       v))))
                          {})
                  clj->js)
             :else
             x))

     (defn- js-kv [{:keys [x uid-entry? js-map?]} acc k]
       (if (some-> k
                   (maybe-> string?)
                   (string/starts-with? "closure_uid_"))
         (do (vreset! uid-entry? true)
             acc)
         (conj acc
               [(with-meta (symbol (str "'" k "':")) {:bling.theme/token :js-object-key})
                (let [v (if js-map? (.get x k) (aget x k))]
                  (if (= v "____js/undefined____")
                    (with-meta (symbol "js/undefined") {:bling.theme/token :nil})
                    v))])))

     (defn- js-obj->array-map 
       [{:keys [print-length 
                depth 
                uid-entry? 
                classname 
                og-x]
         :as   m}]
       ;; TODO should 8 value be tied to user config? 
       (if (> depth 8)
         {}
         (let [js-map?                  (= classname "Map") 
               x                        (resolve-js-obj m)
               keys*                    (if js-map?
                                          (js/Array.from (.keys x)) 
                                          (.keys js/Object x))
               keys                     (take print-length keys*)
               built-in-non-constructor (cond (= og-x js/Math) 'js/Math
                                              (= og-x js/Atomics) 'js/Atomics
                                              (= og-x js/JSON) 'js/JSON 
                                              (= og-x js/Intl) 'js/Intl)]
           (into (if built-in-non-constructor
                   (apply array-map [:fireworks.truncate/js-built-in
                                     built-in-non-constructor])
                   (array-map))
                 (reduce (partial js-kv (assoc m :x x :js-map? js-map?))
                         []
                         keys)))))))

(declare truncate)

(defn- truncate-iterable
  [{:keys [print-length
           array-map?
           transient?
           map-like?
           too-deep?
           coll-size
           depth
           path
           t]
    :as m}
   x]
  (let [;; First we need to check if collection is both not map-like and 
        ;; comprised only of map entries. If this is the case it is most likely
        ;; the result of something like `(vec {:a "a" :b "b"})`, and we need
        ;; to treat all elements in the coll as 2-el vectors (not map-entries),
        ;; in the subsequent nested calls to `truncate`. This is done by passing
        ;; a value of `true` for the :map-entry-in-non-map? option. 
        all-map-entries?
        (and (not map-like?)
             (some->> x
                      seq
                      (take (min 50 print-length))
                      (every? #(-> % map-entry?))))

        ret
        (let [x (if (and (= t :datatype) map-like?)
                  x
                  #_(do
                    ;; (println "og-x" (:og-x m))
                    ;; (println "type x" (type x))
                    ;; (println x)
                    (truncate-datatype-to-map x 10))
                  x)
              taken (->> x (take print-length) vec)
              x-is-set? (set? x)]
          (mapv (fn [i]
                  (let [nth-taken (nth taken i nil)]
                    (truncate {:depth                 (inc depth)
                               :path                  (if (not map-like?)
                                                        (conj path (if x-is-set?
                                                                     nth-taken
                                                                     i))
                                                        path)
                               :map-entry-in-non-map? all-map-entries?}
                              nth-taken)))
                (range (count taken))))]
    (if map-like?
      (if (or (when (number? coll-size)
                (zero? coll-size))
              transient?)  ; treat transient maps as empty map
        ret
        ;; If map is too-deep?, return empty map
        (if too-deep?
          {}
          (seq->sorted-map ret array-map?)))
      ret)))


(defn- new-coll-info
  [{:keys [uid-entry? coll-size all-tags map-like?]}
   coll]
  ;; TODO maybe-change binding name to truncated-coll-size -> coll-size?
  (let [truncated-coll-size (count coll)
        coll-size-adjust    (- (if (number? coll-size) coll-size 0)
                               (if @uid-entry? 1 0))]
   (merge (keyed [truncated-coll-size coll-size-adjust])
          {:js-typed-array? (contains? all-tags :js-typed-array)
           :js-map-like?    (or (contains? all-tags :js-object)
                                (contains? all-tags :js-map)
                                (contains? all-tags :js-map-like-object))
           :js-set?         (contains? all-tags :js-set)
           :num-dropped     (some->> truncated-coll-size (- coll-size-adjust))}
          (when map-like?
            {:sorted-map-keys (mapv #(nth % 0 nil) coll)}))))


(defn- truncated-coll
  [m x]
  
  #?(:cljs
     (if (satisfies? ISeqable x)
       (truncate-iterable m x)
       (let [{:keys [t
                     all-tags
                     dom-node-type
                     path
                     depth
                     js-array?
                     coll-size
                     og-x
                     print-length]}
             m]
         (cond
           dom-node-type
           (truncate {:depth (inc depth)
                      :path  path}
                     (dom-el-attrs-map x))

           (or js-array? (contains? all-tags :js-typed-array))
           (mapv 
            (fn [i]
              (truncate {:depth (inc depth)
                         :path  path}
                        (aget x i)))
            (range (min print-length coll-size)))

           (contains? all-tags :js-set)
           (->> x
                js/Array.from
                (take print-length)
                (mapv (partial truncate {:depth (inc depth)
                                         :path  path})))

           :else
           ;; This is where js objects or js maps get turned into cljs maps
           ;; for printing
           (do 
             (when (= t :SyntheticBaseEvent) (prune-synthetic-base-event x))
             (->> m 
                  js-obj->array-map
                  ;; truncate call
                  (mapv (partial truncate {:depth (inc depth)
                                           :path  path}))
                  (into {}))))))
     :clj
     (truncate-iterable m x)))

(defn- truncated-val-in-meta [x]
  (if (coll? x)
    (let [s (cond (map? x)
                  "{...}"
                  (set? x)
                  "#{...}"
                  (seq? x)
                  "(...)"
                  :else
                  "[...]")]
      (symbol s))
    (if (or (string? x) (keyword? x) (number? x)) 
      (if (-> x str count (> 10))
        (let [s (-> x str (subs 0 7) (str "..."))]
          (symbol (if (string? x) (str "\"" s "\"") s)))
        x)
      x)))

(defn- value-meta [x]
  (when-let [m (meta x)] 
    (when-not (and (list? x) (= '(:line :column) (keys m)))
      (if-not (:multi-line-metadata? @state/config)
        (let [num-dropped (when (< 1 (count m)) (-> m count dec))]
          (if num-dropped
            (let [[k v]          (->> m first)
                  k              (truncated-val-in-meta k)
                  v              (truncated-val-in-meta v)
                  first-kv       [k v]
                  dropped-key    (symbol "")
                  dropped-syntax (symbol (str "...+" num-dropped))]
              (apply array-map 
                     (conj first-kv
                           dropped-key
                           dropped-syntax)))
            m))
        m))))


(defn- resolve-print-length []
  (if (false? (:truncate? @state/config))
    specs.config/print-length
    (:print-length @state/config)))


(defn- reify-if-transient [x tag-map]
  (if (:transient? tag-map)
    (let [t (:t tag-map)]
      (cond
        (= t :map)
        {}
        (= t :set)
        #{}
        :else
        (for [n (take (resolve-print-length) (range (count x)))]
          (nth x n))))
    x))


(defn- container-for-unknown-coll-size
  [{:keys [all-tags] :as tag-map}]
  (when (= (:coll-size tag-map)
           :lasertag.core/unknown-coll-size)
    (cond (contains? all-tags :map-like)
          {(symbol " ...") (symbol "")}
          ;; (contains? all-tags :coll)
          (contains? all-tags :vector)
          [(symbol "...")]
          (contains? all-tags :set-like)
          #{(symbol "...")}
          :else
          (list (symbol "...")))))

(defn- ml-str-with-adjusted-indentation [s]
  (let [re #"\n( +)"
        n  (some->> s
                    str
                    (re-seq re)
                    (group-by #(count (second %)))
                    keys
                    (apply min))
        f  (fn [[a]] (str "\n" (subs a (inc n))))
        s  (string/replace s re f)]
    s))

(defn- ml-str 
  {:doc "Takes a multi-line string like and normalizes the indentation.
         Useful for multi-line strings that nested inside data structures,
         because some editors automatically format these for readability, but
         the resulting strings have unexpected indentations on lines after the
         first."
   :examples '[{:desc "String as map entry value"
                :forms [[(ml-str "Line one
                                  Line two
                                  Line three
                                    - Line four")
                         
                         "Line one\nLine two\nLine three\n  - Line four"]]}]}
  [s]
  (ml-str-with-adjusted-indentation s))

(defn- ml-string? [x]
  (boolean (and (string? x) (re-find #"\n" x))))

(defn- ml-str->vec [s]
  (-> s
      ml-str
      (string/split #"\n")
      vec))

(defn- truncation-profile
  [{:keys [path depth map-entry-in-non-map?]
    :as   m*}
   x]
  (let [multi-line-string?   (ml-string? x)
        val-is-atom?         (cljc-atom? x)
        val-is-volatile?     (volatile? x) 
        val-is-delay?        (delay? x)
        val-is-ref?          #?(:cljs
                                false
                                :clj
                                (= (type x) clojure.lang.Ref))
        val-is-agent?        #?(:cljs
                                false
                                :clj
                                (= (type x) clojure.lang.Agent))
        val-is-future?       #?(:cljs
                                false
                                :clj
                                (future? x))
        val-is-promise?      #?(:cljs
                                false
                                :clj
                                (when (instance? clojure.lang.IPending x)
                                  (when-not (or (coll? x)
                                                val-is-future?
                                                val-is-delay?)
                                    true)))
        val-is-derefable?    (or val-is-atom? 
                                 val-is-volatile? 
                                 val-is-agent? 
                                 val-is-ref?
                                 val-is-future?
                                 val-is-promise?
                                 val-is-delay?)
        val-is-throwable?    (lasertag.cached/throwable? x)
        og-info              (when (or val-is-derefable? val-is-throwable?)
                               (lasertag.core/tag-map x))
        og-t                 (:tag og-info)
        og-class             (:classname og-info)
        x                    (cond
                               val-is-derefable?
                               (with-meta {:status :ready :val @x}
                                 (meta x))

                               multi-line-string?
                               (ml-str->vec x)

                               val-is-throwable?
                               (cond-> {"Message" (or (ex-message x)
                                                      "No message provided")
                                        "Cause"   (or (ex-cause x)
                                                      "No cause provided")}
                                 (ex-data x)
                                 (assoc "Data" (ex-data x)))
                               :else
                               x)
        kv?                  (boolean (when-not map-entry-in-non-map?
                                        (map-entry? x)))
        tag-map              (when-not kv? (util/tag-map* x))
        x                    (or (when (:object-like-datatype? tag-map)
                                   (if (record? x)
                                     (into {} x)
                                     (util/datatype->map
                                      x
                                      {:skip-object-has-fields-check? true})))
                                 (container-for-unknown-coll-size tag-map)
                                 (reify-if-transient x tag-map))
        too-deep?            (> depth (:print-level @state/config))
        sev?                 (boolean (when-not kv?
                                        (and (not (:coll-type? tag-map))
                                             (not (-> tag-map :t (= :datatype))))))
        user-meta            (value-meta x)
        theme-token-override (:bling.theme/token user-meta)
        user-meta            (some-> user-meta (dissoc :bling.theme/token))
        user-meta            (when (seq user-meta)
                               user-meta)]

    (merge m* ;; m* added for :key? and :map-value? entries
           (keyed [val-is-derefable?
                   val-is-volatile?
                   val-is-agent?
                   val-is-atom?
                   val-is-ref?
                   too-deep?
                   og-class
                   depth
                   og-t
                   path
                   sev?
                   kv?
                   x])
           tag-map
           (some->> theme-token-override (hash-map :theme-token-override))
           (when multi-line-string?
             {:multi-line-string-collection? true})
           {:user-meta      user-meta
            :og-x           x
            :uid-entry?     (volatile! false)
            :print-length   (if too-deep? 0 
                                (resolve-print-length))
            :array-map?     (contains? (:all-tags tag-map) :array-map)
            :top-level-sev? (and sev? (zero? depth))}))) 

#_(defn truncate-type-to-map [obj max-fields]
  #?(:cljs
     ()
     :clj
     (->> (util/object-fields obj)
          (take max-fields)
          (reduce (fn [m field]
                    (.setAccessible field true) ; <- Overrides private visibility
                    (assoc m
                           (keyword (.getName field)) 
                           (.get field obj)))
                  {}))))


(defn- truncated-x*
  [{:keys [coll-type? kv? depth carries-meta? classname path og-x all-tags t]
    :as m}
   x]
  (let [x (cond kv?        
                (let [[k v] x
                      ;; path (conj path k)
                      ]
                  ;; truncate call
                  [(truncate {:depth depth 
                              :key?  true 
                              :path  (conj path k :fireworks.highlight/map-key)}
                             k)
                   ;; truncate call
                   (truncate {:depth      depth
                              :map-value? true
                              :path       (conj path k)} v)])

                ;; TODO - try to get this working
                ;; (? (and (contains? all-tags :js-object)
                ;;         (contains? all-tags :built-in)))
                ;; ;; #?(:cljs
                ;; ;;    (contains? #{js/Math js/Atomics js/JSON js/Intl} og-x)
                ;; ;;    :clj
                ;; ;;    nil)
                ;; #?(:cljs
                ;;    (? (cond (= og-x js/Math) "js/Math"
                ;;               (= og-x js/Atomics) "js/Atomics"
                ;;               (= og-x js/JSON) "js/JSON" 
                ;;               (= og-x js/Intl) "js/Intl"))
                ;;    :clj
                ;;    (symbol "wtf?"))
                
                coll-type?
                (if (empty? x) x (truncated-coll m x))

                (= t :datatype)
                (util/datatype->map x)

                (= classname "java.math.BigDecimal")
                (symbol (str x "M"))

                (and (= t :symbol)
                     (= x 'js/undefined))
                (symbol "undefined")

                (and (:quote-symbols? @state/config)
                     (= t :symbol)
                     ;; check that is is not a js-object key such as `'a':`
                     (not (when (:key? m) (re-find #"'[^\']+':$" (str x)))))
                (symbol (str "'" x))

                :else      
                x)]

    (if (or carries-meta?
            (util/carries-meta? x))
      x
      (symbol (str x)))))


(defn with-badge-and-ellipsized
  [x kv? mm*]
  (let [badge      (when-not kv? (profile/annotation-badge mm*))
        mm         (merge mm* badge)
        ellipsized (when (:sev? mm)
                     (ellipsize/ellipsized x mm))] 
    (merge mm* ellipsized)))


#?(:bb
   (defn- remove-sci-lang-type-metadata 
     "Removes verbose :sci.impl/* metadata on custom datatypes."
     [user-meta mm*]
     (if (= "sci.lang.Type" (:classname mm*))
       (reduce-kv (fn [m k v]
                    (if (-> k str (string/starts-with? ":sci.impl"))
                      m
                      (assoc m k v)))
                  {}
                  user-meta)
       user-meta)))

;; Make sure to update :fw/truncated entry in example in docstring, if the shape
;; of that value changes.
(defn truncate
  "Example:
   (? {:print-length 5} (with-meta (range 8) {:foo :bar}))
   
   Assuming x is a (contrived) coll limit of 8, fireworks.truncate/truncate
   gets called recursively, and all coll types are converted to vectors.
   Metadata gets attached to all values and nested values.
   
   ^{:fw/truncated {:og-x                '(0 1 2 3 4 5 6 7),
                    :coll-type?          true,
                    :carries-meta?       true,
                    :array-map?          false,
                    :kv?                 false,
                    :user-meta           {:foo :bar},
                    :truncated-coll-size 5,
                    :map-like?           false,
                    :user-meta?          nil,
                    :type                'clojure.lang.LongRange,
                    :too-deep?           false,
                    :coll-size           8,
                    :all-tags            #{:coll :seq},
                    :print-length        5,
                    :coll-size-adjust    8,
                    :sev?                false,
                    :num-dropped         3,
                    :top-level-sev?      false,
                    :depth               0,
                    :t                   :seq,
                    :x                   '(0 1 2 3 4 5 6 7),
                    :js-typed-array?     false,
                    :val-is-atom?        false,
                    :val-is-volatile?    false,
                    :number-type?        false,
                    :js-map-like?        false},
   :user-meta      {:foo :bar}}
  [^{...}
   1
   ^{...}
   2
   ^{...} 
   3
   ^{...}
   4]"
  [m* x]
  (let [
        ;; fireworks.truncate/truncation-profile calls lasertag/tag-map
        ;; x, if atom, gets reified in fireworks.truncate/truncation-profile
        {:keys [x             
                kv?           
                user-meta         
                coll-type?]
         :as   m}
        (truncation-profile m* x)

        truncated-x 
        (truncated-x* m x)

        mm*           
        (-> m
            (dissoc :uid-entry?) ;; this removes the volatile
            (merge (when (and (not kv?) coll-type?)
                     (new-coll-info m truncated-x))))

        ;; TODO - Use this instead of what is happening in fireworks.profile
        ;; mm*
        ;; (with-badge-and-ellipsized x kv? mm*)
        
        meta-to-attach
        (merge {:fw/truncated mm*
                :fw/user-meta #?(:cljs user-meta
                                 :bb (remove-sci-lang-type-metadata user-meta mm*)
                                 :clj user-meta)}
               (some->> m*
                        :user-meta? 
                        (hash-map :fw/user-meta-map?)))]
    (with-meta truncated-x meta-to-attach)))
