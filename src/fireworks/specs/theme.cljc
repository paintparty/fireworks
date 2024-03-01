(ns fireworks.specs.theme
  (:require 
   [clojure.string :as string]
   [expound.alpha :as expound]  
   [fireworks.defs :as defs]
   [fireworks.basethemes :as basethemes]
   [fireworks.specs.tokens :as tokens]
   [clojure.spec.alpha :as s]))

(s/def ::mood
  #{:light :dark "light" "dark"})

(s/def ::name
  (s/and string?
         #(re-find #"^[A-Z][^\n\t\r]* (?:Light|Dark)$" %)))


(expound/defmsg ::name
  (str "The name of a Fireworks theme must satisfy the following regex:"
       "\n\n"
        "#\"^[A-Z][^\\n\\t\\r]* (?:Light|Dark)$\""
       "\n"
       "\n- Begins with a capital letter"
       "\n- Followed by any number of characters which are not newlines"
       "\n- Followed by a space"
       "\n- Ends with \"Light\", or \"Dark\""))


;; This is for a Fireworks theme map, which usually lives in its own
;; .edn file, but can also live directly in a config map.
(s/def ::theme 
  (s/and map?
         (s/keys :req-un [::tokens/tokens
                          ::name
                          ::mood])))
