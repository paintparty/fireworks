(ns fireworks.foo-test
  (:require
   [clojure.string :as string]
   [fireworks.core :refer [?]]
   [hifi.sample]
   [fireworks.pp :as pp :refer [?pp !?pp pprint]]
   [fireworks.test-util :as tu :refer [escape-sgr]]
   [clojure.test :refer [deftest is]]))


(def theme "Universal")

(defn abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn
  []
  nil)

#_(? {:theme theme}
     hifi.sample/my-record-type)

#_(deftest long-fn-name
    (is (=
         (let [ret              (? :data
                                   {:theme             theme}
                                   {:a abcdefghijklmnopqrstuvwxyz-abcdefghijklmnopqrstuvwxyz-really-long-named-fn})
               formatted-string (-> ret :formatted :string)]
           (string/join (escape-sgr formatted-string)))
         "〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠 〠0〠〠38;5;61〠fireworks.foo-test/abcdefghijk〠0〠〠3;38;5;245〠...〠0〠〠38;5;102〠}〠0〠")))

;; Sample tests for experimenting with the snapshotter. Their goldens start as
;; placeholders; `(tu/snapshot! 'fireworks.foo-test)` fills them in.
#_(deftest sample-map
    (is (= (let [ret (? :data {:theme theme} {:a 1 :b 2})]
             (escape-sgr (-> ret :formatted :string)))
           "〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠:b〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠38;5;102〠}〠0〠")))

#_(deftest sample-vector
    (is (= (escape-sgr (-> (? :data {:theme theme} [1 2 3]) :formatted :string))
           "〠38;5;102〠[〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠38;5;102〠]〠0〠")))

(comment
  ;; Re-snapshot the goldens above from the REPL after the ns is loaded:
  (tu/snapshot! 'fireworks.foo-test)              ; whole ns
  (tu/snapshot! 'fireworks.foo-test 'sample-map)  ; one test
  (tu/snapshot! 'fireworks.foo-test '[sample-map sample-vector]))
