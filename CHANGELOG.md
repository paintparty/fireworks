# Changelog
[Fireworks](https://github.com/paintparty/fireworks): Fireworks is a themeable tapping library for Clojure, ClojureScript, and Babashka.

For a list of breaking changes, check [here](#breaking-changes)


## Unreleased

#### Added

#### Fixed

#### Changed

<br>
<br>


## 0.12.3
2025-6-14

#### Added
- Detect unknown coll size and fallback to pprint. Closes #63.

<br>
<br>


## 0.12.1
2025-6-13

#### Added
- Fall back to pprint if caught exception. Closes #63.

#### Fixed
- Fix printing of maps in when they are at the max print-level. Fixes #62

<br>
<br>


## 0.12.0
2025-5-31

#### Added
- Call-site option to add indent to entire output (left margin). Closes #15.

- Additional highlighting classes:<br>
  - `:highlight-underlined`
  - `:highlight-error`             
  - `:highlight-warning`           
  - `:highlight-info`              
  - `:highlight-error-underlined`  
  - `:highlight-warning-underlined`
  - `:highlight-info-underlined`

  Closes #59

- Automatically sets `"Universal Neutral"` theme and disable rainbow brackets
if user has `NO_COLOR` env var set to a non-black string. Closes #51.

- Ability to provide a `:path` entry to `:find` option map, to highlight by path. Closes #58.

- `:mode` aliases for `:result` `:file` and `:label` Closes #56.

#### Fixed
- Missing bg-color from kv-gap in metadata maps, terminal, node-js env. Fixes #44

#### Changed
- Bumps Lasertag dep to `0.11.1`.
- Remove Bling dep.


<br>
<br>

## 0.11.0
2025-3-12

#### Added
- Support `:string` mode for outputting formatted string. Closes #55.
- Add mode aliases for `:result` `:file` and `:label` Closes #56.

#### Fixed
- Print inline metadata maps multiline. Fixes #32
- Fix multi-line formatting of collections with badge-above. Fixes #35

#### Changed
- Remove all color from `"Universal Neutral"` theme.
- Add :`nan`, `:infinity`, and `:-infinity` to base syntax tokens
- Bumps Lasertag and Bling deps.

<br>
<br>


## 0.10.4
2024-11-20


#### Added
- Support for non-browser environments such as node, deno, etc. Closes #47

- Formatted key and value printing to body of `fireworks.messaging/bad-option-value-warning`.

- `fireworks.sample` namespace as single source of truth for examples and data for testing.

#### Fixed
- Java array printing in `:pp` mode via incorporation of upstream patch from `me.flowthing.pp 2024-11-13.77` into `fireworks.pp`

#### Changed
- Bumps Lasertag and Bling deps.

<br>
<br>

## 0.10.3
2024-11-11

#### Fixed
- #38 
- #40 
- #36

<br>
<br>

## 0.10.2
2024-11-8

#### Fixed
- Bug with printing js Arrays

#### Changed
- Bumps `lasertag` dep to `0.8.2`

<br>
<br>

## 0.10.1
2024-11-7
#### Fixed
- Bug with printing lambda fns

#### Changed
- Bumps `lasertag` dep to `0.8.1`

<br>
<br>


## 0.10.0
2024-11-6

#### Added
- Support for labeling and printing volatiles
- Support for labeling and printing transient data structures

#### Breaking changes
- Rename `:lamda-label` to `:lambda-label` in theme syntax

<br>
<br>

## 0.9.0
2024-11-3

#### Added
- Supports proper labelling of more Java classes such as `java.util.Hashmap`

#### Changed
- Bumps `lasertag` dep to `0.7.0`
- Internal refactoring of `fireworks.truncate`

#### Removed
- Unused `fireworks.alert` namespace


<br>
<br>

## 0.8.1
#### Fixes
- Fixes empty map bug

#### Added
- Exposes `:margin-top` and `:margin-bottom` options

#### Changed
- Increase depth of caught-exception (in `fireworks.core/formatted`) stack trace preview to 12 frames

<br>
<br>

## 0.8.0
2024-10-23

#### Added
- [Add `"Universal Neutral"` theme](https://github.com/paintparty/fireworks/issues/21) that works on both light and dark backgrounds

- [Add `:when` option](https://github.com/paintparty/fireworks/issues/12) that works on both light and dark backgrounds

- Add `:legacy-terminal?` option 

#### Changed
- Undocumented the folowing options:
  - `:enable-terminal-truecolor?`
  - `:enable-terminal-italics?`
  - `:enable-terminal-font-weights?`

- All the above are now `true` by default. User should now only set `:legacy-terminal?` to true if they want truecolor conversion for a terminal that doesn't support trucolor.

- Moved cljc visual regression testing to `visual_test/`

#### Removed
- `:evaled-form-coll-limit` option

<br>
<br>

## 0.7.1
2024-10-16

#### Changed 
- Bump version of Bling dep

<br>
<br>

## 0.7.0
2024-10-16

#### Fixed 
- [Fixes array-map entry order](https://github.com/paintparty/fireworks/issues/19)

#### Added 
- [Add support for `:single-line-coll-length-limit` option](https://github.com/paintparty/fireworks/issues/20)

- Add support for `:label-length-limit` option

- Add separate shadow-cljs project for cljc visual regression testing in `test/fireworks/visual`

<br>
<br>


## 0.7.1
2024-10-16

#### Changed 
- Bump version of Bling dep

<br>
<br>

## 0.7.0
2024-10-16

#### Fixed 
- [Fixes array-map entry order](https://github.com/paintparty/fireworks/issues/19)

#### Added 
- [Add support for `:single-line-coll-length-limit` option](https://github.com/paintparty/fireworks/issues/20)

- Add support for `:label-length-limit` option

- Add separate shadow-cljs project for cljc visual regression testing in `test/fireworks/visual`

<br>
<br>


## 0.6.5
2024-10-09

#### Fixed 
- [Fixes printing of map entries](https://github.com/paintparty/fireworks/issues/24)

<br>
<br>


## 0.6.4
2024-09-09

#### Changed
- Update `fireworks.pp` namespace to latest version of `me.flowthing/pp` (`2024-09-09.69`).

#### Fixed 
- Printing in `:pp` mode now displays converted map entries correctly

```Clojure
;; Before:
(? :pp (into [] {:a "foo"})) => [:a "foo"]

;; After:
(? :pp (into [] {:a "foo"})) => [[:a "foo"]] 
```

<br>
<br>

## 0.6.3
2024-09-01

#### Changed
- Update bling dep to `[io.github.paintparty/bling "0.1.1"]`

<br>
<br>

## 0.6.2
2024-08-25

#### Added
- `:fw/debug?` call-site option for printing diagnostic info about state and config.
- `:fw/print-config?` call-site option for printing merged config options. 

#### Fixed 
- Override theme resolution when no theme is provided in `config.edn`
- Syntax coloring for labels in cljs

<br>
<br>

## 0.6.1
2024-08-23

#### Fixed 
- Logic bug in :trace mode.
- Various syntax coloring inconsistencies across stock themes

<br>
<br>

## 0.6.0
2024-08-20

#### Added
- [bling](https://github.com/paintparty/bling) lib added as dep for formatting of warnings and errors
- Added support for hiding a given data structure's brackets via metadata ala: `^{:fw/hide-brackets? true}`

#### Changed
- Tweaked error and warning templates.
- `fireworks.core/?` now has 0, 1, 2, and 3 arities.
- `fireworks.core/?` now accepts a leading "flag" argument which determines the functionality.
   This should be a keyword, one of the following:
   - `:result`
   - `:comment`
   - `:file`
   - `:log`
   - `:log-`
   - `:pp`
   - `:pp-`
   - `:trace`
   - `:data`.


#### Removed
- `::fireworks.specs.tokens/color-tuple` (no longer needed).
- Removed the following macros and fns, as their respective functionality is now provided by an optional leading keyword argument to `?`:
  - `?-`
  - `?--`
  - `?log`
  - `?log-`
  - `?pp`
  - `?pp-`
  - `?l`
  - `?i`
  - `?trace`
  - `p-data`

<br>
<br>

## 0.5.0
2024-07-6

#### Added
- `:enable-terminal-font-weights` as config option for enabling bold font weight in terminal environments which support it.
- `?i` macro for printing the info, then the result.
- `?l` macro for printing the label, then the result.
- `?trace` macro for tracing `->`, `->>`, 'some->', 'some->>'.
- `?>` macro for sending values to `tap>`, then returning value.

#### Changed
- Use pprint if label for printing is a map.
- Remove trailing bracket on form-to-be-evaled labels.
- Shortened truncation syntax within colls which exceed :print-level
- `?-` macro now prints just the result.
- `?--` macro now prints just a label, for user commentary.
- `?log-` macro now just prints the result, and returns the result.
- Removed following macros: `?println`, `?print`, `?prn`, `?pr`, functionality replaced by `:print-with` call-site option

 #### Fixed
- Bug with clojure.core/max arity, if maps are empty.
- Bug with `:non-coll-result-length-limit` config.
- Colorization of metadata (kv gaps) in terminal context
- Bug with `:non-coll-mapkey-length-limit` config. 

#### Removed
- `?log--` macro.

<br>
<br>

## 0.4.0
2024-05-22

#### Added
- `:metadata` and `:metadata2` tokens in theme -> `[:theme :classes]`

#### Changed
- Metadata maps now formatted with left-justified values. In all stock themes, they are styled monotone, in a contrasting color, with all keys styled bold.

#### Fixed
- Invalid user-theme warning.

<br>
<br>

## 0.3.0
2024-05-03

#### Added
- Multiple variants of print-and-return macros - see readme for more info.

#### Changed
- Evaled forms are now printed in monotone (themeable) color, and always truncated
- File info is now printed as:
  - evaled form or user-supplied label (Optional)
  - namespace-info
  - linebreak
  - result.
- `fireworks.core/?>` is now a "`tap>` and return" macro. It just taps the value and returns the result (no printing).
- When using `?`, `?-`, or `?--` in ClojureScript, and the resulting value is a native js data structure, the result is printed with `js/console.log`.
- Significant refactoring of `fireworks.truncate` namespace.

<br>
<br>

## 0.2.0
2024-03-02

#### Added
- Relax length limit for non-coll result display values [#3](https://github.com/paintparty/fireworks/issues/3)
- Added `:non-coll-result-length-limit` config option 
- Added `:non-coll-depth-1-length-limit` config option 

#### Changed
- Rename `fireworks.core/p*` -> `fireworks.core/p-data`
- Rename config option `:mapkey-value-width-limit` -> `:non-coll-mapkey-length-limit`
- Rename config option `:value-width-limit` -> `:non-coll-length-limit`
- Update dependency coordinates for `typetag`

<br>
<br>

## 0.1.1
2024-03-02

### Fixed
- Ability to change theme at call site via :mood.
- #1 :metadata-position :block bug for non-coll values. 

<br>
<br>

## 0.1.0
2024-03-01

### Initial Release

<br>
<br>

## Breaking changes
#### 0.10.0
- Rename `:lamda-label` to `:lambda-label` in theme syntax

#### 0.8.0
- If no theme (or invalid theme) is provided, `"Universal Neutral"` is used, which works on both light and dark backgrounds.

#### 0.6.0
- Removed the following macros and fns, as their respective functionality is now provided by an optional leading keyword argument to `?`:
  - `?-`
  - `?--`
  - `?log`
  - `?log-`
  - `?pp`
  - `?pp-`
  - `?l`
  - `?i`
  - `?trace`
  - `p-data`

#### 0.5.0
- Use pprint if label for printing is a map.
- Remove trailing bracket on form-to-be-evaled labels.
- Shortened truncation syntax within colls which exceed :print-level
- `?-` macro now prints just the result.
- `?--` macro now prints just a label, for user commentary.
- `?log-` macro now just prints the result, and returns the result.
- Removed macros : `?println`, `?print`, `?prn`, `?pr`

<br>
<br>

### 0.4.0
- Metadata maps now formatted with left-justified values.

<br>
<br>

### 0.3.0
- File info is now printed as:
  - evaled form or user-supplied label (Optional)
  - namespace-info
  - linebreak
  - result.

- `fireworks.core/?>` is now a "`tap>` and return" macro. It just taps the value and returns the result (no printing).

<br>
<br>

### 0.2.0
- Rename `fireworks.core/p*` -> `fireworks.core/p-data`
- Rename config option `:mapkey-value-width-limit` -> `:non-coll-mapkey-length-limit`
- Rename config option `:value-width-limit` -> `:non-coll-length-limit`
