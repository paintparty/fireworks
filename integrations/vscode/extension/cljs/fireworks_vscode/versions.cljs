
(ns fireworks-vscode.versions
  "Single source of truth for the dependency coordinates the extension injects and documents.
   Every place that names a Fireworks or test-refresh version — the deps.edn / project.clj
   editors (fireworks-vscode.deps, .lein), the TS guidance docs (via the bridge `versions`
   export), and the version guard test — reads from here. The `examples/` project files hold
   the same coordinates literally (they are real, standalone build files); fireworks-vscode.
   versions-test asserts they stay in step with these constants.

   Values are plain strings (not symbols) so they cross the JS bridge unchanged; cljs callers
   wrap the sym strings with `symbol` where a real symbol is needed.")

;; Fireworks itself — a project dependency, injected into the top-level :deps / :dependencies
;; when a project's classpath lacks it.
(def fireworks-sym "io.github.paintparty/fireworks")
(def fireworks-version "0.21.2")

;; test-refresh (the Clojure CLI / deps.edn artifact) — lives on the live-code alias classpath.
(def test-refresh-sym "com.jakemccrary/test-refresh")
(def test-refresh-version "0.26.0")

;; lein-test-refresh (the Leiningen plugin artifact) — the eligible :plugins coordinate. Matched
;; by exact version in fireworks-vscode.lein, so this drives eligibility.
(def lein-test-refresh-sym "com.jakemccrary/lein-test-refresh")
(def lein-test-refresh-version "0.26.0")
