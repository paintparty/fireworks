// Copies build-time assets that live outside the extension folder into resources/ so they ship in
// the esbuild bundle's package. Keep the destinations as the single runtime source; the repo-root
// originals stay canonical (author-owned). Run by `compile` and `vscode:prepublish`.
import { copyFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const extDir = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = resolve(extDir, '../../..');

const assets = [
  // [ canonical source (repo-relative), bundled destination (extension-relative) ]
  ['docs/example-bling-config/config.edn', 'resources/example-config.edn'],
];

for (const [src, dest] of assets) {
  const from = resolve(repoRoot, src);
  const to = resolve(extDir, dest);
  mkdirSync(dirname(to), { recursive: true });
  copyFileSync(from, to);
  console.log(`copied ${src} -> ${dest}`);
}
