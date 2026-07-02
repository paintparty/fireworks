
(ns fireworks-vscode.scaffold-test
  "Spec for the Create New Project name-substitution rules."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.scaffold :as sc]
            [rewrite-clj.parser :as p]))

(deftest name-helpers
  (testing "project-ns lowercases + trims; path-seg munges hyphens to underscores"
    (is (= "my-app" (sc/project-ns "  My-App ")))
    (is (= "my_app" (sc/path-seg "My-App")))
    (is (= "acme" (sc/path-seg "acme")))))

(deftest path-renames-example-segment
  (testing "the placeholder `example` dir segment becomes the munged project name"
    (is (= "src/my_app/core.clj" (sc/scaffold-path "deps" "my-app" "src/example/core.clj")))
    (is (= "test/my_app/core_test.clj"
           (sc/scaffold-path "deps" "my-app" "test/example/core_test.clj")))
    (is (= "bb.edn" (sc/scaffold-path "bb" "my-app" "bb.edn")))
    ;; the bb watcher is kept, unrenamed
    (is (= ".fireworks/bb/watch.clj"
           (sc/scaffold-path "bb" "my-app" ".fireworks/bb/watch.clj")))))

(deftest path-skips
  (testing "regenerated / cache / result-cache paths are skipped (nil)"
    (is (nil? (sc/scaffold-path "deps" "x" ".gitignore")))
    (is (nil? (sc/scaffold-path "deps" "x" ".fireworks/results/.gitkeep")))
    (is (nil? (sc/scaffold-path "deps" "x" ".cpcache/foo.baz")))
    (is (nil? (sc/scaffold-path "lein" "x" "target/classes/foo")))
    (is (nil? (sc/scaffold-path "bb" "x" "out/foo")))
    (is (nil? (sc/scaffold-path "deps" "x" ".DS_Store")))))

(deftest content-substitutes-namespace
  (testing "example.core -> <ns>.core in source, requires, -test ns, and bb -m target"
    (is (= "(ns my-app.core)" (sc/scaffold-content "deps" "my-app" "src/example/core.clj" "(ns example.core)")))
    (is (= "(ns my-app.core-test (:require [my-app.core :as core]))"
           (sc/scaffold-content "deps" "my-app" "test/example/core_test.clj"
                                "(ns example.core-test (:require [example.core :as core]))")))
    (is (str/includes? (sc/scaffold-content "bb" "my-app" "bb.edn" "(shell \"bb\" \"-m\" \"example.core\")")
                       "\"my-app.core\""))))

(deftest content-lein-artifact
  (testing "project.clj Leiningen artifact fireworks-lein-example -> project name"
    (is (str/starts-with?
         (sc/scaffold-content "lein" "my-app" "project.clj"
                              "(defproject fireworks-lein-example \"0.1.0-SNAPSHOT\")")
         "(defproject my-app "))))

(def deps-example
  "{:paths [\"src\"]
 :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}
        io.github.paintparty/fireworks {:mvn/version \"0.21.0\"}}
 :aliases
 {:live-code
  {:extra-paths   [\"test\"]
   :extra-deps    {com.jakemccrary/test-refresh {:mvn/version \"0.26.0\"}}
   :override-deps {io.github.paintparty/fireworks {:local/root \"../../../../../../fireworks\"}}
   :main-opts     [\"-m\" \"com.jakemccrary.test-refresh\"]
   :jvm-opts      [\"-Dfireworks.elide=true\"]}}}")

(deftest content-deps-strips-repo-keys
  (testing "deps.edn :live-code loses :override-deps (repo local-root) and :jvm-opts (elide)"
    (let [out (sc/scaffold-content "deps" "my-app" "deps.edn" deps-example)]
      (is (not (str/includes? out ":override-deps")))
      (is (not (str/includes? out ":local/root")))
      (is (not (str/includes? out ":jvm-opts")))
      (is (not (str/includes? out "elide")))
      ;; the useful bits survive
      (is (str/includes? out "com.jakemccrary/test-refresh"))
      (is (str/includes? out ":main-opts"))
      (is (str/includes? out "io.github.paintparty/fireworks"))
      ;; still valid EDN
      (is (some? (p/parse-string-all out))))))

(deftest gitignore-canonical
  (testing "one clean gitignore for all runtimes: the ignore list + a single Fireworks idiom"
    (is (str/includes? sc/gitignore ".cpcache"))
    (is (str/includes? sc/gitignore "**/.clj-kondo/.cache/"))
    (is (str/includes? sc/gitignore ".fireworks/results/*"))
    (is (str/includes? sc/gitignore "!.fireworks/results/.gitkeep"))
    ;; the duplicated stanza the examples had must not reappear
    (is (= 1 (count (re-seq #"\.fireworks/results/\*" sc/gitignore))))))

(deftest launch-and-open
  (testing "launch command + open file per runtime"
    (is (= "clojure -M:live-code" (sc/launch-command "deps")))
    (is (= "lein with-profile +live-code test-refresh" (sc/launch-command "lein")))
    (is (= "bb live-code" (sc/launch-command "bb")))
    (is (= "src/my_app/core.clj" (sc/open-file "deps" "my-app")))))
