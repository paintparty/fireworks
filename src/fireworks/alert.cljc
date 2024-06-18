(ns fireworks.alert
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [fireworks.pp :refer [?pp]]
            [expound.alpha :as expound]
            [fireworks.specs.config :as config]
            [fireworks.color :refer [xterm-colors-by-id]]
            [fireworks.util :as util :refer [char-repeat maybe]]))

(def ^:private border-char "◢◤")
(def ^:private border-len 44)
(def ^:private alert-indent 4)
(defn- border-str [n] (string/join (repeat n border-char)))
(def ^:private unbroken-border (border-str (dec (/ border-len 2))))



(declare rich-console)

#?(:clj
   (defn- alert-tape
     "If passed a label option e.g. \"WARNING\" it will returns this:
   \"◢◤◢◤ WARNING ◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤\"

   If not passed a label option it will return this:
   ◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤
   
   Can be colorized with `:label-color` and :`tape-color` options.

   Can be made bold with `:label-font-weight` and :`tape-font-weight` options."
     [{:keys [label
              label-color
              label-font-weight
              tape-color
              tape-font-weight]
       :as   opts}]
     (let [border-opts {:color       tape-color
                        :font-weight tape-font-weight}]
       (rich-console (if label 
                       (str
                        (rich-console (str border-char
                                           border-char)
                                      border-opts)
                        (rich-console (str " " label " ")
                                      {:color       label-color
                                       :font-weight label-font-weight})
                        (rich-console 
                         (string/join
                          (repeat 
                           (/ (- border-len
                                 (dec alert-indent)
                                 (+ 2 (count label))
                                 2)
                              2)
                           border-char))
                         border-opts))
                       (rich-console unbroken-border border-opts))
                     (assoc opts :font-weight :bold)))))


(def supported-browser-dev-console-props 
  [:text-decoration-line      
   :text-decoration-style     
   :text-decoration-color 
   :text-underline-offset     
   :text-decoration-thickness 
   :line-height               
   :font-weight
   :font-style
   :color
   :background-color
   :border-radius
   :padding
   :padding-block
   :padding-block-start
   :padding-block-end
   :padding-inline
   :padding-inline-start
   :padding-inline-end
   :padding-bottom
   :padding-top
   :padding-right
   :padding-left
   :margin
   :margin-block
   :margin-block-start
   :margin-block-end
   :margin-inline
   :margin-inline-start
   :margin-inline-end
   :margin-bottom
   :margin-top
   :margin-right
   :margin-left])

(def sgr-colors
  {"red"    204 
   "orange" 172 #_208 #_214
   "yellow" 142 #_178
   "green"  70 #_76 #_40 #_41 #_34 #_71
   "blue"   75 #_45  #_69  #_33
   "purple" 135 #_141 #_171 #_99 #_129
   "magenta"   201
   "gray"   247
   "black"  16
   "white"  231})

;; Need to write it out in one map then create slices
(def colors-source
  {"red"    {:sgr 202 :semantic "negative"} 
   "yellow" {:sgr 178 :semantic "warning"}
   "green"  {:sgr 40 :semantic "positive"}
   "blue"   {:sgr 39 :semantic "accent"}
   "purple" {:sgr 135}
   "magenta"{:sgr 201}
   "gray"   {:sgr 247 :semantic "subtle"}
   "black"  {:sgr 16}
   "white"  {:sgr 231}})

(def semantics-by-callout-type
  {"error"    "negative"
   "warning"  "warning"
   "positive" "positive"
   "info"     "accent"
   "accent"   "accent"
   "subtle"   "subtle"
   "neutral"  "neutral"})

(def callout-types-by-semantic
  (set/map-invert semantics-by-callout-type))

(def color-names-by-semantic*
  (reduce-kv (fn [m color {:keys [semantic]}]
               (if semantic
                 (assoc m semantic color)
                 m))
             {}
             colors-source))

;; NIx?
(defn- sgr-css-color [v]
  {:sgr (:sgr (get colors-source v nil))
   :css (:css (get colors-source v nil))})

(defn assoc-hex-colors [m]
  (reduce-kv (fn [m color {:keys [sgr]}] 
               (let [hex (get xterm-colors-by-id sgr nil)]
                 (assoc m color {:sgr sgr
                                 :css hex})))
             {}
             m))

(defn reduce-colors [m1 m2]
  (reduce-kv (fn [m k color] 
               (assoc m k (get m2 color)))
             {}
             m1))

(def color-codes
  (let [colors    (assoc-hex-colors colors-source )
        semantics (reduce-colors color-names-by-semantic* colors)
        callouts  (reduce-colors semantics-by-callout-type semantics)]
    {:all              (merge colors semantics callouts)
     :colors           colors
     :semantics        semantics
     :callouts         callouts
     :colors+semantics (merge colors semantics)}))


;; slice up above data as needed

(def color-names-by-semantic
  {"error"    "red"
   "negative" "red"
   "warning"  "yellow"
   "accent"   "blue"
   "info"     "blue"
   "positive" "green"
   "neutral"  "black"
   "subtle"   "gray"})




(def hex-colors
  (reduce-kv (fn [m k v] 
               (assoc m k (get xterm-colors-by-id v nil)))
             {}
             sgr-colors))

(defn- sgr-css-color [v]
  {:sgr (get sgr-colors v nil)
   :css (get hex-colors v nil)})

(def color-names
  (->> sgr-colors keys (into #{})))

(def color-codes-by-semantic
  (reduce-kv
   (fn [m k v]
     (assoc m k (sgr-css-color v)))
   {}
   color-names-by-semantic))

(defn- semantic-color-code [k]
  (get color-codes-by-semantic (name k) (sgr-css-color "magenta")))

(defn color-names->sgr-ids [m]
  (let [m* (reduce-kv (fn [m k v] 
                        (let [v (if (and (pos-int? v)
                                         (<= 16 v 255))
                                  v
                                  (get sgr-colors (util/as-str v) nil))]
                          (if v (assoc m k v) m)))
                      {}
                      (select-keys m [:color :background-color]))]
    (merge (dissoc m :color :background-color)
           m*)))

(declare rich-console)

(defn callout-type [x]
  (cond 
    (keyword? x) (name x)
    (string? x)  x
    :else
    (some-> (when (map? x)
              (or (get x :alert-type nil)
                  (get x "alert-type" nil)))
            (maybe util/nameable?)
            name)))

(defn message-body [x]
  (cond
    (util/nameable? x)
    (util/as-str x)
    (vector? x)
    (str "\n" (string/join "\n\n" x))))

;; USE :keys
(defn bad-form [line gttr form callout-type]
  (let [opts {:font-weight :bold}
        squig-opts (merge opts 
                          {:color (get (-> color-codes :callouts)
                                       callout-type 
                                       nil)})
        squig (string/join (repeat (count form) "^"))]
    (str line " │ " (rich-console form opts) "\n"
         ;; Replace with tagged-str or enrich?
         gttr " │ " (rich-console squig squig-opts) "\n")))


(defn problem-with-line-info
  [{:keys [line] :as form-meta}
   {:keys [header
           form
           body
           alert-type]}]
  (let [file-info (util/form-meta->file-info form-meta) 
        gttr      (some-> line str count util/spaces) 
        form      (util/shortened form 33)
        callout-type (callout-type alert-type)]
    (str header "\n"
         "\n"
         gttr " ┌─ " file-info "\n"
         gttr " │  \n"
         (bad-form line gttr form callout-type)
         (message-body body))))

(def alert-type->label
  {"warning" "WARNING"
   "error"   "ERROR"
   "info"    "INFO"})

(defn print-lines [n]
  (when (pos-int? n)
              (dotimes [_ n]
                (println))))

(defn default-spacing [n default]
  (if (and (int? n) (<= 0 n)) n default))




(defn console-alert
  [{:keys [label
           message
           alert-type
           margin-top
           margin-bottom
           padding-top
           padding-bottom
           theme]}]
  (let [tape?          #?(:cljs false :clj (= theme :tape))          
        padding-top    (default-spacing padding-top (if tape? 1 0))
        padding-bottom (default-spacing padding-bottom (if tape? 1 0))
        margin-top     (default-spacing margin-top 1)
        margin-bottom  (default-spacing margin-bottom 1)
        callout-type   (callout-type alert-type)
        color-code     (get (-> color-codes :callouts)
                            callout-type 
                            (get (color-codes :colors) "magenta" nil))]


    #?(:cljs
       (let [js-arr (into-array
                     (concat [(str (char-repeat padding-top "\n")
                                   message
                                   (char-repeat padding-bottom "\n")
                                   "\n")]
                             ["font-weight:bold"
                              "font-weight:initial"
                              (str "font-weight:bold;color:"
                                   (:css color-code)
                                   ";")
                              "font-weight:initial;color:initial"]))]
         (.apply (case callout-type 
                   "warning" (.-warn  js/console)
                   "error"   (.-error  js/console)
                   (.-log  js/console))
                 js/console
                 js-arr))
       :clj
       (let [label            (or label
                                  (get alert-type->label
                                       callout-type
                                       nil))
             tape-opts        {:label             label
                                ;; :border-color      (-> callout-type
                                ;;                        alert-type-color
                                ;;                        :sgr)
                               :label-font-weight :bold}
             side-border-opts {:font-weight :bold
                               :color       (:sgr color-code)}]

              ;; With tape
         #_(print
            (str #?(:cljs nil :clj (char-repeat margin-top "\n"))
                 (str 
                  (alert-tape tape-opts) "\n"
                  (char-repeat padding-top "\n")
                  message "\n"
                  (char-repeat padding-bottom "\n")
                  (alert-tape (dissoc tape-opts :label)))
                 #?(:cljs nil :clj (char-repeat margin-bottom "\n")) "\n"))

              ;; simple
         (print
            (str #?(:cljs nil :clj (char-repeat margin-top "\n"))
                 (rich-console (str "┏" (some->> label (str "━ " ))) side-border-opts)
                 #_(rich-console (str "▌  " label) side-border-opts)

                 (string/replace 
                  (str 
                   (char-repeat padding-top "\n") "\n"
                   message)
                  #"\n"
                  (str "\n" (rich-console "┃  " side-border-opts))
                  #_(str "\n" (rich-console "▌  " side-border-opts))
                  )
                 (char-repeat padding-bottom 
                              #_(str "\n" (rich-console "▌  " side-border-opts))
                              (str "\n" (rich-console "┃  " side-border-opts)))

                 #_(str "\n" (rich-console "▘" side-border-opts))
                 (str "\n" (rich-console (str "┗" #_"━") side-border-opts))
                 #?(:cljs nil :clj (char-repeat margin-bottom "\n")) "\n"))
         )))
  nil)

(defn rich-console [s opts]
  #?(:cljs
     (str "%c" s "%c")
     :clj
     (do 
       (str (-> opts
                (color-names->sgr-ids)
                util/m->sgr)
            s
            "\033[0;m"))))

(defn- reduce-colors-to-sgr-or-css [ctx m]
  (reduce-kv (fn [m k v]
               (assoc m k (if (map? v) (ctx v) v)))
             {}
             m))

(defn tagged-str [m s]
  #?(:cljs
     (str "%c" s "%c")
     :clj
     (do 
       (str (->> m
                 (reduce-colors-to-sgr-or-css :sgr)
                 util/m->sgr)
            s
            "\033[0;m"))))


(defrecord EnrichedText [style value])


;; TODO - add underline for SGR
(defn- tag->map [acc s]
  (let [[k m] (case s
                "bold"   [:font-weight "bold"]
                "italic" [:font-style "italic"]
                (let [cs (:all color-codes)
                      m  (get cs s nil)]
                  (if m
                    [:color m]
                    (when-let [nm (string/replace s #"-bg$" "")] 
                      (when-let [m (get cs nm nil)]
                        [:background-color m])))))]
    (if k (assoc acc k m) acc)))


(defn enrich [style v]
  (->EnrichedText 
   (cond 
     (map? style)
     (reduce-kv (fn [m k v]
                  (assoc m
                         k 
                         (if (contains? #{:background-color :color} k)
                           (get (:all color-codes) v nil)
                           v)))
                {}
                style)
     
     (or (keyword? style) (string? style))
     (let [split-re (if (keyword? style) #"\." #" ")
           ks       (-> style name (string/split split-re))
           m        (reduce tag->map {} ks)]
       m))
   (util/as-str v)))


(defn updated-css [css-styles x]
  (if-let [styles (some-> (:style x)
                          (select-keys supported-browser-dev-console-props))]
    (do 
      (let [styles       (reduce-colors-to-sgr-or-css :css styles)
            ks           (keys styles)
            style-resets (reduce (fn [acc k] (assoc acc k "initial")) {} ks)]
        (conj css-styles
              (util/css-stylemap->str styles)
              (util/css-stylemap->str style-resets))))
    css-styles))


(defn- callout-data* [coll]
  (let [[coll css] (reduce
                    (fn [[coll css] x] 
                      (let [s (if (instance? EnrichedText x)
                                (tagged-str (:style x) (:value x))
                                (util/as-str x))]
                        [(conj coll s)
                         (updated-css css x)]))
                    [[] []]
                    coll)
        joined     (string/join coll)]
    {:css-array     (into-array (concat [joined] css))
     :tagged-str joined}))


(defn ^:public callout-data [coll]
  #?(:cljs
     (-> coll callout-data* :css-array)
     :clj
     (-> coll callout-data* :tagged-str)))


(defn ^:public callout-data-css [coll]
  (-> coll callout-data* :css-array))



(defn ^:public callout
  ([coll]
   (callout nil coll))
  ([x coll]
   (let [{:keys [css-array
                 tagged-str]} (callout-data* coll)
         alert-type           (callout-type x)]
     #?(:cljs
        (let [js-arr (into-array (concat [tagged-str] css-array))]
          (-> alert-type 
              (case 
               "warning" js/console.warn
               "error" js/console.error
               js/console.log)
              (.apply js/console js-arr)))
        :clj
        (do (if alert-type
              (console-alert (merge 
                              (maybe x map?)
                              {:message    tagged-str
                               :alert-type alert-type}))
              (println tagged-str))))
     nil)))




;;; END OF NEW ------------------------------------
