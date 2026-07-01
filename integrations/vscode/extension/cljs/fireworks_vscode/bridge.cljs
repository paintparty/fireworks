
(ns fireworks-vscode.bridge
  "The only namespace that touches JS. Converts JS opts -> clj (keywordized;
   variant stays the string \"?\"/\"?>\"), runs the pure logic, and builds the
   outgoing JS plan with camelCase keys so the TS side reads plan.replaceRange,
   plan.insertText, plan.newCursor, plan.reformat. nil -> JS null.

   Phase 2 config functions follow the same discipline: JS in (string text/mode),
   pure fireworks-vscode.config logic, JS result envelopes out. setMode returns
   #js {:text ...} on success or #js {:error \"...\"}; readMode returns #js {:mode ...}
   or #js {:error ...}; defaultConfig returns the rendered text directly."
  (:require [fireworks-vscode.bb :as bb]
            [fireworks-vscode.config :as config]
            [fireworks-vscode.deps :as deps]
            [fireworks-vscode.inline-results :as inline-results]
            [fireworks-vscode.lein :as lein]
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

(defn toggle-trace [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/toggle-trace opts)]
    (when plan (plan->js plan))))

(defn toggle-minus [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/toggle-minus opts)]
    (when plan (plan->js plan))))

(defn toggle-plus [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/toggle-plus opts)]
    (when plan (plan->js plan))))

(defn toggle-pp [^js js-opts]
  (let [opts (js->clj js-opts :keywordize-keys true)
        plan (toggle/toggle-pp opts)]
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

;; Whether a chosen deps.edn alias will put test-refresh + Fireworks on the `-M` classpath, plus
;; what its :main-opts do. #js {:hasTestRefresh bool :hasFireworks bool :mainOpts "none|test-refresh
;; |other"} on success; #js {:error "unparseable"} when the deps.edn won't parse.
(defn deps-alias-status [text alias]
  (let [r (deps/alias-deps-status text alias)]
    (if (nil? r)
      #js {:error "unparseable"}
      #js {:hasTestRefresh (:has-test-refresh r)
           :hasFireworks   (:has-fireworks r)
           :mainOpts       (:main-opts r)})))

;; Add a self-contained :live-code alias (test-refresh + Fireworks + :main-opts) to deps.edn `text`.
;; #js {:text ... :alias name :changed bool} on success; #js {:error "unparseable"} on a parse error.
(defn deps-add-live-code-alias [text]
  (let [r (deps/add-live-code-alias text)]
    (if (:error r)
      #js {:error "unparseable"}
      #js {:text (:text r) :alias (:alias r) :changed (:changed r)})))

;; Additively patch an existing alias's :extra-deps / :main-opts / :extra-paths with what's missing.
;; #js {:text ... :changed bool :added [...]} on success; #js {:error "unparseable"} on a parse error.
(defn deps-patch-alias [text alias]
  (let [r (deps/patch-alias text alias)]
    (if (:error r)
      #js {:error "unparseable"}
      #js {:text (:text r) :changed (:changed r) :added (clj->js (:added r))})))

;; The task names defined under :tasks in a bb.edn string, for the Live Code picker
;; (Babashka projects). #js {:tasks [...]} on success (possibly empty);
;; #js {:error "unparseable"} when the bb.edn won't parse.
(defn bb-tasks [text]
  (let [r (bb/task-names text)]
    (if (nil? r) #js {:error "unparseable"} #js {:tasks (clj->js r)})))

;; The bb.edn task names wired as Fireworks watchers (their body load-files
;; .fireworks/bb/watch.clj), for the Live Code bb runtime. #js {:tasks [...]} on success
;; (empty when none are wired, so bb is not offered); #js {:error "unparseable"} on parse failure.
(defn bb-watch-tasks [text]
  (let [r (bb/watch-task-names text)]
    (if (nil? r) #js {:error "unparseable"} #js {:tasks (clj->js r)})))

;; The generic bb watcher text, seeded into .fireworks/bb/watch.clj when absent. Returned verbatim.
(defn bb-watch-template []
  bb/watch-template)

;; --- Phase 2 config (.test-refresh.edn) -----------------------------------

(defn- error? [r] (and (map? r) (:error r)))
(defn- err->js [r] #js {:error (name (:error r))})

;; The seed .test-refresh.edn text (commented literal) used when neither a project-local
;; nor a global ~/.test-refresh.edn exists at Live Code launch. Returned verbatim as a string.
(defn test-refresh-template []
  config/template)

(defn default-config [mode]
  (config/default-config (keyword mode)))

(defn read-mode [text]
  (let [r (config/read-mode text)]
    (if (error? r) (err->js r) #js {:mode (name r)})))

;; Toggle .test-refresh.edn text between debug/tap and test mode, syncing :banner from the
;; file's own :debug-banner / :test-banner. #js {:text ... :mode "tap"|"test"} on success;
;; #js {:error "unparseable"} when the file won't parse.
(defn toggle-mode [text]
  (let [r (config/toggle-mode text)]
    (if (error? r) (err->js r) #js {:text (:text r) :mode (name (:mode r))})))

(defn set-mode [text mode ^js banners]
  (let [b (or banners #js {})
        r (config/set-mode text (keyword mode)
                           {:tap-banner (.-tapBanner b) :test-banner (.-testBanner b)})]
    (if (error? r) (err->js r) #js {:text r})))

;; --- Phase 2 live coding (Leiningen project.clj) --------------------------

;; The profile names in a project.clj string: #js {:all [...] :eligible [...]} where
;; :eligible carries the exact [com.jakemccrary/lein-test-refresh "0.26.0"] coordinate;
;; #js {:error "unparseable"} when the project.clj won't parse.
(defn lein-profiles [text]
  (let [r (lein/profiles text)]
    (if (error? r)
      (err->js r)
      #js {:all (clj->js (:all r)) :eligible (clj->js (:eligible r))})))

;; Whether project.clj `text` carries a Fireworks dependency (top-level or any profile
;; :dependencies). #js {:hasFireworks bool} on success; #js {:error "unparseable"} on a parse error.
(defn lein-fireworks-status [text]
  (let [r (lein/fireworks-dep-status text)]
    (if (error? r) (err->js r) #js {:hasFireworks (:has-fireworks r)})))

;; Ensure the exact coordinate in `profile`'s :plugins vector in project.clj `text`.
;; #js {:text ... :changed bool} on success; #js {:error "unparseable"} when it won't parse.
(defn lein-add-plugin [text profile]
  (let [r (lein/add-plugin-to-profile text profile)]
    (if (error? r) (err->js r) #js {:text (:text r) :changed (:changed r)})))

;; Ensure the top-level :test-refresh map in project.clj `text` against the baseline.
;; #js {:text ... :changed bool :addedKeys [...]} on success; #js {:error "unparseable"}.
(defn lein-ensure-test-refresh [text]
  (let [r (lein/ensure-test-refresh text)]
    (if (error? r)
      (err->js r)
      #js {:text (:text r) :changed (:changed r) :addedKeys (clj->js (:added-keys r))})))

;; Read ~/.lein/profiles.clj `text`: #js {:hasPlugin bool :hasTestRefresh bool} (the :user
;; entry carries the coordinate / a :test-refresh key); #js {:error "unparseable"}.
(defn lein-user-profile-status [text]
  (let [r (lein/user-profile-status text)]
    (if (error? r)
      (err->js r)
      #js {:hasPlugin (:has-plugin r) :hasTestRefresh (:has-test-refresh r)})))

;; A human-readable :test-refresh { … } snippet (from the baseline) for the modals.
(defn lein-test-refresh-snippet []
  (lein/test-refresh-snippet))
