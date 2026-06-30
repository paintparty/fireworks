
(ns fireworks-vscode.config-test
  "Spec for the pure .test-refresh.edn logic. Assertions parse the returned text and
   compare data (robust to whitespace), plus substring checks for comment survival."
  (:require [cljs.reader :as edn]
            [cljs.test :refer [deftest is testing]]
            [clojure.string :as str]
            [fireworks-vscode.config :as cfg]))

;; ---------------------------------------------------------------------------
;; default-config
;; ---------------------------------------------------------------------------

(deftest default-config-tap
  (testing "tap defaults: :debug true, fire banner, :debug-mode-opts present"
    (let [o (edn/read-string (cfg/default-config :tap))]
      (is (= true (:debug o)))
      (is (= true (:quiet o)))
      (is (= false (:notify-on-success o)))
      (is (= {:print-full-stack-trace? true} (:debug-mode-opts o)))
      (is (string? (:banner o))))))

(deftest default-config-test
  (testing "test defaults: :debug false, running banner, no :debug-mode-opts"
    (let [o (edn/read-string (cfg/default-config :test))]
      (is (= false (:debug o)))
      (is (= "🧪 Running tests..." (:banner o)))
      (is (not (contains? o :debug-mode-opts))))))

;; ---------------------------------------------------------------------------
;; template (the seed .test-refresh.edn)
;; ---------------------------------------------------------------------------

(deftest template-edn
  (testing "seed template parses as valid EDN with tap-mode defaults"
    (let [o (edn/read-string cfg/template)]
      (is (= true (:debug o)))   ; ships in debug/tap mode
      (is (= true (:quiet o)))
      (is (= true (:changes-only o)))
      (is (= false (:notify-on-success o)))
      (is (= true (:clear o)))
      (is (= "🔥" (:banner o)))                  ; active banner == :debug-banner in tap mode
      (is (= "🔥" (:debug-banner o)))             ; banner sources the toggle reads
      (is (= "🧪 Running tests..." (:test-banner o)))
      (is (not (contains? o :debug-mode-opts)))))
  (testing "guidance comments survive in the literal text"
    (is (str/includes? cfg/template "runs in debug mode"))
    (is (str/includes? cfg/template "auto-toggle the :banner"))))

;; ---------------------------------------------------------------------------
;; read-mode
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

;; ---------------------------------------------------------------------------
;; set-mode
;; ---------------------------------------------------------------------------

(deftest mode-set-roundtrip
  (let [banners {:tap-banner "🔥" :test-banner "📋"}
        tap-edn  "{:quiet true\n :changes-only true ; mine\n :debug true\n :banner \"🔥\"\n :debug-mode-opts {:print-full-stack-trace? true}\n :clear true}\n"]
    (testing "tap -> test flips :debug and swaps banner, leaving other keys + comments"
      (let [out (cfg/set-mode tap-edn :test banners)
            o   (edn/read-string out)]
        (is (= false (:debug o)))
        (is (= "📋" (:banner o)))
        (is (= true (:quiet o)))
        (is (= true (:clear o)))
        (is (= {:print-full-stack-trace? true} (:debug-mode-opts o))) ; left as-is
        (is (str/includes? out "; mine"))
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
        (is (= true (:quiet o)))))
    (testing "unparseable -> error"
      (is (= {:error :unparseable} (cfg/set-mode "{:debug " :tap banners))))))

;; ---------------------------------------------------------------------------
;; toggle-mode (used by the Toggle Debug / Test Mode command)
;; ---------------------------------------------------------------------------

(deftest mode-toggle
  (testing "debug -> test: flips :debug, syncs :banner from file's :test-banner, keeps comments"
    (let [out (cfg/toggle-mode cfg/template)
          o   (edn/read-string (:text out))]
      (is (= :test (:mode out)))
      (is (= false (:debug o)))
      (is (= "🧪 Running tests..." (:banner o)))      ; pulled from :test-banner
      (is (= "🔥" (:debug-banner o)))                 ; banner sources left intact
      (is (= "🧪 Running tests..." (:test-banner o)))
      (is (= true (:quiet o)))
      (is (str/includes? (:text out) "runs in debug mode")) ; comments survive
      (is (= :test (cfg/read-mode (:text out))))))
  (testing "test -> debug: flips back, syncs :banner from :debug-banner"
    (let [test-text (:text (cfg/toggle-mode cfg/template))
          out       (cfg/toggle-mode test-text)
          o         (edn/read-string (:text out))]
      (is (= :tap (:mode out)))
      (is (= true (:debug o)))
      (is (= "🔥" (:banner o)))                       ; pulled from :debug-banner
      (is (= :tap (cfg/read-mode (:text out))))))
  (testing "absent banner sources fall back to defaults"
    (let [out (cfg/toggle-mode "{:debug false}\n")
          o   (edn/read-string (:text out))]
      (is (= :tap (:mode out)))
      (is (= true (:debug o)))
      (is (= "🔥" (:banner o)))))                      ; tap-banner-default
  (testing "absent :debug reads as test -> toggles to debug"
    (is (= :tap (:mode (cfg/toggle-mode "{:quiet true}\n")))))
  (testing "unparseable -> error"
    (is (= {:error :unparseable} (cfg/toggle-mode "{:debug ")))))
