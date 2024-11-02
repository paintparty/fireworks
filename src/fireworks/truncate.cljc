(ns ^:dev/always fireworks.truncate
  (:require #?(:cljs [fireworks.macros :refer-macros [keyed]])
            #?(:clj [fireworks.macros :refer [keyed]])
            [clojure.set :as set]
            [clojure.string :as string]
            [fireworks.ellipsize :as ellipsize]
            [fireworks.order :refer [seq->sorted-map]]
            [fireworks.pp :refer [?pp]]
            [fireworks.profile :as profile]
            [fireworks.state :as state]
            [fireworks.util :as util]
            [lasertag.core :as lasertag]))

;; The following set of cljs functions optimizes the printing of js objects.
;; This only applies when the js object is nested within a cljs data structure.
#?(:cljs
   (do 

     (defn- inline-style->map 
       "Converts inline css style string to cljs map"
       [v]
       (as-> v $
         (string/split $ #";")
         (map #(let [kv    (-> % string/trim (string/split #":") )
                     [k v] (map string/trim kv)]
                 [k v]) 
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
                            (= k "class") (into [] (string/split v " "))
                            :else         v)]))))))

     
     (defn- prune-synthetic-base-event 
       "Removes uneeded properties from React synthetic object"
       [x]
       (doto x
         (js-delete "view")
         (js-delete "nativeEvent")
         (js-delete "target")))))


(defn- cljc-atom?
  [x]
  #?(:cljs (= cljs.core/Atom (type x))
     :clj  (= clojure.lang.Atom (type x))))


;; Potential performance gains:
;; Maybe declare truncate new above this and incorporate into kv
;; Mabye use reduce-kv or transducer to speed this up
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

(defn- truncate-iterable
  [{:keys [coll-limit
           map-like?
           array-map?
           depth
           coll-size]}
   x]
  (let [
        ;; First we need to check if collection is both not map-like and 
        ;; comprised only of map entries. If this is the case it is most likely
        ;; the result of something like `(into [] {:a "a" :b "b"})`, and we need
        ;; to treat all elements in the coll as 2-el vectors (not map-entries),
        ;; in the subsequent nested calls to `truncate`. This is done by passing
        ;; a value of `true` for the :map-entry-in-non-map? option. 
        all-map-entries?
        (and (not map-like?)
             (some->> x
                      seq
                      (take (min 50 coll-limit))
                      (every? #(-> % map-entry?))))     

        ret
        (->> x
             (take coll-limit)
             (into [])
             (mapv (partial truncate
                            {:depth                 (inc depth)
                             :map-entry-in-non-map? all-map-entries?})))]
    (if map-like?
      (if (zero? coll-size) ret (seq->sorted-map ret array-map?))
      ret)))


(defn- new-coll-info
  [{:keys [uid-entry?
           coll-size
           all-tags
           map-like?]}
   coll]
  ;; TODO maybe-change binding name to truncated-coll-size -> coll-size?
  (let [truncated-coll-size (count coll)
        coll-size-adjust    (- coll-size (if @uid-entry? 1 0))]
   (merge (keyed [truncated-coll-size coll-size-adjust])
          {:js-typed-array? (contains? all-tags :js/TypedArray)
           :js-map-like?    (contains? all-tags :js/map-like-object)
           :num-dropped     (some->> truncated-coll-size (- coll-size-adjust))}
          (when map-like?
            {:sorted-map-keys (mapv #(nth % 0 nil) coll)}))))


(defn- truncated-coll
  [m x]
  #?(:cljs
     (if (satisfies? ISeqable x)
       (truncate-iterable m x)
       (let [{:keys [t dom-node-type depth]} m]
         (if dom-node-type
           (truncate {:depth (inc depth)} (dom-el-attrs-map x))
           (do 
             (when (= t :SyntheticBaseEvent) (prune-synthetic-base-event x))
             (->> m 
                  js-obj->array-map
                  (map (partial truncate {:depth (inc depth)}))
                  (into {}))))))
     :clj
     (truncate-iterable m x)))


(defn- value-meta [x]
  (when-let [m (meta x)] 
    (when-not (and (list? x) (= '(:line :column) (keys m)))
      m)))


(defn- truncation-profile
  [{:keys [depth map-entry-in-non-map?]
    :as   m*}
   x]
  (let [atom?     (cljc-atom? x)
        x         (if atom? @x x)
        kv?       (boolean (when-not map-entry-in-non-map? (map-entry? x)))
        tag-map   (when-not kv?
                    (set/rename-keys (lasertag/tag-map x) {:tag :t}))
        too-deep? (> depth (:print-level @state/config))
        sev?      (boolean (when-not kv?
                             (not (:coll-type? tag-map))))]
    (merge m* ;; m* added for :key? and :map-value? entries
           (keyed [depth atom? kv? x too-deep? sev?])
           tag-map
           {:user-meta      (value-meta x)
            :og-x           x
            :uid-entry?     (volatile! false)
            :coll-limit     (if too-deep? 0 (:coll-limit @state/config))
            :array-map?     (contains? (:all-tags tag-map) :array-map)
            :top-level-sev? (and sev? (zero? depth))}))) 


(defn- truncated-x*
  [{:keys [coll-type? kv? depth carries-meta?]
    :as m}
   x]
  (let [x (cond
            kv?        
            (let [[k v] x]
              [(truncate {:depth depth :key? true} k)
               (truncate {:depth depth :map-value? true} v)])

            coll-type?
            (truncated-coll m x)

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


;; Make sure to update :fw/truncated entry in example in docstring, if the shape
;; of that value changes.
(defn truncate
  "Example:
   (? {:coll-limit 5} (with-meta (range 8) {:foo :bar}))
   
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
                    :coll-limit          5,
                    :coll-size-adjust    8,
                    :sev?                false,
                    :num-dropped         3,
                    :top-level-sev?      false,
                    :depth               0,
                    :t                   :seq,
                    :x                   '(0 1 2 3 4 5 6 7),
                    :js-typed-array?     false,
                    :atom?               false,
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
        ]
    #_(when (coll? truncated-x)
      (?pp (meta (with-meta truncated-x
                   (merge {:fw/truncated mm*
                           :fw/user-meta user-meta}
                          (some->> m* :user-meta? (hash-map :fw/user-meta-map?)))))))
    (with-meta truncated-x
      (merge {:fw/truncated mm*
              :fw/user-meta user-meta}
             (some->> m* :user-meta? (hash-map :fw/user-meta-map?))))))

