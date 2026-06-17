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
      (is (= "📋 Running tests..." (:banner o)))
      (is (not (contains? o :debug-mode-opts))))))

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
