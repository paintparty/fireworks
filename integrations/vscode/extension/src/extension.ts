// Copyright (c) Jeremiah Coyle
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License 2.0 which is available at
// http://www.eclipse.org/legal/epl-2.0, or the GNU General Public License,
// version 2 or any later version with the GNU Classpath Exception which is
// available at https://www.gnu.org/software/classpath/license.html.
//
// SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';
import * as cljsLib from '../lib/cljs-lib';
import type { EditPlan, RequireEdit } from '../lib/cljs-lib';

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
    vscode.commands.registerCommand('fireworks.addRequire', () => runAddRequire()),
    vscode.commands.registerCommand('fireworks.toggleInlineResults', () => toggleInlineResults()),
    vscode.commands.registerCommand('fireworks.clearInlineResults', () => clearInlineResults()),
    vscode.commands.registerCommand('fireworks.startLiveCoding', () => startLiveCoding()),
    vscode.commands.registerCommand('fireworks.stopLiveCoding', () => stopLiveCoding()),
    vscode.commands.registerCommand('fireworks.restartLiveCoding', () => restartLiveCoding()),
    vscode.commands.registerCommand('fireworks.toggleLiveCodingMode', () => toggleLiveCodingMode()),
    vscode.window.onDidCloseTerminal((t) => {
      if (t === liveTerminal) {
        liveTerminal = undefined;
        updateStatusBar();
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

  const range = new vscode.Range(
    new vscode.Position(plan.replaceRange.start.line, plan.replaceRange.start.col),
    new vscode.Position(plan.replaceRange.end.line, plan.replaceRange.end.col),
  );

  // editor.edit applies the wrap/unwrap/invert text.
  const ok = await editor.edit(
    (b) => b.replace(range, plan!.insertText),
    { undoStopBefore: true, undoStopAfter: false },
  );
  if (!ok) {
    return;
  }

  if (isVimActive()) {
    await vscode.commands.executeCommand('extension.vim_escape'); // Vim's Escape nudges the cursor...
  }

  // Place the cursor (and align) on a later tick rather than synchronously here.
  // Ported from the Joyride scripts, which set the cursor inside a 50ms setTimeout
  // after editor.edit() and worked reliably. The async/await version that set
  // editor.selection synchronously lost a race with vscodevim, which re-asserts
  // its own cursor after the edit and left the caret mid-form. If a synchronous,
  // race-free version can be found later, prefer it.
  setTimeout(() => {
    const cursor = new vscode.Position(plan!.newCursor.line, plan!.newCursor.col);
    editor.selection = new vscode.Selection(cursor, cursor); // ...so we set our cursor after it
    if (plan!.reformat) {
      void vscode.commands.executeCommand('calva-fmt.alignCurrentForm'); // keys off cursor, runs last
    }
  }, 50);
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
let docChangeSub: vscode.Disposable | undefined;
const fadeTimers = new Map<string, ReturnType<typeof setTimeout>[]>(); // in-flight fade per editor
// Values observed *this session* only: result file fsPath -> value text. Painting
// reads from here, never straight from disk, so stale files left on disk from a
// previous run are never shown (e.g. opening a file with no watcher running). The
// watcher populates it on writes; cleared on start/stop and by Clear Inline Results.
const resultsCache = new Map<string, string>();

function inlineResultsEnabled(): boolean {
  return vscode.workspace.getConfiguration('fireworks').get<boolean>('inlineResults.enabled', false);
}

function clamp(n: number, lo: number, hi: number): number {
  return Number.isNaN(n) ? lo : Math.min(hi, Math.max(lo, n));
}

// The decoration foreground: the user's configured color if set, else the editor's
// own foreground (a neutral default that adapts to light/dark themes).
function inlineColor(): string | vscode.ThemeColor {
  const c = cfg().get<string>('inlineResults.color', '').trim();
  return c ? c : new vscode.ThemeColor('editor.foreground');
}

// Resting opacity of the value, floored at 0.5 so it stays legible.
function valueOpacity(): number {
  return clamp(cfg().get<number>('inlineResults.opacity', 0.75), 0.5, 1);
}

// A faint background tint behind the value: the inline color mixed into transparent
// at backgroundOpacity (0–10%). Uses currentColor for the themed default so it
// follows the foreground. undefined when the opacity is 0.
function inlineBackground(color: string | vscode.ThemeColor): string | undefined {
  const p = clamp(cfg().get<number>('inlineResults.backgroundOpacity', 0.01), 0, 0.1);
  if (p <= 0) {
    return undefined;
  }
  const base = typeof color === 'string' ? color : 'currentColor';
  return `color-mix(in srgb, ${base} ${+(p * 100).toFixed(3)}%, transparent)`;
}

const PREFIX = '┊ ';
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
      backgroundColor: inlineBackground(color),
      textDecoration: `none; opacity: ${valueOpacity()}`,
    },
  });
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
    inlineDecoration?.dispose();
    inlineDecoration = makeInlineDecoration();
    for (const ed of vscode.window.visibleTextEditors) {
      repaintEditor(ed);
    }
  });
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
  docChangeSub?.dispose();
  docChangeSub = undefined;
  for (const timers of fadeTimers.values()) {
    for (const t of timers) {
      clearTimeout(t);
    }
  }
  fadeTimers.clear();
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

// A result file changed. Its path is <root>/.fireworks/results/<ns>/<line>:<col>.
// Record the write (or eviction) in the session cache — this is the only place a
// value enters it — then repaint every visible editor whose parsed ns matches.
function onResultChanged(uri: vscode.Uri): void {
  const ns = namespaceFromResultPath(uri.fsPath);
  if (!ns) {
    return;
  }
  const raw = readFileOrNull(uri.fsPath);
  if (raw === null) {
    resultsCache.delete(uri.fsPath); // deleted or unreadable
  } else {
    resultsCache.set(uri.fsPath, raw);
  }
  for (const ed of vscode.window.visibleTextEditors) {
    if (ed.document.languageId !== 'clojure') {
      continue;
    }
    if (cljsLib.analyzeInlineResults(ed.document.getText()).namespace === ns) {
      repaintEditor(ed);
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
  const { namespace, positions } = cljsLib.analyzeInlineResults(editor.document.getText());
  const root = rootFor(editor.document.uri.fsPath);
  if (!namespace || !root || positions.length === 0) {
    cancelFade(editor);
    editor.setDecorations(inlineDecoration, []);
    return;
  }

  const maxLength = cfg().get<number>('inlineResults.maxLength', 80);
  const budget = maxLength > 0 ? maxLength : Infinity; // total chars shared per row
  const gap = cfg().get<number>('inlineResults.gap', 17);
  const nbsp = (n: number): string => '\u00a0'.repeat(Math.max(0, n));

  // Group results by render row (end row). positions arrive in source order, so
  // each row's values stay left-to-right.
  const byRow = new Map<number, string[]>();
  for (const pos of positions) {
    const line = pos.row - 1; // 1-based end row -> 0-based render line
    if (!Number.isInteger(line) || line < 0 || line >= editor.document.lineCount) {
      continue;
    }
    // The result file is keyed by the form's start position (pos.key), but the
    // decoration is anchored on its end row (pos.row). Read from the session cache,
    // not disk, so only values written since start (never stale leftovers) paint.
    const raw = resultsCache.get(path.join(root, RESULTS_DIR, namespace, pos.key));
    if (raw === undefined) {
      continue;
    }
    const arr = byRow.get(line) ?? [];
    arr.push(singleLine(raw));
    byRow.set(line, arr);
  }

  // One decoration per row: up to MAX_RESULTS_PER_LINE values (the budget split as
  // evenly as possible across them) then a "+ n more" tail. The first value uses the
  // configured gap; later ones a fixed gap. All in one element to keep order stable
  // (VS Code groups multiple same-position attachments, breaking left-to-right order).
  const options: vscode.DecorationOptions[] = [];
  for (const [line, values] of byRow) {
    const shown = values.slice(0, MAX_RESULTS_PER_LINE).reverse();
    const overflow = values.length - shown.length;
    const alloc = allocateEven(shown.map((v) => v.length), budget);
    let content = '';
    shown.forEach((v, i) => {
      content += nbsp(i === 0 ? gap : SUBSEQUENT_GAP) + PREFIX + truncateTo(v, alloc[i]);
    });
    if (overflow > 0) {
      content += nbsp(SUBSEQUENT_GAP) + OVERFLOW_PREFIX + `+ ${overflow} more`;
    }
    const eol = editor.document.lineAt(line).range.end;
    options.push({
      // Non-breaking spaces: VS Code collapses runs of normal spaces in contentText.
      range: new vscode.Range(eol, eol),
      renderOptions: { after: { contentText: content } },
    });
  }
  paintWithFade(editor, options);
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

// Same options but with a per-range opacity override — the per-range
// after.textDecoration overrides the type's while leaving its color in place.
function withOpacity(o: vscode.DecorationOptions, opacity: number): vscode.DecorationOptions {
  return {
    range: o.range,
    renderOptions: {
      after: {
        ...(o.renderOptions?.after ?? {}),
        textDecoration: `none; opacity: ${opacity}`,
      },
    },
  };
}

// Paint `options`, fading opacity 0 -> the configured value opacity over fadeInMs
// in a few frames. fadeInMs <= 0 paints at full opacity immediately (no animation).
function paintWithFade(editor: vscode.TextEditor, options: vscode.DecorationOptions[]): void {
  if (!inlineDecoration) {
    return;
  }
  cancelFade(editor);
  const fadeMs = cfg().get<number>('inlineResults.fadeInMs', 90);
  if (fadeMs <= 0 || options.length === 0) {
    editor.setDecorations(inlineDecoration, options);
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
              options.map((o) => withOpacity(o, opacity)),
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
// The test-refresh + Fireworks deps are injected at launch via `clojure -Sdeps`,
// so Fireworks never edits the project's deps.edn. The only file it writes is a
// project .test-refresh.edn (created if missing), holding the watcher options and
// the tap/test mode. TS owns the side effects (file I/O, terminal, status bar);
// the pure .test-refresh.edn edits come from fireworks-vscode.config via the bridge.
// deps.edn projects only — Leiningen is out of scope (set that up by hand).
// ============================================================================

const TAP_BANNER = '🔥🔥🔥🔥🔥🔥🔥🔥🔥';
const TEST_BANNER = '📋 Running tests...';
const TERMINAL_NAME = 'Fireworks Live Coding';
const ALIAS = 'fireworks/live';

let liveTerminal: vscode.Terminal | undefined;
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

// Resolve which project root to act on: the only one, or a Calva-style pick when the
// workspace has several. undefined if none, or the pick was dismissed.
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
      label: vscode.workspace.asRelativePath(root, true),
      description: 'deps.edn',
      root,
    })),
    { placeHolder: 'Pick the Clojure project Fireworks should use as the root' },
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

function optionsPath(root: string): string {
  return path.join(root, '.test-refresh.edn');
}

// Create the project .test-refresh.edn from tap defaults if missing. We never modify
// an existing one here — it is the user's source of truth for the watcher options.
function ensureOptions(root: string): void {
  const file = optionsPath(root);
  if (fs.existsSync(file)) {
    return;
  }
  fs.writeFileSync(file, cljsLib.defaultConfig('tap'), 'utf8');
  log(`created ${file}`);
}

// Forms that intern Fireworks' ?/!? into clojure.core at startup (macro metadata
// preserved via with-meta), so they resolve unqualified in reloaded code. Uses
// (quote ...) and no double quotes, so it nests cleanly inside the -Sdeps EDN's
// :main-opts and the shell's single quotes. Wrapped in try so a load failure can't
// stop the watcher from starting.
const INJECT_FORMS =
  '(try (require (quote fireworks.core))' +
  ' (doseq [s (quote [? !? ?> !?>])]' +
  ' (when-let [v (ns-resolve (quote fireworks.core) s)]' + // skip any macro not in this version
  ' (intern (quote clojure.core) (with-meta s (meta v)) (deref v))))' +
  ' (catch Throwable e (.printStackTrace e)))';

// The watcher command: inject test-refresh + Fireworks via -Sdeps so deps.edn is
// untouched; :extra-paths ["test"] keeps the project's tests on the classpath. When
// injectMacros is on, an -e step runs INJECT_FORMS before -m starts test-refresh.
function watchCommand(): string {
  const fw = cfg().get<string>('liveCoding.fireworksVersion', '0.20.0');
  const tr = cfg().get<string>('liveCoding.testRefreshVersion', '0.26.0');
  const inject = cfg().get<boolean>('liveCoding.injectMacros', false);
  const mainOpts = inject
    ? `["-e" "${INJECT_FORMS}" "-m" "com.jakemccrary.test-refresh"]`
    : '["-m" "com.jakemccrary.test-refresh"]';
  const sdeps =
    `{:aliases {:${ALIAS} ` +
    `{:extra-paths ["test"] ` +
    `:extra-deps {com.jakemccrary/test-refresh {:mvn/version "${tr}"} ` +
    `io.github.paintparty/fireworks {:mvn/version "${fw}"}} ` +
    `:main-opts ${mainOpts}}}}`;
  return `clojure -Sdeps '${sdeps}' -M:${ALIAS}`;
}

function terminalPref(): 'integrated' | 'external' {
  return cfg().get<'integrated' | 'external'>('liveCoding.terminal', 'integrated');
}

async function startLiveCoding(): Promise<void> {
  if (liveTerminal) {
    liveTerminal.show(); // already running
    return;
  }
  const root = await pickProjectRoot();
  if (!root) {
    return;
  }
  if (!preflight()) {
    return;
  }
  ensureOptions(root);
  launchWatcher(root);
}

// Run the watcher for a known root. Records it as the active session so
// Stop/Restart/Toggle act on the same project without re-asking.
function launchWatcher(root: string): void {
  activeRoot = root;
  const command = watchCommand();
  log(`live coding -> ${command} (cwd ${root})`);

  if (terminalPref() === 'external') {
    startExternal(root, command);
    updateStatusBar();
    return;
  }

  liveTerminal = vscode.window.createTerminal({ name: TERMINAL_NAME, cwd: root });
  liveTerminal.show();
  liveTerminal.sendText(command);
  updateStatusBar();
}

function startExternal(root: string, command: string): void {
  const template = cfg().get<string>('liveCoding.externalCommand', '').trim();
  if (!template) {
    vscode.window.showErrorMessage(
      'Fireworks: set fireworks.liveCoding.externalCommand to use external-terminal mode.',
    );
    return;
  }
  const filled = template.replace(/\$\{cwd\}/g, root).replace(/\$\{command\}/g, command);
  log(`external terminal: ${filled}`);
  cp.exec(filled, (err) => {
    if (err) {
      vscode.window.showErrorMessage(`Fireworks: external terminal launch failed: ${err.message}`);
    }
  });
}

// In external mode the watcher is detached, so we have no handle to stop it.
function externalModeBlocked(): boolean {
  if (terminalPref() === 'external' && !liveTerminal) {
    vscode.window.showInformationMessage(
      "Fireworks: Stop/Restart aren't available in external-terminal mode — use Ctrl-C in your terminal, or switch to integrated mode.",
    );
    return true;
  }
  return false;
}

function stopLiveCoding(): void {
  if (externalModeBlocked()) {
    return;
  }
  if (!liveTerminal) {
    return;
  }
  liveTerminal.sendText('\u0003'); // Ctrl-C: let test-refresh shut down cleanly
  liveTerminal.dispose();
  liveTerminal = undefined;
  updateStatusBar();
}

async function restartLiveCoding(): Promise<void> {
  if (externalModeBlocked()) {
    return;
  }
  const root = activeRoot;
  stopLiveCoding();
  if (root) {
    launchWatcher(root); // reuse the running session's root (no re-prompt)
  } else {
    await startLiveCoding();
  }
}

// Read the current mode, flip it in .test-refresh.edn, and restart if running
// (test-refresh reads its config only at startup).
async function toggleLiveCodingMode(): Promise<void> {
  const root = activeRoot ?? (await pickProjectRoot());
  if (!root) {
    return;
  }
  const file = optionsPath(root);
  const text = readFileOrNull(file);
  if (text === null) {
    vscode.window.showErrorMessage('Fireworks: .test-refresh.edn not found — start live coding first.');
    return;
  }
  const modeResult = cljsLib.readMode(text);
  if (modeResult.error || !modeResult.mode) {
    vscode.window.showErrorMessage('Fireworks: could not read the mode from .test-refresh.edn.');
    return;
  }
  const next = modeResult.mode === 'tap' ? 'test' : 'tap';
  const result = cljsLib.setMode(text, next, { tapBanner: TAP_BANNER, testBanner: TEST_BANNER });
  if (result.error || result.text === undefined) {
    vscode.window.showErrorMessage('Fireworks: could not update .test-refresh.edn.');
    return;
  }
  fs.writeFileSync(file, result.text, 'utf8');
  updateStatusBar();
  if (liveTerminal) {
    await restartLiveCoding();
  } else {
    vscode.window.showInformationMessage(`Fireworks: live coding mode set to ${next}.`);
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
