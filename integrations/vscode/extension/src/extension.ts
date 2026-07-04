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
    vscode.commands.registerCommand('fireworks.preflightLiveCoding', () => preflightLiveCoding()),
    vscode.commands.registerCommand('fireworks.stopLiveCoding', () => stopLiveCoding()),
    vscode.commands.registerCommand('fireworks.restartLiveCoding', () => restartLiveCoding()),
    vscode.commands.registerCommand('fireworks.toggleDebugTestMode', () => toggleDebugTestMode()),
    vscode.commands.registerCommand('fireworks.setAutoSaveDelay', () => setAutoSaveDelay()),
    vscode.commands.registerCommand('fireworks.setInlineResultsColor', () => setInlineResultsColor()),
    vscode.commands.registerCommand('fireworks.setInlineResultsBackgroundOpacityLight', () =>
      setInlineResultsBackgroundOpacity('light'),
    ),
    vscode.commands.registerCommand('fireworks.setInlineResultsBackgroundOpacityDark', () =>
      setInlineResultsBackgroundOpacity('dark'),
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
    vscode.commands.registerCommand('fireworks.rainbowTracerVertical', () =>
      rainbowTracerVertical(),
    ),
    vscode.commands.registerCommand('fireworks.rainbowTracerHorizontal', () =>
      rainbowTracerHorizontal(),
    ),
    vscode.commands.registerCommand('fireworks.rainbowTracerCross', () => rainbowTracerCross()),
    vscode.commands.registerCommand('fireworks.setTerminalLocation', () => setTerminalLocation()),
    vscode.commands.registerCommand('fireworks.createPrintingConfig', () => createPrintingConfig()),
    vscode.commands.registerCommand('fireworks.createProject', () => createProject()),
    vscode.commands.registerCommand('fireworks.quickStart', () => quickStart()),
    vscode.commands.registerCommand('fireworks.showOutputChannel', () => output.show()),
    vscode.window.onDidCloseTerminal((t) => {
      // A session still in the map here means the user closed the terminal manually (the Stop/Restart
      // path eager-deletes before dispose, so it won't match) — treat it like a stop: drop the session
      // and clear that project's inline results.
      for (const [root, s] of liveSessions) {
        if (s.terminal === t) {
          liveSessions.delete(root);
          clearResultsForRoot(root);
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

  // If this activation follows a "Create New Project" that opened this folder, auto-start Live Code.
  void resumePendingProject();
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

// ============================================================================
// Live Code diagnostics
// ----------------------------------------------------------------------------
// A structured, human-readable trace of the connect sequence (start / stop /
// restart), emitted to the "Fireworks" output channel whether the command
// succeeds or bails. Each run prints two things: an EDN-style map of the facts
// gathered at every branch, and a numbered decision-tree of the path taken.
//
// A module-level "current run" lets the deep helpers (projectKind, pickAlias,
// resolveLein…) record into the active run without threading a param through
// every signature. When no run is active (e.g. preflight, or a helper called
// outside a diagnosed command) diagFact/diagStep are no-ops. The commands wrap
// their body in try/finally so diagEnd always flushes, error or not. Unlike
// log(), this is emitted unconditionally (these are explicit, low-frequency
// user commands) — open the Fireworks output channel to read it.
// ============================================================================

// A value already formatted as EDN (a keyword vector, a nested map): rendered verbatim,
// not re-quoted like a string. Continuation lines are re-indented to the value column.
class RawEdn {
  constructor(public readonly edn: string) {}
}

type DiagValue = string | number | boolean | null | string[] | RawEdn;

interface DiagRun {
  command: string; // the command name, shown in the header (not repeated in the facts map)
  facts: [string, DiagValue][]; // ordered key/value pairs -> the EDN map
  steps: string[]; // decision-tree lines, in the order branches were taken
  start: number;
}

let diagRun: DiagRun | undefined;

function diagBegin(command: string): void {
  diagRun = { command, facts: [], steps: [], start: Date.now() };
}

// Record a fact (a key/value row in the EDN map). Later keys append in call order.
function diagFact(key: string, value: DiagValue): void {
  if (diagRun) {
    diagRun.facts.push([key, value]);
  }
}

// Record a decision-tree step (a branch taken, with the reason).
function diagStep(line: string): void {
  if (diagRun) {
    diagRun.steps.push(line);
  }
}

function diagEnd(outcome: string): void {
  if (!diagRun) {
    return;
  }
  diagFact('Outcome', outcome);
  output.appendLine(renderDiag(diagRun));
  diagRun = undefined;
}

function ednStr(s: string): string {
  return '"' + s.replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '"';
}

// A `[:a :b]` keyword vector (deps aliases / lein profile names), or `[]`.
function ednKeywords(names: string[]): RawEdn {
  return new RawEdn(names.length === 0 ? '[]' : '[' + names.map((n) => ':' + n).join(' ') + ']');
}

// Render one value, indenting any continuation lines to `col` (the column the value starts at).
function renderDiagValue(v: DiagValue, col: number): string {
  if (v === null) {
    return 'nil';
  }
  if (typeof v === 'boolean' || typeof v === 'number') {
    return String(v);
  }
  if (typeof v === 'string') {
    return ednStr(v);
  }
  if (v instanceof RawEdn) {
    return v.edn
      .split('\n')
      .map((ln, i) => (i === 0 ? ln : ' '.repeat(col) + ln))
      .join('\n');
  }
  if (v.length === 0) {
    return '[]';
  }
  const pad = ' '.repeat(col + 1); // align entries under the char after '['
  return '[' + v.map(ednStr).map((s, i) => (i === 0 ? s : pad + s)).join('\n') + ']';
}

// The run's start time as a friendly header stamp, e.g. "Wed, Jul 1, 5:12 PM".
function diagTimestamp(ms: number): string {
  return new Date(ms).toLocaleString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

// The full diagnostics block: a header (rule, command, timestamp), then a key-aligned EDN map,
// then the numbered decision tree.
function renderDiag(run: DiagRun): string {
  const keyStrs = run.facts.map(([k]) => ednStr(k));
  const keyW = keyStrs.length ? Math.max(...keyStrs.map((k) => k.length)) : 0;
  const rule = '─'.repeat(60);
  const boxRule = '─'.repeat(24);
  const lines: string[] = [
    '',
    boxRule,
    `Command: \`Fireworks: ${run.command}\``,
    diagTimestamp(run.start),
    `┌─ Fireworks Live Code diagnostics ${boxRule}`,
  ];
  run.facts.forEach(([, v], i) => {
    const prefix = (i === 0 ? '{' : ' ') + keyStrs[i].padEnd(keyW) + ' ';
    lines.push(prefix + renderDiagValue(v, prefix.length));
  });
  lines[lines.length - 1] += '}';
  lines.push('', 'Decision tree');
  run.steps.forEach((s, i) => lines.push(`   ${i + 1}. ${s}`));
  lines.push(`└─ done in ${Date.now() - run.start} ms ${rule}`, '');
  return lines.join('\n');
}

// The build files present in a root, in resolution order — the "why eligible" for a target.
function buildFilesOf(root: string): string[] {
  return ['deps.edn', 'bb.edn', 'project.clj'].filter((f) => fs.existsSync(path.join(root, f)));
}

// The "Eligible" fact: a vector of {:name :path :configs} maps, one per eligible root, pretty-printed
// with aligned inner keys. Each line carries its indentation relative to the opening `[`; the
// diagnostics renderer then shifts the whole block right to the value column.
function ednEligible(roots: string[]): RawEdn {
  if (roots.length === 0) {
    return new RawEdn('[]');
  }
  const pad = (k: string) => k.padEnd(':configs'.length); // widest inner key -> aligned value column
  const lines: string[] = [];
  roots.forEach((root) => {
    lines.push(` {${pad(':name')} ${ednStr(path.basename(root))}`);
    lines.push(`  ${pad(':path')} ${ednStr(tildify(root))}`);
    lines.push(`  ${pad(':configs')} ${ednKeywords(buildFilesOf(root)).edn}}`);
  });
  lines[0] = '[' + lines[0].slice(1); // first map's `{` sits right after `[`
  lines[lines.length - 1] += ']';
  return new RawEdn(lines.join('\n'));
}

// A transient bottom-right notification that auto-dismisses after `ms` (default 5s). VS
// Code exposes no timeout on window.showInformationMessage, so drive a notification-
// location progress task that resolves on a timer. The toast closes when it resolves.
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

  // Remember the caret/selection before selectCurrentForm, so a no-op restores it (rather than
  // leaving Calva's whole-form selection highlighted) and a successful edit can return the caret
  // to where the user was instead of the region start.
  const original = editor.selection;

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
    restoreSelection(editor, original);
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
    restoreSelection(editor, original);
    return;
  }
  if (!plan) {
    log(`${label} no-op`);
    restoreSelection(editor, original); // nothing changed -> drop the selection, restore the caret
    return;
  }
  await applyEditPlan(editor, plan, original.active);
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
  const originalCursor = editor.selection.active; // so the caret stays put, not at file start

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
  await applyEditPlan(editor, plan, originalCursor);
}

// Apply a structural EditPlan: replace the range, optionally realign it with the editor's
// range formatter (Calva), then collapse the cursor to the plan's target. Shared by the
// form- and namespace-scoped bulk commands. formatSelection is a no-op/throws if no clojure
// range formatter is registered — the text is already structurally correct without it.
async function applyEditPlan(
  editor: vscode.TextEditor,
  plan: EditPlan,
  preferredCursor?: vscode.Position,
): Promise<void> {
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
  // Keep Vim in normal mode (an edit makes vscodevim re-assert its caret; its Escape nudges it),
  // then set the caret on a later tick so it isn't clobbered by that re-assert — mirroring
  // applyTogglePlan. Prefer the caller's original caret (clamped to the rewritten doc) so it
  // returns to where the user was, falling back to the plan's region-start cursor.
  if (isVimActive()) {
    await vscode.commands.executeCommand('extension.vim_escape');
  }
  const target = preferredCursor
    ? clampToDoc(editor.document, preferredCursor)
    : new vscode.Position(plan.newCursor.line, plan.newCursor.col);
  setTimeout(() => {
    editor.selection = new vscode.Selection(target, target);
  }, 50);
}

// Restore a previously captured selection — used when a bulk command is a no-op, so Calva's
// whole-form selection isn't left highlighted and the caret returns to where it was. Escape
// first under Vim (land in normal mode, not a lingering visual selection) and set on a later
// tick to dodge the same vscodevim re-assert applyTogglePlan works around.
function restoreSelection(editor: vscode.TextEditor, selection: vscode.Selection): void {
  if (isVimActive()) {
    void vscode.commands.executeCommand('extension.vim_escape');
  }
  setTimeout(() => {
    editor.selection = selection;
  }, 50);
}

// Clamp a Position into `doc`'s bounds. The preferred caret is in pre-edit coordinates, so after
// an unwrap shortens the text it may point past the end — clamp rather than throw or jump.
function clampToDoc(doc: vscode.TextDocument, pos: vscode.Position): vscode.Position {
  const line = Math.max(0, Math.min(pos.line, doc.lineCount - 1));
  const maxCol = doc.lineAt(line).range.end.character;
  return new vscode.Position(line, Math.min(pos.character, maxCol));
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
// .fireworks/results/<ns>/<line>_<col> (1-based, at the opening `(`). When that
// file appears/changes we re-analyze the matching visible editors (pure cljs
// gives us the ns + the live `(? …)` positions) and repaint a read-only inline
// decoration at the end of each line. Toggled by fireworks.toggleInlineResults
// (flips fireworks.inlineResults.enabled, Workspace) and auto-started on
// activation when enabled. Decoupled from live coding: it only reads the files,
// whoever produced them.
// ============================================================================

const RESULTS_DIR = '.fireworks/results';

// When true, the "Clear Inline Results" command also deletes the on-disk result
// files (every known root's .fireworks/results tree), not just the in-memory cache
// and decorations. This makes the clear permanent: a value reappears only after the
// form is re-evaluated, not on the next repaint. It also reaps orphans — files for
// forms that have since moved or been removed, and stale files left from before the
// `<line>:<col>` -> `<line>_<col>` rename. Flip to false to make Clear a transient,
// in-memory-only visual reset (the original behavior); nothing else depends on this.
const CLEAR_INLINE_RESULTS_WIPES_DISK = true;

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
// with the root its results were cached under, independent of deps.edn or how the
// results were produced (Babashka, lein, deps). Live coding's deps.edn root discovery
// (findProjectRoots) is separate and unaffected.
const fireworksRoots = new Set<string>();

// The cache identity for a single `?` result, independent of how it arrived. Today
// only the filesystem watcher writes it; a future WebSocket source builds the same
// key from values parsed off the wire.
function resultKey(root: string, ns: string, posKey: string): string {
  return `${root}\0${ns}\0${posKey}`;
}

// analyzeInlineResults parses the whole document with rewrite-clj, by far the heaviest
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

// Per-theme defaults for the background tint opacity. Kept in sync with the
// `inlineResults.backgroundOpacity.{light,dark}` defaults declared in package.json.
const DEFAULT_BG_OPACITY: Record<'light' | 'dark', number> = { light: 0.07, dark: 0.18 };

// Which palette variant the active theme calls for. High-contrast light maps to light,
// plain high-contrast (dark) maps to dark.
function themeVariant(): 'light' | 'dark' {
  const kind = vscode.window.activeColorTheme.kind;
  return kind === vscode.ColorThemeKind.Light || kind === vscode.ColorThemeKind.HighContrastLight
    ? 'light'
    : 'dark';
}

interface FireworksThemeDecision {
  value: string | null; // the FIREWORKS_THEME to force, or null to leave unset (no prefix)
  editorMood: 'light' | 'dark';
  input: string | null; // the current FIREWORKS_THEME env value seen (for the log)
  reason: string;
}

// Resolve whether/what to force for FIREWORKS_THEME, keeping the inputs so launchWatcher can log the
// whole decision to the output channel. The branch logic (stock-theme variants, mood words) lives
// in fireworks-vscode.mood; here we read the current FIREWORKS_THEME env var and supply the editor mood.
function resolveFireworksTheme(): FireworksThemeDecision {
  const current = (process.env.FIREWORKS_THEME ?? '').trim() || null;
  const editorMood = themeVariant();
  const d = cljsLib.fireworksThemeDecision(current, editorMood);
  return { value: d.value, editorMood, input: d.input, reason: d.reason };
}

// Record the FIREWORKS_THEME decision into the active diagnostics run (Fireworks output channel), and
// echo a one-liner to the log for launches outside a diagnosed command (e.g. Create New Project).
function logFireworksThemeDecision(d: FireworksThemeDecision): void {
  diagFact('Editor mood', d.editorMood);
  diagFact('FIREWORKS_THEME (env)', d.input);
  diagFact('FIREWORKS_THEME (forced)', d.value);
  diagStep(`FIREWORKS_THEME: ${d.reason}.`);
  log(`FIREWORKS_THEME decision: ${d.reason} (editor=${d.editorMood}, env=${d.input ?? 'unset'})`);
}

type ShellKind = 'posix' | 'powershell' | 'cmd';

// The integrated terminal's default shell family, so an env-var prefix uses the right syntax.
// vscode.env.shell is the user's configured shell path.
function shellKind(): ShellKind {
  const s = (vscode.env.shell || '').toLowerCase();
  if (s.includes('powershell') || s.includes('pwsh')) {
    return 'powershell';
  }
  if (/(^|[\\/])cmd(\.exe)?$/.test(s)) {
    return 'cmd';
  }
  return 'posix';
}

// A visible env-var prefix for the watcher command in the current shell: POSIX
// `VAR="v" cmd`, PowerShell `$env:VAR="v"; cmd`, cmd.exe `set "VAR=v" && cmd`. Empty string when
// there's nothing to set, so callers can prepend unconditionally.
function envVarPrefix(name: string, value: string | null): string {
  if (!value) {
    return '';
  }
  switch (shellKind()) {
    case 'powershell':
      return `$env:${name}="${value}"; `;
    case 'cmd':
      return `set "${name}=${value}" && `;
    default:
      return `${name}="${value}" `;
  }
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
  const variant = themeVariant();
  const op = clamp(
    cfg().get<number>(`inlineResults.backgroundOpacity.${variant}`, DEFAULT_BG_OPACITY[variant]),
    0,
    0.3,
  );
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
      // The lead gap rides a left margin, outside the attachment's box, so the tint
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
  // reading result files by the new <line>_<col> would surface stale values from
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
// editor. Bound to fireworks.clearInlineResults. When CLEAR_INLINE_RESULTS_WIPES_DISK
// is set (the default) it also deletes each known root's on-disk result tree, so the
// clear is permanent and reaps orphan/stale files; flip that constant off to keep this
// an in-memory-only visual reset.
function clearInlineResults(): void {
  resultsCache.clear();
  // Disk wipe (toggleable): remove the .fireworks/results tree under every root we've
  // seen results from. Best-effort and per-root (clearResultFiles swallows its own
  // errors); roots we never observed results for are simply not in the set, so we
  // never touch an unrelated project.
  if (CLEAR_INLINE_RESULTS_WIPES_DISK) {
    for (const root of fireworksRoots) {
      clearResultFiles(root);
    }
  }
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

// Make sure <root>/.fireworks/results exists. The library writes an inline result file only
// when this directory is already present (it does not create it), so create it up front —
// otherwise live coding prints to the terminal but no inline results ever appear. Gitignored
// via ensureGitignored. Best-effort.
function ensureResultsDir(root: string): void {
  try {
    fs.mkdirSync(path.join(root, '.fireworks', 'results'), { recursive: true });
  } catch (e) {
    log(`could not create .fireworks/results dir at ${root}: ${String(e)}`);
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
// Setup guidance: rendered Markdown opened to the side. No toasts or popups when Live Code
// can't proceed for a setup reason, we open a small readonly Markdown doc in the built-in preview
// (syntax-highlighted code blocks, clickable links; select-and-copy for the snippets). One reusable
// surface for every guidance message.
// ============================================================================

type GuidanceTopic =
  | 'lein-missing-plugin'
  | 'lein-user-test-refresh'
  | 'bb-watch-task'
  | 'deps-alias-deps'
  | 'external-terminal';

const GUIDANCE_SCHEME = 'fireworks-guide';

// The title (shown in the preview tab) and Markdown body for a topic. Built from single-quoted
// lines joined with newlines, so the ``` fences need no escaping. `params` carries any per-open
// data (e.g. the external-terminal command + dir) decoded from the URI query.
function guidanceDoc(
  topic: GuidanceTopic,
  params?: URLSearchParams,
): { title: string; markdown: string } {
  // Version coordinates from the cljs single source of truth (fireworks-vscode.versions) — never
  // hardcode a version in the docs below.
  const {
    fireworksSym,
    fireworksVersion,
    testRefreshSym,
    testRefreshVersion,
    leinTestRefreshSym,
    leinTestRefreshVersion,
  } = cljsLib.versions;
  switch (topic) {
    case 'lein-missing-plugin':
      return {
        title: 'Fireworks lein-test-refresh setup',
        markdown: [
          '### ⚠️ Fireworks: Live Code',
          '# Missing plugin',
          '',
          'For Leiningen projects, you need this plugin:',
          '',
          `**\`[${leinTestRefreshSym} "${leinTestRefreshVersion}"]\`**`,
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
          `    [[${leinTestRefreshSym} "${leinTestRefreshVersion}"]]}})`,
          '```',
          '',
          '  ~ or ~',
          '',
          '**Globally, in your `~/.lein/profiles.clj`:**',
          '```clj',
          '{:user',
          ' {:plugins',
          `  [[${leinTestRefreshSym} "${leinTestRefreshVersion}"]]}}`,
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

          '### ⚠️ Fireworks: Live Code',
          '# Add a `:test-refresh` config',
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
          '### ⚠️ Fireworks: Live Code',
          '# Add a Babashka watch task',
          '',
          'To use `Fireworks: Live Code` with Babashka, add a task to your `bb.edn` that loads the seeded `watch.clj` from within the `.fireworks` dir in your project:',
          '',
          '```clj',
          '{:tasks',
          ' {my-task {:task (load-file ".fireworks/bb/watch.clj")}}}',
          '```',
          '',
          'Then run `Live Code` again and pick that task.',
          '',
          ' Fireworks seeds `.fireworks/bb/watch.clj`',
          'for you, and the watcher self-loads the [fswatcher](https://github.com/babashka/fs) pod —',
          'no `:pods` entry is needed in your `bb.edn`.',
          '',
        ].join('\n'),
      };
    case 'deps-alias-deps': {
      const alias = params?.get('alias') ?? 'live-code';
      const missing = params?.get('missing') ?? 'test-refresh and Fireworks';
      return {
        title: 'Fireworks deps.edn alias dependencies',
        markdown: [
          '### ⚠️ Fireworks: Live Code',
          '# Alias is missing a dependency',
          '',
          `The \`:${alias}\` alias runs \`clojure -M:${alias}\`, but its classpath does not include **${missing}**.`,
          '',
          'Without **test-refresh**, the watcher can’t start (`clojure -M` fails to locate',
          '`com.jakemccrary.test-refresh`). Without **Fireworks**, every namespace that requires',
          '`fireworks.core` (or uses `?`) fails to reload.',
          '',
          'Run `Live Code` again and **confirm the prompt** to let Fireworks add this to your',
          '`deps.edn` for you (additive — it only adds what’s missing, and never edits global config).',
          'Or add it by hand. A working setup:',
          '',
          '```clj',
          '{:paths ["src"]',
          '',
          ' :deps {org.clojure/clojure            {:mvn/version "1.12.0"}',
          `        ${fireworksSym} {:mvn/version "${fireworksVersion}"}}`,
          '',
          ' :aliases',
          ` {:${alias}`,
          '  {:extra-paths ["test"]',
          `   :extra-deps  {${testRefreshSym} {:mvn/version "${testRefreshVersion}"}}`,
          '   :main-opts   ["-m" "com.jakemccrary.test-refresh"]}}}',
          '```',
          '',
          'Coordinates:',
          '',
          '- test-refresh: `com.jakemccrary/test-refresh`',
          '- Fireworks: `io.github.paintparty/fireworks`',
          '',
          'Then run `Live Code` again.',
          '',
        ].join('\n'),
      };
    }
    case 'external-terminal': {
      const cmd = params?.get('cmd') ?? '';
      const dir = params?.get('dir') ?? '';
      return {
        title: 'Fireworks: Run in external terminal',
        markdown: [
          '### ⚡ Fireworks: Live Code',
          '# Run in an external terminal',
          '',
          'Open your favorite terminal and run the following:',
          '',
          '```sh',
          `cd ${dir}`,
          cmd,
          '```',
          '',
          'Inline results still appear in your editor while the watcher runs (when',
          '`Fireworks: Toggle Inline Results` is on).',
          '',
          'Once your project is running, feel free to close this tab!',
          '',
        ].join('\n'),
      };
    }
  }
}

// Register the virtual-doc provider that backs the guidance previews. Content is deterministic per
// topic (carried in the URI query), so no change events are needed.
function registerGuidanceProvider(): vscode.Disposable {
  return vscode.workspace.registerTextDocumentContentProvider(GUIDANCE_SCHEME, {
    provideTextDocumentContent(uri) {
      const params = new URLSearchParams(uri.query);
      return guidanceDoc(params.get('t') as GuidanceTopic, params).markdown;
    },
  });
}

// Open a topic's guidance as a rendered Markdown preview, to the side of the active editor. The
// `.md` path makes VS Code treat the virtual doc as Markdown; the topic rides in the query. Falls
// back to the raw text document if the Markdown preview command is unavailable.
async function showGuidance(
  topic: GuidanceTopic,
  params?: Record<string, string>,
): Promise<void> {
  const sp = new URLSearchParams({ t: topic, ...(params ?? {}) });
  const { title } = guidanceDoc(topic, sp);
  const uri = vscode.Uri.parse(
    `${GUIDANCE_SCHEME}:${encodeURIComponent(title)}.md?${sp.toString()}`,
  );
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
  ensureResultsDir(root); // recreate empty results/ after the wipe so the library writes into it
}

// A result file changed. Its path is <root>/.fireworks/results/<ns>/<line>_<col>.
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
  const rest = fsPath.slice(i + marker.length); // "<ns>/<line>_<col>"
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
// that project's own config: a deps.edn alias (`clojure -M:<alias>`), a bb.edn task
// (`bb <task>`), or a Leiningen profile carrying lein-test-refresh
// (`lein with-profile +<profile> test-refresh`). deps and lein may additively edit their build
// file (picker-driven, no modal — the pick is the consent): a fresh :live-code alias/profile and
// the Fireworks dep, never rewriting the user's own alias/profile. bb writes only the watcher seed.
// ============================================================================

// The terminal name for a root's session, basename-tagged so concurrent sessions in
// different projects are distinguishable in the terminal dropdown.
function terminalName(root: string): string {
  return `Fireworks: Live Code — ${path.basename(root)}`;
}

// Cosmetic: ms to let the shell prompt finish drawing before sending the watcher
// command, so zsh's line editor doesn't double-echo it. Remove the `await` in
// sendWatcherCommand (and this const) to revert.
const PROMPT_SETTLE_MS = 250;

// Babashka only: ms to wait after launching the bb watcher before nudging the active file so its
// inline results paint. The bb watcher only reacts to file saves (its fswatcher pod fires on write),
// and nothing runs at startup, so without this nudge nothing appears until the user saves. deps/lein
// run test-refresh on startup and paint on their own, so they don't need it. Set to 0 to disable.
const BB_STARTUP_NUDGE_MS = 3000;

// One live-coding session per project root. Several can run at once (different projects
// in the same workspace); each owns its own integrated terminal.
interface LiveSession {
  root: string;
  command: string; // the verbatim watcher command it launched with (for restart)
  terminal: vscode.Terminal;
  execution: vscode.TerminalShellExecution | undefined; // tracked when shell integration is available
  testRefresh?: TestRefreshInfo; // the .test-refresh.edn governing the run (Clojure/deps only)
  runtime?: Runtime; // the resolved runtime (drives the bb startup nudge; reused on restart)
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
// several. The pick mirrors VS Code's own "new terminal" cwd picker, with folder name as the
// label and home-abbreviated parent path as the muted description, with recently-launched
// projects floated under a "Recent" header. undefined if none, or the pick was dismissed.
async function pickProjectRoot(): Promise<string | undefined> {
  const roots = await findProjectRoots();
  // Report every eligible root: its name, path, and the build file(s) that made it a target.
  diagFact('Eligible', ednEligible(roots));
  if (roots.length === 0) {
    diagStep('No deps.edn / bb.edn / project.clj in the workspace → abort.');
    vscode.window.showErrorMessage(
      'Fireworks: no deps.edn, bb.edn, or project.clj found in this workspace.',
    );
    return undefined;
  }
  if (roots.length === 1) {
    diagFact('Chosen project', tildify(roots[0]));
    diagStep(`Only one eligible project → auto-selected ${tildify(roots[0])}.`);
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
  const chosen = (pick as RootPickItem | undefined)?.root;
  diagFact('Chosen project', chosen ? tildify(chosen) : null);
  diagStep(
    chosen
      ? `Picked ${tildify(chosen)} from ${roots.length} eligible projects.`
      : `Project pick dismissed (${roots.length} were offered).`,
  );
  return chosen;
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
// runs the task even for builtin-colliding names, and it avoids the `bb run` footgun where
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
  diagFact('.fireworks dir exists?', hasFireworksDir(root));
  const kinds: { label: string; runtime: Runtime }[] = [];
  if (fs.existsSync(path.join(root, 'deps.edn'))) {
    kinds.push({ label: 'Clojure (deps.edn alias)', runtime: 'deps' });
  }
  // bb is offered only when wired for it (a task load-files .fireworks/bb/watch.clj), so a
  // bb.edn kept for build scripts alongside a deps.edn / project.clj isn't mistaken for a watcher.
  const bbPresent = fs.existsSync(path.join(root, 'bb.edn'));
  const bbWired = bbPresent && bbHasWatchTask(root);
  if (bbWired) {
    kinds.push({ label: 'Babashka (.fireworks/bb/watch.clj)', runtime: 'bb' });
  }
  if (bbPresent) {
    diagFact('bb.edn wired as a watcher?', bbWired);
  }
  if (fs.existsSync(path.join(root, 'project.clj'))) {
    kinds.push({ label: 'Leiningen (project.clj profile)', runtime: 'lein' });
  }
  diagFact('Candidate runtimes', ednKeywords(kinds.map((k) => k.runtime)));
  if (kinds.length === 0) {
    // Reached only when the sole build file is an unwired bb.edn (deps/lein always qualify).
    if (bbPresent) {
      diagStep('bb.edn present but no task load-files .fireworks/bb/watch.clj → bb setup guide.');
      await showGuidance('bb-watch-task');
    }
    diagFact('Runtime', null);
    return undefined;
  }
  if (kinds.length === 1) {
    diagFact('Runtime', kinds[0].runtime);
    diagStep(`Single candidate runtime → ${kinds[0].runtime}.`);
    return kinds[0].runtime;
  }
  const pick = await vscode.window.showQuickPick(kinds, {
    placeHolder: 'This project has multiple build files — which runtime?',
  });
  diagFact('Runtime', pick ? pick.runtime : null);
  diagStep(
    pick
      ? `Multiple build files → user chose runtime ${pick.runtime}.`
      : 'Multiple build files → runtime pick dismissed.',
  );
  return pick?.runtime;
}

// Whether `root`'s bb.edn is wired as a Fireworks watcher: a task whose body load-files
// .fireworks/bb/watch.clj. This is the opt-in signal that gates whether bb is offered as a
// runtime. A bb.edn kept only for build scripts returns false, so it isn't mistaken for a
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
// it). Never overwrites an existing file. It may be user-edited and is meant to be committed.
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
    diagStep('bb.edn did not parse → abort.');
    vscode.window.showErrorMessage(`Fireworks: could not parse bb.edn in ${tildify(root)}.`);
    return undefined;
  }
  diagFact('bb watch tasks', ednKeywords(tasks));
  if (tasks.length === 0) {
    // projectKind gates bb on this, so this is a safety net rather than the usual path.
    diagStep('No wired bb watch task → bb setup guide.');
    await showGuidance('bb-watch-task');
    return undefined;
  }
  const task =
    tasks.length === 1
      ? tasks[0]
      : await vscode.window.showQuickPick(tasks, {
          placeHolder: 'Select the bb watch task to run',
        });
  diagBbTask(task, tasks.length);
  if (!task) {
    return undefined;
  }
  const watchExisted = fs.existsSync(path.join(root, '.fireworks', 'bb', 'watch.clj'));
  diagFact('.fireworks/bb/watch.clj existed?', watchExisted);
  if (!ensureBbWatchFile(root)) {
    diagStep('Could not seed .fireworks/bb/watch.clj → abort.');
    return undefined;
  }
  if (!watchExisted) {
    diagStep('Seeded .fireworks/bb/watch.clj from the template.');
  }
  return { command: taskCommand(task) };
}

// Small helper so the bb-task fact/step reads cleanly at both call arities above.
function diagBbTask(task: string | undefined, count: number): void {
  diagFact('Chosen bb task', task ?? null);
  diagStep(
    task
      ? `Chose bb task :${task}${count > 1 ? ` from ${count}` : ' (only one wired)'}.`
      : `bb task pick dismissed (${count} offered).`,
  );
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
// seed write failed. test-refresh falls back to its own defaults).
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
  const global = path.join(os.homedir(), '.test-refresh.edn');
  const localExists = fs.existsSync(local);
  const globalExists = fs.existsSync(global);
  diagFact('local .test-refresh.edn exists?', localExists);
  diagFact('global .test-refresh.edn exists?', globalExists);
  if (localExists) {
    diagStep('Using the project-local .test-refresh.edn (no seed written).');
    log(`.test-refresh.edn: using project-local ${tildify(local)}`);
    return { path: local, source: 'local' };
  }
  if (globalExists) {
    diagStep('No local .test-refresh.edn, but ~/.test-refresh.edn exists → run off the global config.');
    log(`.test-refresh.edn: using global ${tildify(global)}`);
    return { path: global, source: 'global' };
  }
  try {
    fs.writeFileSync(local, cljsLib.testRefreshTemplate(), 'utf8');
    diagStep('No local or global .test-refresh.edn → seeded a local one from the template.');
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
  runtime?: Runtime; // set by resolveWatcherCommand; drives runtime-specific launch behavior (bb nudge)
}

// Resolve the launch plan for `root`: detect the runtime, run its PATH preflight, then pick
// the alias (deps) or task (bb). undefined aborts the launch (a message was already shown,
// or a pick/preflight failed). Tags the plan with the resolved runtime.
async function resolveWatcherCommand(root: string): Promise<WatcherPlan | undefined> {
  const kind = await projectKind(root);
  if (!kind) {
    return undefined;
  }
  const plan =
    kind === 'bb'
      ? await resolveBbCommand(root)
      : kind === 'lein'
        ? await resolveLeinCommand(root)
        : await resolveDepsCommand(root);
  return plan ? { ...plan, runtime: kind } : undefined;
}

// --- Clojure CLI (deps.edn) launch resolution -----------------------------
// The alias must put BOTH test-refresh and Fireworks on the `-M` classpath AND run test-refresh via
// :main-opts, or the launch fails (no test-refresh → can't locate the main ns; no Fireworks → any ns
// that requires fireworks.core or uses `?` fails to reload). When no alias qualifies, the picker
// offers — additively, no modal (the pick is the consent) — to add a fresh :live-code alias
// (Fireworks into the top-level :deps, test-refresh into the alias). We never rewrite the user's own
// alias. Mirrors the Leiningen project.clj flow: the user still owns deps.edn; we only ever add.
async function resolveDepsCommand(root: string): Promise<WatcherPlan | undefined> {
  if (!preflightClojure()) {
    return undefined;
  }
  const depsPath = path.join(root, 'deps.edn');
  const text = readFileOrNull(depsPath);
  if (text === null) {
    diagStep('deps.edn could not be read → abort.');
    vscode.window.showErrorMessage(`Fireworks: could not read deps.edn in ${tildify(root)}.`);
    return undefined;
  }
  const { aliases, error } = cljsLib.depsAliases(text);
  if (error || !aliases) {
    diagStep('deps.edn did not parse → abort.');
    vscode.window.showErrorMessage(`Fireworks: could not parse deps.edn in ${tildify(root)}.`);
    return undefined;
  }
  diagFact('Aliases in deps.edn', ednKeywords(aliases));

  // An alias is eligible for live coding when it runs test-refresh via :main-opts with test-refresh
  // on its `-M` classpath. Fireworks is NOT required here: it's a project dep, so a missing Fireworks
  // is patched into the top-level :deps on launch (below), not a reason to hide the alias. We still
  // never rewrite the user's alias itself. Carry hasFireworks so we know whether to patch on pick.
  type AliasItem = vscode.QuickPickItem & { alias: string; hasFireworks: boolean };
  const eligible: AliasItem[] = aliases.flatMap((a) => {
    const s = cljsLib.depsAliasStatus(text, a);
    if (s.error || !s.hasTestRefresh || s.mainOpts !== 'test-refresh') return [];
    return [{ label: `:${a}`, alias: a, hasFireworks: !!s.hasFireworks }];
  });
  diagFact('Live-coding-eligible aliases', eligible.length ? ednKeywords(eligible.map((i) => i.alias)) : 'none');

  // Some aliases qualify → pick one to run. (No global option here — the user is choosing a specific
  // local alias.) If Fireworks isn't on the chosen alias's classpath, patch it into the top-level
  // :deps first (no modal: the user picked this alias from the list).
  if (eligible.length > 0) {
    const pick = await vscode.window.showQuickPick(eligible, {
      placeHolder: 'Select the deps.edn alias to run',
    });
    if (!pick) {
      diagFact('Chosen alias', null);
      diagStep('Alias pick dismissed.');
      return undefined;
    }
    const alias = pick.alias;
    diagFact('Chosen alias', `:${alias}`);
    if (!pick.hasFireworks && !ensureFireworksDep(depsPath, text)) {
      return undefined;
    }
    diagStep(`Alias :${alias} runs test-refresh → use as-is.`);
    return finishDepsPlan(root, alias);
  }

  // No eligible alias → offer to add a fresh :live-code alias to the local deps.edn. When a valid
  // global setup exists (an eligible alias in ~/.clojure/deps.edn AND a global ~/.test-refresh.edn),
  // also offer to run straight from it — zero project edits. Selecting from the picker is the consent
  // (no modal), mirroring the Leiningen project.clj flow.
  const globalPlan = tryDepsGlobalPlan(root);
  type AddItem = vscode.QuickPickItem & { global?: boolean };
  const items: AddItem[] = [{ label: 'Setup a :live-code alias, in your local deps.edn' }];
  if (globalPlan) {
    items.push({ label: 'Run from your global ~/.clojure/deps.edn', global: true });
  }
  diagStep(
    globalPlan
      ? 'No eligible alias → offer a local :live-code alias or the valid global deps.edn alias.'
      : 'No eligible alias → offer to add a :live-code alias.',
  );
  const pick = await vscode.window.showQuickPick(items, {
    placeHolder: 'No eligible alias in deps.edn',
  });
  if (!pick) {
    diagFact('Chosen alias', null);
    diagStep('Deps launch pick dismissed → abort.');
    return undefined;
  }
  if (pick.global) {
    diagStep('User chose to run from the global ~/.clojure/deps.edn alias.');
    return globalPlan;
  }
  diagStep('User chose to add a :live-code alias → write the deps.edn edit.');
  const created = createLiveCodeAlias(depsPath, text);
  return created ? finishDepsPlan(root, created) : undefined;
}

// Is there a valid, self-sufficient global deps.edn way to run (read-only — global config is never
// written)? Returns a `clojure -M:<alias>` plan when ~/.clojure/deps.edn carries an alias with ALL of:
// test-refresh AND Fireworks on its classpath AND :main-opts running test-refresh, AND a global
// ~/.test-refresh.edn (the options) is in place — clojure auto-merges the user deps.edn, so no project
// edit is needed. The caller then offers "Run from your global ~/.clojure/deps.edn" alongside the
// add-an-alias choice. undefined when anything is missing / absent / unparseable (logged, no toast).
// Mirrors tryLeinGlobalUserPlan on the Leiningen side.
function tryDepsGlobalPlan(root: string): WatcherPlan | undefined {
  const globalDepsPath = path.join(os.homedir(), '.clojure', 'deps.edn');
  const text = readFileOrNull(globalDepsPath);
  diagFact('~/.clojure/deps.edn exists?', text !== null);
  if (text === null) {
    return undefined;
  }
  const { aliases, error } = cljsLib.depsAliases(text);
  if (error || !aliases) {
    diagStep('~/.clojure/deps.edn did not parse → global option not offered.');
    return undefined;
  }
  // The global alias must be fully self-sufficient: test-refresh + Fireworks on its classpath and
  // :main-opts running test-refresh (unlike a project alias, a missing Fireworks isn't patched here —
  // the whole point of the global option is zero project edits).
  const alias = aliases.find((a) => {
    const s = cljsLib.depsAliasStatus(text, a);
    return !s.error && s.hasTestRefresh && s.hasFireworks && s.mainOpts === 'test-refresh';
  });
  diagFact('Fully-provisioned global alias', alias ? `:${alias}` : 'none');
  if (!alias) {
    return undefined;
  }
  const globalTestRefresh = fs.existsSync(path.join(os.homedir(), '.test-refresh.edn'));
  diagFact('global ~/.test-refresh.edn exists?', globalTestRefresh);
  if (!globalTestRefresh) {
    return undefined;
  }
  diagStep(`Global :${alias} alias (test-refresh + Fireworks) + ~/.test-refresh.edn present → offer global run.`);
  return finishDepsPlan(root, alias);
}

// Patch the Fireworks coordinate into the top-level :deps (with the elide comment) when an otherwise
// eligible alias's classpath lacks it. No modal — the user already chose this alias. Returns true to
// proceed (patched, or nothing to do), false on an edit/write failure.
function ensureFireworksDep(depsPath: string, text: string): boolean {
  const r = cljsLib.depsEnsureFireworks(text);
  if (r.error || r.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(depsPath)}.`);
    return false;
  }
  if (!r.changed) return true;
  if (!writeDepsEdn(depsPath, r.text)) return false;
  diagFact('deps.edn edit', 'added Fireworks to top-level :deps');
  diagStep('Patched deps.edn: added Fireworks to the top-level :deps.');
  return true;
}

// Common tail once an alias is settled (chosen-and-ready, patched, or freshly created): ensure a
// .test-refresh.edn governs the run, then build `clojure -M:<alias>`.
function finishDepsPlan(root: string, alias: string): WatcherPlan {
  const testRefresh = ensureTestRefreshConfig(root);
  diagFact('.test-refresh.edn source', testRefresh.source);
  return { command: aliasCommand(alias), testRefresh };
}

// Add a self-contained :live-code alias to deps.edn (+ top-level Fireworks with the elide comment).
// No modal — the user chose "Add a :live-code alias" from the picker. Returns the alias name to run
// on success, undefined on an edit/write failure.
function createLiveCodeAlias(depsPath: string, text: string): string | undefined {
  const r = cljsLib.depsAddLiveCodeAlias(text);
  if (r.error || r.text === undefined || !r.alias) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(depsPath)}.`);
    return undefined;
  }
  if (!writeDepsEdn(depsPath, r.text)) {
    return undefined;
  }
  diagFact('deps.edn edit', `added :${r.alias} alias`);
  diagStep(`Wrote a :${r.alias} alias to deps.edn.`);
  return r.alias;
}

function writeDepsEdn(depsPath: string, text: string): boolean {
  try {
    fs.writeFileSync(depsPath, text, 'utf8');
    log(`deps.edn: patched ${tildify(depsPath)}`);
    return true;
  } catch (e) {
    log(`deps.edn: could not write ${tildify(depsPath)}: ${String(e)}`);
    vscode.window.showErrorMessage(`Fireworks: could not write ${tildify(depsPath)}.`);
    return false;
  }
}

// --- Leiningen launch resolution ------------------------------------------
// Mirrors the deps.edn flow: picker-driven, no confirm modal — selecting a profile (or the
// "add a :live-code profile" choice) is the consent. The Leiningen flow may additively edit the
// user's project.clj (rewrite-clj preserves formatting/comments): add a fresh :live-code profile,
// ensure the top-level :test-refresh map, and patch in the Fireworks dependency. It never rewrites
// a profile the user already has. Eligibility: a :profiles entry whose :plugins carries exactly
// [com.jakemccrary/lein-test-refresh "0.26.0"].

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

// Apply the additive launch edits to project.clj `text` in memory (no write): ensure the Fireworks
// dependency is on the classpath, then ensure the top-level :test-refresh map against the baseline.
// No modal — the profile pick already consented. Returns the patched text (equal to `text` when
// nothing needed adding), or undefined on a parse/edit failure (an error toast is shown).
function patchLeinProjectClj(projectCljPath: string, text: string): string | undefined {
  const fw = cljsLib.leinEnsureFireworks(text);
  if (fw.error || fw.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(projectCljPath)}.`);
    return undefined;
  }
  if (fw.changed) {
    diagStep('Patched project.clj: added Fireworks to the top-level :dependencies.');
  }
  const tr = cljsLib.leinEnsureTestRefresh(fw.text);
  if (tr.error || tr.text === undefined) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(projectCljPath)}.`);
    return undefined;
  }
  if (tr.changed) {
    diagStep(`Patched project.clj: added a :test-refresh config (${(tr.addedKeys ?? []).join(', ')}).`);
  }
  return tr.text;
}

// Is the global ~/.lein/profiles.clj :user profile a valid, self-sufficient way to run (read-only —
// global config is never written)? Returns a plain `lein test-refresh` plan only when :user carries
// ALL of: the test-refresh plugin, a Fireworks dependency, and a :test-refresh options map (it
// auto-merges into every task, so no project edit is needed). The caller then offers "Run from your
// global ~/.lein/profiles.clj" alongside the add-a-profile choice. undefined when anything is missing
// / the file is absent / unparseable (logged, no toast).
function tryLeinGlobalUserPlan(): WatcherPlan | undefined {
  const profilesPath = path.join(os.homedir(), '.lein', 'profiles.clj');
  const text = readFileOrNull(profilesPath);
  diagFact('~/.lein/profiles.clj exists?', text !== null);
  if (text === null) {
    return undefined;
  }
  const status = cljsLib.leinUserProfileStatus(text);
  if (status.error) {
    diagStep('~/.lein/profiles.clj did not parse → global :user not offered.');
    return undefined;
  }
  diagFact('global :user has lein-test-refresh?', !!status.hasPlugin);
  diagFact('global :user has Fireworks?', !!status.hasFireworks);
  diagFact('global :user has :test-refresh?', !!status.hasTestRefresh);
  if (status.hasPlugin && status.hasFireworks && status.hasTestRefresh) {
    diagStep('Global :user carries plugin + Fireworks + :test-refresh → offer plain `lein test-refresh`.');
    return { command: leinCommand() };
  }
  return undefined;
}

// Additively add a fresh :live-code profile (carrying lein-test-refresh), then patch in Fireworks +
// :test-refresh, in one write. No modal — the picker choice was the consent, mirroring the deps.edn
// "Add a :live-code alias" path. Returns the plan, or undefined on an edit/write failure.
function addLeinLiveCodeProfile(projectCljPath: string, text: string): WatcherPlan | undefined {
  const added = cljsLib.leinAddLiveCodeProfile(text);
  if (added.error || added.text === undefined || !added.profile) {
    vscode.window.showErrorMessage(`Fireworks: could not edit ${tildify(projectCljPath)}.`);
    return undefined;
  }
  diagFact('project.clj edit', `added :${added.profile} profile`);
  const patched = patchLeinProjectClj(projectCljPath, added.text);
  if (patched === undefined) {
    return undefined;
  }
  if (!writeProjectClj(projectCljPath, patched)) {
    return undefined;
  }
  diagStep(`Wrote a :${added.profile} profile to project.clj.`);
  return { command: leinCommand(added.profile) };
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
    diagStep('project.clj did not parse → abort.');
    vscode.window.showErrorMessage(`Fireworks: could not parse project.clj in ${tildify(root)}.`);
    return undefined;
  }
  diagFact('All profiles in project.clj', ednKeywords(all));
  diagFact('Eligible profiles', ednKeywords(eligible));

  // An eligible profile already carries the plugin → pick it, patch Fireworks + :test-refresh (no
  // modal — the pick is the consent), launch. Fireworks isn't required for eligibility: it's a
  // project dep, so a missing one is patched into :dependencies here rather than blocking the launch.
  if (eligible.length > 0) {
    const profile = await pickProfile(
      eligible,
      'Select the profile to run (carries lein-test-refresh)',
    );
    diagFact('Chosen profile', profile ? `:${profile}` : null);
    if (!profile) {
      diagStep('Eligible profile pick dismissed → abort.');
      return undefined;
    }
    diagStep(`Profile :${profile} carries the plugin → patch Fireworks/:test-refresh, then launch.`);
    const patched = patchLeinProjectClj(projectCljPath, text);
    if (patched === undefined) {
      return undefined;
    }
    if (patched !== text && !writeProjectClj(projectCljPath, patched)) {
      return undefined;
    }
    return { command: leinCommand(profile) };
  }

  // No eligible profile → offer to add a fresh :live-code profile to the local project.clj. When a
  // valid global ~/.lein/profiles.clj :user profile exists (plugin + :test-refresh, which auto-merges
  // into every task), also offer to run straight from it — zero project edits. Selecting from the
  // picker is the consent (no modal), mirroring the deps.edn "Add a :live-code alias" choice.
  const globalPlan = tryLeinGlobalUserPlan();
  type LeinChoice = vscode.QuickPickItem & { global?: boolean };
  const items: LeinChoice[] = [
    { label: 'Setup a :live-code profile, in your local project.clj' },
  ];
  if (globalPlan) {
    items.push({ label: 'Run from your global ~/.lein/profiles.clj', global: true });
  }
  diagStep(
    globalPlan
      ? 'No eligible profile → offer a local :live-code profile or the valid global :user profile.'
      : 'No eligible profile → offer to add a :live-code profile.',
  );
  const pick = await vscode.window.showQuickPick(items, {
    placeHolder: 'No eligible profile in project.clj',
  });
  if (!pick) {
    diagStep('Lein launch pick dismissed → abort.');
    return undefined;
  }
  if (pick.global) {
    diagStep('User chose to run from the global ~/.lein/profiles.clj :user profile.');
    return globalPlan;
  }
  diagStep('User chose to add a :live-code profile → write the project.clj edit.');
  return addLeinLiveCodeProfile(projectCljPath, text);
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
  diagBegin('Live Code (Start)');
  try {
    const location = await resolveTerminalLocation();
    if (!location) {
      diagStep('Terminal-location pick dismissed → abort.');
      diagEnd('cancelled (no terminal location)');
      return;
    }
    const root = await pickProjectRoot();
    if (!root) {
      diagEnd('cancelled (no project chosen)');
      return;
    }
    // The integrated terminal owns the session lifecycle (reuse/stop/restart). External mode runs
    // the watcher in the user's own terminal, so it creates no session and skips the reuse check.
    if (location === 'integrated') {
      const existing = liveSessions.get(root);
      if (existing) {
        diagStep(`A session is already running for ${tildify(root)} → reuse/notify, no relaunch.`);
        diagEnd('already running');
        await reuseOrNotify(existing);
        return;
      }
    }
    const plan = await resolveWatcherCommand(root);
    if (!plan) {
      diagStep('Command resolution returned nothing (aborted, or a setup guide was shown).');
      diagEnd('aborted during resolution');
      return;
    }
    diagFact('Watcher command', plan.command);
    if (location === 'integrated') {
      // Resolve + log the FIREWORKS_THEME decision here, while the diag run is still open, so it appears
      // in the decision tree (launchWatcher runs after diagEnd, where diagFact/diagStep are no-ops).
      const fireworksTheme = resolveFireworksTheme();
      logFireworksThemeDecision(fireworksTheme);
      diagStep(`Launch in integrated terminal "${terminalName(root)}".`);
      diagEnd('launched (integrated terminal)');
      await launchWatcher(root, plan.command, plan.testRefresh, plan.runtime, fireworksTheme);
    } else {
      // resolveWatcherCommand already seeded configs / patched the build file; we just hand the
      // user the command instead of spawning a terminal. Keep .fireworks ready for the
      // inline-results watcher and record the root, mirroring launchWatcher's bookkeeping.
      prepareResultsDir(root);
      clearResultsForRoot(root);
      recordRecentRoot(root);
      // The user runs this in their own shell; prepend the shell-appropriate env prefix to the shown
      // command (same decision + logging as an integrated launch).
      const fireworksTheme = resolveFireworksTheme();
      logFireworksThemeDecision(fireworksTheme);
      const cmd = envVarPrefix('FIREWORKS_THEME', fireworksTheme.value) + plan.command;
      diagStep('Show the external-terminal guidance (no terminal spawned).');
      diagEnd('launched (external terminal guidance)');
      await showGuidance('external-terminal', { cmd, dir: root });
    }
  } finally {
    diagEnd('ended'); // no-op if a branch above already flushed; safety net on unexpected throw
  }
}

// --- Create New Project ---------------------------------------------------
//
// Scaffold a fresh, correctly-wired project from the on-disk examples/<kind>-project/ tree, open
// it, and auto-start Live Code so the user lands on core.clj ready to jam. The file bodies live in
// examples/ (bundled in the .vsix); the pure name-substitution rules live in fireworks-vscode.scaffold
// (cljsLib.scaffold*). Opening a folder restarts the extension host, so the launch is handed off via
// globalState and resumed by resumePendingProject() on the next activation.

const PENDING_PROJECT_KEY = 'fireworks.pendingProject';

interface PendingProject {
  root: string;
  runtime: Runtime;
  command: string;
  openFile: string; // relative to root
}

// Map a project kind to its bundled example directory name.
const EXAMPLE_DIR: Record<Runtime, string> = {
  deps: 'deps-project',
  lein: 'leiningen-project',
  bb: 'babashka-project',
};

// Copy the bundled example tree into destRoot, letting the cljs rules rename each path (skipping
// nulls) and substitute the project name into each file's contents.
function scaffoldTree(srcDir: string, destRoot: string, kind: Runtime, name: string): void {
  const walk = (dir: string): void => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const abs = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        walk(abs);
        continue;
      }
      const rel = path.relative(srcDir, abs).split(path.sep).join('/');
      const destRel = cljsLib.scaffoldPath(kind, name, rel);
      if (destRel == null) {
        continue; // skipped (regenerated .gitignore, result cache, build/cache dirs)
      }
      const out = cljsLib.scaffoldContent(kind, name, rel, fs.readFileSync(abs, 'utf8'));
      const destAbs = path.join(destRoot, destRel.split('/').join(path.sep));
      fs.mkdirSync(path.dirname(destAbs), { recursive: true });
      fs.writeFileSync(destAbs, out, 'utf8');
    }
  };
  walk(srcDir);
}

// Open a scaffolded project's entry file and launch its Live Code watcher. Shared by the direct
// (add-to-workspace, no host restart) and resumed (after openFolder) paths.
async function openAndLaunch(pending: PendingProject): Promise<void> {
  try {
    const fileUri = vscode.Uri.file(path.join(pending.root, pending.openFile));
    await vscode.window.showTextDocument(fileUri, { preview: false });
  } catch (e) {
    log(`create-project: could not open ${pending.openFile}: ${String(e)}`);
  }
  await launchWatcher(pending.root, pending.command, undefined, pending.runtime);
}

// Called on activation: if a "Create New Project" handoff targets a folder open in this window,
// clear it (once) and auto-start Live Code there.
async function resumePendingProject(): Promise<void> {
  const pending = extContext.globalState.get<PendingProject>(PENDING_PROJECT_KEY);
  if (!pending) {
    return;
  }
  const folders = vscode.workspace.workspaceFolders ?? [];
  if (!folders.some((f) => f.uri.fsPath === pending.root)) {
    return; // handoff is for a different window
  }
  await extContext.globalState.update(PENDING_PROJECT_KEY, undefined); // consume once
  diagBegin('Create New Project (resume)');
  diagFact('Project', tildify(pending.root));
  diagFact('Watcher command', pending.command);
  diagEnd('auto-launching Live Code');
  await openAndLaunch(pending);
}

async function createProject(): Promise<void> {
  diagBegin('Create New Project');
  try {
    const kindPick = await vscode.window.showQuickPick(
      [
       { label: 'Deps', description: '(Official default)', runtime: 'deps' as Runtime },
       { label: 'Babashka', description: '(Scripting runtime with instant startup)', runtime: 'bb' as Runtime },
       { label: 'Leiningen', description: '(Legacy build tool)', runtime: 'lein' as Runtime }
      ],
      { placeHolder: 'What kind of project do you want to create?' },
    );
    if (!kindPick) {
      diagEnd('cancelled (no kind)');
      return;
    }
    const kind = kindPick.runtime;

    const parentPick = await vscode.window.showOpenDialog({
      canSelectFolders: true,
      canSelectFiles: false,
      canSelectMany: false,
      openLabel: 'Select parent folder',
      title: 'Select the parent folder for the new project',
    });
    if (!parentPick || parentPick.length === 0) {
      diagEnd('cancelled (no parent folder)');
      return;
    }
    const parent = parentPick[0].fsPath;

    const name = await vscode.window.showInputBox({
      prompt: 'Project name',
      placeHolder: 'my-app',
      validateInput: (v) => {
        const t = (v ?? '').trim();
        if (!t) {
          return 'Enter a project name.';
        }
        if (!/^[a-zA-Z][a-zA-Z0-9-]*$/.test(t)) {
          return 'Use letters, digits and hyphens; start with a letter.';
        }
        if (fs.existsSync(path.join(parent, t))) {
          return `A folder named "${t}" already exists here.`;
        }
        return undefined;
      },
    });
    if (!name) {
      diagEnd('cancelled (no name)');
      return;
    }
    const projectName = name.trim();
    const root = path.join(parent, projectName);

    const openPick = await vscode.window.showQuickPick(
      [
        { label: 'Add to workspace', desc: 'Adds folder to this workspace', value: 'add' as const },
        { label: 'Same window', desc: 'Creates a new workspace in this window', value: 'same' as const },
        { label: 'New window', desc: 'Creates a new workspace in a new window', value: 'new' as const },
      ],
      { placeHolder: 'Open the new project in…' },
    );
    if (!openPick) {
      diagEnd('cancelled (no open target)');
      return;
    }

    await scaffoldAndOpen(kind, root, projectName, openPick.value);
  } finally {
    diagEnd('ended'); // no-op if a branch already flushed
  }
}

// Scaffold the project at `root` and open/launch it per the target. Shared by createProject (via
// its pickers) and quickStart (with everything pre-computed). Assumes the caller has already opened
// a diag run; flushes it with diagEnd in each branch.
async function scaffoldAndOpen(
  kind: Runtime,
  root: string,
  projectName: string,
  openTarget: 'add' | 'same' | 'new',
): Promise<void> {
  if (fs.existsSync(root)) {
    // Belt-and-suspenders with the input validation (a folder could appear in between).
    await vscode.window.showErrorMessage(`Fireworks: "${root}" already exists — aborting.`);
    diagEnd('aborted (target exists)');
    return;
  }

  diagFact('Kind', kind);
  diagFact('Root', tildify(root));
  const srcDir = vscode.Uri.joinPath(extContext.extensionUri, 'examples', EXAMPLE_DIR[kind]).fsPath;
  try {
    scaffoldTree(srcDir, root, kind, projectName);
    fs.writeFileSync(path.join(root, '.gitignore'), cljsLib.scaffoldGitignore(), 'utf8');
    const resultsDir = path.join(root, '.fireworks', 'results');
    fs.mkdirSync(resultsDir, { recursive: true });
    fs.writeFileSync(path.join(resultsDir, '.gitkeep'), '', 'utf8');
  } catch (e) {
    await vscode.window.showErrorMessage(`Fireworks: could not create project — ${String(e)}`);
    diagEnd(`failed (${String(e)})`);
    return;
  }

  const command = cljsLib.scaffoldLaunch(kind);
  const openFile = cljsLib.scaffoldOpenFile(kind, projectName);
  const pending: PendingProject = { root, runtime: kind, command, openFile };
  const uri = vscode.Uri.file(root);
  const openFolders = vscode.workspace.workspaceFolders ?? [];

  if (openTarget === 'add' && openFolders.length > 0) {
    // Adding a folder to a window that already has one does not restart the host — launch here.
    vscode.workspace.updateWorkspaceFolders(openFolders.length, 0, { uri });
    diagStep('Added to the current workspace (no host restart) → launching Live Code.');
    diagEnd('scaffolded + launched');
    await openAndLaunch(pending);
    return;
  }

  // Every remaining path (new window, same window, or adding the first folder to an empty window)
  // restarts the extension host. Hand the launch off through globalState; resumePendingProject
  // picks it up when the extension reactivates in the opened folder.
  await extContext.globalState.update(PENDING_PROJECT_KEY, pending);
  if (openTarget === 'add') {
    diagStep('Added first folder to an empty window (host restarts) → handoff.');
    diagEnd('scaffolded (handoff via updateWorkspaceFolders)');
    vscode.workspace.updateWorkspaceFolders(0, 0, { uri });
  } else {
    const forceNewWindow = openTarget === 'new';
    diagStep(`Opening folder (${forceNewWindow ? 'new' : 'same'} window; host restarts) → handoff.`);
    diagEnd('scaffolded (handoff via openFolder)');
    await vscode.commands.executeCommand('vscode.openFolder', uri, { forceNewWindow });
  }
}

// Compute the next free ~/Desktop/scratch-<suffix> parent folder name: plain when none exist, else
// the highest existing integer + 1 (an unsuffixed folder counts as 1). Used by quickStart.
function nextScratchParent(desktop: string, kind: Runtime): string {
  const suffix = KIND_SUFFIX[kind];
  const base = `scratch-${suffix}`;
  const re = new RegExp(`^scratch-${suffix}(?:-(\\d+))?$`);
  let max = 0;
  let found = false;
  try {
    for (const entry of fs.readdirSync(desktop, { withFileTypes: true })) {
      if (!entry.isDirectory()) {
        continue;
      }
      const m = re.exec(entry.name);
      if (!m) {
        continue;
      }
      found = true;
      const n = m[1] ? parseInt(m[1], 10) : 1;
      if (n > max) {
        max = n;
      }
    }
  } catch {
    // Desktop missing/unreadable → treat as empty (fall through to the base name).
  }
  return found ? `${base}-${max + 1}` : base;
}

const QUICK_START_KINDS: Record<string, Runtime> = { Babashka: 'bb', Leiningen: 'lein', Deps: 'deps' };
const KIND_SUFFIX: Record<Runtime, string> = { bb: 'bb', lein: 'lein', deps: 'deps' };

// Fireworks: Quick Start — the one-Enter sandbox. Reads the configured project type, computes a
// fresh ~/Desktop/scratch-<type> parent, confirms, then scaffolds a `scratch` project inside it and
// adds it to the workspace. No runtime/parent/name prompts.
async function quickStart(): Promise<void> {
  diagBegin('Quick Start');
  try {
    const typeLabel = vscode.workspace
      .getConfiguration('fireworks')
      .get<string>('quickStart.projectType', 'Babashka');
    const kind = QUICK_START_KINDS[typeLabel] ?? 'bb';
    const desktop = path.join(os.homedir(), 'Desktop');
    const parentName = nextScratchParent(desktop, kind);
    const parentAbs = path.join(desktop, parentName);

    const pick = await vscode.window.showQuickPick([{ label: `Create ${tildify(parentAbs)}?` }], {
      placeHolder: 'Fireworks: Quick Start',
    });
    if (!pick) {
      diagEnd('cancelled');
      return;
    }

    await scaffoldAndOpen(kind, path.join(parentAbs, 'scratch'), 'scratch', 'add');
  } finally {
    diagEnd('ended'); // no-op if a branch already flushed
  }
}

// A fingerprint of the on-disk artifacts the Live Code setup touches, used by preflight to tell
// "already in place" from "just set up". Existence for dirs / seed-once files (.test-refresh.edn,
// watch.clj are never edited, only created); full content for the files setup may edit in place
// (.gitignore, project.clj). A stable string, so a before/after comparison detects any change.
function setupFingerprint(root: string): string {
  const parts: string[] = [];
  const exists = (p: string) => parts.push(fs.existsSync(p) ? '1' : '0');
  const content = (p: string) => parts.push(readFileOrNull(p) ?? '');
  exists(path.join(root, '.fireworks'));
  exists(path.join(root, '.fireworks', 'results'));
  exists(path.join(root, '.fireworks', 'bb', 'watch.clj'));
  exists(path.join(root, '.test-refresh.edn'));
  content(path.join(root, '.gitignore'));
  content(path.join(root, 'project.clj'));
  return parts.join('\0');
}

// Live Code (Preflight): run the same setup the start sequence does — detect the runtime, seed the
// config files and directories, patch the build file where applicable — but stop before choosing a
// terminal or launching anything. If nothing needed creating, tell the user they're good to go.
async function preflightLiveCoding(): Promise<void> {
  const root = await pickProjectRoot();
  if (!root) {
    return;
  }
  const before = setupFingerprint(root);
  // resolveWatcherCommand seeds the runtime's config files as a side effect (.test-refresh.edn,
  // .fireworks/bb/watch.clj, additive project.clj edits) while resolving the launch command; we
  // ignore the command it returns. undefined = aborted or a setup guide was shown — nothing to add.
  const plan = await resolveWatcherCommand(root);
  if (!plan) {
    return;
  }
  // The .fireworks dir / results cache / .gitignore are prepared at launch, not by
  // resolveWatcherCommand — do that here too, minus the result-cache wipe (a launch concern).
  ensureFireworksDir(root);
  ensureResultsDir(root);
  ensureGitignored(root);
  if (setupFingerprint(root) === before) {
    await vscode.window.showInformationMessage(
      'Fireworks: Live Code is already set up for this project — you’re good to go.',
      { modal: true },
    );
  } else {
    flash(`Fireworks: Live Code is set up for ${path.basename(root)}. You’re good to go.`);
  }
}

// Resolve where Live Code should run from. Honors the fireworks.terminalLocation setting; when it's
// "Always ask" (the default) prompts with a two-option pick before the project picker. Returns
// undefined if the user dismisses the pick.
async function resolveTerminalLocation(): Promise<'integrated' | 'external' | undefined> {
  const setting = cfg().get<string>('terminalLocation', 'Always ask');
  diagFact('Terminal location setting', setting);
  if (setting === 'Run from integrated terminal') {
    diagFact('Terminal location', 'Integrated terminal');
    diagStep('Terminal location fixed by setting → integrated (no prompt).');
    return 'integrated';
  }
  if (setting === 'Run from external terminal') {
    diagFact('Terminal location', 'External terminal');
    diagStep('Terminal location fixed by setting → external (no prompt).');
    return 'external';
  }
  const pick = await vscode.window.showQuickPick(
    [
      { label: 'Run from integrated terminal', value: 'integrated' as const },
      { label: 'Run from external terminal', value: 'external' as const },
    ],
    { placeHolder: 'Run Fireworks Live Code from…' },
  );
  diagFact('Terminal location', pick ? (pick.value === 'integrated' ? 'Integrated terminal' : 'External terminal') : null);
  diagStep(`Setting is "Always ask" → prompted → ${pick ? pick.value : 'dismissed'}.`);
  return pick?.value;
}

// A session already exists for the picked root. If its process has exited (the terminal
// is lingering), restart it in place. Otherwise it's genuinely running: reveal it and
// show a modal notice. Starting a second session for the same project is a no-op.
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
// editor's viewport when a session starts or restarts, as a refresh cue, since launching
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
const SWEEP_MAX_COLS = 160; // cap on a horizontal sweep's column count (bounds the frame budget)
const SWEEP_L = 0.72; // oklch lightness for the background tint — a medium tone
const SWEEP_FG_L = 0.85; // brighter lightness for the foreground tint so it pops on dark backgrounds
const SWEEP_C = 0.16; // oklch chroma
// Hues for red, orange, yellow, lime, green, cyan, blue, purple, magenta. The color cycles
// through these along the sweep (row r -> SWEEP_HUES[r % 9] vertical; column c likewise).
const SWEEP_HUES = [25, 55, 95, 130, 150, 200, 260, 305, 340];
// Whether the moving tint colors the cell background, the text foreground, or both. These are
// application-logic switches (no hidden setting yet). The defaults reproduce the original
// background-only sweep. Read once, when the decoration types are first built.
const SWEEP_TINT_BACKGROUND = true;
const SWEEP_TINT_FOREGROUND = true;
// Limit the background tint to cells holding non-blank text (skip whitespace and the empty area
// past end-of-line). When off, a vertical sweep tints whole lines and a horizontal sweep tints
// every column up to each line's length. Application-logic switch (no hidden setting).
const SWEEP_BACKGROUND_TEXT_ONLY = true;
// Skip line-comment lines entirely. No animation on any line whose first non-blank character is a
// `;`. Precomputed once per run, so it costs one test per visible line (not per frame) and trims
// the ranges painted. Application-logic switch (no hidden setting).
const SWEEP_SKIP_LINE_COMMENTS = true;

// [hue][level-1] decoration types: 9 colors x SWEEP_FADE_SPAN opacity levels, in two flavors:
// whole-line tints (one band per row) and single-cell tints (one character cell). The whole-line
// set backs a plain vertical sweep; the cell set backs horizontal sweeps and any sweep limited to
// text cells (SWEEP_BACKGROUND_TEXT_ONLY). isWholeLine is baked into the type, so each flavor is
// cached separately. Level k (1..span) -> alpha (SWEEP_MAX_ALPHA_BACKGROUND/_FOREGROUND) * k/span,
// so the wave front is brightest and the trail dims behind it. Built once, reused, disposed on deactivation.
// Decoration-type sets cached by flavor + slot. wholeLine bakes isWholeLine into the type;
// `slot` lets two sweeps running at once (the cross tracer's vertical + horizontal bands) own
// distinct type instances, so their per-frame setDecorations don't overwrite each other.
const sweepTypeCache = new Map<string, vscode.TextEditorDecorationType[][]>();

// A running sweep. Several can run at once (the cross tracer), so the state that used to be
// module-global lives per handle; liveSweeps tracks them for stop-all and teardown.
interface Sweep {
  timer: ReturnType<typeof setInterval>;
  editor: vscode.TextEditor; // the editor this sweep paints
  types: vscode.TextEditorDecorationType[][]; // the type set this sweep paints
}
const liveSweeps = new Set<Sweep>();

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

function ensureSweepTypes(wholeLine: boolean, slot = 0): vscode.TextEditorDecorationType[][] {
  const key = `${wholeLine ? 'line' : 'cell'}-${slot}`;
  const cached = sweepTypeCache.get(key);
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
  sweepTypeCache.set(key, built);
  return built;
}

// Clear every decoration of one sweep's type set from its editor.
function clearSweepDecorations(sweep: Sweep): void {
  for (const row of sweep.types) {
    for (const t of row) {
      sweep.editor.setDecorations(t, []);
    }
  }
}

// Stop one sweep: cancel its timer, wipe its bands, drop it from the live set. Safe to call
// more than once on the same handle (delete from the set is idempotent).
function stopSweep(sweep: Sweep): void {
  clearInterval(sweep.timer);
  clearSweepDecorations(sweep);
  liveSweeps.delete(sweep);
}

// Stop every in-flight sweep (so starting a new tracer leaves no stray bands). The single-
// direction commands call this first to keep today's "new sweep replaces the old" behavior;
// the cross tracer calls it once, then starts both axes, which then coexist.
function stopAllSweeps(): void {
  for (const sweep of [...liveSweeps]) {
    stopSweep(sweep);
  }
}

// Tear-down for deactivation: stop all sweeps and dispose every cached type set.
function disposeSweep(): void {
  stopAllSweeps();
  for (const set of sweepTypeCache.values()) {
    for (const row of set) {
      for (const t of row) {
        t.dispose();
      }
    }
  }
  sweepTypeCache.clear();
}

// Drive a sweep: each tick refills the per-(color, level) range buckets via `paintFrame` and
// pushes them to the editor, for `totalSteps` ticks `step` ms apart, then clears. Ranges key off
// document positions, so the sweep stays put even though launching focuses the terminal.
//
// Perf: there are 9*SWEEP_FADE_SPAN decoration types but only a handful hold ranges on any frame
// (the trail). So (a) the buckets are allocated once and cleared in place, with no per-frame garbage,
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
  const buckets = types.map((row) => row.map(() => [] as vscode.Range[]));
  const dirty = types.map((row) => row.map(() => false)); // which types currently hold ranges
  let s = 0;
  // The interval references `sweep`, and `sweep` needs the timer — the callback only runs after
  // `step` ms, by which point the const below is assigned, so the forward reference is safe.
  const timer = setInterval(() => {
    if (!vscode.window.visibleTextEditors.includes(editor)) {
      stopSweep(sweep); // editor closed or scrolled out of view — stop wasting frames
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
      stopSweep(sweep);
    }
  }, step);
  const sweep: Sweep = { timer, editor, types };
  liveSweeps.add(sweep);
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

// Which sweep the Live Code startup animation plays. Compile-time knob (not a user setting):
//   'cross'      — the simultaneous top-to-bottom + left-to-right plaid sweep (current default)
//   'configured' — defer to the hidden liveCoding.startupAnimationDirection setting (prior behavior)
//   a direction  — force that single direction, e.g. 'top-to-bottom'
const startupSweepFlavor: 'cross' | 'configured' | SweepDirection = 'cross';

// Play the startup sweep on `editor` per startupSweepFlavor. A fading rainbow band travels
// across the viewport over ~SWEEP_TOTAL_MS, then clears.
function playStartupSweep(editor: vscode.TextEditor): void {
  stopAllSweeps();
  if (startupSweepFlavor === 'cross') {
    playVerticalSweep(editor, false, 0);
    playHorizontalSweep(editor, false, 1);
    return;
  }
  const dir = startupSweepFlavor === 'configured' ? startupAnimDirection() : startupSweepFlavor;
  switch (dir) {
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

function rainbowTracerVertical(): void {
  const editor = vscode.window.activeTextEditor;
  if (editor) {
    stopAllSweeps();
    playVerticalSweep(editor, false);
  }
}

function rainbowTracerHorizontal(): void {
  const editor = vscode.window.activeTextEditor;
  if (editor) {
    stopAllSweeps();
    playHorizontalSweep(editor, false);
  }
}

// Cross tracer: a top-to-bottom and a left-to-right sweep at once. Each axis owns a distinct
// decoration-type slot so their per-frame paints don't clobber each other; where the two bands
// cross, the decorations stack and their alphas blend — the "plaid" wavefront.
function rainbowTracerCross(): void {
  const editor = vscode.window.activeTextEditor;
  if (editor) {
    stopAllSweeps();
    playVerticalSweep(editor, false, 0);
    playHorizontalSweep(editor, false, 1);
  }
}

// Vertical sweep. reversed=false runs top -> bottom, true runs bottom -> top. Each visible row
// lights as the front reaches it (color stays anchored per row: SWEEP_HUES[r % 9]) and fades over
// the next SWEEP_FADE_SPAN rows. Paced from the row count. With SWEEP_BACKGROUND_TEXT_ONLY the row
// tints only its non-blank text runs (cell types); otherwise the whole line (whole-line types).
function playVerticalSweep(editor: vscode.TextEditor, reversed: boolean, slot = 0): void {
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
  runSweep(editor, ensureSweepTypes(!textOnly, slot), rows + SWEEP_FADE_SPAN, step, (s, buckets) => {
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
function playHorizontalSweep(editor: vscode.TextEditor, reversed: boolean, slot = 0): void {
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
  runSweep(editor, ensureSweepTypes(false, slot), cols + SWEEP_FADE_SPAN, step, (s, buckets) => {
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
  runtime?: Runtime,
  fireworksTheme?: FireworksThemeDecision, // pre-resolved + logged by the caller (so it lands in the diag
  // tree, which the command flushes before calling here); resolved + logged here when omitted.
): Promise<void> {
  const animEditor = vscode.window.activeTextEditor; // capture before terminal.show() steals focus
  prepareResultsDir(root); // on-disk fresh slate for this root
  clearResultsForRoot(root); // in-memory fresh slate for this root only (not other projects')
  recordRecentRoot(root); // float this project in the pickers' "Recent" section
  log(`live coding -> ${command} (cwd ${root})`);
  // Force the watcher's Fireworks output mood to match the editor, as a visible shell-appropriate
  // FIREWORKS_THEME prefix on the command (recomputed per launch so a theme switch is picked up on
  // restart).
  if (!fireworksTheme) {
    fireworksTheme = resolveFireworksTheme();
    logFireworksThemeDecision(fireworksTheme);
  }
  const launchCommand = envVarPrefix('FIREWORKS_THEME', fireworksTheme.value) + command;
  const terminal = vscode.window.createTerminal({ name: terminalName(root), cwd: root });
  // session.command stays the bare command (restart re-derives the prefix from the current mood).
  const session: LiveSession = { root, command, terminal, execution: undefined, testRefresh, runtime };
  liveSessions.set(root, session);
  terminal.show();
  if (animEditor && startupAnimEnabled()) {
    playStartupSweep(animEditor); // fire-and-forget; the shell-integration wait runs in parallel
  }

  const si = await waitForShellIntegration(terminal);
  if (liveSessions.get(root) !== session) {
    return; // closed/replaced during wait
  }
  await sendWatcherCommand(session, si, launchCommand);
  if (runtime === 'bb') {
    scheduleBbStartupNudge(session, animEditor); // light up the active file's inline results
  }
}

// The bb watcher only paints results in response to a file save (its fswatcher pod fires on a write);
// nothing runs at startup. So a few seconds after launch, nudge the file the user was looking at so its
// `?` results light up without the user having to save first. Best-effort and bb-only (deps/lein
// test-refresh runs on startup). Skipped when the session was stopped/replaced during the wait, or the
// nudge is disabled (BB_STARTUP_NUDGE_MS = 0).
function scheduleBbStartupNudge(session: LiveSession, editor: vscode.TextEditor | undefined): void {
  if (BB_STARTUP_NUDGE_MS <= 0) {
    return;
  }
  const doc = editor?.document;
  const file = doc?.uri.fsPath;
  if (!doc || !file || !file.startsWith(session.root + path.sep) || !/\.(clj[cs]?|bb)$/.test(file)) {
    return; // no active Clojure file in this project to nudge
  }
  setTimeout(() => {
    if (liveSessions.get(session.root) !== session) {
      return; // stopped/replaced during the wait
    }
    void nudgeBbFile(doc, file);
  }, BB_STARTUP_NUDGE_MS);
}

// Trigger one bb watcher reload so the file's inline results paint. If the buffer has unsaved edits,
// save it (writes the latest to disk → the watcher reloads it); otherwise touch the file on disk
// (rewrite its own bytes), since a clean save is a no-op that wouldn't fire the watcher. Best-effort.
async function nudgeBbFile(doc: vscode.TextDocument, file: string): Promise<void> {
  try {
    if (!doc.isClosed && doc.isDirty) {
      await doc.save(); // real write of the user's edits → watcher reload
      log(`bb startup nudge: saved ${tildify(file)} to light up inline results`);
    } else {
      fs.writeFileSync(file, fs.readFileSync(file)); // touch: identical bytes, but triggers the watcher
      log(`bb startup nudge: touched ${tildify(file)} to light up inline results`);
    }
  } catch (e) {
    log(`bb startup nudge failed for ${tildify(file)}: ${String(e)}`);
  }
}

// Stop a session: Ctrl-C so test-refresh shuts down cleanly, then dispose the terminal
// (onDidCloseTerminal removes it from the map; the eager delete covers the event lag). Also clears
// the stopped project's inline results (in-memory cache + decorations, per-root so other running
// projects are untouched); a restart re-clears via launchWatcher, so the flow stays clean.
function stopSession(session: LiveSession): void {
  session.terminal.sendText('\u0003'); // Ctrl-C: let test-refresh shut down cleanly
  session.terminal.dispose();
  liveSessions.delete(session.root);
  clearResultsForRoot(session.root); // clear this project's inline results on stop
}

// Shown when a Live Code command needs a running (integrated-terminal) session but none is tracked.
// The extension only tracks sessions it launched in an integrated terminal, so a watcher the user
// started in their own external terminal is invisible here — hence the hint.
const NO_SESSION_MSG =
  'Fireworks: no running Live Code session detected. ' +
  'Maybe you’re running the project from an external terminal?';

// The tracked (integrated-terminal) sessions, as a fact + step, for stop/restart diagnostics.
function diagSessions(): void {
  const roots = [...liveSessions.keys()].map((r) => tildify(r));
  diagFact('Tracked sessions (integrated)', roots);
}

async function stopLiveCoding(): Promise<void> {
  diagBegin('Live Code (Stop)');
  try {
    diagSessions();
    if (liveSessions.size === 0) {
      diagStep('No tracked sessions → show the "maybe external terminal?" hint.');
      diagEnd('no tracked session');
      flash(NO_SESSION_MSG);
      return;
    }
    const session = await chooseSession('stop');
    if (session) {
      stopSession(session);
      diagStep(`Stopped ${tildify(session.root)} (Ctrl-C + disposed the terminal).`);
      diagEnd('stopped');
    } else {
      diagStep('Session pick dismissed → nothing stopped.');
      diagEnd('cancelled');
    }
  } finally {
    diagEnd('ended');
  }
}

async function restartLiveCoding(): Promise<void> {
  diagBegin('Live Code (Restart)');
  try {
    diagSessions();
    if (liveSessions.size === 0) {
      diagStep('No tracked sessions → show the "maybe external terminal?" hint.');
      diagEnd('no tracked session');
      flash(NO_SESSION_MSG);
      return;
    }
    const session = await chooseSession('restart');
    if (!session) {
      diagStep('Session pick dismissed → nothing restarted.');
      diagEnd('cancelled');
      return;
    }
    const { root, command, testRefresh, runtime } = session;
    diagFact('Reused command', command);
    diagFact('Reused .test-refresh source', testRefresh ? testRefresh.source : null);
    // Re-resolve + log FIREWORKS_THEME here (editor theme may have changed since the original launch),
    // while the diag run is still open, then hand it to launchWatcher.
    const fireworksTheme = resolveFireworksTheme();
    logFireworksThemeDecision(fireworksTheme);
    diagStep(`Restart ${tildify(root)}: stop, then relaunch the same command (no re-prompt).`);
    diagEnd('restarted');
    stopSession(session);
    await launchWatcher(root, command, testRefresh, runtime, fireworksTheme); // reuse root + command + config
  } finally {
    diagEnd('ended');
  }
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
    diagFact('Chosen session', tildify(all[0].root));
    diagStep(`Only one tracked session → auto-selected ${tildify(all[0].root)}.`);
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
  const chosen = (pick as SessionPickItem | undefined)?.session;
  diagFact('Chosen session', chosen ? tildify(chosen.root) : null);
  diagStep(chosen ? `Chose session ${tildify(chosen.root)} from ${all.length}.` : `Session pick dismissed (${all.length} running).`);
  return chosen;
}

// Preset auto-save delays (seconds) offered by fireworks.setAutoSaveDelay. They tune how
// quickly a save lands after you stop typing, i.e. how snappy the test-refresh/inline loop
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

// The fireworks.terminalLocation values, shared verbatim by the package.json enum, this picker,
// and the start-flow "Always ask" prompt — no value↔label mapping.
const TERMINAL_LOCATIONS = [
  'Always ask',
  'Run from integrated terminal',
  'Run from external terminal',
] as const;

// Pick a terminal location and write it to fireworks.terminalLocation (user settings).
async function setTerminalLocation(): Promise<void> {
  const current = cfg().get<string>('terminalLocation', 'Always ask');
  const pick = await vscode.window.showQuickPick(
    TERMINAL_LOCATIONS.map((label) => ({
      label,
      description: label === current ? '(current)' : undefined,
    })),
    { placeHolder: 'Where should Fireworks: Live Code run the watcher?' },
  );
  if (!pick) {
    return;
  }
  await cfg().update('terminalLocation', pick.label, vscode.ConfigurationTarget.Global);
  flash(`Fireworks: terminal location set to “${pick.label}”.`);
}

// The global locations Fireworks/Bling auto-discover a printing config, in precedence order.
function printingConfigPaths(): string[] {
  const home = os.homedir();
  return [
    path.join(home, '.config', 'fireworks', 'config.edn'),
    path.join(home, '.config', 'bling', 'config.edn'),
  ];
}

// Edit the user's existing global printing config if one is already present at either discovery
// path; otherwise seed a fresh one from the bundled example (the canonical
// docs/example-bling-config/config.edn, copied into resources/ at build time). The config is global
// — Fireworks/Bling auto-discover it at ~/.config/{fireworks,bling}/config.edn — so for the create
// path we let the user choose the destination, defaulting the Save dialog to the fireworks path.
async function createPrintingConfig(): Promise<void> {
  const existing = printingConfigPaths().find((p) => fs.existsSync(p));
  if (existing) {
    const doc = await vscode.workspace.openTextDocument(vscode.Uri.file(existing));
    await vscode.window.showTextDocument(doc);
    return;
  }
  const assetUri = vscode.Uri.joinPath(extContext.extensionUri, 'resources', 'example-config.edn');
  let contents: Uint8Array;
  try {
    contents = await vscode.workspace.fs.readFile(assetUri);
  } catch {
    flash('Fireworks: could not read the bundled example config.');
    return;
  }
  const defaultUri = vscode.Uri.file(printingConfigPaths()[0]);
  const dest = await vscode.window.showSaveDialog({
    defaultUri,
    filters: { EDN: ['edn'] },
    saveLabel: 'Create config.edn',
    title: 'Create a printing options config.edn',
  });
  if (!dest) {
    return;
  }
  // workspace.fs.writeFile creates any missing parent directories (e.g. ~/.config/fireworks).
  await vscode.workspace.fs.writeFile(dest, contents);
  const doc = await vscode.workspace.openTextDocument(dest);
  await vscode.window.showTextDocument(doc);
  flash(`Fireworks: created ${tildify(dest.fsPath)}.`);
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
// setting on every step. Only the option you settle on (a beat after you stop) is applied.
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
const INLINE_BG_OPACITIES = [0, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1, 0.12, 0.16, 0.18, 0.22, 0.26, 0.30];
const INLINE_FG_OPACITIES = [1, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65];
const INLINE_GAP = [1, 2, 4, 6, 8, 10, 12, 14, 16];
const INLINE_MAX_LENGTH = [16, 24, 32, 40, 52, 70, 100, 200];
const INLINE_FADE = [0, 40, 80, 120, 200, 300, 450];

// Label formatters for the scales above. The stored value is unchanged; only the
// QuickPick label differs. Math.round because 0.07 * 100 === 7.000000000000001 in JS.
const asPercent = (v: string | number) => `${Math.round(Number(v) * 100)}%`;
const asMs = (v: string | number) => `${v}ms`;

// Build the live-preview items for a preset scale, tagging the active value "(current)".
// formatLabel defaults to the raw value, so callers that don't pass one are unchanged.
function previewItems(
  values: (string | number)[],
  current: string | number,
  formatLabel: (v: string | number) => string = (v) => `${v}`,
): PreviewItem[] {
  return values.map((v) => ({
    label: formatLabel(v),
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

// Edits the named variant's background-opacity setting (light or dark), independent of the
// active theme — each variant has its own setting and its own command. The live preview only
// shows on screen when `variant` matches the active theme (the decoration reads the active
// theme's variant); the off-theme variant is still updated, just without a visible preview.
async function setInlineResultsBackgroundOpacity(variant: 'light' | 'dark'): Promise<void> {
  const key = `inlineResults.backgroundOpacity.${variant}`;
  const current = cfg().get<number>(key, DEFAULT_BG_OPACITY[variant]);
  await pickWithLivePreview(
    key,
    `Inline Results Background Opacity — ${variant} theme`,
    previewItems(INLINE_BG_OPACITIES, current, asPercent),
    current,
  );
}

async function setInlineResultsOpacity(): Promise<void> {
  const current = cfg().get<number>('inlineResults.opacity', 0.75);
  await pickWithLivePreview(
    'inlineResults.opacity',
    'Inline Results Opacity',
    previewItems(INLINE_FG_OPACITIES, current, asPercent),
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
    previewItems(INLINE_FADE, current, asMs),
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
