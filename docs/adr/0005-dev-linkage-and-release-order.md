# ADR 0005: Sibling-checkout dev; hifi ships to clojars first

Date: 2026-07-09 · Status: accepted

## Context

fireworks depends on hifi. During development both repos change together; at release time the dependency must resolve from clojars.

## Decision

Dev linkage: fireworks adds `../hifi/src` to `:source-paths` (project.clj and shadow-cljs.edn) and `:local/root "../hifi"` in bb.edn — the same pattern already used for lasertag. `lein install` in hifi satisfies the maven coordinate locally.

Release order: publish `io.github.paintparty/hifi` to clojars first, then fireworks (0.22.0+) pinning it.

## Consequences

- CI or a clean machine needs either the sibling checkout or a published hifi.
- Remember to remove/comment local source paths when cutting a release jar.
