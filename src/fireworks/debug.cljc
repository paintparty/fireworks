(ns fireworks.debug
  (:require [clojure.string :as str]
            [fireworks.pp :refer [pprint]])
  #?(:cljs (:require-macros [fireworks.debug :refer [?]])))


(defn- regex? [v]
  (-> v type str (= "class java.util.regex.Pattern")))

(defn- surround-with-quotes [x]
  (str "\"" x "\""))

(defn shortened
  "Stringifies a collection and truncates the result with ellipsis 
   so that it fits on one line."
  [v limit]
  (let [as-str         (str v)
        regex?         (regex? v)
        double-quotes? (or (string? v) regex?)
        regex-pound    (when regex? "#")]
    (if (> limit (count as-str))
      (if double-quotes?
        (str regex-pound (surround-with-quotes as-str))
        as-str)
      (let [ret* (-> as-str
                     (str/split #"\n")
                     first)
            ret  (if (< limit (count ret*))
                   (let [ret (->> ret*
                                  (take limit)
                                  str/join)]
                     (str (if double-quotes?
                            (str regex-pound (surround-with-quotes ret))
                            ret)
                          (when-not double-quotes? " ")
                          "..."))
                   ret*)]
        ret))))

;; TODO - Add some color choices here and italic toggle
(defn- accented [s]
  ;; blue italic
  (str "\033[3;34m" s "\033[0m"))

(defn- ns+ln+col-str
  [form-meta]
  (let [{:keys [line column]} form-meta
        ns-str                (some-> *ns*
                                      ns-name
                                      str
                                      (str ":" line ":" column))
        ns-str                (do 
                                (accented ns-str)
                                )]
    ns-str))


;; TODO - Make variadic - add composable flags and options map to this
(defmacro ? 
    "Prints the value and returns the value."
    ([])
    ([x]
     (let [ns-str (ns+ln+col-str (meta &form))]
       `(do
          (println
           (str "\n"
                (str ~ns-str "\n")
                (str (shortened (quote ~x) 25) "\n")
                (with-out-str (pprint ~x))))
          ~x)))
    ([label x]
     (let [label  (accented (or (:label label) label))
           ns-str (ns+ln+col-str (meta &form))]
       `(do
          (println
           (str "\n"
                (str ~ns-str "\n")
                (str ~label "\n")
                (with-out-str (pprint ~x))))
          ~x))))
