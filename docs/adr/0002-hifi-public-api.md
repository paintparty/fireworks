# ADR 0002: hifi public API is `(hifi x)` / `(hifi x opts)`

Date: 2026-07-09 · Status: accepted

## Context

The engine's serializer (`formatted*`) depends on the `hifi.state` atoms being primed per call. Callers (and hifi's own golden tests) need a single entry point that does both.

## Decision

`hifi.core/hifi`: `([x] [x opts])`. Primes state from `opts` (a map of config option overrides, same shape as config.edn, plus `:find`), runs profile → truncate → serialize, returns the ANSI-colored string. The port is otherwise faithful — no redesign of the internals in this release.

## Consequences

- The generated golden test suites call `(hifi x merged-opts)` directly; no dependency on fireworks macros.
- fireworks' orchestration calls `hifi.core/reset-state!` + `hifi.core/formatted*` separately because it interleaves label/file-info work between the two.
