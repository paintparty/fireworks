(ns ^:dev/always fireworks.defs)

;; Tokens must be present in one of the maps below in order to pick up
;; styling specified in theme.
;; Style props also need to be registered in valid-stylemap-keys

(def base-classes
  {:foreground                   nil ;; -> foreground
   :background                   nil 
   :bracket                      :foreground
   :string                       :foreground
   :comment                      :foreground
   :constant                     :foreground ;; numbers, symbols, keywords, boolean values
   :definition                   :foreground ;; global definitions
   :annotation                   :foreground ;; ns/file info, user-label, num-dropped 
   :metadata                     :foreground
   :metadata2                    :foreground

   :highlight                    :foreground ;; default bg-color, inherited by [:printer :find]
   
   :highlight-error              :foreground ;; default bg-color, inherited by [:printer :hightlight-errror]
   :highlight-warning            :foreground
   :highlight-info               :foreground

   :highlight-underlined         :foreground
   :highlight-error-underlined   :foreground
   :highlight-warning-underlined :foreground
   :highlight-info-underlined    :foreground


   :label                        :foreground ;; literal labels, type labels, atom wrappers, etc.
   :eval-label                   :foreground ;; literal labels, type labels, atom wrappers, etc.
   
   ;; This is hack, fix it
   :eval-label-red               :foreground ;; literal labels, type labels, atom wrappers, etc.
   :eval-label-green             :foreground ;; literal labels, type labels, atom wrappers, etc.
   :eval-label-blue              :foreground ;; literal labels, type labels, atom wrappers, etc.
   
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
          :js-object-key :foreground
          :nan           :constant
          :infinity      :constant
          :-infinity     :constant
          }
         
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

;; TODO add style for print-level terminus character
(def base-printer-tokens
  {:comment               :comment
   :foreground            :foreground
   :function-args         :bracket
   :literal-label         :label
   :type-label            :label
   :lambda-label           :label
   :mutable-wrapper       :label
   :atom-wrapper          :label
   :volatile-wrapper      :label
   :metadata              :metadata
   :metadata2             :metadata2
   :metadata-key          :metadata
   :metadata-key2         :metadata
   :annotation            :annotation
   :ellipsis              :annotation
   :coll-trunction        :annotation
   :user-annotation       :annotation

   ;; Should this be find in output? or just :highlight?
   :find-in-output        :highlight

   :highlight-error              :highlight-error ;; default bg-color, inherited by [:printer :hightlight-errror]
   :highlight-warning            :highlight-warning
   :highlight-info               :highlight-info

   :highlight-underlined         :highlight-underlined
   :highlight-underlined-error   :highlight-underlined-error
   :highlight-underlined-warning :highlight-underlined-warning
   :highlight-underlined-info    :highlight-underlined-info

   :file-info             :annotation
   :line-number           :annotation
   :column-number         :annotation
   :eval-label            :eval-label
   :eval-label-red        :eval-label
   :eval-label-green      :eval-label
   :eval-label-blue       :eval-label
   :eval-form             :eval-label
   :eval-form-red         :eval-label
   :eval-form-green       :eval-label
   :eval-form-blue        :eval-label
   :result-header         {:color            nil
                           :margin-block-end :0.5em}
   :seq-bracket           :bracket
   :lazy-seq-bracket      :bracket
   :max-print-level-label :annotation
   })

(def all-base-printer-tokens (into #{} (keys base-printer-tokens)))


(def valid-stylemap-keys
  #?(:clj [:color
           :background-color
           :text-decoration-line
           :text-decoration-style
           :font-style
           :font-weight]
     :cljs [:color
            :background-color
            :width
            :text-shadow
            :text-decoration-line
            :text-decoration-style
            :font-style 
            :font-size
            :font-weight
            :border-radius
            :margin-block-end
            :margin-block-start
            :padding-block-end
            :padding-block-start
            :margin-inline-end
            :margin-inline-start
            :padding-inline-end
            :padding-inline-start
            :opacity
            :line-height
            :background-image
            :outline]))

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

(def transient-label "Transient")

(def encapsulation-opening-bracket "<")

(def encapsulation-closing-bracket ">")

(defn- mutable-wrap-count
  "Counts the label plus opening and closing encapsulation brackets.
   Example:
   (mutable-wrap-count \"Atom\") ; => 6"
  [s]
  (count (str s encapsulation-opening-bracket encapsulation-closing-bracket)))

(def atom-wrap-count
  (mutable-wrap-count atom-label))

(def volatile-wrap-count
  (mutable-wrap-count volatile-label))

(def lambda-symbol "λ")

(def js-literal-badge "#js ")

(def js-built-in-method-nm-limit 5)

(def inst-badge "#inst ")

(def uuid-badge "#uuid ")

(def fat-arrow "=>")

;; TODO - Implement this for js Map*
(def js-map-arrow "->")

(def inline-badges
  #{js-literal-badge inst-badge uuid-badge lambda-symbol})

;; For printing messages and warnings
(def italic-tag-open #?(:cljs nil :clj "\033[3m"))
(def gray-tag-open #?(:cljs nil :clj "\033[38;5;244m") )
(def bold-gray-tag-open #?(:cljs nil :clj "\033[38;5;244;1m"))
(def italic-gray-tag-open #?(:cljs nil :clj "\033[3;38;5;244m"))
(def bold-italic-gray-tag-open #?(:cljs nil :clj "\033[3;38;5;244;1m"))
(def orange-tag-open #?(:cljs nil :clj "\033[38;5;208;1m"))
(def red-tag-open #?(:cljs nil :clj "\033[38;5;196;1m"))
(def bold-tag-open #?(:cljs nil :clj "\033[1m"))
(def sgr-tag-close #?(:cljs nil :clj "\033[0;m"))


(def bling-colors*
  (apply
   array-map
   ["red"        {:sgr      196
                  :semantic "negative"}
    "orange"     {:sgr      208
                  :semantic "warning"}
    "yellow"     {:sgr 178}
    "olive"      {:sgr 106}
    "green"      {:sgr      40
                  :semantic "positive"}
    "blue"       {:sgr      39
                  :semantic "accent"}
    "purple"     {:sgr 141}
    "magenta"    {:sgr 201}
    "gray"       {:sgr      245
                  :semantic "subtle"}
    "black"      {:sgr 16}
    "white"      {:sgr 231}]))
