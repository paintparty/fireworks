;; TODO - what is difference between @config and user-config-edn

(ns ^:dev/always fireworks.state
  (:require #?(:cljs [fireworks.macros :refer-macros [get-user-configs get-user-color-env-vars keyed]]
               :clj  [fireworks.macros :refer        [get-user-configs get-user-color-env-vars keyed]])
            [clojure.pprint :refer [pprint]]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [fireworks.basethemes :as basethemes]
            [fireworks.color :as color]
            [fireworks.config :as config]
            [fireworks.defs :as defs]
            [fireworks.messaging :as messaging]
            [fireworks.pp :refer [?pp]]
            [fireworks.specs.config :as config.specs]
            [fireworks.specs.theme :as theme]
            [fireworks.specs.tokens :as tokens]
            [fireworks.themes :as themes]
            [fireworks.util :as util]))

#?(:cljs
   ;; In cljs, detect if env is node/deno vs browser
   (defonce node?
     (boolean 
      (some->> 
       (or (when (and (exists? js/window)
                      (exists? js/window.document))
             :browser)

           (when (and (exists? js/process)
                      (not (nil? (some-> js/process .-versions)))
                      (not (nil? (some-> js/process .-versions .-node))))
             :node)

           (when (and (identical? "object" (js/goog.typeOf js/self))
                      (.-constructor js/self)
                      (= (some-> js/self .-constructor .-name)
                         "DedicatedWorkerGlobalScope"))
             :web-worker)

           (when (or (exists? js/window)
                     (and (exists? js/navigator)
                          (when-let [nav (.-userAgent js/navigator)]
                            (and (identical? "object" (js/goog.typeOf nav))
                                 (or (.includes nav "Node.js")
                                     (.includes nav "jsdom"))))))
             :js-dom)

           (when (and (exists? js/Deno)
                      (exists? (.-version js/Deno))
                      (exists? (some-> js/Deno .-version .-deno)))
             :deno))
       (contains? #{:deno :node})))))


;; Internal state atoms for development debugging ------------------------
(def debug-config? false)
(def debug-theme? #_true false)
(def print-config? false)

;; Temp for debugging tagging 
(def *debug-tagging? (atom false))

(defn debug-tagging? []
  @*debug-tagging?)


;; -----------------------------------------------------------------------------
;; Internal state atoms for printing and formatting
;; -----------------------------------------------------------------------------

;; For ClojureScript, browser dev console %c formatting
(def styles (atom []))

;; For Rainbow Parens
(def rainbow-level (atom 0))

;; For :find highlighting
(def highlight (atom nil))

;; When formatting form-to-be-evaled, this should be set to `true`
(def formatting-form-to-be-evaled? (atom false))

;; When top-level-form is not coll, this should be set to `true`.
;; This will disable truncation.
(def top-level-value-is-sev? (atom false))

;; This will make map brackets transparent, when printing
;; results related to let bindings
(def let-bindings? (atom false))

;; This will indent the whole output - label, file-info, and result
(def margin-inline-start (atom nil))

;; Helpers for creating printing options state ------------------------------

;; The user's config from disc
;; Path is set via env var export FIREWORKS_CONFIG="path/to/config.edn"
;; Recommended location for path is "~/.fireworks/config.edn"


;; -----------------------------------------------------------------------------
;; Config options from user's config.edn
;; -----------------------------------------------------------------------------

(def user-config-edn* 
  (do (when debug-config?
        (messaging/fw-debug-report-template
         "def fireworks.state/user-config-edn* (with :path-to-user-config)"
         (get-user-configs)))
      (when debug-theme?
        (messaging/fw-debug-report-template
         "debugging-theme :: deffing fireworks.state/user-config-edn* (with :path-to-user-config)"
         (str
          (some-> (get-user-configs) :theme :name))))
      (get-user-configs)))

(defn user-config-edn*-dynamic []
  (do (when debug-config?
        (messaging/fw-debug-report-template
         "def fireworks.state/user-config-edn*-dynamic (with :path-to-user-config)"
         (get-user-configs)))
      (when debug-theme?
        (messaging/fw-debug-report-template
         "debugging-theme :: deffing fireworks.state/user-config-edn*-dynamic (with :path-to-user-config)"
         (str
          (some-> (get-user-configs) :theme :name))))
      (get-user-configs)))

;; -----------------------------------------------------------------------------
;; Validated config options from user's config.edn
;; -----------------------------------------------------------------------------

;; TODO - figure out how to get filename, line, and column.
(defn validate-option-from-user-config-edn
  [k v {:keys [spec default]}]
  
  (let [undefined? (or (when (string? v) (string/blank? v))
                       (nil? v))
        valid?     (when-not undefined? (if spec (s/valid? spec v) v))
        invalid?   (and (not undefined?) (not valid?))]
    (if invalid?
      #_(do (println :invalid "::" 'validate-option-from-user-config-edn "\n")
          (?pp [k #_v]))
      (messaging/bad-option-value-warning
       (assoc (keyed [k v default spec])
              :header (str (some-> user-config-edn*
                                   :path-to-user-config
                                   (str "\n\n"))
                           (str "Invalid value for " k " in user config."))))
      (if (or undefined? (not valid?))
        nil
        v))))

(defn validate-option-from-user-config-edn-dynamic
  [k v {:keys [spec default]} user-config-edn*]
  (let [undefined? (or (when (string? v) (string/blank? v))
                       (nil? v))
        valid?     (when-not undefined? (if spec (s/valid? spec v) v))
        invalid?   (and (not undefined?) (not valid?))]
    (if invalid?
      #_(do (println :invalid "::" 'validate-option-from-user-config-edn-dynamic "\n")
          ;; (?pp [k v])
          (println "The spec")
          (pprint spec)
          (println "\n\n")
          ;; (pprint user-config-edn*)
          (pprint (s/explain-data spec v))
          (println "\n\n")
          )
      (messaging/bad-option-value-warning
       (assoc (keyed [k v default spec])
              :header (str (some-> user-config-edn*
                                   :path-to-user-config
                                   (str "\n\n"))
                           (str "Invalid value for " k " in user config."))))
      (if (or undefined? (not valid?))
        nil
        v))))

(defn- config-diffs [m1 m2]
  (into {}
        (keep (fn [[k v]]
                (when (and (not= v (get m1 k nil))
                           (not= :path-to-user-config k))
                  [k v]))
              m2)))

;; Is this not ever used??
#_(def user-config-edn
  (do #_(println "user-config-edn")
      (try 
        (let [ret
              (into {}
                    (map (fn [[k v]]
                           (let [m         (k config/options)

                                 validated (validate-option-from-user-config-edn k v m)]
                             [k validated]))
                         user-config-edn*))]
          (when debug-config?
            (messaging/fw-debug-report-template
             "Invalid options from user-config-edn*"
             (config-diffs user-config-edn* ret)))
          (when debug-config?
            (messaging/fw-debug-report-template
             "def fireworks.state/user-cofig-edn (validated)"
             ret))
          (when debug-theme?
            (messaging/fw-debug-report-template
             "debugging theme :: deffing fireworks.state/user-cofig-edn (validated)"
             (some-> ret :theme :name)))
          ret)

        ;; TODO - check if this actually catches anything?
        (catch #?(:cljs js/Object
                  :clj Exception)
               e
          (messaging/caught-exception e {})
          (swap! messaging/warnings-and-errors
                 conj
                 [:messaging/print-error e])
          {}))))

(defn user-config-edn-dynamic []
  (try 
    (let [user-config-edn*
          (user-config-edn*-dynamic)

          ret
          (into {}
                (map (fn [[k v]]
                       (let [m         
                             (k config/options)

                             validated
                             (validate-option-from-user-config-edn-dynamic
                              k
                              v
                              m
                              user-config-edn*)]
                         [k validated]))
                     user-config-edn*))]
      (when debug-config?
        (messaging/fw-debug-report-template
         "Invalid options from user-config-edn*"
         (config-diffs user-config-edn* ret)))
      (when debug-config?
        (messaging/fw-debug-report-template
         "def fireworks.state/user-cofig-edn (validated)"
         ret))
      (when debug-theme?
            (messaging/fw-debug-report-template
             "debugging theme :: deffing fireworks.state/user-cofig-edn-dynamic (validated)"
             (some-> ret :theme :name)))
      ret)

    ;; TODO - check if this actually catches anything?
    (catch #?(:cljs js/Object
              :clj Exception)
           e
      (messaging/caught-exception e {})
      (swap! messaging/warnings-and-errors
             conj
             [:messaging/print-error e])
      {})))

;; -----------------------------------------------------------------------------
;; Internal state atom for merged config
;; -----------------------------------------------------------------------------

(def ^:private default-config
  (into {} (map (fn [[k v]] [k (:default v)]) config/options)))


(defn- dark? [x]
  (contains? #{:dark "dark"} x))

(defn- user-opt-val [user-config-edn k bling-mood]
  (let [opt (k user-config-edn)
        ;; todo - why the string check?
        opt (when-not (and (string? opt)
                           (string/blank? opt))
              opt)
        ret (if (not (nil? opt))
              opt
              (if (= k :theme)

                ;; This is where theme gets set to default light or dark if 
                ;; User only has BLING_MOOD set and no other fireworks config.edn or bling config.edn
                (case bling-mood
                  "light" "Alabaster Light"
                  "dark"  "Alabaster Dark"
                  "Universal Neutral")

                (k default-config)))

        ]
    #_(println k opt ret)
    ret))

;; THIS IS WHERE COLOR ENV VARS GET MERGED INTO CONFIG
(defn merged-config []
  (let [user-config-edn
        (user-config-edn-dynamic)

        {:keys [:env-var/no-color? :env-var/force-color? :env-var/bling-mood]
         :as   color-env-vars}  
        (get-user-color-env-vars)

        ret
        (into (or color-env-vars {})
              (map (fn [[k _]]
                     [k (user-opt-val user-config-edn k bling-mood)])
                   config/options))

        maybe-rainbow-brackets
        (when (and no-color? (not force-color?))
          {:enable-rainbow-brackets? false})]
    (merge ret maybe-rainbow-brackets)))

(def config
  (let [mc (merged-config)] 
    (when debug-config?
      (messaging/fw-debug-report-template
       "Options from user-config-edn that will override defaults"
       (config-diffs default-config user-config-edn*)))
    (when debug-config?
      (messaging/fw-debug-report-template
       "def fireworks.state/config, initial value of atom"
       mc))
    (when debug-theme?
      (messaging/fw-debug-report-template
       "def fireworks.state/config, initial value of atom"
       (if (some-> mc :theme map?)
         (assoc mc :theme {:name (some-> mc :theme :name) '... '...})
         mc)))
    (atom mc)))


;; -----------------------------------------------------------------------------
;; Theme merging functions
;; -----------------------------------------------------------------------------

(defn- resolved-hex [c k]
  (when-not (and (= k :background-color)
                 (contains? #{:transparent "transparent"} c))
   (if-not (s/valid? ::tokens/css-hex c)
     (when (or (string? c) (keyword? c))
       (get color/named-html-colors (name c) nil))
     c)))


(defn- with-removed-or-resolved-color
  [m k {:keys [theme-token from-custom-badge-style?] :as opts}]
  (let [c               (k m)
        bg-transparent? (and (= k :background-color)
                             (contains? #{:transparent "transparent"} c))
        resolved        (when-not bg-transparent? 
                          (some-> c (resolved-hex k)))]
    (cond
      resolved
      (assoc m k resolved)

      (and c (nil? resolved))
      (do 
        (messaging/bad-option-value-warning
         (merge {:k      k
                 :v      c
                 :spec   ::tokens/color-value
                 :header (str "fireworks.state/with-removed-or-resolved-color\n\n"
                              "Invalid color value for theme token " theme-token "."
                              (when from-custom-badge-style?
                                (str "\n\n"
                                     "This is from a :badge-style map within"
                                     " a user-supplied custom printer.")))
                 :body   (str "The fallback color value for the "
                              theme-token 
                              " theme token will be applied.")}
                opts))        
        (dissoc m k))

      :else
      m)))


(defn- kv-pair-str [k v]
  (str "\033[1;m" k " " "\"" v "\"\033[0;m"))

(defn invalid-color-warning 
  [{:keys [header v k footer theme-token from-custom-badge-style?]}]
  (str header
       "\n\n"
       #?(:cljs (if node?
                  (kv-pair-str k v)
                  (str "%c" k " " "\"" v "\"%c"))
          :clj (kv-pair-str k v))
       
       (when from-custom-badge-style?
         (str "\n\n"
              (str "This is from a :badge-style map within"
                   " a user-supplied custom printer.")))
       "\n\n"
       "This color value should be a hex or named html color."
       "\n\n"
       (str "The fallback color value for the "
            theme-token 
            " theme token will be applied.")
       footer))

(defn with-conformed-colors
  "If :background-color is :transparent or \"transparent\", dissoc the entry.
   If :color or :background-color is named-html-color, convert to hex.
   If value is not valid named-html-color or hex, dissoc the entry."
  [{:keys [style-map 
           theme-token
           from-custom-badge-style?]}]
  (let [opts {:theme-token        theme-token
              :from-custom-badge-style? from-custom-badge-style?}
        ret  (-> style-map
                 (with-removed-or-resolved-color :color opts)
                 (with-removed-or-resolved-color :background-color opts))]
    ret))

(def underline-style-codes-by-style
  {"straight" 1 
   "double"   2 
   "wavy"    3 
   "dotted"   4 
   "dashed"   5})  

(defn- sgr-text-decoration [m]
  (when-not (:disable-text-decoration? m)
    (cond
      (or (contains? #{"underline" :underline} (:text-decoration m))
          (contains? #{"underline" :underline} (:text-decoration-line m)))
      (if-let [n (some->> m
                          :text-decoration-style
                          util/as-str
                          (get underline-style-codes-by-style))] 
        (str "4:" n)
        "4")
      (contains? #{"line-through" :strikethrough}
                 (:text-decoration m))
      "9")))

(defn x->sgr [x k]
  (when x
    (let [n (if (= k :fg) 38 48)]
      (if (int? x)
        (str n ";5;" x)
        (let [[r g b _] x
              ret       (str n ";2;" r ";" g ";" b)]
          ret)))))

(defn m->sgr
  [{fgc*        :color
    bgc*        :background-color
    font-style  :font-style
    font-weight :font-weight
    :as         m}]

  (let [debug? false
        fgc    (x->sgr fgc* :fg)
        bgc    (x->sgr bgc* :bg)
        italic (when (and (:enable-terminal-italics? @config)
                          (contains? #{"italic" :italic} font-style))
                 "3")
        weight (when (and (:enable-terminal-font-weights? @config)
                          (contains? #{"bold" :bold} font-weight))
                 "1")

        text-decoration
        (sgr-text-decoration m)

        ret    (str "\033["
                    (string/join ";"
                                 (remove nil?
                                         [italic
                                          fgc
                                          weight
                                          bgc
                                          text-decoration]))
                    "m")]
    (when debug?
      (?pp "\nCombining the fg and bg into a single sgr..." m)
      (println "=>\n"
               (util/readable-sgr ret)
               "\n"))
    ret))


;; This is used for dynmically colorizing brackets when they need bg colors
;; Maybe refactor to use x->sgr? But first refactor x->sgr to handle italics?
(defn kv->sgr
  [{:keys [color background-color font-style font-weight]}]
  ;; `color` is an xterm id such as 67
  (let [fgc    (some->> color (str "38;5;"))
        bgc    (some->> background-color (str "48;5;"))
        italic (when (contains? #{"italic" :italic} font-style) "3;")
        weight (when (contains? #{"bold" :bold} font-weight) ";1")
        ret    (str "\033[" italic fgc weight (when bgc ";") bgc "m")]
    ret))


(defn xterm-id->sgr [n]
  (kv->sgr {:color n}))


(defn map-vals [f m]
  (into {} (map f m)))

(defn enable-truecolor-debug-message [v]
  (println
   (str "enable-terminal-truecolor? is set to true, so converting to [r g b a] "
        "vector via (color/hexa->rgba v) => " v)))

(defn disable-truecolor-debug-message [v]
  (println
   (str "enable-terminal-truecolor? is set to false, so converting to x256 id "
        "(int) via (color/hexa->x256 v) => " v)))

(defn hexa-or-sgr
  [[k v]]
  (let [debug? false
        f      #(if (:enable-terminal-truecolor? @config) 
                  (let [ret (color/hexa->rgba v)] 
                    (when debug? (enable-truecolor-debug-message ret))
                    ret)
                  (let [ret (color/hexa->x256 v)]
                    (when debug? (disable-truecolor-debug-message ret))
                    ret))
        x      (if (and (or (= k :color) (= k :background-color))
                        (string? v))
                 (do
                   (when debug? 
                     (println (str "(string? v) Value for " k " is " v)))
                   #?(:cljs
                      (if node? 
                        (f)
                        (do (when debug? (println (str "Returning " v)))
                            v)) 
                      :clj
                      (f)))
                 v)]
    [k x]))


(defn- fully-hydrated-map [m]
  (let [f (fn [[k v]] 
            [k
             (if (map? v)
               (map-vals hexa-or-sgr v)
               v)])]
    (map-vals f m)))


(defn- reduce-from-base
  [merged base]
  (reduce (fn [acc [k v]]
            (assoc acc k (if (keyword? v) (v base) v)))
          {}
          merged))


(defn kv->css2 [[k v]]
  (str (name k) ":" (if (number? v) v (some-> v name)) ";"))


(defn sanitize-style-map
  [m]
  (let [ret (select-keys m defs/valid-stylemap-keys)
        f #(let [ret* (if (false? (:enable-terminal-italics? @config)) 
                        (dissoc ret :font-style)
                        ret)
                 ret  (if (false? (:enable-terminal-font-weights? @config))
                        (dissoc ret* :font-weight)
                        ret*)]
             ret)]
    #?(:cljs (if node? (f) ret)
       :clj  (f))))


(defn with-line-height [m]
  (assoc m
         :line-height
         (or (:line-height m)
             (:line-height @config))))


(defn serialize-style-maps [merged]
  (map-vals (fn [[k m]] 
              (let [m-with-k (assoc m :k k)
                    m        (sanitize-style-map m-with-k)] 
                #?(:cljs (if node? 
                           [k (m->sgr m-with-k)]
                           (let [m (with-line-height m)]
                             [k (string/join (map kv->css2 m))]))
                   :clj [k (m->sgr  m-with-k)])))
            merged))


(defn- hydrated-classes [base theme]
  (let [tokens  (:tokens theme)
        base*   (fully-hydrated-map (:classes base))
        base    (reduce-from-base base* base*)
        tokens* (fully-hydrated-map (:classes tokens))
        ret     (reduce-from-base (merge base tokens*) base*)]
    ret))


(defn- hydrated* [base theme classes k]
 (let [tokens  (:tokens theme)
       base    (reduce-from-base (k base) classes)
       tokens* (fully-hydrated-map (k tokens))
       ret     (reduce-from-base (merge base tokens*) classes)]
   ret))

(defn- add-metadata-key-entries
  [m]
  ;; Augment the font-style or font-weight
  ;; Currently, set at defaults as bolding creates problems for some terminals.
  (assoc m
         :metadata-key
         (assoc (:metadata m) :font-weight :normal)
         :metadata-key2
         (assoc (:metadata2 m) :font-weight :normal)))

(defn merge-theme+
  "Merges a theme (light or dark) with base theme (light or dark).

   Hydrates style-maps into maps that are computationally easier for interop.
   This step removes the font-weight and font-style options for the terminal
   context, as they are not reliably supported across emulators.

   This :constant example would be under [:tokens :classes], in a typical
   fireworks theme.
   {...
       {:constant {:color       \"#949494\"
                   :font-weight :bold
                   :font-style  :italic}}
   ...}

   cljs =>
   {... \"color:#949494;font-weight:bold;font-style:italic;\"}

   clj =>
   {... \"\033[38;5;246m\"}"
         
  [base theme]
  (let [classes (hydrated-classes base theme)
        syntax  (hydrated* base theme classes :syntax)
        printer (hydrated* base theme classes :printer)
        merged  (add-metadata-key-entries (merge classes syntax printer))
        merged2 (serialize-style-maps merged)]
    ;; (?pp 'base (-> base :classes :label))
    ;; (?pp 'theme (-> theme))
    ;; (?pp classes)
    ;; (?pp (select-keys classes [:highlight :highlight-underlined]))
    ;; (?pp (select-keys merged [:highlight :highlight-underlined]))
    {:with-style-maps            merged
     :with-serialized-style-maps (assoc
                                  merged2
                                  :rainbow-brackets
                                  ;; TODO check if this rainbow-brackets
                                  ;; thing is correct
                                  (or (-> theme :tokens :rainbow-brackets)
                                      (-> base :rainbow-brackets)))}))


;; TODO - don't use xterm for bracket colors
(defn- rainbow-brackets [mood theme]
  (let [contrast (let [x (or (:bracket-contrast theme) 
                             (:bracket-contrast @config))]
                   (case x
                     "high" :high
                     :high  :high
                     :low))
        context  #?(:cljs (if node? :x-term :browser)
                    :clj :x-term)
        ;; TODO - Figure out best way to pull rainbow brackets from theme?
        ret     (or (some->> theme :rainbow-brackets context rest (take-nth 2))
                    (->> basethemes/rainbow-brackets-colorscale
                         mood
                         context
                         contrast
                         vals))
        ret      #?(:cljs (if node? (map xterm-id->sgr ret) ret)
                    :clj (map xterm-id->sgr ret))]
    ret))

(defn valid-user-theme [theme*]
  (when (map? theme*) 
    (if (s/valid? ::theme/theme theme*)
      theme*
      (messaging/bad-option-value-warning
       (let [m (:theme config/options)]
         (merge m
                {:k      :theme
                 :v      theme*
                 :header (str "[fireworks.core/_p]."
                              "Problem with the supplied Fireworks theme \""
                              (:name theme*)
                              "\":")}))))))


;; FALLBACK THEMES
(def fallback-theme
  {:neutral themes/universal-neutral
   :light   themes/alabaster-light
   :dark    themes/alabaster-dark})


(defn- resolve-theme [config user-theme]
  (let [theme*                                             (:theme config)
        {:keys [:env-var/no-color? :env-var/force-color?]} config
        ;; no-color?                                          true
        fallback-theme                                     themes/universal-neutral]
    (when debug-theme?
      (pprint (merge
               (when (string? theme*) {"theme* from config" theme*})
               {"theme* from config is string?"     (string? theme*)
                "theme* from config name"           (some-> theme* :name)
                "fallback theme name"               (some-> fallback-theme :name)
                "valid user theme?"                 (when user-theme true)
                "NO_COLOR env var is non-empty?"    no-color?
                "FORCE_COLOR env var is non-empty?" force-color?}))
      (println))
    (cond
      (and no-color? (not force-color?))
      (do (when debug-theme?
            (println (str "NO_COLOR env var is not empty, so falling back to "
                          (some-> fallback-theme :name)))) 
          fallback-theme)
      

      (string? theme*)
      (do (when debug-theme?
            (println (str "Theme from user-config is string: " theme*)))
          (get basethemes/stock-themes theme*))

      user-theme
      (do (when debug-theme?
            (if user-theme
              (println (str "User theme is valid!" ))
              (println (str "User theme is not valid!"))))
          user-theme)

      :else
      (do (when debug-theme?
            (println "Falling back to " (some-> fallback-theme :name))) 
          fallback-theme))))

(defn merged-theme*
  ([]
   (merged-theme* nil))
  ([reset]
   (let [theme*           (:theme @config) ;; TODO - should this be user-config?
         user-theme       (valid-user-theme theme*)
         theme            (resolve-theme @config user-theme)
         theme-suffix     (some-> theme :name (string/split #" ") last)
         mood             (case theme-suffix
                            "Light"
                            :light
                            "Dark"
                            :dark
                            :universal)
         base-theme       (case mood
                            :dark
                            basethemes/base-theme-dark*
                            :light
                            basethemes/base-theme-light*
                            basethemes/base-theme-universal*)
         rainbow-brackets (rainbow-brackets mood theme)
         base-theme       (assoc base-theme :rainbow-brackets rainbow-brackets)
         ret              (merge-theme+ base-theme theme)]

     (when debug-theme?
       (println)
       (pprint {
                "resolved theme name"   (some-> theme :name)
                "resolved theme suffix" theme-suffix
                "theme mood"            mood
                ;; "rainbow-brackets"      rainbow-brackets
                }))
     #_(?pp (keyed [theme* fallback-theme user-theme theme theme-suffix mood]))
     ret)))



;; -----------------------------------------------------------------------------
;; The merged-theme to use for printing, defs inside let form
;; -----------------------------------------------------------------------------

(let [{:keys [with-style-maps
              with-serialized-style-maps
              err
              err-x
              err-opts]}
      (try (merged-theme*)
           (catch #?(:cljs js/Object :clj Exception)
                  e
             (messaging/->FireworksThrowable e nil nil)))]
  ;; TODO - Does this ever reach the err branch?
  (if err 
    (let [{:keys [line
                  column
                  file]} (:form-meta err-opts)
          ns-str         (:ns-str err-opts)] 
      (messaging/caught-exception
       err
       {:header "[fireworks.state/merged-theme*]"
        :type   :error
        :form   err-x
        :line   line
        :column column
        :file   (or file ns-str)}))
    (do (def merged-theme 
          (atom with-serialized-style-maps))
        (def merged-theme-with-unserialized-style-maps
          (atom with-style-maps)))))



;; -----------------------------------------------------------------------------
;; Helpers for css 
;; -----------------------------------------------------------------------------

(defn line-height-css []
  (str "line-height:" (:line-height @config) ";"))



;; -----------------------------------------------------------------------------
;; Helpers for highlighting
;; -----------------------------------------------------------------------------

(defn- with-bling-color->sgr [m k v]
  (if-let [sgr (some->> v util/as-str (get defs/bling-colors*) :sgr)]
    (assoc m k sgr)
    (assoc m k v)))

(defn- with-bling-colors->sgr [m]
  (some->>
   m
   :style
   (reduce-kv
    (fn [m k v]
      (case (name k)
        "background-color"
        (with-bling-color->sgr m k v)
        "color"
        (with-bling-color->sgr m k v)
        m))
    {})))

(defn ^:public ?sgr
  "For debugging of sgr code printing.

   Prints the value with escaped sgr codes so you can read them in terminal
   emulators (otherwise text would just get colored).

   Returns the value."
  [s]
  ;; TODO - try to figure out way you can preserve the color in the output,
  ;; which would help even more for debugging.
  (some-> s 
          (string/replace #"\u001b\[([0-9;]*)[mK]"
                          (str "\033[38;5;231;48;5;247m"
                               "\\\\033["
                               "$1"
                               "m"
                               "\033[0;m"))
          println)
  s)


(defn highlight-style*
  [{:keys [style pred path class] :as m}]
  ;; (?pp m)
  ;; (?pp (keys @merged-theme))
  ;; (?pp (:highlight-underlined @merged-theme))
  ;; TODO - check is css-str always gonna be css?
  (let [style
        (with-bling-colors->sgr m)
        
        css-str (if style 
                  (let [style (sanitize-style-map style)]
                    (when (seq style)
                      #?(:cljs (if node?
                                 (m->sgr (map-vals hexa-or-sgr style))
                                 (str (->> style (map kv->css2) string/join)
                                      (line-height-css)))
                         :clj  (m->sgr (map-vals hexa-or-sgr style)))))
                  (let [highlight-class-sgr (some->> class (get @merged-theme))
                        highlight-sgr (:highlight @merged-theme)]

                    #_(do 
                      (println 'highlight-sgr)
                      (?sgr highlight-sgr)
                      (println 'highlight-class-sgr)
                      (?sgr highlight-class-sgr)
                      (println))

                    (or highlight-class-sgr highlight-sgr)))]
    (assoc (merge {} 
                  (when pred {:pred pred})
                  (when class {:class class})
                  (when path {:path path}))
           :style
           css-str)))


(defn highlight-style [x]
  (if (s/valid? ::config.specs/find x)
    (cond
      (map? x) 
      (highlight-style* x)
      (vector? x)
      (mapv highlight-style* x))
    (messaging/bad-option-value-warning
     {:k      :find
      :v      x
      :spec   ::config.specs/find
      :header "Problem with the supplied value for the :find (highlighting) option:" })
    #_(messaging/invalid-find-value-option x)))   


(def *formatting-meta-level (atom 0))

(defn formatting-meta-level []
  @*formatting-meta-level)

(defn metadata-token []
  (if (= 2 (formatting-meta-level)) :metadata2 :metadata))
