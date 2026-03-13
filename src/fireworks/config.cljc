(ns fireworks.config
  (:require  
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]))

;; TODO add desc field to each entry, use for docs and potenitaly validation
(def options
  {:theme                                    {:spec           ::config/theme
                                              :default        "Universal Neutral"
                                              :updates-theme? true}
   :truncate?                                {:spec    ::config/truncate
                                              :default true}
   :bold?                                    {:spec           ::config/bold
                                              :default        false
                                              :updates-theme? true}

   ;; Controls how many items are printed in a coll before truncation
   :print-length                             {:spec    ::config/print-length
                                              :default 33}
   
   ;; The strlen limit of a single-line coll, before truncation
   :single-line-coll-max-length              {:spec    ::config/single-line-coll-max-length
                                              :default 33}

   :scalar-result-max-length                 {:spec    ::config/scalar-result-max-length
                                              :default 444}
   :scalar-depth-1-max-length                {:spec    ::config/scalar-depth-1-max-length
                                              :default 69}
   :scalar-print-length                      {:spec    ::config/scalar-print-length
                                              :default 33}

   ;; Change this to max-length
   :scalar-mapkey-print-length               {:spec    ::config/scalar-mapkey-print-length
                                              :default 20}
   :single-column-maps?                      {:spec    ::config/single-column-maps?
                                              :default false}

   ;; Change this to :single-column-map-threshold 
   :single-column-map-print-length-threshold {:spec    ::config/single-column-map-print-length-threshold
                                              :default 44}

   :print-level                              {:spec    ::config/print-level
                                              :default 7}
   :metadata-print-level                     {:spec    ::config/metadata-print-level
                                              :default 4}
   :display-fn-namespaces?                   {:spec    ::config/display-fn-namespaces?
                                              :default true}
   :display-metadata?                        {:spec    ::config/display-metadata?
                                              :default false}
   :metadata-position                        {:spec    ::config/metadata-position
                                              :default "inline"}
   :regex-theme                              {:spec    ::config/regex-theme
                                              :default :neutral}                         
   :enable-rainbow-brackets?                 {:spec           ::config/enable-rainbow-brackets?
                                              :default        true
                                              :updates-theme? true}
   :bracket-contrast                         {:spec           ::config/bracket-contrast
                                              :default        "high"
                                              :updates-theme? true}
   :supports-color-level                     {:spec           ::config/supports-color-level
                                              :default        nil
                                              :updates-theme? true}
   :print-detected-color-level?              {:spec    ::config/print-detected-color-level?
                                              :default false}
   :legacy-terminal?                         {:spec           ::config/legacy-terminal?
                                              :default        false
                                              :updates-theme? true
                                              :deprecated?    true}
   :enable-terminal-truecolor?               {:spec           ::config/enable-terminal-truecolor?
                                              :default        true
                                              :updates-theme? true
                                              :deprecated?    true}
   :enable-terminal-italics?                 {:spec           ::config/enable-terminal-italics?
                                              :default        true
                                              :updates-theme? true}
   :enable-terminal-font-weights?            {:spec           ::config/enable-terminal-font-weights?
                                              :default        true
                                              :updates-theme? true}
   :line-height                              {:spec           ::config/line-height
                                              :default        1.45
                                              :updates-theme? true}
   ;; Change to max-length
   :label-print-length                       {:spec    ::config/label-print-length
                                              :default 44}
   :format-label-as-code?                    {:spec    ::config/format-label-as-code?
                                              :default false}
   :margin-bottom                            {:spec    ::config/margin-bottom
                                              :default 1}
   :margin-top                               {:spec    ::config/margin-top
                                              :default 0}
   :margin-left                              {:spec    ::config/margin-inline-start
                                              :default 0}
   :margin-inline-start                      {:spec    ::config/margin-inline-start
                                              :default 0}
   :print-with                               {:spec    ::config/print-with
                                              :default nil}
   :find                                     {:spec    ::config/find
                                              :default nil}
   :when                                     {:spec    ::config/when
                                              :default nil}
   :dissoc-metadata-keys                     {:spec    ::config/dissoc-metadata-keys
                                              :default nil}
   :select-metadata-keys                     {:spec    ::config/select-metadata-keys
                                              :default nil}
   :multi-line-metadata?                     {:spec    ::config/multi-line-metadata?
                                              :default true}
   :quote-symbols?                           {:spec    ::config/quote-symbols?
                                              :default false}
   })

;; TODO prune this
(def undocumented-option-keys
  #{:fw/log?
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
