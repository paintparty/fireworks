# Changelog
[Fireworks](https://github.com/paintparty/fireworks): A color printer for Clojure, ClojureScript, and Babashka. 



For a list of breaking changes, check [here](#breaking-changes)

## Unreleased
#### Added

#### Changed

#### Fixed

#### Removed

## 0.6.6
2024-10-16

#### Fixed 
- [Fixes array-map entry order](https://github.com/paintparty/fireworks/issues/19)
- [Add support for `:single-line-coll-length-limit` option](https://github.com/paintparty/fireworks/issues/20)
- Add support for `:label-length-limit` option

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
