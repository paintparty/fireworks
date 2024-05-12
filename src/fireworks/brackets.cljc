(ns ^:dev/always fireworks.brackets
  (:require
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.state :as state]
   [fireworks.tag :as tag :refer [tag! tag-reset! style-from-theme]]
   [fireworks.util :refer [badge-type]]))

(defn brackets-by-type
  [{:keys [t map-like? js-map-like? js-typed-array? :fw/user-meta-map?] :as m}]
  (cond 
    (or map-like?
        (contains? #{:map :js/Object :record :js/Map} t)
        js-map-like?)
    (if user-meta-map?
      ["^{" "}"]
      ["{" "}"])

    (or (= t :vector)
        (= t :js/Array)
        js-typed-array?)
    ["[" "]"]

    (or (= :seq t) (= :list t))
    ["(" ")"]

    (or (= t :set) (= t :js/Set))
    ["#{" "}"]

    (= t :meta-map)
    ["^{" "}"]

    (or (= :function t) (= :lamda t))
    ["" ""]
    :else
        ;; This should probably be ["" ""]
        ;; We need more granularity from lasertag first
        ;; :list-like? :array-like?
    ["[" "]"]))


(def num-indent-spaces
  (reduce (fn [acc k] 
            (assoc acc
                   k 
                   (-> {:t k}
                       brackets-by-type
                       first
                       count)))
          {}
          [:meta-map
           :map       
           :js/Object 
           :js/Array  
           :record    
           :set       
           :js/Set    
           :vector    
           :list      
           :seq]))

(defn- rainbow-bracket-color
  []
  (let [rb           (:rainbow-brackets @state/merged-theme) 
        num-brackets (count rb)
        level        @state/rainbow-level
        color        (nth rb
                          level
                          (let [new-index (rem level num-brackets)]
                            (nth rb new-index)))]
    color))

(defn- rainbow-bracket-mixin
  "Solution for getting rainbow bracket fg color and
   mixing in bgc if record or badge etc."
  [mm]
  (let [color (rainbow-bracket-color)
        bgc   (let [label-type
                    (some-> mm :badge badge-type)

                    user-meta
                    (some-> mm :fw/user-meta)

                    {bgc :background-color}
                    (or 
                     ;; This colors brackets if custom-badge-style
                     ;; Leave this out for now
                     #_(some-> mm :fw/custom-badge-style)
                     (when (or user-meta label-type)
                       (let [style-maps
                             @state/merged-theme-with-unserialized-style-maps]
                         (or (get style-maps label-type nil)
                             (get style-maps (state/metadata-token) nil)))))]
                  bgc)]
    #?(:cljs
       (str "color:" color 
            (when bgc (str ";background-color:" bgc))
            ";"
            (state/line-height-css))
       :clj
       (if (and bgc (vector? bgc))
         (let [bgc-sgr (state/x->sgr bgc :bg)
               color   (string/replace color #"m$" (str ";" bgc-sgr "m"))]
           color)
         color))
    ))

(defn- tag-bracket!
  "Adds the appropriate style to the state/styles vector.
   Rainbow parens by default.
   Function args vector not included in rainbow parens."
  [{:keys [t mm]}]
  (let [style (cond
                (pos? (state/formatting-meta-level))
                (get @state/merged-theme (state/metadata-token) nil)

                (= t :fn-args)
                (-> @state/merged-theme t)

                :else
                (if (:enable-rainbow-brackets? @state/config)
                  (rainbow-bracket-mixin mm)
                  (style-from-theme :bracket nil)))]
    #?(:cljs
       (swap! state/styles conj style)
       :clj
       style)))

(defn- bracket!*
  [{:keys [s t] :as m}]
  (let [reset-theme-token (if (pos? (state/formatting-meta-level)) :metadata :foreground)]
    #?(:cljs (if t (do (tag-bracket! m)
                       (tag-reset! reset-theme-token)
                       (str "%c" s "%c"))
                 (do (tag-reset! reset-theme-token)
                     (tag-reset! reset-theme-token)
                     (str "%c" s "%c")))
       :clj (if t 
              (str (tag-bracket! m) s (tag-reset!))
              (str (tag-reset!) s (tag-reset!))))))

(defn- bracket!
  [m kw]
  (let [mm      (-> m :coll meta)
        t       (:t mm)
        t       (cond 
                  (:record? m)     :map
                  (= t :js/Object) :map
                  :else            t)
        [ob cb] (brackets-by-type mm)  
        s       (if (= kw :opening) ob (if (:record? m) (str cb "") cb))
        bracket   (bracket!* {:s s :t t :mm mm})]
    bracket))

(defn closing-bracket!
  [m]
  (swap! state/rainbow-level dec)
  (let [cb (bracket! m :closing)]
    cb))

(defn opening-bracket!
  [m]
  (let [ob         (bracket! m :opening)]
    (swap! state/rainbow-level inc)
    ob))

(defn closing-angle-bracket! 
  "This is for when collections are also like atoms,
   so they can be printed like `Atom<[1 2 3]>`"
  [m]
  (when (-> m :coll meta :atom?)
    (str (tag! :atom-wrapper)
         defs/encapsulation-closing-bracket
         (tag-reset!))))
