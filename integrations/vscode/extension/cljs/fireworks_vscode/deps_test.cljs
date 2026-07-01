
(ns fireworks-vscode.deps-test
  "Spec for reading alias names out of a deps.edn string."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.deps :as deps]))

(deftest alias-names-basic
  (testing "alias keys returned in file order, colon dropped"
    (is (= ["test" "test-refresh"]
           (deps/alias-names
            "{:paths [\"src\"]
              :deps {}
              :aliases {:test {:extra-paths [\"test\"]}
                        :test-refresh {:extra-deps {}}}}")))))

(deftest alias-names-qualified
  (testing "namespaced alias keys keep their namespace"
    (is (= ["build" "my/runner"]
           (deps/alias-names
            "{:aliases {:build {} :my/runner {}}}")))))

(deftest alias-names-no-aliases
  (testing "no :aliases key -> empty vector (parses fine, just nothing to pick)"
    (is (= [] (deps/alias-names "{:paths [\"src\"] :deps {}}")))
    (is (= [] (deps/alias-names "{}")))))

(deftest alias-names-unparseable
  (testing "broken EDN -> nil so TS can abort"
    (is (nil? (deps/alias-names "{:aliases {:test ")))
    (is (nil? (deps/alias-names "}}}not edn")))))

(deftest alias-deps-status-canonical
  (testing "canonical wiring: test-refresh in the alias :extra-deps, Fireworks in project :deps"
    (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
           (deps/alias-deps-status
            "{:paths [\"src\"]
              :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}
                     io.github.paintparty/fireworks {:mvn/version \"0.21.0\"}}
              :aliases
              {:live-code
               {:extra-paths [\"test\"]
                :extra-deps {com.jakemccrary/test-refresh {:mvn/version \"0.26.0\"}}
                :main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
            "live-code")))))

(deftest alias-deps-status-missing-both
  (testing "the reported bug: :main-opts invokes test-refresh but no deps pull it (or Fireworks) in"
    (is (= {:has-test-refresh false :has-fireworks false :main-opts "test-refresh"}
           (deps/alias-deps-status
            "{:paths [\"src\"]
              :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}
              :aliases
              {:live-code
               {:extra-paths [\"test\"]
                :main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
            "live-code")))))

(deftest alias-deps-status-artifact-name-match
  (testing "matched by artifact name: git/local coords and any version still count"
    (is (= {:has-test-refresh true :has-fireworks true :main-opts "none"}
           (deps/alias-deps-status
            "{:aliases
              {:live-code
               {:extra-deps {com.jakemccrary/test-refresh {:git/tag \"v0.26.0\" :git/sha \"abc\"}
                             io.github.paintparty/fireworks {:local/root \"../fireworks\"}}}}}"
            "live-code")))))

(deftest alias-deps-status-override-and-replace
  (testing "deps also seen via the alias :replace-deps / :override-deps maps"
    (is (= {:has-test-refresh true :has-fireworks true :main-opts "none"}
           (deps/alias-deps-status
            "{:aliases
              {:live-code
               {:replace-deps {com.jakemccrary/test-refresh {:mvn/version \"0.26.0\"}}
                :override-deps {io.github.paintparty/fireworks {:local/root \"../fw\"}}}}}"
            "live-code")))))

(deftest alias-deps-status-unparseable
  (testing "broken EDN -> nil so TS can surface + abort"
    (is (nil? (deps/alias-deps-status "{:aliases {:live-code " "live-code")))))

(deftest alias-deps-status-main-opts
  (testing ":main-opts classified as test-refresh / other / none"
    (is (= "test-refresh"
           (:main-opts (deps/alias-deps-status
                        "{:aliases {:live-code {:main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
                        "live-code"))))
    (is (= "other"
           (:main-opts (deps/alias-deps-status
                        "{:aliases {:dev {:main-opts [\"-m\" \"my.app.main\"]}}}"
                        "dev"))))
    (is (= "none"
           (:main-opts (deps/alias-deps-status
                        "{:aliases {:dev {:extra-paths [\"test\"]}}}"
                        "dev"))))))

;; --- additive editors -----------------------------------------------------

(deftest add-live-code-alias-fresh
  (testing "adds a :live-code alias carrying both deps + :main-opts; result re-parses correctly"
    (let [{:keys [text alias changed]}
          (deps/add-live-code-alias "{:paths [\"src\"] :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}}")]
      (is (= "live-code" alias))
      (is (true? changed))
      ;; the written alias must itself pass the readiness checks
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "live-code"))))))

(deftest add-live-code-alias-name-collision
  (testing ":live-code taken -> :fireworks-live-code, original alias untouched"
    (let [{:keys [text alias]}
          (deps/add-live-code-alias "{:aliases {:live-code {:main-opts [\"-m\" \"whatever\"]}}}")]
      (is (= "fireworks-live-code" alias))
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "fireworks-live-code"))))))

(deftest patch-alias-adds-missing-deps
  (testing "the reported bug case: :main-opts runs test-refresh but the deps aren't there"
    (let [{:keys [text changed added]}
          (deps/patch-alias
           "{:paths [\"src\"]
             :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}
             :aliases {:live-code {:extra-paths [\"test\"]
                                   :main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
           "live-code")]
      (is (true? changed))
      (is (some #{"com.jakemccrary/test-refresh"} added))
      (is (some #{"io.github.paintparty/fireworks"} added))
      ;; :main-opts + :extra-paths already present -> not re-added
      (is (not (some #{":main-opts"} added)))
      (is (not (some #{":extra-paths"} added)))
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "live-code"))))))

(deftest patch-alias-only-fireworks-missing
  (testing "test-refresh already present (different version) -> only Fireworks added, version kept"
    (let [{:keys [text added]}
          (deps/patch-alias
           "{:aliases {:live-code {:extra-deps {com.jakemccrary/test-refresh {:mvn/version \"0.25.0\"}}
                                   :main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
           "live-code")]
      (is (= ["io.github.paintparty/fireworks"] (filterv #(re-find #"fireworks|test-refresh" %) added)))
      (is (str/includes? text "0.25.0")) ; the user's pinned test-refresh version is untouched
      (is (:has-fireworks (deps/alias-deps-status text "live-code"))))))

(deftest patch-alias-adds-main-opts-when-absent
  (testing "deps present but no :main-opts -> :main-opts (and :extra-paths) added"
    (let [{:keys [text added]}
          (deps/patch-alias
           "{:deps {io.github.paintparty/fireworks {:mvn/version \"0.21.0\"}}
             :aliases {:dev {:extra-deps {com.jakemccrary/test-refresh {:mvn/version \"0.26.0\"}}}}}"
           "dev")]
      (is (some #{":main-opts"} added))
      (is (some #{":extra-paths"} added))
      (is (= "test-refresh" (:main-opts (deps/alias-deps-status text "dev")))))))

(deftest patch-alias-unknown-alias
  (testing "alias not found -> error"
    (is (= {:error :unparseable} (deps/patch-alias "{:aliases {:dev {}}}" "nope")))))
