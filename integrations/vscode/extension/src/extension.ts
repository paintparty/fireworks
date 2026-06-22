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
    vscode.commands.registerCommand('fireworks.setAutoSaveDelay', () => setAutoSaveDelay()),
    vscode.commands.registerCommand('fireworks.setInlineResultsColor', () => setInlineResultsColor()),
    vscode.commands.registerCommand('fireworks.setInlineResultsBackgroundOpacity', () =>
      setInlineResultsBackgroundOpacity(),
    ),
    vscode.commands.registerCommand('fireworks.setInlineResultsOpacity', () =>
      setInlineResultsOpacity(),
    ),
    vscode.commands.registerCommand('fireworks.setInlineResultsGap', () => setInlineResultsGap()),
    vscode.commands.registerCommand('fireworks.setInlineResultsMaxLength', () =>
      setInlineResultsMaxLength(),
    ),
    vscode.commands.registerCommand('fireworks.setInlineResultsFadeIn', () =>
      setInlineResultsFadeIn(),
    ),
    vscode.window.onDidCloseTerminal((t) => {
      for (const [root, s] of liveSessions) {
        if (s.terminal === t) {
          liveSessions.delete(root);
          break;
        }
      }
    }),
    vscode.window.onDidEndTerminalShellExecution((e) => {
      for (const s of liveSessions.values()) {
        if (s.execution === e.execution) {
          s.execution = undefined; // process exited; terminal may still be open
          break;
        }
      }
    }),
    vscode.workspace.onDidChangeWorkspaceFolders(() => {
      updateStatusBar();
    }),
    vscode.window.onDidChangeActiveTextEditor(() => {
      updateStatusBar();
    }),
    vscode.workspace.onDidChangeConfiguration((e) => {
      if (e.affectsConfiguration('fireworks.inlineResults.enabled')) {
        updateStatusBar();
      }
    }),
  );

  context.subscriptions.push({ dispose: () => stopInlineResults() });

  updateStatusBar();
  if (inlineResultsEnabled()) {
    startInlineResults();
  }
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

// A transient bottom-right notification that auto-dismisses after `ms` (default 5s). VS
// Code exposes no timeout on window.showInformationMessage, so drive a notification-
// location progress task that resolves on a timer — the toast closes when it resolves.
function flash(message: string, ms = 5000): void {
  void vscode.window.withProgress(
    { location: vscode.ProgressLocation.Notification, title: message },
    () => new Promise<void>((resolve) => setTimeout(resolve, ms)),
  );
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

// Project roots that have a `.fireworks/` results tree, learned as result files arrive
// (and lazily when mapping an editor to its root). This is what associates a document
// with the root its results were cached under — independent of deps.edn or how the
// results were produced (Babashka, lein, deps). Live coding's deps.edn root discovery
// (findProjectRoots) is separate and unaffected.
const fireworksRoots = new Set<string>();

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
  const op = clamp(cfg().get<number>('inlineResults.backgroundOpacity', 0.03), 0, 0.3);
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
  flash(`Fireworks: inline results ${next ? 'on' : 'off'}.`);
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
  fireworksRoots.clear();
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

// Per-root wipe (multi-session safe): drop only one root's cached values and clear
// decorations from its visible editors, leaving other projects' inline results intact.
// resultKey is "<root> <ns> <pos>", so the root's entries share the "<root> " prefix.
function clearResultsForRoot(root: string): void {
  const prefix = `${root} `;
  for (const key of resultsCache.keys()) {
    if (key.startsWith(prefix)) {
      resultsCache.delete(key);
    }
  }
  if (!inlineDecoration) {
    return;
  }
  for (const ed of vscode.window.visibleTextEditors) {
    if (rootFor(ed.document.uri.fsPath) === root) {
      cancelFade(ed);
      ed.setDecorations(inlineDecoration, []);
    }
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
  const root = rootFromResultPath(uri.fsPath);
  if (!ns || !root) {
    return;
  }
  fireworksRoots.add(root); // remember it so repaint can map this root's editors
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

// The project root for a result file: everything before the ".fireworks/results/"
// marker (i.e. the dir that holds `.fireworks`). Mirrors namespaceFromResultPath.
function rootFromResultPath(fsPath: string): string | null {
  const marker = `${path.sep}${RESULTS_DIR.split('/').join(path.sep)}${path.sep}`;
  const i = fsPath.indexOf(marker);
  return i < 0 ? null : fsPath.slice(0, i);
}

// Whether `dir` contains a `.fireworks` directory on disk.
function hasFireworksDir(dir: string): boolean {
  try {
    return fs.statSync(path.join(dir, '.fireworks')).isDirectory();
  } catch {
    return false;
  }
}

// The `.fireworks` project root that contains this path, or undefined. Prefers an
// already-discovered root (longest match wins for nested projects); otherwise walks up
// the directory tree to the nearest ancestor holding a `.fireworks` dir and records it.
// The root string is the parent of `.fireworks`, matching what rootFromResultPath
// stores, so cache keys (resultKey) line up between ingestion and repaint.
function rootFor(fsPath: string): string | undefined {
  let best: string | undefined;
  for (const root of fireworksRoots) {
    if (
      (fsPath === root || fsPath.startsWith(root + path.sep)) &&
      (best === undefined || root.length > best.length)
    ) {
      best = root;
    }
  }
  if (best) {
    return best;
  }
  let dir = path.dirname(fsPath);
  for (;;) {
    if (hasFireworksDir(dir)) {
      fireworksRoots.add(dir);
      return dir;
    }
    const parent = path.dirname(dir);
    if (parent === dir) {
      return undefined;
    }
    dir = parent;
  }
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
// Start/Stop/Restart a watcher in an integrated terminal at the picked project root.
// The flow is two picks and no magic: pick the project root, then pick what to run from
// that project's own config — a deps.edn alias (`clojure -M:<alias>`) or, for a Babashka
// project, a bb.edn task (`bb <task>`). The user owns those files — it's on them to
// make the alias/task pull in test-refresh + Fireworks. Fireworks injects nothing and
// writes no project files. Leiningen is out of scope (set that up by hand).
// ============================================================================

// The terminal name for a root's session — basename-tagged so concurrent sessions in
// different projects are distinguishable in the terminal dropdown.
function terminalName(root: string): string {
  return `Fireworks: Live Code — ${path.basename(root)}`;
}

// Cosmetic: ms to let the shell prompt finish drawing before sending the watcher
// command, so zsh's line editor doesn't double-echo it. Remove the `await` in
// sendWatcherCommand (and this const) to revert.
const PROMPT_SETTLE_MS = 250;

// One live-coding session per project root. Several can run at once (different projects
// in the same workspace); each owns its own integrated terminal.
interface LiveSession {
  root: string;
  command: string; // the verbatim watcher command it launched with (for restart)
  terminal: vscode.Terminal;
  execution: vscode.TerminalShellExecution | undefined; // tracked when shell integration is available
}
const liveSessions = new Map<string, LiveSession>(); // keyed by root

let statusBar: vscode.StatusBarItem;
let extContext: vscode.ExtensionContext;

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

// Every directory in the workspace that holds a deps.edn or a bb.edn — across all
// workspace folders and nested subprojects (e.g. a monorepo's repo/foo). The project
// kind (Clojure CLI vs Babashka) is resolved per-root at launch by projectKind.
async function findProjectRoots(): Promise<string[]> {
  const uris = await vscode.workspace.findFiles('**/{deps.edn,bb.edn}', '**/node_modules/**');
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
    vscode.window.showErrorMessage('Fireworks: no deps.edn or bb.edn found in this workspace.');
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
      placeHolder: 'Select the project for Fireworks live coding',
      matchOnDescription: true, // typing filters on the path too, like the native picker
    },
  );
  return pick?.root;
}

function preflightClojure(): boolean {
  if (onPath('clojure') || onPath('clj')) {
    return true;
  }
  vscode.window.showErrorMessage(
    'Fireworks: the `clojure` (or `clj`) command is not on your PATH. Install the Clojure CLI, then try again.',
  );
  return false;
}

function preflightBb(): boolean {
  if (onPath('bb')) {
    return true;
  }
  vscode.window.showErrorMessage(
    'Fireworks: the `bb` (Babashka) command is not on your PATH. Install Babashka, then try again.',
  );
  return false;
}

// The watcher command for a chosen deps.edn alias (Clojure CLI). The alias supplies its
// own deps and :main-opts (test-refresh + Fireworks).
function aliasCommand(alias: string): string {
  return `clojure -M:${alias}`;
}

// The watcher command for a chosen bb.edn task (Babashka). The task supplies the watcher.
// `bb <task>` (not `bb run <task>`): a user task overrides a same-named built-in, so this
// runs the task even for builtin-colliding names — and it avoids the `bb run` footgun where
// a task literally named `run` would swallow the argument.
function taskCommand(task: string): string {
  return `bb ${task}`;
}

// Which runtime to launch for `root`: deps.edn -> Clojure CLI alias; bb.edn -> Babashka
// task. When a project has both files, ask. undefined if neither exists (shouldn't happen
// via pickProjectRoot) or the disambiguation pick was dismissed.
async function projectKind(root: string): Promise<'deps' | 'bb' | undefined> {
  const hasDeps = fs.existsSync(path.join(root, 'deps.edn'));
  const hasBb = fs.existsSync(path.join(root, 'bb.edn'));
  if (hasDeps && hasBb) {
    const pick = await vscode.window.showQuickPick(
      [
        { label: 'Clojure (deps.edn alias)', runtime: 'deps' as const },
        { label: 'Babashka (bb.edn task)', runtime: 'bb' as const },
      ],
      { placeHolder: 'This project has both deps.edn and bb.edn — which runtime?' },
    );
    return pick?.runtime;
  }
  return hasBb ? 'bb' : hasDeps ? 'deps' : undefined;
}

// Pick an alias defined in `root`'s deps.edn. Reads the file, asks the cljs side for the
// alias names, and shows a quick pick. undefined when the file is missing/unparseable,
// defines no aliases, or the pick was dismissed (each surfaces its own message).
async function pickAlias(root: string): Promise<string | undefined> {
  const text = readFileOrNull(path.join(root, 'deps.edn'));
  if (text === null) {
    vscode.window.showErrorMessage(`Fireworks: could not read deps.edn in ${tildify(root)}.`);
    return undefined;
  }
  const { aliases, error } = cljsLib.depsAliases(text);
  if (error || !aliases) {
    vscode.window.showErrorMessage(`Fireworks: could not parse deps.edn in ${tildify(root)}.`);
    return undefined;
  }
  if (aliases.length === 0) {
    vscode.window.showErrorMessage(
      `Fireworks: no aliases defined in ${tildify(root)}/deps.edn. Add an alias with test-refresh + Fireworks, then try again.`,
    );
    return undefined;
  }
  // Show each alias as a keyword (`:dev`) but keep the bare name for `clojure -M:<alias>`.
  const pick = await vscode.window.showQuickPick(
    aliases.map((alias) => ({ label: `:${alias}`, alias })),
    { placeHolder: 'Select the deps.edn alias to run (must include test-refresh + Fireworks)' },
  );
  return pick?.alias;
}

// Pick a task defined in `root`'s bb.edn. Mirrors pickAlias for Babashka projects.
// undefined when the file is missing/unparseable, defines no tasks, or the pick was dismissed.
async function pickTask(root: string): Promise<string | undefined> {
  const text = readFileOrNull(path.join(root, 'bb.edn'));
  if (text === null) {
    vscode.window.showErrorMessage(`Fireworks: could not read bb.edn in ${tildify(root)}.`);
    return undefined;
  }
  const { tasks, error } = cljsLib.bbTasks(text);
  if (error || !tasks) {
    vscode.window.showErrorMessage(`Fireworks: could not parse bb.edn in ${tildify(root)}.`);
    return undefined;
  }
  if (tasks.length === 0) {
    vscode.window.showErrorMessage(
      `Fireworks: no tasks defined in ${tildify(root)}/bb.edn. Add a task that runs your watcher, then try again.`,
    );
    return undefined;
  }
  return vscode.window.showQuickPick(tasks, {
    placeHolder: 'Select the bb.edn task to run (must start your Fireworks watcher)',
  });
}

// Resolve the verbatim watcher command for `root`: detect the runtime, run its PATH
// preflight, then pick the alias (deps) or task (bb). undefined aborts the launch (a
// message was already shown, or a pick/preflight failed).
async function resolveWatcherCommand(root: string): Promise<string | undefined> {
  const kind = await projectKind(root);
  if (!kind) {
    return undefined;
  }
  if (kind === 'bb') {
    if (!preflightBb()) {
      return undefined;
    }
    const task = await pickTask(root);
    return task ? taskCommand(task) : undefined;
  }
  if (!preflightClojure()) {
    return undefined;
  }
  const alias = await pickAlias(root);
  return alias ? aliasCommand(alias) : undefined;
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

// Send the watcher command into a session's terminal, tracking the execution when shell
// integration is available. All launches route through here.
async function sendWatcherCommand(
  session: LiveSession,
  si: vscode.TerminalShellIntegration | undefined,
  command: string,
): Promise<void> {
  // --- cosmetic: avoid the duplicate command echo (delete this await to revert) ---
  await new Promise((r) => setTimeout(r, PROMPT_SETTLE_MS));
  // ---------------------------------------------------------------------------------
  if (liveSessions.get(session.root) !== session) {
    return; // session closed/replaced during the settle delay
  }
  if (si) {
    session.execution = si.executeCommand(command);
  } else {
    session.terminal.sendText(command); // fallback: no execution tracking
    session.execution = undefined;
  }
}

async function startLiveCoding(): Promise<void> {
  const root = await pickProjectRoot();
  if (!root) {
    return;
  }
  const existing = liveSessions.get(root);
  if (existing) {
    await reuseOrNotify(existing);
    return;
  }
  const command = await resolveWatcherCommand(root);
  if (!command) {
    return;
  }
  await launchWatcher(root, command);
}

// A session already exists for the picked root. If its process has exited (the terminal
// is lingering), restart it in place. Otherwise it's genuinely running: reveal it and
// show a modal notice — starting a second session for the same project is a no-op.
async function reuseOrNotify(session: LiveSession): Promise<void> {
  const exited = session.terminal.shellIntegration && session.execution === undefined;
  if (exited) {
    prepareResultsDir(session.root);
    clearResultsForRoot(session.root);
    session.terminal.show();
    // Wipe the dead session's scrollback first (Cmd-K equivalent) so the new run starts
    // clean. Acts on the active terminal, which show() just made it.
    await vscode.commands.executeCommand('workbench.action.terminal.clear');
    await sendWatcherCommand(session, session.terminal.shellIntegration, session.command);
    return;
  }
  session.terminal.show();
  await vscode.window.showInformationMessage(
    'Fireworks: Live Code already running in this project:',
    { modal: true, detail: path.basename(session.root) },
  );
}

// Run the watcher for a root + resolved command, registering it as a session so
// Stop/Restart can act on it. Concurrent sessions for other roots are left untouched.
async function launchWatcher(root: string, command: string): Promise<void> {
  prepareResultsDir(root); // on-disk fresh slate for this root
  clearResultsForRoot(root); // in-memory fresh slate for this root only (not other projects')
  log(`live coding -> ${command} (cwd ${root})`);
  const terminal = vscode.window.createTerminal({ name: terminalName(root), cwd: root });
  const session: LiveSession = { root, command, terminal, execution: undefined };
  liveSessions.set(root, session);
  terminal.show();

  const si = await waitForShellIntegration(terminal);
  if (liveSessions.get(root) !== session) {
    return; // closed/replaced during wait
  }
  await sendWatcherCommand(session, si, command);
}

// Stop a session: Ctrl-C so test-refresh shuts down cleanly, then dispose the terminal
// (onDidCloseTerminal removes it from the map; the eager delete covers the event lag).
function stopSession(session: LiveSession): void {
  session.terminal.sendText('\u0003'); // Ctrl-C: let test-refresh shut down cleanly
  session.terminal.dispose();
  liveSessions.delete(session.root);
}

async function stopLiveCoding(): Promise<void> {
  if (liveSessions.size === 0) {
    flash('Fireworks: no Live Code session is running.');
    return;
  }
  const session = await chooseSession('stop');
  if (session) {
    stopSession(session);
  }
}

async function restartLiveCoding(): Promise<void> {
  if (liveSessions.size === 0) {
    await startLiveCoding(); // nothing to restart — start a fresh session
    return;
  }
  const session = await chooseSession('restart');
  if (!session) {
    return;
  }
  const { root, command } = session;
  stopSession(session);
  await launchWatcher(root, command); // reuse the session's root + command (no re-prompt)
}

// The running session to act on: the only one, or a quick pick when several are running.
// Caller guarantees liveSessions is non-empty; undefined means the pick was dismissed.
async function chooseSession(verb: 'stop' | 'restart'): Promise<LiveSession | undefined> {
  const all = [...liveSessions.values()];
  if (all.length === 1) {
    return all[0];
  }
  const pick = await vscode.window.showQuickPick(
    all.map((session) => ({
      label: path.basename(session.root),
      description: tildify(path.dirname(session.root)),
      session,
    })),
    { placeHolder: `Select the Live Code session to ${verb}`, matchOnDescription: true },
  );
  return pick?.session;
}

// Preset auto-save delays (seconds) offered by fireworks.setAutoSaveDelay. They tune how
// quickly a save lands after you stop typing — i.e. how snappy the test-refresh/inline loop
// feels. VS Code's files.autoSaveDelay is in milliseconds.
const AUTO_SAVE_DELAYS = [0.25, 0.5, 0.8, 1.5, 3, 5];

// Pick a preset and write it to files.autoSaveDelay (user settings). The delay only applies
// in "afterDelay" mode, so if auto-save is anything else (off/onFocusChange/onWindowChange)
// switch it to afterDelay too.
async function setAutoSaveDelay(): Promise<void> {
  const files = vscode.workspace.getConfiguration('files');
  const currentMs = files.get<number>('autoSaveDelay');
  const pick = await vscode.window.showQuickPick(
    AUTO_SAVE_DELAYS.map((seconds) => {
      const ms = Math.round(seconds * 1000);
      return {
        label: `${seconds}`,
        description: ms === currentMs ? 'seconds (current)' : 'seconds',
        ms,
        seconds,
      };
    }),
    { placeHolder: 'Auto-save delay in seconds (sets files.autoSaveDelay)' },
  );
  if (!pick) {
    return;
  }
  const target = vscode.ConfigurationTarget.Global;
  await files.update('autoSaveDelay', pick.ms, target);
  const wasOn = files.get<string>('autoSave') === 'afterDelay';
  if (!wasOn) {
    await files.update('autoSave', 'afterDelay', target);
  }
  flash(
    `Fireworks: auto-save delay set to ${pick.seconds}s` +
      (wasOn ? '.' : ' (auto-save turned on: after delay).'),
  );
}

interface PreviewItem extends vscode.QuickPickItem {
  value: string | number;
}

// How long the highlighted option must hold still before its value is applied. Debounces
// the live preview so holding the arrow key to cycle through options doesn't write the
// setting on every step — only the option you settle on (a beat after you stop) is applied.
const PREVIEW_DEBOUNCE_MS = 350;

// A QuickPick that previews each option live: a beat after you settle on an item, its value
// is written to the given fireworks.* setting (user scope), so inline results re-render.
// Enter keeps the highlighted value (applied immediately); Esc restores the original.
// Preview is only visible while inline results are running, but the chosen value is saved
// either way. inspect() captures the prior user-scope value so a cancel can clear an
// override that wasn't there before.
async function pickWithLivePreview(
  key: string,
  title: string,
  items: PreviewItem[],
  effective: string | number,
): Promise<void> {
  const originalGlobal = cfg().inspect<string | number>(key)?.globalValue;
  const apply = (v: string | number | undefined) =>
    cfg().update(key, v, vscode.ConfigurationTarget.Global);
  const qp = vscode.window.createQuickPick<PreviewItem>();
  qp.title = title;
  qp.items = items;
  qp.activeItems = items.filter((i) => i.value === effective);
  let committed = false;
  let debounce: ReturnType<typeof setTimeout> | undefined;
  const cancelPending = (): void => {
    if (debounce !== undefined) {
      clearTimeout(debounce);
      debounce = undefined;
    }
  };
  return new Promise<void>((resolve) => {
    qp.onDidChangeActive((active) => {
      const item = active[0];
      if (!item) {
        return;
      }
      cancelPending(); // restart the timer on every move, so a held key never applies mid-cycle
      debounce = setTimeout(() => {
        debounce = undefined;
        void apply(item.value);
      }, PREVIEW_DEBOUNCE_MS);
    });
    qp.onDidAccept(() => {
      committed = true;
      cancelPending(); // flush now — don't wait out the debounce on the chosen value
      const item = qp.activeItems[0];
      if (item) {
        void apply(item.value);
      }
      qp.hide();
    });
    qp.onDidHide(() => {
      cancelPending(); // drop any preview that hadn't landed yet
      void (async () => {
        if (!committed) {
          await apply(originalGlobal); // undefined clears the override we added for preview
        }
        qp.dispose();
        resolve();
      })();
    });
    qp.show();
  });
}

// --- Inline-result appearance pickers (live preview; see pickWithLivePreview) ----------
// Preset scales offered by each picker. Keep values within the setting's declared range in
// package.json so the chosen value isn't flagged as invalid in the Settings UI.
const INLINE_COLORS = ['Purple', 'Blue', 'Cyan', 'Green', 'Neutral'];
const INLINE_BG_OPACITIES = [0, 0.03, 0.04, 0.06, 0.09, 0.13, 0.18, 0.22];
const INLINE_FG_OPACITIES = [1, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65];
const INLINE_GAP = [1, 2, 4, 6, 8, 10, 12, 14, 16];
const INLINE_MAX_LENGTH = [16, 24, 32, 40, 52, 70, 100, 200];
const INLINE_FADE = [0, 40, 80, 120, 200, 300, 450];

// Build the live-preview items for a preset scale, tagging the active value "(current)".
function previewItems(values: (string | number)[], current: string | number): PreviewItem[] {
  return values.map((v) => ({
    label: `${v}`,
    description: v === current ? '(current)' : undefined,
    value: v,
  }));
}

async function setInlineResultsColor(): Promise<void> {
  const current = cfg().get<string>('inlineResults.color', 'Purple');
  await pickWithLivePreview(
    'inlineResults.color',
    'Inline Results Color',
    previewItems(INLINE_COLORS, current),
    current,
  );
}

async function setInlineResultsBackgroundOpacity(): Promise<void> {
  const current = cfg().get<number>('inlineResults.backgroundOpacity', 0.03);
  await pickWithLivePreview(
    'inlineResults.backgroundOpacity',
    'Inline Results Background Opacity',
    previewItems(INLINE_BG_OPACITIES, current),
    current,
  );
}

async function setInlineResultsOpacity(): Promise<void> {
  const current = cfg().get<number>('inlineResults.opacity', 0.75);
  await pickWithLivePreview(
    'inlineResults.opacity',
    'Inline Results Opacity',
    previewItems(INLINE_FG_OPACITIES, current),
    current,
  );
}

async function setInlineResultsGap(): Promise<void> {
  const current = cfg().get<number>('inlineResults.gap', 7);
  await pickWithLivePreview(
    'inlineResults.gap',
    'Inline Results Gap',
    previewItems(INLINE_GAP, current),
    current,
  );
}

async function setInlineResultsMaxLength(): Promise<void> {
  const current = cfg().get<number>('inlineResults.maxLength', 80);
  await pickWithLivePreview(
    'inlineResults.maxLength',
    'Inline Results Max Length',
    previewItems(INLINE_MAX_LENGTH, current),
    current,
  );
}

async function setInlineResultsFadeIn(): Promise<void> {
  const current = cfg().get<number>('inlineResults.fadeInMs', 90);
  await pickWithLivePreview(
    'inlineResults.fadeInMs',
    'Inline Results Fade-In (ms)',
    previewItems(INLINE_FADE, current),
    current,
  );
}

// Inline Results on/off toggle in the bottom status bar. Only shown when the active
// editor is a Clojure file (same languageId gate the commands use); hidden otherwise.
function updateStatusBar(): void {
  if (vscode.window.activeTextEditor?.document.languageId !== 'clojure') {
    statusBar.hide();
    return;
  }
  const on = inlineResultsEnabled();
  statusBar.text = on ? '$(circle-filled) Inline Results' : '$(circle-outline) Inline Results';
  statusBar.tooltip = on
    ? 'Fireworks: inline results on. Click to turn off.'
    : 'Fireworks: inline results off. Click to turn on.';
  statusBar.show();
}
