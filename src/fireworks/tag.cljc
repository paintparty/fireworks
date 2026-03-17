(ns ^:dev/always fireworks.tag
  (:require
   [fireworks.util :as util]
   [clojure.string :as string]
   [fireworks.pp :refer [pprint]]
  ;;  [fireworks.pp :refer [pprint ?pp] :rename {?pp ?}]
   [fireworks.state :as state :refer [?sgr]]))


(def tagtype->theme-key
  {:defmulti :function
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
                    ;; TODO - use state/style-map->sgr
                    (let [m (state/sanitize-style-map highlighting)
                          m (state/with-line-height m)
                          m (state/map-vals state/hexa-or-sgr m)]
                      (state/m->sgr m))
                    (or highlighting m))]
       
       (str result)))))


;; TODO - If neutral theme, don't tag
;; TODO - rename to sgr-tag, as it does not side effect anymore 
(defn sgr-tag
  ([t]
   (sgr-tag t nil))
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


(def sgr-reset-tag "\033[0m")

;; TODO - remove this and just use sgr-closing-tag-str, rename to sgr-reset-tag
(defn reset-tag
  ([]
   (reset-tag :foreground))
  ([theme-token]
   (when (state/debug-tagging?)
     (println "tag/reset-tag with \\\033[0m"))
   sgr-reset-tag))


(defn tag-entity 
  ([x t]
   (tag-entity x t nil))
  ([x t highlighting]
   (str (sgr-tag t) x sgr-reset-tag)))


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

    (when (and (or (string? s)
                   (symbol? s)
                   (vector? s)
                   (char? s))
               display?)
      (let [opening-tag (sgr-tag theme-token highlighting)
            closing-tag sgr-reset-tag
            ret         (str opening-tag s closing-tag)]
        (when (state/debug-tagging?)
              (println "\ntag/tagged    :   tagging \"" s "\" with " theme-token))
        ret))))


;; Regex tagging ---------------------------------------------------------------


(defn- re-seq-indexes [re s]
  (->> s
       (util/re-seq-with-index re)
       (reduce (fn [acc m]
                 (apply conj acc (range (:start m) (:end m))))
               #{})))


(defn- re-seq-indexes-with-capture-groups 
  [re s k]
  (let [debug? false #_(and (= k :group-mods-indexes)
                    (= s "#\"((?:(?!"))
        vc (reduce (fn [vc {:keys [match start]
                            :as   m}]
                     (let [[_ needle]        (re-find re match)
                           start-plus-needle (+ start (count needle))
                           start-plus-one    (inc start)
                           new-m             (assoc m
                                                    :start
                                                    start-plus-one
                                                    :end 
                                                    start-plus-needle)]
                       #_(?pp (keyed [re needle m start-plus-needle new-m]))
                       (conj vc new-m)))
                   []
                   (util/re-seq-with-index re s))]
    (reduce (fn [acc m]
              (apply conj acc (range (:start m) (:end m))))
            #{}
            vc)))

(defrecord RegexPart [s theme-token escaped?])


(defn- regex-char-seq-with-ansi-sgr-tags [%]
  (let [sp                          (string/split % #"")
        char-range-indexes          (re-seq-indexes #"[a-zA-Z]-[a-zA-Z]" %)
        number-range-indexes        (re-seq-indexes #"[0-9]-[0-9]" %)
        not-any-of-indexes          (re-seq-indexes-with-capture-groups #"(\[\^)" % :not-any-of-indexes)
        ;; TODO test this group mods thing more thoroughly
        group-mods-indexes          (re-seq-indexes-with-capture-groups #"(\(\?(?:\:|\=|\!|\<\=|\<\!|\<[^\>]*\>))[^\)\(]*" % :group-mods-indexes)
        numeric-quantifiers-indexes (re-seq-indexes-with-capture-groups #"(.\{[0-9]+(?:,|,[0-9]+)?})" % :numeric-quantifiers-indexes)
        with-ansi-sgr-tags          (map-indexed 
                                     (fn [i s]
                                       (let [token  
                                             (cond (contains? group-mods-indexes i)
                                                   :regex.group-mods

                                                   (contains? numeric-quantifiers-indexes i)
                                                   :regex.numeric-quantifier

                                                   (contains? not-any-of-indexes i)
                                                   :regex.not-any-of-delimeter

                                                   (contains? char-range-indexes i)
                                                   :regex.character-range

                                                   (contains? number-range-indexes i)
                                                   :regex.number-range

                                                   (re-find #"^[\[\]]$" s)
                                                   :regex.any-of-delimeter

                                                   (re-find #"^[\(\)]$" s)
                                                   :regex.group-delimeter

                                                   (re-find #"^[\+\*\?]$" s) 
                                                   :regex.quantifier

                                                   (re-find #"^[\^\$]$" s)
                                                   :regex.anchor

                                                   (= s "|")
                                                   :regex.alternation

                                                   :else
                                                   :regex.character)]
                                         (->RegexPart s token false)))
                                     sp)]
    with-ansi-sgr-tags))


(defn tagged-escaped-chars
  [[s]]
  (if (contains? #{"\\s" 
                   "\\S" 
                   "\\d" 
                   "\\D" 
                   "\\w" 
                   "\\W" 
                   "\\t" 
                   "\\n" 
                   "\\cI" 
                   "\\v" 
                   "\\f" 
                   "\\r" 
                   "\\0"}
                 s)
    [(->RegexPart s :regex.special-character true)]
    (conj (->> s
               drop-last 
               (map-indexed
                (fn [i s]
                  (->RegexPart s 
                               (if (even? i)
                                 :regex.escape-backslash
                                 :regex.character)
                               true)))
               vec)
          (-> s
              last
              (->RegexPart :regex.character true)))))


(defn- theme-token-with-highlighting 
  [m nesting]
  (let [theme-token    (:theme-token m)
        neutral-regex? (= :neutral (:regex-theme @state/config))
        highlight      (peek @nesting)]
    (cond
      highlight
      (assoc m
             :theme-token
             (keyword 
              (str (name theme-token)
                   (if (= highlight "(") ".in-group" ".in-any-of")
                   (when neutral-regex? ".neutral"))))

      neutral-regex?
      (assoc m
             :theme-token
             (keyword (str (name theme-token) ".neutral")))

      :else
      m)))


(defn- with-groups-highlighted 
  [nesting
   acc 
   {:keys [s escaped?]
    :as   m}] 
  (when (and (contains? #{"[" "("} s) (not escaped?))
    (swap! nesting conj s))
  (let [m+ (theme-token-with-highlighting m nesting)]
    (when (and (contains? #{"]" ")"} s) (not escaped?))
      (swap! nesting pop))
    (str acc (tagged s m+))))


;; TODO - replace this with dynamic application of bling tags, then remove
;; All the extra regex tokens from the theme

(defn colorized-regex [s]
  #?(:cljs
     s
     :clj
     (let [escapes-re #"(\\+[^\\])"
           escaped    (->> s
                           (re-seq escapes-re)
                           (mapv tagged-escaped-chars))
           split      (mapv regex-char-seq-with-ansi-sgr-tags 
                            (string/split s escapes-re))      
           leaved     (util/interleave-all split escaped)
           nesting    (atom [])]
       (reduce (partial with-groups-highlighted nesting)
               ""
               (apply concat leaved)))))
