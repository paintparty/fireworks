(ns ^:dev/always fireworks.profile
  (:require
   [clojure.set :as set]
   [fireworks.defs :as defs]
   [fireworks.ellipsize :as ellipsize]
   [fireworks.state :as state]
   [fireworks.pp :as pp :refer [?pp]]
   [lasertag.core :as lasertag]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [clojure.string :as string]))


(def badges-by-lasertag
  {:js/Set         "js/Set"
   :js/Promise     "js/Promise"
   :js/Iterator    defs/js-literal-badge
   :js/Object      defs/js-literal-badge
   :js/Array       defs/js-literal-badge
   :lamda          defs/lamda-symbol
   :uuid           defs/uuid-badge
   :js/Date        defs/inst-badge
   :java.util.Date defs/inst-badge})


;; Understand and doc how this works for custom types
(defn- annotation-badge
  [{:keys [t
           lamda?
           js-built-in-object?
           js-built-in-object-name
           all-tags
           :fw/custom-badge-text] :as m}]
  (let [t (if lamda? :lamda t)
        b (or custom-badge-text
              (cond
                (contains? all-tags :record)
                (name t)

                js-built-in-object?
                (str "js/" js-built-in-object-name)

                (and (not= t :js/Object)
                     (or (contains? all-tags :js/map-like-object)
                         (contains? all-tags :js/TypedArray)))
                (or (t badges-by-lasertag)
                    (subs (str t) 1))

                :else
                (t badges-by-lasertag)))
        b #?(:cljs b
             :clj (if (= t :defmulti) "Multimethod" b))]

    ;; If you want to signal React -> (str "⚛ " b)

    (when b {:badge b})))
                         

(defn- highlighting*
  [x m]
  (let [{:keys [pred style]} m]
    (when (pred x)
      {:highlighting style})))


(defn- highlighting
  [x]
  (when-let [hl @state/highlight]
    (cond 
      (map? hl)
      (highlighting* x hl)
      (vector? hl)
      (some (partial highlighting* x) hl))))


(defn- str-len-with-badge
  [badge atom? x]
  (let [extra-str-from-custom-badges
        (when (coll? x)
          (reduce (fn [acc el]
                    (if-not (-> el :fw/truncated)
                      (+ acc (or (-> el
                                     meta
                                     :fw/custom-badge-text
                                     count)
                                 0))
                      acc))
                  0
                  x))

        badge-len
        (or (some-> badge :badge count) 0)

        val-len
        (or (-> x str count) 0)

        encapsulation-closing-bracket-len
        (or (when atom? 
              (count defs/encapsulation-closing-bracket))
            0)
        extra-str-from-custom-badges-len
        (or extra-str-from-custom-badges 0)]

    #_(when-not @state/formatting-form-to-be-evaled?
      (?pp
       (keyed [badge-len
               x
               val-len
               encapsulation-closing-bracket-len
               extra-str-from-custom-badges-len ])))

    (+ badge-len
       val-len
       encapsulation-closing-bracket-len
       extra-str-from-custom-badges-len)))


(defn maybe-ellipsize
  [{:keys [coll-type? ellipsized x t meta-map] :as m}]
  (let [ret* (cond 
               coll-type?
               x

               (contains? #{:function :defmulti :java.lang.Class} t)
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
              (let [badge-count
                    (or (some-> meta-map :badge count) 0)

                    atom-wrap-count
                    (or (when (some-> meta-map :atom?) defs/atom-wrap-count) 0)

                    ellipsized-char-count
                    (or (some-> ellipsized :ellipsized-char-count) 0)

                    ellipsis-char-count
                    (or (when (:exceeds? ellipsized) defs/ellipsis-count) 0)

                    str-len-with-badge2
                    (+ badge-count
                       atom-wrap-count
                       ellipsized-char-count
                       ellipsis-char-count)

                    ;; _ (when-not @state/formatting-form-to-be-evaled?
                    ;;     (?pp x)
                    ;;     (?pp (keyed [m
                    ;;                  badge-count
                    ;;                  atom-wrap-count
                    ;;                  ellipsized-char-count
                    ;;                  str-len-with-badge2])))

                    meta-map            
                    (assoc meta-map
                           :str-len-with-badge2
                           str-len-with-badge2)]
                (with-meta ret* meta-map)))]
    ret))

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


(defn meta-map*
  [x mapkey]
  (let [mm (meta x)]
   (if (:fw/pre-profiled-mapkey mm)
     mm
     (let [{:keys [og-x atom?]
            :as fw-truncated-meta}  (or (some-> x meta :fw/truncated)
                                        ;; only for map-entries (kvs)
                                        (-> x
                                            lasertag/tag-map
                                            (set/rename-keys {:tag :t})))
           ret   (merge 
                  fw-truncated-meta
                  {:x    (or (:x fw-truncated-meta) x)
                   :og-x og-x}

                  (highlighting og-x)

                     ;; TODO - need this mapkey map?
                  mapkey

                     ;; TODO - after getting custom printing working
                     ;; refactor this :fw/type-after-custom-printing
                     ;; into select-keys vec and use rename-keys
                  (some->> x
                           meta
                           :fw/type-after-custom-printing 
                           (hash-map :t))
                  (some-> x
                          meta
                          (select-keys [:fw/custom-badge-style
                                        :fw/custom-badge-text
                                        :fw/user-meta])))

            ;; TODO - make dedicated badge fn?
           badge (annotation-badge ret)
           ret   (let [some-colls-as-keys?                  
                       (some-colls-as-keys? x)

                       some-syms-carrying-metadata-as-keys?
                       (some-syms-carrying-metadata-as-keys? x)

                       single-column-map-layout?            
                       (or some-colls-as-keys?
                           some-syms-carrying-metadata-as-keys?)

                       inline-badge?
                       (and badge
                            (contains? defs/inline-badges (:badge badge)))]
                   (merge ret
                          badge
                          (when badge {:inline-badge? inline-badge?})
                          (keyed [some-colls-as-keys?
                                  some-syms-carrying-metadata-as-keys?
                                  single-column-map-layout?])
                          {:str-len-with-badge            
                           (str-len-with-badge badge atom? x)
                           
                           :some-elements-carry-user-metadata?
                           (some-elements-carry-user-metadata? x)}))]

        ret))))


(defn re-profile
  "Reprofile to deal with map keys"
  [x meta-map f]
  (if (:map-like? meta-map) 
    (into []
          (map-indexed 
           (fn [i [k v]]
             (let [k (f k {:key?             true
                           :js-map-like-key? (:js-map-like? meta-map)
                           :index            i})]
               [k v]))
           x))
    x))

(defn profile
  ([x]
   (profile x nil))
  ([x {:keys [key? js-map-like-key? index] :as mapkey}]
   ;; TODO - doc when (-> x meta :ellipsized-char-count) happen?
   (if (-> x meta :ellipsized-char-count)
     x
     (let [meta-map*
          (meta-map* x mapkey)

           ellipsized
           (when-not (:coll-type? meta-map*)
             (ellipsize/ellipsized x meta-map*))
           
           meta-map
           (merge 
                  meta-map*
                  ellipsized
                  (when (contains? (:all-tags meta-map*) :record)
                    {:record? true})
                  (when key? {:key? true})
                  (when js-map-like-key? {:js-map-like-key? true})
                  (when key? {:fw/pre-profiled-mapkey true})
                  (when index {:index index}) )

           x
           (re-profile x meta-map profile)
           
           {:keys [coll-type? t]}
           meta-map

           ret
           (maybe-ellipsize (keyed [coll-type? ellipsized x t meta-map]))]

       ret))))
