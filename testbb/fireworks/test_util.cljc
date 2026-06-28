(ns fireworks.test-util
  (:require [clojure.string :as string]
            [clojure.test]))

(def visual-mode? true)

;; The ANSI escape (CSI introducer) as a string, built from its char code so no
;; raw control byte lives in this source file.
(def ^:private esc (str (char 27)))

(defn escape-sgr
  "Replace ANSI SGR codes with readable 〠CODE〠 markers for golden tests."
  [s]
  (string/replace s (re-pattern (str esc "\\[([0-9;]*)[mK]")) "〠$1〠"))

(defn unescape-sgr
  "Inverse of escape-sgr: 〠CODE〠 -> real ANSI SGR escape. Only the numeric
  params are kept by escape-sgr (not the m/K terminator), so this always emits m."
  [s]
  (string/replace s #"〠([0-9;]*)〠" (str esc "[$1m")))

(defn- sgr-string? [x]
  (and (string? x) (string/includes? x "〠")))

(defn- find-sgr
  "Walk an `is` report value and return the first 〠-bearing string, or nil.
  Handles both the raw clojure.test shapes (`:expected` => `(= actual expected)`
  form, `:actual` => `(not (= aval bval))`) and humane-test-output's reshaped map
  (`:expected` => the string, `:actual` => `(string)`)."
  [x]
  (cond
    (sgr-string? x)  x
    (sequential? x)  (some find-sgr x)
    :else            nil))

(defn- sgr-fail?
  "True when a :fail report's values carry 〠 markers (one of ours)."
  [m]
  (boolean (or (find-sgr (:expected m)) (find-sgr (:actual m)))))

(defn- print-sgr-diff
  "Print the two sides of an sgr golden failure re-rendered as real ANSI color.
  Caller prints the FAIL header; this prints only the expected/actual blocks."
  [m]
  (let [e (find-sgr (:expected m))
        a (find-sgr (:actual m))]
    (println)
    (println "── expected ──")
    (println)
    (println (unescape-sgr (str e)))
    (println)
    (println "── actual ──")
    (println)
    (println (unescape-sgr (str a)))
    (println)
    (println "─────────────────────────")))

;; Wrap the current clojure.test/report (rather than defmethod). For our own sgr
;; failures we skip the wrapped reporter (suppressing its text expected/actual
;; dump), print the FAIL header, count the failure, and render the colored blocks;
;; everything else passes through. `defonce` avoids stacking the wrap on reload.
(defonce ^:private sgr-report-hook
  (do (alter-var-root
       #'clojure.test/report
       (fn [report]
         (fn [m]
           (if (and (= :fail (:type m)) (sgr-fail? m))
             (clojure.test/with-test-out
               (clojure.test/inc-report-counter :fail)
               (println "\nFAIL in" (clojure.test/testing-vars-str m))
               (print-sgr-diff m))
             (report m)))))
      true))
