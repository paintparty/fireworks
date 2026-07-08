(ns fireworks.macros
 (:require  
  [fireworks.messaging :as messaging]
  [fireworks.specs.config :as config]
  [fireworks.specs.theme :as theme]
  [fireworks.basethemes :as basethemes]
  [fireworks.defs :as defs]
  [fireworks.fs]
  [clojure.pprint :refer [pprint]]
  [clojure.string :as str]
  [clojure.edn :as edn]
  [clojure.spec.alpha :as s]
  ))

(def debug-config? false)

;; START: For resolving theme from COLOR_THEME env var  ------------------------

;; (? (resolve-bling-theme "dark:Atom One Dark, light:Atom One Light"))
(defn ^:public when->
  "If `(= (pred x) true)`, returns x, otherwise nil.
   Useful in a `clojure.core/some->` threading form."
  [x pred]
  (when (pred x) x))

(defn ^:public when->>
  "If (= (pred x) true), returns x, otherwise nil.
   Useful in a `clojure.core/some->>` threading form."
  [pred x]
  (when (pred x) x))

(defn ^:public contains?->
  [x set]
  (when (contains? set x) x))

(defn ^:public contains?->>
  [set x]
  (when (contains? set x) x))

(def valid-synced-theme-re
  #"^(light|dark):\s*([^,]+?)\s*,\s*(light|dark):\s*([^,\s](?:[^,]*[^,\s])?)$")

(defn valid-synced-theme [s]
  (some->> s (when->> string?) (re-find valid-synced-theme-re)))

(defn synced-themes [s]
  (when-let [[_ k1 v1 k2 v2] (valid-synced-theme s)]
    ;; Check to make sure no duplicate keys (e.g., light: A, light: B)
    (when (not= k1 k2)
      {(keyword k1) v1, 
       (keyword k2) v2})))

(defn valid-synced-themes? [m]
  (when (map? m)
          (or (and (-> m :dark (str/ends-with? " Dark"))
                   (-> m :light (str/ends-with? " Light")))
              (and (-> m :dark (str/ends-with? " Neutral"))
                   (-> m :light (str/ends-with? " Neutral"))))))

(def valid-theme-re 
  #"^[a-zA-Z][^\n\t\r]* (?:light|Light|dark|Dark|neutral|Neutral)$")

;; (def valid-theme-re2 #"^([a-z0-9]+(?:-[a-z0-9]+)*-)?(light|dark|neutral)$")

(defn valid-theme
  "All valid:
   \"Zenburn Dark\", \"zenburn dark\"
   \"Zenburn Light\", \"Zenburn light\"
   \"Zenburn Neutral\", \"Zenburn neutral\"
   \"Zenburn3 Dark\",

   Not valid:
   \"Zenburn\"        ;<- ambigous
   \"Zenburn Darkk\"  ;<- typo in suffix
   \"Zenburn Dark \"  ;<- trailing whitespace
   \" Zenburn Dark \" ;<- leading whitespace"
  [s]
  (some->> s
           (when->> string?)
           (re-find valid-theme-re )))


;; TODO
;; add some kind of warnings entries to profile:
;; {:host-mood nil
;;  :host-mood/report ["An `host-mood` value was not supplied to theme-profile"]
;;  :theme nil
;;  :theme/report ["The `s` (theme name) arge supplied to theme-profile was <s>. This is not valid"]}

;; Also:
;; Mention something about host-mood and literal theme value mismatch
;; Resolve host-mood vs supplied theme of "light" or "dark" conflict


(defn- theme-profile
  "This expects a string (s) which is the value of the COLOR_THEME env var.

   Optional second argument (host-mood) is a string, one of \"dark\" or 
   \"light\", which is the result of a successfully detected \"host\" appearance.
   The \"host\" environment could be an active application (such as an IDE), or
   the user's OS.

   The `:theme` result represents a valid name of a syntax coloring theme
   to be applied to rendered source code or data that is the result of
   source-code evaluation. A valid theme name must pass `valid-theme-re`.

   If there is a detected mood, and s is a valid synced theme syntax, the value
   of `:theme` in the result is theme that the user prefers with the detected
   host-mood.

   (theme-profile \"light:Alabaster Light, dark: Alabaster Dark\" \"light\")
   =>
   {:host-mood       \"light\"
    :theme         \"Alabaster Light\"
    :theme-mood    nil
    :synced-themes {:light \"Alabaster Light\" :dark \"Alabaster Dark\"}}  

   (theme-profile \"light\")
   =>
   {:host-mood       nil
    :theme         \"Alabaster Light\"
    :synced-themes {:light \"Alabaster Light\" :dark \"Alabaster Dark\"}}  
   "
  ([s]
   (theme-profile s nil))
  ([s host-mood]
   (theme-profile s host-mood nil))
  ([s host-mood report?]
   (let [host-mood
         (contains?->> #{"light" "dark"} host-mood)

         s       
         (when-> s string?)

         m       
         (cond 
           ;; Theme name provided is not a string, but host-mood detected 
           (and (not s) host-mood)
           {:host-mood       prefers 
            :theme         nil 
            :theme-mood    nil
            :synced-themes nil}

           ;; Theme name provided is a string, and host-mood detected 
           (and s host-mood)
           (let [{:keys [synced-theme-light synced-theme-dark] 
                  :as   synced-themes}
                 (some-> s
                         (when-> #(str/index-of % ","))
                         synced-themes
                         (when-> valid-synced-themes?))

                 theme                 
                 (if (and synced-themes host-mood)
                   (if (= host-mood "light") synced-theme-light synced-theme-dark)
                   (valid-theme s))]
             {:host-mood       host-mood 
              :theme         theme 
              :theme-mood    nil
              :synced-themes synced-themes})

           ;; Theme name is supplied, No host-mood.
           ;; If theme name is valid, and not a synced them, use it.
           :else
           {:host-mood       nil 
            :theme         (when-not (valid-synced-theme s)
                            (valid-theme s)) 
            :theme-mood    nil
            :synced-themes nil})]
     ;; If COLOR_THEME_ENV_VAR is set to "neutral" "light" "dark", :theme-mood
     ;; gets set. 
     ;; TODO - incorporate NO_COLOR + (FORCE_COLOR)
     (cond-> m
       (and (not (:theme m))
            (contains? #{"neutral" "light" "dark"} s))
       (assoc :theme-mood s)))))

;; END: For resolving theme profile from COLOR_THEME env var 
;; -----------------------------------------------------------------------------



(defmacro let-map
  "Equivalent of
   (let [a 5
         b (+ a 5)]
     {:a a :b b})"
  [kvs]
  (let [keys (keys (apply hash-map kvs))
        keyword-symbols (mapcat #(vector (keyword (str %)) %) keys)]
    `(let [~@kvs]
       (hash-map ~@keyword-symbols))))


(let [transforms {:keys keyword
                  :strs str
                  :syms identity}]
  (defmacro keyed
    "Create a map in which, for each symbol S in vars, (keyword S) is a
     key mapping to the value of S in the current scope. If passed an optional
     :strs or :syms first argument, use strings or symbols as the keys."
    ([vars] `(keyed :keys ~vars))
    ([key-type vars]
     (let [transform (comp (partial list `quote)
                           (transforms key-type))]
       (into {} (map (juxt transform identity) vars))))))


(def ^:private load-failure-body
  (str "Please check:"
       "\n"
       "\n"
       "- The file path"
       "\n"
       "- The name of the file"
       "\n"
       "- Does the file exist?"
       "\n"
       "\n"
       "The default Fireworks configuration options will be applied."
       "\n"
       "The default Theme of \"Alabaster Light\" will be used."))


(defn- load-edn-exception-opts
  [{:keys [file key source]}]
  {:k      key
   :v      (str "\"" source "\"")
   :header (str file "\n\nCould not open file:")
   :body   load-failure-body})


(defn- load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [{:keys [key file source] :as opts}]
  (use 'clojure.java.io)
  (if-not (fireworks.fs/file-exists? source)
    (when debug-config?
      (println (str "[!WARNING][fireworks.macros/load-edn]\n"
                    "User-specified config file not found:\n"
                    "\"" source "\"")))
    (try
      (with-open [r (clojure.java.io/reader source)]
        (edn/read (java.io.PushbackReader. r)))
      (catch java.io.IOException err
        (let [opts (merge 
                    (load-edn-exception-opts opts)
                    {:label  "java.io.IOException (CAUGHT)"
                     :header (str "Caused by bad value in "
                                  defs/italic-tag-open file defs/sgr-tag-close
                                  "\n\nCould not open file:")
                     :regex  #"^fireworks\.|^lasertag\."})]
          (swap! messaging/warnings-and-errors
                 conj
                 [:messaging/read-file-warning opts])
          (messaging/caught-exception err opts)))

      (catch RuntimeException err
        (let [opts (merge 
                    (load-edn-exception-opts opts)
                    {:label  "RuntimeException (CAUGHT)"
                     :header (str "Caused by bad value in "
                                  defs/italic-tag-open file defs/sgr-tag-close
                                  "\n\nCould not parse file:")
                     :regex  #"^fireworks\.|^lasertag\."})]
          (swap! messaging/warnings-and-errors
                 conj
                 [:messaging/read-file-warning opts])
          (messaging/caught-exception err opts))))))


(defmacro compile-time-warnings-and-errors []
  (let [ret @messaging/warnings-and-errors]
    `~ret))


(defn- env-var-color-non-empty? [s]
  (boolean (when-let [v (System/getenv s)]
             (not (str/blank? v)))))


(def bling-mood-names-set #{"light" "dark" "medium"})

(defn- bling-mood-from-env-var []
  (or (System/getenv "BLING_MOOD")
      (System/getProperty "bling.mood")))

(defn- syntax-theme-from-env-var []
  (or (System/getenv "FIREWORKS_THEME")
      (System/getProperty "fireworks.theme")))

(defmacro get-user-color-env-vars [] 
  {:env-var/no-color?    (env-var-color-non-empty? "NO_COLOR")
   :env-var/force-color? (env-var-color-non-empty? "FORCE_COLOR")
   :env-var/bling-mood?  (let [s (bling-mood-from-env-var)]
                           (if (contains? bling-mood-names-set s)
                             s
                             "medium"))
   :env-var/debug-detect-color? (env-var-color-non-empty?
                                 "BLING_DEBUG_DETECT_COLOR")})

(defn- config-with-default-theme [m]
  (let [theme (or (:theme theme-profile)            ; <- COLOR_THEME is valid theme name
                  (case (:theme-mood theme-profile) ; <- COLOR_THEME is "light" "dark" or "neutral"
                    "light"
                    "Alabaster Light"
                    "dark"
                    "Alabaster Dark"
                    "neutral"
                    "Universal Neutral"
                    nil)
                  "Universal Neutral"               ; <- Fallback 
                  )]
    {:theme theme}))

(defn- set-default-theme [bling-mood]
  (let [theme (case bling-mood
                    "light"
                    "Alabaster Light"
                    "dark"
                    "Alabaster Dark"
                    "Universal Neutral")
            config {:theme theme}]
        (when debug-config?
          (messaging/fw-debug-report-template
           (str "Debugging config :: fireworks.macros/get-user-config"
                "\n\n"
                "BLING_CONFIG is not set."
                "\n\n"
                "BLING_MOOD env var is set to: " 
                (or (when (contains? #{"light" "dark"} bling-mood)
                      (str "\"" bling-mood "\""))
                    (str "\"" bling-mood "\""))
                "\n\n"
                "Returning a config of:" )
           config))
        config))


(defmacro get-user-configs
  "This gets the path to user config from sys env var, then returns a map of
   user config with resolved :theme entry.
   
   First, the path set by the user via \"FIREWORKS_CONFIG\" env var is
   validated. If it is a non-blank string that does not point to .edn file,
   issue a bad-option-value-warning. Also update messaging/warnings-and-errors
   atom, which will surface the warning if the user is in cljs land,
   (issues warning in browser console).

   If the path set by the user via \"FIREWORKS_CONFIG\" env var points to a
   non-existant `.edn` file, or a file that is not parseable by
   `clojure.edn/read`, a warning is issued via fireworks.macros/load-edn.
   Also update the `messaging/warnings-and-errors` atom, which will surface the
   warning if the user is in cljs land, (issues warning in browser console).
   
   If the config map is successfully loaded from edn file, and the :theme entry
   is a valid `.edn` path, but this path points to a non-existant `.edn` file,
   or a file that is not parseable by `clojure.edn/read`, a warning is issued
   via `fireworks.macros/load-edn`.

   If a valid `:theme` map is resolved, it will be assoc'd to the user's config
   map, and returned. If a \"COLOR_THEME\" env var is set Otherwise, just the
   config map is returned."
  []
  (use 'clojure.java.io)
  (reset! messaging/warnings-and-errors [])

  (let [bling-config     (System/getenv "BLING_CONFIG")
        fireworks-config (System/getenv "FIREWORKS_CONFIG")
        syntax-theme     (syntax-theme-from-env-var)
        theme-profile    (some-> syntax-theme theme-profile)]

    ;; (? 'FIREWORKS_THEME syntax-theme)
    ;; (? theme-profile)
    
    (if-let [path-to-user-config (or bling-config fireworks-config)]
      (let [form-meta   (meta &form)
            valid-path? (s/valid?
                         ::config/edn-file-path 
                         path-to-user-config)]

        (if-not valid-path?  
          (let [opts {:v      path-to-user-config
                      :k      (if bling-config 
                                "BLING_CONFIG="
                                "FIREWORKS_CONFIG=")
                      :spec   ::config/edn-file-path
                      :header (str "[fireworks.core/_p] Invalid value"
                                   " for environmental variable:")
                      :body   (str "The default "
                                   (if bling-config "config" "fireworks")
                                   " config values will be applied.")}]

            (messaging/bad-option-value-warning opts)

            (swap! messaging/warnings-and-errors
                   conj
                   [:messaging/bad-option-value-warning opts]))

          (if-let [config (load-edn {:source path-to-user-config})]
            (let [config (assoc config :path-to-user-config path-to-user-config)]
              (if-let [theme* (or (:theme theme-profile)            ; <- FIREWORKS_THEME is valid theme name
                                  (case (:theme-mood theme-profile)  ; <- FIREWORKS_THEME is "light" "dark" or "neutral"
                                    "light"
                                    "Alabaster Light"
                                    "dark"
                                    "Alabaster Dark"
                                    "neutral"
                                    "Universal Neutral"
                                    nil)
                                  (:theme config)                  ; <- FIREWORKS_THEME User has set `:theme` in global config
                                  )]
                (if-let [user-theme*
                         (when-let [x 
                                    (cond
                                      ;; Supplied theme is file path to a theme
                                      (s/valid? ::config/edn-file-path theme*)
                                      (load-edn {:source theme*
                                                 :file   path-to-user-config
                                                 :key    :theme})

                                      ;; TODO - maybe eliminate this posibility ?
                                      ;; theme is a map defined in user global config 
                                      (map? theme*)
                                      theme*

                                      ;; Theme is a name, maps to a stock theme
                                      (string? theme*)
                                      (get basethemes/stock-themes theme* nil))]

                           ;; To debug theme problems
                           ;;  (? theme* (->> (s/explain-data ::theme/theme x)
                           ;;                 :clojure.spec.alpha/problems
                           ;;                 (take 2)))
                           
                           (when (s/valid? ::theme/theme x)
                             x))]

                  ;; :theme entry resolves to a valid map, so assoc it to user config
                  (let [config (assoc config :theme user-theme*)]
                    `~config)

                  ;; Else :theme entry exists, but doesn't resolve to a map
                  ;; dissoc :theme entry and issue warning for user
                  (let [config (assoc config 
                                      :theme
                                      (get basethemes/stock-themes
                                           "Universal Neutral"
                                           nil))
                        opts   {:v      theme*
                                :k      ":theme"
                                :spec   ::config/theme
                                :header (str "[fireworks.core/_p] Invalid value "
                                             "for :theme entry.")
                                :body   (str "The default theme "
                                             defs/italic-tag-open
                                             "\"Universal Neutral\" "
                                             defs/sgr-tag-close
                                             "will be used instead.")}]

                    #_(println :invalid "::" 'get-user-configs)
                    (messaging/bad-option-value-warning opts)

                    (swap! messaging/warnings-and-errors
                           conj
                           [:messaging/bad-option-value-warning opts])                 

                    `~config))

                ;; Else
                ;; - COLOR_THEME is not valid theme or "theme-mood"
                ;; - :theme entry is nil or not supplied,
                ;; So just return user config
                `~config))
            ;; Else no valid user config.edn at path set in FIREWORKS_CONFIG or BLING_CONFIG,
            ;; just return a config map with theme based on `COLOR_MOOD` env var, or fallback
            (config-with-default-theme theme-profile))))


      ;; Else no user config.edn path set at all in FIREWORKS_CONFIG or BLING_CONFIG.
      ;; Just return a config map with theme based on `COLOR_MOOD` env var, or fallback
      (config-with-default-theme theme-profile))))


;; TODO remove
#_(defn get-user-config-edn-dynamic
  "This gets the path to user config from sys env var, then returns a map of
   user config with resolved :theme entry.
   
   First, the path set by the user via \"FIREWORKS_CONFIG\" env var is
   validated. If it is a non-blank string that does not point to .edn file,
   issue a bad-option-value-warning. Also update messaging/warnings-and-errors
   atom, which will surface the warning if the user is in cljs land, and maybe
   not looking at the build process in their terminal.

   If the path set by the user via \"FIREWORKS_CONFIG\" env var points to a
   non-existant `.edn` file, or a file that is not parseable by
   `clojure.edn/read`, a warning is issued via fireworks.macros/load-edn.
   Also update the `messaging/warnings-and-errors` atom, which will surface the
   warning if the user is in cljs land, and maybe not looking at the build
   process in their terminal.
   
   If the config map is successfully loaded from edn file, and the :theme entry
   is a valid `.edn` path, but this path points to a non-existant `.edn` file,
   or a file that is not parseable by `clojure.edn/read`, a warning is issued
   via fireworks.macros/load-edn.

   If a valid :theme map is resolved, it will be assoc'd to the user's config
   map, and returned. Otherwised, just the config map is returned. 
   "
  []
  (use 'clojure.java.io)
  (reset! messaging/warnings-and-errors [])
  (if-let [path-to-user-config (System/getenv "FIREWORKS_CONFIG")]
    (let [valid-path? (s/valid?
                       ::config/edn-file-path 
                       path-to-user-config)]

      (if-not valid-path?  
        (let [opts {:v      path-to-user-config
                    :k      "FIREWORKS_CONFIG="
                    :spec   ::config/edn-file-path
                    :header (str "[fireworks.core/_p] Invalid value"
                                 " for environmental variable.")}]

          (messaging/bad-option-value-warning opts)

          (swap! messaging/warnings-and-errors
                 conj
                 [:messaging/bad-option-value-warning opts]))

        (when-let [config (load-edn {:source path-to-user-config})]
          (let [config (assoc config :path-to-user-config path-to-user-config)]
            (if-let [theme* (:theme config)]
              (if-let [user-theme*
                       (when-let [x 
                                  (cond
                                    (s/valid? ::config/edn-file-path theme*)
                                    (load-edn {:source theme*
                                               :file   path-to-user-config
                                               :key    :theme})

                                    (map? theme*)
                                    theme*

                                    (string? theme*)
                                    (get basethemes/stock-themes theme* nil))]
                         (when (s/valid? ::theme/theme x)
                           x))]

                ;; :theme entry resolves to a map, so assoc it to user config
                (let [config (assoc config :theme user-theme*)]
                  config)

                ;; :theme entry exists, but doesn't resolve to a map
                ;; dissoc :theme entry and issue warning for user
                (let [config (assoc config 
                                    :theme
                                    (get basethemes/stock-themes
                                         "Universal Neutral"
                                         nil))
                      opts   {:v      theme*
                              :k      ":theme"
                              :spec   ::config/theme
                              :header (str "[fireworks.core/_p] Invalid value "
                                           "for :theme entry.")
                              :body   (str "The default theme "
                                           defs/italic-tag-open
                                           "\"Universal Neutral\" "
                                           defs/sgr-tag-close
                                           "will be used instead.")}]

                  #_(println :invalid "::" 'get-user-config-edn-dynamic)
                  (messaging/bad-option-value-warning opts)

                  (swap! messaging/warnings-and-errors
                         conj
                         [:messaging/bad-option-value-warning opts])                 

                  config))

              ;; :theme entry is nil or non-existant, just return user config
              config)))))

    ;; If (System/getenv "FIREWORKS_CONFIG") resolves to nil, we set theme to
    ;; default \"Universal Neutral\"" stock theme.
    {:theme "Universal Neutral"}))


(def supports-color-level-1-term-re
 #"(?i)^screen|^xterm|^vt100|^vt220|^rxvt|color|ansi|cygwin|linux")


;; Logic culled from https://github.com/chalk/supports-color
(defn- detect-color-level [dbgf]
  (let [term         (System/getenv "TERM")
        term-program (System/getenv "TERM_PROGRAM")
        colorterm    (System/getenv "COLORTERM")
        ci           (System/getenv "CI")]
    (cond 
      ci
      (cond (contains? #{"GITHUB_ACTIONS" "GITEA_ACTIONS" "CIRCLECI"} ci)
            (do
              (dbgf "CI=\"" ci "\", setting to 3")
              3)
            ;; :else branch covers 'TRAVIS', 'APPVEYOR', 'GITLAB_CI',
            ;; 'BUILDKITE', 'DRONE', and (= (System/getenv "CI_NAME"))
            :else
            (do (dbgf "CI=\"" ci "\", setting to 1")
                1))

      (= "truecolor" colorterm)
      (do (dbgf "COLORTERM=\"truecolor\", setting to 3")
          3)

      (contains? #{"xterm-ghostty" "xterm-kitty" "wezterm"} term)
      (do (dbgf (str "TERM=" term ", setting to 3"))
          3)

      (not (str/blank? term-program))
      (cond 
        (= term-program "iTerm.app")
        (if-let [v (some-> (System/getenv "TERM_PROGRAM_VERSION")
                           str
                           (str/split #"\.")
                           first
                           Integer/parseInt)]
          (let [ret (if (>= v 3) 3 2)]
            (do (dbgf "iTerm.app, version " v  )
                ret))
          2)

        (= term-program "Apple_Terminal")
        (do (dbgf "Apple_Terminal, setting to 2")
            2))

      (when (string? term) (re-find #"-256(color)?$" term))
      (do (dbgf (str "TERM="
                     (re-find #"-256(color)?$" term)
                     ", setting to 1"))
          2)

      (when (string? term) (re-find supports-color-level-1-term-re term))
      (do (dbgf (str "TERM="
                     (re-find supports-color-level-1-term-re term)
                     ", setting to 1"))
          1)

      colorterm
      (do (dbgf (str "(System/getenv \"COLORTERM\") => "
                     colorterm 
                     ", setting to 1"))
          1)

      :else
      (do (dbgf (println "color level could not be detected, falling back to 2"))
          2))))


(defmacro get-detected-color-level []
  (let [{:keys [:env-var/no-color? 
                :env-var/force-color?
                :env-var/debug-detect-color?]} 
        (get-user-color-env-vars)

        dbgf                                                                                   
        (if debug-detect-color?
          #(println (str "\n"
                         "BLING_DEBUG_DETECT_COLOR=\"true\"\n" 
                         "Debugging "
                         "\033[3m"
                         "fireworks.macros/detect-color-level ...\n"
                         "\033[0;m"
                         % 
                         "\n"))
          identity)]
    (try (cond force-color?
               (detect-color-level dbgf)
               no-color?
               :NO_COLOR
               :else
               (detect-color-level dbgf))
         (catch Throwable e
           (when debug-detect-color?
             (println e)
             (dbgf "Error (caught), falling back to 2"))
           2))))
