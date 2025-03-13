(ns fireworks.config
  (:require  
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]))

;; TODO add desc field to each entry, use for docs and potenitaly validation
(def options
  {:theme                         {:spec           ::config/theme
                                   :default        "Alabaster Light"
                                   :updates-theme? true}
   :mood                          {:spec           ::theme/mood
                                   :default        "light"
                                   :updates-theme? true}
   :coll-limit                    {:spec    ::config/coll-limit
                                   :default 33}
   :single-line-coll-length-limit {:spec    ::config/single-line-coll-length-limit
                                   :default 33}
   :non-coll-result-length-limit  {:spec    ::config/non-coll-result-length-limit
                                   :default 444}
   :non-coll-depth-1-length-limit {:spec    ::config/non-coll-depth-1-length-limit
                                   :default 69}
   :non-coll-length-limit         {:spec    ::config/non-coll-length-limit
                                   :default 33}
   :non-coll-mapkey-length-limit  {:spec    ::config/non-coll-mapkey-length-limit
                                   :default 20}
   :print-level                   {:spec    ::config/print-level
                                   :default 7}
   :metadata-print-level          {:spec    ::config/metadata-print-level
                                   :default 7}
   :display-namespaces?           {:spec    ::config/display-namespaces?
                                   :default true}
   :display-metadata?             {:spec    ::config/display-metadata?
                                   :default true}
   :metadata-position             {:spec    ::config/metadata-position
                                   :default "inline"}
   :enable-rainbow-brackets?      {:spec           ::config/enable-rainbow-brackets?
                                   :default        true
                                   :updates-theme? true}
   :bracket-contrast              {:spec           ::config/bracket-contrast
                                   :default        "high"
                                   :updates-theme? true}
   :legacy-terminal?              {:spec           ::config/legacy-terminal?
                                   :default        false
                                   :updates-theme? true}
   :enable-terminal-truecolor?    {:spec           ::config/enable-terminal-truecolor?
                                   :default        true
                                   :updates-theme? true}
   :enable-terminal-italics?      {:spec           ::config/enable-terminal-italics?
                                   :default        true
                                   :updates-theme? true}
   :enable-terminal-font-weights? {:spec           ::config/enable-terminal-font-weights?
                                   :default        true
                                   :updates-theme? true}
   :line-height                   {:spec           ::config/line-height
                                   :default        1.45
                                   :updates-theme? true}
   :label-length-limit            {:spec    ::config/label-length-limit
                                   :default 44}
   :margin-bottom                 {:spec    ::config/margin-bottom
                                   :default 1}
   :margin-top                    {:spec    ::config/margin-top
                                   :default 0}
   :custom-printers               {:spec    ::config/custom-printers
                                   :default {}}
   :find                          {:spec    ::config/find
                                   :default nil}
   :when                          {:spec    ::config/when
                                   :default nil}
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
