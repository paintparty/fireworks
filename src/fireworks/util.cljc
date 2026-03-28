(ns fireworks.util
  (:require [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [fireworks.defs :as defs]
            [clojure.set :as set]
            [lasertag.fns :as fns]
            [lasertag.core :as lasertag]))

(defn ? 
  "Debugging macro internal to lib"
  ([x]
   (? nil x))
  ([l x]
   (try (if l
          (println (str " " l "\n") x)
          (println x))
        (catch #?(:cljs js/Object :clj Throwable)
               e
          (println "WARNING [lasertag.core/?] Unable to print value")))
   x))

(defn spaces [n] (string/join (repeat n " ")))

(defn badge-type
  "Derived badge type keyword, based on input string.\n
   (badge-type \"#js \") => :literal-label\n
   (badge-type \"λ\") => :lambda-label\n
   (badge-type \"SomeOtherTypeOrClass\") => :type-label"
  [s]
  (cond
    (contains? defs/inline-badges s) :literal-label 
    (= s defs/lambda-badge)          :lambda-label
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

(defn nameable? [x]
  (or (string? x) (keyword? x) (symbol? x)))

(defn as-str [x]
  (str (if (or (keyword? x) (symbol? x)) (name x) x)))

(defn css-stylemap->str [m]
 (reduce-kv (fn [acc k v]
               (if (and k v)
                 (str acc (as-str k) ":" (as-str v) ";")
                 acc))
             ""
             m))


(defn char-repeat [n s]
  (when (pos-int? n)
    (string/join (repeat n (or s "")))))


(defn ^:public maybe->
  "If `(= (pred x) true)`, returns x, otherwise nil.
   Useful in a `clojure.core/some->` threading form."
  [x pred]
  (when (true? (pred x)) x))


(defn ^:public maybe->>
  "If (= (pred x) true), returns x, otherwise nil.
   Useful in a `clojure.core/some->>` threading form."
  [pred x]
  (when (true? (pred x)) x))


(defn last-word [s]
  (some-> s
          (maybe-> string?)
          (subs (inc (.lastIndexOf s " ")))))

(defn- java-util-class? [s]
  (boolean (some-> s (string/starts-with? "java.util"))))

(defn- java-lang-class? [s]
  (boolean (some->> s (re-find #"java\.lang"))))

(defn tag-map*
  ([x]
   (tag-map* x nil))
  ([x opts]
   (let [{:keys [all-tags classname t]
          :as   tag-map}
         (-> x
             (lasertag/tag-map opts)
             (set/rename-keys {:tag :t}))]
     (merge tag-map
            (when (contains? #{:function :class :defmulti} t)
              (fns/fn-info x t))
            (when (contains? all-tags :carries-meta) {:carries-meta? true})
            (when (or (contains? all-tags :coll-type) ; <- deprecate in lasertag
                      (contains? all-tags :coll-like))
              {:coll-type? true})
            (when (contains? all-tags :map-like) {:map-like? true})
            (when (contains? all-tags :set-like) {:set-like? true})
            (when (contains? all-tags :transient) {:transient? true})
            (when (contains? all-tags :number) {:number-type? true})
            #?(:cljs nil
               :clj (when (some->> classname java-lang-class?)
                      {:java-lang-class? true}))
            #?(:cljs nil
               :clj (when (some->> classname java-util-class?)
                      {:java-util-class? true}))
            #?(:cljs (merge (when (object? x) {:js-object? true})
                            (when (array? x) {:js-array? true})))))))

(defn re-seq-with-index 
"Example usage
 (re-seq-with-index #\"\\d+\" \"abc123def456ghi\")
 => [{:match \"123\", :start 3, :end 6}
     {:match \"456\", :start 9, :end 12}]"
  [pattern string]
  #?(:cljs
     (let [matcher (re-pattern pattern)]
       (loop [matches    []
              last-index 0]
         (let [match (.exec matcher string)]
           (if (and match (>= (.-index match) last-index))
             (recur (conj matches {:match  (first match)
                                   :start  (.-index match)
                                   :end    (+ (.-index match) (count (first match)))
                                   :groups (vec (rest match))})
                    (+ (.-index match) (count (first match))))
             matches))))
     :clj
     (let [matcher (re-matcher pattern string)]
       (loop [matches []]
         (if (.find matcher)
           (recur (conj matches {:match (.group matcher)
                                 :start (.start matcher)
                                 :end   (.end matcher)}))
           matches)))))

(defn interleave-all [& colls]
  (lazy-seq
    (when (some seq colls)
      (concat (keep first colls)
              (apply interleave-all (map rest colls))))))

(defn insert-at [vc i elem]
  (into (conj (subvec vc 0 i) elem)
        (subvec vc i)))
