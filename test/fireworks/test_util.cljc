(ns fireworks.test-util
  (:require [clojure.string :as string]
            #?(:clj [clojure.test] :cljs [cljs.test])
            #?(:clj [clojure.java.io :as io])
            #?(:clj [rewrite-clj.zip :as z])))

;; Toggle this true / false to generate tests
(def write-tests? false)

;; Toggle this true / false to see tests
(def visual-mode? false)

;; If this is populated, only those tests will be shown, when visual mode is active
(def filter-tests
  #{}
  ;; #{'datatype-value 'java-util-hashmap}
  )

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

;; Install the colored-diff hook. On the JVM we WRAP the current value of
;; clojure.test/report rather than `defmethod` it: humane-test-output (loaded via
;; the global lein :injections) replaces the report var with its own fn, which
;; orphans any defmethod on the original multimethod. Wrapping runs after whatever
;; reporter is active. For our own sgr failures we SKIP the wrapped reporter
;; entirely (so humane's text expected/actual/diff dump is suppressed), print the
;; FAIL header ourselves, count the failure, and render the colored blocks; every
;; other event passes straight through. cljs has no such override, so defmethod is
;; fine there. `defonce` keeps the wrap from stacking on namespace reload.
#?(:clj
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

   :cljs
   ;; cljs failure counting lives inside the default :fail method, so we always
   ;; delegate (keeping its header + counting) and just append the colored blocks
   ;; for our sgr failures.
   (let [default-fail (get-method cljs.test/report :fail)]
     (defmethod cljs.test/report :fail [m]
       (default-fail m)
       (when (sgr-fail? m)
         (print-sgr-diff m)))))


;; -----------------------------------------------------------------------------
;; Golden snapshotter (JVM-only, dev tooling)
;;
;; Surgically rewrites the *expected* literal of `(is (= <actual> <expected>))`
;; forms in a test source file, in place, by evaluating the `<actual>` side and
;; storing its result. Formatting/comments are preserved (rewrite-clj). Target a
;; single test, several, or a whole namespace. Fire from the REPL:
;;
;;   (snapshot! 'fireworks.foo-test)                 ; whole ns
;;   (snapshot! 'fireworks.foo-test 'long-fn-name)   ; one test
;;   (snapshot! 'fireworks.foo-test '[t1 t2])        ; several
;;
;; Requires the test namespace to be loaded (so its vars/refers resolve during
;; eval). Re-require / re-run the ns afterwards to pick up the new goldens.
;; -----------------------------------------------------------------------------

#?(:clj
   (do
     (defn- ns->source-path
       "Resolve a namespace symbol to its on-disk source file, searching the
        usual test/src roots for the matching .cljc/.clj/.cljs file."
       [nsym]
       (let [base  (-> (name nsym)
                       (string/replace "." "/")
                       (string/replace "-" "_"))]
         (or (first (for [root ["test" "testbb" "src" "."]
                          ext  [".cljc" ".clj" ".cljs"]
                          :let [p (str root "/" base ext)]
                          :when (.exists (io/file p))]
                      p))
             (throw (ex-info (str "No source file found for namespace " nsym)
                             {:ns nsym})))))

     (defn- deftest-zloc?
       "True when zloc is a (deftest NAME ...) list node."
       [zloc]
       (and (= :list (z/tag zloc))
            (= 'deftest (some-> zloc z/down z/sexpr))))

     (defn- deftest-name [zloc]
       (-> zloc z/down z/right z/sexpr))

     (defn- is=zloc?
       "True when zloc is an `(is (= <actual> <expected>))` form whose expected
        side is a string literal (the only shape we snapshot)."
       [zloc]
       (and (= :list (z/tag zloc))
            (= 'is (some-> zloc z/down z/sexpr))
            (let [eq (-> zloc z/down z/right)]
              (and eq
                   (= :list (z/tag eq))
                   (= '= (some-> eq z/down z/sexpr))
                   (let [expected (-> eq z/down z/right z/right)]
                     (and expected (string? (z/sexpr expected))))))))

     (defn- rewrite-is!
       "Given an `(is (= actual expected))` zloc, eval the actual side in nsym and
        replace the expected literal with the result. Returns the zloc at the is
        node.

        We eval the actual node's *source string* (via read-string) rather than
        its z/sexpr: z/sexpr rebuilds map literals as unordered hash-maps, which
        would reorder keys (e.g. {:a 1 :b 2} -> :b :a) and produce goldens that
        don't match the reader-built array-maps the running test sees."
       [zloc nsym]
       (let [eq         (-> zloc z/down z/right)
             actual-z   (-> eq z/down z/right)
             expected-z (-> actual-z z/right)
             new-val    (binding [*ns* (the-ns nsym)]
                          (eval (read-string (z/string actual-z))))]
         (-> expected-z (z/replace new-val) z/up z/up)))

     (defn- all-deftest-names
       "Names of every top-level deftest in `src`, in order."
       [src]
       (loop [zloc (z/of-string src)
              acc  []]
         (let [acc (if (deftest-zloc? zloc) (conj acc (deftest-name zloc)) acc)]
           (if-let [r (z/right zloc)]
             (recur r acc)
             acc))))

     (defn snapshot-source
       "Pure transform: return `src` with every targeted `(is (= actual expected))`
        expected literal rewritten from a fresh eval of its actual side. `targets`
        is a set of deftest-name symbols. Exposed for testing.

        z/of-string sits at the first top-level form, so we iterate siblings via
        z/right and prewalk *within* each targeted deftest (prewalk returns to its
        start node, keeping the sibling walk on the top level)."
       [src nsym targets]
       (loop [zloc (z/of-string src)]
         (let [zloc (if (and (deftest-zloc? zloc)
                             (contains? targets (deftest-name zloc)))
                      (z/prewalk zloc
                                 is=zloc?
                                 (fn [z] (rewrite-is! z nsym)))
                      zloc)]
           (if-let [r (z/right zloc)]
             (recur r)
             (z/root-string zloc)))))

     (defn snapshot!
       "Rewrite golden `(is (= actual expected))` literals in nsym's source file
        in place. `which` selects tests: nil/omitted = the whole namespace, a
        single symbol = that test, a coll of symbols = those tests.

        The namespace must be loaded first so the actual side can be evaluated."
       ([nsym] (snapshot! nsym nil))
       ([nsym which]
        (let [path    (ns->source-path nsym)
              src     (slurp path)
              targets (cond
                        (nil? which)    (set (all-deftest-names src))
                        (symbol? which) #{which}
                        :else           (set which))
              out     (snapshot-source src nsym targets)]
          (if (= out src)
            (println "snapshot!: no changes for" (pr-str (vec targets)) "in" path)
            (do (spit path out)
                (println "snapshot!: rewrote" (count targets) "test(s) in" path)))
          path)))))
