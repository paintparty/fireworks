# Changelog
[Fireworks](https://github.com/paintparty/fireworks): A color printer for Clojure, ClojureScript, and Babashka. 



For a list of breaking changes, check [here](#breaking-changes)


## Unreleased
- 

<br>
<br>

## 0.4.0
2024-05-22

#### Added
- `:metadata` and `:metadata2` tokens in theme -> `[:theme :classes]`

#### Changed
- Metadata maps now formatted with left-justified values. In all stock themes, they are styled monotone, in a contrasting color, with all keys styled bold.

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
