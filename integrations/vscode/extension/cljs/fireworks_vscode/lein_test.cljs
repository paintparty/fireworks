
(ns fireworks-vscode.lein-test
  "Spec for reading/editing a Leiningen project.clj (and ~/.lein/profiles.clj) for live coding."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.config :as config]
            [fireworks-vscode.lein :as lein]
            [fireworks-vscode.versions :as v]))

;; Built from the single source of truth so it follows a plugin-version bump automatically.
(def ^:private eligible-coord
  (str "[" v/lein-test-refresh-sym " \"" v/lein-test-refresh-version "\"]"))

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

;; --- add-live-code-profile ------------------------------------------------

(deftest add-live-code-profile-splices-when-no-profiles
  (testing "no :profiles -> splices a commented :live-code profile carrying the plugin"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]])")
          {:keys [text profile changed]} (lein/add-live-code-profile text)]
      (is changed)
      (is (= "live-code" profile))
      (is (str/includes? text eligible-coord))
      ;; the commented header from examples/leiningen-project rides along verbatim
      (is (str/includes? text ";; `lein with-profile +live-code test-refresh`."))
      ;; the fresh profile is now eligible and named :live-code
      (is (= ["live-code"] (:eligible (lein/profiles text)))))))

(deftest add-live-code-profile-assocs-into-existing
  (testing "existing :profiles (none eligible) -> assoc a fresh :live-code, leaving others intact"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:dev {:source-paths [\"dev\"]}})")
          {:keys [text profile changed]} (lein/add-live-code-profile text)]
      (is changed)
      (is (= "live-code" profile))
      (is (str/includes? text ":dev")) ; user's profile untouched
      (is (= {:all ["dev" "live-code"] :eligible ["live-code"]} (lein/profiles text))))))

(deftest add-live-code-profile-avoids-name-collision
  (testing "a taken :live-code (not eligible) -> the new profile is :fireworks-live-code"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :profiles {:live-code {:source-paths [\"dev\"]}})")
          {:keys [text profile changed]} (lein/add-live-code-profile text)]
      (is changed)
      (is (= "fireworks-live-code" profile))
      (is (= ["fireworks-live-code"] (:eligible (lein/profiles text)))))))

(deftest add-live-code-profile-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/add-live-code-profile "(defproject")))))

;; --- ensure-fireworks -----------------------------------------------------

(deftest ensure-fireworks-appends-when-absent
  (testing "no Fireworks anywhere -> append the coordinate to the top-level :dependencies"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]]\n"
                    "  :profiles {:live-code {:plugins [" eligible-coord "]}})")
          {:keys [text changed]} (lein/ensure-fireworks text)]
      (is changed)
      (is (str/includes? text "io.github.paintparty/fireworks"))
      (is (:has-fireworks (lein/fireworks-dep-status text)))
      ;; the existing dep is preserved and the vector still parses
      (is (str/includes? text "org.clojure/clojure")))))

(deftest ensure-fireworks-noop-top-level
  (testing "already a top-level dep -> unchanged"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]\n"
                    "                 [io.github.paintparty/fireworks \"0.21.0\"]])")
          {:keys [text changed]} (lein/ensure-fireworks text)]
      (is (false? changed))
      (is (= 1 (count (re-seq #"paintparty/fireworks" text)))))))

(deftest ensure-fireworks-noop-in-profile
  (testing "Fireworks already in a profile's :dependencies -> unchanged (it merges onto classpath)"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]]\n"
                    "  :profiles {:live-code {:dependencies [[io.github.paintparty/fireworks \"0.21.0\"]]}})")
          {:keys [changed]} (lein/ensure-fireworks text)]
      (is (false? changed)))))

(deftest ensure-fireworks-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/ensure-fireworks "(defproject")))))

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

(deftest user-profile-plugin-fireworks-and-test-refresh
  (testing "plugin, Fireworks dep, and a :test-refresh key all detected (the full global setup)"
    (let [text (str "{:user {:plugins [" eligible-coord "]\n"
                    "        :dependencies [[io.github.paintparty/fireworks \"0.21.0\"]]\n"
                    "        :test-refresh {:quiet true}}}")]
      (is (= {:has-plugin true :has-fireworks true :has-test-refresh true}
             (lein/user-profile-status text))))))

(deftest user-profile-no-fireworks
  (testing "plugin + :test-refresh present but no Fireworks dep -> has-fireworks false"
    (let [text (str "{:user {:plugins [" eligible-coord "]\n"
                    "        :test-refresh {:quiet true}}}")]
      (is (= {:has-plugin true :has-fireworks false :has-test-refresh true}
             (lein/user-profile-status text))))))

(deftest user-profile-plugin-no-test-refresh
  (testing "plugin present but no :test-refresh in :user"
    (let [text (str "{:user {:plugins [" eligible-coord "]}}")]
      (is (= {:has-plugin true :has-fireworks false :has-test-refresh false}
             (lein/user-profile-status text))))))

(deftest user-profile-no-plugin
  (testing "no coordinate anywhere in :user"
    (is (= {:has-plugin false :has-fireworks false :has-test-refresh false}
           (lein/user-profile-status "{:user {:plugins [[lein-ancient \"1.0.0\"]]}}")))))

(deftest user-profile-fireworks-artifact-name-match
  (testing "Fireworks matched by artifact name: any group/version counts"
    (let [text "{:user {:dependencies [[org.clojars.someone/fireworks \"9.9.9\"]]}}"]
      (is (true? (:has-fireworks (lein/user-profile-status text)))))))

(deftest user-profile-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/user-profile-status "{:user ")))))

;; --- Fireworks dependency -------------------------------------------------

(deftest fireworks-dep-top-level
  (testing "Fireworks in the top-level :dependencies is detected (canonical placement)"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]\n"
                    "                 [io.github.paintparty/fireworks \"0.21.0\"]]\n"
                    "  :profiles {:live-code {:plugins [" eligible-coord "]}})")]
      (is (= {:has-fireworks true} (lein/fireworks-dep-status text))))))

(deftest fireworks-dep-missing
  (testing "no Fireworks coordinate anywhere -> false"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]]\n"
                    "  :profiles {:live-code {:plugins [" eligible-coord "]}})")]
      (is (= {:has-fireworks false} (lein/fireworks-dep-status text))))))

(deftest fireworks-dep-in-profile
  (testing "Fireworks in a profile's :dependencies also counts (merges onto the classpath)"
    (let [text (str "(defproject my-app \"0.1.0\"\n"
                    "  :dependencies [[org.clojure/clojure \"1.12.0\"]]\n"
                    "  :profiles {:live-code {:dependencies [[io.github.paintparty/fireworks \"0.21.0\"]]\n"
                    "                         :plugins [" eligible-coord "]}})")]
      (is (= {:has-fireworks true} (lein/fireworks-dep-status text))))))

(deftest fireworks-dep-artifact-name-match
  (testing "matched by artifact name: any group/version counts"
    (let [text "(defproject my-app \"0.1.0\" :dependencies [[org.clojars.someone/fireworks \"9.9.9\"]])"]
      (is (= {:has-fireworks true} (lein/fireworks-dep-status text))))))

(deftest fireworks-dep-unparseable
  (testing "broken text -> error"
    (is (= {:error :unparseable} (lein/fireworks-dep-status "(defproject my-app ")))))

;; --- snippet --------------------------------------------------------------

(deftest snippet-renders-baseline
  (testing "the snippet mentions :test-refresh and the baseline keys"
    (let [s (lein/test-refresh-snippet)]
      (is (str/includes? s ":test-refresh"))
      (is (str/includes? s ":quiet")))))
