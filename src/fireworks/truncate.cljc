(ns ^:dev/always fireworks.truncate
  (:require
   [fireworks.pp :refer [?pp]]
   [clojure.string :as string]
   [clojure.set :as set]
   [fireworks.state :as state]
   [fireworks.profile :as profile]
   [fireworks.ellipsize :as ellipsize]
   [fireworks.order :refer [seq->sorted-map]]
   [fireworks.util :as util]
   [lasertag.core :as lasertag]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])))

#?(:cljs
   (do 
     (defn inline-style->map [v]
       (as-> v $
         (string/split $ #";")
         (map #(let [kv    (-> % string/trim (string/split #":") )
                     [k v] (map string/trim kv)]
                 [k v]) 
              $)
         (into {} $)))

     (defn dom-el-attrs-map
       [x]
       (when-let [el x #_(some-> x meta :fw/truncated :html-element)]
         ;; You could pull :fw/user-meta out of html element,
         ;; but when would it have it?
         
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
                            (= k "class") (into [] (string/split v " "))
                            :else         v)]))))))))

(defn cljc-atom?
  [x]
  #?(:cljs (= cljs.core/Atom (type x))
     :clj  (= clojure.lang.Atom (type x))))


;; Performance gain?
;; Maybe declare truncate new above this and incorporate into kv
;; Mabye use reduce-kv or transducer to speed this up?
#?(:cljs 
   (defn- js-obj->array-map 
     [{:keys [x coll-limit depth uid-entry?]}]
     (if (> depth 8)
       {}
       (let [keys  (take coll-limit (.keys js/Object x))]
         (into (array-map)
               (reduce (fn [acc k]
                         (if (string/starts-with? k "closure_uid_")
                           (do (vreset! uid-entry? true)
                               acc)
                           (conj acc
                                 [(symbol (str "'" k "':"))
                                  (aget x k)])))
                       []
                       keys))))))


(declare truncate)


(defn truncate-iterable
  [x
   coll-limit
   map-like?
   depth]
  (let [ret (->> x
                 (take coll-limit)
                 (into [])
                 (mapv (partial truncate {:depth (inc depth)})))]
    (if map-like?
      (seq->sorted-map ret)
      ret)))


(defn new-coll-info
  [coll {:keys [uid-entry?
                coll-size
                all-tags
                map-like?]}]
  ;; TODO maybe-change binding name to truncated-coll-size -> coll-size?
  (let [truncated-coll-size (count coll)
        coll-size-adjust    (- coll-size (if @uid-entry? 1 0))
        num-dropped         (some->> truncated-coll-size
                                     (- coll-size-adjust))
        js-typed-array?     (contains? all-tags :js/TypedArray)
        js-map-like?        (contains? all-tags :js/map-like-object)
        ret*                (keyed [truncated-coll-size  
                                    coll-size-adjust        
                                    js-typed-array?                  
                                    js-map-like?          
                                    num-dropped])
        ret                 (merge ret*
                                   (when map-like?
                                     {:sorted-map-keys (mapv #(nth % 0 nil) coll)}))]
    ret))


(defn new-coll2
  ;; [x uid-entry? tag-map depth t too-deep?]
  [{:keys [x
           t
           depth
           map-like?
           too-deep?
           dom-node-type]
    :as opts+}]
  (let [coll-limit (if too-deep? 0 (:coll-limit @state/config))
        ;; TODO - maybe do this?
        ;; opts* (assoc opts :coll-limit coll-limit)
        ]

    ;; can we use cljc-friendly sequable?
    (if #?(:cljs (not (satisfies? ISeqable x)) :clj nil)
      ;; This if for js objects
      #?(:cljs
         (if dom-node-type
           (truncate {:depth (inc depth)} (dom-el-attrs-map x))
           (let [_   (when (= t :SyntheticBaseEvent)
                         (doto x
                           (js-delete "view")
                           (js-delete "nativeEvent")
                           (js-delete "target")))
                   ret (js-obj->array-map (assoc opts+
                                                 :coll-limit
                                                 coll-limit))]
               ;; Maybe incorporate this into js-obj->array-map?
               (into {}
                     (map (partial truncate
                                   {:depth (inc depth)})
                          ret))))
         :clj nil)

      ;; This if for everything else
      (truncate-iterable x coll-limit map-like? depth)
      ;; Can we do this?
      ;; (truncate-iterable opts*)
      )))

(defn- user-meta* [x]
  (let [m (meta x)
        m (when m 
            (when-not (and (list? x)
                           (= '(:line :column)
                              (keys m)))
              m))]
    m))

(defn- truncate-opts
  [opts x]
  (let [depth      (:depth opts)
        atom?      (cljc-atom? x)
        x          (if atom? @x x)
        user-meta  (user-meta* x)
        kv?        (map-entry? x)
        tag-map    (when-not kv?
                     (set/rename-keys (lasertag/tag-map x)
                                      {:tag :t}))
        uid-entry? (volatile! false)
        too-deep?  (> depth (:print-level @state/config))
        sev?       (boolean (when-not kv? (not (:coll-type? tag-map))))
        ret        (keyed [uid-entry?       
                           user-meta       
                           too-deep?           
                           tag-map   
                           depth         
                           atom?     
                           sev?
                           kv? 
                           x])]
    (merge (:tag-map ret)
           (dissoc ret :tag-map)))) 


(defn new-x
  [depth
   x
   {:keys [coll-type? kv?]
    :as opts+}]
  (let [x (cond
            kv?        (let [[k v] x]
                         [(truncate {:depth depth
                                     :key?  true} k)
                          (truncate {:depth      depth
                                     :map-value? true} v)])
            coll-type? (new-coll2 opts+)
            :else      x)]

    (if (or (:carries-meta? opts+)
            (util/carries-meta? x))
      x
      (symbol (str  x)))))


(defn with-badge-and-ellipsized
  [x kv? mm*]
  (let [badge      (when-not kv? (profile/annotation-badge mm*))
        mm         (merge mm* badge)
        ellipsized (when (:sev? mm)
                     (ellipsize/ellipsized x mm))] 
    (merge mm* ellipsized)))


(defn truncate
  [{:keys [depth map-value? key? user-meta?]
    :as opts}
   x]
  (let [{:keys [x             
                t     
                kv?           
                user-meta         
                coll-type?]
         :as   opts+}
        (truncate-opts opts x)

        new-x 
        (new-x depth x opts+)

        new-coll-info
        (when (and (not kv?) coll-type?)
          (new-coll-info new-x opts+))

        mm*           
        (let [opts+ (dissoc opts+ :uid-entry?)]
          (merge opts+
                 new-coll-info
                 {:og-x  x
                  :t     t
                  :depth depth}
                 (when (and (zero? depth)
                            (:sev? opts+))
                   {:top-level-sev? true})))

        ;; TODO - Use this instead of what you are doing in profile
        ;; mm*
        ;; (with-badge-and-ellipsized
        ;;   x
        ;;   kv?
        ;;   (merge mm*
        ;;          ;; TODO - put this up in mm* binding
        ;;          (keyed [key? map-value?])))

        ret           
        (with-meta new-x
          (merge {:fw/truncated mm*
                  :fw/user-meta user-meta}
                 (when user-meta? {:fw/user-meta-map? user-meta?})))]
    ret))

