# Changelog

## 0.2.4
<sub>07-04-2026</sub>

#### Added
- Adds a `Fireworks: Quick Start` command for the frictionless creation of a scratch project on the user's desktop.
- Adds a `Quick Start Project Type` setting. Defaults to Babashka.

#### Fixed
- Scaffolded projects (`Create New Project` / `Quick Start`) now pin the Fireworks and test-refresh versions to the single source of truth in `fireworks-vscode.versions`, instead of copying whatever version the bundled example build files carried.
- Live Code now reads `FIREWORKS_THEME` live from your login shell at launch, so a `.zshrc` (or equivalent) theme change made after VS Code started is picked up. Previously the extension used the environment snapshot frozen when VS Code launched, so shell edits were ignored until a full restart.

## 0.2.3
<sub>07-04-2026</sub>

#### Added
- Adds logic to syncronize users vscode theme with the theme of the fireworks output that gets printed to terminal. If needed, a `FIREWORKS_THEME` prefix to cli command that launches process in itegrated terminal.

#### Changed
- Updated readme

#### Fixed
- Outdated readme

## 0.2.2
<sub>07-02-2026</sub>

#### Fixed
- `--baseImgagesUrl` in build script missing


## 0.2.1
<sub>07-02-2026</sub>

#### Fixed
- Bumped fireworks dep to `0.22.2`

<br>


## 0.2.0 
<sub>07-01-2026</sub>

#### Added
- Create New Project Command
- More docs

<br>

## 0.1.0
<sub>07-01-2026</sub>

First public release.

- Toggle commands for the Fireworks macros (`?`, `?>`, `#_`, `:trace`, `:-`, `:+`, `:pp`) on one form or several nested forms.
- Unwrap-all and toggle-all-silent commands.
- Live Code mode with inline results for Clojure CLI (deps.edn), Babashka (bb.edn), and Leiningen (project.clj).
- Configurable inline-result color, opacity, gap, max length, and fade-in.
