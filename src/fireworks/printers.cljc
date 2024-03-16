(ns fireworks.printers
  (:require
   [clojure.string :as string]
   [fireworks.truncate :as truncate]
   [fireworks.state :as state]
   [clojure.set :as set]
   [typetag.core :as typetag]))

#?(:cljs
   (do 
     (defn inline-style->map [v]
       (as-> v $
         (string/split $ #";")
         (map #(let [kv    (-> % string/trim (string/split #":") )
                     [k v] (map string/trim kv)]
                 [k v]) 
              $)
         (into {} $)
         (clj->js $)))

     (defn dom-el-attrs-map
       [x]
       (when-let [el (some-> x meta :fw/truncated :html-element)]
         ;; You could pull :fw/user-meta out of html element,
         ;; but when would it have it?
         
         ;; Check for when no attrs
         (when (.hasAttributes el)
           (let [attrs (.-attributes el)]
             (with-meta
               (into {}  
                     (for [i    (range (.-length attrs))
                           :let [item (.item attrs i)
                                 k (.-name item)
                                 v (.-value item)]]
                       [(symbol (str k ":"))
                        (if-not (= k "style")
                          v
                          (inline-style->map v))]))
               {:fw/og-t (some-> x meta :fw/truncated :t) })))))))


(def void-html-elements 
  #{"AREA"  "BASE"
    "BR"    "COL"
    "EMBED" "HR"
    "IMG"   "INPUT"
    "LINK"  "META"
    "PARAM" "SOURCE"
    "TRACK" "WBR"})

(defn html-tag 
  "(html-tag \"DIV\" \"<div>\" )"
  [s]
  (let [void-element? (contains? void-html-elements s)]
    (str "<" (string/lower-case s) (when void-element? "/") ">")))

(def fireworks-custom-printers
  #?(:cljs 
     {#{:dom-element-node}
      {:pred       any?
       :f          dom-el-attrs-map
       :badge-text #(let [post-og-truncation-meta (some-> % meta :fw/truncated)
                          og-t                    (:t post-og-truncation-meta)
                          html-element            (if (keyword? og-t)
                                                    (name og-t)
                                                    og-t)
                          html-tag                (some->
                                                   post-og-truncation-meta 
                                                   :dom-element-tag-name 
                                                   html-tag)]
                      (some->> html-tag (str html-element  " ")))}}
     :clj  nil))

(defn- resolve-custom-printers
  [coll og-type t all-tags]
  (when-let [cp (or (get coll og-type nil)
                    (get coll t nil)
                    (some (fn [[k v]] 
                            (when (and (set? k)
                                       (some-> k
                                               (set/intersection all-tags)
                                               seq
                                               (nth 0 nil)))
                              v))
                          coll))]
    (if (vector? cp) cp [cp])))

(defn- some-printers [x coll]
  (some (fn [{:keys [pred]
              :as   cp}]
          (when-let [og-x (some-> x meta :fw/truncated :og-x)] 
            (when (pred og-x) cp)))
        coll))

(defn custom 
  "Post-truncation, all values and nested values are checked against user-
   supplied custom printers and built-in fireworks custom printers
   (fireworks.printers/fireworks-custom-printers).

   If a custom printer is found for the value, the supplied custom-printing
   function will be applied to the value. The value will then be truncated
   (again) via fireworks.truncate/truncate.
   
   Additional (optional) metadata describing custom badge style and text is
   also attached to the transformed value.
   
   Custom printers only operate on collection-like values."
  [x]
  (let [user-supplied-custom-printers-keys
        (some-> @state/config :custom-printers keys seq)

        fireworks-custom-printers-keys
        (some-> fireworks-custom-printers keys seq)]
    (if-not (or user-supplied-custom-printers-keys
                fireworks-custom-printers-keys) 
     x
     (let [{og-type      :type
            t            :t
            all-tags :all-tags}
           (or (some-> x meta :fw/truncated)
               (let [m (typetag/tag-map x 
                                        {:function-info?           false
                                         :js-built-in-object-info? false})]
                 (set/rename-keys m {:tag :t})))

           user-supplied-custom-printers
           (resolve-custom-printers
            (:custom-printers @state/config)
            og-type
            t
            all-tags)

           fireworks-custom-printers
           (resolve-custom-printers 
            fireworks-custom-printers 
            og-type
            t
            all-tags)

           {:keys [f badge-text badge-style]}
           (or (some-printers x user-supplied-custom-printers)
               (some-printers x fireworks-custom-printers))

           transformed-x
           (if (and (some-> x meta :fw/truncated :coll-type?)
                    (fn? f))
             (let [v*
                   (f x)

                   v
                   (truncate/truncate 0 v*)

                   ;; TODO maybe use util/carries-metadata instead?
                   ;; Address this when doing truncate/metadata refactorings
                   meta-data-incompatible-type
                   (cond (string? v*) :string
                         (keyword? v*) :keyword
                         (number? v*) :number)

                   custom-printing-mm-entries
                   (merge (when (or badge-text badge-style)
                            (let [badge-text (if (fn? badge-text)
                                               (badge-text x)
                                               badge-text)]
                              {:fw/custom-badge-text
                               badge-text
                               :fw/custom-badge-style
                               (when badge-style
                                 (state/with-conformed-colors
                                   {:style-map                badge-style
                                    :theme-token              :type-label
                                    :from-custom-badge-style? true}))}))
                          (when meta-data-incompatible-type
                            {:fw/type-after-custom-printing
                             meta-data-incompatible-type}))]

               ;; If data is transformed to a value that doesn't satisfy coll?,
               ;; all user metadata is lost 
               (if (coll? v)
                 (vary-meta v merge custom-printing-mm-entries)
                 v))
             x)]
       transformed-x))))
