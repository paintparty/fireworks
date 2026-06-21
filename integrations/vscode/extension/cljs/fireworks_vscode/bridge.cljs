
(ns fireworks-vscode.bridge
  "The only namespace that touches JS. Converts JS opts -> clj (keywordized;
   variant stays the string \"?\"/\"?>\"), runs the pure logic, and builds the
   outgoing JS plan with camelCase keys so the TS side reads plan.replaceRange,
   plan.insertText, plan.newCursor, plan.reformat. nil -> JS null.

   Phase 2 config functions follow the same discipline: JS in (string text/mode),
   pure fireworks-vscode.config logic, JS result envelopes out. setMode returns
   #js {:text ...} on success or #js {:error \"...\"}; readMode returns #js {:mode ...}
   or #js {:error ...}; defaultConfig returns the rendered text directly."
  (:require [fireworks-vscode.config :as config]
            [fireworks-vscode.deps :as deps]
            [fireworks-vscode.inline-results :as inline-results]
            [fireworks-vscode.ns-require :as ns-require]
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

(defn unwrap-all [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/unwrap-all opts)]
    (when plan (plan->js plan))))

(defn toggle-all-silent [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/toggle-all-silent opts)]
    (when plan (plan->js plan))))

;; :option arrives as a JS string (":+" …) or null; js->clj keeps it a string / nil.
(defn set-form-option [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/set-form-option opts)]
    (when plan (plan->js plan))))

(defn add-fireworks-require [text]
  (when-let [{:keys [replace-range insert-text]} (ns-require/add-fireworks-require text)]
    #js {:replaceRange #js {:start (pos->js (:start replace-range))
                            :end   (pos->js (:end replace-range))}
         :insertText   insert-text}))

;; --- Inline results -------------------------------------------------------
;; Analyze a document for its namespace and the "<line>:<col>" positions of every
;; `?` call. namespace nil -> JS null; positions -> a JS string array.

(defn analyze-inline-results [text]
  (let [{:keys [namespace positions]} (inline-results/analyze text)]
    #js {:namespace namespace
         :positions (clj->js positions)}))

;; --- Phase 2 live coding --------------------------------------------------

;; The alias names defined under :aliases in a deps.edn string, for the Live Code
;; picker. #js {:aliases [...]} on success (possibly empty); #js {:error "unparseable"}
;; when the deps.edn won't parse.
(defn deps-aliases [text]
  (let [r (deps/alias-names text)]
    (if (nil? r) #js {:error "unparseable"} #js {:aliases (clj->js r)})))

;; --- Phase 2 config (.test-refresh.edn) -----------------------------------

(defn- error? [r] (and (map? r) (:error r)))
(defn- err->js [r] #js {:error (name (:error r))})

(defn default-config [mode]
  (config/default-config (keyword mode)))

(defn read-mode [text]
  (let [r (config/read-mode text)]
    (if (error? r) (err->js r) #js {:mode (name r)})))

(defn set-mode [text mode ^js banners]
  (let [b (or banners #js {})
        r (config/set-mode text (keyword mode)
                           {:tap-banner (.-tapBanner b) :test-banner (.-testBanner b)})]
    (if (error? r) (err->js r) #js {:text r})))
