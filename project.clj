(defproject io.github.paintparty/fireworks "0.4.0"
  :description "Color Pretty Printer for Clojure(Script)"
  :url "https://github.com/paintparty/fireworks"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [expound "0.9.0"]
                 [io.github.paintparty/lasertag "0.4.0"]]
  :repl-options {:init-ns fireworks.core}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]])

