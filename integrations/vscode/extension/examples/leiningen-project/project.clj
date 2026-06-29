(defproject fireworks-lein-example "0.1.0-SNAPSHOT"
  :description "Fireworks Live Coding example (Leiningen)."

  ;; Fireworks is a real dependency because this project's code calls `?`.
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.github.paintparty/fireworks "0.21.0-SNAPSHOT"]]


  ;; Live Code runs `lein with-profile +live-code test-refresh`. The plugin
  ;; lives in a profile (not top-level :plugins) so the extension detects it as
  ;; an eligible profile and it stays out of the default build.
  :profiles {:live-code {:plugins [[com.jakemccrary/lein-test-refresh "0.26.0"]]}}
  ;; `test-refresh` options.
  ;; The Fireworks extension keeps this map present.
  ;; If it is not present when the user runs the `Fireworks: Live Code` command,
  ;; the extension will ask the user for permission to add it to the `project.clj`
  :test-refresh {:changes-only      true
                 :quiet             true
                 :notify-on-success false
                 :debug             true
                 :banner            "🔥"
                 :debug-banner      "🔥"
                 :test-banner       "🧪 Running tests..."
                 :clear             true})
