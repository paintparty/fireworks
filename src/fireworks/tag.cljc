(ns ^:dev/always fireworks.tag
  (:require [clojure.string :as string]
            [fireworks.macros :refer [keyed]]
            [fireworks.pp :refer [?pp pprint]]
            #?(:cljs [fireworks.macros :refer-macros [keyed]])
            #?(:clj [fireworks.state :as state :refer [?sgr]]
               :cljs [fireworks.state :as state :refer [node? mock-node?]])))


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
     #_(when debug?
         (pprint (convert-map-values-to-readable-sgr
                  (keyed [t highlighting]))))

     (let [highlighting-is-a-map? false
           f #(let [k                (get tagtype->theme-key % %)

                    merged-theme-map (if highlighting-is-a-map?
                                       @state/merged-theme-with-unserialized-style-maps
                                       @state/merged-theme)

                    ret              (k merged-theme-map)

                  ;;  ret-alt          (k @state/merged-theme-with-unserialized-style-maps)
                    ]
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

           result (if highlighting-is-a-map?
                    (let [m (state/sanitize-style-map highlighting)
                          m (state/with-line-height m)
                          f #(let [m (state/map-vals state/hexa-or-sgr m)]
                               (state/m->sgr m))]
                      #?(:cljs
                         (if (or node? @mock-node?) (f) (string/join (mapv state/kv->css2 m)))
                         :clj
                         (f)))
                    (or highlighting m))]
       
       (str result)))))

(defn tag!
  ([t]
   (tag! t nil))
  ([t highlighting]
   (let [metadata-*? (contains? #{:metadata :metadata-key} t)
         s           (style-from-theme (cond
                                         metadata-*?
                                         t

                                         (= t :type-label-inline)
                                         :type-label

                                         :else
                                         t)
                                       ;; Don't highlight value if in metadata
                                       (when-not metadata-*? highlighting))]

     (when (state/debug-tagging?)
       #?(
          ;; :cljs (if (or node? @mock-node?)
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

     #?(:cljs (if (or node? @mock-node?)
                s
                ;; TODO - lose if post-replace works
                (let [s (cond
                          (= t :type-label)
                          s
                          ;; (= t :metadata)
                          ;; (str s "")
                          :else
                          s)]
                  (when (state/debug-tagging?)
                    (println "tag/tag!  with  " s))
                  (swap! state/styles conj s)
                  "%c"))
        :clj s))))

(def sgr-closing-tag-str "\033[0m")

(defn tag-reset!
  ([]
   (tag-reset! :foreground))
  ([theme-token]
   #?(:cljs (if (or node? @mock-node?)
              (do (when (state/debug-tagging?)
                    (println "tag/tag-reset!  with  \\\033[0m"))
                  sgr-closing-tag-str)

              ;; TODO - lose if post-replace works
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

          ;; TODO -  if post-replace works
          sgr-closing-tag-str)
      )))

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

  ;;  #?(:cljs (pprint m))

   #_(when highlighting
     (println "has highlighting: " s))

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

