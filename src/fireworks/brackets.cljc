(ns ^:dev/always fireworks.brackets
  (:require [clojure.string :as string]
            [fireworks.defs :as defs]
            #?(:clj [fireworks.state :as state]
               :cljs [fireworks.state :as state :refer [node?]])
            [fireworks.tag :as tag :refer [style-from-theme tag! tag-reset!]]
            [fireworks.util :refer [badge-type]]))

(defn brackets-by-type
  [{:keys [t
           map-like?
           set-like?
           js-map-like?
           js-typed-array?
           :fw/user-meta-map?]
    :as m}]
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

    set-like?
    ["#{" "}"]

    (= t :meta-map)
    ["^{" "}"]

    (or (= :function t) (= :lambda t))
    ["" ""]
    :else
        ;; This should probably be ["" ""]
        ;; We need more granularity from lasertag first
        ;; :list-like? :array-like?
    ["[" "]"]))

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
                  bgc)
        f #(if (and bgc (vector? bgc))
             (let [bgc-sgr (state/x->sgr bgc :bg)
                   color   (string/replace color #"m$" (str ";" bgc-sgr "m"))]
               color)
             color)]
    #?(:cljs
       (if node? 
         (f)
         (str "color:" color 
              (when bgc (str ";background-color:" bgc))
              ";"
              (state/line-height-css)))
       :clj
       (f))
    ))

(defn- tag-bracket!
  "Adds the appropriate style to the state/styles vector.
   Rainbow parens by default.
   Function args vector not included in rainbow parens."
  [{:keys [t mm]} s]
  (let [theme-token (cond
                      (-> mm :user-meta :fw/hide-brackets?)
                      nil

                      (pos? (state/formatting-meta-level))
                      (state/metadata-token)

                      (= t :fn-args)
                      t

                      :else
                      :rainbow-brackets)
        style (when-not (:highlighting mm) ;; <- if collection is to be highlighted, we just leave it alone.
                (if (= theme-token :rainbow-brackets) 
                  (if (:enable-rainbow-brackets? @state/config)
                    (rainbow-bracket-mixin mm)
                    (style-from-theme :bracket nil))
                  (get @state/merged-theme theme-token nil)))]

    #_(when (state/debug-tagging?)
        (println "tag-bracket! "
                 #?(:clj (str style s "\033[0m"))
                 "   theme-token is   "
                 theme-token
                 (str ",  style is:   " (readable-sgr style))))

    #?(:cljs (if node? style (swap! state/styles conj style))
       :clj style)))

(defn- bracket!*
  [{:keys [s t] :as m}]
  (let [reset-theme-token (if (pos? (state/formatting-meta-level))
                            ;; :metadata
                            :foreground
                            :foreground)
        s (if (-> m
                  :mm
                  :user-meta
                  :fw/hide-brackets?)
            " "
            s)
        f #(if t 
             (str (tag-bracket! m s) s (tag-reset!))
             (str (tag-reset!) s (tag-reset!)))]

    (when (state/debug-tagging?)
      (println "\nbracket!*  " s ",  t: " t))

    #?(:cljs (if node? 
               (f)
               (if t 
                 (do (tag-bracket! m s)
                     (tag-reset! reset-theme-token)
                     (str "%c" s "%c"))
                 (do
                   (tag-reset! reset-theme-token)
                   (tag-reset! reset-theme-token)
                   (str "%c" s "%c"))))
       :clj (f))))

(defn- bracket!
  [m kw]
  (let [mm      (-> m :coll meta)
        t       (:t mm)
        t       (cond 
                  (:record? m)     :map
                  (= t :js/Object) :map
                  :else            t)
        [ob cb] (brackets-by-type mm)  
        s       (cond 
                  ;; TODO - maybe move this bracket up to bracket!*
                  (:let-bindings? m)
                  " "

                  (= kw :opening)    
                  ob

                  (:record? m)       
                  (str cb "")

                  :else              
                  cb)
        bracket (bracket!* {:s  s
                            :t  t
                            :mm mm})]
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
  "This is for when collections are encapsulated in a mutable construct such as
   an atom or volatile, so they can be printed like `Atom<[1 2 3]>`"
  [m]
  ;; TODO - change names to avoid shadowing
  (let [{:keys [val-is-atom? val-is-volatile?]} (-> m :coll meta)]
    (when (or val-is-atom? val-is-volatile?)
      (let [k (if val-is-atom? :atom-wrapper :volatile-wrapper)]
        (when (state/debug-tagging?)
          (println "\ntagging " defs/encapsulation-closing-bracket " with " k))
        (str (tag! k)
             defs/encapsulation-closing-bracket
             (tag-reset!))))))
