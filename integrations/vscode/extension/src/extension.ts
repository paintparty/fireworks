import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';
import * as os from 'os';
import * as cljsLib from '../lib/cljs-lib';
import type { EditPlan, RequireEdit, InlineAnalysis, UnwrapAllOpts, FlagOpts } from '../lib/cljs-lib';

let output: vscode.OutputChannel;

export function activate(context: vscode.ExtensionContext): void {
  output = vscode.window.createOutputChannel('Fireworks');
  extContext = context;

  statusBar = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
  statusBar.command = 'fireworks.toggleInlineResults';

  context.subscriptions.push(
    output,
    statusBar,
    registerGuidanceProvider(),
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
    vscode.commands.registerCommand('fireworks.toggleTrace', () =>
      runToggleFlag(cljsLib.toggleTrace, 'toggle-trace'),
    ),
    vscode.commands.registerCommand('fireworks.toggleMinus', () =>
      runToggleFlag(cljsLib.toggleMinus, 'toggle-minus'),
    ),
    vscode.commands.registerCommand('fireworks.togglePlus', () =>
      runToggleFlag(cljsLib.togglePlus, 'toggle-plus'),
    ),
    vscode.commands.registerCommand('fireworks.togglePp', () =>
      runToggleFlag(cljsLib.togglePp, 'toggle-pp'),
    ),
    vscode.commands.registerCommand('fireworks.addRequire', () => runAddRequire()),
    vscode.commands.registerCommand('fireworks.toggleInlineResults', () => toggleInlineResults()),
    vscode.commands.registerCommand('fireworks.clearInlineResults', () => clearInlineResults()),
    vscode.commands.registerCommand('fireworks.startLiveCoding', () => startLiveCoding()),
    vscode.commands.registerCommand('fireworks.stopLiveCoding', () => stopLiveCoding()),
    vscode.commands.registerCommand('fireworks.restartLiveCoding', () => restartLiveCoding()),
    vscode.commands.registerCommand('fireworks.toggleDebugTestMode', () => toggleDebugTestMode()),
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
  context.subscriptions.push({ dispose: () => disposeSweep() });

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

// A pure per-flag toggle: toggleTrace/toggleMinus/togglePlus/togglePp. Adds or removes
// one leading flag (preserving the others), or wraps a bare form, returning an edit plan
// or null (no-op).
type FlagToggle = (opts: FlagOpts) => EditPlan | null;

// Toggle one Fireworks flag (:trace, :-, :+, :pp) on the wrapped form at the cursor (or
// wrap a bare form), preserving any other leading flags. Same selectCurrentForm + apply
// flow as runToggle/runWithOption; `toggle` is the pure per-flag function. No picker —
// each flag has its own command.
async function runToggleFlag(toggle: FlagToggle, label: string): Promise<void> {
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
  let plan: EditPlan | null;
  try {
    plan = toggle({
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

// Ensure <root>/.fireworks/results is git-ignored so the ephemeral result cache never lands in
// `git status`. Only results/ is ignored — the rest of .fireworks/ (config.edn, bb/watch.clj)
// is meant to be committed. Conservative and idempotent: skipped when the manageGitignore setting
// is off; `git check-ignore` decides, so it does nothing when the path is already ignored (by any
// gitignore — this one, a parent, or a global) or when there's no git repo. Only when the path is
// genuinely untracked does it append one commented entry to <root>/.gitignore (created only if
// missing). Never rewrites or reorders existing content. Failures log.
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
    const res = cp.spawnSync('git', ['-C', root, 'check-ignore', '-q', '.fireworks/results']);
    if (res.status !== 1) {
      return;
    }
    const gitignore = path.join(root, '.gitignore');
    const prior = fs.existsSync(gitignore) ? fs.readFileSync(gitignore, 'utf8') : '';
    // Separate from any prior content with a blank line; don't double up newlines.
    const lead = prior === '' || prior.endsWith('\n') ? '' : '\n';
    fs.appendFileSync(gitignore, `${lead}\n# Fireworks inline-result cache\n.fireworks/results/\n`);
    log(`added .fireworks/results/ to ${gitignore}`);
  } catch (e) {
    log(`could not update .gitignore at ${root}: ${String(e)}`);
  }
}

// ============================================================================
// Setup guidance — rendered Markdown opened to the side. No toasts or popups: when Live Code
// can't proceed for a setup reason, we open a small readonly Markdown doc in the built-in preview
// (syntax-highlighted code blocks, clickable links; select-and-copy for the snippets). One reusable
// surface for every guidance message.
// ============================================================================

type GuidanceTopic = 'lein-missing-plugin' | 'lein-user-test-refresh' | 'bb-watch-task';

const GUIDANCE_SCHEME = 'fireworks-guide';

// The title (shown in the preview tab) and Markdown body for a topic. Built from single-quoted
// lines joined with newlines, so the ``` fences need no escaping.
function guidanceDoc(topic: GuidanceTopic): { title: string; markdown: string } {
  switch (topic) {
    case 'lein-missing-plugin':
      return {
        title: 'Fireworks lein-test-refresh setup',
        markdown: [
          '# Fireworks: Missing plugin',
          '',
          'For Leiningen projects, you need this plugin:',
          '',
          '**`[com.jakemccrary/lein-test-refresh "0.26.0"]`**',
          '',
          '<br>',
          '<br>',
          'These coordinates need to exist in at least one of 2 places:',
          '',
          '<br>',
          '',
          '**Locally, in your `project.clj`:**',
          '```clj',
          '(defproject myproject "0.1.0"',
          '  :profiles',
          '  {:my-profile',
          '   {:plugins',
          '    [[com.jakemccrary/lein-test-refresh "0.26.0"]]}})',
          '```',
          '',
          '  ~ or ~',
          '',
          '**Globally, in your `~/.lein/profiles.clj`:**',
          '```clj',
          '{:user',
          ' {:plugins',
          '  [[com.jakemccrary/lein-test-refresh "0.26.0"]]}}',
          '```',
          '<br>',
          '<br>',
          'Helpful links:',
          '',
          'See [lein-test-refresh, Leiningen based projects](https://github.com/jakemcc/test-refresh/blob/master/docs/leiningen.md).',
          '',
        ].join('\n'),
      };
    case 'lein-user-test-refresh':
      return {
        title: 'Fireworks test-refresh config',
        markdown: [
          '# Fireworks: add a `:test-refresh` config',
          '',
          'Your `~/.lein/profiles.clj` `:user` profile has the `lein-test-refresh` plugin but no',
          '`:test-refresh` config.',
          '',
          'You need to manually add this into your `:user` map (the Fireworks extension will not edit global a global config file):',
          '',
          '```clj',
          cljsLib.leinTestRefreshSnippet(),
          '```',
          '',
          'See [lein-test-refresh configuration](https://github.com/jakemcc/lein-test-refresh#configuration).',
          '',
        ].join('\n'),
      };
    case 'bb-watch-task':
      return {
        title: 'Fireworks Babashka watch task',
        markdown: [
          '### ⚠️ Fireworks:',
          '# Add a Babashka watch task',
          '',
          'To use `Fireworks: Live Code` with Babashka, add a task to your `bb.edn` that loads the watcher:',
          '',
          '```clj',
          '{:tasks',
          ' {live {:task (load-file ".fireworks/bb/watch.clj")}}}',
          '```',
          '',
          'Then run Live Code again and pick that task.',
          '',
          ' Fireworks seeds `.fireworks/bb/watch.clj`',
          'for you, and the watcher self-loads the [fswatcher](https://github.com/babashka/fs) pod —',
          'no `:pods` entry is needed in your `bb.edn`.',
          '',
        ].join('\n'),
      };
  }
}

// Register the virtual-doc provider that backs the guidance previews. Content is deterministic per
// topic (carried in the URI query), so no change events are needed.
function registerGuidanceProvider(): vscode.Disposable {
  return vscode.workspace.registerTextDocumentContentProvider(GUIDANCE_SCHEME, {
    provideTextDocumentContent(uri) {
      return guidanceDoc(uri.query as GuidanceTopic).markdown;
    },
  });
}

// Open a topic's guidance as a rendered Markdown preview, to the side of the active editor. The
// `.md` path makes VS Code treat the virtual doc as Markdown; the topic rides in the query. Falls
// back to the raw text document if the Markdown preview command is unavailable.
async function showGuidance(topic: GuidanceTopic): Promise<void> {
  const { title } = guidanceDoc(topic);
  const uri = vscode.Uri.parse(`${GUIDANCE_SCHEME}:${encodeURIComponent(title)}.md?${topic}`);
  try {
    await vscode.commands.executeCommand('markdown.showPreviewToSide', uri);
  } catch {
    const doc = await vscode.workspace.openTextDocument(uri);
    await vscode.window.showTextDocument(doc, vscode.ViewColumn.Beside);
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
    // Trailing 1ch of tinted blank space: widen the content box by one cell and extend
    // the last band over it, so the row's background ends on a visible pad, not flush
    // against the final glyph. Only when there's a tint to show (and something painted).
    if (tint && bands.length > 0) {
      content += nbsp(1);
      bands[bands.length - 1].end += 1;
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
// that project's own config — a deps.edn alias (`clojure -M:<alias>`), a bb.edn task
// (`bb <task>`), or a Leiningen profile carrying lein-test-refresh
// (`lein with-profile +<profile> test-refresh`). For deps/bb the user owns those files and
// Fireworks writes nothing. Leiningen is the one exception: it may patch project.clj's
// :test-refresh (and add the plugin to a profile) — always prompt-then-patch, additive only.
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
  testRefresh?: TestRefreshInfo; // the .test-refresh.edn governing the run (Clojure/deps only)
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

// Every directory in the workspace that holds a deps.edn, bb.edn, or project.clj — across all
// workspace folders and nested subprojects (e.g. a monorepo's repo/foo). The project kind
// (Clojure CLI / Babashka / Leiningen) is resolved per-root at launch by projectKind.
async function findProjectRoots(): Promise<string[]> {
  const uris = await vscode.workspace.findFiles(
    '**/{deps.edn,bb.edn,project.clj}',
    '**/node_modules/**',
  );
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

// --- Recent-projects memory (per workspace) -------------------------------
// The project roots Live Code has launched in, most-recent first, persisted in
// workspaceState so the pickers can float a "Recent" section to the top. The store is
// capped so it can't grow unbounded; the pickers only ever surface the first few.
const RECENT_ROOTS_KEY = 'fireworks.recentLiveRoots';
const RECENT_SECTION_LIMIT = 3; // how many recent roots a picker shows under the "Recent" header
const RECENT_STORE_LIMIT = 20; // how many we remember in total

function getRecentRoots(): string[] {
  return extContext.workspaceState.get<string[]>(RECENT_ROOTS_KEY, []);
}

// Promote `root` to the front of the recency list (de-duped, capped). Called whenever a
// watcher launches, so "recent" tracks the last time Live Code ran on each project.
function recordRecentRoot(root: string): void {
  const next = [root, ...getRecentRoots().filter((r) => r !== root)].slice(0, RECENT_STORE_LIMIT);
  void extContext.workspaceState.update(RECENT_ROOTS_KEY, next);
}

// Lay quick-pick entries out with a "Recent" header: candidates whose root launched recently
// (freshest first, capped at RECENT_SECTION_LIMIT) move into a top section; the rest keep
// their given order below a divider. `candidates` pairs each item with its root so recency
// can be keyed. Separators are never returned as the user's pick.
function withRecentSection<T extends vscode.QuickPickItem>(
  candidates: { root: string; item: T }[],
): (T | vscode.QuickPickItem)[] {
  const rank = new Map(getRecentRoots().map((r, i) => [r, i] as const));
  const recent = candidates
    .filter((c) => rank.has(c.root))
    .sort((a, b) => rank.get(a.root)! - rank.get(b.root)!)
    .slice(0, RECENT_SECTION_LIMIT);
  if (recent.length === 0) {
    return candidates.map((c) => c.item);
  }
  const recentSet = new Set(recent.map((c) => c.root));
  const rest = candidates.filter((c) => !recentSet.has(c.root));
  const items: (T | vscode.QuickPickItem)[] = [
    { label: 'Recent', kind: vscode.QuickPickItemKind.Separator },
    ...recent.map((c) => c.item),
  ];
  if (rest.length > 0) {
    items.push({ label: '', kind: vscode.QuickPickItemKind.Separator }); // divider
    items.push(...rest.map((c) => c.item));
  }
  return items;
}

type RootPickItem = vscode.QuickPickItem & { root: string };

// Resolve which project root to act on: the only one, or a pick when the workspace has
// several. The pick mirrors VS Code's own "new terminal" cwd picker — folder name as the
// label, the home-abbreviated parent path as the muted description — with recently-launched
// projects floated under a "Recent" header. undefined if none, or the pick was dismissed.
async function pickProjectRoot(): Promise<string | undefined> {
  const roots = await findProjectRoots();
  if (roots.length === 0) {
    vscode.window.showErrorMessage(
      'Fireworks: no deps.edn, bb.edn, or project.clj found in this workspace.',
    );
    return undefined;
  }
  if (roots.length === 1) {
    return roots[0];
  }
  const items = withRecentSection<RootPickItem>(
    roots.map((root) => ({
      root,
      item: { label: path.basename(root), description: tildify(path.dirname(root)), root },
    })),
  );
  const pick = await vscode.window.showQuickPick(items, {
    placeHolder: 'Select the project for Fireworks live coding',
    matchOnDescription: true, // typing filters on the path too, like the native picker
  });
  return (pick as RootPickItem | undefined)?.root;
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

function preflightLein(): boolean {
  if (onPath('lein')) {
    return true;
  }
  vscode.window.showErrorMessage(
    'Fireworks: the `lein` (Leiningen) command is not on your PATH. Install Leiningen, then try again.',
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

// The watcher command for a Leiningen project. With a profile, `with-profile +<profile>`
// activates the profile carrying lein-test-refresh (the `+` merges onto defaults rather than
// replacing). With no profile (the ~/.lein/profiles.clj :user case, which auto-merges), run
// plain `lein test-refresh`.
function leinCommand(profile?: string): string {
  return profile ? `lein with-profile +${profile} test-refresh` : 'lein test-refresh';
}

type Runtime = 'deps' | 'bb' | 'lein';

// Which runtime to launch for `root`: deps.edn -> Clojure CLI alias; bb.edn -> Babashka
// task; project.clj -> Leiningen profile. When a project has more than one of these files,
// ask. undefined if none exists (shouldn't happen via pickProjectRoot) or the disambiguation
// pick was dismissed.
async function projectKind(root: string): Promise<Runtime | undefined> {
  const kinds: { label: string; runtime: Runtime }[] = [];
  if (fs.existsSync(path.join(root, 'deps.edn'))) {
    kinds.push({ label: 'Clojure (deps.edn alias)', runtime: 'deps' });
  }
  // bb is offered only when wired for it (a task load-files .fireworks/bb/watch.clj), so a
  // bb.edn kept for build scripts alongside a deps.edn / project.clj isn't mistaken for a watcher.
  if (fs.existsSync(path.join(root, 'bb.edn')) && bbHasWatchTask(root)) {
    kinds.push({ label: 'Babashka (.fireworks/bb/watch.clj)', runtime: 'bb' });
  }
  if (fs.existsSync(path.join(root, 'project.clj'))) {
    kinds.push({ label: 'Leiningen (project.clj profile)', runtime: 'lein' });
  }
  if (kinds.length === 0) {
    // Reached only when the sole build file is an unwired bb.edn (deps/lein always qualify).
    if (fs.existsSync(path.join(root, 'bb.edn'))) {
      await showGuidance('bb-watch-task');
    }
    return undefined;
  }
  if (kinds.length === 1) {
    return kinds[0].runtime;
  }
  const pick = await vscode.window.showQuickPick(kinds, {
    placeHolder: 'This project has multiple build files — which runtime?',
  });
  return pick?.runtime;
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

// Whether `root`'s bb.edn is wired as a Fireworks watcher: a task whose body load-files
// .fireworks/bb/watch.clj. This is the opt-in signal that gates whether bb is offered as a
// runtime — a bb.edn kept only for build scripts returns false, so it isn't mistaken for a
// watcher alongside a deps.edn / project.clj. Missing or unparseable bb.edn -> false.
function bbHasWatchTask(root: string): boolean {
  const text = readFileOrNull(path.join(root, 'bb.edn'));
  if (text === null) {
    return false;
  }
  const { tasks } = cljsLib.bbWatchTasks(text);
  return !!tasks && tasks.length > 0;
}

// Seed .fireworks/bb/watch.clj from the template when absent (the user's bb.edn task load-files
// it). Never overwrites an existing file — it may be user-edited and is meant to be committed.
// Returns false only on a write failure (a message is shown); true when the file is in place.
function ensureBbWatchFile(root: string): boolean {
  const file = path.join(root, '.fireworks', 'bb', 'watch.clj');
  if (fs.existsSync(file)) {
    return true;
  }
  try {
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, cljsLib.bbWatchTemplate(), 'utf8');
    log(`bb watcher: seeded ${tildify(file)}`);
    return true;
  } catch (e) {
    log(`bb watcher: could not write ${tildify(file)}: ${String(e)}`);
    vscode.window.showErrorMessage(`Fireworks: could not create ${tildify(file)}.`);
    return false;
  }
}

// Resolve the Babashka launch plan for `root`: the watcher task(s) wired to .fireworks/bb/watch.clj
// (run directly if one, pick if several), seeding the watcher file when missing. bb sessions carry
// no TestRefreshInfo (config is .fireworks/config.edn, read by watch.clj itself).
async function resolveBbCommand(root: string): Promise<WatcherPlan | undefined> {
  if (!preflightBb()) {
    return undefined;
  }
  const text = readFileOrNull(path.join(root, 'bb.edn'));
  if (text === null) {
    vscode.window.showErrorMessage(`Fireworks: could not read bb.edn in ${tildify(root)}.`);
    return undefined;
  }
  const { tasks, error } = cljsLib.bbWatchTasks(text);
  if (error || !tasks) {
    vscode.window.showErrorMessage(`Fireworks: could not parse bb.edn in ${tildify(root)}.`);
    return undefined;
  }
  if (tasks.length === 0) {
    // projectKind gates bb on this, so this is a safety net rather than the usual path.
    await showGuidance('bb-watch-task');
    return undefined;
  }
  const task =
    tasks.length === 1
      ? tasks[0]
      : await vscode.window.showQuickPick(tasks, {
          placeHolder: 'Select the bb watch task to run',
        });
  if (!task) {
    return undefined;
  }
  if (!ensureBbWatchFile(root)) {
    return undefined;
  }
  return { command: taskCommand(task) };
}

// Pick a Leiningen profile from a given list (mirrors pickAlias). Auto-returns a single
// candidate; otherwise shows each as a keyword (`:dev`) but keeps the bare name for the
// `lein with-profile +<profile>` command. undefined when the pick was dismissed.
async function pickProfile(profiles: string[], placeHolder: string): Promise<string | undefined> {
  if (profiles.length === 1) {
    return profiles[0];
  }
  const pick = await vscode.window.showQuickPick(
    profiles.map((profile) => ({ label: `:${profile}`, profile })),
    { placeHolder },
  );
  return pick?.profile;
}

// Which .test-refresh.edn governs a Clojure watcher: 'local' (project root), 'global'
// (~/.test-refresh.edn), 'created' (we seeded one in the project root), or 'error' (the
// seed write failed — test-refresh falls back to its own defaults).
type TestRefreshSource = 'local' | 'global' | 'created' | 'error';

// The .test-refresh.edn governing a running Clojure session: which file and how it was
// resolved. Tracked on the session so the debug/test toggle knows whether it may rewrite
// the file ('local'/'created') or must defer to the user ('global').
interface TestRefreshInfo {
  path: string; // the governing file (a project-root path, or the global ~/.test-refresh.edn)
  source: TestRefreshSource;
}

// Ensure a .test-refresh.edn is in effect for a Clojure (deps) project before its watcher
// launches. test-refresh reads the project-root .test-refresh.edn, falling back to the
// global ~/.test-refresh.edn. We never overwrite an existing config (local or global);
// only when neither exists do we seed one in the project root from the Fireworks template.
// Returns which file governs the run and how it was resolved.
function ensureTestRefreshConfig(root: string): TestRefreshInfo {
  const local = path.join(root, '.test-refresh.edn');
  if (fs.existsSync(local)) {
    log(`.test-refresh.edn: using project-local ${tildify(local)}`);
    return { path: local, source: 'local' };
  }
  const global = path.join(os.homedir(), '.test-refresh.edn');
  if (fs.existsSync(global)) {
    log(`.test-refresh.edn: using global ${tildify(global)}`);
    return { path: global, source: 'global' };
  }
  try {
    fs.writeFileSync(local, cljsLib.testRefreshTemplate(), 'utf8');
    log(`.test-refresh.edn: none found locally or globally — created ${tildify(local)}`);
    return { path: local, source: 'created' };
  } catch (e) {
    log(`.test-refresh.edn: could not create ${tildify(local)}: ${String(e)}`);
    vscode.window.showWarningMessage(
      `Fireworks: could not create .test-refresh.edn in ${tildify(root)}. test-refresh will run with its defaults.`,
    );
    return { path: local, source: 'error' };
  }
}

// What a launch needs: the verbatim watcher command, plus (Clojure/deps only) the
// .test-refresh.edn that governs the run so the session can track it.
interface WatcherPlan {
  command: string;
  testRefresh?: TestRefreshInfo;
}

// Resolve the launch plan for `root`: detect the runtime, run its PATH preflight, then pick
// the alias (deps) or task (bb). undefined aborts the launch (a message was already shown,
// or a pick/preflight failed).
async function resolveWatcherCommand(root: string): Promise<WatcherPlan | undefined> {
  const kind = await projectKind(root);
  if (!kind) {
    return undefined;
  }
  if (kind === 'bb') {
    return resolveBbCommand(root);
  }
  if (kind === 'lein') {
    return resolveLeinCommand(root);
  }
  if (!preflightClojure()) {
    return undefined;
  }
  const alias = await pickAlias(root);
  if (!alias) {
    return undefined;
  }
  // Clojure runtime only: make sure a .test-refresh.edn governs the run (seed one from the
  // template if the project has neither a local nor a global config). Done after the alias
  // pick so a dismissed pick writes nothing.
  const testRefresh = ensureTestRefreshConfig(root);
  return { command: aliasCommand(alias), testRefresh };
}

// --- Leiningen launch resolution ------------------------------------------
// Unlike deps/bb, the Leiningen flow may edit the user's project.clj — but only after a
// confirm modal, and only additively (rewrite-clj preserves formatting/comments). Eligibility:
// a :profiles entry whose :plugins carries exactly [com.jakemccrary/lein-test-refresh "0.26.0"].

// Confirm a project.clj edit. Modal so it can't be missed; returns true only on the explicit
// confirm button (Escape/dismiss = false, launch aborts).
async function confirmProjectCljWrite(detail: string): Promise<boolean> {
  const confirm = 'Edit project.clj';
  const pick = await vscode.window.showInformationMessage(
    'Fireworks: Live Code wants to edit project.clj',
    { modal: true, detail },
    confirm,
  );
  return pick === confirm;
}

function writeProjectClj(projectCljPath: string, text: string): boolean {
  try {
    fs.writeFileSync(projectCljPath, text, 'utf8');
    log(`project.clj: patched ${tildify(projectCljPath)}`);
    return true;
  } catch (e) {
    log(`project.clj: could not write ${tildify(projectCljPath)}: ${String(e)}`);
    vscode.window.showErrorMessage(`Fireworks: could not write ${tildify(projectCljPath)}.`);
    return false;
  }
}

// Ensure project.clj's top-level :test-refresh map against the baseline (prompt-then-patch).
// Returns true to proceed with the launch, false to abort (parse error, or the user declined).
async function ensureTestRefreshLein(projectCljPath: string, text: string): Promise<boolean> {
  const r = cljsLib.leinEnsureTestRefresh(text);
  if (r.error || r.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not parse ${tildify(projectCljPath)}.`);
    return false;
  }
  if (!r.changed) {
    return true; // baseline already satisfied
  }
  const keys = (r.addedKeys ?? []).join(', ');
  if (!(await confirmProjectCljWrite(`Add a :test-refresh config (${keys}) to project.clj.`))) {
    return false;
  }
  return writeProjectClj(projectCljPath, r.text);
}

// Add the plugin coordinate to the chosen profile AND ensure :test-refresh, in one combined
// confirm + one write. Returns true to proceed, false to abort.
async function addPluginAndTestRefresh(
  projectCljPath: string,
  text: string,
  profile: string,
): Promise<boolean> {
  const added = cljsLib.leinAddPlugin(text, profile);
  if (added.error || added.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(projectCljPath)}.`);
    return false;
  }
  const ensured = cljsLib.leinEnsureTestRefresh(added.text);
  if (ensured.error || ensured.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(projectCljPath)}.`);
    return false;
  }
  if (!added.changed && !ensured.changed) {
    return true; // nothing to do (shouldn't happen for an ineligible profile, but safe)
  }
  const parts: string[] = [];
  if (added.changed) {
    parts.push(`add [com.jakemccrary/lein-test-refresh "0.26.0"] to the :${profile} profile`);
  }
  if (ensured.changed) {
    parts.push(`add a :test-refresh config (${(ensured.addedKeys ?? []).join(', ')})`);
  }
  if (!(await confirmProjectCljWrite(`Fireworks will ${parts.join(' and ')} in project.clj.`))) {
    return false;
  }
  return writeProjectClj(projectCljPath, ensured.text);
}

// Fall back to ~/.lein/profiles.clj when project.clj defines no profiles. Read-only: the
// extension never writes global config. Returns a plan (plain `lein test-refresh`, since the
// :user profile auto-merges) only when :user carries the plugin AND a :test-refresh key;
// otherwise opens the matching setup guide to the side.
async function resolveLeinUserProfile(): Promise<WatcherPlan | undefined> {
  const profilesPath = path.join(os.homedir(), '.lein', 'profiles.clj');
  const text = readFileOrNull(profilesPath);
  const status = text === null ? undefined : cljsLib.leinUserProfileStatus(text);
  if (status && status.error) {
    vscode.window.showErrorMessage(`Fireworks: could not parse ${tildify(profilesPath)}.`);
    return undefined;
  }
  if (!status || !status.hasPlugin) {
    await showGuidance('lein-missing-plugin');
    return undefined;
  }
  if (!status.hasTestRefresh) {
    await showGuidance('lein-user-test-refresh');
    return undefined;
  }
  return { command: leinCommand() };
}

// Resolve the Leiningen launch plan for `root`. Lein sessions carry no TestRefreshInfo (the
// config lives in project.clj, not a .test-refresh.edn) — like bb, the debug/test toggle skips them.
async function resolveLeinCommand(root: string): Promise<WatcherPlan | undefined> {
  if (!preflightLein()) {
    return undefined;
  }
  const projectCljPath = path.join(root, 'project.clj');
  const text = readFileOrNull(projectCljPath);
  if (text === null) {
    vscode.window.showErrorMessage(`Fireworks: could not read project.clj in ${tildify(root)}.`);
    return undefined;
  }
  const { all, eligible, error } = cljsLib.leinProfiles(text);
  if (error || !all || !eligible) {
    vscode.window.showErrorMessage(`Fireworks: could not parse project.clj in ${tildify(root)}.`);
    return undefined;
  }
  // An eligible profile already carries the plugin → pick it, ensure :test-refresh, launch.
  if (eligible.length > 0) {
    const profile = await pickProfile(
      eligible,
      'Select the profile to run (carries lein-test-refresh)',
    );
    if (!profile) {
      return undefined;
    }
    if (!(await ensureTestRefreshLein(projectCljPath, text))) {
      return undefined;
    }
    return { command: leinCommand(profile) };
  }
  // No eligible profile, but profiles exist → offer to add the plugin to one.
  if (all.length > 0) {
    const profile = await pickProfile(all, 'Add lein-test-refresh to which profile?');
    if (!profile) {
      return undefined;
    }
    if (!(await addPluginAndTestRefresh(projectCljPath, text, profile))) {
      return undefined;
    }
    return { command: leinCommand(profile) };
  }
  // No profiles at all → fall through to ~/.lein/profiles.clj.
  return resolveLeinUserProfile();
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
  const plan = await resolveWatcherCommand(root);
  if (!plan) {
    return;
  }
  await launchWatcher(root, plan.command, plan.testRefresh);
}

// A session already exists for the picked root. If its process has exited (the terminal
// is lingering), restart it in place. Otherwise it's genuinely running: reveal it and
// show a modal notice — starting a second session for the same project is a no-op.
async function reuseOrNotify(session: LiveSession): Promise<void> {
  const exited = session.terminal.shellIntegration && session.execution === undefined;
  if (exited) {
    prepareResultsDir(session.root);
    clearResultsForRoot(session.root);
    recordRecentRoot(session.root); // re-running here counts as recent, too
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

// ============================================================================
// Live Code startup sweep: a brief rainbow highlight that travels across the active
// editor's viewport when a session starts or restarts — a "refresh" cue, since launching
// shows/focuses the terminal and gives the editor no signal of its own. A short fading
// trail sweeps in the configured direction (each cell lights at the wave front, then fades
// back to normal), cycling the color through 9 oklch medium tones. Paced from the visible
// row (vertical) or column (horizontal) count so the whole sweep lasts ~1s. Active editor
// only; gated by the hidden fireworks.liveCoding.startupAnimation + .startupAnimationDirection settings.
// ============================================================================

const SWEEP_TOTAL_MS = 1250; // target duration of the whole sweep
const SWEEP_MIN_STEP_MS = 12; // floor on the per-row step so a dense viewport doesn't busy-loop
const SWEEP_FADE_SPAN = 7; // rows in the fading trail = opacity levels per color
const SWEEP_MAX_ALPHA_BACKGROUND = 0.375; // background alpha at the bright front (spec's "10%" = 0.1)
const SWEEP_MAX_ALPHA_FOREGROUND = 1.0; // foreground (text) alpha at the front; used iff SWEEP_TINT_FOREGROUND
const SWEEP_MAX_COLS = 120; // cap on a horizontal sweep's column count (bounds the frame budget)
const SWEEP_L = 0.72; // oklch lightness for the background tint — a medium tone
const SWEEP_FG_L = 0.85; // brighter lightness for the foreground tint so it pops on dark backgrounds
const SWEEP_C = 0.16; // oklch chroma
// Hues for red, orange, yellow, lime, green, cyan, blue, purple, magenta. The color cycles
// through these along the sweep (row r -> SWEEP_HUES[r % 9] vertical; column c likewise).
const SWEEP_HUES = [25, 55, 95, 130, 150, 200, 260, 305, 340];
// Whether the moving tint colors the cell background, the text foreground, or both. These are
// application-logic switches (no hidden setting yet) — the defaults reproduce the original
// background-only sweep. Read once, when the decoration types are first built.
const SWEEP_TINT_BACKGROUND = true;
const SWEEP_TINT_FOREGROUND = true;
// Limit the background tint to cells holding non-blank text (skip whitespace and the empty area
// past end-of-line). When off, a vertical sweep tints whole lines and a horizontal sweep tints
// every column up to each line's length. Application-logic switch (no hidden setting).
const SWEEP_BACKGROUND_TEXT_ONLY = true;
// Skip line-comment lines entirely — no animation on any line whose first non-blank character is a
// `;`. Precomputed once per run, so it costs one test per visible line (not per frame) and trims
// the ranges painted. Application-logic switch (no hidden setting).
const SWEEP_SKIP_LINE_COMMENTS = true;

// [hue][level-1] decoration types: 9 colors x SWEEP_FADE_SPAN opacity levels, in two flavors —
// whole-line tints (one band per row) and single-cell tints (one character cell). The whole-line
// set backs a plain vertical sweep; the cell set backs horizontal sweeps and any sweep limited to
// text cells (SWEEP_BACKGROUND_TEXT_ONLY). isWholeLine is baked into the type, so each flavor is
// cached separately. Level k (1..span) -> alpha (SWEEP_MAX_ALPHA_BACKGROUND/_FOREGROUND) * k/span,
// so the wave front is brightest and the trail dims behind it. Built once, reused, disposed on deactivation.
let sweepTypesWholeLine: vscode.TextEditorDecorationType[][] | undefined;
let sweepTypesCell: vscode.TextEditorDecorationType[][] | undefined;
let sweepTimer: ReturnType<typeof setInterval> | undefined;
let sweepEditor: vscode.TextEditor | undefined; // the editor the running sweep paints
let sweepActive: vscode.TextEditorDecorationType[][] | undefined; // the type set the running sweep paints

// Hidden (JSON-only) settings, mirroring fireworks.liveCoding.manageGitignore: read fresh, not
// declared in package.json so they stay out of the Settings UI.
function startupAnimEnabled(): boolean {
  return vscode.workspace
    .getConfiguration('fireworks')
    .get<boolean>('liveCoding.startupAnimation', true);
}

type SweepDirection = 'top-to-bottom' | 'bottom-to-top' | 'left-to-right' | 'right-to-left';

function startupAnimDirection(): SweepDirection {
  const v = vscode.workspace
    .getConfiguration('fireworks')
    // .get<string>('liveCoding.startupAnimationDirection', 'bottom-to-top');
    .get<string>('liveCoding.startupAnimationDirection', 'top-to-bottom');
  switch (v) {
    case 'top-to-bottom':
    case 'bottom-to-top':
    case 'right-to-left':
      return v;
    default:
      return 'left-to-right';
  }
}

function ensureSweepTypes(wholeLine: boolean): vscode.TextEditorDecorationType[][] {
  const cached = wholeLine ? sweepTypesWholeLine : sweepTypesCell;
  if (cached) {
    return cached;
  }
  const built = SWEEP_HUES.map((hue) =>
    Array.from({ length: SWEEP_FADE_SPAN }, (_, i) => {
      const level = i + 1; // 1..SWEEP_FADE_SPAN; the wave front is the top level
      const frac = level / SWEEP_FADE_SPAN; // the background fades down the trail (1/span .. 1)
      const bg = `oklch(${SWEEP_L} ${SWEEP_C} ${hue} / ${+(SWEEP_MAX_ALPHA_BACKGROUND * frac).toFixed(3)})`;
      // Foreground lights only the front cell — no trailing fade — at a brighter lightness, so the
      // active row pops on dark backgrounds and trailing rows drop straight back to their own color.
      const onFront = level === SWEEP_FADE_SPAN;
      const fg = `oklch(${SWEEP_FG_L} ${SWEEP_C} ${hue} / ${SWEEP_MAX_ALPHA_FOREGROUND})`;
      return vscode.window.createTextEditorDecorationType({
        isWholeLine: wholeLine,
        backgroundColor: SWEEP_TINT_BACKGROUND ? bg : undefined,
        color: SWEEP_TINT_FOREGROUND && onFront ? fg : undefined,
      });
    }),
  );
  if (wholeLine) {
    sweepTypesWholeLine = built;
  } else {
    sweepTypesCell = built;
  }
  return built;
}

// Clear every decoration of the set the running sweep is painting, from its editor.
function clearSweepDecorations(): void {
  if (!sweepActive || !sweepEditor) {
    return;
  }
  for (const row of sweepActive) {
    for (const t of row) {
      sweepEditor.setDecorations(t, []);
    }
  }
}

// Stop any in-flight sweep and wipe its decorations (so a restart mid-sweep leaves no
// stray bands). Safe to call when nothing is running.
function stopSweep(): void {
  if (sweepTimer !== undefined) {
    clearInterval(sweepTimer);
    sweepTimer = undefined;
  }
  clearSweepDecorations();
  sweepEditor = undefined;
  sweepActive = undefined;
}

// Tear-down for deactivation: stop the sweep and dispose both type flavors.
function disposeSweep(): void {
  stopSweep();
  for (const set of [sweepTypesWholeLine, sweepTypesCell]) {
    if (set) {
      for (const row of set) {
        for (const t of row) {
          t.dispose();
        }
      }
    }
  }
  sweepTypesWholeLine = undefined;
  sweepTypesCell = undefined;
}

// Drive a sweep: each tick refills the per-(color, level) range buckets via `paintFrame` and
// pushes them to the editor, for `totalSteps` ticks `step` ms apart, then clears. Ranges key off
// document positions, so the sweep stays put even though launching focuses the terminal.
//
// Perf: there are 9*SWEEP_FADE_SPAN decoration types but only a handful hold ranges on any frame
// (the trail). So (a) the buckets are allocated once and cleared in place — no per-frame garbage —
// and (b) setDecorations is called only for a type whose ranges changed this frame (non-empty now,
// or non-empty last frame and needing a clear), tracked in `dirty`. That turns ~9*SWEEP_FADE_SPAN
// renderer round-trips per frame into ~2*trail, the single biggest cost in the animation.
function runSweep(
  editor: vscode.TextEditor,
  types: vscode.TextEditorDecorationType[][],
  totalSteps: number,
  step: number,
  paintFrame: (s: number, buckets: vscode.Range[][][]) => void,
): void {
  stopSweep(); // cancel any prior sweep before starting this one
  sweepEditor = editor;
  sweepActive = types;
  const buckets = types.map((row) => row.map(() => [] as vscode.Range[]));
  const dirty = types.map((row) => row.map(() => false)); // which types currently hold ranges
  let s = 0;
  sweepTimer = setInterval(() => {
    if (!vscode.window.visibleTextEditors.includes(editor)) {
      stopSweep(); // editor closed or scrolled out of view — stop wasting frames
      return;
    }
    for (const row of buckets) {
      for (const b of row) {
        b.length = 0; // reuse the arrays; no per-frame allocation
      }
    }
    paintFrame(s, buckets);
    for (let c = 0; c < types.length; c++) {
      for (let k = 0; k < types[c].length; k++) {
        const hasRanges = buckets[c][k].length > 0;
        if (hasRanges || dirty[c][k]) {
          editor.setDecorations(types[c][k], buckets[c][k]); // only when this type changed
          dirty[c][k] = hasRanges;
        }
      }
    }
    s++;
    if (s >= totalSteps) {
      stopSweep();
    }
  }, step);
}

// The visible lines' text, indexed from the first visible line. Feeds the text-only background
// (SWEEP_BACKGROUND_TEXT_ONLY) and the horizontal sweep's per-column reach.
function visibleLineTexts(editor: vscode.TextEditor, startLine: number, endLine: number): string[] {
  const texts: string[] = [];
  for (let line = startLine; line <= endLine; line++) {
    texts.push(editor.document.lineAt(line).text);
  }
  return texts;
}

// A line whose first non-blank character is a `;` (a Clojure line comment).
function isLineComment(text: string): boolean {
  return /^\s*;/.test(text);
}

// Push a range for each run of non-blank characters on the line, so only the code is tinted —
// whitespace and the empty area past end-of-line stay clear.
function pushNonBlankRuns(bucket: vscode.Range[], line: number, text: string): void {
  let runStart = -1;
  for (let col = 0; col <= text.length; col++) {
    const blank = col === text.length || text[col].trim() === '';
    if (!blank && runStart < 0) {
      runStart = col; // a non-blank run begins
    } else if (blank && runStart >= 0) {
      bucket.push(new vscode.Range(line, runStart, line, col)); // run ends just before this blank
      runStart = -1;
    }
  }
}

// Play the startup sweep on `editor` in the configured direction. A fading rainbow band
// travels across the viewport over ~SWEEP_TOTAL_MS, then clears.
function playStartupSweep(editor: vscode.TextEditor): void {
  switch (startupAnimDirection()) {
    case 'top-to-bottom':
      playVerticalSweep(editor, false);
      break;
    case 'bottom-to-top':
      playVerticalSweep(editor, true);
      break;
    case 'right-to-left':
      playHorizontalSweep(editor, true);
      break;
    default: // 'left-to-right'
      playHorizontalSweep(editor, false);
      break;
  }
}

// Vertical sweep. reversed=false runs top -> bottom, true runs bottom -> top. Each visible row
// lights as the front reaches it (color stays anchored per row: SWEEP_HUES[r % 9]) and fades over
// the next SWEEP_FADE_SPAN rows. Paced from the row count. With SWEEP_BACKGROUND_TEXT_ONLY the row
// tints only its non-blank text runs (cell types); otherwise the whole line (whole-line types).
function playVerticalSweep(editor: vscode.TextEditor, reversed: boolean): void {
  const ranges = editor.visibleRanges;
  if (ranges.length === 0) {
    return;
  }
  const startLine = ranges[0].start.line;
  const endLine = ranges[ranges.length - 1].end.line;
  const rows = endLine - startLine + 1;
  if (rows <= 0) {
    return;
  }
  const textOnly = SWEEP_BACKGROUND_TEXT_ONLY;
  const skipComments = SWEEP_SKIP_LINE_COMMENTS;
  const lineTexts = textOnly || skipComments ? visibleLineTexts(editor, startLine, endLine) : null;
  const commentLine = skipComments && lineTexts ? lineTexts.map(isLineComment) : null;
  const step = Math.max(SWEEP_TOTAL_MS / rows, SWEEP_MIN_STEP_MS); // ms the front spends per row
  runSweep(editor, ensureSweepTypes(!textOnly), rows + SWEEP_FADE_SPAN, step, (s, buckets) => {
    for (let r = 0; r < rows; r++) {
      if (commentLine && commentLine[r]) {
        continue; // leave line-comment rows untouched
      }
      const pos = reversed ? rows - 1 - r : r; // this row's place in the sweep order
      const age = s - pos; // steps since the front passed this row
      if (age < 0 || age >= SWEEP_FADE_SPAN) {
        continue; // not yet reached, or fully faded
      }
      const level = SWEEP_FADE_SPAN - age; // front (age 0) is the top level, trail dims behind it
      const line = startLine + r;
      const bucket = buckets[r % SWEEP_HUES.length][level - 1];
      if (textOnly) {
        pushNonBlankRuns(bucket, line, lineTexts![r]);
      } else {
        bucket.push(new vscode.Range(line, 0, line, 0)); // whole line
      }
    }
  });
}

// Horizontal sweep. reversed=false runs left -> right, true runs right -> left. A column band
// sweeps across the visible rows; each column lights as the front reaches it (color anchored per
// column: SWEEP_HUES[c % 9]) and fades over the next SWEEP_FADE_SPAN columns. A cell paints only
// where that line has a character (and, with SWEEP_BACKGROUND_TEXT_ONLY, only a non-blank one), so
// the band tracks the code. Paced from the longest visible line, capped at SWEEP_MAX_COLS.
function playHorizontalSweep(editor: vscode.TextEditor, reversed: boolean): void {
  const ranges = editor.visibleRanges;
  if (ranges.length === 0) {
    return;
  }
  const startLine = ranges[0].start.line;
  const endLine = ranges[ranges.length - 1].end.line;
  const lineTexts = visibleLineTexts(editor, startLine, endLine);
  const cols = Math.min(Math.max(...lineTexts.map((t) => t.length)), SWEEP_MAX_COLS);
  if (cols <= 0) {
    return; // nothing but blank lines in view
  }
  const textOnly = SWEEP_BACKGROUND_TEXT_ONLY;
  const commentLine = SWEEP_SKIP_LINE_COMMENTS ? lineTexts.map(isLineComment) : null;
  const step = Math.max(SWEEP_TOTAL_MS / cols, SWEEP_MIN_STEP_MS); // ms the front spends per column
  runSweep(editor, ensureSweepTypes(false), cols + SWEEP_FADE_SPAN, step, (s, buckets) => {
    for (let col = 0; col < cols; col++) {
      const pos = reversed ? cols - 1 - col : col; // this column's place in the sweep order
      const age = s - pos; // steps since the front passed this column
      if (age < 0 || age >= SWEEP_FADE_SPAN) {
        continue; // not yet reached, or fully faded
      }
      const level = SWEEP_FADE_SPAN - age; // front (age 0) is the top level, trail dims behind it
      const bucket = buckets[col % SWEEP_HUES.length][level - 1];
      for (let i = 0; i < lineTexts.length; i++) {
        if (commentLine && commentLine[i]) {
          continue; // leave line-comment rows untouched
        }
        const text = lineTexts[i];
        if (col < text.length && (!textOnly || text[col].trim() !== '')) {
          const line = startLine + i;
          bucket.push(new vscode.Range(line, col, line, col + 1));
        }
      }
    }
  });
}

// Run the watcher for a root + resolved command, registering it as a session so
// Stop/Restart can act on it. Concurrent sessions for other roots are left untouched.
async function launchWatcher(
  root: string,
  command: string,
  testRefresh?: TestRefreshInfo,
): Promise<void> {
  const animEditor = vscode.window.activeTextEditor; // capture before terminal.show() steals focus
  prepareResultsDir(root); // on-disk fresh slate for this root
  clearResultsForRoot(root); // in-memory fresh slate for this root only (not other projects')
  recordRecentRoot(root); // float this project in the pickers' "Recent" section
  log(`live coding -> ${command} (cwd ${root})`);
  const terminal = vscode.window.createTerminal({ name: terminalName(root), cwd: root });
  const session: LiveSession = { root, command, terminal, execution: undefined, testRefresh };
  liveSessions.set(root, session);
  terminal.show();
  if (animEditor && startupAnimEnabled()) {
    playStartupSweep(animEditor); // fire-and-forget; the shell-integration wait runs in parallel
  }

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
  const { root, command, testRefresh } = session;
  stopSession(session);
  await launchWatcher(root, command, testRefresh); // reuse root + command + config (no re-prompt)
}

// Toggle the picked Clojure session between debug (tap) and test mode by rewriting its
// .test-refresh.edn in place: flip :debug and sync :banner to the file's own :debug-banner /
// :test-banner. A 'global' session can't be rewritten from here (the file is the user's
// shared config), so surface a modal pointing them at it; bb sessions carry no test-refresh
// config at all. The change takes effect on the watcher's next config read (restart if needed).
async function toggleDebugTestMode(): Promise<void> {
  if (liveSessions.size === 0) {
    flash('Fireworks: no Live Code session is running.');
    return;
  }
  const session = await chooseSession('toggle debug/test mode for');
  if (!session) {
    return;
  }
  const tr = session.testRefresh;
  if (!tr) {
    flash('Fireworks: debug/test mode applies to Clojure (deps) live coding sessions only.');
    return;
  }
  if (tr.source === 'global') {
    await vscode.window.showInformationMessage(
      'Fireworks: these settings must be changed in your global ~/.test-refresh.edn.',
      { modal: true, detail: 'This session runs off the global config. Edit it there, then restart Live Code.' },
    );
    return;
  }
  const text = readFileOrNull(tr.path);
  if (text === null) {
    vscode.window.showErrorMessage(`Fireworks: could not read ${tildify(tr.path)}.`);
    return;
  }
  const { text: out, mode, error } = cljsLib.toggleMode(text);
  if (error || !out || !mode) {
    vscode.window.showErrorMessage(`Fireworks: could not parse ${tildify(tr.path)} to toggle mode.`);
    return;
  }
  try {
    fs.writeFileSync(tr.path, out, 'utf8');
  } catch (e) {
    log(`could not write ${tildify(tr.path)}: ${String(e)}`);
    vscode.window.showErrorMessage(`Fireworks: could not write ${tildify(tr.path)}.`);
    return;
  }
  const label = mode === 'tap' ? 'debug' : 'test';
  log(`live coding mode -> ${label} (${tildify(tr.path)})`);
  // test-refresh reads .test-refresh.edn at startup, so restart the watcher to pick up the
  // new mode: stop this session and relaunch it in place (reusing root/command/config).
  const { root, command, testRefresh } = session;
  stopSession(session);
  await launchWatcher(root, command, testRefresh);
  flash(`Fireworks: live coding mode → ${label} (restarting watcher)`);
}

type SessionPickItem = vscode.QuickPickItem & { session: LiveSession };

// The running session to act on: the only one, or a quick pick when several are running
// (freshest under a "Recent" header). Caller guarantees liveSessions is non-empty; undefined
// means the pick was dismissed.
async function chooseSession(
  verb: 'stop' | 'restart' | 'toggle debug/test mode for',
): Promise<LiveSession | undefined> {
  const all = [...liveSessions.values()];
  if (all.length === 1) {
    return all[0];
  }
  const items = withRecentSection<SessionPickItem>(
    all.map((session) => ({
      root: session.root,
      item: {
        label: path.basename(session.root),
        description: tildify(path.dirname(session.root)),
        session,
      },
    })),
  );
  const pick = await vscode.window.showQuickPick(items, {
    placeHolder: `Select the Live Code session to ${verb}`,
    matchOnDescription: true,
  });
  return (pick as SessionPickItem | undefined)?.session;
}

// Preset auto-save delays (seconds) offered by fireworks.setAutoSaveDelay. They tune how
// quickly a save lands after you stop typing — i.e. how snappy the test-refresh/inline loop
// feels. VS Code's files.autoSaveDelay is in milliseconds.
const AUTO_SAVE_DELAYS = [0.25, 0.5, 0.75, 1, 1.5, 2, 3, 5];

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

// The row to highlight when the picker opens: the exact match if the current value is itself
// a preset, otherwise (for numeric scales) the nearest preset — so the user navigates up/down
// relative to where they already are. Strings (the color enum) only ever match exactly.
function nearestItem(items: PreviewItem[], current: string | number): PreviewItem | undefined {
  const exact = items.find((i) => i.value === current);
  if (exact || typeof current !== 'number') {
    return exact;
  }
  return items.reduce((best, i) =>
    Math.abs((i.value as number) - current) < Math.abs((best.value as number) - current)
      ? i
      : best,
  );
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
  const active = nearestItem(items, effective);
  qp.activeItems = active ? [active] : [];
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
const INLINE_BG_OPACITIES = [0, 0.03, 0.04, 0.5, 0.06, 0.7, 0.8, 0.09, 0.11, 0.13, 0.18, 0.22];
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
