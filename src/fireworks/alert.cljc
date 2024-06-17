(ns fireworks.alert
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp]]
            [expound.alpha :as expound]
            [fireworks.specs.config :as config]
            [fireworks.util :as util :refer [char-repeat]]))

(def ^:private border-char "◢◤")
(def ^:private border-len 44)
(def ^:private alert-indent 4)
(defn- border-str [n] (string/join (repeat n border-char)))
(def ^:private unbroken-border (border-str (dec (/ border-len 2))))



(declare rich-console)

(defn- alert-tape
  "If passed a label option e.g. \"WARNING\" it will returns this:
   \"◢◤◢◤ WARNING ◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤\"

   If not passed a label option it will return this:
   ◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤
   
   Can be colorized with `:label-color` and :`tape-color` options.

   Can be made bold with `:label-font-weight` and :`tape-font-weight` options."
  [{:keys [label
           label-color
           label-font-weight
           tape-color
           tape-font-weight]
    :as opts}]
  (let [border-opts {:color       tape-color
                     :font-weight tape-font-weight}]
    (rich-console (if label 
                 (str
                  (rich-console (str border-char
                                  border-char)
                             border-opts)
                  (rich-console (str " " label " ")
                             {:color       label-color
                              :font-weight label-font-weight})
                  (rich-console 
                   (string/join
                    (repeat 
                     (/ (- border-len
                           (dec alert-indent)
                           (+ 2 (count label))
                           2)
                        2)
                     border-char))
                   border-opts))
                 (rich-console unbroken-border border-opts))
               (assoc opts :font-weight :bold))))


(defn alert-type-color [k]
  (case k
    :error    {:sgr 196
               :css "#ff4979"}

    :warning  {:sgr 208
              ;;  :sgr 214
               :css "#fe9959"}
    :info {:sgr 75
           :css "#5fafff"}
    {:sgr 201
     :css :#ff00ff}))



(defn rich-console [s opts]
  #?(:cljs
     (str "%c" s "%c" )
     :clj
     (str (util/m->sgr opts)
          s
          "\033[0;m")))

(defn message-body [x]
  (cond
    (util/nameable? x)
    (util/as-str x)
    (vector? x)
    (str "\n " (string/join "\n\n" x))))

;; TODO - Abstract sgr stuff out into utility fn
(defn bad-form [line gttr form alert-type]
  (let [opts {:color       (-> alert-type
                               alert-type-color 
                               :sgr)
              :font-weight :bold}
        squig (string/join (repeat (count form) "^"))]
    (str line " │ " (rich-console form {:font-weight :bold}) "\n"
         gttr " │ " (rich-console squig opts) "\n")))

(defn problem-with-line-info
  [{:keys [line] :as form-meta}
   {:keys [header
           form
           body
           alert-type]}]
  (let [file-info (util/form-meta->file-info form-meta) 
        gttr      (some-> line str count util/spaces) 
        form      (util/shortened form 33)]
    (str header "\n"
         "\n"
         gttr " ┌─ " file-info "\n"
         gttr " │  \n"
         (bad-form line gttr form alert-type)
         (message-body body))))

(def alert-type->label
  {:warning "WARNING"
   :error   "ERROR"
   :info    "INFO"})

(defn print-lines [n]
  (when (pos-int? n)
              (dotimes [_ n]
                (println))))

(defn default-spacing [n default]
  (if (and (int? n) (<= 0 n)) n default))



(defn console-alert
  [{:keys [label
           message
           alert-type
           margin-top
           margin-bottom
           padding-top
           padding-bottom
           theme]}]
  (let [tape?          true #_(= theme :tape)          
        padding-top    (default-spacing padding-top (if tape? 1 0))
        padding-bottom (default-spacing padding-bottom (if tape? 1 0))
        margin-top     (default-spacing margin-top 1)
        margin-bottom  (default-spacing margin-bottom 1)]
    #?(:cljs (let [js-arr (into-array
                           (concat [(str (char-repeat padding-top "\n")
                                         message
                                         (char-repeat padding-bottom "\n")
                                         "\n")]
                                   ["font-weight:bold;"
                                    "font-weight:normal;"
                                    (str "font-weight:bold;color:" (-> alert-type alert-type-color :css) ";")
                                    "font-weight:normal;color:default;"]))]
               (.apply (case alert-type 
                         :warning (.-warn  js/console)
                         :error (.-error  js/console)
                         (.-log  js/console))
                       js/console
                       js-arr))
       :clj (let [label          (or label
                                     (get alert-type->label
                                          alert-type
                                          nil))
                  tape-opts      {:label             label
                                    ;; :border-color      (-> alert-type
                                    ;;                        alert-type-color
                                    ;;                        :sgr)
                                  :label-font-weight :bold}
                  side-border-opts {:font-weight :bold
                                    :color       (-> alert-type alert-type-color :sgr)}]

              ;; With tape
              (print
               (str #?(:cljs nil :clj (char-repeat margin-top "\n"))
                    ;; (rich-console (str "┏━ " label) side-border-opts)
                    (str 
                     (alert-tape tape-opts) "\n"
                     (char-repeat padding-top "\n")
                     message "\n"
                     (char-repeat padding-bottom "\n")
                     (alert-tape (dissoc tape-opts :label)))

                    #_(char-repeat padding-bottom (str "\n" (rich-console "┃  " side-border-opts)))
                    #_(str "\n" (rich-console "┗━" side-border-opts))

                    ;;  "\n"
                    #?(:cljs nil :clj (char-repeat margin-bottom "\n")) "\n"))

              ;; simple
              (print
               (str #?(:cljs nil :clj (char-repeat margin-top "\n"))
                    (rich-console (str "┏━ " label) side-border-opts)
                    (string/replace 
                     (str 
                      ;; #_(alert-border border-opts) "\n"
                      
                      (char-repeat padding-top "\n") "\n"
                      message #_"\n"
                      #_(char-repeat padding-bottom "\n")
                      #_(alert-border (dissoc border-opts :label)))
                     #"\n"
                     (str "\n" (rich-console "┃  " side-border-opts)))

                    (char-repeat padding-bottom (str "\n" (rich-console "┃  " side-border-opts)))
                    (str "\n" (rich-console "┗━" side-border-opts))

                    ;;  "\n"
                    #?(:cljs nil :clj (char-repeat margin-bottom "\n")) "\n"))

              ;; (print-lines margin-top)
              ;; (println (alert-border border-opts))
              ;; (print-lines padding-top)
              ;; (println message)
              ;; (print-lines padding-bottom)
              ;; (println (alert-border (dissoc border-opts :label)))
              ;; (print-lines margin-bottom)
              
              )))
  nil)
