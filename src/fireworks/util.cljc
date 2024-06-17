(ns fireworks.util
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp]]
            [fireworks.config :as config]
            [fireworks.defs :as defs]))

(defn spaces [n] (string/join (repeat n " ")))

(defn badge-type
  "Derived badge type keyword, based on input string.\n
   (badge-type \"#js \") => :literal-label\n
   (badge-type \"λ\") => :lamda-label\n
   (badge-type \"SomeOtherTypeOrClass\") => :type-label"
  [s]
  (cond
    (contains? defs/inline-badges s) :literal-label 
    (= s defs/lamda-symbol)          :lamda-label
    :else                            :type-label))

(defrecord Wrapper [x])

(defn carries-meta? [x]
  #?(:clj  (instance? clojure.lang.IObj x)
     :cljs (satisfies? IWithMeta x)))

(defn readable-sgr [x]
  #?(:cljs x
     :clj (str "\\033" (subs x 1))))

(defn form-meta->file-info
  [{:keys [file line column]}]
  (str (some-> file (str ":")) line ":" column))


(defn string-like? [v]
  (or (string? v)
      #?(:clj (-> v type str (= "java.util.regex.Pattern"))
         :cljs (-> v type str (= "#object[RegExp]")))
      (symbol? v)
      (keyword? v)
      (number? v)))

(defn shortened
  "Stringifies a collection and truncates the result with ellipsis 
   so that it fits on one line."
  [v limit]
  (let [limit  limit
        as-str (str v)]
    (if (> limit (count as-str))
      as-str
      (let [shortened*      (-> as-str
                                (string/split #"\n")
                                first)
            shortened       (if (< limit (count shortened*))
                              (let [ret          (take limit shortened*)
                                    string-like? (string-like? v)]
                                (str (string/join ret)
                                     (when-not string-like? " ")
                                     "..."))
                              shortened*)]
        shortened))))

(defn css-style-string [m]
  (string/join ";"
               (map (fn [[k v]]
                      (str (name k)
                           ":"
                           (if (number? v) (str v) (name v))))
                    m)))

(defn nameable? [x]
  (or (string? x) (keyword? x) (symbol? x)))

(defn as-str [x]
  (str (if (or (keyword? x) (symbol? x)) (name x) x)))

(defn char-repeat [n s]
  (when (pos-int? n)
    (string/join (repeat n (or s "")))))


;; SGR for colorizing terminal output
(defn x->sgr [x k]
  (when x
    (let [n (if (= k :fg) 38 48)]
      (if (int? x)
        (str n ";5;" x)
        (let [[r g b _] x
              ret       (str n ";2;" r ";" g ";" b)]
          ret)))))

(defn m->sgr
  [{fgc*        :color
    bgc*        :background-color
    :keys       [k
                 font-style 
                 font-weight
                 italics?
                 font-weights?
                 debug-on-token?]
    :as         m}]
  (when debug-on-token?
    (println "\n(fireworks.state/m->sgr " m ")"))
  (let [fgc    (x->sgr fgc* :fg)
        bgc    (x->sgr bgc* :bg)
        italic (when (and italics?
                          (contains? #{"italic" :italic} font-style))
                 "3;")
        weight (when (and font-weights?
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
    (when  debug-on-token?
      (?pp "\nCombining the fg and bg into a single sgr..." m)
      (println "=>\n"
               (readable-sgr ret)
               "\n"))
    ret))
