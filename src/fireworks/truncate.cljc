(ns ^:dev/always fireworks.truncate
  (:require #?(:cljs [fireworks.macros :refer-macros [keyed]])
            #?(:clj [fireworks.macros :refer [keyed]])
            [fireworks.pp :refer [?pp]]
            [clojure.string :as string]
            [fireworks.ellipsize :as ellipsize]
            [fireworks.order :refer [seq->sorted-map]]
            [fireworks.profile :as profile]
            [fireworks.state :as state]
            [fireworks.util :as util]))

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
                            (= k "class") (into [] (string/split v " "))
                            :else         v)]))))))

     
     (defn- prune-synthetic-base-event 
       "Removes uneeded properties from React synthetic object"
       [x]
       (doto x
         (js-delete "view")
         (js-delete "nativeEvent")
         (js-delete "target")))))


;; Maybe this could go in Lastertag?
(defn- cljc-atom?
  [x]
  #?(:cljs (= cljs.core/Atom (type x))
     :clj  (= clojure.lang.Atom (type x))))


;; Potential performance gains:
;; Maybe declare truncate new above this and incorporate into kv
;; Maybe use reduce-kv or transducer to speed this up
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
           coll-size
           transient?
           path]}
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
        (let [taken (->> x (take coll-limit) (into []))
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
                (range (count taken))))
        #_(->> x
             (take coll-limit)
             (into [])
             ;; truncate call
             (mapv (partial truncate
                            {:depth                 (inc depth)
                             :path                  path
                             :map-entry-in-non-map? all-map-entries?}))
             (into []))]
    (if map-like?
      (if (or (zero? coll-size)
              transient?)  ; treat transient maps as empty map
        ret
        (seq->sorted-map ret array-map?))
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
       (let [{:keys [t
                     all-tags
                     dom-node-type
                     path
                     depth
                     js-array?
                     coll-size
                     coll-limit]}
             m]
         (cond
           dom-node-type
           (truncate {:depth (inc depth) :path path}
                     (dom-el-attrs-map x))

           (or js-array? (contains? all-tags :js/TypedArray))
           (mapv 
             (fn [i]
               ;; truncate call
               (truncate {:depth (inc depth)
                          :path path}
                         (aget x i)))
             (range (min coll-limit coll-size)))

           :else
           (do 
             (when (= t :SyntheticBaseEvent) (prune-synthetic-base-event x))
             (->> m 
                  js-obj->array-map
                  ;; truncate call
                  (mapv (partial truncate {:depth (inc depth) :path path}))
                  (into {}))))))
     :clj
     (truncate-iterable m x)))


(defn- value-meta [x]
  (when-let [m (meta x)] 
    (when-not (and (list? x) (= '(:line :column) (keys m)))
      m)))


(defn- reify-if-transient [x tag-map]
  (if (:transient? tag-map)
    (let [t (:t tag-map)]
      (cond
        (= t :map)
        {}
        (= t :set)
        #{}
        :else
        (for [n (take (:coll-limit @state/config) (range (count x)))]
          (nth x n))))
    x))


(defn- truncation-profile
  [{:keys [path depth map-entry-in-non-map?]
    :as   m*}
   x]
  (let [val-is-atom?     (cljc-atom? x)
        val-is-volatile? (volatile? x) 
        x                (if (or val-is-atom? val-is-volatile?) @x x)
        kv?              (boolean (when-not map-entry-in-non-map? (map-entry? x)))
        tag-map          (when-not kv? (util/tag-map* x))
        x                (reify-if-transient x tag-map)
        too-deep?        (> depth (:print-level @state/config))

        sev?             (boolean (when-not kv?
                                    (not (:coll-type? tag-map))))]

    (merge m* ;; m* added for :key? and :map-value? entries
           (keyed [val-is-atom?
                   val-is-volatile?
                   too-deep?
                   depth
                   path
                   sev?
                   kv?
                   x])
           tag-map
           {:user-meta      (value-meta x)
            :og-x           x
            :uid-entry?     (volatile! false)
            :coll-limit     (if too-deep? 0 (:coll-limit @state/config))
            :array-map?     (contains? (:all-tags tag-map) :array-map)
            :top-level-sev? (and sev? (zero? depth))}))) 


(defn- truncated-x*
  [{:keys [coll-type? kv? depth carries-meta? classname path]
    :as m}
   x]
  ;; (println "\n\nx in truncated-x*" x)
  (let [x (cond
            kv?        
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


            coll-type?
            (truncated-coll m x)

            (= classname "java.math.BigDecimal")
            (symbol (str x "M"))

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
        ]
    (with-meta truncated-x
      (merge {:fw/truncated mm*
              :fw/user-meta user-meta}
             (some->> m* :user-meta? (hash-map :fw/user-meta-map?))))))

