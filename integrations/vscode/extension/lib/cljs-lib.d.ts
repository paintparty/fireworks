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

export interface TasksResult {
  tasks?: string[]; // task names (symbol keys under :tasks), in bb.edn order; [] if none
  error?: string; // "unparseable" when the bb.edn text won't parse
}

// The task names under :tasks in a bb.edn string, for the Live Code picker (Babashka
// projects). The user owns bb.edn; they choose a task that starts their Fireworks
// watcher, and the extension runs `bb <task>`. Special keyword keys
// (:init/:requires/:enter/:leave) are excluded — only symbol-keyed tasks are returned.
export function bbTasks(text: string): TasksResult;

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
