;; Copyright © 2024-2025 Jeremiah Coyle

;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0.

;; This Source Code may also be made available under the following Secondary
;; Licenses when the conditions for such availability set forth in the Eclipse
;; Public License, v. 2.0 are satisfied: GNU General Public License as published by
;; the Free Software Foundation, either version 2 of the License, or (at your
;; option) any later version, with the GNU Classpath Exception which is available
;; at https://www.gnu.org/software/classpath/license.html.

;; {:name         "Toggle Fireworks"
;;  :description  "Joyride script providing editor support for Fireworks"
;;  :url          "https://github.com/paintparty/fireworks/integrations/vscode/joyride/scripts/"
;;  :license      {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
;;                 :url  "https://www.eclipse.org/legal/epl-2.0/"}
;;  :version      "0.1.0"
;;  :release-date {2025-11-20}}
:foo
(ns toggle-fireworks
  (:require ["vscode" :as vscode]
            [clojure.string :as string]
            [joyride.core :as joyride]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(defn text-for-selection-command [ed selection-command-id]
  (let [_         (vscode/commands.executeCommand selection-command-id)
        selection ed.selection
        code      (.getText ed.document ed.selection)]
    [code selection]))

;; For dev of this script
(do 
  (defn ? ([x] (? nil x)) ([x y] y))
  (defn !? ([x] (!? nil x)) ([x y] y))
  (defn ?> ([x] (?> nil x)) ([x y] y))
  (defn !?> ([x] (!?> nil x)) ([x y] y))
  (defn !?u ([x] (!?u nil x)) ([x y] y)))

(defn lo
  ([o]
   (lo nil o))
  ([s o]
   (println (str "\n\n" s)  (js/JSON.stringify o nil 2))))

(defn vim-enabled? []
  (when-let [vim-ext (.getExtension vscode/extensions "vscodevim.vim")]
    (.-isActive vim-ext)))

(defn fireworks-macro-sym [code]
  (and (string? code)
       (string/ends-with? code ")")
       (re-find #"^\((\s*\?|!\?|\?>|!\?>) *" code)))

(defn- reformat-code [code sym]
  (-> code 
      (string/split #"\n")
      (->> (map-indexed #(if (zero? %1) 
                           %2
                           (str (apply str (repeat (+ 2 (count sym)) " "))
                                %2))))
      (->> (string/join "\n"))))

(defn node-1 [zd]
  {:loc    (-> zd z/node meta)
   :string (-> zd z/string)})

(defn node-2 [zd]
  {:loc    (-> zd z/right z/node meta)
   :string (-> zd z/right z/string)})

(defn node-3 [zd]
  {:loc    (-> zd z/right z/right z/node meta)
   :string (-> zd z/right z/right z/string)})

(defn node-4 [zd]
  {:loc    (-> zd z/right z/right z/node meta)
   :string (-> zd z/right z/right z/right z/string)})


(defn fireworks-wrapped-form [s]
  (when (fireworks-macro-sym s)
    (let [sexpr (-> s p/parse-string n/sexpr)
          zloc  (z/of-string s)
          zd    (-> zloc z/down)
          n     (count sexpr)]
      (!? (-> zloc
              z/down
              z/right
              z/right
              z/string))
      (case n
        2 [(node-1 zd)
           (node-2 zd)]
        3 [(node-1 zd)
           (node-2 zd)
           (node-3 zd)]
        1 [(node-1 zd)]
        [(node-1 zd)
         (node-2 zd)
         (node-3 zd)
         (node-4 zd)]))))


(def debug? false)


(defn edit! [{:keys [ed selection s vim? line col reformat?]}]
  (.edit ed
         (fn [^js builder]
           (.delete builder selection)
           (.insert builder (.-start selection) s)
           (when vim? (.executeCommand vscode/commands "extension.vim_escape"))
           (js/setTimeout
            (fn []
              (let [new-position (new vscode/Position line col)]
                (set! (.-selection ed)
                      (new vscode/Selection new-position new-position)))
              (when reformat?
                (.executeCommand vscode/commands "calva-fmt.alignCurrentForm")))
            50))
         #js {:undoStopBefore true
              :undoStopAfter  false}))

(def inverse-macro-sym-by-macro
  {"?" "!?"
   "!?" "?"
   "?>" "!?>"
   "!?>" "?>"})

(defn- unwrap-fireworks-form 
  [forms sel-start-col opts sel-start-line]

  (println)
  (println 'unwrap-fireworks-form)

  (let [{last-node-as-str  :string
         {:keys [row col]} :loc} 
        (last forms)

        last-node-on-same-line?                                           
        (= 1 row)

        reformatted                                                       
        (-> last-node-as-str 
            (string/split #"\n")
            (->> (map-indexed #(if (zero? %1) 
                                 %2
                                 (subs %2
                                       (if last-node-on-same-line?
                                         (dec col)
                                         (- (dec col) sel-start-col))
                                       ))))
            (->> (string/join "\n")))]

    #_(comment
      (println "last-node-on-same-line?" last-node-on-same-line?)
      (println "sel-start-col" sel-start-col)
      (println "row" row)
      (println "col" col)
      (println "last" last-node-as-str)
      (println)
      (println "last-node-as-str\n")
      (println (str "|" last-node-as-str "|"))
      (println)
      (println "reformatted\n")
      (println reformatted))
    
    (edit! (assoc opts
                  :s
                  reformatted
                  :line
                  sel-start-line
                  :col
                  sel-start-col))))


(defn- selection-for-?-sym 
  [sel-start-line sel-start-col ed]
  (let [np-start (new vscode/Position sel-start-line sel-start-col)
        np-end   (new vscode/Position sel-start-line (inc sel-start-col))
        new-sel  (new vscode/Selection np-start np-end)]
    (set! (.-selection ed) new-sel)
    new-sel))


(defn main []
  (let [ed                ^js vscode/window.activeTextEditor                    
        [code selection*] (text-for-selection-command ed "calva.selectCurrentForm")
        sel-start-line    (-> selection* .-start .-line)
        sel-start-col     (-> selection* .-start .-character)
        double-form?      (boolean (re-find #"^\? .+" code))
        code              (if double-form? "?" code)
        selection         (if double-form?
                            (selection-for-?-sym sel-start-line sel-start-col ed)
                            selection*)
        vim?              (vim-enabled?)
        opts              {:ed        ed
                           :selection selection
                           :vim?      vim?}]

    #_(do 
        (println "code" code)
        (lo "selection*" selection*)
        (lo "selection" selection)
        (println "double-form?" inverse-macro-sym)
        (println "inverse-macro-sym" inverse-macro-sym)
        (println "sel-start-line" sel-start-line)
        (println "sel-end-line"   sel-end-line)
        (println "sel-start-col"  sel-start-col)
        (println "sel-end-col"    sel-end-col))

    (if-let [inverse-macro-sym (get inverse-macro-sym-by-macro code nil)]
      ;; -----------------------------------------------------------------------
      ;; The current form is a symbol bound to a fireworks debugging macro, so
      ;; we will toggle the macro to silence/unsilence printing
      ;; -----------------------------------------------------------------------
      (let [start-col sel-start-col
            line      sel-start-line
            col       (+ start-col (count inverse-macro-sym))]
        (println "\nInverting macro sym form")
        (edit! (assoc opts
                      :s
                      inverse-macro-sym
                      :line
                      line
                      :col
                      col
                      :reformat? 
                      true)))
      

      (if-let [forms (fireworks-wrapped-form code)]
        ;; ---------------------------------------------------------------------
        ;; The current form is an existing wrapped call to a fireworks macro
        ;; So we will unwrap it and try to preserve line formatting.
        ;; ---------------------------------------------------------------------
        (unwrap-fireworks-form forms sel-start-col opts sel-start-line)
        
        ;; ---------------------------------------------------------------------
        ;; The current form will be wrapped with a call to fireworks.core/?
        ;; ---------------------------------------------------------------------
        (let [code (if (re-find #"\n" code)
                     (reformat-code code "?")
                     code)]
          (println "\nwrapping with ?\n")
          (edit! (assoc opts
                        :s
                        (str "(? " code ")")
                        :line
                        sel-start-line
                        :col
                        sel-start-col)))))))


(when (= (joyride/invoked-script) joyride/*file*)
  (main))


(false? :ignore/meandyou)
