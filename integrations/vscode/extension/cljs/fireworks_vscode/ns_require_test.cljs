;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.ns-require-test
  "Spec for the pure Add-Fireworks-Require logic. Each case feeds the whole
   document to add-fireworks-require and asserts the resulting document (the plan
   spliced back into the source) or nil. add-plan reconstructs what the TS editor
   edit would produce, so the tests pin both the range and the insert text."
  (:require [cljs.test :refer [deftest is testing]]
            [clojure.string :as str]
            [fireworks-vscode.ns-require :as ns-require]))

(defn- ->offset [text line col]
  (let [lines (str/split text #"\n" -1)]
    (+ (apply + (map #(inc (count %)) (take line lines))) col)))

(defn- apply-plan
  "Splice a plan's insert-text into `text` over its replace-range, mirroring the
   TS editor.edit call."
  [text {:keys [replace-range insert-text]}]
  (let [{s :start e :end} replace-range
        a (->offset text (:line s) (:col s))
        b (->offset text (:line e) (:col e))]
    (str (subs text 0 a) insert-text (subs text b))))

(defn- added [text] (apply-plan text (ns-require/add-fireworks-require text)))

;; ---------------------------------------------------------------------------
;; No existing :require -> create one
;; ---------------------------------------------------------------------------

(deftest create-require-simple
  (testing "ns with no :require gets a :require created, indented 2 spaces"
    (is (= (str "(ns foo.bar\n"
                "  (:require [fireworks.core :refer [? !? ?> !?>]]))")
           (added "(ns foo.bar)")))))

(deftest create-require-plan-shape
  (testing "the plan confines its replace-range to the ns form (0-based, end excl.)"
    (is (= {:replace-range {:start {:line 0 :col 0} :end {:line 0 :col 12}}
            :insert-text   (str "(ns foo.bar\n"
                                "  (:require [fireworks.core :refer [? !? ?> !?>]]))")}
           (ns-require/add-fireworks-require "(ns foo.bar)")))))

(deftest create-require-with-docstring
  (testing "a docstring is preserved; :require appended after it"
    (is (= (str "(ns foo.bar\n"
                "  \"Doc.\"\n"
                "  (:require [fireworks.core :refer [? !? ?> !?>]]))")
           (added "(ns foo.bar\n  \"Doc.\")")))))

(deftest ns-not-first-form
  (testing "an ns preceded by a comment is still found and offsets are correct"
    (is (= (str ";; a comment\n"
                "(ns foo.bar\n"
                "  (:require [fireworks.core :refer [? !? ?> !?>]]))")
           (added ";; a comment\n(ns foo.bar)")))))

;; ---------------------------------------------------------------------------
;; Existing :require -> append, aligned under the libspecs
;; ---------------------------------------------------------------------------

(deftest append-to-single-libspec
  (testing "append under a single existing libspec, aligned to its column"
    (is (= (str "(ns foo.bar\n"
                "  (:require [clojure.string :as str]\n"
                "            [fireworks.core :refer [? !? ?> !?>]]))")
           (added (str "(ns foo.bar\n"
                       "  (:require [clojure.string :as str]))"))))))

(deftest append-to-multi-libspec
  (testing "append after the last of several libspecs, keeping alignment"
    (is (= (str "(ns foo.bar\n"
                "  (:require [clojure.string :as str]\n"
                "            [clojure.set :as set]\n"
                "            [fireworks.core :refer [? !? ?> !?>]]))")
           (added (str "(ns foo.bar\n"
                       "  (:require [clojure.string :as str]\n"
                       "            [clojure.set :as set]))"))))))

;; ---------------------------------------------------------------------------
;; No-op cases -> nil
;; ---------------------------------------------------------------------------

(deftest already-present-is-noop
  (testing "fireworks.core already required -> nil (no duplicate)"
    (is (nil? (ns-require/add-fireworks-require
               (str "(ns foo.bar\n"
                    "  (:require [fireworks.core :refer [? !? ?> !?>]]))"))))))

(deftest no-ns-form-is-noop
  (testing "a file with no ns form -> nil"
    (is (nil? (ns-require/add-fireworks-require "(defn foo [] 1)")))))
