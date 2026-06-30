(ns fireworks.fs
  ;; kondo is gonna complain about this ns form but it is fine
  (:require
   #?@(:clj              [[clojure.java.io :as io]]
       :org.babashka/nbb [["fs" :as fs] ["path" :as path]]
       :node             [["fs" :as fs] ["path" :as path]])))

(defn dir-exists? [p]
  #?(:clj              (.isDirectory (io/file p))
     :org.babashka/nbb (try (.isDirectory (fs/statSync p)) (catch :default _ false))
     :node             (try (.isDirectory (fs/statSync p)) (catch :default _ false))
     :cljs             false))

;; TODO - what if one of parts is nil?
(defn join-path [& parts]
  #?(:clj              (.getPath (apply io/file parts))
     :org.babashka/nbb (apply path/join parts)
     :node             (apply path/join parts)
     :cljs             (apply str (interpose "/" parts))))

(defn ensure-dir! [p]
  #?(:clj              (io/make-parents p)
     :org.babashka/nbb (fs/mkdirSync (path/dirname p) #js {:recursive true})
     :node             (fs/mkdirSync (path/dirname p) #js {:recursive true})
     :cljs             nil))

(defn write-file! [p s]
  #?(:clj              (clojure.core/spit p s)
     :org.babashka/nbb (fs/writeFileSync p s)
     :node             (fs/writeFileSync p s)
     :cljs             nil))

(defn file-exists? [p]
  #?(:clj              (.isFile (io/file p))
     :org.babashka/nbb (try (.isFile (fs/statSync p)) (catch :default _ false))
     :node             (try (.isFile (fs/statSync p)) (catch :default _ false))
     :cljs             false))

(defn path-exists? 
  "True if anything exists at p.
   Follows symlinks: a broken symlink reads as false."
  [p]
  #?(:clj              (.exists (io/file p))
     :org.babashka/nbb (fs/existsSync p)
     :node             (fs/existsSync p)
     :cljs             false))
