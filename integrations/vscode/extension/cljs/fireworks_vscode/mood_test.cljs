
(ns fireworks-vscode.mood-test
  "Spec for the BLING_MOOD decision, plus a drift guard that the mirrored stock-theme names stay in
   step with fireworks.basethemes/stock-themes (parsed off disk; cwd is the extension dir under
   :node-test)."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.mood :as mood]
            [rewrite-clj.zip :as z]
            ["fs" :as fs]))

(deftest theme-name-extraction
  (testing "the :theme string is pulled from EDN text; junk / non-string / missing -> nil"
    (is (= "Monokai Dark" (mood/theme-name "{:theme \"Monokai Dark\"}")))
    (is (nil? (mood/theme-name nil)))                       ; no config file
    (is (nil? (mood/theme-name "{:theme \"x\"")))           ; unparseable
    (is (nil? (mood/theme-name "{:some-other-key 1}")))     ; no :theme
    (is (nil? (mood/theme-name "{:theme :not-a-string}")))  ; inline/custom, not a name string
    (is (nil? (mood/theme-name "{:theme {:custom :map}}")))))

(deftest bling-mood-decision
  (testing "no theme -> force the editor mood"
    (is (= "dark" (mood/bling-mood nil "dark")))
    (is (= "light" (mood/bling-mood nil "light"))))
  (testing "Universal themes are mood-agnostic -> leave unset"
    (is (nil? (mood/bling-mood "Universal" "dark")))
    (is (nil? (mood/bling-mood "Universal Neutral" "light"))))
  (testing "stock theme whose variant matches the editor -> leave unset"
    (is (nil? (mood/bling-mood "Monokai Dark" "dark")))
    (is (nil? (mood/bling-mood "Solarized Light" "light"))))
  (testing "stock theme whose variant disagrees -> force the editor mood"
    (is (= "light" (mood/bling-mood "Monokai Dark" "light")))
    (is (= "dark" (mood/bling-mood "Solarized Light" "dark"))))
  (testing "custom (non-stock) theme -> force the editor mood"
    (is (= "dark" (mood/bling-mood "My Custom Theme" "dark")))
    (is (= "light" (mood/bling-mood "My Custom Theme" "light")))))

(deftest decision-shape
  (testing "decision carries the value plus the fields the output-channel log renders"
    (let [d (mood/decision "Monokai Dark" "light")]
      (is (= "light" (:value d)))
      (is (true? (:stock? d)))
      (is (= :dark (:variant d)))
      (is (string? (:reason d)))
      (is (str/includes? (:reason d) "disagrees")))
    (let [d (mood/decision nil "dark")]
      (is (= "dark" (:value d)))
      (is (false? (:stock? d)))
      (is (str/includes? (:reason d) "no :theme")))
    (let [d (mood/decision "Universal" "dark")]
      (is (nil? (:value d)))
      (is (str/includes? (:reason d) "mood-agnostic")))))

;; --- drift guard against fireworks.basethemes/stock-themes ----------------

(defn- basethemes-stock-names
  "The theme name strings under `stock-themes` in the real fireworks.basethemes source. rewrite-clj
   (not edn) because the map values are namespaced symbols (themes/…) that don't resolve here."
  []
  (let [text (.readFileSync fs "../../../src/fireworks/basethemes.cljc" "utf8")]
    (-> (z/of-string text)
        (z/find-value z/next 'stock-themes)
        z/right
        z/sexpr
        keys
        set)))

(deftest stock-theme-names-in-sync
  (testing "the mirrored stock-theme-variants covers exactly fireworks.basethemes/stock-themes"
    (is (= (basethemes-stock-names)
           (set (keys mood/stock-theme-variants))))))
