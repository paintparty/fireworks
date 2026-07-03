
(ns fireworks-vscode.mood
  "Pure decision for the BLING_MOOD env var the extension sets on a Live Code watcher process so
   Fireworks/Bling colour output matches the editor's light/dark mood. Same boundary discipline as
   the other fireworks-vscode namespaces: data in (a config's :theme + the editor mood), a value or
   nil out; no VS Code, no file I/O — the TS side reads the config files and creates the terminal.

   Set BLING_MOOD = the editor mood in every case EXCEPT when the user's configured theme is a stock
   theme that already agrees with the editor: Universal / Universal Neutral (mood-agnostic), or a
   stock theme whose variant matches the editor mood. No theme, a stock variant that disagrees, or a
   custom (non-stock) theme all force the editor mood.

   `stock-theme-variants` mirrors fireworks.basethemes/stock-themes (the extension cljs build can't
   reach the Fireworks lib); fireworks-vscode.mood-test guards it against drift."
  (:require [clojure.edn :as edn]))

;; Stock theme name -> its palette variant (:light / :dark), or nil for the mood-agnostic Universal
;; themes. Mirrors fireworks.basethemes/stock-themes.
(def stock-theme-variants
  {"Alabaster Light"   :light
   "Alabaster Dark"    :dark
   "Neutral Light"     :light
   "Neutral Dark"      :dark
   "Degas Light"       :light
   "Degas Dark"        :dark
   "Zenburn Light"     :light
   "Zenburn Dark"      :dark
   "Solarized Light"   :light
   "Solarized Dark"    :dark
   "Monokai Light"     :light
   "Monokai Dark"      :dark
   "Universal"         nil
   "Universal Neutral" nil})

(defn theme-name
  "The :theme string from a Bling/Fireworks EDN config `config-text`, or nil when there is no text,
   it won't parse, or :theme isn't a string (e.g. an inline custom-theme map)."
  [config-text]
  (when config-text
    (try
      (let [t (:theme (edn/read-string config-text))]
        (when (string? t) t))
      (catch :default _ nil))))

(defn decision
  "The full BLING_MOOD decision as data, so the TS side can log the reasoning to the output channel:
   {:value <\"light\"/\"dark\"/nil>  ; the value to force, nil = leave unset
    :theme-name <string/nil>        ; the configured :theme (echoed back)
    :stock? <bool>                  ; whether it's one of the stock theme names
    :variant <:light/:dark/nil>     ; the stock theme's variant (nil = mood-agnostic / non-stock)
    :reason <string>}               ; a human sentence explaining the branch taken
   Unset only when `theme-name` is a stock theme that already agrees with `vscode-mood`."
  [theme-name vscode-mood]
  (let [stock?  (contains? stock-theme-variants theme-name)
        variant (get stock-theme-variants theme-name)]
    (cond
      (nil? theme-name)
      {:value vscode-mood :theme-name nil :stock? false :variant nil
       :reason (str "no :theme found in any config → force editor mood " (pr-str vscode-mood))}

      (not stock?)
      {:value vscode-mood :theme-name theme-name :stock? false :variant nil
       :reason (str "custom (non-stock) theme " (pr-str theme-name)
                    " → force editor mood " (pr-str vscode-mood))}

      (nil? variant)
      {:value nil :theme-name theme-name :stock? true :variant nil
       :reason (str "stock mood-agnostic theme " (pr-str theme-name) " → leave BLING_MOOD unset")}

      (= (name variant) vscode-mood)
      {:value nil :theme-name theme-name :stock? true :variant variant
       :reason (str "stock " (name variant) " theme " (pr-str theme-name)
                    " matches editor mood → leave BLING_MOOD unset")}

      :else
      {:value vscode-mood :theme-name theme-name :stock? true :variant variant
       :reason (str "stock " (name variant) " theme " (pr-str theme-name)
                    " disagrees with editor mood " (pr-str vscode-mood)
                    " → force " (pr-str vscode-mood))})))

(defn bling-mood
  "The BLING_MOOD value to force (\"light\"/\"dark\"), or nil to leave it unset. See `decision`."
  [theme-name vscode-mood]
  (:value (decision theme-name vscode-mood)))
