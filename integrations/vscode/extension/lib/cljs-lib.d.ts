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

// --- Phase 2: live-coding config (.test-refresh.edn) ----------------------
//
// The test-refresh + Fireworks deps are injected at launch via `clojure -Sdeps`
// (built TS-side), so nothing edits deps.edn. These functions only render and edit
// the small .test-refresh.edn holding the watcher options and the tap/test mode.
// `error` is the kebab string "unparseable".

export type Mode = 'tap' | 'test';

export interface TextResult {
  text?: string; // new file text on success
  error?: string;
}

export interface ModeResult {
  mode?: Mode;
  error?: string;
}

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
