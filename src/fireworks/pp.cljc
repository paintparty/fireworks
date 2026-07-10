(ns fireworks.pp
  "Thin wrapper over hifi.pp, preserving the fireworks.pp public API.
   The pretty-printer implementation lives in hifi.pp."
  (:require [hifi.pp])
  #?(:cljs (:require-macros [fireworks.pp :refer [?pp]])))

(def pprint hifi.pp/pprint)

(def !?pp hifi.pp/!?pp)

(defmacro ?pp
  ([x]
   (with-meta `(hifi.pp/?pp ~x) (meta &form)))
  ([label x]
   (with-meta `(hifi.pp/?pp ~label ~x) (meta &form))))
