# Glossary

Terms shared by hifi and fireworks.

- **engine** — everything that turns a value into a colored string: profile → truncate → serialize, plus the state/theme machinery. Lives in hifi.
- **orchestration** — the layer above the engine that arranges file-info, label/form, margins, and dispatches printing. Lives in `fireworks.core`.
- **profile** (`hifi.profile`) — per-node analysis pass that walks a value and annotates each node (type, size, key lengths, badges) before serialization.
- **truncation** (`hifi.truncate`) — depth/length limiting per config (`:print-level`, `:coll-limit`, scalar max lengths), with `...`/`...+N` annotations.
- **serialization** (`hifi.core/formatted*`, formerly `fireworks.serialize`) — reduces the profiled/truncated value to a tagged string.
- **tagged string / SGR tags** — output string interleaved with ANSI SGR escape codes per the merged theme's tokens.
- **theme tokens** — semantic style keys (`:string`, `:keyword`, `:file-info`, `:eval-label`, bracket levels...) resolved from a theme in `hifi.themes`/`hifi.basethemes` and pre-compiled into `hifi.state`.
- **state priming** (`hifi.core/reset-state!`) — resetting the `hifi.state` atoms (config, merged theme, highlight, counters) from a call-site opts map before a formatting run.
- **sev** — "scalar with extra visual" rendering path in the serializer for single (non-collection) values with badges/annotations.
- **p-data** — the data map returned by fireworks `?` in `:data` mode (`:formatted`, `:formatted+`, `:quoted-form`, `:template`, ...). `:formatted :string` is exactly the engine output.
- **template** — vector describing output arrangement, e.g. `[:file-info :form-or-label :result]`.
- **golden tests** — generated deftests comparing engine output (SGR codes escaped to `〠CODE〠` markers via `escape-sgr`) against baked-in expected strings.
- **visual mode** — `test-util/visual-mode?` toggle that prints each test's themed output for eyeballing instead of asserting.
- **badge** — the small type annotation block (e.g. `TransientVector`) rendered above/inline with a value.
