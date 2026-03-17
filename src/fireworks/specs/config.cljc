(ns fireworks.specs.config
  (:require 
   [clojure.string :as string]
   [expound.alpha :as expound]  
   [fireworks.defs :as defs]
   [fireworks.basethemes :as basethemes]
   [fireworks.specs.theme :as theme]
   [fireworks.specs.tokens :as tokens]
   [clojure.spec.alpha :as s]))

;; User config validation specs
(s/def ::edn-file-path 
  (s/and string? #(re-find #"\.edn$" %)))

;; Upper bound of scalar-max-lengths get set here
(def scalar-max-length
  10000)

(def print-length
  1000)

;; TODO - Should this be 1?
(s/def ::fw-print-length
  (s/and int? #(<= 1 % print-length)))

;; This is for the theme entry in a fireworks config .edn
;; It can be either
;; - Name of stock theme e.g. "Zenburn Dark"
;; - Path to .edn config file
;; - An theme map that satisfies fireworks.specs.theme/theme

(expound/defmsg ::theme
  (str "The :theme option in a Fireworks config should be one of the following:"
       "\n"
       "\n" - "Name of stock theme e.g. \"Zenburn Dark\""
       "\n" - "Path to .edn config file"
       "\n" - "An theme map that satisfies fireworks.specs.theme/theme"
       ))

(s/def ::theme
  (s/or :name  ::theme/name
        :path  ::edn-file-path
        :theme ::theme/theme))

(s/def ::bold boolean?)

(s/def ::truncate boolean?)

(s/def ::label-max-length
  (s/and int? #(<= 10 % 100)))

(s/def ::format-label-as-code?
  boolean?)

(s/def ::line-height
  (s/and number? #(<= 0.5 % 3.0)))

(s/def ::margin-bottom
  (s/and int? #(<= 0 % 100)))

(s/def ::margin-top
  (s/and int? #(<= 0 % 100)))

(s/def ::margin-inline-start
  (s/and int? #(<= 0 % 100)))

(s/def ::single-line-coll-max-length
  (s/and int? #(<= 2 % 200)))

(s/def ::print-length
  ::fw-print-length)

(s/def ::scalar-extended-print-length
  (s/and int? #(<= 10 % scalar-max-length)))

(s/def ::scalar-result-max-length
  ::scalar-extended-print-length)

(s/def ::scalar-depth-1-max-length
  ::scalar-extended-print-length)

(s/def ::scalar-max-length
  (s/and int? #(<= 10 % scalar-max-length)))

(s/def ::scalar-mapkey-max-length
  (s/and int? #(<= 5 % scalar-max-length)))

(s/def ::single-column-maps?
  boolean?)

(s/def ::single-column-map-threshold
  (s/and int? #(< 1 %)))

(s/def ::print-level
  (s/and int? #(<= 1 % 20)))

(s/def ::metadata-print-level
  (s/and int? #(<= 1 % 10)))

(s/def ::display-namespaces? 
  boolean?)

(s/def ::display-metadata?
  boolean?)

(s/def ::metadata-position
  #{:inline :block "inline" "block"})

(s/def ::enable-rainbow-brackets?
  boolean?)

(s/def ::regex-theme
  #{:color :neutral "color" "neutral"})

(s/def ::bracket-contrast
  #{:high :low "high" "low"})

(s/def ::supports-color-level
  (s/nilable #(and int? 
                   (< 0 % 4))))

(s/def ::print-detected-color-level?
  boolean?)

(s/def ::legacy-terminal?
  boolean?)

(s/def ::enable-terminal-truecolor?
  boolean?)

(s/def ::enable-terminal-italics?
  boolean?)

(s/def ::enable-terminal-font-weights?
  boolean?)

;; flesh out this spec
(s/def ::custom-printers map?)

(s/def ::print-with fn?)

;; find for highlighting related -----------------------------------------------
(s/def ::returns-boolean
  #(or (true? (% 1))
       (false? (% 1))))

(s/def ::pred 
  (s/and fn? ::returns-boolean))

(s/def ::find-for-highlighting-map
  (s/or
   :find-by-pred
   (s/and map?
          (s/keys :req-un [::pred]
                  :opt-un [::tokens/style]))

   :find-by-path
   (s/and map?
          (s/keys :req-un [::path]
                  :opt-un [::tokens/style]))))


(s/def ::find
  (s/or :map
        ::find-for-highlighting-map
        :vector
        (s/coll-of ::find-for-highlighting-map :kind? vector?)))

(s/def ::when any?)

(s/def ::dissoc-metadata-keys vector?)

(s/def ::select-metadata-keys vector?)

(s/def ::multi-line-metadata? boolean?)

(s/def ::quote-symbols? boolean?)

(s/def ::colorize? boolean?)
