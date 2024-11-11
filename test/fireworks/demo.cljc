(ns fireworks.demo
  (:require [fireworks.core :refer [? !? ?> !?>]]
            [clojure.string :as string]))


(str "hello " "world")

(def m {:a "a" :b "b" :c "c"})

m

;; collections are printed on one line
(range 9)

;; Or multi-line based on the strlen of coll
(range 20)

;; Large collections are always truncated for
;; printing. This :coll-limit value can be tweaked
;; in your config or at the call site.
(range 50)


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

;; Atoms and Volatiles are displayed with a tag
;; and ecapsulated in angle brackets
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



;; You can tweak the display of the label and file
;; info alacarte...

;; Show just the result (omit label and file info)
(!? :result {:a 1 :b 2})

;; Customize the label
(!? "My custom label" {:a 1 :b 2})

;; omit the label
(!? :file {:a 1 :b 2})



;; You can also use a different formatting method...
;; Use pprint instead of fireworks
(!? :pp- {:a 1 :b 2})

;; use pprint, just result (omit label and file info)
(!? :pp- {:a 1 :b 2})


;; Rainbow brackets are enabled be default
(!? "rainbow-brackets demo" [[[[[[]]]]]])

;; Rainbow brackets contrast can be set in your
;; global fireworks config, or at call site 
(!? "low-contrast rainbow-brackets" {:bracket-contrast :low} [[[[[[]]]]]])

(!? "no rainbow-brackets" {:enable-rainbow-brackets? false} [[[[[[]]]]]])

(!? "WTF" {:coll-limit 10} {:asdfasd 1
                            :b 2
                            :xasdfasd 1
                            :c 2
                            :dddf 2
                            })


;; Unlike pprint, Fireworks is more explicit about
;; labeling native Java objects
#_(pprint (java.util.ArrayList. (range 6)))
(!? (java.util.ArrayList. (range 6)))


;; A map with a bunch of different value types
(def kitchen-sink 
 {:string             "string"
  :uuid               #uuid "4fe5d828-6444-11e8-8222-720007e40350"
  :number             1234
  :symbol             'foo
  :symbol-w-meta      (with-meta 'foo {:foo :bar})
  :boolean            true
  :set                #{1 2 3}
  :lambda             #(inc %)
  :fn                 juxt
  :regex              #"^hi$"
  :record             myrecord
  :atom/number        (atom 1)
  :brackets           [[[[[[]]]]]]
  :hash-map/with-meta (with-meta
                        {:a :foo
                         :b 2}
                        {:a {:abc 1
                             :xyz "abcdefg"}})
  :java/array         (into-array [1 2 3 4])
  :java/HashSet       (java.util.HashSet. #{1 2 3})
  :java/HashMap       (java.util.HashMap. {:a 1
                                           :b 2})
  })

(!? kitchen-sink)


;; Find and hightlight a specific value
(!? {:find {:pred #(= 2 %)}} kitchen-sink)

;; Find and hightlight multiple values
(!?
 {:find [{:pred #(= 2 %)}
         {:pred  #(= 1 %)
          :style {:background-color "#a0f7fd"}}]}
 kitchen-sink)

