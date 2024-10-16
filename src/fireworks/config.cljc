(ns fireworks.config
  (:require  
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]))

   
(def options
  {:theme                         {:spec           ::config/theme
                                   :default        "Alabaster Light"
                                   :updates-theme? true}
   :mood                          {:spec           ::theme/mood
                                   :default        "light"
                                   :updates-theme? true}
   :coll-limit                    {:spec    ::config/coll-limit
                                   :default 15}
   :single-line-coll-length-limit {:spec    ::config/single-line-coll-length-limit
                                   :default 15}
   :evaled-form-coll-limit        {:spec    ::config/evaled-form-coll-limit
                                   :default 7}
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
   :enable-terminal-truecolor?    {:spec           ::config/enable-terminal-truecolor?
                                   :default        false
                                   :updates-theme? true}
   :enable-terminal-italics?      {:spec           ::config/enable-terminal-italics?
                                   :default        false
                                   :updates-theme? true}
   :enable-terminal-font-weights? {:spec           ::config/enable-terminal-font-weights?
                                   :default        false
                                   :updates-theme? true}
   :line-height                   {:spec           ::config/line-height
                                   :default        1.45
                                   :updates-theme? true}
   :label-length-limit            {:spec           ::config/label-length-limit
                                   :default        25}
   :custom-printers               {:spec    ::config/custom-printers
                                   :default {}}
   :find                          {:spec    ::config/custom-printers
                                   :default nil}
   })

;; Add new option keys to this list!
;; TODO - maybe dynamically construct this from (-> options-map keys (into #{}))
(def option-keys
  #{:line-height
    :label-length-limit
    :enable-terminal-italics?
    :enable-terminal-font-weights?
    :non-coll-result-length-limit
    :non-coll-depth-1-length-limit
    :non-coll-mapkey-length-limit
    :non-coll-length-limit
    :display-namespaces?
    :enable-rainbow-brackets?
    :enable-terminal-truecolor?
    :print-level
    :theme
    :metadata-print-level
    :mood
    :coll-limit
    :single-line-coll-length-limit
    :evaled-form-coll-limit
    :display-metadata?
    :metadata-position
    :bracket-contrast
    :custom-printers
    })

;; Add new option keys that update theme to this list!
;; TODO - maybe dynamically construct this from
;; (->> options-map
;;      (filter (fn [[_ m]] (:updates-theme? m)))
;;      keys
;;      (into #{}))

(def option-keys-that-update-theme
  #{:line-height
    :enable-terminal-italics?
    :enable-terminal-font-weights?
    :enable-rainbow-brackets?
    :enable-terminal-truecolor?
    :mood
    :bracket-contrast
    :theme})
