;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.config-test
  "Spec for the pure config-editing logic. Assertions parse the returned text and
   compare data (robust to whitespace) plus substring checks for the survival of
   unrelated content and comments — the precise specification for the managed-key
   merges (see fireworks-vscode-extension-phase2-design.md §Testing)."
  (:require [cljs.reader :as edn]
            [cljs.test :refer [deftest is testing]]
            [clojure.string :as str]
            [fireworks-vscode.config :as cfg]))

(def fw 'io.github.paintparty/fireworks)
(def tr 'com.jakemccrary/test-refresh)
(def versions {:fireworks-version "0.16.1" :test-refresh-version "0.26.0"})

(defn- alias-of [text]
  (get-in (edn/read-string text) [:aliases :test-refresh]))

;; ---------------------------------------------------------------------------
;; ensure-deps-alias
;; ---------------------------------------------------------------------------

(deftest deps-create-from-defaults
  (testing "nil text creates a self-sufficient :test-refresh alias with pinned versions"
    (let [a (alias-of (cfg/ensure-deps-alias nil versions))]
      (is (= ["test"] (:extra-paths a)))
      (is (= "0.26.0" (get-in a [:extra-deps tr :mvn/version])))
      (is (= "0.16.1" (get-in a [:extra-deps fw :mvn/version])))
      (is (= ["-m" "com.jakemccrary.test-refresh"] (:main-opts a)))))
  (testing "blank text is treated as create"
    (is (some? (alias-of (cfg/ensure-deps-alias "   \n" versions))))))

(deftest deps-merge-preserves-unrelated
  (let [in  ";; my project\n{:deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}\n :aliases\n {:my-alias {:extra-paths [\"src\"]} ; keep me\n  }}\n"
        out (cfg/ensure-deps-alias in versions)
        d   (edn/read-string out)]
    (testing "existing :deps and unrelated alias survive untouched"
      (is (= "1.12.0" (get-in d [:deps 'org.clojure/clojure :mvn/version])))
      (is (= ["src"] (get-in d [:aliases :my-alias :extra-paths]))))
    (testing "managed alias added"
      (is (= "0.26.0" (get-in d [:aliases :test-refresh :extra-deps tr :mvn/version]))))
    (testing "comments preserved"
      (is (str/includes? out "my project"))
      (is (str/includes? out "keep me")))))

(deftest deps-no-aliases-key
  (testing "a deps.edn without :aliases gains one with the managed alias"
    (let [out (cfg/ensure-deps-alias "{:deps {a {:mvn/version \"1\"}}}\n" versions)
          d   (edn/read-string out)]
      (is (= "1" (get-in d [:deps 'a :mvn/version])))
      (is (= "0.16.1" (get-in d [:aliases :test-refresh :extra-deps fw :mvn/version]))))))

(deftest deps-idempotent
  (testing "re-running over an alias with older versions updates only the versions"
    (let [once  (cfg/ensure-deps-alias "{:deps {a {:mvn/version \"1\"}}}\n"
                                       {:fireworks-version "0.10.0" :test-refresh-version "0.20.0"})
          twice (cfg/ensure-deps-alias once versions)
          d     (edn/read-string twice)]
      (is (= "1" (get-in d [:deps 'a :mvn/version])))
      (is (= "0.26.0" (get-in d [:aliases :test-refresh :extra-deps tr :mvn/version])))
      (is (= "0.16.1" (get-in d [:aliases :test-refresh :extra-deps fw :mvn/version]))))))

(deftest deps-unparseable
  (testing "unbalanced input returns a clean error, not a throw"
    (is (= {:error :unparseable} (cfg/ensure-deps-alias "{:deps {a " versions)))))

;; ---------------------------------------------------------------------------
;; write-options / read-options
;; ---------------------------------------------------------------------------

(deftest options-create-from-defaults
  (testing "nil text renders an options file carrying the given keys"
    (let [out (cfg/write-options nil {:quiet true :changes-only true :debug true
                                      :banner "🔥" :clear true})
          o   (edn/read-string out)]
      (is (= true (:quiet o)))
      (is (= true (:debug o)))
      (is (= "🔥" (:banner o))))))

(deftest options-merge-preserves-unrelated
  (testing "merging adds/updates only the given keys; others and comments survive"
    (let [in  "{:quiet true ; hand comment\n :debug true\n :banner \"old\"}\n"
          out (cfg/write-options in {:changes-only true :banner "new"})
          o   (edn/read-string out)]
      (is (= true (:quiet o)))            ; untouched
      (is (= true (:debug o)))            ; untouched
      (is (= true (:changes-only o)))     ; added
      (is (= "new" (:banner o)))          ; updated in place
      (is (str/includes? out "hand comment")))))

(deftest options-read-roundtrip
  (testing "read-options parses to a map"
    (is (= {:quiet true :debug false}
           (cfg/read-options "{:quiet true\n :debug false}"))))
  (testing "unparseable -> error"
    (is (= {:error :unparseable} (cfg/read-options "{:quiet ")))))

;; ---------------------------------------------------------------------------
;; read-mode / set-mode
;; ---------------------------------------------------------------------------

(deftest mode-read
  (testing ":debug true -> :tap"
    (is (= :tap (cfg/read-mode "{:debug true}"))))
  (testing ":debug false -> :test"
    (is (= :test (cfg/read-mode "{:debug false}"))))
  (testing "absent :debug -> :test"
    (is (= :test (cfg/read-mode "{:quiet true}"))))
  (testing "unparseable -> error"
    (is (= {:error :unparseable} (cfg/read-mode "{:debug ")))))

(deftest mode-set-roundtrip
  (let [banners {:tap-banner "🔥" :test-banner "📋"}
        tap-edn "{:quiet true\n :changes-only true\n :debug true\n :banner \"🔥\"\n :debug-mode-opts {:print-full-stack-trace? true}\n :clear true}\n"]
    (testing "tap -> test flips :debug and swaps banner, leaving other keys"
      (let [out (cfg/set-mode tap-edn :test banners)
            o   (edn/read-string out)]
        (is (= false (:debug o)))
        (is (= "📋" (:banner o)))
        (is (= true (:quiet o)))
        (is (= true (:clear o)))
        (is (= {:print-full-stack-trace? true} (:debug-mode-opts o)))  ; left as-is
        (is (= :test (cfg/read-mode out)))))
    (testing "test -> tap flips back"
      (let [back (cfg/set-mode (cfg/set-mode tap-edn :test banners) :tap banners)
            o    (edn/read-string back)]
        (is (= true (:debug o)))
        (is (= "🔥" (:banner o)))
        (is (= :tap (cfg/read-mode back)))))
    (testing "adds :debug/:banner when absent"
      (let [out (cfg/set-mode "{:quiet true}\n" :tap banners)
            o   (edn/read-string out)]
        (is (= true (:debug o)))
        (is (= "🔥" (:banner o)))
        (is (= true (:quiet o)))))))

;; ---------------------------------------------------------------------------
;; extract-managed (seed-from-global)
;; ---------------------------------------------------------------------------

(deftest extract-deps-versions
  (testing "pulls the managed versions from a global :test-refresh alias, ignoring other aliases"
    (let [global "{:aliases {:other {:extra-paths [\"x\"]}\n :test-refresh {:extra-deps {com.jakemccrary/test-refresh {:mvn/version \"0.25.0\"}\n io.github.paintparty/fireworks {:mvn/version \"0.15.0\"}}}}}"]
      (is (= {:fireworks-version "0.15.0" :test-refresh-version "0.25.0"}
             (cfg/extract-managed global :deps)))))
  (testing "missing alias -> nil versions, not an error"
    (is (= {:fireworks-version nil :test-refresh-version nil}
           (cfg/extract-managed "{:deps {}}" :deps)))))

(deftest extract-test-refresh-edn
  (testing "pulls the whole options map from a global ~/.test-refresh.edn"
    (is (= {:options {:quiet true :debug true}}
           (cfg/extract-managed "{:quiet true\n :debug true}" :test-refresh-edn))))
  (testing "unparseable -> error"
    (is (= {:error :unparseable} (cfg/extract-managed "{:quiet " :test-refresh-edn)))))

;; ---------------------------------------------------------------------------
;; ensure-lein-setup (project.clj is a (defproject ...) list, not a map)
;; ---------------------------------------------------------------------------

(def lein-versions {:fireworks-version "0.16.1" :test-refresh-version "0.26.0"})

(defn- project-map
  "Turn a (defproject name version :k v ...) into the {:k v} option map."
  [text]
  (apply hash-map (drop 3 (edn/read-string text))))

(def minimal-project
  "(defproject demo \"0.1.0\"\n  :dependencies [[org.clojure/clojure \"1.12.0\"]])\n")

(deftest lein-create-from-minimal
  (let [out (cfg/ensure-lein-setup minimal-project lein-versions)
        m   (project-map out)]
    (testing "result is still a parseable defproject"
      (is (cfg/read-options out)))                          ; non-error => parsed :test-refresh
    (testing "plugin added with the test-refresh version"
      (is (= [['com.jakemccrary/lein-test-refresh "0.26.0"]] (:plugins m))))
    (testing "Fireworks scoped to :dev :dependencies (not top-level :dependencies)"
      (is (= [['io.github.paintparty/fireworks "0.16.1"]]
             (get-in m [:profiles :dev :dependencies])))
      (is (= [['org.clojure/clojure "1.12.0"]] (:dependencies m))))  ; untouched
    (testing ":test-refresh options map created from tap defaults"
      (is (= true (get-in m [:test-refresh :debug]))))))

(deftest lein-merge-preserves-unrelated
  (let [in  "(defproject demo \"0.1.0\"\n  :description \"hi\"\n  :dependencies [[org.clojure/clojure \"1.12.0\"]]\n  :plugins [[lein-ancient \"1.0\"]]\n  :test-refresh {:quiet false} ; mine\n  :profiles {:dev {:dependencies [[midje \"1.10\"]]}})\n"
        out (cfg/ensure-lein-setup in lein-versions)
        m   (project-map out)]
    (testing "existing plugin survives, managed plugin appended"
      (is (= [['lein-ancient "1.0"] ['com.jakemccrary/lein-test-refresh "0.26.0"]]
             (:plugins m))))
    (testing "existing :dev dependency survives, Fireworks appended"
      (is (= [['midje "1.10"] ['io.github.paintparty/fireworks "0.16.1"]]
             (get-in m [:profiles :dev :dependencies]))))
    (testing "pre-existing :test-refresh (user's, the source of truth) is left untouched"
      (is (= {:quiet false} (:test-refresh m)))
      (is (str/includes? out "; mine")))
    (testing "unrelated :description survives"
      (is (= "hi" (:description m))))))

(deftest lein-idempotent
  (testing "re-running over the produced project.clj changes nothing"
    (let [once (cfg/ensure-lein-setup minimal-project lein-versions)]
      (is (= once (cfg/ensure-lein-setup once lein-versions))))))

(deftest lein-version-update
  (testing "re-running with new versions updates the managed plugin and dep in place"
    (let [old (cfg/ensure-lein-setup minimal-project
                                     {:fireworks-version "0.10.0" :test-refresh-version "0.20.0"})
          new (cfg/ensure-lein-setup old lein-versions)
          m   (project-map new)]
      (is (= [['com.jakemccrary/lein-test-refresh "0.26.0"]] (:plugins m)))
      (is (= [['io.github.paintparty/fireworks "0.16.1"]]
             (get-in m [:profiles :dev :dependencies]))))))

(deftest lein-errors
  (testing "a non-defproject map -> :not-defproject (so TS doesn't mangle a stray file)"
    (is (= {:error :not-defproject} (cfg/ensure-lein-setup "{:a 1}" lein-versions))))
  (testing "unparseable -> :unparseable"
    (is (= {:error :unparseable} (cfg/ensure-lein-setup "(defproject demo " lein-versions)))))

;; ---------------------------------------------------------------------------
;; read-mode / set-mode polymorphism over a project.clj :test-refresh map
;; ---------------------------------------------------------------------------

(def lein-with-tr
  "(defproject demo \"0.1\"\n  :test-refresh {:debug true\n                 :banner \"x\"})\n")

(deftest lein-mode-read-and-set
  (testing "read-mode reads :debug from the defproject :test-refresh map"
    (is (= :tap (cfg/read-mode lein-with-tr))))
  (testing "set-mode flips the map in place and returns the whole project.clj"
    (let [out (cfg/set-mode lein-with-tr :test {:tap-banner "🔥" :test-banner "📋"})
          m   (project-map out)]
      (is (= false (get-in m [:test-refresh :debug])))
      (is (= "📋" (get-in m [:test-refresh :banner])))
      (is (str/includes? out "defproject demo"))
      (is (= :test (cfg/read-mode out)))))
  (testing "a lein project with no :test-refresh yet -> :no-options for set-mode"
    (is (= {:error :no-options}
           (cfg/set-mode "(defproject demo \"0.1\")" :tap {})))))

;; ---------------------------------------------------------------------------
;; extract-managed :lein (seed from ~/.lein/profiles.clj)
;; ---------------------------------------------------------------------------

(deftest extract-lein-from-profiles
  (testing "pulls plugin version, Fireworks dep version, and :test-refresh options from any profile"
    (let [global "{:user {:plugins [[com.jakemccrary/lein-test-refresh \"0.25.0\"]]\n :dependencies [[io.github.paintparty/fireworks \"0.15.0\"]]\n :test-refresh {:quiet true :debug true}}\n :other {:plugins [[lein-ancient \"1.0\"]]}}"]
      (is (= {:test-refresh-version "0.25.0"
              :fireworks-version    "0.15.0"
              :options              {:quiet true :debug true}}
             (cfg/extract-managed global :lein)))))
  (testing "missing pieces -> nils, not an error"
    (is (= {:test-refresh-version nil :fireworks-version nil :options nil}
           (cfg/extract-managed "{:user {}}" :lein))))
  (testing "unparseable -> error"
    (is (= {:error :unparseable} (cfg/extract-managed "{:user " :lein)))))
