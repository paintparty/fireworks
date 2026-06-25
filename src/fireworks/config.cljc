(ns fireworks.config
  (:require
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]
   #?@(:clj [[clojure.string :as str]
             [clojure.java.io :as io]])))

;; TODO add desc field to each entry, use for docs and potentially validation
(def options
  {:theme                         {:spec           ::config/theme
                                   :default        "Universal Neutral"
                                   :updates-theme? true
                                   :category       :theme
                                   :tags           [:theme]}
   :truncate?                     {:spec    ::config/truncate
                                   :default true
                                   :desc    "Intended primarily to used at the call site when you want to turn off all truncation of collections and all truncation (ellipsis) of self-evaluating values (string, keywords, symbols, etc.). If set to `false`, all truncation will be capped at 1000, meaning 1000 things in a collection and a length of 1000 for self-evaluating values."
                                   :category :limits
                                   :tags     [:limits :collections :strings]}
   :bold?                         {:spec           ::config/bold
                                   :default        false
                                   :updates-theme? true
                                   :desc           "Will render the printed output with a `font-weight` of `bold`."
                                   :category       :decorations
                                   :tags           [:decorations :theme]}
   :print-length                  {:spec    ::config/print-length
                                   :default 33
                                   :desc    "Controls how many items are printed in a coll before truncation"
                                   :category :limits
                                   :tags     [:limits :collections]}
   :print-length-inline-results   {:spec    ::config/print-length-inline-results
                                   :default 8
                                   :desc    "Controls how many items are printed in a coll before truncation, for inline results"
                                   :category :limits
                                   :tags     [:limits :collections :inline-results]}
   :single-line-coll-max-length   {:spec    ::config/single-line-coll-max-length
                                   :default 33
                                   :desc    "The strlen limit of a single-line coll, before truncation"
                                   :category :limits
                                   :tags     [:limits :collections]}
   :scalar-result-max-length      {:spec    ::config/scalar-result-max-length
                                   :default 444
                                   :desc    "Sets the max length of a scalar value such as a string, keyword, function name, etc. Only applies when the value itself is the result of the evaluation (not nested within a data structure)."
                                   :category :limits
                                   :tags     [:limits :scalars]}
   :scalar-depth-1-max-length     {:spec    ::config/scalar-depth-1-max-length
                                   :default 69
                                   :desc    "Sets the max length of a scalar value such as a string, keyword, function name, etc. Only applies when the value is nested one level deep inside the result, which would be a non-associative collection such as a vector or seq."
                                   :category :limits
                                   :tags     [:limits :scalars]}
   :scalar-max-length             {:spec    ::config/scalar-max-length
                                   :default 33
                                   :desc    "Sets the max length of things like strings, keywords, function names, etc., when they are nested more than one level deep inside a data structure. Values whose length exceeds this will be ellipsized."
                                   :category :limits
                                   :tags     [:limits :scalars]}
   :scalar-mapkey-max-length      {:spec    ::config/scalar-mapkey-max-length
                                   :default 33
                                   :desc    "Sets the max length of things like strings, keywords, function names, etc., when they are used as keys in maps. Longer values will be ellipsized."
                                   :category :limits
                                   :tags     [:limits :scalars :maps]}
   :single-column-maps?           {:spec    ::config/single-column-maps?
                                   :default false
                                   :category :decorations
                                   :tags     [:decorations :maps]}
   :single-column-map-threshold   {:spec    ::config/single-column-map-threshold
                                   :default 44
                                   :category :decorations
                                   :tags     [:decorations :maps]}
   :print-level                   {:spec    ::config/print-level
                                   :default 7
                                   :desc    "Sets the max depth of printing for nested collections."
                                   :category :limits
                                   :tags     [:limits :nesting]}
   :print-level-inline-results    {:spec    ::config/print-level-inline-results
                                   :default 3
                                   :desc    "Sets the max depth of printing for nested collections in inline results"
                                   :category :limits
                                   :tags     [:limits :nesting :inline-results]}
   :metadata-print-level          {:spec    ::config/metadata-print-level
                                   :default 4
                                   :desc    "Sets the max depth of printing for metadata maps that contain nested collections."
                                   :category :limits
                                   :tags     [:limits :metadata :nesting]}
   :display-namespaces?           {:spec    ::config/display-namespaces?
                                   :default true
                                   :desc    "Whether or not to print out fully qualified namespaces for functions and classes. Note that even if set to `true`, namespaces may get dropped if the count of fully qualified symbol exceeds the `:scalar-print-length` or the `:scalar-mapkey-max-length` (in the case of map keys)."
                                   :category :decorations
                                   :tags     [:decorations :namespaces]}
   :display-metadata?             {:spec    ::config/display-metadata?
                                   :default false
                                   :desc    "Print metadata values."
                                   :category :decorations
                                   :tags     [:decorations :metadata]}
   :metadata-position             {:spec    ::config/metadata-position
                                   :default "inline"
                                   :desc    "Determines position of metadata relative to value that is carrying it. Options are `\"inline\"` (default), or `\"block\"`."
                                   :category :decorations
                                   :tags     [:decorations :metadata]}
   :regex-theme                   {:spec    ::config/regex-theme
                                   :default :neutral
                                   :category :decorations
                                   :tags     [:decorations :theme]}
   :enable-rainbow-brackets?      {:spec           ::config/enable-rainbow-brackets?
                                   :default        true
                                   :updates-theme? true
                                   :desc           "Whether or not to use rainbow brackets. Rainbow brackets can be customized in your theme."
                                   :category       :decorations
                                   :tags           [:decorations :theme :brackets]}
   :bracket-contrast              {:spec           ::config/bracket-contrast
                                   :default        "high"
                                   :updates-theme? true
                                   :desc           "Sets the level of rainbow bracket intensity to `\"high\"` or `\"low\"`.  Default value can also be overridden by `:bracket-contrast` entry in a Fireworks theme map."
                                   :category       :decorations
                                   :tags           [:decorations :theme :brackets]}
   :supports-color-level          {:spec           ::config/supports-color-level
                                   :default        nil
                                   :updates-theme? true
                                   :desc           "You should generally not need to set this, as Fireworks automatically detects the host environment's level of color support, and will set this value internally. Most terminal environments support level `3` (truecolor). If set to `2`, Fireworks will convert the hex color values to sgr-rgb codes (x256) for terminal emulators that do not support 24-bit color. If set to `1`, Fireworks will use a b&w theme (\"Universal Neutral\"). If you find that your host environment's color support level is not being detected, you can set this value explicitly to match a target level of color support."
                                   :category       :decorations
                                   :tags           [:decorations :theme :color]}
   :print-detected-color-level?   {:spec    ::config/print-detected-color-level?
                                   :default false
                                   :category :decorations
                                   :tags     [:decorations :color]}
   :line-height                   {:spec           ::config/line-height
                                   :default        1.45
                                   :updates-theme? true
                                   :desc           "Sets the line-height. Only takes effect in browser consoles."
                                   :category       :limits
                                   :tags           [:limits :theme]}
   :label-max-length              {:spec    ::config/label-max-length
                                   :default 44
                                   :desc    "Sets the max length of the form-to-be-evaled label, or the user label, if supplied."
                                   :category :fireworks
                                   :tags     [:fireworks :labels :limits]}
   :format-label-as-code?         {:spec    ::config/format-label-as-code?
                                   :default false
                                   :desc    "If a custom label is not supplied, this will pretty-print the form-to-be-printed, instead of truncating it."
                                   :category :fireworks
                                   :tags     [:fireworks :labels]}
   :label-color                   {:spec    ::config/label-color
                                   :desc    "Sets the color of the form-to-be-evaled label, or the user label, if supplied. Valid values are `:blue`, `:green`, or `:red`. All stock themes will have a preset color that is synced with the particular theme, so this option is intended to be used as an override at the call site if you have multiple printings from different places in your codebase, and you want an easy way to distinguish them from each other."
                                   :default nil
                                   :category :fireworks
                                   :tags     [:fireworks :labels :theme]}
   :margin-bottom                 {:spec    ::config/margin-bottom
                                   :default 1
                                   :category :margins
                                   :tags     [:margins]}
   :margin-top                    {:spec    ::config/margin-top
                                   :default 0
                                   :category :margins
                                   :tags     [:margins]}
   :margin-left                   {:spec    ::config/margin-inline-start
                                   :default 0
                                   :category :margins
                                   :tags     [:margins]}
   :margin-inline-start           {:spec    ::config/margin-inline-start
                                   :default 0
                                   :category :margins
                                   :tags     [:margins]}
   :print-with                    {:spec    ::config/print-with
                                   :default nil
                                   :desc    "A custom printing formatting / printing function can be passed at the call site. The function must ultimately call a core printing function such as `clojure.core/println`"
                                   :category :fireworks
                                   :tags     [:fireworks :call-site]}
   :find                          {:spec    ::config/find
                                   :default nil
                                   :desc    "Find and highlight values in the printed output."
                                   :category :decorations
                                   :tags     [:decorations]}
   :when                          {:spec    ::config/when
                                   :default nil
                                   :desc    "If supplied, this value should be a predicate. Will only print something if value passes predicate."
                                   :category :fireworks
                                   :tags     [:fireworks :call-site]}
   :dissoc-metadata-keys          {:spec    ::config/dissoc-metadata-keys
                                   :default nil
                                   :category :decorations
                                   :tags     [:decorations :metadata]}
   :select-metadata-keys          {:spec    ::config/select-metadata-keys
                                   :default nil
                                   :category :decorations
                                   :tags     [:decorations :metadata]}
   :multi-line-metadata?          {:spec    ::config/multi-line-metadata?
                                   :default true
                                   :category :decorations
                                   :tags     [:decorations :metadata]}
   :quote-symbols?                {:spec    ::config/quote-symbols?
                                   :default false
                                   :category :decorations
                                   :tags     [:decorations :symbols]}
   :quote-lists?                  {:spec    ::config/quote-lists?
                                   :default false
                                   :category :decorations
                                   :tags     [:decorations :lists]}
   :colorize?                     {:spec    ::config/colorize?
                                   :default true
                                   :desc    "Experimental colorization for :pp mode, which dispatches to pp/pprint"
                                   :category :decorations
                                   :tags     [:decorations]}


   ;; Deprecated / Internal dev only
   :legacy-terminal?              {:spec           ::config/legacy-terminal?
                                   :default        false
                                   :updates-theme? true
                                   :deprecated?    true
                                   :category       :deprecated
                                   :tags           [:deprecated]}
   :enable-terminal-truecolor?    {:spec           ::config/enable-terminal-truecolor?
                                   :default        true
                                   :updates-theme? true
                                   :deprecated?    true
                                   :category       :deprecated
                                   :tags           [:deprecated]}
   :enable-terminal-italics?      {:spec           ::config/enable-terminal-italics?
                                   :default        true
                                   :updates-theme? true
                                   :category       :deprecated
                                   :tags           [:deprecated]}
   :enable-terminal-font-weights? {:spec           ::config/enable-terminal-font-weights?
                                   :default        true
                                   :updates-theme? true
                                   :category       :deprecated
                                   :tags           [:deprecated]}})


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
    :p-data?

    :display-file-info?
    :display-form-or-label?
    :data?
    :trace?
    })

;; Option keys
(def option-keys
  (->> options keys (into #{})))

;; Option keys that update theme
(def option-keys-that-update-theme
  (->> options
       (filter (fn [[_ m]] (:updates-theme? m)))
       keys
       (into #{})))

;; This section writes an example config.edn file, based on the
;; fireworks.config/options
#?(:clj
   (do
     ;; Blank lines above an option line (between its desc and the option).
     (def option-margin-top 1)

     ;; Blank lines above a desc block (separating consecutive entries).
     (def options-desc-margin-top 3)

     ;; Blank lines above a section header (separating sections).
     (def section-header-margin-top 4)

     (def ^:private example-theme-names
       ["Universal Neutral"
        "Alabaster Light"
        "Alabaster Dark"
        "Neutral Light"
        "Neutral Dark"
        "Degas Light"
        "Degas Dark"
        "Zenburn Light"
        "Zenburn Dark"
        "Solarized Light"
        "Solarized Dark"
        "Monokai Light"
        "Monokai Dark"])

     (def ^:private example-category-order
       [:theme :limits :decorations :margins :fireworks :undocumented :deprecated])

     (def ^:private example-category-titles
       {:theme        "Themes"
        :limits       "Limits"
        :decorations  "Decorations"
        :margins      "Margins"
        :fireworks    "Fireworks"
        :undocumented "Undocumented"
        :deprecated   "Deprecated"})

     (defn- example-section-header [title]
       (let [rule (str "  ;; " (str/join (repeat 75 "-")))]
         (str rule "\n"
              "  ;;  " title " \n"
              rule)))

     (defn- example-wrap-lines [desc]
       (let [max-w 74
             words (str/split (str/trim desc) #"\s+")]
         (loop [words words, cur "", lines []]
           (if (empty? words)
             (if (empty? cur) lines (conj lines cur))
             (let [w    (first words)
                   cand (if (empty? cur) w (str cur " " w))]
               (if (and (seq cur) (> (count cand) max-w))
                 (recur (rest words) w (conj lines cur))
                 (recur (rest words) cand lines)))))))

     (defn- example-format-desc [desc]
       (str/join "\n"
         (map #(str "  ;; " %) (example-wrap-lines desc))))

     (defn- example-option-line [k default-val]
       (let [key-str (str ":" (name k))
             pad     (str/join (repeat (max 1 (- 31 (count key-str))) " "))
             dval    (pr-str default-val)]
         (str "  ;; " key-str pad dval " ; defaults to " dval)))

     (defn- example-format-entry [k m]
       (let [line (example-option-line k (:default m))]
         (if-let [desc (:desc m)]
           (str (example-format-desc desc)
                (apply str (repeat (inc option-margin-top) "\n"))
                line)
           line)))

     ;; Separator yielding `n` intervening lines: all blank except the
     ;; penultimate, which is indented `  `.
     (defn- example-margin [n]
       (let [lines (map (fn [i] (if (= i (- n 2)) "  " "")) (range n))]
         (str "\n" (str/join "\n" lines) "\n")))

     (defn- example-theme-section []
       (str (example-section-header "Themes")
            "\n\n  :theme \"" (first example-theme-names) "\"\n"
            (str/join "\n"
              (map #(str "  ;; :theme \"" % "\"")
                   (rest example-theme-names)))))

     (defn- example-other-section [category]
       (let [entries (->> options
                          (filter (fn [[_ m]] (= (:category m) category)))
                          (sort-by (fn [[k _]] (name k))))]
         (when (seq entries)
           (str (example-section-header (example-category-titles category))
                "\n\n"
                (str/join (example-margin options-desc-margin-top)
                  (map (fn [[k m]] (example-format-entry k m)) entries))))))

     (defn generate-example-config!
       "Generates an example config file from the options map.
        Run from the REPL or a script. Writes to path (default:
        docs/example-bling-config/config.edn)."
       ([]
        (generate-example-config! "docs/example-bling-config/config.edn"))
       ([path]
        (let [sections (cons (example-theme-section)
                             (keep example-other-section
                                   (rest example-category-order)))
              content  (str " {\n\n"
                            (str/join (example-margin section-header-margin-top) sections)
                            "\n\n }\n")]
          (io/make-parents path)
          (spit path content)
          (println "Wrote" path))))))

