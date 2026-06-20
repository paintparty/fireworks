import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';
import * as os from 'os';
import * as cljsLib from '../lib/cljs-lib';
import type { EditPlan, RequireEdit, InlineAnalysis, UnwrapAllOpts } from '../lib/cljs-lib';

let output: vscode.OutputChannel;

export function activate(context: vscode.ExtensionContext): void {
  output = vscode.window.createOutputChannel('Fireworks');
  extContext = context;

  statusBar = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
  statusBar.command = 'fireworks.toggleInlineResults';

  context.subscriptions.push(
    output,
    statusBar,
    vscode.commands.registerCommand('fireworks.toggle', () => runToggle('?')),
    vscode.commands.registerCommand('fireworks.toggleTap', () => runToggle('?>')),
    vscode.commands.registerCommand('fireworks.toggleIgnore', () => runToggle('#_')),
    vscode.commands.registerCommand('fireworks.unwrapAll', () =>
      runFormEdit(cljsLib.unwrapAll, 'unwrap-all'),
    ),
    vscode.commands.registerCommand('fireworks.unwrapAllInNs', () =>
      runNsEdit(cljsLib.unwrapAll, 'unwrap-all-in-ns'),
    ),
    vscode.commands.registerCommand('fireworks.toggleAllSilent', () =>
      runFormEdit(cljsLib.toggleAllSilent, 'toggle-all-silent'),
    ),
    vscode.commands.registerCommand('fireworks.toggleAllSilentInNs', () =>
      runNsEdit(cljsLib.toggleAllSilent, 'toggle-all-silent-in-ns'),
    ),
    vscode.commands.registerCommand('fireworks.withOption', () => runWithOption()),
    vscode.commands.registerCommand('fireworks.addRequire', () => runAddRequire()),
    vscode.commands.registerCommand('fireworks.toggleInlineResults', () => toggleInlineResults()),
    vscode.commands.registerCommand('fireworks.clearInlineResults', () => clearInlineResults()),
    vscode.commands.registerCommand('fireworks.startLiveCoding', () => startLiveCoding()),
    vscode.commands.registerCommand('fireworks.stopLiveCoding', () => stopLiveCoding()),
    vscode.commands.registerCommand('fireworks.restartLiveCoding', () => restartLiveCoding()),
    vscode.window.onDidCloseTerminal((t) => {
      if (t === liveTerminal) {
        liveTerminal = undefined;
        liveExecution = undefined;
      }
    }),
    vscode.window.onDidEndTerminalShellExecution((e) => {
      if (e.execution === liveExecution) {
        liveExecution = undefined;
      }
    }),
    vscode.workspace.onDidChangeWorkspaceFolders(() => {
      void refreshRoots();
    }),
    vscode.workspace.onDidChangeConfiguration((e) => {
      if (e.affectsConfiguration('fireworks.inlineResults.enabled')) {
        updateStatusBar();
      }
    }),
  );

  context.subscriptions.push({ dispose: () => stopInlineResults() });

  void refreshRoots().then(() => {
    if (inlineResultsEnabled()) {
      startInlineResults();
    }
  });
}

export function deactivate(): void {}

function debugEnabled(): boolean {
  return vscode.workspace.getConfiguration('fireworks').get<boolean>('debug', false);
}

function log(msg: string): void {
  if (debugEnabled()) {
    output.appendLine(msg);
  }
}

function isVimActive(): boolean {
  return !!vscode.extensions.getExtension('vscodevim.vim')?.isActive;
}

async function runToggle(variant: '?' | '?>' | '#_'): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor || editor.document.languageId !== 'clojure') {
    return; // silent no-op
  }

  try {
    await vscode.commands.executeCommand('calva.selectCurrentForm');
  } catch (e) {
    vscode.window.showErrorMessage('Fireworks needs Calva to be active.');
    output.appendLine(`calva.selectCurrentForm failed: ${String(e)}`);
    return;
  }

  const sel = editor.selection;
  // The '#_' variant needs the text just left of the selection, so an already
  // ignored form is detected whether selectCurrentForm includes the #_ or not.
  const beforeStart = new vscode.Position(sel.start.line, Math.max(0, sel.start.character - 2));
  const before = editor.document.getText(new vscode.Range(beforeStart, sel.start));
  let plan: EditPlan | null;
  try {
    plan = cljsLib.toggleForm({
      text: editor.document.getText(sel),
      start: { line: sel.start.line, col: sel.start.character },
      end: { line: sel.end.line, col: sel.end.character },
      variant,
      before,
    });
  } catch (e) {
    log(`toggleForm threw, treating as no-op: ${String(e)}`);
    return;
  }
  if (!plan) {
    log('no-op');
    return;
  }

  await applyTogglePlan(editor, plan);
}

// Apply a single-form EditPlan from a selectCurrentForm-based command (toggle, ? with
// option). Replaces the range, then sets the cursor + Calva-aligns on a later tick.
// Ported from the Joyride scripts, which set the cursor inside a 50ms setTimeout after
// editor.edit() and worked reliably. The async/await version that set editor.selection
// synchronously lost a race with vscodevim, which re-asserts its own cursor after the
// edit and left the caret mid-form. If a synchronous, race-free version can be found
// later, prefer it. (applyEditPlan is the variant for the bulk/ns commands, which do not
// go through selectCurrentForm and so are not subject to this race.)
async function applyTogglePlan(editor: vscode.TextEditor, plan: EditPlan): Promise<void> {
  const range = new vscode.Range(
    new vscode.Position(plan.replaceRange.start.line, plan.replaceRange.start.col),
    new vscode.Position(plan.replaceRange.end.line, plan.replaceRange.end.col),
  );

  // editor.edit applies the wrap/unwrap/invert/option text.
  const ok = await editor.edit(
    (b) => b.replace(range, plan.insertText),
    { undoStopBefore: true, undoStopAfter: false },
  );
  if (!ok) {
    return;
  }

  if (isVimActive()) {
    await vscode.commands.executeCommand('extension.vim_escape'); // Vim's Escape nudges the cursor...
  }

  setTimeout(() => {
    const cursor = new vscode.Position(plan.newCursor.line, plan.newCursor.col);
    editor.selection = new vscode.Selection(cursor, cursor); // ...so we set our cursor after it
    if (plan.reformat) {
      void vscode.commands.executeCommand('calva-fmt.alignCurrentForm'); // keys off cursor, runs last
    }
  }, 50);
}

// --- ? with option ----------------------------------------------------------
// Each entry is a picker row; `option` is the keyword passed to the pure side
// (null = the "Remove Options" row).
const FIREWORKS_OPTIONS: { option: string | null; label: string; detail: string }[] = [
  { option: ':+', label: ':+', detail: 'Disable all truncation' },
  { option: ':-', label: ':-', detail: 'Just print the result' },
  { option: ':no-file', label: ':no-file', detail: "Don't print the file info" },
  { option: ':no-label', label: ':no-label', detail: "Don't print the label" },
  { option: ':trace', label: ':trace', detail: 'Print intermediary values in threading forms' },
  { option: ':pp', label: ':pp', detail: 'Print with pprint' },
];

type OptionPick = vscode.QuickPickItem & { option?: string | null };

// Pick a Fireworks print option from a QuickPick and attach it (or Remove Options to
// strip it) on the wrapped form at the cursor. Same selectCurrentForm + apply flow as
// runToggle; the pure side handles wrap/replace/remove and the bare-form wrap-then-add.
async function runWithOption(): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor || editor.document.languageId !== 'clojure') {
    return; // silent no-op
  }

  try {
    await vscode.commands.executeCommand('calva.selectCurrentForm');
  } catch (e) {
    vscode.window.showErrorMessage('Fireworks needs Calva to be active.');
    output.appendLine(`calva.selectCurrentForm failed: ${String(e)}`);
    return;
  }

  const items: OptionPick[] = [
    ...FIREWORKS_OPTIONS.map((o) => ({ label: o.label, detail: o.detail, option: o.option })),
    { label: '', kind: vscode.QuickPickItemKind.Separator },
    { label: 'Remove Options', detail: 'Strip the option keyword', option: null },
  ];
  const picked = await vscode.window.showQuickPick(items, {
    placeHolder: 'Fireworks option',
    matchOnDetail: true,
  });
  if (!picked) {
    return; // cancelled
  }

  const sel = editor.selection;
  let plan: EditPlan | null;
  try {
    plan = cljsLib.setFormOption({
      text: editor.document.getText(sel),
      start: { line: sel.start.line, col: sel.start.character },
      end: { line: sel.end.line, col: sel.end.character },
      option: picked.option ?? null,
    });
  } catch (e) {
    log(`setFormOption threw, treating as no-op: ${String(e)}`);
    return;
  }
  if (!plan) {
    log('with-option no-op');
    return;
  }

  await applyTogglePlan(editor, plan);
}

// A pure bulk structural transform over a region: unwrapAll or toggleAllSilent. Takes the
// region's text + 0-based bounds, returns an edit plan or null (no-op).
type RegionEdit = (opts: UnwrapAllOpts) => EditPlan | null;

// Form-scoped driver: operate on the active selection, or Calva's current form when there
// is none (like the other toggle commands), then run `transform` and apply its plan.
// Shared by the "… in Form" bulk commands.
async function runFormEdit(transform: RegionEdit, label: string): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor || editor.document.languageId !== 'clojure') {
    return; // silent no-op
  }

  // No manual selection -> select the current form, as the other toggle commands do.
  if (editor.selection.isEmpty) {
    try {
      await vscode.commands.executeCommand('calva.selectCurrentForm');
    } catch (e) {
      vscode.window.showErrorMessage('Fireworks needs Calva to be active.');
      output.appendLine(`calva.selectCurrentForm failed: ${String(e)}`);
      return;
    }
  }

  const sel = editor.selection;
  if (sel.isEmpty) {
    return; // no form at the cursor
  }

  let plan: EditPlan | null;
  try {
    plan = transform({
      text: editor.document.getText(sel),
      start: { line: sel.start.line, col: sel.start.character },
      end: { line: sel.end.line, col: sel.end.character },
    });
  } catch (e) {
    log(`${label} threw, treating as no-op: ${String(e)}`);
    return;
  }
  if (!plan) {
    log(`${label} no-op`);
    return;
  }
  await applyEditPlan(editor, plan);
}

// Namespace-scoped driver: hand the whole document to `transform`. Needs no selection and
// no Calva — the pure side cheaply pre-checks for any wrap and no-ops a wrap-free file
// without parsing. The reformat pass then realigns the replaced region (here, the file).
async function runNsEdit(transform: RegionEdit, label: string): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor || editor.document.languageId !== 'clojure') {
    return; // silent no-op
  }
  const doc = editor.document;
  const end = doc.lineAt(doc.lineCount - 1).range.end;

  let plan: EditPlan | null;
  try {
    plan = transform({
      text: doc.getText(),
      start: { line: 0, col: 0 },
      end: { line: end.line, col: end.character },
    });
  } catch (e) {
    log(`${label} (ns) threw, treating as no-op: ${String(e)}`);
    return;
  }
  if (!plan) {
    log(`${label} no-op`);
    return;
  }
  await applyEditPlan(editor, plan);
}

// Apply a structural EditPlan: replace the range, optionally realign it with the editor's
// range formatter (Calva), then collapse the cursor to the plan's target. Shared by the
// form- and namespace-scoped bulk commands. formatSelection is a no-op/throws if no clojure
// range formatter is registered — the text is already structurally correct without it.
async function applyEditPlan(editor: vscode.TextEditor, plan: EditPlan): Promise<void> {
  const range = new vscode.Range(
    new vscode.Position(plan.replaceRange.start.line, plan.replaceRange.start.col),
    new vscode.Position(plan.replaceRange.end.line, plan.replaceRange.end.col),
  );
  const ok = await editor.edit((b) => b.replace(range, plan.insertText));
  if (!ok) {
    return;
  }
  if (plan.reformat) {
    const endPos = endOfInsert(plan.replaceRange.start, plan.insertText);
    editor.selection = new vscode.Selection(
      new vscode.Position(plan.replaceRange.start.line, plan.replaceRange.start.col),
      endPos,
    );
    try {
      await vscode.commands.executeCommand('editor.action.formatSelection');
    } catch (e) {
      log(`formatSelection failed: ${String(e)}`);
    }
  }
  const cursor = new vscode.Position(plan.newCursor.line, plan.newCursor.col);
  editor.selection = new vscode.Selection(cursor, cursor);
}

// The end Position of `text` inserted at `start` (0-based line/col).
function endOfInsert(start: { line: number; col: number }, text: string): vscode.Position {
  const lines = text.split('\n');
  return lines.length === 1
    ? new vscode.Position(start.line, start.col + lines[0].length)
    : new vscode.Position(start.line + lines.length - 1, lines[lines.length - 1].length);
}

// Add `[fireworks.core :refer [? !? ?> !?>]]` to the current namespace's ns form.
// The pure cljs side parses the whole document and returns an edit confined to the
// ns form (or null: no ns form, already required, or unparseable). No selection,
// cursor move, or Calva format needed.
async function runAddRequire(): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor || editor.document.languageId !== 'clojure') {
    return; // silent no-op
  }

  let plan: RequireEdit | null;
  try {
    plan = cljsLib.addFireworksRequire(editor.document.getText());
  } catch (e) {
    log(`addFireworksRequire threw, treating as no-op: ${String(e)}`);
    return;
  }
  if (!plan) {
    log('add-require no-op');
    return;
  }

  const range = new vscode.Range(
    new vscode.Position(plan.replaceRange.start.line, plan.replaceRange.start.col),
    new vscode.Position(plan.replaceRange.end.line, plan.replaceRange.end.col),
  );
  await editor.edit((b) => b.replace(range, plan!.insertText));
}

// ============================================================================
// Inline results: watch .fireworks/results/ and paint `?` values inline.
//
// When the side-effecting `?` macro runs, Fireworks writes the value to
// .fireworks/results/<ns>/<line>:<col> (1-based, at the opening `(`). When that
// file appears/changes we re-analyze the matching visible editors (pure cljs
// gives us the ns + the live `(? …)` positions) and repaint a read-only inline
// decoration at the end of each line. Toggled by fireworks.toggleInlineResults
// (flips fireworks.inlineResults.enabled, Workspace) and auto-started on
// activation when enabled. Decoupled from live coding: it only reads the files,
// whoever produced them.
// ============================================================================

const RESULTS_DIR = '.fireworks/results';

let inlineDecoration: vscode.TextEditorDecorationType | undefined;
let resultsWatcher: vscode.FileSystemWatcher | undefined;
let visibleEditorsSub: vscode.Disposable | undefined;
let inlineConfigSub: vscode.Disposable | undefined;
let themeSub: vscode.Disposable | undefined;
let docChangeSub: vscode.Disposable | undefined;
const fadeTimers = new Map<string, ReturnType<typeof setTimeout>[]>(); // in-flight fade per editor
// Values observed *this session* only: logical result key (see resultKey) -> value
// text. Painting reads from here, never straight from disk, so stale files left on
// disk from a previous run are never shown (e.g. opening a file with no watcher
// running). Populated by ingestResult; cleared on start/stop and by Clear Inline
// Results. The key is transport-independent so non-filesystem sources (a future
// WebSocket push from a browser runtime) can feed the same cache.
const resultsCache = new Map<string, string>();

// The cache identity for a single `?` result, independent of how it arrived. Today
// only the filesystem watcher writes it; a future WebSocket source builds the same
// key from values parsed off the wire.
function resultKey(root: string, ns: string, posKey: string): string {
  return `${root} ${ns} ${posKey}`;
}

// analyzeInlineResults parses the whole document with rewrite-clj — by far the heaviest
// step in a repaint. Its output (the ns + the `(? …)` positions) depends only on document
// content, which changes on edit (and bumps document.version), never on which result file
// arrived. So cache one analysis per document version: a test-refresh burst that writes
// many result files then reuses a single parse instead of re-parsing the whole file on
// every file event (the redundant parse was freezing the host, stalling the cursor).
const analysisCache = new Map<string, { version: number; analysis: InlineAnalysis }>();
function analyzeCached(doc: vscode.TextDocument): InlineAnalysis {
  const key = doc.uri.toString();
  const hit = analysisCache.get(key);
  if (hit && hit.version === doc.version) {
    return hit.analysis;
  }
  const analysis = cljsLib.analyzeInlineResults(doc.getText());
  analysisCache.set(key, { version: doc.version, analysis });
  return analysis;
}

// Coalesce result-driven repaints: a burst of result-file writes (one per `?` form on a
// test-refresh run) lands as many separate events. Collect the editors to repaint and
// flush once on the next tick so a burst paints in a single pass, not N back-to-back.
const pendingRepaints = new Map<string, vscode.TextEditor>();
let repaintTimer: ReturnType<typeof setTimeout> | undefined;
function scheduleRepaint(editor: vscode.TextEditor): void {
  pendingRepaints.set(editorKey(editor), editor);
  if (repaintTimer !== undefined) {
    return;
  }
  repaintTimer = setTimeout(() => {
    repaintTimer = undefined;
    const editors = [...pendingRepaints.values()];
    pendingRepaints.clear();
    for (const ed of editors) {
      repaintEditor(ed);
    }
  }, 16);
}

function inlineResultsEnabled(): boolean {
  return vscode.workspace.getConfiguration('fireworks').get<boolean>('inlineResults.enabled', false);
}

function clamp(n: number, lo: number, hi: number): number {
  return Number.isNaN(n) ? lo : Math.min(hi, Math.max(lo, n));
}

// Named palette for the inline `?` color. The one chosen name drives both the value
// foreground and its background tint; each name carries a light- and dark-theme HSL so
// the color stays legible on either. Keys match the `inlineResults.color` enum exactly.
const INLINE_PALETTE: Record<string, { light: string; dark: string }> = {
  Purple: { light: 'hsl(277 100% 44%)', dark: 'hsl(277 88% 74%)' },
  Blue: { light: 'hsl(213 100% 44%)', dark: 'hsl(221 100% 75%)' },
  Cyan: { light: 'hsl(168 100% 25%)', dark: 'hsl(182 100% 47%)' },
  Green: { light: 'hsl(107 100% 26%)', dark: 'hsl(107 70% 51%)' },
  Neutral: { light: 'hsl(0 0% 20%)', dark: 'hsl(0 0% 80%)' },
};
const DEFAULT_INLINE_COLOR = 'Purple';

// Which palette variant the active theme calls for. High-contrast light maps to light,
// plain high-contrast (dark) maps to dark.
function themeVariant(): 'light' | 'dark' {
  const kind = vscode.window.activeColorTheme.kind;
  return kind === vscode.ColorThemeKind.Light || kind === vscode.ColorThemeKind.HighContrastLight
    ? 'light'
    : 'dark';
}

// The decoration color: the chosen palette name resolved to its HSL for the active theme
// (falls back to the default for an unknown/empty value).
function inlineColor(): string {
  const name = cfg().get<string>('inlineResults.color', DEFAULT_INLINE_COLOR);
  const entry = INLINE_PALETTE[name] ?? INLINE_PALETTE[DEFAULT_INLINE_COLOR];
  return entry[themeVariant()];
}

// The background-tint base color: the same chosen color drives the tint.
function inlineBgColor(): string {
  return inlineColor();
}

// The background tint: inlineBgColor mixed into transparent at backgroundOpacity
// (clamped 0–0.2). undefined when the opacity is 0 (no tint).
function inlineBackground(): string | undefined {
  const op = clamp(cfg().get<number>('inlineResults.backgroundOpacity', 0.01), 0, 0.2);
  if (op <= 0) {
    return undefined;
  }
  return `color-mix(in srgb, ${inlineBgColor()} ${+(op * 100).toFixed(3)}%, transparent)`;
}

// Resting opacity of the value, floored at 0.5 so it stays legible.
function valueOpacity(): number {
  return clamp(cfg().get<number>('inlineResults.opacity', 0.75), 0.5, 1);
}

const PREFIX = '▏ ';
const OVERFLOW_PREFIX = '▏ '; // marks the "+ n more" tail when a row has >3 results
const MAX_RESULTS_PER_LINE = 3;
const SUBSEQUENT_GAP = 4; // fixed gap before each result after the first

function makeInlineDecoration(): vscode.TextEditorDecorationType {
  const color = inlineColor();
  return vscode.window.createTextEditorDecorationType({
    // ClosedClosed: the end-of-line anchor must not follow text inserted at its
    // boundary. Without this, pressing Enter with the caret at the line end (the
    // decoration's exact position) carries the value down onto the new line until
    // the next eval repaints.
    rangeBehavior: vscode.DecorationRangeBehavior.ClosedClosed,
    // Marker + gap + value share one `after` element per range (built in
    // repaintEditor). They share an opacity, but a single element is required: VS
    // Code groups all `before` attachments then all `after` attachments at the same
    // position, so a separate `before` marker bunches away from its value when two
    // `?` results land on one line. opacity rides the textDecoration CSS escape
    // hatch since the attachment has no opacity field of its own.
    after: {
      color,
      // The lead gap rides a left margin — outside the attachment's box — so the tint
      // never reaches the empty gap before the first prefix bar.
      margin: `0 0 0 ${cfg().get<number>('inlineResults.gap', 17)}ch`,
      // The tint is a per-row background-image gradient (built in repaintEditor) with
      // transparent bands over the gaps, injected via the textDecoration escape hatch.
      // A solid backgroundColor here would bleed through those bands, so it's omitted.
      textDecoration: `none; opacity: ${valueOpacity()}`,
    },
  });
}

// Recreate the (color-baked) decoration type and repaint every visible editor. Called
// when a setting or the active theme changes either of the baked-in values.
function refreshDecoration(): void {
  inlineDecoration?.dispose();
  inlineDecoration = makeInlineDecoration();
  for (const ed of vscode.window.visibleTextEditors) {
    repaintEditor(ed);
  }
}

async function toggleInlineResults(): Promise<void> {
  const next = !inlineResultsEnabled();
  await vscode.workspace
    .getConfiguration('fireworks')
    .update('inlineResults.enabled', next, vscode.ConfigurationTarget.Workspace);
  if (next) {
    startInlineResults();
  } else {
    stopInlineResults();
  }
  vscode.window.showInformationMessage(`Fireworks: inline results ${next ? 'on' : 'off'}.`);
}

function startInlineResults(): void {
  if (resultsWatcher) {
    return; // already running
  }
  resultsCache.clear(); // start blank: nothing paints until the watcher sees a write
  if (!inlineDecoration) {
    inlineDecoration = makeInlineDecoration();
  }
  resultsWatcher = vscode.workspace.createFileSystemWatcher(`**/${RESULTS_DIR}/**`);
  resultsWatcher.onDidCreate((uri) => onResultChanged(uri));
  resultsWatcher.onDidChange((uri) => onResultChanged(uri));
  resultsWatcher.onDidDelete((uri) => onResultChanged(uri));
  // Repaint editors as they come on screen (switching tabs, opening splits).
  visibleEditorsSub = vscode.window.onDidChangeVisibleTextEditors((editors) => {
    for (const ed of editors) {
      repaintEditor(ed);
    }
  });
  // React live to color/gap/maxLength changes. The color is baked into the
  // decoration type, so recreate it; gap/maxLength are read fresh on repaint.
  inlineConfigSub = vscode.workspace.onDidChangeConfiguration((e) => {
    if (!e.affectsConfiguration('fireworks.inlineResults')) {
      return;
    }
    refreshDecoration();
  });
  // The resolved color also depends on the active theme (light/dark variant), and that
  // HSL is baked into the decoration type — so recreate it on theme switches too.
  themeSub = vscode.window.onDidChangeActiveColorTheme(() => refreshDecoration());
  // Clear (don't repaint) on edits: any edit can shift a form's coordinates, and
  // reading result files by the new <line>:<col> would surface stale values from
  // other forms (phantoms). Values reappear via the watcher on the next eval, when
  // the files are fresh and at correct coordinates.
  docChangeSub = vscode.workspace.onDidChangeTextDocument((e) => {
    if (e.document.languageId !== 'clojure') {
      return;
    }
    for (const ed of vscode.window.visibleTextEditors) {
      if (ed.document === e.document && inlineDecoration) {
        cancelFade(ed);
        ed.setDecorations(inlineDecoration, []);
      }
    }
  });
  for (const ed of vscode.window.visibleTextEditors) {
    repaintEditor(ed);
  }
}

function stopInlineResults(): void {
  resultsWatcher?.dispose();
  resultsWatcher = undefined;
  visibleEditorsSub?.dispose();
  visibleEditorsSub = undefined;
  inlineConfigSub?.dispose();
  inlineConfigSub = undefined;
  themeSub?.dispose();
  themeSub = undefined;
  docChangeSub?.dispose();
  docChangeSub = undefined;
  for (const timers of fadeTimers.values()) {
    for (const t of timers) {
      clearTimeout(t);
    }
  }
  fadeTimers.clear();
  if (repaintTimer !== undefined) {
    clearTimeout(repaintTimer);
    repaintTimer = undefined;
  }
  pendingRepaints.clear();
  analysisCache.clear();
  resultsCache.clear();
  inlineDecoration?.dispose(); // clears every decoration it owns
  inlineDecoration = undefined;
}

// Manual wipe: empty the session cache and clear decorations from every visible
// editor (does not delete any result files). Bound to fireworks.clearInlineResults.
function clearInlineResults(): void {
  resultsCache.clear();
  if (!inlineDecoration) {
    return;
  }
  for (const ed of vscode.window.visibleTextEditors) {
    cancelFade(ed);
    ed.setDecorations(inlineDecoration, []);
  }
}

// Delete a project root's on-disk result tree (<root>/.fireworks/results). Called when a
// live coding run starts or restarts so each run begins on a clean slate — orphan files
// from earlier edits (forms that have since moved or been removed) don't accumulate.
// Best-effort: a run recreates whatever it needs, so a failure here is non-fatal. Only the
// `results` subdir is removed; `.fireworks/` itself (which may hold other state) is left
// intact. The in-memory cache is cleared separately by clearInlineResults.
function clearResultFiles(root: string): void {
  const dir = path.join(root, '.fireworks', 'results');
  try {
    fs.rmSync(dir, { recursive: true, force: true });
  } catch (e) {
    log(`could not clear result files at ${dir}: ${String(e)}`);
  }
}

// Make sure <root>/.fireworks exists. The library creates it on the first write, but
// pre-creating it lets the .gitignore check below resolve the dir-only pattern and gives
// the project a visible home for the result cache. Best-effort.
function ensureFireworksDir(root: string): void {
  try {
    fs.mkdirSync(path.join(root, '.fireworks'), { recursive: true });
  } catch (e) {
    log(`could not create .fireworks dir at ${root}: ${String(e)}`);
  }
}

// Ensure <root>/.fireworks is git-ignored so the ephemeral result cache never lands in
// `git status`. Conservative and idempotent: skipped when the manageGitignore setting is
// off; `git check-ignore` decides, so it does nothing when the path is already ignored (by
// any gitignore — this one, a parent, or a global) or when there's no git repo. Only when
// the path is genuinely untracked does it append one commented entry to <root>/.gitignore
// (created only if missing). Never rewrites or reorders existing content. Failures log.
function ensureGitignored(root: string): void {
  // Deliberately NOT contributed in package.json, so it stays out of the Settings UI —
  // it's a JSON-only escape hatch. get() still honors it from settings.json; the explicit
  // `true` is the effective default. Don't add it back to contributes.configuration.
  if (!cfg().get<boolean>('liveCoding.manageGitignore', true)) {
    return;
  }
  try {
    // -q exit codes: 0 = already ignored, 1 = not ignored, 128 = not a repo / git error.
    // We act only on 1; a null status (git missing) is likewise left alone.
    const res = cp.spawnSync('git', ['-C', root, 'check-ignore', '-q', '.fireworks']);
    if (res.status !== 1) {
      return;
    }
    const gitignore = path.join(root, '.gitignore');
    const prior = fs.existsSync(gitignore) ? fs.readFileSync(gitignore, 'utf8') : '';
    // Separate from any prior content with a blank line; don't double up newlines.
    const lead = prior === '' || prior.endsWith('\n') ? '' : '\n';
    fs.appendFileSync(gitignore, `${lead}\n# Fireworks inline-result cache\n.fireworks/\n`);
    log(`added .fireworks/ to ${gitignore}`);
  } catch (e) {
    log(`could not update .gitignore at ${root}: ${String(e)}`);
  }
}

// Prepare a root's result cache for a fresh live-coding run: make sure .fireworks exists
// and is git-ignored, then drop the previous run's (and any orphaned) result files.
function prepareResultsDir(root: string): void {
  ensureFireworksDir(root);
  ensureGitignored(root);
  clearResultFiles(root);
}

// A result file changed. Its path is <root>/.fireworks/results/<ns>/<line>:<col>.
// Thin filesystem adapter: parse the logical (root, ns, posKey) identity off the
// path, read the value (null = deleted/unreadable), and hand it to ingestResult.
function onResultChanged(uri: vscode.Uri): void {
  const ns = namespaceFromResultPath(uri.fsPath);
  const root = rootFor(uri.fsPath);
  if (!ns || !root) {
    return;
  }
  const posKey = uri.fsPath.split(/[\\/]/).pop();
  if (!posKey) {
    return;
  }
  ingestResult(root, ns, posKey, readFileOrNull(uri.fsPath));
}

// The one place a value enters (or leaves) the session cache, regardless of source.
// Records the write (`value`) or eviction (`null`), then repaints every visible
// editor whose parsed ns matches. A future WebSocket source calls this directly with
// values parsed off the wire — no filesystem touch.
function ingestResult(root: string, ns: string, posKey: string, value: string | null): void {
  const key = resultKey(root, ns, posKey);
  if (value === null) {
    resultsCache.delete(key);
  } else {
    resultsCache.set(key, value);
  }
  for (const ed of vscode.window.visibleTextEditors) {
    if (ed.document.languageId !== 'clojure') {
      continue;
    }
    // Cached parse (cheap on a hit); the repaint it schedules reuses the same cache.
    if (analyzeCached(ed.document).namespace === ns) {
      scheduleRepaint(ed);
    }
  }
}

// The <ns> segment that follows ".fireworks/results/" in a result file path.
function namespaceFromResultPath(fsPath: string): string | null {
  const marker = `${path.sep}${RESULTS_DIR.split('/').join(path.sep)}${path.sep}`;
  const i = fsPath.indexOf(marker);
  if (i < 0) {
    return null;
  }
  const rest = fsPath.slice(i + marker.length); // "<ns>/<line>:<col>"
  const ns = rest.split(/[\\/]/)[0];
  return ns || null;
}

// The cached project root that contains this document, or undefined.
function rootFor(fsPath: string): string | undefined {
  return knownRoots.find((root) => fsPath === root || fsPath.startsWith(root + path.sep));
}

// Render the value for each live `(? …)` in `editor` (clearing any stale ones).
function repaintEditor(editor: vscode.TextEditor): void {
  if (!inlineDecoration || editor.document.languageId !== 'clojure') {
    return;
  }
  const { namespace, positions } = analyzeCached(editor.document);
  const root = rootFor(editor.document.uri.fsPath);
  if (!namespace || !root || positions.length === 0) {
    cancelFade(editor);
    editor.setDecorations(inlineDecoration, []);
    return;
  }

  const maxLength = cfg().get<number>('inlineResults.maxLength', 80);
  const budget = maxLength > 0 ? maxLength : Infinity; // total chars shared per row
  const nbsp = (n: number): string => '\u00a0'.repeat(Math.max(0, n));

  // Group results by render row (end row). positions arrive in source order, so
  // each row's values stay left-to-right.
  const byRow = new Map<number, string[]>();
  for (const pos of positions) {
    const line = pos.row - 1; // 1-based end row -> 0-based render line
    if (!Number.isInteger(line) || line < 0 || line >= editor.document.lineCount) {
      continue;
    }
    // The result is keyed by the form's start position (pos.key), but the
    // decoration is anchored on its end row (pos.row). Read from the session cache,
    // not disk, so only values written since start (never stale leftovers) paint.
    const raw = resultsCache.get(resultKey(root, namespace, pos.key));
    if (raw === undefined) {
      continue;
    }
    const arr = byRow.get(line) ?? [];
    arr.push(singleLine(raw));
    byRow.set(line, arr);
  }

  // One decoration per row: up to MAX_RESULTS_PER_LINE values (the budget split as
  // evenly as possible across them) then a "+ n more" tail, all in one `after` element
  // to keep order stable (VS Code groups same-position attachments, breaking
  // left-to-right order). The tint is a per-row gradient (built from each segment's
  // character offset) that stays transparent over the gaps between values.
  const tint = inlineBackground();
  const paints: InlinePaint[] = [];
  for (const [line, values] of byRow) {
    const shown = values.slice(0, MAX_RESULTS_PER_LINE).reverse();
    const overflow = values.length - shown.length;
    const alloc = allocateEven(shown.map((v) => v.length), budget);
    let content = '';
    const bands: Band[] = []; // [start, end) of the tint for each value, in characters
    let col = 0; // running character offset within the content box (after the margin)
    const addSegment = (gapN: number, prefix: string, text: string): void => {
      // First value's lead gap is the left margin; inner values carry an in-text gap.
      content += nbsp(gapN) + prefix + text;
      col += gapN;
      // Start the tint at the bar cell's left edge — slightly left of the visible bar
      // glyph — so the colorization butts right up to the bar with no gap before it.
      bands.push({ start: col, end: col + prefix.length + text.length });
      col += prefix.length + text.length;
    };
    shown.forEach((v, i) =>
      addSegment(i === 0 ? 0 : SUBSEQUENT_GAP, PREFIX, truncateTo(v, alloc[i])),
    );
    if (overflow > 0) {
      addSegment(SUBSEQUENT_GAP, OVERFLOW_PREFIX, `+ ${overflow} more`);
    }
    const eol = editor.document.lineAt(line).range.end;
    paints.push({
      // Non-breaking spaces: VS Code collapses runs of normal spaces in contentText.
      range: new vscode.Range(eol, eol),
      content,
      gradient: tint ? buildGradient(bands, tint) : undefined,
    });
  }
  paintWithFade(editor, paints);
}

function editorKey(editor: vscode.TextEditor): string {
  return `${editor.document.uri.toString()}::${editor.viewColumn ?? 'a'}`;
}

// Cancel an in-flight fade for `editor` so a new paint/clear doesn't fight it.
function cancelFade(editor: vscode.TextEditor): void {
  const key = editorKey(editor);
  const timers = fadeTimers.get(key);
  if (timers) {
    for (const t of timers) {
      clearTimeout(t);
    }
    fadeTimers.delete(key);
  }
}

interface Band {
  start: number; // tint starts here, in characters (ch)
  end: number; // tint ends here
}

interface InlinePaint {
  range: vscode.Range;
  content: string; // the `after` contentText for the row
  gradient?: string; // this row's background-image value, or undefined for no tint
}

// The `after` CSS, smuggled through the textDecoration escape hatch (the attachment
// has no opacity/background-image fields of its own): the value opacity, plus the
// per-row tint gradient when one is wanted. background-image — not backgroundColor — so
// it can carry transparent bands over the gaps (and a two-color tint sweep).
function afterCss(opacity: number, gradient?: string): string {
  const base = `none; opacity: ${opacity}`;
  return gradient ? `${base}; background-image: ${gradient}` : base;
}

// A left-to-right gradient that tints [start, end) of each band and stays transparent
// everywhere else (the gaps between values). Offsets are in ch; the content box ends at
// the last value, and any sub-cell remainder past it is left transparent.
function buildGradient(bands: Band[], tint: string): string {
  const stops: string[] = [];
  let cursor = 0;
  for (const b of bands) {
    stops.push(`transparent ${cursor}ch`, `transparent ${b.start}ch`);
    stops.push(`${tint} ${b.start}ch`, `${tint} ${b.end}ch`);
    cursor = b.end;
  }
  stops.push(`transparent ${cursor}ch`);
  return `linear-gradient(to right, ${stops.join(', ')})`;
}

// A row's DecorationOptions at the given opacity (rebuilt per fade frame). The
// per-range after.textDecoration overrides the type's, carrying opacity + the tint
// gradient (which already encodes one or two colors).
function toOption(p: InlinePaint, opacity: number): vscode.DecorationOptions {
  return {
    range: p.range,
    renderOptions: {
      after: { contentText: p.content, textDecoration: afterCss(opacity, p.gradient) },
    },
  };
}

// Paint `paints`, fading opacity 0 -> the configured value opacity over fadeInMs
// in a few frames. fadeInMs <= 0 paints at full opacity immediately (no animation).
function paintWithFade(editor: vscode.TextEditor, paints: InlinePaint[]): void {
  if (!inlineDecoration) {
    return;
  }
  cancelFade(editor);
  const fadeMs = cfg().get<number>('inlineResults.fadeInMs', 90);
  if (fadeMs <= 0 || paints.length === 0) {
    editor.setDecorations(
      inlineDecoration,
      paints.map((p) => toOption(p, valueOpacity())),
    );
    return;
  }
  const target = valueOpacity();
  const frames = Math.max(2, Math.round(fadeMs / 16)); // ~60fps
  const timers: ReturnType<typeof setTimeout>[] = [];
  for (let i = 1; i <= frames; i++) {
    const opacity = (target * i) / frames;
    timers.push(
      setTimeout(
        () => {
          if (inlineDecoration) {
            editor.setDecorations(
              inlineDecoration,
              paints.map((p) => toOption(p, opacity)),
            );
          }
        },
        (fadeMs * i) / frames,
      ),
    );
  }
  fadeTimers.set(editorKey(editor), timers);
}

// The value's first line, with "..." appended when it had more lines. (A lone
// trailing newline does not count as "more".)
function singleLine(raw: string): string {
  const lines = raw.replace(/\n+$/, '').split('\n');
  return lines.length > 1 ? lines[0] + '...' : lines[0];
}

// Truncate `s` to `total` characters total, ending in "..." when shortened.
function truncateTo(s: string, total: number): string {
  if (s.length <= total) {
    return s;
  }
  return total <= 3 ? '...'.slice(0, Math.max(0, total)) : s.slice(0, total - 3) + '...';
}

// Max-min fair split of `budget` chars across items of the given full lengths:
// items shorter than their share keep their full length and donate the surplus, so
// the longer items come out as even as possible. budget may be Infinity (no cap).
function allocateEven(lengths: number[], budget: number): number[] {
  const alloc = new Array<number>(lengths.length).fill(0);
  let active = lengths.map((_, i) => i);
  let remaining = budget;
  while (active.length > 0) {
    const share = Math.floor(remaining / active.length);
    const fits = active.filter((i) => lengths[i] <= share);
    if (fits.length === 0) {
      let extra = remaining - share * active.length; // spread the remainder one-per-item
      for (const i of active) {
        alloc[i] = share + (extra > 0 ? 1 : 0);
        if (extra > 0) extra--;
      }
      break;
    }
    for (const i of fits) {
      alloc[i] = lengths[i];
      remaining -= lengths[i];
    }
    active = active.filter((i) => lengths[i] > share);
  }
  return alloc;
}

// ============================================================================
// Phase 2: live coding (test-refresh watcher)
//
// Start/Stop/Restart a test-refresh watcher in an integrated terminal at the picked
// deps.edn root. The watcher command is the `fireworks.liveCoding.command` setting
// (default `clojure -M:test-refresh`), run verbatim — Fireworks does not inject deps
// or macros and does not write any project files. Those are deferred; the cljs
// config code (defaultConfig/readMode/setMode) stays in place for re-wiring later.
// deps.edn projects only — Leiningen is out of scope (set that up by hand).
// ============================================================================

const TERMINAL_NAME = 'Fireworks: Live Code';

// Cosmetic: ms to let the shell prompt finish drawing before sending the watcher
// command, so zsh's line editor doesn't double-echo it. Remove the `await` in
// sendWatcherCommand (and this const) to revert.
const PROMPT_SETTLE_MS = 250;

let liveTerminal: vscode.Terminal | undefined;
let liveExecution: vscode.TerminalShellExecution | undefined;
let statusBar: vscode.StatusBarItem;
let extContext: vscode.ExtensionContext;
let knownRoots: string[] = []; // deps.edn dirs in the workspace (cached for the status bar)
let activeRoot: string | undefined; // the root the running / last-used session operates on

function cfg(): vscode.WorkspaceConfiguration {
  return vscode.workspace.getConfiguration('fireworks');
}

function readFileOrNull(filePath: string): string | null {
  try {
    return fs.readFileSync(filePath, 'utf8');
  } catch {
    return null;
  }
}

// Cheap PATH check; the terminal owns real runtime errors (see the design doc).
function onPath(command: string): boolean {
  try {
    cp.execFileSync(process.platform === 'win32' ? 'where' : 'which', [command], {
      stdio: 'ignore',
    });
    return true;
  } catch {
    return false;
  }
}

// Every directory in the workspace that holds a deps.edn — across all workspace
// folders and nested subprojects (e.g. a monorepo's repo/foo).
async function findProjectRoots(): Promise<string[]> {
  const uris = await vscode.workspace.findFiles('**/deps.edn', '**/node_modules/**');
  const roots = new Set<string>();
  for (const u of uris) {
    roots.add(path.dirname(u.fsPath));
  }
  return [...roots].sort();
}

// Abbreviate a home-relative path with `~`, the way VS Code's own pickers do.
function tildify(p: string): string {
  const home = os.homedir();
  if (p === home) {
    return '~';
  }
  return p.startsWith(home + path.sep) ? '~' + p.slice(home.length) : p;
}

// Resolve which project root to act on: the only one, or a pick when the workspace has
// several. The pick mirrors VS Code's own "new terminal" cwd picker — folder name as the
// label, the home-abbreviated parent path as the muted description. undefined if none, or
// the pick was dismissed.
async function pickProjectRoot(): Promise<string | undefined> {
  const roots = await findProjectRoots();
  if (roots.length === 0) {
    vscode.window.showErrorMessage('Fireworks: no deps.edn found in this workspace.');
    return undefined;
  }
  if (roots.length === 1) {
    return roots[0];
  }
  const pick = await vscode.window.showQuickPick(
    roots.map((root) => ({
      label: path.basename(root),
      description: tildify(path.dirname(root)),
      root,
    })),
    {
      placeHolder: 'Select the Clojure project for Fireworks live coding',
      matchOnDescription: true, // typing filters on the path too, like the native picker
    },
  );
  return pick?.root;
}

// Refresh the cached roots (for the status bar) and repaint it.
async function refreshRoots(): Promise<void> {
  knownRoots = await findProjectRoots();
  updateStatusBar();
}

function preflight(): boolean {
  if (onPath('clojure') || onPath('clj')) {
    return true;
  }
  vscode.window.showErrorMessage(
    'Fireworks: the `clojure` (or `clj`) command is not on your PATH. Install the Clojure CLI, then try again.',
  );
  return false;
}

// The watcher command, run verbatim in the integrated terminal: the
// `fireworks.liveCoding.command` setting, or `clojure -M:test-refresh` if it's blank.
function watchCommand(): string {
  return cfg().get<string>('liveCoding.command', '').trim() || 'clojure -M:test-refresh';
}

function waitForShellIntegration(
  terminal: vscode.Terminal,
  timeoutMs = 2000,
): Promise<vscode.TerminalShellIntegration | undefined> {
  if (terminal.shellIntegration) {
    return Promise.resolve(terminal.shellIntegration);
  }
  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      disposable.dispose();
      resolve(undefined);
    }, timeoutMs);
    const disposable = vscode.window.onDidChangeTerminalShellIntegration((e) => {
      if (e.terminal === terminal) {
        disposable.dispose();
        clearTimeout(timer);
        resolve(e.shellIntegration);
      }
    });
  });
}

// Send the watcher command, tracking the execution when shell integration is
// available. All launches route through here.
async function sendWatcherCommand(
  terminal: vscode.Terminal,
  si: vscode.TerminalShellIntegration | undefined,
  command: string,
): Promise<void> {
  // --- cosmetic: avoid the duplicate command echo (delete this await to revert) ---
  await new Promise((r) => setTimeout(r, PROMPT_SETTLE_MS));
  // ---------------------------------------------------------------------------------
  if (terminal !== liveTerminal) {
    return; // terminal closed/replaced during the settle delay
  }
  if (si) {
    liveExecution = si.executeCommand(command);
  } else {
    terminal.sendText(command); // fallback: no execution tracking
    liveExecution = undefined;
  }
}

async function startLiveCoding(): Promise<void> {
  clearInlineResults(); // wipe any stale inline values before the new session paints
  if (liveTerminal) {
    if (liveTerminal.shellIntegration && liveExecution === undefined) {
      // Shell integration confirmed the process exited — restart in the same terminal
      if (activeRoot) {
        prepareResultsDir(activeRoot); // same fresh-slate prep as launchWatcher
      }
      liveTerminal.show();
      // Wipe the dead session's scrollback first (Cmd-K equivalent) so the new run
      // starts on a clean terminal. Acts on the active terminal, which show() just made it.
      await vscode.commands.executeCommand('workbench.action.terminal.clear');
      await sendWatcherCommand(liveTerminal, liveTerminal.shellIntegration, watchCommand());
      return;
    }
    liveTerminal.show(); // process running, or no shell integration — just focus
    return;
  }
  const root = await pickProjectRoot();
  if (!root) {
    return;
  }
  if (!preflight()) {
    return;
  }
  await launchWatcher(root);
}

// Run the watcher for a known root. Records it as the active session so
// Stop/Restart act on the same project without re-asking.
async function launchWatcher(root: string): Promise<void> {
  prepareResultsDir(root); // ensure .fireworks exists + ignored, then fresh-slate results
  activeRoot = root;
  liveExecution = undefined;
  const command = watchCommand();
  log(`live coding -> ${command} (cwd ${root})`);
  liveTerminal = vscode.window.createTerminal({ name: TERMINAL_NAME, cwd: root });
  liveTerminal.show();

  const si = await waitForShellIntegration(liveTerminal);
  if (!liveTerminal) return; // closed during wait

  await sendWatcherCommand(liveTerminal, si, command);
}

function stopLiveCoding(): void {
  if (!liveTerminal) {
    return;
  }
  liveTerminal.sendText('\u0003'); // Ctrl-C: let test-refresh shut down cleanly
  liveTerminal.dispose();
  liveTerminal = undefined;
  liveExecution = undefined;
}

async function restartLiveCoding(): Promise<void> {
  const root = activeRoot;
  stopLiveCoding();
  if (root) {
    await launchWatcher(root); // reuse the running session's root (no re-prompt)
  } else {
    await startLiveCoding();
  }
}

function updateStatusBar(): void {
  const on = inlineResultsEnabled();
  statusBar.text = on ? '$(circle-filled) Fireworks' : '$(circle-outline) Fireworks';
  statusBar.tooltip = on
    ? 'Fireworks: inline results on. Click to turn off.'
    : 'Fireworks: inline results off. Click to turn on.';
  statusBar.show();
}
