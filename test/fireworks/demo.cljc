(ns fireworks.demo
  (:require [fireworks.core :refer [? !? ?> !?> pprint]]
            [clojure.string :as string]))






(str "hello " "world")

(range 9)

(range 20)

(def sample
  {:string      "string"
   :number      1234
   :symbol      'foo
   :symbol+     (with-meta 'foo {:foo :bar})
   :boolean     true
   :set         #{1 2 3}
   :lambda      #(inc %)
   :fn          juxt
   :regex       #"^hi$"
   :atom/number (atom 1)
   :brackets    [[[[[[]]]]]]
   :java/array  (into-array [1 2 3 4])})

sample

(defn sample-fn [s]
  (-> s
      (string/split #" ")
      drop-last
      string/join
      string/upper-case))

(sample-fn "foo bar baz")



















;; Tour -----------------------------------------

(str "hello " "world")

(def m {:a "a" :b "b" :c "c"})

m


;; collections are printed on one line
(range 9)

;; Or multi-line based on the strlen of coll
(range 20)

;; Large collections are always truncated for
;; printing. This :coll-limit value can be
;; tweaked in your config or at the call site.
(range 42)


;; Values in maps are always justified
{:abcdefgh 1 :ijk 2 :lmnopqrstuv 3}

;; Maps which contain data-structures as keys
;; are formatted in a single-column layout,
;; with an empty line between each entry pair
{["abcdef" "ABCDEF"] "vector"
 :b                  "keyword"
 {:a "abc" :b "ABC"} "map"
 "d"                 "string"
 #{1 2 3}            "set"}

;; Atoms and Volatiles are displayed with a
;; tag and ecapsulated in angle brackets
(atom m)
(volatile! m)


;; Labels for records are displayed above
(defrecord Foo [a b])

(def myrecord (->Foo [1 [1 2]] [3 [3 4]]))

myrecord


;; Metadata, by default, is displayed inline
(def mysym
  (with-meta (symbol "foo") {:a 1 :b 2}))

mysym

;; Metadata on a collection
(def coll-with-meta
  ^{:bar "bar"} [1 mysym 2 3])

coll-with-meta


;; You can temporarily disable printing by 
;; toggling fireworks.core/!?
(!? {:a 1 :b 2})



;; You can tweak the display of the context...

;; Show just the result 
(!? :result {:a 1 :b 2})

;; Customize the label
(!? "My custom label" {:a 1 :b 2})

;; omit the label
(!? :file {:a 1 :b 2})



;; You can also use a different printing fn...

;; Use pprint instead of fireworks
(!? :pp- {:a 1 :b 2})

;; use pprint, just result
(!? :pp- {:a 1 :b 2})





;; Rainbow Brackets --------------------------


;; Rainbow brackets are enabled be default
(!? "rainbow-brackets demo" [[[[[[]]]]]])

;; Rainbow brackets contrast can be set in your
;; global fireworks config, or at call site 
(!? "low-contrast rainbow-brackets" 
    {:bracket-contrast :low}
    [[[[[[]]]]]])

;; Or disabled entirely
(!? "no rainbow-brackets"
    {:enable-rainbow-brackets? false}
    [[[[[[]]]]]])


;; Fireworks is more explicit than pprint
;; wrt labeling native Java objects
#_(pprint (java.util.ArrayList. (range 6)))
(!? (java.util.ArrayList. (range 6)))









;; Sample --------------------------

(def sample
 {:string       "string"
  :number       1234
  :symbol       'foo
  :symbol+      (with-meta 'foo {:foo :bar})
  :boolean      true
  :set          #{1 2 3}
  :lambda       #(inc %)
  :fn           juxt
  :regex        #"^hi$"
  :record       myrecord
  :atom/number  (atom 1)
  :brackets     [[[[[[]]]]]]
  :java/array   (into-array [1 2 3 4])
  :java/HashSet (java.util.HashSet. #{1 2 3})})

(!? sample)






;; Find --------------------------

;; Find and hightlight a specific value
(!? {:find {:pred #(= 2 %)}} sample)

;; Find and hightlight multiple values
(!?
 {:find [{:pred #(= 2 %)}
         {:pred  #(= 1 %)
          :style {:background-color "#a0f7fd"}}]}
 sample)

