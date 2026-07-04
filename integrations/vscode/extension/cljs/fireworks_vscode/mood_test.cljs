
(ns fireworks-vscode.mood-test
  "Spec for the FIREWORKS_THEME decision, plus a drift guard that the mirrored stock-theme names stay in
   step with fireworks.basethemes/stock-themes (parsed off disk; cwd is the extension dir under
   :node-test)."
  (:require [clojure.string :as str]
            [cljs.test :refer [deftest is testing]]
            [fireworks-vscode.mood :as mood]
            [rewrite-clj.zip :as z]
            ["fs" :as fs]))

(deftest fireworks-theme-decision
  (testing "unset -> force the editor mood"
    (is (= "dark" (mood/fireworks-theme nil "dark")))
    (is (= "light" (mood/fireworks-theme "" "light")))            ; blank counts as unset
    (is (= "dark" (mood/fireworks-theme "  " "dark"))))           ; whitespace too
  (testing "stock theme whose variant matches the editor -> leave as-is (nil)"
    (is (nil? (mood/fireworks-theme "Monokai Dark" "dark")))
    (is (nil? (mood/fireworks-theme "Solarized Light" "light"))))
  (testing "stock theme whose variant disagrees -> force the SIBLING (same family, editor variant)"
    (is (= "Monokai Light" (mood/fireworks-theme "Monokai Dark" "light")))
    (is (= "Solarized Dark" (mood/fireworks-theme "Solarized Light" "dark")))
    (is (= "Alabaster Light" (mood/fireworks-theme "Alabaster Dark" "light"))))
  (testing "mood-agnostic Universal stock themes -> leave as-is (nil)"
    (is (nil? (mood/fireworks-theme "Universal" "dark")))
    (is (nil? (mood/fireworks-theme "Universal Neutral" "light"))))
  (testing "bare mood word neutral -> force FIREWORKS_THEME=neutral"
    (is (= "neutral" (mood/fireworks-theme "neutral" "dark")))
    (is (= "neutral" (mood/fireworks-theme "neutral" "light"))))
  (testing "bare mood word light/dark -> leave as-is when it agrees, force editor mood when not"
    (is (nil? (mood/fireworks-theme "dark" "dark")))
    (is (= "light" (mood/fireworks-theme "dark" "light")))
    (is (= "dark" (mood/fireworks-theme "light" "dark"))))
  (testing "custom / unrecognized value -> leave as-is (nil), don't clobber"
    (is (nil? (mood/fireworks-theme "My Custom Theme" "dark")))
    (is (nil? (mood/fireworks-theme "light:Alabaster Light, dark:Alabaster Dark" "dark")))))

(deftest decision-shape
  (testing "decision carries the value + fields the output-channel log renders"
    (let [d (mood/decision "Monokai Dark" "light")]
      (is (= "Monokai Light" (:value d)))
      (is (= "Monokai Dark" (:input d)))
      (is (string? (:reason d)))
      (is (str/includes? (:reason d) "disagrees")))
    (let [d (mood/decision nil "dark")]
      (is (= "dark" (:value d)))
      (is (nil? (:input d)))
      (is (str/includes? (:reason d) "unset")))
    (let [d (mood/decision "neutral" "dark")]
      (is (= "neutral" (:value d)))
      (is (str/includes? (:reason d) "neutral")))))

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
