(ns ^:dev/always fireworks.tag
  (:require [clojure.string :as string]
            [fireworks.state :as state]))



(def tagtype->theme-key
  {:fn-args         :function-args
   :defmulti        :function
   :java.lang.Class :function})

(defn style-from-theme
  ([t bgc]
   (style-from-theme t bgc nil))
  ([t bgc custom-badge-style]
   (let [f #(let [k (get tagtype->theme-key % %)]
              (k (if custom-badge-style
                   @state/merged-theme-with-unserialized-style-maps
                   @state/merged-theme)))

         m (or (f t) (f :foreground))
         m (if custom-badge-style
             (let [m (state/sanitize-style-map custom-badge-style)
                   m (state/with-line-height m)]
               #?(:cljs
                  (string/join (map state/kv->css2 m))
                  :clj
                  (let [m (state/map-vals state/hexa-or-sgr m)]
                    (state/m->sgr m))))
             m)]
     (str m bgc))))

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
     #?(:cljs (let [s (cond
                        (= t :type-label)
                        (str s)
                        ;; (= t :metadata)
                        ;; (str s "")
                        :else
                        s)]
                (swap! state/styles conj s)
                "%c")
        :clj s))))

(defn tag-reset!
  ([]
   (tag-reset! :foreground))
  ([theme-token]
   #?(:cljs (let [theme-token (or theme-token :foreground)]
              (when (state/debug-tagging?)
                (println "tag/tag-reset!  with  " theme-token))
              (swap! state/styles
                     conj
                     (-> @state/merged-theme theme-token))
              "%c")
              :clj "\033[0m")))

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

