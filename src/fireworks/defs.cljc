(ns ^:dev/always fireworks.defs)

(def base-classes
  {:foreground    nil ;; -> foreground
   :background nil 
   :bracket    :foreground
   :string     :foreground
   :comment    :foreground
   :constant   :foreground ;; numbers, symbols, keywords, boolean values
   :definition :foreground ;; global definitions
   :annotation :foreground ;; ns/file info, user-label, num-dropped 
   :metadata   :foreground
   :highlight  :foreground ;; default bg-color, inherited by [:printer :find]
   :label      :foreground ;; literal labels, type labels, atom wrappers, etc.

  ;; Experimental, not working yet
  ;;  :modifier   :foreground ;; earmuffs, deref, unused-arg, whitespace
   })

(def all-base-classes (into #{} (keys base-classes)))


(def base-syntax-tokens
  (merge {:string        :string
          :number        :constant
          :keyword       :constant
          :boolean       :constant
          :nil           :constant
          :symbol        :definition
          :regex         :string
          :def           :definition
          :local         :definition
          :local-binding :definition
          :function      :definition
          :class         :definition
          :datatype      :definition
          :record        :definition
          :multimethod   :definition
          :uuid          :string
          :inst          :string
          :js-object-key :foreground}
         
          ;;  Experimental, not working yet 
          {
            ;;  :square-bracket  :bracket
            ;;  :curly-bracket   :bracket
            ;;  :round-bracket   :bracket
            ;;  :map-bracket     :bracket
            ;;  :set-bracket     :bracket
            ;;  :list-bracket    :bracket
            ;;  :js-badge      :annotation
            ;;  :record-badge  :annotation
            ;;  :atom-badge    :annotation
          
            ;;  ;; advanced
            ;;  :earmuff       :modifier
            ;;  :deref         :modifier
            ;;  :unused-arg    :modifier
            ;;  :whitespace    :modifier
          }))

(def all-base-syntax-tokens (into #{} (keys base-syntax-tokens)))

(def base-printer-tokens
  {:foreground               :foreground
   :function-args         :bracket
   :literal-label         :label
   :type-label            :label
   :lamda-label           :label
   :atom-wrapper          :label
   :metadata              :metadata
   :annotation            :annotation
   :ellipsis              :annotation
   :coll-trunction        :annotation
   :user-annotation       :annotation
   :find-in-output        :highlight
   :file-info             :annotation
   :line-number           :annotation
   :column-number         :annotation
   :file-info-separator   :annotation
   :eval-fat-arrow        :foreground
   :seq-bracket           :bracket
   :lazy-seq-bracket      :bracket
   :max-print-level-label :annotation})

(def all-base-printer-tokens (into #{} (keys base-printer-tokens)))

(def num-indent-spaces
  {:map       1
   :js/Object 1
   :js/Array  1
   :record    1
   :set       2
   :js/Set    2
   :vector    1
   :list      1
   :seq       1})

(def valid-stylemap-keys
  #?(:clj [:color
           :background-color
           :font-style]
     :cljs [:color
            :background-color
            :width
            :text-shadow
            :font-style 
            :font-size
            :font-weight
            :border-radius
            :opacity
            :line-height]))

(def quoting-chars
  {:string \"
   :regex \"
   :uuid \"
   :object-key \'})

(def values-wrapped-in-quotes
  #{:string :regex :js/Date :java.util.Date :object-key :uuid})


(def kv-gap 1)

 ;; number of spaces between value and metadata
(def metadata-position-inline-offset 5)

(def mysterious-fn-args '[])

(def truncated-fn-args '[...])

(def ellipsis "...")

(def max-print-level-label "#")

(def ellipsis-count (count ellipsis))

(def atom-label "Atom")

(def volatile-label "Volatile")

(def encapsulation-opening-bracket "<")

(def encapsulation-closing-bracket ">")

(def atom-wrap-count
  (count (str atom-label
              encapsulation-opening-bracket
              encapsulation-closing-bracket)))

(def num-dropped-prefix (str ellipsis "+"))

(def lamda-symbol "λ")

(def js-literal-badge "#js")

(def js-built-in-method-nm-limit 5)

(def inst-badge "#inst")

(def uuid-badge "#uuid")

(def fat-arrow "=>")

(def inline-badges
  #{js-literal-badge inst-badge uuid-badge lamda-symbol})
