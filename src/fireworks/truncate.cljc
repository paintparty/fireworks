(ns ^:dev/always fireworks.truncate
  (:require
   [clojure.set :as set]
   [fireworks.defs :as defs]
   [fireworks.state :as state]
   [fireworks.serialize :refer [seq->sorted-set seq->sorted-map]]
   [fireworks.util :as util]
   [typetag.core :as typetag]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:cljs [typetag.cljs-interop :refer [js-built-in-objects-by-object-map]])
   #?(:clj [fireworks.macros :refer [keyed]])))

(defn pre-truncated? [x]
  (let [mm (meta x)]
    (and (contains? mm :fw/truncated)
         (contains? mm :fw/user-meta))))

(defn- js-obj-key [k]
  (let [sq "'" #_(when (re-find #" " k) "'")]
    (symbol (str sq k sq ":"))))

(defn cljc-atom?
  [x]
  #?(:cljs (= cljs.core/Atom (type x))
     :clj  (= clojure.lang.Atom (type x))))

(defn maybe-hydrated-atom
  [x]
  (let [atom?     (cljc-atom? x)
        val-meta  (if (pre-truncated? x)
                    (-> x meta :fw/user-meta)
                    (meta (if atom? @x x)))  
        x         (if atom? @x x)]
    (keyed [atom? val-meta x])))

#?(:cljs 
   (do
     (defn- js-kv [k v]
       [(js-obj-key k) v])

     (defn- entries->map
       "Converts a js-map-like object to a cljs map. Removes the
        \"closure_uid_*\" key from the converted object, because we don't want
        to print it. Also changes the keys from strings to symbols with a
        trailing colon (js-style) like:
        \"mykey\" => `mykey:`
        \"mykey with spaces\" => `'mykey with spaces':`"
       [coll]
       (into {}
             (keep (fn [[k v]]
                     (when-not (re-find #"^closure_uid_d*" k)
                       [(js-obj-key k) v]))
                   coll))) 

     (defn jsobj->cljs
       "Removes the \"closure_uid_*\" key from the converted object, because we
        don't want to print it."
       [o]
       (-> o js->clj entries->map))

     (defn map-like-jsobj->cljs
       "Removes the \"closure_uid_*\" key from the converted object, because we
        don't want to print it."
       [o]
       (let [entries (js->clj (js/Object.entries o))]
         (if (seq entries)
           (entries->map entries)
           (if-let [props (some->
                           o
                           type
                           js-built-in-objects-by-object-map
                           :instance-properties)]
             (into {} 
                   (keep (fn [k]
                           (when-let [v (aget o k)]
                             (js-kv k v))) 
                         props))
             {}))))))


(def function-by-coll-type
  {:set                 seq->sorted-set
   :map                 #(into {} %)
   :record              #(into {} %)
   :vector              #(apply vector %)
   :list                #(apply vector %)
   :seq                 #(apply vector %)
   :java                #(apply vector %)
   :js/Array            #?(:cljs #(js->clj %) :clj nil)
   :js/Object           #?(:cljs #(-> % js->clj entries->map)
                           :clj nil)
   :js/Map              #?(:cljs #(-> % js->clj entries->map)
                           :clj nil)
   :js/Set              #?(:cljs #(-> % 
                                      js/Array.from 
                                      js->clj 
                                      (into #{}))
                           :clj nil)
   :js/map-like-object  #?(:cljs map-like-jsobj->cljs :clj nil)
   :js/DataView         #?(:cljs map-like-jsobj->cljs :clj nil)

   :js/dom-element-node #?(:cljs (fn [] {}) :clj nil)})


;; TODO - maybe just replace this inline with (count coll),
;; as all cols here should have gotten converted to
;; clj colls in fireworks.truncate/shorten-coll.
(defn- coll-size
  ([coll]
   (coll-size coll (-> coll meta :t)))
  ([coll t]
   #?(:cljs
      (cond
        (seqable? coll)
        (count coll)
        
        (contains? #{:js/Object :js/map-like-object} t)
        (.-length (js/Object.keys coll))

        (contains? #{:js/Array :js/TypedArray} t)
        (.-length coll)

        (contains? #{:js/Map :js/Set} t)
        (.-size coll))
      :clj
      (cond
        (seqable? coll)
        (do 
          (count coll))
        
        :else
        (do 
          (.-size coll))))))


(defn- take-cljc
  [{:keys [t coll-limit x]}]
  #?(:cljs
     (cond 
       (= t :js/Object)
       (let [as-array  (js/Object.entries x)
             truncated (.slice as-array 0 coll-limit)]
         (js/Object.fromEntries truncated))

       (= t :js/Array)
       (.slice x 0 coll-limit)

       (= t :SyntheticBaseEvent)
       (let [_         (doto x
                         (js-delete "view")
                         (js-delete "nativeEvent")
                         (js-delete "target"))
             as-array  (js/Object.entries x)
             truncated (.slice as-array 0 coll-limit)]
         (js/Object.fromEntries truncated))

       :else
       (take coll-limit x))

     :clj
     (take coll-limit x)))


(defn- coll-type2 [coll t js-map-like? dom-element-node?]
  (if (contains? function-by-coll-type t)
    t
    (cond (record? coll)
          :record
          (map? coll)
          :map
          (set? coll)
          :set
          dom-element-node?
          :js/dom-element-node
          js-map-like?
          :js/map-like-object
          :else       
          :vector)))


(defn- shorten-coll
  [{:keys [x
           t
           n
           js-map-like?
           og-coll-size
           dom-element-node?
           evaled-form?]
    :as m}]
  (let [{:keys 
         [print-level
          evaled-form-coll-limit
          coll-limit]}
        @state/config

        too-deep?  (> n print-level)
        coll-limit (if too-deep?
                     0
                     (if evaled-form?
                       evaled-form-coll-limit
                       coll-limit))
        coll-type  (coll-type2 x t js-map-like? dom-element-node?)
        size-diff  (- (or og-coll-size 0) (or coll-limit 0))
        f          (coll-type function-by-coll-type)
        ret        (if (< 1 size-diff)
                     ;; This branch truncates the coll
                     (let [taken (take-cljc (merge m
                                                   (keyed [coll-limit
                                                           coll-type])))
                           ret   (f taken)]
                       ret)

                     ;; TODO - the if branch here should only catch actual
                     ;; clj/cljs colls, all other colls need to be converted
                     ;; to clj/cljs colls here.
                     (let [ret (if (contains? #{:map #_:set :vector} coll-type)
                                 x
                                 (f x))]
                       ret))
        locals      (keyed [print-level
                            coll-limit
                            evaled-form-coll-limit
                            too-deep?
                            coll-limit
                            coll-type
                            size-diff
                            f])]
    [too-deep? ret]))


(defn- stringified-coll [coll]
  (try (str coll)
       #?(:cljs (catch js/Object e nil)
          :clj (catch Exception e nil))))


(defn- coll-str-len
  "Gives the string length of a coll without things like commas in the case of
   maps (as in clojure.pprint/print). Includes num-dropped trailing annotation."

  [{:keys [coll num-dropped map-like? set-like? new-coll-size]}]

  ;; The `(:value-width-limit @state/config)` value the `or` branch of the
  ;; str-len binding is temp placeholder, in case an exception is caught from
  ;; trying to stringify a coll that contains something like a js/TypedArray.
  (let [str-len (or (some-> coll stringified-coll count)
                    (:value-width-limit @state/config))
        ret*    (cond 
                  ;; We are adding +1 for leading `#` char on
                  ;; set-like interop objects such as js/Set
                  set-like?
                  (inc str-len)

                  ;; This accounts for the fact that we are
                  ;; not printing the comma chars that result
                  ;; from stringification of map-like objects.
                  map-like?
                  (- str-len (dec new-coll-size))

                  :else
                  str-len)
        ret     (if (some-> num-dropped pos?)
                  (+ ret* 
                     (count (str " "
                                 defs/num-dropped-prefix
                                 num-dropped)))
                  ret*)]
    ret))


(declare truncate)


(defn- new-coll*
  [{:keys [t n map-like? coll meta-map val-meta]}]
  (let [ret*     (if map-like? 
                   (->> coll
                        (map (partial truncate (inc n)))
                        seq->sorted-map)
                   (let [f* (if (map-entry? coll) identity inc)
                         f  (partial truncate (f* n))]
                     (mapv f coll)))
        meta-map (if map-like?
                   (assoc meta-map :sorted-map-keys (keys ret*))
                   meta-map)
        ret      (if (= :set t) (seq->sorted-set ret*) ret*)]
    (with-meta
      ret
      {:fw/truncated meta-map

       ;; Need to sanitize user-meta here, because JVM clojure reader
       ;; automatically adds metadata with :line and :column entries
       ;; to quoted lists. ClojureScript, however, does not do this.
       :fw/user-meta (dissoc val-meta :line :column)})))


(defn- new-coll
  [{:keys [n
           x
           t
           typetag-map
           kv?
           atom?
           val-meta
           evaled-form?] 
    :as opts}]
  (let [map-like?         (:map-like? typetag-map)
        set-like?         (or (= t :js/Set) (:set-like? typetag-map))
        og-coll-size      (:coll-size typetag-map)
        all-tags          (:all-tags typetag-map)
        js-map-like?      (contains? all-tags :js/map-like-object)
        js-typed-array?   (contains? all-tags :js/TypedArray)
        dom-element-node? (contains? all-tags :dom-element-node)
        shorten-coll-opts (keyed [og-coll-size
                                  js-map-like?
                                  dom-element-node?])
        [too-deep? coll]  (shorten-coll (merge opts shorten-coll-opts))
        meta-map          (let [t             (if js-typed-array?
                                                :js/TypedArray
                                                t)
                                new-coll-size (coll-size coll t)

                                ;; In the next binding, `num-dropped`, the first 
                                ;; coll-size test (in `if` branch) is needed cos 
                                ;; we might have a difference of 1 when doing
                                ;; (- (or og-coll-size 0) (or new-coll-size 0))
                                ;; even if coll is less than config'd coll-limit
                                ;; in the case of js/Objects ... because cljs
                                ;; will add a `:clojure-uid-*` entry.
                                num-dropped   (if (<= (or og-coll-size 0)
                                                      (or (:coll-limit
                                                           @state/config) 0))
                                                0
                                                (max (- (or og-coll-size 0)
                                                        (or new-coll-size 0))
                                                     0))
                                opts          (keyed [coll
                                                      num-dropped
                                                      map-like?
                                                      set-like?
                                                      new-coll-size])
                                str-len       (coll-str-len opts)
                                _             (when (= t :js/Set) str-len)
                                meta-map      (when-not kv?
                                                (merge
                                                 typetag-map
                                                 (keyed [str-len
                                                         num-dropped
                                                         map-like?
                                                         js-map-like?
                                                         js-typed-array?
                                                         atom?
                                                         too-deep?])
                                                 {:og-x                
                                                  x
                                                  :print-level         
                                                  n
                                                  :truncated-coll-size
                                                  new-coll-size}
                                                 (when dom-element-node?
                                                   {:html-element x})))]
                            meta-map)
        ret-with-meta     (new-coll* (merge opts
                                            (keyed [map-like?
                                                    coll
                                                    meta-map
                                                    val-meta])))]
    ret-with-meta))


;; This is multi-arity with `opts` so we can pick up :evaled-form? value
(defn truncate
  ([n x]
   (truncate n x nil))
  ([n x opts]
   (let [og-x               
         x

         {:keys [x atom? val-meta]}  
         (maybe-hydrated-atom x)

         og-t 
         (:fw/og-t val-meta)

         val-meta
         (if og-t (dissoc val-meta :fw/og-t) val-meta)

         kv?                    
         (map-entry? x)

         {:keys [coll-type? t]
          :as   typetag-map}     
         (when-not kv?
           (-> x
               (typetag/tag-map 
                (when 
                 ;; TODO - When refactoring to attach metadata to
                 ;; non-colls in this truncation fn ...
                 ;; change this from true to something else, so we
                 ;; can do a simple check to use narrowing options
                 ;; when we know we don't need them
                 ;; maybe (not (or (instance? js/Object og-x) (fn? og-x)))
                 true
                  {:js-built-in-object-info? false
                   :function-info?           false}))
               (set/rename-keys {:tag :t})
               (merge (some->> og-t (hash-map :t)))))]

     (if (or coll-type? kv?)

       ;; Potenitally shorten coll and attach meta
       (new-coll (merge (keyed [n x kv? t typetag-map atom? val-meta]) opts))

       ;; Optionally symbolize non-colls and attach meta
       (if (pre-truncated? x)
         x
         (let [meta-map (assoc typetag-map 
                               :x x
                               :og-x og-x
                               :atom? atom?
                               :sev? true
                               :depth n)
               ret*     (if (util/carries-meta? x) 
                          x
                          (symbol (str x)))
               ret      (with-meta
                          ret*
                          {:fw/truncated meta-map
                             ;; Sanitize val meta as clojure reader will add
                             ;; location metadata noise to quoted lists.
                           :fw/user-meta (when val-meta
                                           (dissoc val-meta :line :column))})] 
           ret))))))
