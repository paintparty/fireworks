(ns fireworks.specs.config
  (:require 
   [clojure.string :as string]
   [expound.alpha :as expound]  
   [fireworks.defs :as defs]
   [fireworks.basethemes :as basethemes]
   [fireworks.specs.theme :as theme]
   [clojure.spec.alpha :as s]))

;; User config validation specs
(s/def ::edn-file-path 
  (s/and string? #(re-find #"\.edn$" %)))

;; TODO - Should this be 1?
(s/def ::fw-coll-limit
  (s/and int? #(<= 2 % 200)))

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



(s/def ::label-length-limit
  (s/and int? #(<= 10 % 100)))

(s/def ::line-height
  (s/and number? #(<= 0.5 % 3.0)))

(s/def ::single-line-coll-length-limit
  (s/and int? #(<= 2 % 200)))

(s/def ::coll-limit
  ::fw-coll-limit)

(s/def ::evaled-form-coll-limit
  ::fw-coll-limit)

(s/def ::non-coll-extended-length-limit
  (s/and int? #(<= 10 % 1000)))

(s/def ::non-coll-result-length-limit
  ::non-coll-extended-length-limit)

(s/def ::non-coll-depth-1-length-limit
  ::non-coll-extended-length-limit)

(s/def ::non-coll-length-limit
  (s/and int? #(<= 10 % 80)))

(s/def ::non-coll-mapkey-length-limit
  (s/and int? #(<= 5 % 80)))

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

(s/def ::bracket-contrast
  #{:high :low "high" "low"})

(s/def ::legacy-terminal?
  boolean?)

(s/def ::enable-terminal-truecolor?
  boolean?)

(s/def ::enable-terminal-italics?
  boolean?)

(s/def ::enable-terminal-font-weights?
  boolean?)

;; flesh out this spec
(s/def ::custom-printers
  map?)

;; find for highlighting related -----------------------------------------------
(s/def ::returns-boolean
  #(or (true? (% 1))
       (false? (% 1))))

(s/def ::pred 
  (s/and fn? ::returns-boolean))


(s/def ::find-for-highlighting-map
  (s/and map?
         (s/keys :req-un [::pred]
                 :opt-un [::style])))

(s/def ::find
  (s/or :map
        ::find-for-highlighting-map
        :vector
        (s/coll-of ::find-for-highlighting-map :kind? vector?)))

(s/def ::when fn?)

(s/def ::fireworks-user-config
  (s/and map?
         (s/keys :opt-un [::line-height 
                          ::enable-terminal-italics? 
                          ::non-coll-result-length-limit
                          ::non-coll-depth-1-length-limit
                          ::non-coll-mapkey-length-limit 
                          ::non-coll-length-limit 
                          ::display-namespaces? 
                          ::enable-rainbow-brackets? 
                          ::enable-terminal-truecolor? 
                          ::print-level 
                          ::theme 
                          ::metadata-print-level 
                          ::mood 
                          ::coll-limit 
                          ::evaled-form-coll-limit 
                          ::display-metadata? 
                          ::metadata-position 
                          ::bracket-contrast
                          ::custom-printers
                          ;; TODO test this
                          ;; ::find
                          ])))
