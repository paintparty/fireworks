(ns fireworks.stubs.pp
  #?(:cljs (:require-macros 
            [fireworks.stubs.pp :refer [?pp pprint]])))

(defmacro ?pp
  ([])
  ([x] x)
  ([_ x] x))

(defmacro pprint
  ([])
  ([_])
  ([_ _])
  ([_ _ _]))
