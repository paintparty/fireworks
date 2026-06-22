(ns fireworks.config
  (:require  
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]))

;; TODO add desc field to each entry, use for docs and potentially validation
(def options
  {:theme                         {:spec           ::config/theme
                                   :default        "Universal Neutral"
                                   :updates-theme? true}
   :truncate?                     {:spec    ::config/truncate
                                   :default true
                                   :desc    "Intended primarily to used at the call site when you want to turn off all truncation of collections and all truncation (ellipsis) of self-evaluating values (string, keywords, symbols, etc.). If set to `false`, all truncation will be capped at 1000, meaning 1000 things in a collection and a length of 1000 for self-evaluating values."}
   :bold?                         {:spec           ::config/bold
                                   :default        false
                                   :updates-theme? true
                                   :desc           "Will render the printed output with a `font-weight` of `bold`."}
   :print-length                  {:spec    ::config/print-length
                                   :default 33
                                   :desc    "Controls how many items are printed in a coll before truncation"}
   :print-length-inline-results   {:spec    ::config/print-length-inline-results
                                   :default 8
                                   :desc    "Controls how many items are printed in a coll before truncation, for inline results"}
   :single-line-coll-max-length   {:spec    ::config/single-line-coll-max-length
                                   :default 33
                                   :desc    "The strlen limit of a single-line coll, before truncation"}
   :scalar-result-max-length      {:spec    ::config/scalar-result-max-length
                                   :default 444
                                   :desc    "Sets the max length of a scalar value such as a string, keyword, function name, etc. Only applies when the value itself is the result of the evaluation (not nested within a data structure)."}
   :scalar-depth-1-max-length     {:spec    ::config/scalar-depth-1-max-length
                                   :default 69
                                   :desc    "Sets the max length of a scalar value such as a string, keyword, function name, etc. Only applies when the value is nested one level deep inside the result, which would be a non-associative collection such as a vector or seq."}
   :scalar-max-length             {:spec    ::config/scalar-max-length
                                   :default 33
                                   :desc    "Sets the max length of things like strings, keywords, function names, etc., when they are nested more than one level deep inside a data structure. Values whose length exceeds this will be ellipsized."}
   :scalar-mapkey-max-length      {:spec    ::config/scalar-mapkey-max-length
                                   :default 33 
                                   :desc    "Sets the max length of things like strings, keywords, function names, etc., when they are used as keys in maps. Longer values will be ellipsized."}
   :single-column-maps?           {:spec    ::config/single-column-maps?
                                   :default false}
   :single-column-map-threshold   {:spec    ::config/single-column-map-threshold
                                   :default 44}
   :print-level                   {:spec    ::config/print-level
                                   :default 7
                                   :desc    "Sets the max depth of printing for nested collections."}
   :print-level-inline-results    {:spec    ::config/print-level-inline-results
                                   :default 3
                                   :desc    "Sets the max depth of printing for nested collections in inline results"}
   :metadata-print-level          {:spec    ::config/metadata-print-level
                                   :default 4
                                   :desc    "Sets the max depth of printing for metadata maps that contain nested collections."}
   :display-namespaces?           {:spec    ::config/display-namespaces?
                                   :default true
                                   :desc    "Whether or not to print out fully qualified namespaces for functions and classes. Note that even if set to `true`, namespaces may get dropped if the count of fully qualified symbol exceeds the `:scalar-print-length` or the `:scalar-mapkey-max-length` (in the case of map keys)."}
   :display-metadata?             {:spec    ::config/display-metadata?
                                   :default false
                                   :desc    "Print metadata values."}
   :metadata-position             {:spec    ::config/metadata-position
                                   :default "inline"
                                   :desc    "Determines position of metadata relative to value that is carrying it. Options are `\"inline\"` (default), or `\"block\"`."}
   :regex-theme                   {:spec    ::config/regex-theme
                                   :default :neutral}                         
   :enable-rainbow-brackets?      {:spec           ::config/enable-rainbow-brackets?
                                   :default        true
                                   :updates-theme? true
                                   :desc           "Whether or not to use rainbow brackets. Rainbow brackets can be customized in your theme."}
   :bracket-contrast              {:spec           ::config/bracket-contrast
                                   :default        "high"
                                   :updates-theme? true
                                   :desc           "Sets the level of rainbow bracket intensity to `\"high\"` or `\"low\"`.  Default value can also be overridden by `:bracket-contrast` entry in a Fireworks theme map."}
   :supports-color-level          {:spec           ::config/supports-color-level
                                   :default        nil
                                   :updates-theme? true
                                   :desc           "You should generally not need to set this, as Fireworks automatically detects the host environment's level of color support, and will set this value internally. Most terminal environments support level `3` (truecolor). If set to `2`, Fireworks will convert the hex color values to sgr-rgb codes (x256) for terminal emulators that do not support 24-bit color. If set to `1`, Fireworks will use a b&w theme (\"Universal Neutral\"). If you find that your host environment's color support level is not being detected, you can set this value explicitly to match a target level of color support."}
   :print-detected-color-level?   {:spec    ::config/print-detected-color-level?
                                   :default false}
   :line-height                   {:spec           ::config/line-height
                                   :default        1.45
                                   :updates-theme? true
                                   :desc           "Sets the line-height. Only takes effect in browser consoles."}
   :label-max-length              {:spec    ::config/label-max-length
                                   :default 44
                                   :desc    "Sets the max length of the form-to-be-evaled label, or the user label, if supplied."}
   :format-label-as-code?         {:spec    ::config/format-label-as-code?
                                   :default false
                                   :desc    "If a custom label is not supplied, this will pretty-print the form-to-be-printed, instead of truncating it."}
   :label-color                   {:spec    ::config/label-color
                                   :desc    "Sets the color of the form-to-be-evaled label, or the user label, if supplied. Valid values are `:blue`, `:green`, or `:red`. All stock themes will have a preset color that is synced with the particular theme, so this option is intended to be used as an override at the call site if you have multiple printings from different places in your codebase, and you want an easy way to distinguish them from each other."
                                   :default nil}
   :margin-bottom                 {:spec    ::config/margin-bottom
                                   :default 1}
   :margin-top                    {:spec    ::config/margin-top
                                   :default 0}
   :margin-left                   {:spec    ::config/margin-inline-start
                                   :default 0}
   :margin-inline-start           {:spec    ::config/margin-inline-start
                                   :default 0}
   :print-with                    {:spec    ::config/print-with
                                   :default nil
                                   :desc    "Although more of an edge-case, you can pass a `:print-with` option at the call site if you would like to print the value using a built-in clojure core printing function. The value must be one of `pr`, `pr-str`, `prn`, `prn-str`, `print`, or `println`."}
   :find                          {:spec    ::config/find
                                   :default nil
                                   :desc    "Find and highlight values in the printed output."}
   :when                          {:spec    ::config/when
                                   :default nil
                                   :desc    "If supplied, this value should be a predicate. Will only print something if value passes predicate."}
   :dissoc-metadata-keys          {:spec    ::config/dissoc-metadata-keys
                                   :default nil}
   :select-metadata-keys          {:spec    ::config/select-metadata-keys
                                   :default nil}
   :multi-line-metadata?          {:spec    ::config/multi-line-metadata?
                                   :default true}
   :quote-symbols?                {:spec    ::config/quote-symbols?
                                   :default false}
   :quote-lists?                  {:spec    ::config/quote-lists?
                                   :default false}
   :colorize?                     {:spec    ::config/colorize?
                                   :default true
                                   :desc    "Experimental colorization for :pp mode, which dispatches to pp/pprint"}
   
   
   ;; Deprecated / Internal dev only
   :legacy-terminal?              {:spec           ::config/legacy-terminal?
                                   :default        false
                                   :updates-theme? true
                                   :deprecated?    true}
   :enable-terminal-truecolor?    {:spec           ::config/enable-terminal-truecolor?
                                   :default        true
                                   :updates-theme? true
                                   :deprecated?    true}
   :enable-terminal-italics?      {:spec           ::config/enable-terminal-italics?
                                   :default        true
                                   :updates-theme? true}
   :enable-terminal-font-weights? {:spec           ::config/enable-terminal-font-weights?
                                   :default        true
                                   :updates-theme? true}
   
   })


;; TODO prune this
;; Should these all be consistently namespaced?
(def undocumented-option-keys
  #{:fw/log?
    :fw/hide-brackets?
    :log?
    :form-meta
    :user-opts
    :fw-fnsym
    :mode
    :template
    :ns-str
    :alias-mode
    :label
    :label-color
    :quoted-fw-form
    :qf
    :elide-branches
    :p-data?})

;; Option keys
(def option-keys
  (->> options keys (into #{})))

;; Option keys that update theme
(def option-keys-that-update-theme
  (->> options
       (filter (fn [[_ m]] (:updates-theme? m)))
       keys
       (into #{})))
