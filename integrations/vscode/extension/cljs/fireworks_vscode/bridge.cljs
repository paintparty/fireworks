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
   plan.insertText, plan.newCursor, plan.reformat. nil -> JS null."
  (:require [fireworks-vscode.toggle :as toggle]))

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
