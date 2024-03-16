# Changelog
[Fireworks](https://github.com/paintparty/fireworks): A color printer for Clojure, ClojureScript, and Babashka. 



For a list of breaking changes, check [here](#breaking-changes)


## Unreleased
- 

## 0.2.0
2024-03-02

### Added
- Relax length limit for non-coll result display values [#3](https://github.com/paintparty/fireworks/issues/3)
- Added `:non-coll-result-length-limit` config option 
- Added `:non-coll-depth-1-length-limit` config option 

### Changed
- Rename `fireworks.core/p*` -> `fireworks.core/p-data`
- Rename config option `:mapkey-value-width-limit` -> `:non-coll-mapkey-length-limit`
- Rename config option `:value-width-limit` -> `:non-coll-length-limit`
- Update dependency coordinates for `typetag`


## 0.1.1
2024-03-02

### Fixed
- Ability to change theme at call site via :mood.
- #1 :metadata-position :block bug for non-coll values. 

## 0.1.0
2024-03-01

### Initial Release
