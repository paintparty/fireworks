(ns fireworks.macros
 (:require  
  [fireworks.messaging :as messaging]
  [fireworks.specs.config :as config]
  [fireworks.specs.theme :as theme]
  [fireworks.basethemes :as basethemes]
  [fireworks.defs :as defs]
  [clojure.pprint :refer [pprint]]
  [clojure.string :as string]
  [clojure.edn :as edn]
  [clojure.spec.alpha :as s]))

(defn- regex? [v]
  (-> v type str (= "class java.util.regex.Pattern")))

(defn- surround-with-quotes [x]
  (str "\"" x "\""))

(defn shortened
  "Stringifies a collection and truncates the result with ellipsis 
   so that it fits on one line."
  [v limit]
  (let [as-str         (str v)
        regex?         (regex? v)
        double-quotes? (or (string? v) regex?)
        regex-pound    (when regex? "#")]
    (if (> limit (count as-str))
      (if double-quotes?
        (str regex-pound (surround-with-quotes as-str))
        as-str)
      (let [ret* (-> as-str
                     (string/split #"\n")
                     first)
            ret  (if (< limit (count ret*))
                   (let [ret (->> ret*
                                  (take limit)
                                  string/join)]
                     (str (if double-quotes?
                            (str regex-pound (surround-with-quotes ret))
                            ret)
                          (when-not double-quotes? " ")
                          "..."))
                   ret*)]
        ret))))

  (defn- ns+ln+col-str
    [form-meta]
    (let [{:keys [line column]} form-meta
          ns-str                (some-> *ns*
                                        ns-name
                                        str
                                        (str ":" line ":" column))
          ns-str                (do 
                                  (str "\033[3;38;5;247m" ns-str "\033[0;m")

                                  ;; magenta bold
                                  (str "\033[3;38;5;201;1m" ns-str "\033[0m")

                                  ;; olive bold
                                  (str "\033[3;38;5;69;m" ns-str "\033[0m")
                                  
                                  )]
      ns-str))

(defmacro ? 
    ([x]
     (let [ns-str (ns+ln+col-str (meta &form))]
       `(do
          (println
           (str ~ns-str
                "\n"
                (shortened (quote ~x) 25)
                "\n"
                (with-out-str (pprint ~x))))
          ~x)))
    ([label x]
     (let [label  (or (:label label) label)
           ns-str (ns+ln+col-str (meta &form))]
      ;;  (println "FOOO" ns-str)
       `(do
          (println
           (if (= :- ~label)
             (with-out-str (pprint ~x))
             #_(string/replace (with-out-str (pprint ~x)) #"\n$" "")
             (str ~ns-str
                  "\n"
                  ~label
                  "\n"
                  (with-out-str (pprint ~x)))))
          ~x))))

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
  (try
    (with-open [r (clojure.java.io/reader source)]
      (edn/read (java.io.PushbackReader. r)))
    (catch java.io.IOException err
      (let [opts (merge 
                  (load-edn-exception-opts opts)
                  {:label  "java.io.IOException (CAUGHT)"
                   :header (str "Caused by bad value in "
                                defs/italic-tag-open file defs/sgr-tag-close
                                "\n\nCould not open file:")})]
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
                                "\n\nCould not parse file:")})]
        (swap! messaging/warnings-and-errors
               conj
               [:messaging/read-file-warning opts])
        (messaging/caught-exception err opts)))))


(defmacro compile-time-warnings-and-errors []
  (let [ret @messaging/warnings-and-errors]
    `~ret))

(defn- env-var-color-non-empty? [s]
  (boolean (when-let [v (System/getenv s)]
             (not (string/blank? v)))))

(def bling-mood-names-set #{"light" "dark" "medium"})

(defmacro get-user-color-env-vars [] 
  {:env-var/no-color?    (env-var-color-non-empty? "NO_COLOR")
   :env-var/force-color? (env-var-color-non-empty? "FORCE_COLOR")
   :env-var/bling-mood?  (let [s (System/getenv "BLING_MOOD")]
                           (if (contains? bling-mood-names-set s)
                             s
                             "medium"))})

(defmacro get-user-configs
  "This gets the path to user config from sys env var, then returns a map of
   user config with resolved :theme entry.
   
   First, the path set by the user via \"FIREWORKS_CONFIG\" env var is
   validated. If it is a non-blank string that does not point to .edn file,
   issue a bad-option-value-warning. Also update messaging/warning-and-errors
   atom, which will surface the warning if the user is in cljs land, and maybe
   not looking at the build process in their terminal.

   If the path set by the user via \"FIREWORKS_CONFIG\" env var points to a
   non-existant `.edn` file, or a file that is not parseable by
   `clojure.edn/read`, a warning is issued via fireworks.macros/load-edn.
   Also update the `messaging/warning-and-errors` atom, which will surface the
   warning if the user is in cljs land, and maybe not looking at the build
   process in their terminal.
   
   If the config map is successfully loaded from edn file, and the :theme entry
   is a valid `.edn` path, but this path points to a non-existant `.edn` file,
   or a file that is not parseable by `clojure.edn/read`, a warning is issued
   via `fireworks.macros/load-edn`.

   If a valid `:theme` map is resolved, it will be assoc'd to the user's config
   map, and returned. Otherwise, just the config map is returned. 
   "
  []
  (use 'clojure.java.io)
  (reset! messaging/warnings-and-errors [])
  (let [bling-config (System/getenv "BLING_CONFIG")]
    (if-let [path-to-user-config (or bling-config 
                                     (System/getenv "FIREWORKS_CONFIG"))]
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

                           ;; To debug theme problems
                           #_(? theme* (->> (s/explain-data ::theme/theme x)
                                            :clojure.spec.alpha/problems
                                            (take 2)))

                           (when (s/valid? ::theme/theme x)
                             x))]

                  ;; :theme entry resolves to a map, so assoc it to user config
                  (let [config (assoc config :theme user-theme*)]
                    `~config)

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

                    #_(println :invalid "::" 'get-user-configs)
                    (messaging/bad-option-value-warning opts)

                    (swap! messaging/warnings-and-errors
                           conj
                           [:messaging/bad-option-value-warning opts])                 

                    `~config))

                ;; :theme entry is nil or non-existant, just return user config map
                `~config)))))

    ;; If (System/getenv "FIREWORKS_CONFIG") resolves to nil, or
    ;; (System/getenv "BLING_CONFIG") resolves to nil, we set theme to
    ;; default \"Universal Neutral\"" stock theme.
      {:theme "Universal Neutral"})))


(defn get-user-config-edn-dynamic
  "This gets the path to user config from sys env var, then returns a map of
   user config with resolved :theme entry.
   
   First, the path set by the user via \"FIREWORKS_CONFIG\" env var is
   validated. If it is a non-blank string that does not point to .edn file,
   issue a bad-option-value-warning. Also update messaging/warning-and-errors
   atom, which will surface the warning if the user is in cljs land, and maybe
   not looking at the build process in their terminal.

   If the path set by the user via \"FIREWORKS_CONFIG\" env var points to a
   non-existant `.edn` file, or a file that is not parseable by
   `clojure.edn/read`, a warning is issued via fireworks.macros/load-edn.
   Also update the `messaging/warning-and-errors` atom, which will surface the
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
                      opts {:v      theme*
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

              ;; :theme entry is nil or non-existant, just return user config map
              config)))))

    ;; If (System/getenv "FIREWORKS_CONFIG") resolves to nil, we set theme to
    ;; default \"Universal Neutral\"" stock theme.
    {:theme "Universal Neutral"}))
