(ns ^:dev/always fireworks.sev
  (:require
   [fireworks.defs :as defs]
   [fireworks.state :as state]
   [fireworks.tag :as tag :refer [tagged tag! tag-reset!]]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])))

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
  [{:keys [num-chars-dropped t truncate-fn-name?]}]
  (when (or truncate-fn-name?
            (some-> num-chars-dropped pos?))
    (if (collection-type? t)
      (str (tag! :ellipsis) "+" num-chars-dropped (tag-reset!))
      (str (tag! :ellipsis) defs/ellipsis (tag-reset!)))))


(defn sev!
  "Creates a string with the properly placed \"%c\" formatting tags.
   The tag! and tag-reset! fns mutate the styles atom for syntax coloring."
  [{:keys [x
           s
           str-len-with-badge
           fn-display-name
           t
           num-chars-dropped
           ellipsized-char-count
           highlighting
           fn-args
           badge
           number-type?
           atom?
           js-map-like-key?
           all-tags
           :fw/user-meta
           indent
           :fw/custom-badge-style
           sev?]
    :as m}]
  (let [encapsulated?
        (or (= t :uuid) (contains? all-tags :inst))

        metadata-position
        (when user-meta (:metadata-position @state/config))

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
        (when (and user-meta
                   (contains? #{:block "block"} metadata-position))
          (tag/stringified-user-meta
           (keyed [user-meta
                   indent
                   str-len-with-badge
                   metadata-position
                   sev?])))

        atom-tagged
        (tagged (str defs/atom-label
                     defs/encapsulation-opening-bracket) 
                {:theme-token  :atom-wrapper
                 :display?     atom?
                 :highlighting highlighting})

        badge-tagged
        (tagged badge
                {:custom-badge-style custom-badge-style 
                 :theme-token        (if (= badge defs/lamda-symbol) 
                                       :lamda-label 
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

        main-entity-tag                  
        (tag! theme-tag highlighting)

        ;; Additional tagging (and atom mutation) happens within
        ;; fireworks.serialize/add-truncation-annotation!
        chars-dropped-syntax 
        (add-truncation-annotation! m)

        main-entity-tag-reset            
        (tag-reset!)

        fn-args-tagged
        (tagged fn-args {:theme-token :function-args})

        atom-closing-bracket-tagged
        (tagged defs/encapsulation-closing-bracket
                {:theme-token  :atom-wrapper 
                 :display?     atom?
                 :highlighting highlighting})

        user-meta-inline-tagged
        (when (and (:display-metadata? @state/config)
                   user-meta
                   (contains? #{:inline "inline"} metadata-position))
          (tag/stringified-user-meta
           (keyed [user-meta indent str-len-with-badge metadata-position])))

        ;; Atom mutation end  ------------------------------------------------
        
        
        

        ;; Putting all the colorization tagged bits together
        escaped              
        (str 
         ;; Optional, conditional metadata of coll element
         ;; positioned block-level, above element
         ;; :metadata-postition must be set to :block (in user config)
         user-meta-block-tagged
         
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
         user-meta-inline-tagged
         )

        ret                  
        (keyed [x
                s
                fn-display-name
                t
                num-chars-dropped 
                ellipsized-char-count
                escaped])
        locals (merge ret
                      (keyed [main-entity-tag
                              chars-dropped-syntax
                              main-entity-tag-reset]))]

    ;; (when-not @state/formatting-form-to-be-evaled?
    ;;   (when (= s "bar")
    ;;     #> m
    ;;     #> locals))
    ;; (!? locals)

    ret))
