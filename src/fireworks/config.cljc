(ns fireworks.config
  (:require  
   [fireworks.specs.config :as config]
   [fireworks.specs.theme :as theme]))

   
(def options
  {:theme                     {:spec           ::config/theme
                               :default        "Alabaster Light"
                               :updates-theme? true}
   :mood                      {:spec           ::theme/mood
                               :default        "light"
                               :updates-theme? true}
   :coll-limit                {:spec    ::config/coll-limit
                               :default 15}
   :evaled-form-coll-limit    {:spec    ::config/evaled-form-coll-limit
                               :default 7}
   :value-width-limit         {:spec    ::config/value-width-limit
                               :default 33}
   :mapkey-width-limit        {:spec    ::config/mapkey-width-limit
                               :default 20}
   :print-level               {:spec    ::config/print-level
                               :default 7}
   :metadata-print-level      {:spec    ::config/metadata-print-level
                               :default 7}
   :display-namespaces?       {:spec    ::config/display-namespaces?
                               :default true}
   :display-metadata?         {:spec    ::config/display-metadata?
                               :default true}
   :metadata-position         {:spec    ::config/metadata-position
                               :default "inline"}
   :enable-rainbow-brackets?  {:spec           ::config/enable-rainbow-brackets?
                               :default        true
                               :updates-theme? true}
   :bracket-contrast          {:spec           ::config/bracket-contrast
                               :default        "high"
                               :updates-theme? true}
   :enable-terminal-truecolor? {:spec          ::config/enable-terminal-truecolor?
                                :default       false
                                :updates-theme? true}
   :enable-terminal-italics?  {:spec           ::config/enable-terminal-italics?
                               :default        false
                               :updates-theme? true}
   :line-height               {:spec           ::config/line-height
                               :default        1.45
                               :updates-theme? true}
   :custom-printers           {:spec           ::config/custom-printers
                               :default        {}}
   :find                      {:spec     ::config/custom-printers
                               :default  nil}

   :display-all-built-in-js-objects-uniformly?
   {:spec    ::config/display-all-built-in-js-objects-uniformly?
    :default false}})

;; Add new option keys to this list!
(def option-keys
  #{:mapkey-width-limit
    :line-height
    :enable-terminal-italics?
    :value-width-limit
    :display-namespaces?
    :enable-rainbow-brackets?
    :enable-terminal-truecolor?
    :print-level
    :theme
    :metadata-print-level
    :mood
    :coll-limit
    :evaled-form-coll-limit
    :display-all-built-in-js-objects-uniformly?
    :display-metadata?
    :metadata-position
    :bracket-contrast
    :custom-printers
    })

;; Add new option keys that update theme to this list!
(def option-keys-that-update-theme
  #{:line-height
    :enable-terminal-italics?
    :enable-rainbow-brackets?
    :enable-terminal-truecolor?
    :mood
    :bracket-contrast
    :theme})
