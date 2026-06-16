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

// --- Phase 2: live-coding config editing ----------------------------------
//
// All functions are pure: they take the current file text (null/"" when the file
// does not exist yet) plus values to apply, and return a result envelope. Text-
// producing functions return { text } on success or { error } on failure; reads
// return { mode } / { options } / managed fields, with { error } on failure.
// `error` is a kebab string: "unparseable", "not-defproject", "no-options",
// "unsupported".

export type Mode = 'tap' | 'test';

// A test-refresh options map as a plain object keyed by the edn option names
// (e.g. "quiet", "changes-only", "notify-on-success", "debug", "banner",
// "debug-mode-opts", "clear"). Opaque to TS: obtained from defaultOptions /
// extractManaged / readOptions and passed back into writeOptions / ensureLeinSetup.
export type Options = Record<string, unknown>;

export interface Versions {
  fireworksVersion: string;
  testRefreshVersion: string;
}

export interface TextResult {
  text?: string; // new file text on success
  error?: string;
}

export interface ModeResult {
  mode?: Mode;
  error?: string;
}

export interface OptionsResult {
  options?: Options;
  error?: string;
}

// extractManaged result: the managed pieces pulled from a global config, ready to
// pass back in (versions match the Versions/ensure* shape). Fields are null when
// the global config didn't carry them.
export interface ManagedResult {
  fireworksVersion?: string | null;
  testRefreshVersion?: string | null;
  options?: Options | null;
  error?: string;
}

// deps.edn: ensure a single managed :test-refresh alias. text null/"" => create.
export function ensureDepsAlias(text: string | null, versions: Versions): TextResult;

// project.clj: the three scoped lein edits (:plugins, :test-refresh, :dev Fireworks).
// options defaults to the tap-mode map when omitted.
export function ensureLeinSetup(
  text: string,
  opts: Versions & { options?: Options },
): TextResult;

// .test-refresh.edn: merge the given option keys (managed-key only). text null/""
// => render a fresh file.
export function writeOptions(text: string | null, options: Options): TextResult;

// Read the options map from a bare .test-refresh.edn or a project.clj :test-refresh.
export function readOptions(text: string): OptionsResult;

// Read the live-coding mode (:debug truthy => "tap", else "test").
export function readMode(text: string): ModeResult;

// Flip mode in place (sets :debug, swaps :banner); operates on a bare
// .test-refresh.edn or a project.clj :test-refresh map (returns the whole file).
export function setMode(
  text: string,
  mode: Mode,
  banners: { tapBanner?: string; testBanner?: string },
): TextResult;

// Seed-from-global: pull the managed subset out of a global config's text.
export function extractManaged(
  text: string,
  fileKind: 'deps' | 'lein' | 'test-refresh-edn',
): ManagedResult;

// The default option map for a mode (used to seed a created config).
export function defaultOptions(mode: Mode): Options;
