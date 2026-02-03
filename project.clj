(defproject io.github.paintparty/fireworks "0.19.0"
  :description "Themeable print debugging library for Clojure, ClojureScript, and Babashka"
  :url "https://github.com/paintparty/fireworks"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src"
                 ;; for local dev
                  "../lasertag/src"
                  #_"../../community/pp/src"
                 ]
  :dependencies [[org.clojure/clojure "1.12.4"]
                 [expound "0.9.0"]
                 #_[me.flowthing/pp "2024-11-13.77"]
                 [io.github.paintparty/lasertag "0.11.7"]]
  :repl-options {:init-ns fireworks.core}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]])
