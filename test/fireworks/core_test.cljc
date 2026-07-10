;; TODO - Add tests for:
;; - label display variations
;; - different :mode templates
;; - :when pred option for selective printing
;; - correct margins when using margin options
;; - correct margins when using :result flag

(ns fireworks.core-test
  (:require
   [clojure.string :as string]
   [fireworks.core :refer [? !? ?> !?>]]
   [clojure.test :refer [deftest is]]))


;; Basic tapping macros ---------------------------------------------------------

(deftest !?-par (is (= (!? "foo") "foo")))
(deftest ?>-par (is (= (?> "foo") "foo")))
(deftest !?>-par (is (= (!?> "foo") "foo")))


;; Tests for file-info and label/form annotation -------------------------------
;; These test the arrangement of the file info and label, and general
;; compositing of the options flags and / or option map.

(def xx {:a (take 10 (map inc (range 18))) :b "foo"})

(defn my-custom-printer [x] (println (str "HIHIHIH " x)))

(def dissoc-ks [:formatted-with-header
                :formatted
                :formatted+
                :display-label
                :line
                :column
                :file-info-str])

(defn data-result [x]
  (as-> x $ (apply dissoc $ dissoc-ks)))

(deftest annotation-default
  (is (= (data-result (? {:data? true} :fooo))
         {:quoted-form         :fooo,
          :threading?          nil,
          :template            [:file-info :form-or-label :result],
          :ns-str              "fireworks.core-test",
          :user-supplied-label nil,
          :truncate?           nil})))

(deftest annotation-just-result
  (is (= (data-result (? {:data?                  true
                          :display-file-info?     false
                          :display-label-or-form? false}
                         :foo))
         {:quoted-form         nil,
          :threading?          nil,
          :template            [:result],
          :ns-str              "fireworks.core-test",
          :user-supplied-label nil,
          :truncate?           nil})))

(deftest annotation-no-label
  (is (= (data-result (? {:data?                  true
                          :display-label-or-form? false}
                         :foo))
         {:quoted-form         nil,
          :threading?          nil,
          :template            [:file-info :result],
          :ns-str              "fireworks.core-test",
          :user-supplied-label nil,
          :truncate?           nil})))

(deftest annotation-no-file
  (is (= (data-result (? {:data?              true
                          :display-file-info? false}
                         :foo))
         {:quoted-form         :foo,
          :threading?          nil,
          :template            [:form-or-label :result],
          :ns-str              "fireworks.core-test",
          :user-supplied-label nil,
          :truncate?           nil})))

;; Note: a :label passed in an opts map is not surfaced in the output nor as
;; :user-supplied-label under {:data? true} (same behavior in fireworks
;; 0.21.3). The custom-label form is the leading string arg.
(deftest annotation-custom-label
  (let [out (with-out-str (? "custom label as string" xx))]
    (is (string/includes? out "custom label as string"))))


;; print-with and log? variants return the value (output silenced) -------------

(deftest print-with-prn-returns-value
  (is (= "ln1\nln2"
         (let [ret (atom nil)]
           (with-out-str (reset! ret (? {:print-with prn} "ln1\nln2")))
           @ret))))

(deftest print-with-custom-fn-returns-value
  (is (= :foo
         (let [ret (atom nil)]
           (with-out-str (reset! ret (? {:print-with my-custom-printer} :foo)))
           @ret))))

(deftest log?-returns-value
  (is (= xx
         (let [ret (atom nil)]
           (with-out-str (reset! ret (? {:log? true} xx)))
           @ret))))
