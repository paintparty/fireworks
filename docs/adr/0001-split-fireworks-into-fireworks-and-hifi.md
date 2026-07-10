# ADR 0001: Split fireworks into fireworks (macros) + hifi (engine)

Date: 2026-07-09 · Status: accepted

## Context

Fireworks fused two concerns: the print-and-return / tapping macros (`?`, `!?`, `?>`, `!?>`, `?flop`), and a formatting engine (~90% of the code) that pretty-prints, truncates, and syntax-colors values per user theme. The engine is useful standalone and the macro library is easier to maintain when small.

## Decision

Extract the engine into a standalone lein library `hifi` (sibling repo). fireworks keeps the macro layer, the label/file-info arrangement, and printing dispatch (`formatted`, `print-formatted`, `_p`/`_p2`, public `pprint`), and depends on hifi.

Exception to the split line: the state-priming machinery (`reset-state!`, `reset-config+theme!`, `opts-to-reset` family) moved from `fireworks.core` into `hifi.core`, because hifi's `hifi` entry fn and hifi's own tests need to prime the engine without fireworks. `fireworks.state` (config merge, theme compilation, the atoms) moved wholesale — it is a hard compile-time dependency of the serializer.

## Consequences

- fireworks src is now `fireworks.core` (+ thin `fireworks.pp` wrapper, dev-only prof/profiling); everything else lives in hifi under the same names (`fireworks.X` → `hifi.X`, `fireworks.serialize` → `hifi.core`).
- hifi is testable standalone via `(hifi x opts)` golden tests.
- fireworks releases must pin a published hifi version (hifi ships first).
