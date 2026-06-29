(defproject fireworks-lein-example "0.1.0-SNAPSHOT"
  :description "Fireworks Live Coding example (Leiningen)."

  ;; Fireworks is a real dependency because this project's code calls `?`.
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.github.paintparty/fireworks "0.20.0"]]

  ;; test-refresh options. The extension keeps this map present.
  :test-refresh {:changes-only true}

  ;; Live Code runs `lein with-profile +fireworks test-refresh`. The plugin
  ;; lives in a profile (not top-level :plugins) so the extension detects it as
  ;; an eligible profile and it stays out of the default build.
  :profiles {:fireworks {:plugins [[com.jakemccrary/lein-test-refresh "0.26.0"]]}})
