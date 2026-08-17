(ns fireworks.util
  (:require [clojure.string :as str]
            [fireworks.defs :as defs]
            ;; [fireworks.pp :refer [?pp] :rename {?pp ?}]
            [clojure.set :as set]
            [lasertag.fns :as fns]
            [lasertag.cached]
            [lasertag.core :as lasertag]))

(defn spaces [n] (str/join (repeat n " ")))

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
                                (str/split #"\n")
                                first)
            shortened       (if (< limit (count shortened*))
                              (let [ret          (take limit shortened*)
                                    string-like? (string-like? v)]
                                (str (str/join ret)
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
    (str/join (repeat n (or s "")))))


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
  (boolean (some-> s (str/starts-with? "java.util"))))

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

            ;; This is where we get the coll-size or collection size
            (when (contains? all-tags :coll-like)
              {:coll-type? true
               :coll-size  (lasertag.core/coll-size* (assoc tag-map :x x))})
            (when (contains? #{:function :class :defmulti} t) (fns/fn-info x t))

            (when (and (contains? all-tags :map-like)
                       (contains? all-tags :datatype)) 
              {:object-like-datatype? true})
            ;; TODO - try to eliminate these and look into all-tags instead
            (when (contains? all-tags :carries-meta) {:carries-meta? true})
            (when (contains? all-tags :map-like) {:map-like? true})
            (when (contains? all-tags :set-like) {:set-like? true})
            (when (contains? all-tags :transient) {:transient? true})
            (when (contains? all-tags :number) {:number-type? true})
            (when (contains? all-tags :lambda) {:lambda? true})
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


(defn longer-than?
  "Returns true if `coll` has more than `n` elements.

  Avoids fully realizing lazy sequences — walks at most (inc n) elements.

  Examples:
    (longer-than? 5 (range 3))   ;=> false
    (longer-than? 5 (range 5))   ;=> false
    (longer-than? 5 (range 6))   ;=> true
    (longer-than? 5 (range 1e9)) ;=> true"
  [n coll]
  (some? (nth coll n nil)))

(defn- idx-of-vector-bracket
  "Index of the ] closing the [ at `open`, or nil if unbalanced/truncated."
  [s open]
  (let [n (count s)]
    (loop [i       (inc open)
           depth   1
           in-str? false]
      (when (< i n)
        (let [c (nth s i)]
          (cond
            (= c \\)  (recur (+ i 2) depth in-str?) ; escape or char literal
            in-str?   (recur (inc i) depth (not= c \"))
            (= c \")  (recur (inc i) depth true)
            (= c \[)  (recur (inc i) (inc depth) false)
            (= c \])  (if (= depth 1) i (recur (inc i) (dec depth) false))
            :else     (recur (inc i) depth false)))))))

(defn- short-classname
  "Returns last segment of classname.
   \"clojure.lang.Delay\" => \"Delay\""
  [s]
  (let [short-name (-> s (str/split #"\.") last)]
    (if-let [f (second (re-find #"^([a-z-]+?)(?:_call)?\$reify" short-name))]
      (str/capitalize f)
      short-name)))

(defn- de-noise-one
  "Maybe de-noise a single #object[...]"
  [inner]
  (let [[classname rest*] (let [i (str/index-of inner " ")]
                            (if i [(subs inner 0 i) (subs inner (inc i))] [inner ""]))
        rest*             (str/replace rest* #"^0x[0-9a-f]+ ?" "")
        nm                (some->> classname short-classname (str "#"))
        space             " "]
    (cond
      (str/starts-with? rest* "{")
      (str nm space rest*)

      (str/starts-with? rest* "\"")
      (cond
        ;; If just identity noise e.g., `java.lang.Object@1c655221`, drop it.
        (str/starts-with? rest* (str "\"" classname "@"))
        (str nm)
        :else
        (str nm space "\"" (subs rest* 1 (-> rest* count (- 1))) "\""))

      :else nil)))

(def ^:private obj-prefix "#object[")
(def ^:private obj-prefix-count (count obj-prefix))

(defn- de-noise-str
  "Makes printed representations of objects easier to read.
   Works across clj, bb, and cljs.
   Example:
   #object[clojure.lang.Atom 0x1 {:a 1}] => #Atom{:a 1}"
  [s]
  (if-not (str/index-of s obj-prefix)
    s
    (loop [s    s
           from 0]
      (if-let [i (str/index-of s obj-prefix from)]
        (if-let [j (idx-of-vector-bracket s (+ i (dec obj-prefix-count)))]
          (if-let [de-noised (de-noise-one (subs s (+ i obj-prefix-count) j))]
            (recur (str (subs s 0 i) de-noised (subs s (inc j)))
                   (+ i (count de-noised)))
            (recur s (+ i obj-prefix-count)))
          s)
        s))))


(defn- truncated-seq 
  "Truncates a seq without realizing it.
   
   Optional trucation label or symbol is
   appended to result (useful for printing).
   (truncated-seq 3 '(1 2 3 4 5) '...) => '(1 2 3 ...)"
  ([n x]
   (truncated-seq n x nil))
  ([n x label]
   (longer-than? n x)
   (if (longer-than? n x)
     (concat (take n x) [label])
     (take n x))))


(defn ^:no-doc safe-str
  "Pass any value and safely get a print representation.

   Optional truncation and ellipsis.

   You can pass options to get a substring as
   well, with guarded index-out-of-bounds checks.

   Unprintable custom objects that would normally throw an AbstractMethodError
   in clojure are returned as (str \"<Print Error: \" (.getMessage e) \">\"),
   or equivalent for target platform"
  ([x]
   (safe-str x nil))
  ([x
    {:keys [start end print-length print-level ellipsis?] 
     :or   {start        0
            end          100
            print-length 10 ;<- max length of colls
            print-level  10
            ellipsis?    false}}]
   (try (binding [*print-length* print-length
                  *print-level*  print-level]
          (let [lazy-seq?  (lasertag.cached/lazyish-seq? x)
                s          (pr-str (if lazy-seq?
                                     ;; TODO - util fn (seq-overflow?) to compute '...+5,
                                     ;; How to make safe for infinite, etc
                                     (truncated-seq print-length x '...)
                                     x))
                len        (count s)
                start+     (max 0 (min start len))
                end+       (max start+ (min end len))
                target-len (- end+ start+)
                overflows? (> len end+)]
            (if (and ellipsis? overflows? (>= target-len 3))
                (de-noise-str (str (subs s start+ (- end+ 3)) "..."))
                (de-noise-str (subs s start+ end+)))))

        ;; Catch the missing impl error for each platform
        (catch #?(:clj AbstractMethodError :cljs js/TypeError) _
          (str "<Unprintable Custom Type: " 
               #?(:clj  (.getName (class x))
                  :cljs (if-let [t (type x)] (.-name t) "Unknown")) 
               ">"))

        ;; Catch standard exceptions for other printing failures
        (catch #?(:clj Exception :cljs js/Error) e
          (str "<Print Error: " 
               #?(:clj  (.getMessage e)
                  :cljs (.-message e)) 
               ">")) )))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Raw datatype objects start 

(defn object-fields 
  "If the given object instance has one or more named fields declared in its
   underlying structure (across JVM, CLJS, and Babashka), returns a list of
   those names."
  [obj]
  (if (nil? obj)
    false
    #?(:bb   nil
       :clj  (.getDeclaredFields (.getClass obj))
       :cljs (when (seq (.getBasis (.-constructor obj)))
               (keys (into {} obj))))))

(defn object-field-count
  "Returns true if the given object instance has one or more named fields 
   declared in its underlying structure across JVM, CLJS, and Babashka."
  [obj]
  (if (nil? obj)
    false
    #?(:clj  (some-> obj object-fields alength)
       :cljs (alength (object-fields obj)))))

(defn datatype->map 
  "Turns a raw datatype with declared fields into a map.
   If no declared fields are detected, just returns the object.
   Set :skip-declared-fields check to `true` to skip check for perf."
  ([obj]
   (datatype->map obj nil))
  ([obj {:keys [skip-object-has-fields-check?]
         :or   {skip-object-has-fields-check? false}}]
   (if-not (or skip-object-has-fields-check?
               (pos? (object-field-count obj)))
     obj
     #?(:bb
        {}

        ;; => (:a :b)
        :clj
        ;; Standard Clojure JVM path
        (->> (.getDeclaredFields (.getClass obj))
             (reduce (fn [m field]
                       (.setAccessible field true)
                       (assoc m (symbol (.getName field)) (.get field obj)))
                     {}))

        :cljs
        ;; ClojureScript path
        (->> (js/Object.keys obj)
             (reduce (fn [m k]
                       (assoc m (keyword k) (aget obj k)))
                     {}))))))

;; Raw datatype objects end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
