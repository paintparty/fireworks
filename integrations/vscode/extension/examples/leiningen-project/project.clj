(defproject fireworks-lein-example "0.1.0-SNAPSHOT"
  :description "Fireworks Live Coding example (Leiningen)."

  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.github.paintparty/fireworks "0.21.2"]]


  ;; The VSCode extension command `Fireworks: Live Code` runs:
  ;; `lein with-profile +live-code test-refresh`.
  ;; The plugin lives in a profile (not top-level :plugins) so the extension
  ;; detects it as an eligible profile and it stays out of the default build.
  :profiles {:live-code {:plugins [[com.jakemccrary/lein-test-refresh "0.26.0"]]}

             ;; Strips Fireworks taps from release builds. Elision is a
             ;; macroexpansion-time flag, so it must be set on the JVM that
             ;; compiles the code. The entry `:aot :all` ensures every namespace
             ;; containing a `?` macro call is compiled here with the flag set.
             ;; `lein uberjar` merges this profile automatically, so the shipped
             ;; jar has no un-elided `?` calls;
             ;; development builds (lein run/repl) don't get it, so all calls to
             ;; `?` will work as expected.
             :uberjar {:aot :all
                       :jvm-opts ["-Dfireworks.elide=true"]}})
