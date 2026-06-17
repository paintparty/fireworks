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
  statusBar.command = 'fireworks.toggleLiveCodingMode';

  context.subscriptions.push(
    output,
    statusBar,
    vscode.commands.registerCommand('fireworks.toggle', () => runToggle('?')),
    vscode.commands.registerCommand('fireworks.toggleTap', () => runToggle('?>')),
    vscode.commands.registerCommand('fireworks.toggleIgnore', () => runToggle('#_')),
    vscode.commands.registerCommand('fireworks.addRequire', () => runAddRequire()),
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
  );

  void refreshRoots();
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

function currentMode(root: string): 'tap' | 'test' | undefined {
  const text = readFileOrNull(optionsPath(root));
  if (text === null) {
    return undefined;
  }
  const result = cljsLib.readMode(text);
  return result.error ? undefined : result.mode;
}

// Shown whenever the workspace has at least one deps.edn project. Click toggles mode.
function updateStatusBar(): void {
  if (knownRoots.length === 0) {
    statusBar.hide();
    return;
  }
  const running = liveTerminal !== undefined;
  let mode: 'tap' | 'test' | undefined;
  if (running && activeRoot) {
    mode = currentMode(activeRoot);
  } else if (knownRoots.length === 1) {
    mode = currentMode(knownRoots[0]);
  }
  statusBar.text = running ? `$(flame) ${mode ?? 'tap'}` : '$(flame) live (off)';
  statusBar.tooltip = running
    ? `Fireworks live coding: ${mode ?? 'tap'} mode, running. Click to toggle mode.`
    : 'Fireworks live coding: stopped. Click to toggle mode.';
  statusBar.show();
}
