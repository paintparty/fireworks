(ns fireworks-vscode.scaffold
  "Pure name-substitution rules for the `Create New Project` command. Same boundary discipline as
   the rest of the cljs: text in, text out, no I/O.

   WHERE THINGS LIVE (for the maintainer):
   - The file BODIES a new project is built from are the on-disk example trees,
     `examples/{deps,leiningen,babashka}-project/`. To change what a scaffolded project looks like,
     edit those. The TS side walks the chosen tree and, for each file, calls `scaffold-path` /
     `scaffold-content` here to rename it into the user's project.
   - THIS namespace only holds the rules: which paths to keep/rename/skip (`scaffold-path`), how to
     substitute the project name into a file's text (`scaffold-content`), the single canonical
     `.gitignore` written for every runtime (`gitignore`), the Live Code launch command per runtime
     (`launch-command`), and which file to open after scaffolding (`open-file`).

   The examples use the placeholder name `example` (namespace `example.core`, dirs `src/example/`,
   Leiningen artifact `fireworks-lein-example`). Scaffolding replaces those with the user's name."
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]))

;; --- Name helpers ---------------------------------------------------------

(defn project-ns
  "The top namespace segment for `name` (lowercased). The examples use a single segment
   (`example.core`), so a project named \"my-app\" yields namespace `my-app.core`."
  [name]
  (str/lower-case (str/trim name)))

(defn path-seg
  "The directory segment for `name` — Clojure's ns->path munge (`-` -> `_`). So ns `my-app`
   lives under `src/my_app/`."
  [name]
  (str/replace (project-ns name) "-" "_"))

;; --- Path rules -----------------------------------------------------------

(defn- skip?
  "True for example-tree paths that should not be copied into a new project: the regenerated
   .gitignore (see `gitignore`), the runtime result cache, build/cache dirs, and editor state
   (.calva/.lsp/.clj-kondo/.git) that may be lying around in the maintainer's example checkout."
  [rel-path]
  (or (= rel-path ".gitignore")
      (= rel-path ".DS_Store")
      (str/starts-with? rel-path ".fireworks/results")
      (str/starts-with? rel-path ".cpcache")
      (str/starts-with? rel-path "target")
      (str/starts-with? rel-path "out/")
      (str/starts-with? rel-path ".calva")
      (str/starts-with? rel-path ".lsp")
      (str/starts-with? rel-path ".clj-kondo")
      (str/starts-with? rel-path ".git/")))

(defn scaffold-path
  "The destination relative path for an example-tree file at `rel-path` (forward-slash separated),
   or nil to skip it. Renames the placeholder `example` directory segment to `name`'s munged form,
   e.g. `src/example/core.clj` -> `src/my_app/core.clj`. `kind` is \"deps\"|\"lein\"|\"bb\"."
  [_kind name rel-path]
  (when-not (skip? rel-path)
    (let [seg (path-seg name)]
      (->> (str/split rel-path #"/")
           (map (fn [s] (if (= s "example") seg s)))
           (str/join "/")))))

;; --- Content rules --------------------------------------------------------

(defn- strip-live-code-key
  "Structurally remove key `k` (and its value) from the :live-code alias in deps.edn `text`,
   preserving the surrounding formatting and comments. No-op when the key (or alias) is absent."
  [text k]
  (let [root (z/of-string text)
        az   (some-> root (z/get :aliases) (z/get :live-code))
        vz   (when az (z/get az k))]
    (if vz
      ;; remove the value node, which repositions on the key, then remove the key node;
      ;; z/remove cleans up the surrounding whitespace so the line disappears cleanly.
      (z/root-string (-> vz z/remove z/remove))
      text)))

(defn scaffold-content
  "The text to write for the example-tree file at `rel-path`, with the project `name` substituted.

   - Any `example.core` (namespace, :require, `example.core-test`, bb.edn `-m` target) -> `<ns>.core`.
   - project.clj: the Leiningen artifact `fireworks-lein-example` -> `name`.
   - deps.edn: drop the `:override-deps` (in-repo Fireworks :local/root — must not leak to a user
     project; the published Fireworks is already in the top-level :deps) and `:jvm-opts` (elide flag
     — would hide `?` output during a live-coding session) keys from the :live-code alias."
  [_kind name rel-path content]
  (let [ns (project-ns name)
        base (str/replace content "example.core" (str ns ".core"))]
    (cond
      (str/ends-with? rel-path "project.clj")
      (str/replace base "fireworks-lein-example" name)

      (str/ends-with? rel-path "deps.edn")
      (-> base
          (strip-live-code-key :override-deps)
          (strip-live-code-key :jvm-opts))

      :else base)))

;; --- Generated .gitignore -------------------------------------------------

(def gitignore
  "The single canonical .gitignore written for every scaffolded project (all three runtimes). The
   maintainer-supplied Clojure ignore list, followed once by the Fireworks inline-result idiom
   (track the results dir, not the results)."
  (str/join
   "\n"
   [".DS_Store"
    "/target"
    "/classes"
    "/checkouts"
    "profiles.clj"
    "pom.xml"
    "pom.xml.asc"
    "*.jar"
    "*.class"
    "/.lein-*"
    "/.nrepl-port"
    "/.prepl-port"
    ".hgignore"
    ".hg/"
    ".lsp"
    ".lein-repl-history"
    "**/.clj-kondo/.cache/"
    ".calva"
    ".joyride"
    ".vscode"
    ".idea"
    ".ls"
    "/public/js"
    ".cpcache"
    "pom.properties"
    ""
    "# Fireworks writes inline results at runtime; track the dir, not the results."
    ".fireworks/results/*"
    "!.fireworks/results/.gitkeep"
    ""]))

;; --- Launch + open --------------------------------------------------------

(defn launch-command
  "The Live Code watcher command auto-run after scaffolding a `kind` project."
  [kind]
  (case kind
    "deps" "clojure -M:live-code"
    "lein" "lein with-profile +live-code test-refresh"
    "bb"   "bb live-code"
    nil))

(defn open-file
  "The relative path of the source file to open in the editor after scaffolding, so the user lands
   on their code ready to jam."
  [_kind name]
  (str "src/" (path-seg name) "/core.clj"))
