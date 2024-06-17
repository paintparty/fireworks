(ns fireworks.util
  (:require [clojure.string :as string]
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

