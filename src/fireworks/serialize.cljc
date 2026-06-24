(ns ^:dev/always fireworks.serialize 
  (:require
   [clojure.walk :as walk]
   [fireworks.profile :as profile]
   [fireworks.truncate :as truncate]
   [fireworks.brackets
    :as brackets
    :refer [closing-bracket! 
            opening-bracket!
            brackets-by-type]]
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.tag :as tag :refer [sgr-tag reset-tag sgr-reset-tag tagged]]
   [fireworks.util :as util :refer [spaces badge-type]]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])
   [fireworks.state :as state :refer [meta-level-inc! meta-level-dec!]]
   [fireworks.ansi :as ansi]))

(declare tagged-val)

(declare tagged-key)

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


(defn- truncation-annotation 
  [{:keys [num-chars-dropped t truncate-fn-name? top-level-sev? highlighting]
    :as m}]
  (when (or truncate-fn-name?
            (some-> num-chars-dropped pos?))
    (let [theme-tag (if (pos? (state/formatting-meta-level))
                      (state/metadata-token)
                      :ellipsis)
          ;; TODO/perf - replace collection-type? with (some-> m :all-tags :coll-type)
          ;; Or Determine if colls ever reach this fn and just eliminate 
          s         (if (collection-type? t)
                      (str "+" num-chars-dropped)
                      defs/ellipsis)]

      (when (state/debug-tagging?)
        (println "\nserialize/truncation-annotation-annotation:  tagging \""
                 s 
                 "\" with " 
                 theme-tag ))

      (if top-level-sev?
        s
        (str (sgr-tag theme-tag highlighting) s sgr-reset-tag)))))


(defn- sev-user-meta-position-match? [user-meta position]
  (and (:display-metadata? @state/config)
       (seq user-meta)
       (contains? (if (= position :block)
                    #{:block "block"}
                    #{:inline "inline"})
                  (:metadata-position @state/config))))


;; SEV                                                                    
;;                                                                     
;;    SSSSSSSSSSSSSSS EEEEEEEEEEEEEEEEEEEEEEVVVVVVVV           VVVVVVVV
;;  SS:::::::::::::::SE::::::::::::::::::::EV::::::V           V::::::V
;; S:::::SSSSSS::::::SE::::::::::::::::::::EV::::::V           V::::::V
;; S:::::S     SSSSSSSEE::::::EEEEEEEEE::::EV::::::V           V::::::V
;; S:::::S              E:::::E       EEEEEE V:::::V           V:::::V 
;; S:::::S              E:::::E               V:::::V         V:::::V  
;;  S::::SSSS           E::::::EEEEEEEEEE      V:::::V       V:::::V   
;;   SS::::::SSSSS      E:::::::::::::::E       V:::::V     V:::::V    
;;     SSS::::::::SS    E:::::::::::::::E        V:::::V   V:::::V     
;;        SSSSSS::::S   E::::::EEEEEEEEEE         V:::::V V:::::V      
;;             S:::::S  E:::::E                    V:::::V:::::V       
;;             S:::::S  E:::::E       EEEEEE        V:::::::::V        
;; SSSSSSS     S:::::SEE::::::EEEEEEEE:::::E         V:::::::V         
;; S::::::SSSSSS:::::SE::::::::::::::::::::E          V:::::V          
;; S:::::::::::::::SS E::::::::::::::::::::E           V:::V           
;;  SSSSSSSSSSSSSSS   EEEEEEEEEEEEEEEEEEEEEE            VVV            
;;                                                                     
;;                                                                     
;;                                                                     

(defn- sev
  "Creates a string with ansi-sgr formatting tags."
  ;; "The main entity that gets tagged is either `s` or `fn-display-name`"
  [{:keys [x
           s
           t
           key?
           sev?
           badge
           indent
           all-tags
           separator
           max-keylen
           multi-line?
           number-type?
           highlighting
           :fw/user-meta
           fn-display-name
           js-map-like-key?
           num-chars-dropped
           str-len-with-badge
           theme-token-override
           ellipsized-char-count]
    :as m}]

  ;; break these into helper fns?
  ;; "The intial set of bindings:                                            
  ;;     -  user-metadata-map-block (displays user-meta above value), optional
  ;;     -  annotation (badge), optional
  ;;     -  ansi sgr tag
  ;;     -  num-chars-dropped-syntax
  ;;     -  ansi sgr tag-reset 
  ;;     -  user-metadata-map-inline (displays user-meta inline, after value), optional"
  
  (let [user-metadata-map-block-tagged                                          
        (when (sev-user-meta-position-match? user-meta :block)
          
          (meta-level-inc!)
          (let [ret (formatted* user-meta {:indent     indent
                                           :user-meta? true})]
            (meta-level-dec!)
            ret))
        
        user-metadata-map-block-tagged-separator
        (when (and user-metadata-map-block-tagged
                   multi-line?)
          (tagged (str separator
                       (some-> max-keylen
                               (+ defs/kv-gap)
                               spaces))))

        badge-tagged
        (tagged badge
                {:theme-token (cond
                                (pos? (state/formatting-meta-level))
                                (state/metadata-token)
                                (= badge defs/lambda-badge) 
                                :lambda-label 
                                :else
                                :literal-label)})

        theme-tag
        (cond number-type?
              :number
              js-map-like-key?
              :js-object-key
              :else
              t)

        theme-tag
        (let [meta-level (state/formatting-meta-level)]
          (if (pos? meta-level)
            (let [l2? (= 2 meta-level)]
              (if key?
                (if l2? :metadata-key2 :metadata-key)
                (if l2? :metadata2 :metadata)))
            (or theme-token-override theme-tag)))

        _ 
        (when (state/debug-tagging?)
          (println "\nsev   tagging " s " with " theme-tag))

        ;; This is where value's tag gets created
        main-entity-tag                  
        (sgr-tag theme-tag highlighting)

        ;; Additional tagging happens within fireworks.serialize/truncation-annotation
        chars-dropped-syntax 
        (truncation-annotation m #_(assoc m :highlighting highlighting))

        main-entity-tag-reset            
        (reset-tag (if (pos? (state/formatting-meta-level))
                     (state/metadata-token)
                     :foreground))

        user-metadata-map-inline-tagged
        (when (sev-user-meta-position-match? user-meta :inline)
          (meta-level-inc!)
          (let [offset        defs/metadata-position-inline-offset
                inline-offset (tagged (spaces (dec offset))
                                      {:theme-token (state/metadata-token)})
                ret           (formatted*
                               (dissoc user-meta :__loc :__meta-loc)
                               {:indent     (+ indent
                                               offset
                                               (or ellipsized-char-count 0))
                                :user-meta? true})]
            
            (meta-level-dec!)
            ;; TODO - figure out how to create left arrow char with foreground
            ;; color of metadata background.
            (str " " inline-offset ret)))

        ;; Additional tagging end  ---------------------------------------------
        
        string-delimiter-sgr-tag
        (when (= t :string)
          (sgr-tag (if (pos? (state/formatting-meta-level))
                     (state/metadata-token)
                     :string-delimiter)
                   highlighting))

        ;; Putting all the colorization tagged bits together
        escaped              
        (str 
         ;; Optional, conditional metadata of coll element
         ;; positioned block-level, above element
         ;; :metadata-postition must be set to :block (in user config)
         user-metadata-map-block-tagged

         ;; Only when user-metadata-map-block-tagged is present
         user-metadata-map-block-tagged-separator
         
         ;; Conditional `Atom`, positioned inline, to left of value 
         ;;  atom-tagged
         
         ;; Conditional `badge` (for `#uuid`, or `#inst`),
         ;; positioned inline, to left of value 
         badge-tagged

         ;; If datatype which shows a memory address, we do badge above
         (when (= t :datatype)
           "\n")


         ;; The self-evaluating value
         (when (= t :string)
           (str string-delimiter-sgr-tag
                "\"" 
                sgr-reset-tag))

         main-entity-tag
         ;; (some-> num-chars-dropped pos?)
         (or (when s
               (cond (= t :string)
                     (string/replace (subs s 1 (-> s count dec))
                                     #"\""
                                     ;; Maybe we should add this at an earlier stage
                                     (str (sgr-tag :escape-char)
                                          "\\\\"
                                          main-entity-tag-reset
                                          (sgr-tag :escaped-double-quote-char)
                                          "\""
                                          main-entity-tag-reset
                                          main-entity-tag))

                     (= t :regex)
                     (tag/colorized-regex s)

                     (and (= t :number) (re-find #"^[0-9]+\.[0-9]+" s))
                     (let [[integral-part decimal-part] (string/split s #"\.")]
                       (str (sgr-tag :number highlighting)
                            integral-part "."
                            main-entity-tag-reset
                            (sgr-tag :decimal highlighting)
                            decimal-part
                            main-entity-tag-reset))

                     :else
                     s))
             fn-display-name)
         main-entity-tag-reset
         chars-dropped-syntax

         (when (= t :string) 
           (str string-delimiter-sgr-tag "\"" sgr-reset-tag))
         
         ;; Conditional `Atom`, closing parts,
         ;; positioned inline, to right of value 
         #_atom-closing-bracket-tagged

         ;; Optional, conditional metadata of coll element
         ;; positioned inline, to right of element
         ;; Default position.
         ;; Will not display if :metadata-postition is set
         ;; explicitly to :block (in user config).
         user-metadata-map-inline-tagged)

        ret                  
        (keyed [ellipsized-char-count
                num-chars-dropped
                fn-display-name
                escaped
                x 
                s
                t])]

    #_(? 'locals
           (merge ret
                  (keyed [main-entity-tag
                          chars-dropped-syntax
                          main-entity-tag-reset])))
    ret))




;;                                                                               
;;                                                                               
;; DDDDDDDDDDDDD      RRRRRRRRRRRRRRRRR        OOOOOOOOO     PPPPPPPPPPPPPPPPP   
;; D::::::::::::DDD   R::::::::::::::::R     OO:::::::::OO   P::::::::::::::::P  
;; D:::::::::::::::DD R::::::RRRRRR:::::R  OO:::::::::::::OO P::::::PPPPPP:::::P 
;; DDD:::::DDDDD:::::DRR:::::R     R:::::RO:::::::OOO:::::::OPP:::::P     P:::::P
;;   D:::::D    D:::::D R::::R     R:::::RO::::::O   O::::::O  P::::P     P:::::P
;;   D:::::D     D:::::DR::::R     R:::::RO:::::O     O:::::O  P::::P     P:::::P
;;   D:::::D     D:::::DR::::RRRRRR:::::R O:::::O     O:::::O  P::::PPPPPP:::::P 
;;   D:::::D     D:::::DR:::::::::::::RR  O:::::O     O:::::O  P:::::::::::::PP  
;;   D:::::D     D:::::DR::::RRRRRR:::::R O:::::O     O:::::O  P::::PPPPPPPPP    
;;   D:::::D     D:::::DR::::R     R:::::RO:::::O     O:::::O  P::::P            
;;   D:::::D     D:::::DR::::R     R:::::RO:::::O     O:::::O  P::::P            
;;   D:::::D    D:::::D R::::R     R:::::RO::::::O   O::::::O  P::::P            
;; DDD:::::DDDDD:::::DRR:::::R     R:::::RO:::::::OOO:::::::OPP::::::PP          
;; D:::::::::::::::DD R::::::R     R:::::R OO:::::::::::::OO P::::::::P          
;; D::::::::::::DDD   R::::::R     R:::::R   OO:::::::::OO   P::::::::P          
;; DDDDDDDDDDDDD      RRRRRRRR     RRRRRRR     OOOOOOOOO     PPPPPPPPPP          
;;                                                                               
;;                                                                               

;;;; Num-dropped-syntax start --------------------------------------------------

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


(defn- spaces-between-kv [max-keylen too-deep? map-key-ghost]
  (spaces (if too-deep?
            0
            (let [max-keylen+space (inc max-keylen)]
              (- max-keylen+space
                 (count map-key-ghost))))))


(defn- kv-spacing-str [m max-keylen too-deep?]
  (let [map-key-ghost     (map-key-ghost m)
        spaces-between-kv (spaces-between-kv max-keylen
                                             too-deep?
                                             map-key-ghost)]
    (str map-key-ghost spaces-between-kv)))


(defn- num-dropped-annotation
  [{:keys [indent
           too-deep?
           max-keylen
           multi-line?
           num-dropped
           highlighting]
    :as m}]
  (let [num-dropped-syntax (str (when-not too-deep? defs/ellipsis)
                                "+"
                                num-dropped)
        coll-is-map?       (boolean max-keylen)
        num-indent-chars   indent      
        indent-chars       (spaces num-indent-chars)      
        leading+indent     (str (leading-space m)
                                (when multi-line? indent-chars))
        kv-spacing-str     (when coll-is-map?
                             (symbol ""))
        extra              (str leading+indent
                                (sgr-tag {} highlighting)
                                kv-spacing-str
                                num-dropped-syntax
                                sgr-reset-tag)]

    (when (state/debug-tagging?)
      (println "\nnum-dropped-annotion! : tagging " extra " with " :ellipsis))

    (if highlighting 
      extra
      (str (sgr-tag :ellipsis) extra sgr-reset-tag))))

;;;; Num-dropped-syntax end ----------------------------------------------------

;;                                                                                         
;;                                                                                         
;;         CCCCCCCCCCCCC     OOOOOOOOO     LLLLLLLLLLL             LLLLLLLLLLL             
;;      CCC::::::::::::C   OO:::::::::OO   L:::::::::L             L:::::::::L             
;;    CC:::::::::::::::C OO:::::::::::::OO L:::::::::L             L:::::::::L             
;;   C:::::CCCCCCCC::::CO:::::::OOO:::::::OLL:::::::LL             LL:::::::LL             
;;  C:::::C       CCCCCCO::::::O   O::::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;; C:::::C              O:::::O     O:::::O  L:::::L                 L:::::L               
;;  C:::::C       CCCCCCO::::::O   O::::::O  L:::::L         LLLLLL  L:::::L         LLLLLL
;;   C:::::CCCCCCCC::::CO:::::::OOO:::::::OLL:::::::LLLLLLLLL:::::LLL:::::::LLLLLLLLL:::::L
;;    CC:::::::::::::::C OO:::::::::::::OO L::::::::::::::::::::::LL::::::::::::::::::::::L
;;      CCC::::::::::::C   OO:::::::::OO   L::::::::::::::::::::::LL::::::::::::::::::::::L
;;         CCCCCCCCCCCCC     OOOOOOOOO     LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
;;                                                                                         
;                                                                                         


(defn- stringified-bracketed-coll-with-num-dropped-syntax! 
  "This is where the stringified coll (with formatting and escaped style tags)
   gets wrapped in the appropriate brackets, depending on the collection type.
   The `extra` value is the annotation-styled syntax, which conveys
   how many entries were dropped. This will be something like \"...+5\".
   This value is displayed, in the last position of the collection."

  [{:keys [ob ret num-dropped] :as m}]
  (let [extra (when (some-> num-dropped pos?)
                (num-dropped-annotation m)) ;<- mutates?
        cb    (closing-bracket! m) ;<- mutates state for rainbow brackets
        ]
    (str ob
         ret
         extra
         cb
         #_cb2)))


(defn- el-with-block-level-badge [x]
  (let [{:keys [badge inline-badge? x]} (meta x)]
    (when (and badge (not inline-badge?)) true)))


(defn- some-elements-have-block-level-badges?
  [x]
  (->> x
       (keep el-with-block-level-badge)
       seq
       boolean))

(defn- double-truncated-map? [coll t]
  (and (= t :map)
       (< 1 (count coll))
       (= (first coll) ['... (symbol " ")])))

(defn- reduce-coll-profile
  [coll indent*]
  (let [{:keys [some-elements-carry-user-metadata?  
                multi-line-string-collection?
                single-column-map-layout?
                ;; str-len-with-badge-ellipsized
                ;; str-len-val-ellipsized
                ;; str-len-with-badge
                str-len-with-badge
                :fw/user-meta-map? ; <- maybe this is redundant? also exists as unq kw user-meta in this map. Maybe change unq key to user-metadata-map?, or val-is-user-metadata-map?
                :fw/user-meta
                badge-str-len ; <- maybe this is redundant? also exists as unq kw in this map. Maybe change unq key to user-metadata-map
                js-map-like?
                val-is-derefable?         
                val-is-volatile?         
                val-is-agent?
                val-is-atom?
                val-is-ref?
                highlighting
                num-dropped
                val-str-len
                user-meta?
                too-deep?
                set-like?
                js-set?
                record?
                array?         
                badge
                depth
                og-t
                t]
         :as   meta-map}
        (meta coll)

        user-meta    
        (when (seq user-meta) user-meta)

        coll-count
        (count coll) ; <- TODO - don't we already know this from lasertag/tag-map? 
        
        metadata-position
        (:metadata-position @state/config)

        display-metadata?
        (:display-metadata? @state/config)

        badge-above?
        (some->> badge (contains? defs/inline-badges) not)

        user-meta-above?
        (boolean (and user-meta (contains? #{:block "block"} metadata-position)))
        
        ;; _ (? meta-map)
        
        ;; This is where multi-line for collections is determined
        ;; _ (? (select-keys meta-map [:str-len-with-badge-ellipsized
        ;;                               :str-len-val-ellipsized
        ;;                               :str-len-with-badge
        ;;                               :val-str-len
        ;;                               :badge-str-len]))
        multi-line?  
        (when-not (and user-meta?
                       (false? (:multi-line-metadata? @state/config)))
          (or multi-line-string-collection?
              (and display-metadata?
                   user-meta?
                   (contains? #{:inline "inline"} metadata-position))
              (and display-metadata?
                   some-elements-carry-user-metadata?)
              (some-elements-have-block-level-badges? coll)
              (boolean 
               (when (< 1 coll-count)
                 ;; This is where multi-line for maps is determined
                 (or single-column-map-layout?
                     (let [strlen-greater-than-limit? 
                           (< (:single-line-coll-max-length @state/config)
                              (or (if badge-above?
                                    val-str-len        ;; <- should this be str-len-val-ellipsized?
                                    str-len-with-badge ;; <- should this be str-len-with-val-ellipsized?
                                    )
                                  0))]
                       (or strlen-greater-than-limit?
                           (when display-metadata?
                             (some #(when (meta %) %)
                                   (tree-seq coll? seq (:og-x meta-map)))))))))
              (double-truncated-map? coll t)))


        ;; _ (when-not multi-line? (? meta-map))
        
        ;; This is where indenting for multi-line collections is determined
        num-indent-spaces-for-t
        ;; TODO - shouldn't need to check js/Set here, set-like? should cover it
        (if (or set-like?
                user-meta-map? 
                (= t :js/Set) 
                (and (:quote-lists? @state/config)
                     (-> coll meta :root-list?)))
          2 1)

        indent       
        (+ (or indent* 0) num-indent-spaces-for-t)

        indent       
        (or (when-not (or badge-above? user-meta-above?) 
              (some->> badge-str-len
                       ;; dec
                       (+ (or indent 0))))
            indent)

        indent (if (and (or val-is-atom? 
                            val-is-volatile? 
                            val-is-agent? 
                            val-is-ref?
                            val-is-derefable?)
                        (not badge-above?))
                 (+ (or (count (str (cond val-is-atom?
                                          defs/atom-label 
                                          val-is-volatile?
                                          defs/volatile-label
                                          val-is-ref?
                                          defs/ref-label
                                          val-is-agent?
                                          defs/agent-label
                                          :else
                                          (-> t name string/capitalize))))
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
        (when (or js-map-like? js-set? array?) ",")

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
        (keyed [multi-line-string-collection?
                str-len-with-badge     
                annotation-newline 
                metadata-position
                user-meta-above?
                badge-str-len
                let-bindings?
                val-is-derefable?
                val-is-volatile?
                val-is-atom?   
                val-is-agent?   
                val-is-ref?   
                highlighting
                num-dropped
                multi-line?
                coll-count
                separator
                user-meta
                too-deep?
                record?
                indent
                badge
                og-t])]
    ret))


(defn- formatted-user-meta [user-meta indent*]
  (formatted* (let [dissoced 
                    (apply dissoc
                           user-meta
                           (:dissoc-metadata-keys @state/config))
                    ks       (:select-metadata-keys @state/config)
                    selected (if (seq ks)
                               (select-keys dissoced ks)
                               dissoced)]
                selected)
              {:user-meta? true
               :indent     indent*}))

(defn- user-metadata-map-block
  [indent*
   metadata-position
   {:keys [badge user-meta] :as m}]
  (when (and (:display-metadata? @state/config)
             user-meta
             (contains? #{:block "block"} metadata-position))
    
    (meta-level-inc!)
    (let [ret (formatted-user-meta user-meta indent*)]
      (meta-level-dec!)
      ret)))


(defn- user-metadata-map-inline-for-coll
  [{:keys [user-meta
           metadata-position
           coll
           indent*]}]
  (when (and (:display-metadata? @state/config)
             (seq user-meta)
             (contains? #{:inline "inline"} metadata-position))
    
    (meta-level-inc!)
    (let [offset        defs/metadata-position-inline-offset
          inline-offset (tagged (spaces (dec offset))
                                {:theme-token (state/metadata-token)})
          ob            (some-> coll
                                meta
                                brackets-by-type
                                first)
          ob            (brackets/maybe-quoted-opening-paren coll ob)
          indent+ob     (+ (or (count ob) 0)
                           indent*)
          ret           (formatted-user-meta user-meta (+ indent+ob offset))]
      (meta-level-dec!)
      (str " " inline-offset ret))))


;; TODO - break ob into its own fn
(defn- profile+ob
  [coll indent*]
  (let [{:keys [og-t
                badge
                record?
                user-meta
                separator
                val-is-derefable?
                highlighting
                let-bindings?
                badge-str-len
                annotation-newline
                multi-line-string-collection?]
         :as   m} 
        (reduce-coll-profile coll indent*)
        
        ;; Block-level badges for colls / custom data types are tagged here
        badge               
        (when badge
          (let [theme-token (badge-type badge)
                pred        true]
            (tagged badge (keyed [theme-token pred]))))

        metadata-position
        (:metadata-position @state/config)

        ;; Move up?
        user-metadata-map-block
        (user-metadata-map-block indent* metadata-position m)

        ob* 
        (str #_mutable-opening-encapsulation
             badge
             (some-> user-metadata-map-block (str (when badge " ")))
             annotation-newline
             (opening-bracket! (keyed [coll
                                       record?
                                       let-bindings?
                                       highlighting
                                       multi-line-string-collection?]))
             #_(if highlighting
                 ;; Change this - move logic down in to opening bracket
                 (str #_(sgr-tag {} highlighting)
                      (opening-bracket! (keyed [coll record? let-bindings?]))
                      #_sgr-reset-tag)
                 (opening-bracket! (keyed [coll record? let-bindings?])))
             (when (-> coll meta :too-deep?)
               (tagged "#" {:theme-token :max-print-level-label})))
        
        user-metadata-map-inline-for-coll
        (user-metadata-map-inline-for-coll
         (keyed [
                ;;  mutable-opening-encapsulation-str
                 metadata-position
                 user-meta
                 indent*
                 badge
                 badge-str-len
                 coll]))

        ob
        (str ob* (some-> user-metadata-map-inline-for-coll
                         (str (tagged separator
                                      {:theme-token :foreground}))))]

    ;; TODO - isn't record alreay in m?
    (assoc m :ob ob :record? record?)))


(defn- coll-sep 
  "Provides the correct separator for items in coll, based on whether they are
   an native collection type, whether they are multiline etc."
  [{:keys [t js-typed-array? single-column-map-layout? separator]}
   idx]
  (let [value-in-mapentry? (and single-column-map-layout? (odd? idx))
        maybe-comma        (when (or js-typed-array?
                                     (contains? #{:js-array :js-set} t))
                             ",")]
    ;; Check this (does id get separator twice)?
    (str (if value-in-mapentry? separator maybe-comma)
         separator)))


(defn- coll-sep-tagged
  [{:keys [highlighting multi-line?] :as m} idx]
  (let [formatting-meta? (pos? (state/formatting-meta-level)) 
        theme-token      (if formatting-meta? 
                           (state/metadata-token)
                           :foreground)
        opts             (merge
                          {:theme-token theme-token}
                          (when-not (or multi-line? formatting-meta?)
                            {:highlighting highlighting}))]
    (tagged (coll-sep m idx) opts)))


(defn- reduce-coll-inner 
  [{:keys [indent
           multi-line?
           separator
           coll-count]
    :as m}
   idx
   v]
  (let [val-props
        (meta v)

        tagged-val
        (tagged-val (keyed [v val-props indent multi-line? separator]))]

    (str tagged-val
         (when-not (= coll-count (inc idx)) 
           (coll-sep-tagged m idx)))))


(defn tag-nested-lists
  "Based on nesting, recursively augments metadata of vectors that represent
   list values with a `:root-list?` entry, and potentially a `:nested-list?`
   entry.
   
   This is only called when the `:quote-lists` option is true.
   
   This allows for lists to use the quoted syntax, without adding syntax quoting
   to nested lists.

   Example:
   ```clojure
   (? {:quote-lists? true} '(foo bar '(bang)))
   ;; prints
   '(foo bar (bang))
   ```

   One use case for the `:quote-lists` feature is printing generated code that
   is to be copied and pasted source code."
  ([coll]
   (tag-nested-lists coll false))
  ([coll inside-list?]
   (if (vector? coll)
     (let [m                 (meta coll)
           og-list?          (= (:t m) :list)
           
           ;; Resolve new metadata
           new-meta          (cond
                               (not og-list?) m
                               inside-list?   (assoc m :nested-list? true)
                               :else          (assoc m :root-list? true))
           
           ;; If already inside a list, or this vector is a list,
           ;; all nested children are considered "inside-list".
           next-inside-list? (or inside-list? og-list?)]
       
       ;; Reconstruct the vec by recursively mapping over els, and applying the
       ;; new metadata.
       (with-meta (mapv #(tag-nested-lists % next-inside-list?) coll)
         new-meta))
     
     ;; Return non-vectors
     coll)))


;; Should we use a StringBuilder construct here in jvm?
;; What about cljs - just push to an array?
(defn- reduce-coll
  [coll indent*]
  (let [coll
        ;; Could keep a current-value-has-lists? in state for perf
        ;; It would turned on during tagging in truncate
        (if (and (:quote-lists? @state/config)
                 (not (-> coll meta :nested-list?)))
          (tag-nested-lists coll)
          coll)
        
        {:keys [single-column-map-layout?]
         :as   coll-meta}
        (meta coll) 

        coll                
        (if single-column-map-layout?
          (with-meta (sequence cat coll) coll-meta)
          coll)

        coll-meta+
        (merge coll-meta (profile+ob coll indent*))                 

        ret
        (string/join
         (map-indexed (partial reduce-coll-inner coll-meta+)
                      coll))]
    (stringified-bracketed-coll-with-num-dropped-syntax!
     (assoc coll-meta+ :coll coll :ret ret))))


(defn- gap-spaces [{:keys [s k formatting-meta? theme-token-map highlighting]}]
  (if formatting-meta?
    (do (when (state/debug-tagging?)
          (println (str "\ntagging "
                        (name k)
                        " in metadata map")))
        (tagged s
                (assoc theme-token-map
                       (keyword (str (name k) "?"))
                       true)))
    (tagged s (keyed [highlighting]))))


;;                                                                                     
;;                                                                                     
;; MMMMMMMM               MMMMMMMM               AAA               PPPPPPPPPPPPPPPPP   
;; M:::::::M             M:::::::M              A:::A              P::::::::::::::::P  
;; M::::::::M           M::::::::M             A:::::A             P::::::PPPPPP:::::P 
;; M:::::::::M         M:::::::::M            A:::::::A            PP:::::P     P:::::P
;; M::::::::::M       M::::::::::M           A:::::::::A             P::::P     P:::::P
;; M:::::::::::M     M:::::::::::M          A:::::A:::::A            P::::P     P:::::P
;; M:::::::M::::M   M::::M:::::::M         A:::::A A:::::A           P::::PPPPPP:::::P 
;; M::::::M M::::M M::::M M::::::M        A:::::A   A:::::A          P:::::::::::::PP  
;; M::::::M  M::::M::::M  M::::::M       A:::::A     A:::::A         P::::PPPPPPPPP    
;; M::::::M   M:::::::M   M::::::M      A:::::AAAAAAAAA:::::A        P::::P            
;; M::::::M    M:::::M    M::::::M     A:::::::::::::::::::::A       P::::P            
;; M::::::M     MMMMM     M::::::M    A:::::AAAAAAAAAAAAA:::::A      P::::P            
;; M::::::M               M::::::M   A:::::A             A:::::A   PP::::::PP          
;; M::::::M               M::::::M  A:::::A               A:::::A  P::::::::P          
;; M::::::M               M::::::M A:::::A                 A:::::A P::::::::P          
;; MMMMMMMM               MMMMMMMMAAAAAAA                   AAAAAAAPPPPPPPPPP          
;;                                                                                     
;;                                                                                     

(defn- reduce-map*
  [{:keys [indent
           separator
           max-keylen
           coll-count
           untokenized
           multi-line?
           highlighting]}
  idx
  [k v]]
  (let [[key-props val-props]
        (nth untokenized idx)

        indent               
        (+ (or max-keylen 0)
           (or defs/kv-gap 0)
           (or indent 0))

        {escaped-key    :escaped
         key-char-count :ellipsized-char-count}
        ;; New hack for small colls-as-keys that are not single-line
        (if (coll? k)
          (when-let [tk (tagged-key 
                         (keyed [k
                                 indent
                                 key-props
                                 separator
                                 multi-line?
                                 max-keylen]))]
            {:escaped               tk
             :ellipsized-char-count (ansi/adjusted-char-count tk)})

          ;; assoc :indent to key-props, and conditionally assoc :highlighting
          ;; from parent map, but only if key does not have its own entry for
          ;; :highlighting.
          (sev (cond-> (assoc key-props :indent indent)
                 (and (nil? (:highlighting key-props))
                      highlighting)
                 (assoc :highlighting highlighting))))

        theme-token-map
        {:theme-token (state/metadata-token)}
        
        formatting-meta?
        (pos? (state/formatting-meta-level))

        gap-spaces-opts
        (keyed [formatting-meta? theme-token-map highlighting])

        leading-or-trailing-truncation-kv?
        (and (or (= k (symbol "... "))
                 (= k (symbol "...")))
             (= v (symbol "")))

        spaces-after-key
        (if leading-or-trailing-truncation-kv? 
          ""
          (let [num-extra (if multi-line?
                            (- (or max-keylen 0)
                               (or key-char-count 0))
                            0)
                s         (when (some-> num-extra pos?) (spaces num-extra))
                k         :spaces-after-key]
            (gap-spaces (assoc gap-spaces-opts :s s :k k))))

        kv-gap-spaces
        (if leading-or-trailing-truncation-kv?
          ""
          (let [s (spaces defs/kv-gap)
                k :kv-gap-spaces]
            (gap-spaces (assoc gap-spaces-opts :s s :k k))))

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
            (tagged separator (when-not multi-line? theme-token-map))
            (tagged separator (when-not multi-line? (keyed [highlighting])))))

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
        (mapv (fn [[k v]]
               (let [key-props (meta k)
                     val-props (meta v)]
                 [key-props val-props]))
             coll)

        max-keylen
        (or (some->> untokenized
                     (mapv #(or (some-> % first :ellipsized-char-count)
                                ;; For mapkeys that are small collections
                                (some-> % first :x str count)))
                     seq
                     ;; possibly remove nil?
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



;;                                                                
;;                                                                
;; KKKKKKKKK    KKKKKKKEEEEEEEEEEEEEEEEEEEEEEYYYYYYY       YYYYYYY
;; K:::::::K    K:::::KE::::::::::::::::::::EY:::::Y       Y:::::Y
;; K:::::::K    K:::::KE::::::::::::::::::::EY:::::Y       Y:::::Y
;; K:::::::K   K::::::KEE::::::EEEEEEEEE::::EY::::::Y     Y::::::Y
;; KK::::::K  K:::::KKK  E:::::E       EEEEEEYYY:::::Y   Y:::::YYY
;;   K:::::K K:::::K     E:::::E                Y:::::Y Y:::::Y   
;;   K::::::K:::::K      E::::::EEEEEEEEEE       Y:::::Y:::::Y    
;;   K:::::::::::K       E:::::::::::::::E        Y:::::::::Y     
;;   K:::::::::::K       E:::::::::::::::E         Y:::::::Y      
;;   K::::::K:::::K      E::::::EEEEEEEEEE          Y:::::Y       
;;   K:::::K K:::::K     E:::::E                    Y:::::Y       
;; KK::::::K  K:::::KKK  E:::::E       EEEEEE       Y:::::Y       
;; K:::::::K   K::::::KEE::::::EEEEEEEE:::::E       Y:::::Y       
;; K:::::::K    K:::::KE::::::::::::::::::::E    YYYY:::::YYYY    
;; K:::::::K    K:::::KE::::::::::::::::::::E    Y:::::::::::Y    
;; KKKKKKKKK    KKKKKKKEEEEEEEEEEEEEEEEEEEEEE    YYYYYYYYYYYYY    
;;                                                                
;;                                                                
;;                                                                
;;                                                                
;;                                                                
;;                                                                
;;                                                                

;; maybe a short coll as key
(defn- tagged-key
  [{:keys [t
           indent
           key-props
           separator
           max-keylen
           multi-line?
           highlighting]
    coll-as-key :k
    :as m}]
  (let [t                          
        (or t (:t key-props))

        {:keys [map-like? single-column-map-layout? coll-type?]}                         
        key-props
        
        source-is-nil?
        (nil? t)]
    
    (cond
      source-is-nil?
      coll-as-key
      
      map-like?
      (if single-column-map-layout?
        (reduce-coll coll-as-key indent)
        (reduce-map coll-as-key indent))

      coll-type?
      (let [r (reduce-coll coll-as-key indent)] 
        r)

      :else
      (:escaped (sev (merge key-props
                            (keyed [indent
                                    separator
                                    max-keylen
                                    multi-line?
                                    highlighting])))))))


;;                                                                         
;;                                                                         
;; VVVVVVVV           VVVVVVVV   AAA               LLLLLLLLLLL             
;; V::::::V           V::::::V  A:::A              L:::::::::L             
;; V::::::V           V::::::V A:::::A             L:::::::::L             
;; V::::::V           V::::::VA:::::::A            LL:::::::LL             
;;  V:::::V           V:::::VA:::::::::A             L:::::L               
;;   V:::::V         V:::::VA:::::A:::::A            L:::::L               
;;    V:::::V       V:::::VA:::::A A:::::A           L:::::L               
;;     V:::::V     V:::::VA:::::A   A:::::A          L:::::L               
;;      V:::::V   V:::::VA:::::A     A:::::A         L:::::L               
;;       V:::::V V:::::VA:::::AAAAAAAAA:::::A        L:::::L               
;;        V:::::V:::::VA:::::::::::::::::::::A       L:::::L               
;;         V:::::::::VA:::::AAAAAAAAAAAAA:::::A      L:::::L         LLLLLL
;;          V:::::::VA:::::A             A:::::A   LL:::::::LLLLLLLLL:::::L
;;           V:::::VA:::::A               A:::::A  L::::::::::::::::::::::L
;;            V:::VA:::::A                 A:::::A L::::::::::::::::::::::L
;;             VVVAAAAAAA                   AAAAAAALLLLLLLLLLLLLLLLLLLLLLLL
;;                                                                         
;;                                                                         
;;                                                                         
;;                                                                         
;;                                                                         
;;                                                                         
;;                                                                         

(defn- tagged-val
  [{:keys [v
           val-props
           t
           indent
           multi-line?
           separator
           max-keylen]
    :as   m}]
  (let [t                          
        (or t (:t val-props))

        {:keys [map-like? single-column-map-layout? coll-type?]}                         
        val-props
        
        source-is-nil?
        (nil? t)]
    
    (cond
      source-is-nil?
      v
      
      map-like?
      (if single-column-map-layout?
        (reduce-coll v indent)
        (reduce-map v indent))

      coll-type?
      (let [r (reduce-coll v indent)] 
        r)

      :else
      (:escaped (sev (merge val-props
                            (keyed [indent
                                    multi-line?
                                    separator
                                    max-keylen])))))))





(defn serialized
  [v indent]

  (str (when-not (pos? (state/formatting-meta-level))
         (some-> indent util/spaces))
       (tagged-val {:v         v
                    :indent    indent 
                    :val-props (meta v)})))


(defn augment-meta [form path new-form]
  (with-meta new-form
    (assoc-in (meta form) [:fw/truncated :path] path)))


(defn path-walker
  "Walks a potentially nested data structure that is the result of
   fireworks.truncate/truncate. Adds an entry to each value's metadata via
   assoc-in [:fw/truncated :path]. The value of this entry is a vector, which
   describes the value's location in the nested data structure. This path can be
   leveraged for highlighting problem values with path info provided by spec or
   malli."
  ([form]
   (path-walker form []))
  ([form path]
   (let [with-meta+ (partial augment-meta form path)]
     (cond 
       (map? form)
       (with-meta+ 
         (reduce-kv (fn [m k v]
                      (let [
                            ;; k (-> k meta :fw/truncated :og-x)
                            ;; _ (? (-> k meta :fw/truncated :og-x))
                            ;; k (-> k meta :fw/truncated :og-x)
                            ]
                       (assoc m
                            ;; version with map-entry style paths for spec :in
                            ;;  (path-walker k (conj path k 0))
                            ;;  (path-walker v (conj path k 1))
                              
                            ;; version for malli :in
                              (path-walker k (conj path k nil))
                              (path-walker v (conj path k))
                              )))
                    {}
                    form) )
       
       (coll? form)
       (with-meta+ 
         (let [mapped  
               (map-indexed
                (fn [i v]
                  (path-walker v (conj path i)))
                form)]
           (cond (list? form)
                 (apply list mapped)
                 (seq? form)
                 (doall mapped)
                 :else
                 (into (empty form) mapped))) )
       :else
       (with-meta+ form )))))



(defn formatted*
  ([source]
   (formatted* source nil))
  ;; user-meta? when the value being formatted is a user meta map
  ;; TODO - Maybe change :user-meta? to :value-is-user-meta-map?
  ;; TODO - Maybe this second argument should be the place where all state that
  ;;        is related to formatting lives and use volatiles instead of atoms?
  ;;        It would need to be passed down and thru
  ([source {:keys [indent user-meta?]
            :or   {indent (or @state/margin-inline-start 0)}
            :as   opts}]

   ;; Just for debugging
   ;;  #?(:cljs (js/console.clear))

   (let [truncated  (truncate/truncate {:path       []
                                        :depth      0
                                        :user-meta? user-meta?} source)
         ;;  truncated  (path-walker truncated)
         ;;  custom-printed truncated
         ;; TODO - revisit this custom printing stuff
         ;; custom-printed (if (:evaled-form? opts)
         ;;                  truncated
         ;;                  (walk/postwalk printers/custom truncated))
         profiled   (walk/prewalk profile/profile truncated)
         serialized (serialized profiled indent)
         ;;  len            (-> profiled meta :str-len-with-badge)
         ]

     ;; For adding line-based undline decor such as "^^^^"
     ;; 1) Look for specific style signature based on class like :error-highlight
     ;; 2) Find line index and char index of first match
     ;; 3) Calculate row end
     ;; 4) Insert decoration line with ^^^^^^^ based on theme
     ;; 5) Add theme token in themes
     ;; 
     
     


     ;; for debugging path info
    ;;  (walk/postwalk (fn [x]
    ;;                     (println)
    ;;                     (pprint (-> x meta :fw/truncated (select-keys [:og-x :path]))) x)
    ;;                   truncated)

     ;; Just for debugging
     ;;  (when (:coll-type? (meta profiled))
     ;;      (? (meta profiled)))
     ;;  (? (:s (meta profiled)))
     ;;  (? (map 
     ;;        (fn [a b]
     ;;          [a b])
     ;;        (rest (string/split serialized "%c"))
     ;;        @state/styles))
     ;;  (? :serialized serialized)
     ;;  (? @state/styles)
     
     serialized)))
