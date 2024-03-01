(ns fireworks.messaging
  (:require
   [expound.alpha :as expound]  
   [fireworks.specs.config :as config]
   [clojure.string :as string]))

(defrecord FireworksThrowable [err])

;; Compile time errors for surfacing to cljs browser console
(def warnings-and-errors (atom []))



;; Helpers ----------------------------------------------------------------
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
       "text-decoration-color: red;"
       "text-underline-offset: 0.4em;"
       "line-height: 2em;"))

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


(defn invalid-user-theme-warning [theme* mood fallback-theme]
  (println 
   (str "Problem with the supplied Fireworks theme \"" (:name theme*) "\":"
        "\n\n"
        (expound/expound-str ::config/theme
                             theme*
                             {:print-specs? false})
        "\n\n"
        "Falling back to default "
        (name mood)
        " Fireworks theme \""
        (:name fallback-theme)
        "\".")))




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
  (let [header  (str "[fireworks.core/p] Invalid color value for theme token " 
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


 (defn print-user-friendly-clj-stack-trace [err]
   #?(:clj
      (let [st          (->> err .getStackTrace)
            msg         (->> err .getMessage)
            st-len      (count st)
            mini-st     (->> st (take 10) (map StackTraceElement->vec))
            indexes     (keep-indexed
                         (fn [i [f]]
                           (when (re-find #"^fireworks\.|^typetag\."
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
      (str "[fireworks.core/p]"
           "\n\n"
           "Exception caught. Nothing will be printed."
           "\n\n")
      err)
     :clj
     (do (println "\n")
         (println (simple-alert-header-border-top "CAUGHT EXCEPTION "))
         (println)
         (println
          "[fireworks.core/p] Caught Exception, nothing will be printed.")
         (println)
         (print-user-friendly-clj-stack-trace err)
         (println unbroken-border) 
         (println)

         ;; Print the full error - leave this commented except
         ;; when devving on fireworks itself.
         ;; (println :full (.getStackTrace err))

         )))


(defn print-formatted
  ([x]
   (print-formatted x nil))
  ([{:keys [fmt err] :as x} f]
   (if (instance? FireworksThrowable x)
     (print-error err)
     #?(:cljs
        (f x)
        :clj
        (do (print fmt)
            ;; Trailing line for readability.
            ;; Maybe make this a config option? (true by default).
            (println "\n"))))))

(def dispatch 
  {:messaging/bad-option-value-warning bad-option-value-warning
   :messaging/read-file-warning        read-file-warning
   :messaging/print-error              print-error})

;; Race-condition-free version of clojure.core/println,
;; maybe useful if any weird behavior arises
;; (defn safe-println [& more]
;;   (.write *out* (str (clojure.string/join " " more) "\n")))
