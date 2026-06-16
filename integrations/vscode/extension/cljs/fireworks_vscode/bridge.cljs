;; Copyright (c) Jeremiah Coyle
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
;; version 2 or any later version with the GNU Classpath Exception which is
;; available at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

(ns fireworks-vscode.bridge
  "The only namespace that touches JS. Converts JS opts -> clj (keywordized;
   variant stays the string \"?\"/\"?>\"), runs the pure logic, and builds the
   outgoing JS plan with camelCase keys so the TS side reads plan.replaceRange,
   plan.insertText, plan.newCursor, plan.reformat. nil -> JS null.

   Phase 2 config functions follow the same discipline: JS in (camelCase opts,
   string text/mode/file-kind), pure fireworks-vscode.config logic, JS result
   envelopes out. Text-producing functions return #js {:text ...} on success or
   #js {:error \"...\"} on failure; reads return #js {:mode ...} / #js {:options ...}
   / the managed fields, each with #js {:error ...} on failure. Option maps round-
   trip as plain objects with edn-keyed string keys (clj->js / js->clj keywordize)."
  (:require [fireworks-vscode.config :as config]
            [fireworks-vscode.toggle :as toggle]))

(defn- pos->js [{:keys [line col]}]
  #js {:line line :col col})

(defn- plan->js [{:keys [replace-range insert-text new-cursor reformat?]}]
  #js {:replaceRange #js {:start (pos->js (:start replace-range))
                          :end   (pos->js (:end replace-range))}
       :insertText   insert-text
       :newCursor    (pos->js new-cursor)
       :reformat     (boolean reformat?)})

(defn toggle-form [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true) ; variant kept as "?"/"?>"
        plan (toggle/toggle-form opts)]
    (when plan (plan->js plan))))

;; --- Phase 2 config -------------------------------------------------------

(defn- error? [r] (and (map? r) (:error r)))
(defn- err->js [r] #js {:error (name (:error r))})

(defn- text-result
  "Shape a config text-producer result: a string -> {:text ...}, an {:error e}
   map -> {:error \"e\"}."
  [r]
  (if (error? r) (err->js r) #js {:text r}))

(defn ensure-deps-alias [text ^js opts]
  (text-result
   (config/ensure-deps-alias
    text
    {:fireworks-version    (.-fireworksVersion opts)
     :test-refresh-version (.-testRefreshVersion opts)})))

(defn ensure-lein-setup [text ^js opts]
  (text-result
   (config/ensure-lein-setup
    text
    {:fireworks-version    (.-fireworksVersion opts)
     :test-refresh-version (.-testRefreshVersion opts)
     :options              (when-let [o (.-options opts)]
                             (js->clj o :keywordize-keys true))})))

(defn write-options [text ^js options]
  (text-result (config/write-options text (js->clj options :keywordize-keys true))))

(defn read-options [text]
  (let [r (config/read-options text)]
    (if (error? r) (err->js r) #js {:options (clj->js r)})))

(defn read-mode [text]
  (let [r (config/read-mode text)]
    (if (error? r) (err->js r) #js {:mode (name r)})))

(defn set-mode [text mode ^js banners]
  (let [b (or banners #js {})]
    (text-result
     (config/set-mode text (keyword mode)
                       {:tap-banner (.-tapBanner b) :test-banner (.-testBanner b)}))))

(defn extract-managed [text file-kind]
  (let [r (config/extract-managed text (keyword file-kind))]
    (if (error? r)
      (err->js r)
      #js {:fireworksVersion   (:fireworks-version r)
           :testRefreshVersion (:test-refresh-version r)
           :options            (some-> (:options r) clj->js)})))

(defn default-options [mode]
  (clj->js (config/default-options (keyword mode))))
