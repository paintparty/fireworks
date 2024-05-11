(ns ^:dev/always fireworks.tag
  (:require
   [fireworks.pp :refer [?pp]]
   [clojure.string :as string]
   [fireworks.defs :as defs]
   [fireworks.state :as state]
   [fireworks.util :refer [spaces]]
   #?(:cljs [fireworks.macros :refer-macros [keyed]])
   #?(:clj [fireworks.macros :refer [keyed]])))



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
                             custom-badge-style)
        ;;  _ (when (= t :metadata-key) (?pp s))
         ]
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
      :or   {display? true
             theme-token :foreground}}]

    ;; s is always a string or vector (in the case of fn-args)
    (when (and (or (string? s)
                   (symbol? s)
                   (vector? s))
               display?)
      (let [opening-tag (tag! theme-token highlighting custom-badge-style)
            closing-tag (tag-reset!)]
        (str opening-tag s closing-tag)))))


;; TODO NIX this
(defn- metamap-offset-background [l]
  (str (tagged " " {:theme-token :foreground})
       (tagged (subs l 1) {:theme-token :metadata})))


;; TODO NIX this
(defn- metamap-offset-dashes [l]
  (str (tagged " "
               {:theme-token :foreground})
       (tagged (string/join (take (dec (count l)) (repeat "-")))
               {:theme-token :metadata-offset})))

;; TODO nix this
(defn- multi-line-meta-map 
  [{:keys [user-meta
           inline-offset
           indent
           str-len-with-badge
           block?
           optional-caret-char] }]
  (let [ret       (string/replace 
                   (with-out-str 
                     (binding [*print-level*
                               (:metadata-print-level @state/config)]
                       (fireworks.pp/pprint user-meta)))
                   #"\n$" "")
        lines     (string/split-lines ret)
        w-indent* (interleave 
                   (map-indexed (fn [i _]
                                  (if (zero? i)
                                    inline-offset
                                    (str "\n"
                                         (spaces indent)
                                         (when-not block?
                                           (spaces str-len-with-badge))
                                         inline-offset)))
                                lines)
                   (if optional-caret-char
                     (map-indexed (fn [i v]
                                    (str (if (zero? i)
                                           optional-caret-char
                                           " ")
                                         v))
                                  lines)
                     lines))
        w-indent  (map-indexed (fn [i l]
                                 (let [theme-token (cond
                                                     (zero? i) :metadata
                                                     (even? i) :foreground
                                                     :else     :metadata)]
                                   (if (zero? i) 
                                     #_(metamap-offset-dashes l) ;; alt style
                                     (when inline-offset (metamap-offset-background l))
                                     (tagged l {:theme-token theme-token}))))
                               w-indent*)]
    (string/join w-indent)))


;; TODO nix this
(defn stringified-user-meta
  [{:keys [user-meta
           metadata-position
           indent
           str-len-with-badge
           sev?]}]
  (let [block?              (contains? #{"block" :block} metadata-position)
        optional-caret-char (when block? "^") 
        stringified         (str optional-caret-char user-meta)
        multi-line?         (< (:non-coll-length-limit @state/config)
                               (count stringified))
        inline-offset       (when-not block?
                              (spaces defs/metadata-position-inline-offset))
        ret                 (if multi-line?
                              (multi-line-meta-map
                               (keyed [user-meta
                                       inline-offset 
                                       indent
                                       str-len-with-badge
                                       optional-caret-char
                                       block?]))
                              (str
                               (when-not block?
                                 (metamap-offset-background inline-offset))
                               (tagged (str stringified)
                                       {:theme-token :metadata})))]
    (str ret
         (when block? (str (when sev? "\n") (spaces indent))))))
