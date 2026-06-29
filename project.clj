(defproject io.github.paintparty/fireworks "0.21.0-SNAPSHOT"
  :description "Themeable print debugging library for Clojure, ClojureScript, and Babashka"
  :url "https://github.com/paintparty/fireworks"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src"
                 ;; for local dev
                ;;  "../lasertag/src"
                 ]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [expound "0.9.0"]
                 [io.github.paintparty/lasertag "0.13.0-SNAPSHOT"]]
  :repl-options {:init-ns fireworks.core}

  ;; rewrite-clj is dev/test-only: it backs the experimental golden-snapshotter
  ;; in fireworks.test-util/snapshot!. Not a runtime dependency of the library.
  :profiles {:dev {:dependencies [[rewrite-clj "1.1.47"]]}}

  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]])

