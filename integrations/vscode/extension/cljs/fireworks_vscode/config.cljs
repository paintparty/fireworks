
(ns fireworks-vscode.config
  "Pure editing of the project .test-refresh.edn for Phase 2 (live coding). Same
   boundary discipline as fireworks-vscode.toggle: no VS Code, no Calva, no file
   I/O. The test-refresh + Fireworks dependencies are injected at launch via
   `clojure -Sdeps` (the TS side builds that command), so this namespace never
   touches deps.edn — it only renders and edits the small .test-refresh.edn that
   holds the watcher options and the tap/test mode. On unparseable input a function
   returns {:error :unparseable} rather than throwing, so TS can abort cleanly."
  (:require [clojure.string :as str]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(def ^:private tap-banner-default  "🔥🔥🔥🔥🔥🔥🔥🔥🔥")
(def ^:private test-banner-default "📋 Running tests...")

;; Rendered key order for a created .test-refresh.edn.
(def ^:private option-order
  [:quiet :changes-only :notify-on-success :debug :banner :debug-mode-opts :clear])

(defn- default-options
  "The default option map for `mode`: tap carries :debug true, the fire banner, and
   :debug-mode-opts; test flips :debug/:banner and drops :debug-mode-opts (it only
   applies in tap/debug mode). Mirrors the design doc."
  [mode]
  (let [tap? (= mode :tap)
        base {:quiet             true
              :changes-only      true
              :notify-on-success false
              :debug             tap?
              :banner            (if tap? tap-banner-default test-banner-default)
              :clear             true}]
    (if tap?
      (assoc base :debug-mode-opts {:print-full-stack-trace? true})
      base)))

(defn- render-options
  "Render an options map as an aligned, multi-line .test-refresh.edn string. Known
   keys come first in option-order; any extra keys follow."
  [opts]
  (let [ks    (concat (filter #(contains? opts %) option-order)
                      (remove (set option-order) (keys opts)))
        width (apply max (map (comp count str) ks))
        line  (fn [k]
                (let [s   (str k)
                      pad (apply str (repeat (- width (count s)) " "))]
                  (str s pad " " (pr-str (get opts k)))))
        lines (map line ks)]
    (str "{" (first lines) "\n"
         (str/join "\n" (map #(str " " %) (rest lines)))
         "}\n")))

;; --- rewrite-clj helpers for in-place mode edits --------------------------

(defn- newline-before-key
  "key-loc is at a freshly-added map key. If it has a preceding sibling, replace the
   separator whitespace to its left with newline + `indent` spaces; otherwise it is
   the first key, so leave it. Returns a loc at the map."
  [key-loc indent]
  (if (z/left key-loc)
    (-> key-loc
        z/left*
        (z/replace* (n/newlines 1))
        (z/insert-right* (n/spaces indent))
        z/up)
    (z/up key-loc)))

(defn- assoc-nl
  "Assoc k -> v into the map at zmap, updating in place when present, else placing it
   on its own line indented `indent`. Returns a loc at the map."
  [zmap k v indent]
  (if (z/get zmap k)
    (z/assoc zmap k v)
    (-> zmap (z/assoc k v) (z/get k) z/left (newline-before-key indent))))

;; --- public surface -------------------------------------------------------

(defn default-config
  "The default .test-refresh.edn text for `mode` (:tap | :test), used to create the
   file when the project doesn't have one yet."
  [mode]
  (render-options (default-options mode)))

(defn read-mode
  "Read the live-coding mode from .test-refresh.edn text: :tap when :debug is truthy,
   else :test (absent :debug => :test). Returns the mode or {:error :unparseable}."
  [text]
  (try
    (let [debug (z/get (z/of-string text) :debug)]
      (if (and debug (z/sexpr debug)) :tap :test))
    (catch :default _ {:error :unparseable})))

(defn set-mode
  "Flip .test-refresh.edn text to `mode` (:tap | :test): set :debug and swap :banner
   to the matching banner, in place. Other keys (incl. :debug-mode-opts) are left
   as-is. Returns new text or {:error :unparseable}."
  [text mode {:keys [tap-banner test-banner]}]
  (try
    (let [tap?   (= mode :tap)
          banner (if tap? (or tap-banner tap-banner-default)
                          (or test-banner test-banner-default))]
      (-> (z/of-string text)
          (assoc-nl :debug tap? 1)
          (assoc-nl :banner banner 1)
          z/root-string))
    (catch :default _ {:error :unparseable})))
