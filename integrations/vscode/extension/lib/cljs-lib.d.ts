// Hand-written types for the shadow-cljs `:node-library` output (lib/cljs-lib.js).
// Keep in sync with the `:exports` map in shadow-cljs.edn and the camelCase keys
// emitted by fireworks-vscode.bridge. shadow-cljs does not generate these.

export interface Pos {
  line: number;
  col: number;
}

export interface ToggleOpts {
  text: string; // the text Calva's selectCurrentForm selected
  start: Pos; // selection start (0-based line, 0-based character)
  end: Pos; // selection end
  variant: '?' | '?>' | '#_'; // which macro/prefix this command operates on
  before?: string; // text just left of the selection; only the '#_' variant uses it
}

export interface EditPlan {
  replaceRange: { start: Pos; end: Pos };
  insertText: string;
  newCursor: Pos;
  reformat: boolean; // run calva-fmt.alignCurrentForm afterward
}

// Returns null for the no-op cases: empty/whitespace selection, nothing
// actionable, or a parse failure (e.g. unbalanced parens mid-edit).
export function toggleForm(opts: ToggleOpts): EditPlan | null;

export interface UnwrapAllOpts {
  text: string; // the text of the region to unwrap (current form or manual selection)
  start: Pos; // region start (0-based line, 0-based character)
  end: Pos; // region end
}

// Bulk-unwrap every Fireworks macro wrap ((? …), (!? …), (?> …), (!?> …), nested
// included) inside the region, dropping any label/options arg. The plan replaces the
// whole region; `reformat` is true (kept forms keep their original indent, so the TS
// side realigns the replaced range with the formatter). null when the region is
// blank, unparseable, or has no wrapped forms.
export function unwrapAll(opts: UnwrapAllOpts): EditPlan | null;

// Master-toggle the silence of every Fireworks wrap in the region: if any wrap is loud
// (?, ?>), silence them all (-> !?, !?>); only when all are already silent does it make
// them all loud. The `>` tap distinction is preserved. The plan replaces the whole region
// (`reformat` true, to realign the symbol-width change). null when the region is blank,
// unparseable, or has no wraps. Same opts shape as unwrapAll.
export function toggleAllSilent(opts: UnwrapAllOpts): EditPlan | null;

// --- ? with option ---------------------------------------------------------

export interface SetOptionOpts {
  text: string; // the current form's text (from calva.selectCurrentForm)
  start: Pos; // form start (0-based line/character)
  end: Pos; // form end
  option: string | null; // keyword option to set (":+" …), or null to remove
}

// Set or remove the leading keyword option on the Fireworks wrap at the cursor. On
// an existing wrap (?, !?, ?>, !?>, head preserved): replaces an existing leading
// keyword option, inserts one when absent, or (option null) strips it. On a bare form
// with an option, wraps it as (? <option> form); remove on a bare form is a no-op.
// `reformat` is true so the range formatter realigns the column shift. null when the
// form is blank, unparseable, or there is nothing to remove.
export function setFormOption(opts: SetOptionOpts): EditPlan | null;

// --- Toggle a flag (:trace, :-, :+, :pp) -----------------------------------

export interface FlagOpts {
  text: string; // the current form's text (from calva.selectCurrentForm)
  start: Pos; // form start (0-based line/character)
  end: Pos; // form end
}

// Toggle one leading flag on the Fireworks wrap at the cursor, preserving any other
// leading flags: removes the flag when present (form stays wrapped), inserts it ahead
// of the others when absent. A bare form is wrapped as (? <flag> form). The head
// (?, !?, ?>, !?>) is preserved. `reformat` is true so the range formatter realigns the
// column shift. null when the form is blank or unparseable. One function per flag:
export function toggleTrace(opts: FlagOpts): EditPlan | null; // :trace — intermediary values in threading forms
export function toggleMinus(opts: FlagOpts): EditPlan | null; // :-    — just print the result
export function togglePlus(opts: FlagOpts): EditPlan | null; //  :+    — disable all truncation
export function togglePp(opts: FlagOpts): EditPlan | null; //    :pp   — print with pprint

// --- Add Fireworks Require -------------------------------------------------

export interface RequireEdit {
  replaceRange: { start: Pos; end: Pos }; // confined to the ns form
  insertText: string; // the rewritten ns form
}

// Adds [fireworks.core :refer [? !? ?> !?>]] to the document's ns form — appended
// to an existing :require (aligned under its libspecs) or creating the :require if
// absent. `text` is the whole document. null if there is no ns form, Fireworks is
// already required, or the source won't parse.
export function addFireworksRequire(text: string): RequireEdit | null;

// --- Inline results --------------------------------------------------------

export interface InlinePosition {
  key: string; // "<start-row>:<start-col>" (1-based, at the `(`) — the result filename
  row: number; // the form's last row (1-based) — where the decoration is anchored
}

export interface InlineAnalysis {
  namespace: string | null; // the document's ns name, or null if none / fast path
  positions: InlinePosition[]; // one entry per `(? …)` call
}

// Analyze a whole document for its namespace and every `(? …)` call. `key` matches
// the filename the `?` macro writes under .fireworks/results/<ns>/; `row` is the
// form's end row, where TS anchors the inline decoration (so multi-line forms show
// at their end). Never throws — malformed source yields an empty result.
export function analyzeInlineResults(text: string): InlineAnalysis;

// --- Phase 2: live coding --------------------------------------------------

export interface AliasesResult {
  aliases?: string[]; // alias names (colon dropped), in deps.edn order; [] if none
  error?: string; // "unparseable" when the deps.edn text won't parse
}

// The alias names under :aliases in a deps.edn string, for the Live Code picker.
// The user owns deps.edn (the extension never writes it); they choose an alias that
// pulls in test-refresh + Fireworks, and the watcher runs `clojure -M:<alias>`.
export function depsAliases(text: string): AliasesResult;

export interface AliasDepsStatus {
  hasTestRefresh?: boolean; // the -M classpath (project :deps + alias deps) carries test-refresh
  hasFireworks?: boolean; // …and Fireworks
  mainOpts?: 'none' | 'test-refresh' | 'other'; // what the alias's :main-opts do
  error?: string; // "unparseable" when the deps.edn text won't parse
}

// Whether the chosen alias will put test-refresh + Fireworks on the `clojure -M:<alias>` classpath
// (project :deps merged with the alias's :extra-deps/:replace-deps/:override-deps/:deps), and what
// its :main-opts do. Matched by artifact name, so version and mvn/git/local form don't matter.
export function depsAliasStatus(text: string, alias: string): AliasDepsStatus;

export interface AddAliasResult {
  text?: string; // new deps.edn text on success
  alias?: string; // the alias name added ("live-code", or "fireworks-live-code" if taken)
  changed?: boolean;
  error?: string; // "unparseable"
}

// Add a fresh live-coding alias: Fireworks into the top-level :deps (appended, or a :deps map
// created if absent — skipped when already present), test-refresh + :main-opts + :extra-paths into a
// new :aliases entry. Creates the :aliases map if absent; existing aliases are never touched. Names
// it :live-code, or :fireworks-live-code if taken. Additive; prompt-then-write.
export function depsAddLiveCodeAlias(text: string): AddAliasResult;

export interface EnsureFireworksResult {
  text?: string; // deps.edn text with Fireworks in the top-level :deps (unchanged if already present)
  changed?: boolean; // false when Fireworks was already a top-level dep
  error?: string; // "unparseable"
}

// Ensure the top-level :deps carries the Fireworks coordinate (with the elide comment above it) —
// used to patch an eligible alias's project deps before launch. Additive; the file is written
// without a modal (the user picked the alias from the list).
export function depsEnsureFireworks(text: string): EnsureFireworksResult;

export interface TasksResult {
  tasks?: string[]; // task names (symbol keys under :tasks), in bb.edn order; [] if none
  error?: string; // "unparseable" when the bb.edn text won't parse
}

// The task names under :tasks in a bb.edn string, for the Live Code picker (Babashka
// projects). The user owns bb.edn; they choose a task that starts their Fireworks
// watcher, and the extension runs `bb <task>`. Special keyword keys
// (:init/:requires/:enter/:leave) are excluded — only symbol-keyed tasks are returned.
export function bbTasks(text: string): TasksResult;

// The bb.edn task names wired as Fireworks watchers — those whose body load-files
// `.fireworks/bb/watch.clj` (a leading `./` is tolerated). This is the opt-in signal that a
// bb.edn is a live-coding target: `tasks` empty ⇒ bb is not offered as a runtime. The watcher
// runs `bb <task>`; the extension seeds .fireworks/bb/watch.clj (see bbWatchTemplate) when absent.
export function bbWatchTasks(text: string): TasksResult;

// The generic Fireworks bb watcher, written into .fireworks/bb/watch.clj when missing. Static
// text (no substitution); per-project options come from .fireworks/config.edn at runtime.
export function bbWatchTemplate(): string;

// --- Phase 2: live-coding config (.test-refresh.edn) ----------------------
//
// These functions render and edit the small .test-refresh.edn holding the watcher
// options and the tap/test mode. Not currently wired into the live-coding flow (kept
// for later). `error` is the kebab string "unparseable".

export type Mode = 'tap' | 'test';

export interface TextResult {
  text?: string; // new file text on success
  error?: string;
}

export interface ModeResult {
  mode?: Mode;
  error?: string;
}

export interface ToggleModeResult {
  text?: string; // rewritten file text on success
  mode?: Mode; // the mode toggled to ("tap" => debug, "test")
  error?: string;
}

// The seed .test-refresh.edn text (commented literal) written when neither a project-local
// nor a global ~/.test-refresh.edn exists at Live Code launch.
export function testRefreshTemplate(): string;

// The default .test-refresh.edn text for a mode (used to create the file).
export function defaultConfig(mode: Mode): string;

// Read the live-coding mode (:debug truthy => "tap", else "test").
export function readMode(text: string): ModeResult;

// Flip mode in place: set :debug and swap :banner; other keys are left as-is.
export function setMode(
  text: string,
  mode: Mode,
  banners: { tapBanner?: string; testBanner?: string },
): TextResult;

// Toggle mode in place: flip :debug and sync :banner from the file's own :debug-banner /
// :test-banner. Returns the rewritten text and the mode toggled to.
export function toggleMode(text: string): ToggleModeResult;

// --- Phase 2: live coding (Leiningen project.clj) -------------------------
//
// A project.clj is Live-Code-eligible when its defproject :profiles has a profile (any
// name) whose :plugins carries exactly [com.jakemccrary/lein-test-refresh "0.26.0"]; the
// watcher then runs `lein with-profile +<profile> test-refresh`. Mirroring the deps.edn flow,
// the extension may additively edit project.clj (picker-consented, no modal): add a fresh
// :live-code profile, ensure the top-level :test-refresh map, and patch in the Fireworks dep.

export interface LeinProfilesResult {
  all?: string[]; // every profile name in :profiles order (colon dropped)
  eligible?: string[]; // those whose :plugins carries the exact coordinate
  error?: string; // "unparseable"
}

// Read the profile names from a project.clj string, splitting eligible (carries the
// coordinate) from all. {all:[], eligible:[]} when there is no :profiles entry.
export function leinProfiles(text: string): LeinProfilesResult;

export interface LeinFireworksStatus {
  hasFireworks?: boolean; // a Fireworks dep in the top-level or any profile :dependencies
  error?: string; // "unparseable"
}

// Whether project.clj carries a Fireworks dependency (top-level :dependencies or any profile's),
// matched by artifact name. Live Code needs it so namespaces that call `?` can reload.
export function leinFireworksStatus(text: string): LeinFireworksStatus;

export interface ChangedTextResult {
  text?: string; // new project.clj text on success
  changed?: boolean; // false when nothing needed changing
  error?: string;
}

// Ensure project.clj carries a Fireworks dependency: append the coordinate to the top-level
// :dependencies (aligned) when absent everywhere. Additive; matched by artifact name so any
// group/version already present is left alone (changed=false). Mirrors depsEnsureFireworks.
export function leinEnsureFireworks(text: string): ChangedTextResult;

export interface AddProfileResult {
  text?: string; // new project.clj text on success
  profile?: string; // the profile added ("live-code", or "fireworks-live-code" on collision)
  changed?: boolean; // always true on success
  error?: string;
}

// Add a fresh :live-code profile carrying lein-test-refresh (the eligible-profile shape), never
// rewriting a profile the user already has: spliced with a comment header when the project has no
// :profiles, else assoc'd into the existing map. Mirrors depsAddLiveCodeAlias on the deps side.
export function leinAddLiveCodeProfile(text: string): AddProfileResult;

export interface EnsureTestRefreshResult {
  text?: string; // new project.clj text on success
  changed?: boolean; // false when every baseline key was already present
  addedKeys?: string[]; // the baseline keys added/merged (for the diagnostics line)
  error?: string;
}

// Ensure the top-level defproject :test-refresh map against the baseline: add the full map
// when absent, else merge only the missing keys (existing values untouched). Applied (with the
// Fireworks + profile edits) before project.clj is written, picker-consented — no modal.
export function leinEnsureTestRefresh(text: string): EnsureTestRefreshResult;

export interface UserProfileStatusResult {
  hasPlugin?: boolean; // the test-refresh plugin coordinate appears nested anywhere in the :user entry
  hasFireworks?: boolean; // a Fireworks dependency appears nested anywhere in the :user entry
  hasTestRefresh?: boolean; // the :user map has a :test-refresh options key
  error?: string;
}

// Read ~/.lein/profiles.clj (the global :user check, read-only): does the :user entry carry the
// test-refresh plugin, a Fireworks dependency, and a :test-refresh options key? All three are
// required to run Live Code off the global profile. The extension never writes global config.
export function leinUserProfileStatus(text: string): UserProfileStatusResult;

// A human-readable `:test-refresh { … }` snippet (from the baseline) for the global :user
// setup guidance.
export function leinTestRefreshSnippet(): string;
