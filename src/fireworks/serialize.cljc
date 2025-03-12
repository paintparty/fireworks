(ns ^:dev/always fireworks.serialize 
  (:require
   [clojure.walk :as walk]
   [fireworks.profile :as profile]
   [fireworks.pp :refer [?pp]]
   [fireworks.truncate :as truncate]
   [fireworks.brackets
    :as brackets
    :refer [closing-bracket! 
            opening-bracket!
            closing-angle-bracket!
            brackets-by-type]]
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.state :as state]
   [fireworks.tag :as tag :refer [tag! tag-reset! tagged]]
   [fireworks.util :refer [spaces badge-type]]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [fireworks.util :as util]))

(declare tagged-val)

(declare reduce-map)                                                                             

(declare reduce-coll)                                                                             

(declare formatted*)                                                                             

(defn collection-type? [k]
  (contains? #{:set
               :map
               :js/Array 
               :js/Object
               :interop
               :record
               :vector
               :list
               :seq}
             k))

(defn add-truncation-annotation! 
  [{:keys [num-chars-dropped t truncate-fn-name? top-level-sev?]
    :as m}]
  (when (or truncate-fn-name?
            (some-> num-chars-dropped pos?))
    (let [theme-tag (if (pos? (state/formatting-meta-level))
                      (state/metadata-token)
                      :ellipsis)
          ;; TODO/perf - replace collection-type? with (:coll-type? m)
          s         (if (collection-type? t)
                      (str "+" num-chars-dropped)
                      defs/ellipsis)]

      (when (state/debug-tagging?)
        (println "\nserialize/add-truncation-annotation:  tagging \""
                 s 
                 "\" with " 
                 theme-tag ))

      (if top-level-sev?
        s
        (str (tag! theme-tag) s (tag-reset!))))))

(defn sev-user-meta-position-match? [user-meta position]
  (and (:display-metadata? @state/config)
       (seq user-meta)
       (contains? (if (= position :block)
                    #{:block "block"}
                    #{:inline "inline"})
                  (:metadata-position @state/config))))


(defn sev!
  "Creates a string with the properly placed \"%c\" (or sgr) formatting tags.
   The tag! and tag-reset! fns mutate the styles atom for syntax coloring."
  [{:keys [x
           s
           t
           key?
           sev?
           badge
           indent
           fn-args
           all-tags
           separator
           max-keylen
           multi-line?
           val-is-atom?
           number-type?
           highlighting
           :fw/user-meta
           fn-display-name
           val-is-volatile?
           js-map-like-key?
           num-chars-dropped
           str-len-with-badge
           ellipsized-char-count
           :fw/custom-badge-style]
    :as m}]
  (let [encapsulated?
        (or (= t :uuid) (contains? all-tags :inst))

        mutable-tagging-opts
        (when (or val-is-atom? val-is-volatile?)
          {:theme-token  (if val-is-atom? :atom-wrapper :volatile-wrapper)
           :display?     true
           :highlighting highlighting})

        ;; State mutation start  ---------------------------------------------
        
        ;; The next set of bindings mutate state/styles.
        ;; They must happen in the following order:
        
        ;; user-meta-block (displays user-meta above value), optional
        ;; atom-opening,  optional
        ;; annotation (badge), optional
        ;; value tag
        ;; num-chars-dropped-syntax
        ;; value tag-reset 
        ;; fn-args-tag, optional
        ;; fn-args-tag reset, optional
        ;; atom-closing, optional
        ;; user-meta-block (displays user-meta inline, after value), optional
        
        user-meta-block-tagged
        (when (sev-user-meta-position-match? user-meta :block)
          (swap! state/*formatting-meta-level inc)
          (let [ret (formatted* user-meta {:indent     indent
                                           :user-meta? true})]
            (swap! state/*formatting-meta-level dec)
            ret))
        
        user-meta-block-tagged-separator
        (when (and user-meta-block-tagged
                   multi-line?)
          (tagged (str separator
                       (some-> max-keylen
                               (+ defs/kv-gap)
                               spaces))))

        atom-tagged
        (some->> mutable-tagging-opts
                 (tagged (str (if val-is-volatile? defs/volatile-label defs/atom-label)
                              defs/encapsulation-opening-bracket)))

        badge-tagged
        (tagged badge
                {:custom-badge-style custom-badge-style 
                 :theme-token        (cond
                                       (pos? (state/formatting-meta-level))
                                       (state/metadata-token)
                                       (= badge defs/lambda-symbol) 
                                       :lambda-label 
                                       :else
                                       :literal-label)})

        theme-tag
        (cond
          number-type?
          :number
          js-map-like-key?
          :js-object-key
          encapsulated?
          :string
          :else
          t)

        theme-tag
        (let [meta-level (state/formatting-meta-level)]
          (if (pos? meta-level)
            (let [l2? (= 2 meta-level)]
              (if key?
                (if l2? :metadata-key2 :metadata-key)
                (if l2? :metadata2 :metadata)))
            theme-tag))

        _ 
        (when (state/debug-tagging?)
            (println "\nsev!   tagging " s " with " theme-tag))

        main-entity-tag                  
        (tag! theme-tag highlighting)

        ;; Additional tagging (and atom mutation) happens within
        ;; fireworks.serialize/add-truncation-annotation!
        chars-dropped-syntax 
        (add-truncation-annotation! m)

        main-entity-tag-reset            
        (tag-reset! (if (pos? (state/formatting-meta-level))
                      (state/metadata-token)
                      :foreground))

        fn-args-tagged
        (tagged fn-args {:theme-token :function-args})

        atom-closing-bracket-tagged

        (some->> mutable-tagging-opts 
                 (tagged defs/encapsulation-closing-bracket))

        user-meta-inline-tagged
        (when (sev-user-meta-position-match? user-meta :inline)
          (swap! state/*formatting-meta-level inc)
          (let [offset        defs/metadata-position-inline-offset
                inline-offset (tagged (spaces (dec offset))
                                      {:theme-token (state/metadata-token)})
                ret           (formatted*
                               user-meta
                               {:indent     (+ indent
                                               offset
                                               (or ellipsized-char-count 0))
                                :user-meta? true})]
            (swap! state/*formatting-meta-level dec)
            ;; TODO - figure out how to create left arrow char with foreground
            ;; color of metadata background.
            (str " " inline-offset ret)))

        ;; Atom mutation end  ------------------------------------------------
        
        
        

        ;; Putting all the colorization tagged bits together
        escaped              
        (str 
         ;; Optional, conditional metadata of coll element
         ;; positioned block-level, above element
         ;; :metadata-postition must be set to :block (in user config)
         user-meta-block-tagged

         ;; Only when user-meta-block-tagged is present
         user-meta-block-tagged-separator
         
         ;; Conditional `Atom`, positioned inline, to left of value 
         atom-tagged

         ;; Conditional `badge` (for `#uuid`, or `#inst`),
         ;; positioned inline, to left of value 
         badge-tagged

         ;; The self-evaluating value
         main-entity-tag
         (or s fn-display-name)
         chars-dropped-syntax
         main-entity-tag-reset

         ;; Conditional fn-args, positioned inline, to right of value 
         fn-args-tagged         

         ;; Conditional `Atom`, closing parts,
         ;; positioned inline, to right of value 
         atom-closing-bracket-tagged

         ;; Optional, conditional metadata of coll element
         ;; positioned inline, to right of element
         ;; Default position.
         ;; Will not display if :metadata-postition is set
         ;; explicitly to :block (in user config).
         user-meta-inline-tagged)

        ret                  
        (keyed [ellipsized-char-count
                num-chars-dropped
                fn-display-name
                escaped
                x 
                s
                t])
        locals (merge ret
                      (keyed [main-entity-tag
                              chars-dropped-syntax
                              main-entity-tag-reset]))]
    ret))


;;                               ooo OOO OOO ooo
;;                           oOO                 OOo
;;                       oOO                         OOo
;;                    oOO                               OOo
;;                  oOO                                   OOo
;;                oOO                                       OOo
;;               oOO                                         OOo
;;              oOO                                           OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;             oOO                                             OOo
;;              oOO                                           OOo
;;               oOO                                         OOo
;;                oOO                                       OOo
;;                  oOO                                   OOo
;;                    oO                                OOo
;;                       oOO                         OOo
;;                           oOO                 OOo
;;                               ooo OOO OOO ooo


;;;; For dealing with collections

(defn- leading-space
  [m]
  (if (:single-column-map-layout? m) 
    "\n\n"
    (if (:multi-line? m)
      "\n"
      (when-not (some-> m :truncated-coll-size zero?)
        (when-not (:too-deep? m) " ")))))

(defn- map-key-ghost 
  [{:keys [too-deep? max-keylen]}]
  (when-not too-deep?
                             (string/join
                              (repeat (if (some-> max-keylen pos?)
                                        (min 3 max-keylen)
                                        3)
                                      "."))))


(defn- num-dropped-annotation!
  [{:keys [indent
           too-deep?
           max-keylen
           multi-line?
           num-dropped]
    :as m}]
  (let [num-dropped-syntax (str (when-not too-deep? defs/ellipsis)
                                "+"
                                num-dropped)
        map-key-ghost      (map-key-ghost m)
        coll-is-map?       (boolean max-keylen)
        spaces-between-kv  (when coll-is-map?
                             (let [max-keylen+space (inc max-keylen)]
                               (spaces (if too-deep?
                                         0
                                         (- max-keylen+space
                                            (count map-key-ghost))))))
        num-indent-chars   indent      
        indent-chars       (spaces num-indent-chars)      
        extra*             (let [leading+indent (str (leading-space m)
                                                     (when multi-line?
                                                       indent-chars))]
                             (if coll-is-map?
                               (str leading+indent
                                    map-key-ghost
                                    spaces-between-kv
                                    num-dropped-syntax)
                               (str leading+indent
                                    num-dropped-syntax
                                    spaces-between-kv)))]

    (when (state/debug-tagging?)
      (println "\nnum-dropped-annotion! : tagging " extra* " with " :ellipsis))

    (str (tag! :ellipsis) extra* (tag-reset!))))


(defn- stringified-bracketed-coll-with-num-dropped-syntax! 
  "This is where the stringified coll (with formatting and escaped style tags)
   gets wrapped in the appropriate brackets, depending on the collection type.
   The `extra` value is the annotation-styled syntax, which conveys
   how many entries were dropped. This will be something like \"...+5\".
   This value is displayed, in the last position of the collection."

  [{:keys [ob ret num-dropped let-bindings?] :as m}]
  (let [extra (when (some-> num-dropped pos?)
                (num-dropped-annotation! m))
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
                ;; str-len-with-badge-ellipsized
                ;; str-len-val-ellipsized
                ;; str-len-with-badge
                val-str-len
                ;; badge-str-len
                single-column-map-layout?
                :fw/custom-badge-style
                str-len-with-badge
                :fw/user-meta-map?
                val-is-volatile?
                :fw/user-meta
                js-map-like?         
                val-is-atom?
                num-dropped
                too-deep?
                set-like?
                record?
                badge
                depth
                t]
         :as   meta-map}
        (meta coll)

        user-meta    
        (when (seq user-meta) user-meta)

        coll-count
        (count coll)

        metadata-position
        (:metadata-position @state/config)

        ;; This is where multi-line for collections is determined
        ;; _ (?pp (select-keys meta-map [:str-len-with-badge-ellipsized
        ;;                               :str-len-val-ellipsized
        ;;                               :str-len-with-badge
        ;;                               :val-str-len
        ;;                               :badge-str-len]))
        
        badge-above?
        (some->> badge (contains? defs/inline-badges) not)

        user-meta-above?
        (boolean (and user-meta
                      (contains? #{:block "block"}
                                 metadata-position)))
        multi-line?  
        (or (and user-meta
                 (contains? #{:inline "inline"} 
                            metadata-position))
            some-elements-carry-user-metadata?
            (some-elements-have-block-level-badges? coll)
            (boolean 
             (when (< 1 coll-count)
               (or single-column-map-layout?
                   (< (:single-line-coll-length-limit @state/config)
                      (or (if badge-above?
                            val-str-len        ;; <- should this be str-len-val-ellipsized?
                            str-len-with-badge ;; <- should this be str-len-with-val-ellipsized?
                            )
                          0))))))

        ;; This is where indenting for multi-line collections is determined
        num-indent-spaces-for-t
        (if (or set-like? user-meta-map? (= t :js/Set)) 2 1)

        indent       
        (+ (or indent* 0)
           num-indent-spaces-for-t)


        indent       
        (or (when-not (or badge-above?
                          user-meta-above?) 
              (some->> badge 
                       count
                       ;; dec
                       (+ (or indent 0))))
            indent)

        ;; Add support for volatile! encapsulation
        indent (if (and (or val-is-atom? val-is-volatile?) (not badge-above?))
                 (+ (or (count (str (if val-is-atom?
                                      defs/atom-label
                                      defs/volatile-label)
                                    defs/encapsulation-opening-bracket))
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
          (str maybe-comma
               "\n"
               (spaces indent))
          (str maybe-comma " "))

        let-bindings?
        (and (zero? depth)
             @state/let-bindings?)

        ret          
        (keyed [custom-badge-style 
                str-len-with-badge     
                annotation-newline 
                metadata-position
                user-meta-above?
                val-is-volatile?
                let-bindings?
                val-is-atom?   
                num-dropped
                multi-line?
                coll-count
                separator
                user-meta
                too-deep?
                record?
                indent
                badge])]
      ret))


(defn- user-meta-block
  [indent*
   metadata-position
   {:keys [badge user-meta] :as m}]
  (when (and (:display-metadata? @state/config)
             user-meta
             (contains? #{:block "block"} metadata-position))
    (swap! state/*formatting-meta-level inc)
    (let [ret (formatted* user-meta {:user-meta? true :indent indent*})]
      (swap! state/*formatting-meta-level dec)
      ret)))

(defn- user-meta-inline
  [{:keys [user-meta
           metadata-position
           mutable-opening-encapsulation
           badge
           coll
           indent*]}]
  (when (and (:display-metadata? @state/config)
                   (seq user-meta)
                   (contains? #{:inline "inline"} metadata-position))
    (swap! state/*formatting-meta-level inc)
    (let [offset        defs/metadata-position-inline-offset
          inline-offset (tagged (spaces (dec offset))
                                {:theme-token (state/metadata-token)})
          ret           (formatted* 
                          user-meta
                          {:user-meta?
                           true
                           :indent 
                           (let [ob (str mutable-opening-encapsulation
                                         badge
                                         (some-> coll
                                                 meta
                                                 brackets-by-type
                                                 first))

                                 indent+ob
                                 (+ (or (count ob) 0) indent*)]
                            (+ indent+ob offset))})]
      (swap! state/*formatting-meta-level dec)
      (str " " inline-offset ret))))

(defn- profile+ob
  [coll indent*]
  (let [{:keys [badge
                record?
                user-meta
                separator
                val-is-atom?
                let-bindings?
                val-is-volatile?
                annotation-newline
                custom-badge-style]
         :as m} 
        (reduce-coll-profile coll indent*)

        ;; Mutable-wrapper opening made here
        mutable-opening-encapsulation
        (when (or val-is-atom? val-is-volatile?) 
          (tagged (str (if val-is-atom? defs/atom-label defs/volatile-label)
                       defs/encapsulation-opening-bracket) 
                  {:theme-token (if val-is-atom? :atom-wrapper :volatile-wrapper)}))

        ;; Block-level badges for colls (display above coll) are made here
        badge               
        (when badge
          (let [theme-token (badge-type badge)
                pred        true]
            (tagged badge
                    (keyed [theme-token
                            pred
                            custom-badge-style]))))

        metadata-position
        (:metadata-position @state/config)

        ;; Move up?
        user-meta-block
        (user-meta-block indent* metadata-position m)

        ob* 
        (str mutable-opening-encapsulation
             badge
             (some-> user-meta-block (str (when badge " ")))
             annotation-newline
             (opening-bracket! (keyed [coll record? let-bindings?]))
             (when (-> coll meta :too-deep?)
               (tagged "#" {:theme-token :max-print-level-label})))
        
        user-meta-inline
        (user-meta-inline (keyed [mutable-opening-encapsulation
                                  metadata-position
                                  user-meta
                                  indent*
                                  badge
                                  coll]))

        ob
        (str ob* (some-> user-meta-inline
                         (str (tagged separator
                                      {:theme-token :foreground}))))]
    (assoc m :ob ob :record? record?)))


(defn- reduce-coll
  [coll indent*]
  (let [{:keys [t
                js-typed-array?
                truncated-coll-size                  ;; <- remove?
                some-colls-as-keys?                  ;; <- remove?
                single-column-map-layout?
                some-syms-carrying-metadata-as-keys? ;; <- remove?
                ]
         :as   meta-map}
        (meta coll) 

        coll                
        (if single-column-map-layout?
          (with-meta (sequence cat coll) meta-map)
          coll)

        {:keys [ob         
                indent 
                separator
                too-deep?      ; <-remove
                coll-count
                num-dropped    ; <-remove
                multi-line?
                let-bindings?  ; <-remove
                ]
         :as profile+ob}
        (profile+ob coll indent*)

        ret                 
        (string/join
         (map-indexed
          (fn [idx v]
            (let [val-props
                  (meta v)

                  tagged-val
                  (tagged-val (keyed [v
                                      val-props
                                      indent
                                      multi-line?
                                      separator]))

                  value-in-mapentry?
                  (and single-column-map-layout? (odd? idx))

                  maybe-comma
                  (when (or js-typed-array?
                            (contains? #{:js/Array :js/Set} t))
                    ",")

                  separator
                  (str (if value-in-mapentry? separator maybe-comma)
                       separator)

                  ret         
                  (str
                   tagged-val
                   (when-not (= coll-count (inc idx)) 
                     (if multi-line?
                       (tagged separator {:theme-token :foreground})
                       (if (pos? (state/formatting-meta-level))
                         (tagged separator
                                 {:theme-token (state/metadata-token)})
                         separator))))]
              ret))
          coll))
        
        ret                 
        (stringified-bracketed-coll-with-num-dropped-syntax!
         ;; TODO - use (merge meta-map profile+ob (keyed [...]))
         (keyed [some-syms-carrying-metadata-as-keys? ;; <-remove?
                 single-column-map-layout?
                 truncated-coll-size                  ;; <-remove?
                 some-colls-as-keys?                  ;; <-remove?
                 let-bindings?                        ;; <-remove?
                 num-dropped                          ;; <-remove?
                 multi-line?
                 too-deep?                            ;; <-remove?
                 indent
                 coll
                 ret
                 ob]))]
    ret))

(defn- gap-spaces
  [{:keys [s k formatting-meta? theme-token-map]}]
  #?(:cljs s ;; TODO - do we need to handle node here?
     :clj  (if formatting-meta?
             (do (when (state/debug-tagging?)
                   (println (str "\ntagging "
                                 (name k)
                                 " in metadata map")))
                 (tagged s
                         (assoc theme-token-map
                                (keyword (str (name k) "?"))
                                true)))
             s)))

(defn- reduce-map*
  [{:keys [indent
           separator
           max-keylen
           coll-count
           untokenized
           multi-line?]}
  idx
  [_ v]]
  (let [[key-props val-props]
        (nth untokenized idx)

        indent               
        (+ (or max-keylen 0)
           (or defs/kv-gap 0)
           (or indent 0))

        {escaped-key    :escaped
         key-char-count :ellipsized-char-count}
        (sev! (merge key-props {:indent indent}))

        theme-token-map
        {:theme-token (state/metadata-token)}
        
        formatting-meta?
        (pos? (state/formatting-meta-level))

        spaces-after-key
        (let [num-extra (if multi-line?
                          (- (or max-keylen 0) (or key-char-count 0))
                          0)
              s         (when (some-> num-extra pos?) 
                          (spaces num-extra))
              k         :spaces-after-key]
          (gap-spaces (keyed [s k formatting-meta? theme-token-map])))

        kv-gap-spaces
        (let [s (spaces defs/kv-gap)
              k :kv-gap-spaces]
          (gap-spaces (keyed [s k formatting-meta? theme-token-map])))

        tagged-val          
        (tagged-val (keyed [v
                            indent
                            val-props
                            separator
                            multi-line?
                            max-keylen]))

        sep 
        (when-not (= coll-count (inc idx))
          (if formatting-meta?
            (tagged separator
                    (when-not multi-line?
                      theme-token-map))
            separator))

        ret                  
        (str escaped-key
             spaces-after-key
             kv-gap-spaces
             tagged-val
             sep)]
    ret))


(defn- reduce-map
  "This reduces and serializes maps. Multiline maps (maps with data
   structures as keys), do not get reduced by reduce-map. Instead they
   are reduced by reduce-coll."
  [coll indent]
  (let [m     
        (profile+ob coll indent)

        untokenized
        ;; TODO - perf use mapv
        (map (fn [[k v]]
               (let [key-props (meta k)
                     val-props (meta v)]
                 [key-props val-props]))
             coll)

        max-keylen
        (or (some->> untokenized
                     ;; TODO - perf use mapv
                     (map #(-> % first :ellipsized-char-count))
                     seq
                     (apply max))
            0)

        ret        
        (string/join
         (map-indexed
          (partial reduce-map*
                   (merge m (keyed [untokenized max-keylen])))
          coll))

        ret        
        (stringified-bracketed-coll-with-num-dropped-syntax!
         (merge m
                (keyed [coll
                        ret
                        max-keylen])))]
     ret))


(defn- tagged-val
  [{:keys [v
           val-props
           t
           indent
           multi-line?
           separator
           max-keylen]
    :as m}]
  (let [t                          
        (or t (:t val-props))

        {:keys [map-like?
                single-column-map-layout?
                coll-type?]}                         
         val-props
        
        ;; TODO - why is this named source-is-scalar?
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
      (:escaped (sev! (merge val-props
                             (keyed [indent
                                     multi-line?
                                     separator
                                     max-keylen])))))))


(defn serialized
  [v indent]
  (let [ret (str (when-not (pos? (state/formatting-meta-level))
                   (some-> indent util/spaces))
                 (tagged-val {:v         v
                              :indent    indent 
                              :val-props (meta v)}))]
    ret))

(defn formatted*
  ([source]
   (formatted* source nil))
  ;; user-meta? when the value being formatted is a user meta map
  ;; TODO - Maybe change :user-meta? to :value-is-user-meta-map?
  ([source {:keys [indent user-meta?]
            :or   {indent (or @state/margin-inline-start 0)}
            :as   opts}]

   ;; Just for debugging
   ;;  #?(:cljs (js/console.clear))
   
   (let [truncated  (truncate/truncate {:depth      0
                                        :user-meta? user-meta?}
                                       source)
        ;;  custom-printed truncated
         ;; TODO - revisit this custom printing stuff
         ;; custom-printed (if (:evaled-form? opts)
         ;;                  truncated
         ;;                  (walk/postwalk printers/custom truncated))
         profiled   (walk/prewalk profile/profile truncated)
         serialized (serialized profiled indent)
        ;;  len            (-> profiled meta :str-len-with-badge)
         ]

    ;; Just for debugging
    ;;  (when (:coll-type? (meta profiled))
    ;;      (?pp (meta profiled)))
    ;; (?pp (meta profiled))
    ;;  (?pp (map 
    ;;        (fn [a b]
    ;;          [a b])
    ;;        (rest (string/split serialized "%c"))
    ;;        @state/styles))
    ;;  (?pp :serialized serialized)
    ;;  (?pp @state/styles)
     
     serialized)))
