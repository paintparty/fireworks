(ns ^:dev/always fireworks.profile
  (:require
   [fireworks.defs :as defs]
   [fireworks.pp :refer (?pp)]
   [fireworks.ellipsize :as ellipsize]
   [fireworks.state :as state]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [clojure.string :as string])
  #?(:clj
     (:import (clojure.lang PersistentVector))))


;; TODO revist how your doing this, maybe there should be a :classname entry
;; returned from lasertag
(def badges-by-lasertag
  {:js/Set      "js/Set"
   :js/Promise  "js/Promise"
   :js/Iterator defs/js-literal-badge
   :js/Object   defs/js-literal-badge
   :js/Array    defs/js-literal-badge
   :lambda      defs/lambda-symbol
   :transient   defs/transient-label
   :uuid        defs/uuid-badge})


;; Understand and doc how this works for custom types
(defn annotation-badge
  [{:keys [t 
           lambda?
           js-built-in-object?
           js-built-in-object-name
           all-tags
           java-util-class?
           java-lang-class?
           coll-type?
           transient?
           classname]
    :as m}]
  (when (map? m)
   
   (let [t     (cond lambda?
                     :lambda
                     transient?
                     :transient
                     :else
                     t)

         badge (cond
                 (contains? all-tags :datatype)
                 (:classname m)

                 ;; Interesting visualization in JVM Clojure
                 ;; Labels everything, including primitives
                 ;; Maybe this could be exposed in an option 
                 #_(or java-util-class? java-lang-class?)
                 #_classname

                 (= t :inst)
                 defs/inst-badge

                 (or java-util-class?
                     (and coll-type? java-lang-class?))
                 classname

                 js-built-in-object?
                 (str "js/" js-built-in-object-name)

                 (and (not= t :js/Object)
                      (or (contains? all-tags :js-map-like-object)
                          (contains? all-tags :js-typed-array)))
                 (str "js/" classname)
                 
                 transient?
                 (or (some->> #?(:cljs #"/" :clj #"\$")
                              (string/split classname)
                              last)
                     defs/transient-label)

                 :else
                 #?(:cljs
                    (get {"Map"    "js/Map"
                          "Set"    "js/Set"
                          "Array"  "js/Array"
                          "Object" "js/Object"}
                         classname 
                         nil)
                    :clj
                    (get badges-by-lasertag t nil)))
         badge #?(:cljs badge
                  :clj  (if (= t :defmulti) "Multimethod" badge))]

     (when badge {:badge badge}))))
                         
(defn target-path-is-ancestor-coll?
  [tp vp tp-list]
  (let [debug?
        false
        #_(boolean (and (= tp [:map :c])
                      (= vp [:map :c :fireworks.highlight/map-key])))

        value-path-has-more-nodes-than-target-path?
        (boolean (and tp (< (count tp) (count vp))))

        target-path-is-potentially-an-ancestor?
        (boolean (= tp-list (take (count tp) vp)))

        rest-of-value-path
        (drop (count tp) vp)

        map-key-for-a-target-value?
        (boolean (and (= (count rest-of-value-path) 1)
                      (= (first rest-of-value-path)
                         :fireworks.highlight/map-key)))
        ret
        (boolean
         (when value-path-has-more-nodes-than-target-path?
           (and target-path-is-potentially-an-ancestor?
                (not map-key-for-a-target-value?))))]

    #_(when debug?
      (?pp (keyed [value-path-has-more-nodes-than-target-path?
                   target-path-is-potentially-an-ancestor?
                   map-key-for-a-target-value?
                   rest-of-value-path
                   tp
                   vp
                   tp-list
                   ret])))
    ret))

(defn- highlighting*
  "Determines whether value receives highlighting"
  [x
   value-path
   {:keys       [pred style]
    target-path :path
    :as         m}]
  (when (or (and pred (pred x))
            (and target-path (= target-path value-path))
            (target-path-is-ancestor-coll? target-path
                                           value-path
                                           (apply list target-path)))
    {:highlighting style}))


(defn- highlighting
  [x path]
  (when-let [hl @state/highlight]
    (cond 
      (map? hl)
      (highlighting* x path hl)
      (vector? hl)
      (some (partial highlighting* x path) hl))))


;; TODO - double check accuracy of this
;; It seems this value is still used in fireworks.serialize/reduce-coll-profile
;; To determine if coll should be printed multi-line?
(defn- str-len-with-badge
  [badge val-is-atom? x]
  (let [badge-str-len
        (or (some-> badge :badge count) 0)

        val-str-len
        (or (-> x str count) 0)

        ;; This currently only checks encapsulation-closing-bracket-len,
        ;; maybe don't need this.
        encapsulation-closing-bracket-len
        (or (when val-is-atom? 
              (count defs/encapsulation-closing-bracket))
            0)

        str-len-with-badge
        (+ badge-str-len
           val-str-len
           ;; Not currenlty supporting custom-badges, so leave commented
           #_(or (extra-str-from-custom-badges x) 0)
           encapsulation-closing-bracket-len)]

    #_(when-not (not (coll? x)) #_@state/formatting-form-to-be-evaled?
      (?pp
       (keyed [str-len-with-badge
               val-str-len
               badge-str-len
               x
               encapsulation-closing-bracket-len
               ])))

    (keyed [str-len-with-badge
            badge-str-len
            val-str-len
            encapsulation-closing-bracket-len])))


(defn- mutable-wrapper-count
  [{:keys [val-is-atom? val-is-volatile?]}]
  (cond val-is-atom?
        defs/atom-wrap-count
        val-is-volatile?
        defs/volatile-wrap-count
        :else
        0))


(defn- exceeds-map-value-threshold? [k-or-v n]
  (->> k-or-v
           meta 
           :ellipsized-char-count
           (< n)))


(defn force-single-column-map-layout-on-threshold-basis? 
  [coll meta-map]
  (boolean
   (when (-> meta-map :map-like?)
     (when-let [n (:single-column-maps-length-threshold @state/config)]
       (some (fn [[k v]] 
               (or (exceeds-map-value-threshold? k n)
                   (exceeds-map-value-threshold? v n)))
             coll)))))


(defn maybe-ellipsize
  "Attaches badge and ellipsis, when appropriate.

   Wraps atoms and volatiles.

   Adds :strlen-with-badge-ellipsized entry to meta, to potentially be used for
   formatting in fireworks.serialize/serialized.
   
   Potentially sets single-column-map-layout? entry in meta to true, if x is a
   map-like? coll and any of the keys or values exceed the value of the config
   option :single-column-maps-length-threshold.
   "
   
  [{:keys [coll-type? ellipsized x t meta-map]}]
  (let [ret* (cond 
               coll-type?
               x

               (contains? #{:function :defmulti :class} t)
               (let [{:keys [fn-display-name
                             truncate-fn-name?]} ellipsized]
                 (if fn-display-name
                   (symbol (str fn-display-name 
                                (when truncate-fn-name?
                                  defs/ellipsis)))
                   (let [ret (symbol (str "λ_" (gensym)))]
                     #?(:cljs ret
                        :clj (if (= t :defmulti)
                               'MultiFn
                               ret)))))

               :else
               (symbol (str (:s ellipsized)
                            (when (:exceeds? ellipsized)
                              defs/ellipsis))))

        ret (if (or (= t :clojure.lang.MapEntry)
                    (= t :cljs.core.MapEntry))
              ret* 
              (let [ellipsized-char-count
                    (some-> ellipsized :ellipsized-char-count)

                    ellipsis-char-count
                    (when (:exceeds? ellipsized) defs/ellipsis-count)

                    str-len-val-ellipsized
                    (+ (or ellipsized-char-count
                           (:val-str-len meta-map)
                           0)
                       (or ellipsis-char-count 0))


                    ;; TODO - Do we still need str-len-with-badge-ellipsized
                    str-len-with-badge-ellipsized
                    (+ (:badge-str-len meta-map)
                       (mutable-wrapper-count meta-map)
                       str-len-val-ellipsized)

                    ;; _ (when-not @state/formatting-form-to-be-evaled?
                    ;;     (?pp x)
                    ;;     (?pp (keyed [m
                    ;;                  badge-str-len
                    ;;                  atom-wrap-count
                    ;;                  ellipsized-char-count
                    ;;                  str-len-with-badge-ellipsized])))
                    
                    single-column
                    (when (force-single-column-map-layout-on-threshold-basis?
                           ret*
                           meta-map)
                      {:single-column-map-layout? true})


                    meta-map            
                    (merge meta-map
                           {:str-len-val-ellipsized
                            str-len-val-ellipsized
                            :str-len-with-badge-ellipsized
                            str-len-with-badge-ellipsized}
                           single-column)]
                (with-meta ret* meta-map)))]
    ret))


(defn- force-single-column-map-layout?
  [coll]
  (boolean (and (map? coll)
                (:single-column-maps? @state/config))))


(defn- some-colls-as-keys?
  [coll]
  (boolean (when (map? coll)
             (some (fn [[k _]] (coll? k)) coll))))


(defn- user-metadata [x]
  (some-> x meta :fw/user-meta))


(defn- some-syms-carrying-metadata-as-keys?
  [coll]
  (boolean (when (map? coll)
             (some (fn [[k _]]
                     (and (= :symbol (some-> k meta :fw/truncated :t))
                          (user-metadata k)))
                   coll))))


(defn- el-with-meta [x]
  (let [mm (meta x)]
    (or
     (some-> mm :fw/truncated :user-meta)
     (some-> mm :fw/user-meta)
     (when-let [ks (-> mm keys seq)]
       (when-not (some #(string/starts-with? (str %) ":fw/") ks)
         mm)))))


(defn- some-elements-carry-user-metadata?
  [x]
  (when (coll? x)
    (boolean 
     (seq (if (map? x)
            (some (fn [[k v]] (or (user-metadata k) (user-metadata v))) x)
            (let [els-with-meta (keep el-with-meta x)]
              els-with-meta))))))


(def ^:private meta-map-mapentry-vector
  "Performance cheat to save a call to util-tag-map*"
  {:type          #?(:cljs cljs.core/PersistentVector
                     :clj clojure.lang.PersistentVector) 
   :all-tags      #{:coll :vector :coll-type :carries-meta}
   :classname     #?(:cljs "cljs.core/PersistentVector"
                     :clj "clojure.lang.PersistentVector")
   :coll-size     2
   :t             :vector
   :carries-meta? true
   :coll-type?    true})

(defn meta-map*
  [x mapkey]
  (let [mm (meta x)]
   (if (:fw/pre-profiled-mapkey mm)
     mm
     (let [{:keys [og-x val-is-atom?]
            :as fw-truncated-meta}  (or (:fw/truncated mm)
                                        ;; TODO - describe why it is necessary
                                        ;; to provide this map here
                                        meta-map-mapentry-vector)
           ret   (merge 
                  fw-truncated-meta
                  {:x    (or (:x fw-truncated-meta) x)
                   :og-x og-x}

                  (highlighting og-x (:path fw-truncated-meta))

                  ;; TODO - need this mapkey map?
                  mapkey

                  ;; TODO - after getting custom printing working
                  ;; refactor this :fw/type-after-custom-printing
                  ;; into select-keys vec and use rename-keys
                  (some->> mm
                           :fw/type-after-custom-printing 
                           (hash-map :t))

                  ;; TODO - This select-keys is where :fw/custom-badge-style and
                  ;; :fw/custom-badge-text would go
                  (some-> mm
                          (select-keys [:fw/user-meta :fw/user-meta-map?])))

           ;; TODO - make dedicated badge fn?
           badge (annotation-badge ret)
           ret   (let [some-colls-as-keys?                  
                       (some-colls-as-keys? x)

                       some-syms-carrying-metadata-as-keys?
                       (some-syms-carrying-metadata-as-keys? x)

                       single-column-map-layout?            
                       (or some-colls-as-keys?
                           some-syms-carrying-metadata-as-keys?
                           (force-single-column-map-layout? x))

                       inline-badge?
                       (and badge
                            (contains? defs/inline-badges (:badge badge)))]
                   (merge ret
                          badge
                          (when badge {:inline-badge? inline-badge?})
                          (keyed [some-colls-as-keys?
                                  some-syms-carrying-metadata-as-keys?
                                  single-column-map-layout?])

                          ;; TODO - Currently, str-len-with-badge is only used
                          ;; in fireworks.serialize/reduce-coll-profile to
                          ;; determine if collection should be multiline.
                          
                          ;; Maybe it can be left out here and just called
                          ;; inline at fireworks.serialize/reduce-coll-profile,
                          ;; only if needed.
                          (str-len-with-badge badge val-is-atom? x)

                          {:some-elements-carry-user-metadata? 
                           (some-elements-carry-user-metadata? x)}))]
        (when (coll? x) ret)
        ret))))


(defn re-profile
  "Reprofile to deal with map keys"
  [x meta-map f]
  (if (:map-like? meta-map) 
    (let [ret 
          (into []
                (map-indexed 
                 (fn [i [k v]]
                   ;;  (?pp (meta k))
                   ;;  (?pp k)
                   (let [k (f k {:key?             true
                                 :associated-value v
                                 :js-map-like-key? (:js-map-like? meta-map)
                                 :index            i})
                         v (vary-meta v assoc-in [:fw/truncated :map-value?] true)]

                     ;;  (?pp k)
                     ;;  (?pp (meta k))
                     [k v]))
                 x))

          ]

      ret)
    x))

;; TODO - Address the following:
;; - Is one of :user-meta and :fw/user-meta redundant?
;; - Is :str-len-with-badge-ellipsized redundant?
;; - Maybe use :tag instead of :t globally?

(defn profile
  "Enriches value's metadata map by merging it with :fw/truncated entry and
   adding additional entries to such as:

   :some-colls-as-keys?                 
   :str-len-with-badge-ellipsized                 
   :single-column-map-layout?           
   :some-syms-carrying-metadata-as-keys?
   :str-len-with-badge                  
   :some-elements-carry-user-metadata?  

   These additional entries address things like:
   - Data structures as map keys
   - Map keys with metadata
   - Elements in collection with metadata
   - Lables for objects, records, transients
   - Ellipises
   - Adjusted string length

   Example:
   (? {:coll-limit 5} (with-meta (range 8) {:foo :bar}))

   ^{:og-x                                 '(0 1 2 3 4 5 6 7),
     :some-colls-as-keys?                  false,
     :str-len-with-badge-ellipsized                  0,
     :coll-type?                           true,
     :carries-meta?                        true,
     :array-map?                           false,
     :kv?                                  false,
     :user-meta                            {:foo :bar},
     :truncated-coll-size                  5,
     :map-like?                            false,
     :user-meta?                           nil,
     :single-column-map-layout?            false,
     :type                                 'clojure.lang.LongRange,
     :too-deep?                            false,
     :coll-size                            8,
     :some-syms-carrying-metadata-as-keys? false,
     :all-tags                             #{:coll :seq},
     :str-len-with-badge                   11,
     :coll-limit                           5,
     :some-elements-carry-user-metadata?   false,
     :fw/user-meta                         {:foo :bar},
     :coll-size-adjust                     8,
     :sev?                                 false,
     :num-dropped                          3,
     :top-level-sev?                       false,
     :depth                                0,
     :t                                    :seq,
     :x                                    '(0 1 2 3 4 5 6 7),
     :js-typed-array?                      false,
     :val-is-atom?                         false,
     :val-is-volatile?                     false,
     :number-type?                         false,
     :js-map-like?                         false}
  [^{...}
   1
   ^{...}
   2
   ^{...} 
   3
   ^{...}
   4]"
  ([x]
   (profile x nil))
  ([x {:keys [key?
              associated-value
              js-map-like-key?
              index]
       :as mapkey}]
   ;; TODO - doc when (-> x meta :ellipsized-char-count) happen?
   (if (-> x meta :ellipsized-char-count)
     x
     (let [meta-map*
           (meta-map* x mapkey)

           ellipsized
           (when-not (:coll-type? meta-map*)
             (ellipsize/ellipsized x meta-map*))
           
           meta-map
           (merge meta-map*
                  ellipsized
                  (when (contains? (:all-tags meta-map*) :record)
                    {:record? true})
                  (when key? {:key? true :fw/pre-profiled-mapkey true})
                  (when associated-value {:associated-value associated-value})
                  (when js-map-like-key? {:js-map-like-key? true})
                  (when index {:index index}))

           x
           (re-profile x meta-map profile)
           
           {:keys [coll-type? t]}
           meta-map

           ;; this adds more stuff to the meta-map
           ret
           (maybe-ellipsize (keyed [coll-type? ellipsized x t meta-map]))]
       ret))))
