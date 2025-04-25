(ns fireworks.tstring
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [fireworks.core :refer [? !?]]))

;; - No nested paragraphs in paragraphs , no [:p [:p]]

;; - No nested vectors in styling vectors, no [:bold [:red "hi"] [:blue "hi"]]
;;   - they will be converted to strings with pprint

;; - Nothing inside :br vector

;; - Styling tag can be:
;;   #{:b :i}
;;   or
;;   #{:red :blue ...} 
;;   or 
;;   ::coll-of #{:red :blue ...}



;; clj-fstring | ---------------------------------------------------------------

(defn- escaped?
  [s index]
  (if (neg? (- index 1))
    false
    (= "'" (subs s (dec index) index))
    ))

(defn- f-first-brackets-index
  [s]
  (let [curly-open (string/index-of s "{")
        curly-close (string/index-of s "}")]
    (cond
      (and (not curly-open) (not curly-close))
      nil

      (and (some? curly-open) (not curly-close))
      (throw (ex-info "Curly brackets mismatch" {:string s}))

      (and (not curly-open) (some? curly-close))
      (throw (ex-info "Curly brackets mismatch" {:string s}))

      :else [curly-open curly-close ])
    ))

(defn- f-string-prepare
  ([s]
   (f-string-prepare s []))
  ([s acc]
   (if-let [indeces (f-first-brackets-index s)]
     (let [curly-open (first indeces)
           curly-close (last indeces)
           start-s (subs s 0 curly-open)
           rest-s (subs s (inc curly-close))
           sym (subs s (inc curly-open) curly-close)]
       (if (escaped? s curly-open)
         (recur rest-s (concat acc [(string/replace (subs s 0 (inc curly-close)) "'{" "{")]))
         (recur rest-s (concat acc [start-s (edn/read-string sym)]))))
     (concat acc s))))

(defn- f-string-prepare-hiccup
  ([s]
   (f-string-prepare s []))
  ([s acc]
   (if-let [indeces (f-first-brackets-index s)]
     (let [curly-open  (first indeces)
           curly-close (last indeces)
           start-s     (subs s 0 curly-open)
           rest-s      (subs s (inc curly-close))
           sym         (subs s (inc curly-open) curly-close)]
       (if (escaped? s curly-open)
         (recur rest-s (concat acc [(string/replace (subs s 0 (inc curly-close)) "'{" "{")]))
         (recur rest-s (concat acc [start-s (edn/read-string sym)]))))
     (concat acc s))))

;; (defmacro f-str
;;   [s]
;;   `(str ~@(f-string-prepare s)))

;; clj-fstring end | -----------------------------------------------------------




(def sr string/replace)

(def ssplit string/split)

(defn line-is-only-tabs-or-spaces? [s]
  (boolean (re-find #"^[\t ]+$" s)))

(defn replace-leading-tabs-or-spaces-with-stub
  [leading-spaces s]
  (sr s #"^[\t ]+" (sr leading-spaces #"[\t ]" "·")))

(defn replace-leading-tabs-or-spaces-with-empty
  [leading-spaces s]
  (sr s #"^[\t ]+" (sr leading-spaces #"[\t ]" "")))

(defn find-leading-spaces [s]
  (some->> s (re-find #"^([\t ]+)[^\t ]*")))

(defn remove-remaining-tabs-or-spaces-markers [s]
  (sr s #"·" ""))

(defn leading-tabs-or-spaces->f [s f]
  (->> (ssplit s #"\n")
       (map #(if-let [[match leading-spaces]
                      (find-leading-spaces %)]
               (if (line-is-only-tabs-or-spaces? match) "" (f leading-spaces %))
               %))
       (string/join "\n")) )

(defn leading-indent-markers->spaces [s]
  (sr s #"\n\|" (fn [%] "\n ")))

(defn indent-markers->spaces [s]
  (sr s #"(\n·+)\|"
      (fn [%] (str (nth % 1 nil) " "))))

(defn remove-leading-dollar-curly [s]
 (sr s #"^\$\{" "{"))

(defn remove-all-dollar-curly [s]
 (sr s #"([^'])\$\{" #(str (second %) "{")))

(defn preformat
  ([s]
   (preformat s nil))
  ([s {:keys [show-leading-spaces?]}]
   (if show-leading-spaces?
     (-> s
         (leading-tabs-or-spaces->f replace-leading-tabs-or-spaces-with-stub)
         indent-markers->spaces)
     (-> s
         (leading-tabs-or-spaces->f replace-leading-tabs-or-spaces-with-empty)
         leading-indent-markers->spaces))))

(defmacro t-string [s]
  (let [pf (if (string/index-of s "\n")
             (preformat s)
             s)
        pf (-> pf
               remove-leading-dollar-curly
               remove-all-dollar-curly)]
    `(str ~@(f-string-prepare pf))))

(defn hiccupized-tag-as-str [s tag]
  (!? (str "[:" (-> tag (sr #"^<-" "")) " " s "]")))

(defn dbl-quote [s]
 (str "\"" s "\"") )

(defn dbl-quote-w-spaces [s re]
  (->> s
       (re-find re)
       second
       dbl-quote))

#_(-> the-str
    (dbl-quote-w-spaces  #" ([^ ]+)$")
    (hiccupized-tag-as-str (nth styles i)))

(defn hiccupized-tags-as-strs [strs styles]
  (doall (map-indexed
          (fn [i the-str]
            (if (re-find #" " the-str)
              (let [untagged-part
                    (if (re-find #"\$" the-str)
                      (let [coll
                            (->> the-str
                                 remove-leading-dollar-curly
                                 remove-all-dollar-curly
                                 f-string-prepare)

                            ret
                            (!? (cons 'str (drop-last coll)))
                            ]
                        ret)
                      (-> the-str
                          (dbl-quote-w-spaces #"(.* +)[^ ]+$")))
                    tagged-part
                    (if (re-find #"\$" the-str)
                      (let [coll
                            (->> the-str
                                 remove-leading-dollar-curly
                                 remove-all-dollar-curly
                                 f-string-prepare)

                            tagged
                            (!? (hiccupized-tag-as-str
                                 (cons 'str (take-last 1 coll))
                                 (nth styles i) ))

                            untagged
                            (!? (cons 'str (drop-last coll)))

                            ret
                            (!? (str untagged " " tagged))]
                        ret)
                      (-> the-str
                          (dbl-quote-w-spaces  #" ([^ ]+)$")
                          (hiccupized-tag-as-str (nth styles i)))
                      )]
                (str (!? untagged-part)
                     (!? tagged-part)))
              (dbl-quote the-str)))
          strs)))

(def trailing-bling-tag-re #"<-(?:[a-z]\.*)+\b")

(defn trailing-tags->hiccup [s]
  (let [strs   (ssplit s trailing-bling-tag-re)
        styles (re-seq trailing-bling-tag-re s)]
    (hiccupized-tags-as-strs strs styles)))


(defmacro t-string-bling [s]
  (let [preformatted
        (if (string/index-of s "\n") (preformat s) s)

        trailing-tags-hiccupized
        (-> preformatted
            trailing-tags->hiccup
            !?
            string/join)

        #_with-vars
        #_(? (as-> trailing-tags-hiccupized $
            ;;  (remove-leading-dollar-curly $)
            ;;  (remove-all-dollar-curly $)
             (str "[" $ "]")
            ;;  (edn/read-string $)
          ))
        ]
    ;; (walk/postwalk #(!? % (type %)) pf)
    ;; `:hi
    ;; `(str ~@(f-string-prepare with-vars))
    ))


;; not sure if this is related

(def sam
  "One two three,
   four five six

   Blank line<br>
   next one<br>
          
   Blank line<br>
   next one



   Last one.<br>")


(def foo "BOB")

;; (? (t-string "Hi there whats ${foo} up?"))


;; TODO
;; Add ansi formatting support
;; Add %c formatting support
;; Add hicuup support?
;; 


;; (? (t-string-bling "Hi<-gold.bold.italic ${baz}here ${foo}<-bold.red.italic"))

;; (!? (t-string->bling "${foo}<-bold.red.italic up? or ${foo}bold.red.italic down"))


;; (? (t-string "Hi there whats up

;;               next line
;;               just a {foo}

;;               \|    4 space indent

;;               next line."))


