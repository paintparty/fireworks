(ns ^:dev/always fireworks.tag
  (:require [clojure.string :as string]
            [fireworks.macros :refer [keyed]]
            [fireworks.pp :refer [?pp pprint]]
            #?(:cljs [fireworks.macros :refer-macros [keyed]])
            #?(:clj [fireworks.state :as state :refer [?sgr]]
               :cljs [fireworks.state :as state :refer [node?]])))


(def tagtype->theme-key
  {:fn-args  :function-args
   :defmulti :function
   :class    :function})

(defn style-from-theme
  ([t bgc]
   (style-from-theme t bgc nil))
  ([t bgc custom-badge-style]
   (let [debug? (state/debug-tagging?)]
     #_(when debug?
       (pprint (reduce-kv
                (fn [m k v]
                  (assoc m k (if (string? v) (state/?sgr-str v) v)))
                {}
                (keyed [t bgc custom-badge-style]))))

     (let [f #(let [k
                    (get tagtype->theme-key % %)

                    merged-theme-map
                    (if custom-badge-style
                      @state/merged-theme-with-unserialized-style-maps
                      @state/merged-theme)

                    ret
                    (k merged-theme-map)

                    ret-alt
                    (k @state/merged-theme-with-unserialized-style-maps)]
                #_(when debug?
                  (println "\n")
                  (println (list 'get 'tagtype->theme-key % %) '=> k)
                  (println (str "\nValue for " k " (" % ") from merged theme: "))
                  (pprint (if (string? ret)
                            (state/?sgr-str ret)
                            ret))

                  (println "\n")

                  (pprint (if (string? ret-alt)
                            (state/?sgr-str ret-alt)
                            ret-alt))

                  (println "\n"))
                ret)

           m (or (f t) (f :foreground))

           m (if custom-badge-style
               (let [m (state/sanitize-style-map custom-badge-style)
                     m (state/with-line-height m)
                     f #(let [m (state/map-vals state/hexa-or-sgr m)]
                          (state/m->sgr m))]
                 #?(:cljs
                    (if node? (f) (string/join (mapv state/kv->css2 m)))
                    :clj
                    (f)))
               m)]
       
       (str m bgc)))))

(defn tag!
  ([t]
   (tag! t nil nil))
  ([t bgc]
   (tag! t bgc nil))
  ([t bgc custom-badge-style]
   (let [s (style-from-theme (cond
                               (contains? #{:metadata :metadata-key} t)
                               t

                               (= t :type-label-inline)
                               :type-label

                               :else
                               t)
                             bgc
                             custom-badge-style)]

     (when (state/debug-tagging?)
       #?(
          ;; :cljs (if node?
          ;;         s
          ;;         (let [s (cond
          ;;                   (= t :type-label)
          ;;                   (str s)
          ;;                 ;; (= t :metadata)
          ;;                 ;; (str s "")
          ;;                   :else
          ;;                   s)]
          ;;           (swap! state/styles conj s)
          ;;           "%c"))
          :clj (do (println "style from theme" )
                   (?sgr s))
          ))

     #?(:cljs (if node?
                s
                (let [s (cond
                          (= t :type-label)
                          (str s)
                          ;; (= t :metadata)
                          ;; (str s "")
                          :else
                          s)]
                  (swap! state/styles conj s)
                  "%c"))
        :clj s))))

(def sgr-closing-tag-str "\033[0m")

(defn tag-reset!
  ([]
   (tag-reset! :foreground))
  ([theme-token]
   #?(:cljs (if node?
              (do (when (state/debug-tagging?)
                    (println "tag/tag-reset!  with  \\\033[0m"))
                  sgr-closing-tag-str)
              (let [theme-token (or theme-token :foreground)]
                (when (state/debug-tagging?)
                  (println "tag/tag-reset!  with  " theme-token))
                (swap! state/styles
                       conj
                       (-> @state/merged-theme theme-token))
                "%c"))
      :clj 
      (do (when (state/debug-tagging?)
            (println "tag/tag-reset!  with  \\033[0m"))
          sgr-closing-tag-str)
      )))

(defn tag-entity! 
  ([x t]
   (tag-entity! x t nil))
  ([x t bgc]
   (str (tag! t) x (tag-reset!))))

(defn tagged
   ([s]
    (tagged s nil))
   ([s
     {:keys [theme-token
             display? 
             highlighting
             custom-badge-style]
      :or   {display?    true
             theme-token :foreground}}]

   (pprint (keyed [theme-token
                   display? 
                   highlighting
                   custom-badge-style]))

    ;; s is always a string or vector (in the case of fn-args)
    (when (and (or (string? s)
                   (symbol? s)
                   (vector? s))
               display?)

      (let [opening-tag (tag! theme-token highlighting custom-badge-style)
            closing-tag (tag-reset!)
            ret         (str opening-tag s closing-tag)]

        (when (state/debug-tagging?)
              (println "\ntag/tagged    :   tagging \"" s "\" with " theme-token) )

        ret))))

