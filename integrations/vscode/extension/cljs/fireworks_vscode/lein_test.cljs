
(ns fireworks-vscode.lein-test
  "Spec for reading/editing a Leiningen project.clj (and ~/.lein/profiles.clj) for live coding."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.config :as config]
            [fireworks-vscode.lein :as lein]))

(def ^:private eligible-coord "[com.jakemccrary/lein-test-refresh \"0.26.0\"]")

;; --- profiles -------------------------------------------------------------

(deftest profiles-eligible
  (testing "a profile whose :plugins carries the exact coordinate is eligible"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.11.1\"]]\n"
                    "  :profiles {:dev {:plugins [" eligible-coord "]}\n"
                    "             :test {}})")]
      (is (= {:all ["dev" "test"] :eligible ["dev"]} (lein/profiles text))))))

(deftest profiles-wrong-version-not-eligible
  (testing "wrong version stays in :all but not :eligible"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh \"0.27.0\"]]}})")]
      (is (= {:all ["dev"] :eligible []} (lein/profiles text))))))

(deftest profiles-namespaced-key
  (testing "namespaced profile keys keep their namespace"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev/ci {:plugins [" eligible-coord "]}})")]
      (is (= {:all ["dev/ci"] :eligible ["dev/ci"]} (lein/profiles text))))))

(deftest profiles-none
  (testing "no :profiles entry -> empty vectors (parses fine)"
    (is (= {:all [] :eligible []}
           (lein/profiles "(defproject my-app \"0.1.0\" :dependencies [])")))
    (testing "name + version only"
      (is (= {:all [] :eligible []} (lein/profiles "(defproject my-app \"0.1.0\")"))))))

(deftest profiles-unparseable
  (testing "broken text -> error so TS can abort"
    (is (= {:error :unparseable} (lein/profiles "(defproject my-app")))))

;; --- add-plugin-to-profile ------------------------------------------------

(deftest add-plugin-to-existing-plugins
  (testing "appends the coordinate to an existing :plugins vector"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:plugins [[lein-ancient \"1.0.0\"]]}})")
          {:keys [text changed]} (lein/add-plugin-to-profile text "dev")]
      (is changed)
      (is (str/includes? text "lein-ancient"))
      (is (str/includes? text eligible-coord))
      ;; now eligible
      (is (= ["dev"] (:eligible (lein/profiles text)))))))

(deftest add-plugin-creates-plugins
  (testing "creates a :plugins vector when the profile lacks one"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:source-paths [\"dev\"]}})")
          {:keys [text changed]} (lein/add-plugin-to-profile text "dev")]
      (is changed)
      (is (= ["dev"] (:eligible (lein/profiles text)))))))

(deftest add-plugin-already-present
  (testing "no-op when the exact coordinate is already there"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:plugins [" eligible-coord "]}})")
          {:keys [changed]} (lein/add-plugin-to-profile text "dev")]
      (is (false? changed)))))

(deftest add-plugin-replaces-wrong-version
  (testing "replaces a wrong-version lein-test-refresh instead of duplicating"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh \"0.27.0\"]]}})")
          {:keys [text changed]} (lein/add-plugin-to-profile text "dev")]
      (is changed)
      (is (str/includes? text "\"0.26.0\""))
      (is (not (str/includes? text "\"0.27.0\"")))
      (is (= ["dev"] (:eligible (lein/profiles text)))))))

(deftest add-plugin-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/add-plugin-to-profile "(defproject" "dev")))))

;; --- ensure-test-refresh --------------------------------------------------

(deftest ensure-test-refresh-adds-when-absent
  (testing "adds the full baseline :test-refresh when absent"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.11.1\"]])")
          {:keys [text changed added-keys]} (lein/ensure-test-refresh text)]
      (is changed)
      (is (= (mapv name (keys config/baseline)) added-keys))
      (is (str/includes? text ":test-refresh"))
      ;; the new map round-trips as the top-level :test-refresh
      (is (str/includes? text ":quiet"))
      ;; the commented, aligned block is inserted verbatim (multi-line, not a flat map)
      (is (str/includes? text ";; `test-refresh` options."))
      (is (str/includes? text ":changes-only      true"))
      (is (str/includes? text ":notify-on-success false")))))

(deftest ensure-test-refresh-merges-missing
  (testing "merges only the missing baseline keys, leaving existing values untouched"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :test-refresh {:quiet false :changes-only true})")
          {:keys [text changed added-keys]} (lein/ensure-test-refresh text)]
      (is changed)
      (is (not (some #{"quiet" "changes-only"} added-keys)))
      (is (some #{"debug"} added-keys))
      ;; existing user value preserved
      (is (str/includes? text ":quiet false")))))

(deftest ensure-test-refresh-noop-when-complete
  (testing "no change when every baseline key is already present"
    (let [full (str "{" (->> config/baseline
                             (map (fn [[k v]] (str k " " (pr-str v))))
                             (str/join " ")) "}")
          text (str "(defproject my-app \"0.1.0\" :test-refresh " full ")")
          {:keys [changed added-keys]} (lein/ensure-test-refresh text)]
      (is (false? changed))
      (is (= [] added-keys)))))

(deftest ensure-test-refresh-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/ensure-test-refresh "(defproject")))))

;; --- user-profile-status (~/.lein/profiles.clj) ---------------------------

(deftest user-profile-plugin-and-test-refresh
  (testing "plugin nested in :user and a :test-refresh key both detected"
    (let [text (str "{:user {:plugins [" eligible-coord "]\n"
                    "        :test-refresh {:quiet true}}}")]
      (is (= {:has-plugin true :has-test-refresh true} (lein/user-profile-status text))))))

(deftest user-profile-plugin-no-test-refresh
  (testing "plugin present but no :test-refresh in :user"
    (let [text (str "{:user {:plugins [" eligible-coord "]}}")]
      (is (= {:has-plugin true :has-test-refresh false} (lein/user-profile-status text))))))

(deftest user-profile-no-plugin
  (testing "no coordinate anywhere in :user"
    (is (= {:has-plugin false :has-test-refresh false}
           (lein/user-profile-status "{:user {:plugins [[lein-ancient \"1.0.0\"]]}}")))))

(deftest user-profile-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/user-profile-status "{:user ")))))

;; --- snippet --------------------------------------------------------------

(deftest snippet-renders-baseline
  (testing "the snippet mentions :test-refresh and the baseline keys"
    (let [s (lein/test-refresh-snippet)]
      (is (str/includes? s ":test-refresh"))
      (is (str/includes? s ":quiet")))))
