# Examples

Minimal, idiomatic projects wired for Fireworks Live Coding. One per runtime.

Open a folder, run `Fireworks: Live Code (Start)`, pick the project, then save
`src/example/core.clj`. Every top-level `?` form re-runs and its value paints inline.

| Folder | Runtime | Build file | Live Code runs |
| --- | --- | --- | --- |
| `deps/` | Clojure CLI | `deps.edn` | `clojure -M:test-refresh` |
| `leiningen/` | Leiningen | `project.clj` | `lein with-profile +fireworks test-refresh` |
| `babashka/` | Babashka | `bb.edn` | `bb dev` |

Each project ships the same `example.core` namespace with a few `?`-wrapped forms and a
matching `example.core-test`. See the Live Coding section of the extension README for the
full flow.
