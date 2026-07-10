# ADR 0003: hifi keeps reading FIREWORKS_* config names for now

Date: 2026-07-09 · Status: accepted

## Context

Config resolution reads the `FIREWORKS_CONFIG` env var, config.edn conventions, and `COLOR_THEME`. Renaming them during the split would break every existing fireworks user in the same release that restructures the libraries.

## Decision

hifi's config/state namespaces keep reading the existing FIREWORKS_* names and conventions unchanged. Rename (or a HIFI_*-first-with-fallback scheme) is deferred to a later hifi release. Functional keywords like `:fireworks.highlight/map-key` also keep their names for the same reason.

## Consequences

- Zero user-facing config breakage in fireworks 0.22.0.
- hifi standalone users configure it via fireworks-named conventions until the rename ships.
