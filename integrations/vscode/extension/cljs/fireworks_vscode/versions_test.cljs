
(ns fireworks-vscode.versions-test
  "Guard: the version coordinates live in exactly one place (fireworks-vscode.versions). The
   `examples/` project files are real, standalone build files that repeat those coordinates
   literally — these tests fail loudly if they drift, and if the deps/lein editors ever inject a
   version other than the canonical one. Runs under :node-test, so it reads the example files off
   disk (cwd is the extension dir when `shadow-cljs compile :test` runs)."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.deps :as deps]
            [fireworks-vscode.lein :as lein]
            [fireworks-vscode.versions :as v]
            ["fs" :as fs]))

(defn- slurp-example [rel]
  (.readFileSync fs (str "examples/" rel) "utf8"))

;; The coordinate strings as they appear in the example build files, built from the constants.
(def ^:private fireworks-deps-coord  (str v/fireworks-sym " {:mvn/version \"" v/fireworks-version "\"}"))
(def ^:private fireworks-lein-coord  (str "[" v/fireworks-sym " \"" v/fireworks-version "\"]"))
(def ^:private test-refresh-deps-coord (str v/test-refresh-sym " {:mvn/version \"" v/test-refresh-version "\"}"))
(def ^:private lein-plugin-coord     (str "[" v/lein-test-refresh-sym " \"" v/lein-test-refresh-version "\"]"))

(deftest examples-deps-in-sync
  (testing "examples/deps-project/deps.edn carries the canonical Fireworks + test-refresh coords"
    (let [text (slurp-example "deps-project/deps.edn")]
      (is (str/includes? text fireworks-deps-coord)
          (str "deps example missing " fireworks-deps-coord))
      (is (str/includes? text test-refresh-deps-coord)
          (str "deps example missing " test-refresh-deps-coord)))))

(deftest examples-lein-in-sync
  (testing "examples/leiningen-project/project.clj carries the canonical Fireworks dep + plugin"
    (let [text (slurp-example "leiningen-project/project.clj")]
      (is (str/includes? text fireworks-lein-coord)
          (str "lein example missing " fireworks-lein-coord))
      (is (str/includes? text lein-plugin-coord)
          (str "lein example missing " lein-plugin-coord)))))

(deftest examples-bb-in-sync
  (testing "examples/babashka-project/bb.edn carries the canonical Fireworks dep"
    (let [text (slurp-example "babashka-project/bb.edn")]
      (is (str/includes? text fireworks-deps-coord)
          (str "bb example missing " fireworks-deps-coord)))))

(deftest deps-editor-injects-canonical-versions
  (testing "add-live-code-alias injects the canonical Fireworks + test-refresh versions"
    (let [{:keys [text]} (deps/add-live-code-alias "{:paths [\"src\"]}")]
      (is (str/includes? text (str "\"" v/fireworks-version "\"")))
      (is (str/includes? text (str "\"" v/test-refresh-version "\""))))))

(deftest lein-editor-injects-canonical-fireworks
  (testing "lein/ensure-fireworks injects the canonical Fireworks version"
    (let [{:keys [text]} (lein/ensure-fireworks
                          "(defproject my-app \"0.1.0\" :dependencies [[org.clojure/clojure \"1.12.0\"]])")]
      (is (str/includes? text (str "\"" v/fireworks-version "\""))))))
