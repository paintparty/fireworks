(defproject fireworks-test-refresh-workflow "0.1.0-SNAPSHOT"
  :description  "Sample project using Fireworks and test-refresh"
  :dependencies [[org.clojure/clojure "1.12.3"]
                 [io.github.paintparty/fireworks "0.16.1"]]
  :plugins      [[com.jakemccrary/lein-test-refresh  "0.26.0"]]
  :test-refresh {:quiet             true
                 :notify-on-success false
                 :changes-only      true

                 ;; The :debug option, if set to true, will skip all tests, but still reloads
                 ;; namespaces that have changed. This is a nice way to spin up a hot-reload dev
                 ;; environment for tap-driven development with Fireworks in JVM Clojure.
                 ;; If you want to run tests on every refresh, just set this back to the default
                 ;; value of false.
                 :debug             true

                 ;; With a hot-reload workflow, it is nice to use a simple custom banner.
                 :banner            "🔥🔥🔥🔥🔥🔥🔥🔥🔥"

                 ;; With a hot-reload workflow, it is nice to clear the terminal on refresh. 
                 :clear             true})
