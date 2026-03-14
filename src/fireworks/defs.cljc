(ns ^:dev/always fireworks.defs)

;; Tokens must be present in one of the maps below in order to pick up
;; styling specified in theme.
;; Style props also need to be registered in valid-stylemap-keys
(def bling-colors
  {:red            {:sgr       196
                    :css       "#ff0000"
                    :semantic  "negative"
                    :sgr-dark  124
                    :sgr-light 203
                    :css-dark  "#af0000"
                    :css-light "#ff5f5f"}
   :medium-red     {:sgr 196
                    :css "#ff0000"}
   :dark-red       {:sgr 124
                    :css "#af0000"}
   :light-red      {:sgr 203
                    :css "#ff5f5f"}
   :orange         {:sgr       172
                    :css       "#d78700"
                    :semantic  "warning"
                    :sgr-dark  166
                    :sgr-light 208
                    :css-dark  "#d75f00"
                    :css-light "#ff8700"}
   :medium-orange  {:sgr 172
                    :css "#d78700"}
   :dark-orange    {:sgr 166
                    :css "#d75f00"}
   :light-orange   {:sgr 208
                    :css "#ff8700"}
   :yellow         {:sgr       178
                    :css       "#d7af00"
                    :sgr-dark  136
                    :sgr-light 220
                    :css-dark  "#af8700"
                    :css-light "#ffd700"}
   :medium-yellow  {:sgr 178
                    :css "#d7af00"}
   :dark-yellow    {:sgr 136
                    :css "#af8700"}
   :light-yellow   {:sgr 220
                    :css "#ffd700"}
   :olive          {:sgr       106
                    :css       "#87af00"
                    :sgr-dark  100
                    :sgr-light 143
                    :css-dark  "#878700"
                    :css-light "#afaf5f"}
   :medium-olive   {:sgr 106
                    :css "#87af00"}
   :dark-olive     {:sgr 100
                    :css "#878700"}
   :light-olive    {:sgr 143
                    :css "#afaf5f"}
   :green          {:sgr       40
                    :css       "#00d700"
                    :semantic  "positive"
                    :sgr-dark  28
                    :sgr-light 82
                    :css-dark  "#008700"
                    :css-light "#5fff00"}
   :medium-green   {:sgr 40
                    :css "#00d700"}
   :dark-green     {:sgr 28
                    :css "#008700"}
   :light-green    {:sgr 82
                    :css "#5fff00"}
   :blue           {:sgr       39
                    :css       "#00afff"
                    :semantic  "accent"
                    :sgr-dark  26
                    :sgr-light 81
                    :css-dark  "#005fd7"
                    :css-light "#5fd7ff"}
   :medium-blue    {:sgr 39
                    :css "#00afff"}
   :dark-blue      {:sgr 26
                    :css "#005fd7"}
   :light-blue     {:sgr 81
                    :css "#5fd7ff"}
   :purple         {:sgr       141
                    :css       "#af87ff"
                    :sgr-dark  129
                    :sgr-light 147
                    :css-dark  "#af00ff"
                    :css-light "#afafff"}
   :medium-purple  {:sgr 141
                    :css "#af87ff"}
   :dark-purple    {:sgr 129
                    :css "#af00ff"}
   :light-purple   {:sgr 147
                    :css "#afafff"}
   :magenta        {:sgr       201
                    :css       "#ff00ff"
                    :sgr-dark  163
                    :sgr-light 213
                    :css-dark  "#d700af"
                    :css-light "#ff87ff"}
   :medium-magenta {:sgr 201
                    :css "#ff00ff"}
   :dark-magenta   {:sgr 163
                    :css "#d700af"}
   :light-magenta  {:sgr 213
                    :css "#ff87ff"}
   :gray           {:sgr       247
                    :css       "#9e9e9e"
                    :semantic  "subtle"
                    :sgr-dark  244
                    :sgr-light 249
                    :css-dark  "#808080"
                    :css-light "#b2b2b2"}
   :medium-gray    {:sgr 247
                    :css "#9e9e9e"}
   :dark-gray      {:sgr 244
                    :css "#808080"}
   :light-gray     {:sgr 249
                    :css "#b2b2b2"}
   :black          {:sgr       16
                    :css       "#000000"
                    :sgr-dark  16
                    :sgr-light 16
                    :css-dark  nil
                    :css-light nil}
   :white          {:sgr       231
                    :css       "#ffffff"
                    :sgr-dark  231
                    :sgr-light 231
                    :css-dark  nil
                    :css-light nil}})

;; To do - change to use name and lookup by string
(defn bling-css-color [k]
  (-> k bling-colors :css))

(defn bling-sgr-color [k]
  (-> k bling-colors :sgr))

(def base-classes
  {:foreground                   nil ;; -> foreground
   :background                   nil
   :bracket                      :foreground
   :string                       :foreground
   :number                       :foreground
   :comment                      :foreground
   :constant                     :foreground ;; numbers, symbols, keywords, boolean values
   :definition                   :foreground ;; global definitions
   :annotation                   :foreground ;; ns/file info, user-label, num-dropped 
   :metadata                     :foreground
   :metadata2                    :foreground

   :highlight                    :foreground ;; default bg-color, inherited by [:printer :find]

   :highlight-error              :foreground ;; default bg-color, inherited by [:printer :highlight-errror]
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
  (merge {:string                                       :string
          :number                                       :constant
          :decimal                                      :constant
          :keyword                                      :constant
          :boolean                                      :constant
          :nil                                          :constant
          :symbol                                       :definition
          :regex                                        :string
          :def                                          :definition
          :local                                        :definition
          :local-binding                                :definition
          :function                                     :definition
          :class                                        :definition
          :datatype                                     :definition
          :record                                       :definition
          :multimethod                                  :definition
          :uuid                                         :string
          :inst                                         :string
          :js-object-key                                :foreground
          :nan                                          :constant
          :infinity                                     :constant
          :-infinity                                    :constant
          :escaped-double-quote-char                    :annotation
          :escape-char                                  :string
          :string-delimiter                             :string

          ;; regex-related
          :regex.quantifier                             :foreground
          :regex.numeric-quantifier                     :foreground
          :regex.group-mods                             :foreground
          :regex.any-of-delimeter                       :foreground
          :regex.group-delimeter                        :foreground
          :regex.not-any-of-delimeter                   :foreground
          :regex.anchor                                 :foreground
          :regex.character-range                        :foreground
          :regex.special-character                      :foreground
          :regex.escape-backslash                       :foreground
          :regex.number-range                           :foreground
          :regex.character                              :foreground
          :regex.alternation                            :foreground

          :regex.quantifier.neutral                     :foreground
          :regex.numeric-quantifier.neutral             :foreground
          :regex.group-mods.neutral                     :foreground
          :regex.any-of-delimeter.neutral               :foreground
          :regex.group-delimeter.neutral                :foreground
          :regex.not-any-of-delimeter.neutral           :foreground
          :regex.anchor.neutral                         :foreground
          :regex.character-range.neutral                :foreground
          :regex.special-character.neutral              :foreground
          :regex.escape-backslash.neutral               :foreground
          :regex.number-range.neutral                   :foreground
          :regex.character.neutral                      :foreground
          :regex.alternation.neutral                    :foreground

          :regex.quantifier.in-group                    :foreground
          :regex.numeric-quantifier.in-group            :foreground
          :regex.group-mods.in-group                    :foreground
          :regex.any-of-delimeter.in-group              :foreground
          :regex.group-delimeter.in-group               :foreground
          :regex.not-any-of-delimeter.in-group          :foreground
          :regex.anchor.in-group                        :foreground
          :regex.character-range.in-group               :foreground
          :regex.special-character.in-group             :foreground
          :regex.escape-backslash.in-group              :foreground
          :regex.number-range.in-group                  :foreground
          :regex.character.in-group                     :foreground
          :regex.alternation.in-group                   :foreground

          :regex.quantifier.in-any-of                   :foreground
          :regex.numeric-quantifier.in-any-of           :foreground
          :regex.group-mods.in-any-of                   :foreground
          :regex.any-of-delimeter.in-any-of             :foreground
          :regex.group-delimeter.in-any-of              :foreground
          :regex.not-any-of-delimeter.in-any-of         :foreground
          :regex.anchor.in-any-of                       :foreground
          :regex.character-range.in-any-of              :foreground
          :regex.special-character.in-any-of            :foreground
          :regex.escape-backslash.in-any-of             :foreground
          :regex.number-range.in-any-of                 :foreground
          :regex.character.in-any-of                    :foreground
          :regex.alternation.in-any-of                  :foreground

          :regex.quantifier.in-any-of.neutral           :foreground
          :regex.numeric-quantifier.in-any-of.neutral   :foreground
          :regex.group-mods.in-any-of.neutral           :foreground
          :regex.any-of-delimeter.in-any-of.neutral     :foreground
          :regex.group-delimeter.in-any-of.neutral      :foreground
          :regex.not-any-of-delimeter.in-any-of.neutral :foreground
          :regex.anchor.in-any-of.neutral               :foreground
          :regex.character-range.in-any-of.neutral      :foreground
          :regex.special-character.in-any-of.neutral    :foreground
          :regex.escape-backslash.in-any-of.neutral     :foreground
          :regex.number-range.in-any-of.neutral         :foreground
          :regex.character.in-any-of.neutral            :foreground
          :regex.alternation.in-any-of.neutral          :foreground

          :regex.quantifier.in-group.neutral            :foreground
          :regex.numeric-quantifier.in-group.neutral    :foreground
          :regex.group-mods.in-group.neutral            :foreground
          :regex.any-of-delimeter.in-group.neutral      :foreground
          :regex.group-delimeter.in-group.neutral       :foreground
          :regex.not-any-of-delimeter.in-group.neutral  :foreground
          :regex.anchor.in-group.neutral                :foreground
          :regex.character-range.in-group.neutral       :foreground
          :regex.special-character.in-group.neutral     :foreground
          :regex.escape-backslash.in-group.neutral      :foreground
          :regex.number-range.in-group.neutral          :foreground
          :regex.character.in-group.neutral             :foreground
          :regex.alternation.in-group.neutral           :foreground}

         ;;  Experimental, not working yet 
         {;;  :square-bracket  :bracket
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

   :highlight-error              :highlight-error ;; default bg-color, inherited by [:printer :highlight-errror]
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
   :max-print-level-label :annotation})

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

(def ellipsis "...")

(def max-print-level-label "#")

(def ellipsis-count (count ellipsis))

(def atom-label "Atom")

(def volatile-label "Volatile")

(def ref-label "Ref")

(def agent-label "Agent")

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

(def regex-badge "")

(def fat-arrow "=>")

;; TODO - Implement this for js Map*
(def js-map-arrow "->")

(def inline-badges
  #{js-literal-badge inst-badge uuid-badge lambda-symbol})

;; For printing messages and warnings
(def italic-tag-open #?(:cljs nil :clj "\033[3m"))
(def sgr-tag-close #?(:cljs nil :clj "\033[0;m"))


;; To do - eliminate this, use bling-colors
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
    "gray"       {:sgr      247
                  :semantic "subtle"}
    "black"      {:sgr 16}
    "white"      {:sgr 231}]))


#?(:cljs
   (do (def html-collection-types-primary
         #{"NodeList"
           "DOMTokenList"
           "HTMLCollection"
           "CSSStyleDeclaration"
           "StyleSheetList"
           "CSSRuleList"
           "DOMStringList"})
       (def html-collection-types-secondary
         #{"RadioNodeList"
           "NamedNodeMap"
           "HTMLOptionsCollection"
           "HTMLFormControlsCollection"
           "HTMLAllCollection"
           "MediaList"
           "DOMRectList"
           "FileList"
           "TouchList"
           "DataTransferItemList"})))

(def highlight-dark
  {:color            "#ffffff"
   :background-color "#0000e0"
   :font-weight      :bold})

(def highlight-underlined-dark
  {:color                 "#ffffff"
   :background-color      "#0000e0"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-warning-dark
  {:background-color "#6b4200"
   :color            "#ffe4b8"
   :font-weight      :bold})

(def highlight-warning-underlined-dark
  {:background-color      "#6b4200"
   :color                 "#ffe4b8"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-error-dark
  {:background-color "#670013"
   :color            "#ffe0e0"
   :font-weight      :bold})

(def highlight-error-underlined-dark
  {:background-color      "#670013"
   :color                 "#ffe0e0"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-light                    
  {:background-color "#ffee00"
   :color            "#003c5c"
   :font-weight      :bold})

(def highlight-underlined-light         
  {:background-color      "#ffee00"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-warning-light            
  {:background-color "#ffdea8"
   :color            "#754800"
   :font-weight      :bold})

(def highlight-info-light               
  highlight-light)

(def highlight-error-light
  {:background-color "#ffdbdb"
   :color            "#660000"
   :font-weight      :bold})

(def highlight-error-underlined-light   
  {:background-color      "#ffdbdb"
   :color                 "#660000"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-warning-underlined-light 
  {:background-color      "#ffdea8"
   :color                 "#754800"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})

(def highlight-info-underlined-light    
  {:background-color      "#d6efff"
   :color                 "#003c5c"
   :font-weight           :bold
   :text-decoration-line  :underline
   :text-decoration-style :wavy})





(def highlight-universal
  {:background-color "#8a8a8a"
   :color            "#ffffff"
   :font-weight      :bold})

;; These need to be updated if above maps change! -----------------------------
(def highlight-error-dark-sgr
  "\033[38;2;255;224;224;1;48;2;103;0;19m")

(def highlight-error-light-sgr
  "\033[38;2;102;0;0;1;48;2;255;219;219m")

(def highlight-universal-sgr
  "\033[38;2;255;255;255;1;48;2;138;138;138m")
;; -----------------------------------------------------------------------------

(def highlight-classes
  {:highlight-dark                     highlight-dark
   :highlight-underlined-dark          highlight-dark
   :highlight-warning-dark             highlight-warning-dark
   :highlight-warning-underlined-dark  highlight-warning-underlined-dark
   :highlight-error-dark               highlight-error-dark
   :highlight-error-underlined-dark    highlight-error-underlined-dark
   :highlight-light                    highlight-light
   :highlight-warning-light            highlight-warning-light
   :highlight-warning-underlined-light highlight-warning-underlined-light
   :highlight-error-light              highlight-error-light
   :highlight-error-underlined-light   highlight-error-underlined-light
   :highlight-universal                highlight-universal})

