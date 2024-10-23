(ns ^:dev/always fireworks.state
  (:require #?(:cljs [fireworks.macros :refer-macros [get-user-configs keyed]]
      :clj  [fireworks.macros :refer        [get-user-configs keyed]])
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

;; Internal state atoms for development debugging ------------------------

;; Temp for debugging theme tokens
(def debug-on-token? (atom false))
(def token-debugging-target
  #_:nothing
  #_:annotation
  #_:definition
  #_:constant
  #_:comment)

(defn reset-token-debugging! [k]
  (reset! debug-on-token? (= k token-debugging-target)))

;; Temp for debugging tagging 
(def *debug-tagging? (atom false))

(defn debug-tagging? []
  @*debug-tagging?)


;; Internal state atoms for printing and formatting ------------------------

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
;; result of `?let`, `?if-let`, or `?when-let`
(def let-bindings? (atom false))

;; This will indent the whole output - label, file-info, and result
(def margin-inline-start (atom nil))

;; Helpers for creating printing options state ------------------------------

;; The user's config from disc
;; Path is set via env var export FIREWORKS_CONFIG="path/to/config.edn"
;; Recommended location for path is "~/.fireworks/config.edn"


(def user-config (get-user-configs))


;; TODO - figure out how to get filename, line, and column.
(defn validate-option-from-user-config-edn
  [k v {:keys [spec default]}]
  (let [undefined? (or (when (string? v) (string/blank? v))
                       (nil? v))
        valid?     (when-not undefined? (if spec (s/valid? spec v) v))
        invalid?   (and (not undefined?) (not valid?))]
    (if invalid?
      (messaging/bad-option-value-warning
       (assoc (keyed [k v default spec])
              :header (str (some-> user-config
                                   :path-to-user-config
                                   (str "\n\n"))
                           (str "Invalid value for " k " in user config."))))
      (if (or undefined? (not valid?))
        nil
        v))))


;; Validated config options from user's config.edn
(def user-options
  (try 
    (into {}
          (map (fn [[k v]]
                 (let [m         (k config/options)
                       validated (validate-option-from-user-config-edn k v m)]
                   [k validated]))
               user-config))

    ;; TODO - check if this actually catches anything?
    (catch #?(:cljs js/Object
              :clj Exception)
           e
      (messaging/caught-exception e {})
      (swap! messaging/warnings-and-errors
             conj
             [:messaging/print-error e])
      {})))


(def fallbacks
  (into {} (map (fn [[k v]] [k (:default v)]) config/options)))

(defn- dark? [x]
  (contains? #{:dark "dark"} x))

(defn user-opt-val [k]
  (let [opt (k user-options)
        opt (when-not 
             (and (string? opt)
                  (string/blank? opt))
              opt)]
    (or opt
        (if (= k :theme)
            ;; TODO !!!
            ;; Should these resolve to map to be consistent?
          (if (dark? (:mood user-options))
            "Alabaster Dark"
            "Alabaster Light")
          (k fallbacks)))))


;; Internal state atom for user options ------------------------------------
(defn config* []
  (into {}
        (map (fn [[k _]]
               [k (user-opt-val k)])
             config/options)))
(def config
  (atom (config*)))


;; Theme merging functions -------------------------------------------------
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

(defn invalid-color-warning 
  [{:keys [header v k footer theme-token from-custom-badge-style?]}]
  (str header
       "\n\n"
       #?(:cljs
          (str "%c" k " " "\"" v "\"%c")
          :clj
          (str "\033[1;m" k " " "\"" v "\"\033[0;m"))
       
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

(defn x->sgr [x k]
  (when x
    (let [n (if (= k :fg) 38 48)]
      (if (int? x)
        (str n ";5;" x)
        (let [[r g b _] x
              ret       (str n ";2;" r ";" g ";" b)]
          ret)))))

(defn legacy? [k]
  (or (:legacy-terminal? @config)
      (false? (k @config))))

(defn m->sgr
  [{fgc*        :color
    bgc*        :background-color
    font-style  :font-style
    font-weight :font-weight
    :as         m}]
  (when @debug-on-token?
    (println "\n(fireworks.state/m->sgr " m ")"))
  (let [fgc    (x->sgr fgc* :fg)
        bgc    (do #_(when bgc* (println "m->sgr:bgc* " bgc*))
                   (x->sgr bgc* :bg))
        italic (when (and (not (false? (:enable-terminal-italics? @config)))
                          (contains? #{"italic" :italic} font-style))
                 "3;")
        weight (when (and (not (false? (:enable-terminal-font-weights? @config)))
                          (contains? #{"bold" :bold} font-weight))
                 ";1")
        ret    (str "\033[" 
                    italic
                    fgc
                    weight
                    (when (or (and fgc bgc)
                              (and weight bgc))
                      ";")
                    bgc
                    "m")]
    (when  @debug-on-token?
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


(defn hexa-or-sgr
  [[k v]]
  (let [debug? @debug-on-token?
        x (if (or (= k :color)
                  (= k :background-color))
            (cond
              (string? v)
              (do 
                (when debug? 
                  (println
                   (str "(string? v) Value for " k " is " v)))
                #?(:cljs
                   (do 
                     (when debug? (println (str "Returning " v)))
                     v) 
                   :clj
                   (do 
                     (if (legacy? :enable-terminal-truecolor?) 
                       (do 
                         (when debug?
                           (println 
                            (str "enable-terminal-truecolor? is set to"
                                 " false, so converting to x256 id (int)"
                                 " via (color/hexa->x256 v) => "
                                 (color/hexa->x256 v))))
                         (color/hexa->x256 v))
                       (do 
                         (when debug?
                           (println
                            (str "enable-terminal-truecolor? is set to"
                                 " true, so converting to [r g b a] vector"
                                 " via (color/hexa->rgba v) => "
                                 (color/hexa->rgba v))))
                         (color/hexa->rgba v)))))))
            v)]
    [k x]))


(defn- fully-hydrated-map [m]
  (let [f (fn [[k v]] 
            (reset-token-debugging! k)
            [k
             (if (map? v)
               (do (when @debug-on-token?
                     (println "(map-vals hexa-or-sgr v) =>"))
                   (map-vals hexa-or-sgr v))
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


(defn sanitize-style-map [m]
  (let [ret (select-keys m defs/valid-stylemap-keys)]
    #?(:cljs
       ret
       :clj
       (let [ret* (if (false? (:enable-terminal-italics? @config)) 
                    (dissoc ret :font-style)
                    ret)
             ret (if (false? (:enable-terminal-font-weights? @config))
                   (dissoc ret* :font-weight)
                   ret*)]
         ret))))


(defn with-line-height [m]
  (assoc m
         :line-height
         (or (:line-height m)
             (:line-height @config))))


(defn serialize-style-maps [merged]
  (map-vals (fn [[k m]] 
              (let [m-with-k (assoc m :k k)
                    m        (sanitize-style-map m-with-k)] 
                #?(:cljs (let [m (with-line-height m)]
                           [k (string/join (map kv->css2 m))])
                   :clj (do
                          (reset-token-debugging! k)
                          (when @debug-on-token?
                            (println "\n" "(serialize-style-maps) => \n" k m))
                          [k (m->sgr m-with-k)]))))
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

(defn- add-metadata-key-entries [m]
  (assoc m
         :metadata-key
         (assoc (:metadata m) :font-weight :bold)
         :metadata-key2
         (assoc (:metadata2 m) :font-weight :bold)))

(defn merge-theme+
  "Merges a theme (light or dark) with base theme (light or dark).

   Hydrates style-maps into maps that are computationally easier
   for interop. This step removes the font-weight and font-style
   options for the terminal context, as they are not reliably supported
   across emulators.

   This :constant example would be under [:tokens :classes],
   in a typical fireworks theme.
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
        context  #?(:cljs :browser
                    :clj :x-term)
        ;; TODO - Figure out best way to pull rainbow brackets from theme?
        ret     (or (some->> theme :rainbow-brackets context rest (take-nth 2))
                    (->> basethemes/bracket-colors
                         mood
                         context
                         contrast
                         vals))
        ret      #?(:cljs ret
                    :clj (map xterm-id->sgr ret))]
    ret))

;; TODO v0.5.0
;; (defn metadata-bgcs
;;   [mood
;;    theme
;;    base-theme]
;;   (let [printer-metadata (some-> theme
;;                                  :printer
;;                                  :metadata)
;;         bgc              (when (map? printer-metadata)
;;                            (:background-color printer-metadata))
;;         bgc              (or bgc
;;                              (-> base-theme
;;                                  :classes
;;                                  :metadata
;;                                  :background-color))
;;         ;; rgb              ()
;;         ]
;;     (?pp :bgc bgc)))


(defn merged-theme*
  ([]
   (merged-theme* nil))
  ([reset]
   ;; TODO - Add observability for theme, user-config.
   ;; from env-vars, parsed and validated.
   (let [theme*           (:theme @config)

         fallback-theme   themes/universal-neutral

         valid-user-theme (when (map? theme*) 
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
                                                      "\":")})))))

         theme            (cond
                            (string? theme*)
                            (get basethemes/stock-themes theme*)
                            valid-user-theme
                            valid-user-theme
                            :else
                            fallback-theme)

         mood             (if (dark? (:mood theme))
                            :dark
                            :light)     

         base-theme       (if (= mood :dark)
                            basethemes/base-theme-dark*
                            basethemes/base-theme-light*)

         rainbow-brackets (rainbow-brackets mood theme)

        ;;  metadata-bgcs    (metadata-bgcs mood theme base-theme)
         
         base-theme       (assoc base-theme :rainbow-brackets rainbow-brackets)

         ret              (merge-theme+ base-theme theme)]

     ;; TODO - Add observability for theme
     ret)))


;; The merged theme to use for printing --------------------------------
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


;; Helpers for css -----------------------------------------------------
(defn line-height-css []
  (str "line-height:" (:line-height @config) ";"))


;; Helpers for highlighting --------------------------------------------
(defn highlight-style* [{:keys [style pred]}]
  (let [css-str (if style 
                  (let [style (sanitize-style-map style)]
                    (when (seq style)
                      #?(:cljs (str (->> style
                                         (map kv->css2)
                                         string/join)
                                    (line-height-css))
                         :clj  (m->sgr (map-vals hexa-or-sgr style)))))
                  (:highlight @merged-theme))]
    (assoc {:pred pred}
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
