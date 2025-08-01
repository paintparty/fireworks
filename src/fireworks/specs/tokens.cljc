(ns fireworks.specs.tokens
  (:require 
   [clojure.string :as string]
   [expound.alpha :as expound]  
   [fireworks.defs :as defs]
   [clojure.spec.alpha :as s]))

(def bling-color-names-set
  (let [ks (keys defs/bling-colors*)]
    (into #{} 
          (concat ks
                  (mapv keyword ks)))))

(def css-hex-regex
  #?(:clj
     #"(?i)[a-fA-F\d]{3}(?:[a-f\d]?|(?:[a-f\d]{3}(?:[a-f\d]{2})?)?)\b"
     :cljs 
     (js/RegExp.
      "[a-fA-F\\d]{3}(?:[a-f\\d]?|(?:[a-f\\d]{3}(?:[a-f\\d]{2})?)?)\\b" "i")))

(s/def ::css-hex 
 (s/and
  #(when (or (string? %) (keyword? %))
    (re-find css-hex-regex 
             (if (keyword? %) (name %) %)))))

(expound/defmsg ::css-hex
  (str "A string or keyword representation of a valid css hex code"))

(s/def ::xterm-color-id
 (s/and int? #(< 15 % 256)))

(expound/defmsg ::xterm-color-id
  (str "A integer representing the numerical id of an xterm color."
       "\nThis value must be between 16 and 256 (inclusive)."))

(s/def ::color-value 
  (s/or 
   :css-hex ::css-hex
   :xterm-color-id ::xterm-color-id
   :bling-color-name bling-color-names-set))

(s/def ::font-style #{"italic" "normal" :italic :normal})

(s/def ::background-color ::color-value)

(s/def ::color ::color-value)

(s/def ::style
  (s/and map? 
   (s/keys :opt-un [::background-color ::font-style ::color])))

(defn- key-must-be [coll kw]
  (str "Keys in the " kw " map must be one of the following:\n\n"
       (string/join "\n" coll)))

(s/def ::class-kw defs/all-base-classes)

(expound/defmsg ::class-kw
  (key-must-be defs/all-base-classes :classes))

(s/def ::syntax-kw
  (into #{} (-> defs/base-syntax-tokens keys)))

(expound/defmsg ::syntax-kw
  (key-must-be defs/all-base-syntax-tokens :syntax))

(s/def ::printer-kw
  (into #{} (-> defs/base-printer-tokens keys)))

(expound/defmsg ::printer-kw
  (key-must-be defs/all-base-printer-tokens :printer))

(s/def ::theme-token-value 
  (s/or :style-map ::style
        :class-kw  ::class-kw))

(expound/defmsg ::theme-token-value
  (str "A theme token value must be either:"
       "A valid :class keyword"
       "A valid token style-map"))

(s/def ::classes
  (s/map-of ::class-kw ::theme-token-value))

(s/def ::syntax
  (s/map-of ::syntax-kw ::theme-token-value))

(s/def ::printer
  (s/map-of ::printer-kw ::theme-token-value))


;; This is for the :theme entry in a Fireworks theme, (which itself is the
;; :theme entry in a Fireworks config map).
;;
;; A merged config has this shape:
;; {...
;;  :theme       {:name   "Foo Dark"
;;                :tokens {:classes {...}
;;                         :syntax  {...}
;;                         :printer {...}}
;;  :coll-limit  33
;;  :line-height 1.45
;;  ...}

(s/def ::tokens
  (s/and map?
         (s/keys :opt-un [::classes
                          ::syntax
                          ::printer])))
