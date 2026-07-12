## Clojure codebase exploration

Use the `clj-surgeon` skill (auto-loaded from `.claude/skills/clj-surgeon/`) for structural ops on .clj files. ALWAYS run `/clj-surgeon` with `:op :ls` before reading any .clj file over 200 lines or spawning an Explore agent for Clojure code — measured 150x more token-efficient (~1k tokens vs ~150k) and returns in ms vs ~100s. Use `:ls` for form boundaries, then Read only the specific line ranges you need. Only spawn Explore agents for targeted follow-ups with specific file paths.

## The batter-up rite

When the user says "batter-up": read `.claude/batters/on-deck.txt` (in this repo) and treat its contents as the next user prompt. Then rename the file to `batter-N.txt` in the same dir, where N = (number of existing `batter-*.txt` files) + 1. If `on-deck.txt` does not exist, say so and stop.

## Relationship to hifi

Since 0.22.0, fireworks is only the macro layer (`?`, `!?`, `?>`, `!?>`, `?flop` in `fireworks.core`) plus file-info/label arrangement and printing dispatch. The formatting/colorizing engine lives in the sibling repo `../hifi` (namespaces `hifi.*`; `fireworks.serialize` became `hifi.core`). See `docs/adr/` for the split decisions and `docs/glossary.md` for terms.

- Dev linkage: `../hifi/src` is on `:source-paths` in project.clj and shadow-cljs.edn; bb.edn uses `:local/root "../hifi"`. A local `lein install` in ../hifi satisfies the maven coordinate.
- Release order: publish hifi to clojars first, then fireworks pinning it.
- Config env vars (`FIREWORKS_CONFIG`, `FIREWORKS_THEME`) are read by hifi and intentionally keep the FIREWORKS_* names (hifi ADR 0003).
- Engine golden tests live in hifi. Fireworks tests (`test/fireworks/core_test.cljc`, testbb) cover macro behavior only: tapping passthrough, annotation templates, print-with/log? passthrough.
- Tests: `lein test` · `bb test:bb` · `npx shadow-cljs compile test && npx karma start --single-run`.
