# ADR 0004: fireworks.pp moves to hifi.pp; fireworks keeps a thin wrapper

Date: 2026-07-09 · Status: accepted

## Context

`fireworks.pp` (standalone pretty-printer, no coloring) is required by engine namespaces (tag, brackets, state, messaging), so it must move with the engine. But it also exposes the user-facing `?pp` / `!?pp` / `pprint` dev helpers.

## Decision

The implementation is `hifi.pp`. fireworks keeps a thin `fireworks.pp` wrapper ns: `pprint` and `!?pp` as defs, `?pp` as a delegating macro that propagates `&form` metadata (so call-site line info in labels survives).

## Consequences

- Users' `fireworks.pp` requires keep working unchanged.
- One implementation; no divergence risk.
