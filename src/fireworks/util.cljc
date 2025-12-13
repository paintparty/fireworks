(ns fireworks.util
  (:require [clojure.string :as string]
            [fireworks.defs :as defs]
            [clojure.set :as set]
            [lasertag.core :as lasertag]))

(defn spaces [n] (string/join (repeat n " ")))

(defn badge-type
  "Derived badge type keyword, based on input string.\n
   (badge-type \"#js \") => :literal-label\n
   (badge-type \"λ\") => :lambda-label\n
   (badge-type \"SomeOtherTypeOrClass\") => :type-label"
  [s]
  (cond
    (contains? defs/inline-badges s) :literal-label 
    (= s defs/lambda-symbol)          :lambda-label
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


(defn maybe [x pred]
  (when (if (set? pred)
          (contains? pred x)
          (pred x))
    x))

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

(defn tag-map*
  ([x]
   (tag-map* x nil))
  ([x opts]
   (let [{:keys [all-tags]
          :as   tag-map}
         (-> x
             (lasertag/tag-map opts)
             (set/rename-keys {:tag :t}))]
     (merge tag-map
            (when (contains? all-tags :carries-meta) {:carries-meta? true})
            (when (contains? all-tags :coll-type) {:coll-type? true})
            (when (contains? all-tags :map-like) {:map-like? true})
            (when (contains? all-tags :set-like) {:set-like? true})
            (when (contains? all-tags :transient) {:transient? true})
            (when (contains? all-tags :number-type) {:number-type? true})
            (when (contains? all-tags :java-lang-class) {:java-lang-class? true})
            (when (contains? all-tags :java-util-class) {:java-util-class? true})
            #?(:cljs (merge (when (object? x) {:js-object? true})
                            (when (array? x) {:js-array? true})))))))

