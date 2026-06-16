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
