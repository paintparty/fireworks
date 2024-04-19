(ns ^:dev/always fireworks.serialize 
  (:require
   [fireworks.pp :refer [?pp]]
   [fireworks.brackets :refer [closing-bracket! 
                               opening-bracket!
                               closing-angle-bracket!]]
   [fireworks.order :as order]
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.sev :refer [sev!]]
   [fireworks.state :as state]
   [fireworks.tag :as tag :refer [tag! tag-reset! tagged]]
   [fireworks.util :refer [spaces badge-type]]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])))

(declare tagged-val)

(declare reduce-map)                                                                             

(declare reduce-coll)                                                                             


(defn seq->array-map
  [coll]
  (apply array-map (sequence cat coll)))


(defn seq->sorted-set
  [coll]
  (apply (partial sorted-set-by order/rank) coll))


(defn seq->sorted-map
  [coll]
  (if (< 8 (count coll))
    (seq->array-map (sort order/rank coll))
    (if (map? coll)
      coll
      (into {} coll))))


(defn- num-dropped-annotation!
  [{:keys [max-keylen
           indent
           num-dropped
           single-column-map-layout?
           multi-line?
           truncated-coll-size]}]
  (let [coll-is-map?       (boolean max-keylen)
        num-dropped-syntax (str defs/num-dropped-prefix
                                num-dropped)
        map-key-ghost      (string/join
                            (repeat (if (some-> max-keylen pos?)
                                      (min 3 max-keylen)
                                      3)
                                    "."))
        spaces-between-k-v (when coll-is-map?
                             (let [max-keylen+space (inc max-keylen)
                                   num-spaces       (- max-keylen+space
                                                       (count map-key-ghost))]
                               (spaces num-spaces)))
        num-indent-chars   indent      
        indent-chars       (spaces num-indent-chars)      
        extra              (str (tag! :ellipsis)
                                (if single-column-map-layout? 
                                  "\n\n"
                                  (if multi-line?
                                    "\n"
                                    (when-not (zero? truncated-coll-size)
                                      " ")))
                                (when multi-line? indent-chars)
                                (if coll-is-map?
                                  map-key-ghost
                                  num-dropped-syntax)
                                spaces-between-k-v
                                (when coll-is-map? num-dropped-syntax)
                                (tag-reset!))
        locals             (keyed [max-keylen
                                   indent
                                   num-dropped
                                   num-dropped-syntax
                                   coll-is-map?
                                   spaces-between-k-v
                                   extra])]
    extra))


(defn- stringified-bracketed-coll-with-num-dropped-syntax! 
  "This is where the stringified coll (with formatting and escaped style tags)
   gets wrapped in the appropriate brackets, depending on the collection type.
   The `extra` value is the annotation-styled syntax, which conveys
   how many entries were dropped. This will be something like \"...+5\".
   This value is displayed, in the last position of the collection."

  [{:keys [ob ret num-dropped] :as m}]
  (let [extra (when (some-> num-dropped pos?) (num-dropped-annotation! m))
        cb    (closing-bracket! m)
        cb2   (closing-angle-bracket! m)
        ret   (str ob ret extra cb cb2)]
    ret))


(defn- el-with-block-level-badge [x]
  (let [{:keys [badge inline-badge? x]} (meta x)]
    (when (and badge (not inline-badge?)) true)))


(defn- some-elements-have-block-level-badges?
  [x]
  (->> x
       (keep el-with-block-level-badge)
       seq
       boolean))


(defn- reduce-coll-profile
  [coll indent*]
  (let [{:keys [some-elements-carry-user-metadata?  
                single-column-map-layout?
                :fw/custom-badge-style
                str-len-with-badge
                :fw/user-meta
                js-map-like?
                num-dropped         
                map-like?
                record?
                badge
                atom?
                t]
         :as   meta-map}
        (meta coll)
        user-meta    
        (when (seq user-meta) (str user-meta))

        coll-count
        (count coll)

        metadata-position
        (:metadata-position @state/config)

        ;; This is where multi-line for collections is determined
        multi-line?  
        (or (and user-meta
                 (contains? #{:inline "inline"} 
                            metadata-position))
            some-elements-carry-user-metadata?
            (some-elements-have-block-level-badges? coll)
            (boolean 
             (when (< 1 coll-count)
               (or single-column-map-layout?
                   (< (:non-coll-length-limit @state/config)
                      (or str-len-with-badge 0))))))

        ;; This is where indenting for multi-line collections is determined
        t-for-indent
        (cond
          (or record? js-map-like? map-like?)
          :map
          :else
          t) 

        num-indent-spaces-for-t
        (t-for-indent defs/num-indent-spaces)

        indent       
        (+ (or indent* 0)
           (or num-indent-spaces-for-t 1))

        badge-above?
        (some->> badge (contains? defs/inline-badges) not)

        user-meta-above?
        (boolean (and user-meta
                      (contains? #{:block "block"}
                                 metadata-position)))

        indent       
        (or (when-not (or badge-above?
                          user-meta-above?) 
              (some->> badge 
                       count
                            ;; dec
                       (+ (or indent 0))))
            indent)

        ;; Add support for volatile! encapsultion
        indent (if (and atom? (not badge-above?))
                 (+ (or (count (str defs/atom-label defs/encapsulation-opening-bracket))
                        0)
                    indent)
                 indent)

        ;; Badge on its own line above data-structure.
        ;; for records, classes, js/Set, js/Map etc.
        annotation-newline
        (when (or badge-above? user-meta-above?) 
          (str "\n"
               (when-not (zero? indent*)
                 (spaces (- indent
                            num-indent-spaces-for-t)))))

        maybe-comma  
        (when js-map-like? ",")

        separator    
        (if multi-line?
          (str maybe-comma "\n" (spaces indent))
          (str maybe-comma " "))

        ret          
        (keyed [num-dropped 
                str-len-with-badge     
                multi-line? 
                user-meta-above?
                metadata-position
                atom?
                separator   
                coll-count
                indent
                badge
                user-meta
                annotation-newline
                record?
                custom-badge-style])]
      ret))


(defn- profile+ob
  [coll indent*]
  (let [{:keys [badge
                custom-badge-style
                annotation-newline
                user-meta
                record?
                atom?
                separator]
         :as m} 
        (reduce-coll-profile coll indent*)
        
        ;; Atom-wrapper opening made here
        atom-opening-encapsulation
        (when atom? 
          (tagged (str defs/atom-label
                       defs/encapsulation-opening-bracket) 
                  {:theme-token :atom-wrapper}))

        ;; Block-level badges for colls (display above coll) are made here
        badge               
        (when badge
          (let [theme-token (badge-type badge)
                pred        true]
            (tagged badge (keyed [theme-token pred custom-badge-style]))))

        metadata-position
        (:metadata-position @state/config)

        ;; move up?
        user-meta-block
        (when (and (:display-metadata? @state/config)
                   user-meta
                   (contains? #{:block "block"} metadata-position))
          (as-> user-meta $
            (str
             (when badge " ")
             (tag! :metadata)
             (tag/stringified-user-meta 
              {:user-meta         $
               :metadata-position :block
               :indent            indent*

               ;; maybe use this if you have a coll that is single-line,
               ;; but with multi-line metadata map

               ;; :str-len-with-badge str-len-with-badge
               })
             (tag-reset!))))

        ob*                  
        (str atom-opening-encapsulation
             badge
             (some-> user-meta-block (str (when badge " ")))
             annotation-newline
             (opening-bracket! (keyed [coll record?]))
             (when (-> coll meta :too-deep?)
               (tagged "#" {:theme-token :max-print-level-label})))
        
        
        user-meta-inline
        (when (and (:display-metadata? @state/config)
                   user-meta
                   (contains? #{:inline "inline"} metadata-position))
          (as-> user-meta $
            (str
             (when badge " ")
             (tag! :metadata)
             (tag/stringified-user-meta 
              {:user-meta         $
               :metadata-position :inline
               :indent            indent*
               ;; maybe use this if you have a coll that is single-line,
               ;; but with multi-line metadata map

               ;; :str-len-with-badge str-len-with-badge
               })

             (tag-reset!))))
        
        ob (str ob*
                (some-> user-meta-inline (str  separator)))]
    (assoc m :ob ob :record? record?)))

(defn- color-result-gutter-space-char
  [separator]
  (str "\n"
       (tag/tag-entity! " " :result-gutter)
       (subs separator 2)))

(defn- color-result-gutter-space-char-two-lines
  [separator]
  (let [num-newlines (->> separator (re-seq #"\n") count)
        _            (dotimes [_ num-newlines]
                       (tag/tag-entity! " " :result-gutter))
        ret*         (string/replace separator
                                     #"\n"
                                     "\n%c %c")
        ret          (if (> num-newlines 1)
                       (subs ret* 0 (dec (count ret*)))
                       ret*)]
    ret))

(defn- reduce-coll
  [coll indent*]
  (let [{:keys [some-colls-as-keys?
                some-syms-carrying-metadata-as-keys?
                single-column-map-layout?
                truncated-coll-size
                t
                js-typed-array?]
         :as   meta-map}
        (meta coll) 

        coll                
        (if single-column-map-layout?
          (with-meta (sequence cat coll) meta-map)
          coll)

        {:keys [coll-count         
                num-dropped 
                separator
                indent
                multi-line?
                ob]
         :as b}     
        (profile+ob coll indent*)

        ret                 
        (string/join
         (map-indexed
          (fn [idx v]
            (let [val-props   (meta v)
                  tagged-val  (tagged-val (keyed [v val-props indent]))
                  map-value?  (and single-column-map-layout?
                                   (odd? idx))
                  maybe-comma (when (or js-typed-array?
                                        (contains? #{:js/Array :js/Set} t))
                                ",")
                  separator   (cond map-value?
                                    (str separator separator)
                                    :else
                                    (str maybe-comma
                                         separator))
                  ret         (str
                               tagged-val
                               (when-not (= coll-count (inc idx)) 
                                 (if multi-line?
                                   separator
                                   #_(if map-value? 
                                     (color-result-gutter-space-char-two-lines separator)
                                     (color-result-gutter-space-char separator))
                                   separator)))]
              ret))
          coll))
        
        ret                 
        (stringified-bracketed-coll-with-num-dropped-syntax!
         (keyed [coll
                 ob
                 ret
                 indent
                 num-dropped
                 single-column-map-layout?
                 some-colls-as-keys?
                 some-syms-carrying-metadata-as-keys?
                 multi-line?
                 truncated-coll-size]))]
    ret))


(defn- reduce-map*
  [{:keys [untokenized
           max-keylen
           indent
           coll-count
           separator
           multi-line?]}
  idx
  [_ v]]
  (let [[key-props val-props]
        (nth untokenized idx)

        indent               
        (+ (or max-keylen 0) (or defs/kv-gap 0) (or indent 0))

        {escaped-key    :escaped
         key-char-count :ellipsized-char-count}
        (sev! (merge key-props {:indent indent}))

        tagged-val          
        (tagged-val (keyed [v val-props indent]))

        num-extra-spaces-after-key
        (if multi-line?
          (- (or max-keylen 0) (or key-char-count 0))
          0)

        ret                  
        (str escaped-key
             (when (some-> num-extra-spaces-after-key pos?) 
               (spaces num-extra-spaces-after-key))
             (spaces defs/kv-gap)
             tagged-val
             (when-not (= coll-count (inc idx))
               separator
               #_(color-result-gutter-space-char separator)))]
    ret))


(defn- reduce-map
  [coll indent]
  (let [{:keys [coll-count         
                num-dropped 
                separator
                indent
                multi-line?
                ob
                record?]}     
        (profile+ob coll indent)

        untokenized
        (map (fn [[k v]]
               (let [key-props (meta k)
                     val-props (meta v)]
                 [key-props val-props]))
             coll)

        max-keylen
        (->> untokenized 
             (map #(-> %
                       first
                       :ellipsized-char-count))
             (apply max))

        ret        
        (string/join
         (map-indexed
          (partial reduce-map*
                   (keyed [untokenized
                           max-keylen
                           indent
                           coll-count
                           separator
                           multi-line?]))
          coll))

        ret        
        (stringified-bracketed-coll-with-num-dropped-syntax!
         (keyed [coll
                 ob
                 ret
                 indent
                 num-dropped
                 max-keylen
                 multi-line?
                 record?]))]
     ret))


(defn- tagged-val
  [{:keys [v val-props t indent]}]
  (let [t                          
        (or t (:t val-props))

        {:keys [map-like?
                single-column-map-layout?
                coll-type?]}                         
         val-props
        
        source-is-scalar?
        (nil? t)]
    
    (cond
      source-is-scalar?
      v
      
      map-like?
      (if single-column-map-layout?
        (reduce-coll v indent)
        (reduce-map v indent))

      coll-type?
      (reduce-coll v indent)

      :else
      (:escaped (sev! (assoc val-props :indent indent))))))


(defn serialized
  [v]
  (let [ret (str #_(tag/tag-entity! " " :result-gutter-start)
                 (tagged-val {:v         v
                              :indent    0
                              :val-props (meta v)}))]
    ret))
