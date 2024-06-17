(ns fireworks.messaging
  (:require [clojure.string :as string]
            [fireworks.pp :refer [?pp]]
            [expound.alpha :as expound]
            [fireworks.specs.config :as config]
            [fireworks.util :as util]))

(defrecord FireworksThrowable [err])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))



;; Helpers ----------------------------------------------------------------
(defn string-like? [v]
  (or (string? v)
      #?(:clj (-> v type str (= "java.util.regex.Pattern"))
         :cljs (-> v type str (= "#object[RegExp]")))
      (symbol? v)
      (keyword? v)
      (number? v)))

(defn shortened
  "Stringifies a collection and truncates the result with ellipsis 
   so that it fits on one line."
  [v limit]
  (let [limit  limit
        as-str (str v)]
    (if (> limit (count as-str))
      as-str
      (let [shortened*      (-> as-str
                                (string/split #"\n")
                                first)
            shortened       (if (< limit (count shortened*))
                              (let [ret          (take limit shortened*)
                                    string-like? (string-like? v)]
                                (str (string/join ret)
                                     (when-not string-like? " ")
                                     "..."))
                              shortened*)]
        shortened))))

(defn sgr-bold [s]
  (str "\033[1;m" s "\033[0;m"))

(def border-char "◢◤")

(defn border-str [n] (string/join (repeat n border-char)))

(def border-len 50)

(def alert-indent 4)

(def unbroken-border (border-str (/ border-len 2)))

(defn dq [v]
  (if (string? v) (str "\"" v "\"") v))

(defn simple-alert-header-border-top [header]
  (str border-char
       border-char
       " "
       (sgr-bold header)
       " "
       (string/join
        (repeat 
         (/ (- border-len
               (dec alert-indent)
               (+ 2 (count header))
               2)
            2)
         border-char))))

(def bad-value-with-red-underline
  (str "font-weight:bold;"
       "text-decoration-line: underline;"
       "text-decoration-style: wavy;"
       ;; "text-decoration-color: #ff5252;"
       "text-decoration-color: #ff00ff;"
       "text-underline-offset: 0.4em;"
       "text-decoration-thickness: 1.5px;"
       "line-height: 2.5em;"))

(def kv-with-bad-value
  ["font-weight:bold; line-height: 2em;"
   "font-weight:normal; line-height: 2em;"
   bad-value-with-red-underline
   "font-weight:normal"])

(def bad-value
  [bad-value-with-red-underline
   "font-weight:normal"])


;; Warning and error blocks  --------------------------------------------------------

(defn read-file-warning
  [{:keys [path header default]}]
  (let [path    (dq path)
        warning (str header
                     "\n\n"
                     #?(:cljs
                        (str "%c" path "%c")
                        :clj
                        (sgr-bold (str path))))
        body   (str warning 
                    (some->> (str "\n\n" default))
                    #?(:cljs "\n"))]
    #?(:cljs (let [js-arr (into-array 
                           (concat [body] bad-value))]
               (.apply (.-warn  js/console)
                       js/console
                       js-arr))
       :clj (println 
             (str "\n"
                  (simple-alert-header-border-top "WARNING")
                  "\n\n"
                  body
                  "\n\n"
                  unbroken-border
                  "\n")))
    nil))

(defn invalid-find-value-option [x]
  (println 
   (str "Problem with the supplied value for the :find (highlighting) option:" 
        "\n\n"
        (expound/expound-str ::config/find
                             x
                             {:print-specs? false})
        "\n\n"
        "Nothing will be highlighted")))


(defn bad-option-value-warning
  [{:keys [k v spec header default]}]
  (let [v       (dq v)
        warning (str header
                     "\n\n"
                     #?(:cljs
                        (str "%c" k "%c %c" v "%c")
                        :clj
                        (sgr-bold (str k " " v)))
                     "\n\n"
                     (expound/expound-str spec v {:print-specs? false})
                     (when default
                       (str "\n\n"
                            "The default value of `" default "` will be applied."))
                     #?(:cljs "\n"))]
    #?(:cljs (let [js-arr (into-array 
                           (concat [warning] kv-with-bad-value))]
               (.apply (.-warn  js/console)
                       js/console
                       js-arr))
       :clj (println 
             (str "\n"
                  (simple-alert-header-border-top "WARNING")
                  "\n\n"
                  warning
                  "\n\n"
                  unbroken-border
                  "\n")))
    nil))


(defn invalid-color-warning 
  [{:keys [header v k footer theme-token from-custom-badge-style?]}]
  (str header
       "\n\n"
       #?(:cljs
          (str "%c" k " " "\"" v "\"%c")
          :clj
          (str "\033[1;m" k " " "\"" v "\"\033[0;m"))
       
       (when from-custom-badge-style?
         (str "\n\n"
              (str "This is from a :badge-style map within"
                   " a user-supplied custom printer.")))
       "\n\n"
       "This color value should be a hex or named html color."
       "\n\n"
       (str "The fallback color value for the "
            theme-token 
            " theme token will be applied.")
       footer))


(defn color-warning [{:keys [theme-token] :as opts}]
  (let [header  (str "[fireworks.core/_p] Invalid color value for theme token " 
                     theme-token
                     ".")
        warning (str (invalid-color-warning (merge {:header header} opts)))]
    #?(:cljs (let [js-arr (into-array (concat [warning]
                                              ["font-weight:bold"
                                               "font-weight:normal"]))]
               (.apply (.-warn  js/console)
                       js/console
                       js-arr))
       :clj (do (println "\n")
                (println (simple-alert-header-border-top "WARNING"))
                (println)
                (println warning)
                (println)
                (println unbroken-border)
                (println)))
    nil))


;; New June 16 -------------------------------------------------------------------

(declare rich-console)

(defn alert-border
  [{:keys [label
           label-color
           border-color
           label-font-weight
           border-font-weight]
    :as opts}]
  (let [border-opts {:color       border-color
                     :font-weight border-font-weight}]
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

(defn ^:public css-style-string [m]
  (string/join ";"
               (map (fn [[k v]]
                      (str (name k)
                           ":"
                           (if (number? v) (str v) (name v))))
                    m)))

(defn alert-type-color [k]
  (case k
    :error    {:sgr 196
               :css "#ff4979"}

    :warning  {:sgr 208
              ;;  :sgr 214
               :css "#fe9959"}
    :info     {:sgr 75
               :css "#5fafff"}
    {:sgr 201
     :css :#ff00ff}))

(defn  alert-type->css [k]
  (css-style-string
   {:font-weight               :bold
    :text-decoration-line      :underline
    :text-decoration-style     :wavy
    :text-decoration-color     (-> k alert-type-color :css)
    :text-underline-offset     :0.3em
    :text-decoration-thickness :1.0px
    :line-height               :1.9em}))

(defn nameable? [x]
  (or (string? x) (keyword? x) (symbol? x)))

(defn as-str [x]
  (str (if (or (keyword? x) (symbol? x)) (name x) x)))

(defn char-repeat [n s]
  (when (pos-int? n)
    (string/join (repeat n (or s "")))))

(defn x->sgr [x k]
  (when x
    (let [n (if (= k :fg) 38 48)]
      (if (int? x)
        (str n ";5;" x)
        (let [[r g b _] x
              ret       (str n ";2;" r ";" g ";" b)]
          ret)))))

(defn m->sgr
  [{fgc*        :color
    bgc*        :background-color
    font-style  :font-style
    font-weight :font-weight
    k           :k
    :as         m}]
  (let [fgc    (x->sgr fgc* :fg)
        bgc    (do #_(when bgc* (println "m->sgr:bgc* " bgc*))
                   (x->sgr bgc* :bg))
        italic (when (and :enable-terminal-italics?
                          (contains? #{"italic" :italic} font-style))
                 "3;")
        weight (when (and :enable-terminal-font-weights?
                          (contains? #{"bold" :bold} font-weight))
                 ";1")
        ret    (str "\033[" 
                    italic
                    fgc
                    weight
                    (when (or (and fgc bgc)
                              (and weight bgc))
                      ";")
                    bgc
                    "m")]
    #_(when true
      (?pp "\nsgr..." m)
      (println "=>\n"
               (util/readable-sgr ret)
               "\n"))
    ret))

(defn rich-console [s opts]
  #?(:cljs
     (str "%c" s "%c" )
     :clj
     (str (m->sgr opts)
          s
          "\033[0;m")))

(defn message-body [x]
  (cond
    (nameable? x)
    (as-str x)
    (vector? x)
    (let [joined (string/join "\n\n" x)]
      #?(:cljs
         (str "\n" joined "\n")
         :clj
         (str "\n" joined)))))

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
        form      (shortened form 33)]
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

(defn default-padding [n default]
  (if (pos-int? n) n default))

(defn console-alert
  [{:keys [label
           message
           alert-type
           margin-top
           margin-bottom
           padding-top
           padding-bottom]}]
  (let [padding-top    (default-padding padding-top #?(:cljs 0 :clj 1))    
        padding-bottom (default-padding padding-bottom 1)]
    #?(:cljs (let [js-arr (into-array
                           (concat [(str 
                                     (char-repeat padding-top "\n")
                                     message
                                     (char-repeat padding-bottom "\n"))]
                                   ["font-weight:bold;"
                                    "font-weight:normal;"
                                    (str "font-weight:bold;color:" (-> alert-type alert-type-color :css) ";")
                                    "font-weight:normal;color:default;"
                                    ]))]
               (.apply (case alert-type 
                         :warning (.-warn  js/console)
                         :error (.-error  js/console)
                         (.-log  js/console))
                       js/console
                       js-arr))
       :clj (let [label       (or label (get alert-type->label alert-type nil))
                  border-opts {:label             label
                              ;;  :border-color      (-> alert-type
                              ;;                         alert-type-color
                              ;;                         :sgr)
                               :label-font-weight :bold}
                  side-border-opts {:font-weight :bold
                                    :color       (-> alert-type alert-type-color :sgr)}]
               (print
                (str #_(char-repeat "\n" margin-top)
                     (rich-console (str "\n┏━ " label) side-border-opts)
                     (string/replace 
                      (str 
                           #_(alert-border border-opts) "\n"

                           (char-repeat "\n" padding-top) "\n"
                           message "\n"
                           (char-repeat "\n" padding-bottom) "\n"

                           #_(alert-border (dissoc border-opts :label)))
                      #"\n"
                      (str "\n" (rich-console "┃  " side-border-opts)))
                    ;;  "\n"
                     #_(char-repeat "\n" margin-bottom) "\n"))

              ;; (print-lines margin-top)
              ;; (println (alert-border border-opts))
              ;; (print-lines padding-top)
              ;; (println message)
              ;; (print-lines padding-bottom)
              ;; (println (alert-border (dissoc border-opts :label)))
              ;; (print-lines margin-bottom)
              
              )))
  nil)

(defn unable-to-trace
  [{:keys [form-meta quoted-form alert-type]}]
  (console-alert
   {:alert-type    alert-type
    :margin-top    0
    :margin-bottom 0
    ;; :padding-top 6
    :padding-bottom 0
    :message       (problem-with-line-info 
                    form-meta
                    {:alert-type alert-type
                     :header     "Unable to trace form."
                     :form       (str "(?trace " quoted-form ")")
                     :body       [(str "fireworks.core/?trace will trace forms beginning with:\n"
                                       "-> , some->, ->>, some->>, let")]})}))

;; End new June 16 -------------------------------------------------------------------



 (defn print-user-friendly-clj-stack-trace [err]
   #?(:clj
      (let [st          (->> err .getStackTrace)
            msg         (->> err .getMessage)
            st-len      (count st)
            mini-st     (->> st (take 10) (map StackTraceElement->vec))
            indexes     (keep-indexed
                         (fn [i [f]]
                           (when (re-find #"^fireworks\.|^lasertag\."
                                          (str f)) 
                             i))
                         mini-st)
            last-index  (when (seq indexes)
                          (->> indexes (take 3) last))
            trace*      (when last-index
                          (->> mini-st (take (inc last-index))))
            len         (when trace* (count trace*)) 
            trace       (some->> trace* (interpose "\n") (into []))
            num-dropped (- (or st-len 0) (or len 0))
            trace       (when trace
                          (conj trace
                                "\n"
                                (symbol (str "..."
                                             (some->> num-dropped
                                                      (str "+"))))))]

        (println "Message:")
        (println msg)
        (println)
        (println "Stacktrace:")
        (println trace))))


(defn print-error [err]
  #?(:cljs
     (js/console.warn
      (str "[fireworks.core/_p]"
           "\n\n"
           "Exception caught. Nothing will be printed."
           "\n\n")
      err)
     :clj
     (do (println "\n")
         (println (simple-alert-header-border-top "CAUGHT EXCEPTION "))
         (println)
         (println
          "[fireworks.core/_p] Caught Exception, nothing will be printed.")
         (println)
         (print-user-friendly-clj-stack-trace err)
         (println unbroken-border) 
         (println)

         ;; Print the full error - leave this commented except
         ;; when devving on fireworks itself.
         ;; (println :full (.getStackTrace err))

         )))


;; Maybe this should go in core?
(defn print-formatted
  ([x]
   (print-formatted x nil))
  ([{:keys [fmt log? err] :as x} f]
   (if (instance? FireworksThrowable x)
     (print-error err)
     #?(:cljs
        (f x)
        :clj
        (do 
          (some-> fmt print)
          ;; Trailing line for readability.
          ;; Maybe make this a config option? (true by default).
          ((if log? print println) "\n"))))))

(def dispatch 
  {:messaging/bad-option-value-warning bad-option-value-warning
   :messaging/read-file-warning        read-file-warning
   :messaging/print-error              print-error})

;; Race-condition-free version of clojure.core/println,
;; maybe useful if any weird behavior arises
;; (defn safe-println [& more]
;;   (.write *out* (str (clojure.string/join " " more) "\n")))
