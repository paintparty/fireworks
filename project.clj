(defproject io.github.paintparty/fireworks "0.12.6"
  :description "Themeable print debugging library for Clojure, ClojureScript, and Babashka"
  :url "https://github.com/paintparty/fireworks"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src"
                 ;; for local dev and fireworks deps
                ;;  "../lasertag/src"
                 ]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [expound "0.9.0"]
                 ;; for testing
                 ;; [com.taoensso/tufte "2.6.3"]
                 [io.github.paintparty/lasertag "0.11.5"]]
  :repl-options {:init-ns fireworks.core}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]])
