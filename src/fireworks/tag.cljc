(ns ^:dev/always fireworks.tag
  (:require [clojure.string :as string]
            [fireworks.pp :refer [pprint]]
            [fireworks.state :as state :refer [?sgr]]))


(def tagtype->theme-key
  {:fn-args  :function-args
   :defmulti :function
   :class    :function})

(defn convert-map-values-to-readable-sgr [m]
  (reduce-kv
   (fn [m k v]
     (assoc m k (if (string? v) (state/?sgr-str v) v)))
   {}
   m))

(defn style-from-theme
  ([t]
   (style-from-theme t nil))
  ([t highlighting]
   (let [debug? (state/debug-tagging?)]
     (when debug?
       (pprint (convert-map-values-to-readable-sgr
                {:t            t
                 :highlighting highlighting})))

     (let [highlighting-is-a-map? false
           f #(let [k                (get tagtype->theme-key % %)
                    merged-theme-map (if highlighting-is-a-map?
                                       @state/merged-theme-with-unserialized-style-maps
                                       @state/merged-theme)
                    ret              (k merged-theme-map)]
                (when debug?
                  (println "\n")
                  (println (list 'get 'tagtype->theme-key % %) '=> k)
                  (println (str "\nValue for " k " (" % ") from merged theme: "))
                  (pprint (if (string? ret) (state/?sgr-str ret) ret))
                  (println "\n")
                  (let [ret-alt 
                        (k @state/merged-theme-with-unserialized-style-maps)]
                    (pprint (if (string? ret-alt)
                              (state/?sgr-str ret-alt)
                              ret-alt)))

                  (println "\n"))
                ret)

           m (or (f t) (f :foreground))

           result (if highlighting-is-a-map?
                    (let [m (state/sanitize-style-map highlighting)
                          m (state/with-line-height m)
                          m (state/map-vals state/hexa-or-sgr m)]
                      (state/m->sgr m))
                    (or highlighting m))]
       
       (str result)))))

;; TODO - If neutral theme, don't tag
(defn tag!
  ([t]
   (tag! t nil))
  ([t highlighting]
   (let [metadata-*? (contains? #{:metadata :metadata-key} t)
         coerced-tag (cond
                       metadata-*?              t
                       (= t :type-label-inline) :type-label
                       :else                    t)
         s           (style-from-theme coerced-tag
                                       ;; Don't highlight value if in metadata
                                       (when-not metadata-*? highlighting))]
     (when (state/debug-tagging?)
       (println "style from theme")
       (?sgr s))
     s)))

(def sgr-closing-tag-str "\033[0m")

(defn tag-reset!
  ([]
   (tag-reset! :foreground))
  ([theme-token]
   (when (state/debug-tagging?)
     (println "tag/tag-reset! with \\\033[0m"))
   sgr-closing-tag-str))

(defn tag-entity! 
  ([x t]
   (tag-entity! x t nil))
  ([x t highlighting]
   (str (tag! t) x (tag-reset!))))

(defn tagged
   ([s]
    (tagged s nil))
   ([s
     {:keys [theme-token
             display? 
             highlighting]
      :or   {display?    true
             theme-token :foreground}
      :as m}]

    ;; s is always a string or vector (in the case of fn-args)
    (when (and (or (string? s)
                   (symbol? s)
                   (vector? s))
               display?)

      (let [opening-tag (tag! theme-token highlighting)
            closing-tag (tag-reset!)
            ret         (str opening-tag s closing-tag)]

        (when (state/debug-tagging?)
              (println "\ntag/tagged    :   tagging \"" s "\" with " theme-token) )

        ret))))

