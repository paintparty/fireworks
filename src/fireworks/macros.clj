(ns fireworks.macros
 (:require  
  [fireworks.messaging :as messaging]
  [fireworks.specs.config :as config]
  [fireworks.specs.theme :as theme]
  [fireworks.basethemes :as basethemes]
  [bling.core :refer [bling]]
  [clojure.edn :as edn]
  [clojure.spec.alpha :as s]))

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
    (catch java.io.IOException e
      (let [opts (merge 
                  (load-edn-exception-opts opts)
                  {:label  "java.io.IOException (CAUGHT)"
                   :header (bling "Caused by bad value in "
                                     [:italic file]
                                     "\n\nCould not open file:")})]
        (swap! messaging/warnings-and-errors
               conj
               [:messaging/read-file-warning opts])
        (messaging/caught-exception opts)))

    (catch RuntimeException e
      (let [opts (merge 
                  (load-edn-exception-opts opts)
                  {:label  "RuntimeException (CAUGHT)"
                   :header (bling "Caused by bad value in "
                                     [:italic file]
                                     "\n\nCould not parse file:")})]
        (swap! messaging/warnings-and-errors
               conj
               [:messaging/read-file-warning opts])
        (messaging/caught-exception opts)))))


(defmacro compile-time-warnings-and-errors []
  (let [ret @messaging/warnings-and-errors]
    `~ret))

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
   via fireworks.macros/load-edn.

   If a valid :theme map is resolved, it will be assoc'd to the user's config
   map, and returned. Otherwised, just the config map is returned. 
   "
  []
  (use 'clojure.java.io)
  (reset! messaging/warnings-and-errors [])
  (if-let [path-to-user-config (System/getenv "FIREWORKS_CONFIG")]
    (let [form-meta   (meta &form)
          valid-path? (s/valid?
                       ::config/edn-file-path 
                       path-to-user-config)]

      (if-not valid-path?  
        (let [opts {:v      path-to-user-config
                    :k      "FIREWORKS_CONFIG="
                    :spec   ::config/edn-file-path
                    :header (str "[fireworks.core/_p] Invalid value"
                                 " for environmental variable:")
                    :body   "The default fireworks config values will be applied."}]

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
                  `~config)

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
                            :body   (bling "The default theme "
                                           [:italic "\"Universal Neutral\" "]
                                           "will be used instead.")}]

                  (messaging/bad-option-value-warning opts)

                  (swap! messaging/warnings-and-errors
                         conj
                         [:messaging/bad-option-value-warning opts])                 

                  `~config))

              ;; :theme entry is nil or non-existant, just return user config map
              `~config)))))

    ;; If (System/getenv "FIREWORKS_CONFIG") resolves to nil, we set theme to
    ;; default \"Universal Neutral\"" stock theme.
    {:theme "Universal Neutral"}))

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
                            :body   (bling "The default theme "
                                           [:italic "\"Universal Neutral\" "]
                                           "will be used instead.")}]

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
