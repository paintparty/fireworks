
(ns fireworks-vscode.mood
  "Pure decision for the FIREWORKS_THEME env var the extension sets on a Live Code watcher command so
   Fireworks colour output matches the editor's light/dark mood. Same boundary discipline as the
   other fireworks-vscode namespaces: data in (the current FIREWORKS_THEME value + the editor mood), a
   value or nil out; no VS Code, no env/file I/O — the TS side reads process.env.FIREWORKS_THEME and
   builds the command.

   FIREWORKS_THEME (read by Fireworks, and overriding a config :theme) may be a stock theme name
   (\"Alabaster Dark\"), a mood word (\"light\"/\"dark\"/\"neutral\"), or a custom value. The decision:
   - unset               → force the editor mood (\"light\"/\"dark\")
   - stock theme name    → leave as-is when its variant already agrees with the editor (or it's a
                           mood-agnostic Universal theme); otherwise force the SIBLING variant of the
                           same family (\"Monokai Dark\" + light editor → \"Monokai Light\")
   - \"neutral\"           → force FIREWORKS_THEME=neutral
   - \"light\"/\"dark\"      → leave as-is when it agrees with the editor; otherwise force the editor mood
   - anything else       → leave as-is (respect a custom theme / synced-theme string)
   `value` nil means \"don't set a prefix\"; the watcher then inherits FIREWORKS_THEME from the env it was
   launched with (the terminal inherits process.env, which is where we read it from).

   `stock-theme-variants` mirrors fireworks.basethemes/stock-themes (the extension cljs build can't
   reach the Fireworks lib); fireworks-vscode.mood-test guards it against drift."
  (:require [clojure.string :as str]))

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

(defn- sibling-variant
  "The same stock theme family with its variant word set to match `vscode-mood`:
   \"Monokai Dark\" + \"light\" -> \"Monokai Light\"."
  [stock-name vscode-mood]
  (let [base (str/replace stock-name #"\s+(Light|Dark|Neutral)$" "")]
    (str base " " (if (= vscode-mood "light") "Light" "Dark"))))

(defn decision
  "The full FIREWORKS_THEME decision as data, so the TS side can log the reasoning to the output channel:
   {:value <string/nil>   ; the FIREWORKS_THEME value to force, nil = leave unset (don't prefix)
    :input <string/nil>   ; the current FIREWORKS_THEME value seen (echoed back)
    :reason <string>}     ; a human sentence explaining the branch taken
   `fireworks-theme` is the current FIREWORKS_THEME env value (string or nil); `vscode-mood` is \"light\"/\"dark\"."
  [fireworks-theme vscode-mood]
  (let [v (some-> fireworks-theme str/trim not-empty)]
    (cond
      (nil? v)
      {:value vscode-mood :input nil
       :reason (str "FIREWORKS_THEME unset → force editor mood " (pr-str vscode-mood))}

      (contains? stock-theme-variants v)
      (let [variant (get stock-theme-variants v)]
        (cond
          (nil? variant)
          {:value nil :input v
           :reason (str "stock mood-agnostic theme " (pr-str v) " → leave FIREWORKS_THEME as-is")}

          (= (name variant) vscode-mood)
          {:value nil :input v
           :reason (str "stock " (name variant) " theme " (pr-str v)
                        " matches editor mood → leave FIREWORKS_THEME as-is")}

          :else
          (let [sib (sibling-variant v vscode-mood)]
            {:value sib :input v
             :reason (str "stock " (name variant) " theme " (pr-str v)
                          " disagrees with editor mood " (pr-str vscode-mood)
                          " → force sibling " (pr-str sib))})))

      (= v "neutral")
      {:value "neutral" :input v
       :reason "FIREWORKS_THEME \"neutral\" → force FIREWORKS_THEME=neutral"}

      (contains? #{"light" "dark"} v)
      (if (= v vscode-mood)
        {:value nil :input v
         :reason (str "FIREWORKS_THEME " (pr-str v) " matches editor mood → leave FIREWORKS_THEME as-is")}
        {:value vscode-mood :input v
         :reason (str "FIREWORKS_THEME " (pr-str v) " disagrees with editor mood → force "
                      (pr-str vscode-mood))})

      :else
      {:value nil :input v
       :reason (str "FIREWORKS_THEME " (pr-str v)
                    " is a custom/unrecognized value → leave FIREWORKS_THEME as-is")})))

(defn fireworks-theme
  "The FIREWORKS_THEME value to force, or nil to leave it unset. See `decision`."
  [fireworks-theme-val vscode-mood]
  (:value (decision fireworks-theme-val vscode-mood)))
