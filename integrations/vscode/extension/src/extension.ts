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
import * as cljsLib from '../lib/cljs-lib';
import type { EditPlan } from '../lib/cljs-lib';

let output: vscode.OutputChannel;

export function activate(context: vscode.ExtensionContext): void {
  output = vscode.window.createOutputChannel('Fireworks');
  context.subscriptions.push(
    output,
    vscode.commands.registerCommand('fireworks.toggle', () => runToggle('?')),
    vscode.commands.registerCommand('fireworks.toggleTap', () => runToggle('?>')),
    vscode.commands.registerCommand('fireworks.toggleIgnore', () => runToggle('#_')),
  );
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
