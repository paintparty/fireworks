
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
  (testing "Fireworks appended to top-level :deps, test-refresh into a new :live-code alias"
    (let [{:keys [text alias changed]}
          (deps/add-live-code-alias "{:paths [\"src\"] :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}}")]
      (is (= "live-code" alias))
      (is (true? changed))
      ;; Fireworks lives in the project :deps, not the alias :extra-deps
      (is (str/includes? text "io.github.paintparty/fireworks"))
      ;; the written alias must itself pass the readiness checks
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "live-code"))))))

(deftest add-live-code-alias-formatted
  (testing "the appended alias is cljfmt'd multi-line, not a single flat line, and comments survive"
    (let [{:keys [text]}
          (deps/add-live-code-alias
           "{:paths [\"src\"]\n\n ;; keep this comment\n :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}\n\n :aliases\n {:release {:jvm-opts [\"-Dfireworks.elide=true\"]}}}")]
      ;; each alias-body entry on its own line (newline + indent before the keys)
      (is (re-find #"\n\s+:extra-deps" text))
      (is (re-find #"\n\s+:main-opts" text))
      ;; the user's comment and existing alias are preserved
      (is (str/includes? text ";; keep this comment"))
      (is (str/includes? text ":release {:jvm-opts [\"-Dfireworks.elide=true\"]}")))))

(deftest add-live-code-alias-no-deps-map
  (testing "no :deps map at all -> one is created carrying Fireworks"
    (let [{:keys [text]} (deps/add-live-code-alias "{:paths [\"src\"]}")]
      (is (str/includes? text "io.github.paintparty/fireworks"))
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "live-code"))))))

(deftest add-live-code-alias-fireworks-already-top-level
  (testing "Fireworks already a top-level dep -> not duplicated, alias still added"
    (let [in   "{:deps {io.github.paintparty/fireworks {:mvn/version \"0.21.0\"}}}"
          {:keys [text]} (deps/add-live-code-alias in)]
      ;; only one Fireworks coordinate in the result
      (is (= 1 (count (re-seq #"io\.github\.paintparty/fireworks" text))))
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "live-code"))))))

(deftest add-live-code-alias-name-collision
  (testing ":live-code taken -> :fireworks-live-code, original alias untouched, block parameterized"
    (let [{:keys [text alias]}
          (deps/add-live-code-alias "{:aliases {:live-code {:main-opts [\"-m\" \"whatever\"]}}}")]
      (is (= "fireworks-live-code" alias))
      ;; the user's :live-code alias is left exactly as it was
      (is (str/includes? text ":live-code {:main-opts [\"-m\" \"whatever\"]}"))
      ;; the comment block names the actual alias
      (is (str/includes? text ";; Example :fireworks-live-code alias"))
      (is (str/includes? text "`clojure -M:fireworks-live-code`"))
      (is (= {:has-test-refresh true :has-fireworks true :main-opts "test-refresh"}
             (deps/alias-deps-status text "fireworks-live-code"))))))

(deftest add-live-code-alias-comment-block
  (testing "the alias comment block sits between :aliases and its map; :deps gets no elide comment"
    (let [{:keys [text]}
          (deps/add-live-code-alias "{:paths [\"src\"] :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}}")
          lines (vec (str/split-lines text))
          al-i  (first (keep-indexed (fn [i l] (when (re-find #":aliases\s*$" l) i)) lines))
          fw-i  (first (keep-indexed (fn [i l] (when (str/includes? l "io.github.paintparty/fireworks") i)) lines))]
      ;; the block, with its own elide guidance, is present
      (is (str/includes? text ";; Example :live-code alias, which is added by the Fireworks VSCode extension."))
      (is (str/includes? text ";; Elide Fireworks for non-dev builds -> {:jvm-opts [\"-Dfireworks.elide=true\"]}"))
      ;; block comes right after the :aliases line (between :aliases and its {)
      (is (str/includes? (nth lines (inc al-i)) ";; Example :live-code alias"))
      ;; the :deps coordinate is plain — no elide comment on the line above it
      (is (not (str/includes? (nth lines (dec fw-i)) ";; Elide")))
      (is (not (str/includes? text ";; Elide Fireworks for non-dev builds with"))))))

;; --- ensure-fireworks (standalone deps patch, no alias) -------------------

(deftest ensure-fireworks-patches-when-missing
  (testing "adds Fireworks (plain, no comment) to top-level :deps; existing aliases untouched"
    (let [in "{:paths [\"src\"]\n :deps {org.clojure/clojure {:mvn/version \"1.12.0\"}}\n :aliases\n {:live-code {:extra-deps {com.jakemccrary/test-refresh {:mvn/version \"0.26.0\"}}\n              :main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]}}}"
          {:keys [text changed]} (deps/ensure-fireworks in)]
      (is (true? changed))
      (is (str/includes? text "io.github.paintparty/fireworks"))
      ;; no comment is inserted into the :deps map on this path
      (is (not (str/includes? text ";; Elide")))
      ;; the alias is left alone, and Fireworks is now on its classpath
      (is (str/includes? text ":main-opts [\"-m\" \"com.jakemccrary.test-refresh\"]"))
      (is (:has-fireworks (deps/alias-deps-status text "live-code"))))))

(deftest ensure-fireworks-noop-when-present
  (testing "Fireworks already a top-level dep -> unchanged"
    (let [in "{:deps {io.github.paintparty/fireworks {:mvn/version \"0.21.0\"}}}"
          {:keys [text changed]} (deps/ensure-fireworks in)]
      (is (false? changed))
      (is (= in text)))))

(deftest ensure-fireworks-unparseable
  (testing "broken EDN -> error"
    (is (= {:error :unparseable} (deps/ensure-fireworks "{:deps {")))))
