## Fireworks: Jeremiah Coyle

Q3 2025 Report No. 2, Published Dec 30, 2025

<br>

I'm happy to report that 5 of the primary goals and 6 of the secondary goals were achieved in Q3. Many thanks to Clojurists Together for supporting this work!


### Primary goals
  - Add support for automatic detection of the 3 levels of color support (16-color, 256-color, or Truecolor), using an approach similar to [Chalk](https://github.com/chalk/supports-color).<br>[#42](https://github.com/paintparty/fireworks/issues/42)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)
  
<br>

  - Support call-site option to disable all truncation and ellipsis<br>
[#14](https://github.com/paintparty/fireworks/issues/14)<br>
[Completed](https://github.com/paintparty/fireworks/commit/d1232b7fe3d522f751009c2cccc8aeca87966d34)

<br>

  - Documentation of interactive workflow:
    - [`deps.edn` sample project](https://github.com/paintparty/fireworks?tab=readme-ov-file#jvm-clojure-deps-setup) 
    - [`Leiningen` sample project](https://github.com/paintparty/fireworks?tab=readme-ov-file#jvm-clojure-leiningen-setup)

<br>

  - VS Code Integration <br>
[Completed](https://github.com/paintparty/fireworks/blob/main/docs/editors/vscode/vscode.md)

<br>

  - Cursive / IntelliJ Integration <br>
[Completed](https://github.com/paintparty/fireworks/blob/main/docs/editors/cursive/cursive.md)

<br>

  - Emacs Integration<br>
In progress. Work on this will commence and once a sufficient amount of data from the use of the Joyride and Cursive implementations is gathered. This will inform any unforeseen details about ergonomics and/or implementation details.

<br>

### Secondary goals

  - Allow for call-site changes to the label color.<br>[#53](https://github.com/paintparty/fireworks/issues/53)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)<br>
```Clojure
(? {:label-color :red} (+ 1 1))
```

<br>

  - Flag for eliding truncation and ellipsis at callsite <br>
[#77](https://github.com/paintparty/fireworks/issues/77)<br>
[Completed](https://github.com/paintparty/fireworks/pull/77)<br>

```Clojure
(? :+ my-coll)
;; as shorthand for:
(? {:truncation? false} my-coll)
```

<br>

  - Add option to produce bold output.<br>[#70](https://github.com/paintparty/fireworks/issues/70)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)<br>

```Clojure
(? {:bold? true} (+ 1 1))
```

<br>

  - Add option to format the label as code. <br>[#82](https://github.com/paintparty/fireworks/issues/82)<br>
[Completed](https://github.com/paintparty/fireworks/pull/82)<br>
```Clojure
(? {:format-label-as-code? true}
   (mapv (fn [i] (str "id-" i))
         (range 20)))
```

<br>

  - Add function to set options globally for project, at runtime, with a `config!` macro. <br>[#81](https://github.com/paintparty/fireworks/issues/81)<br>
[Completed](https://github.com/paintparty/fireworks/pull/81)
```Clojure
(fireworks.core/config!
 {:format-label-as-code? true
  :template              [:file-info :form-or-label :result]
  :label-length-limit    100})
```

<br>

  - Properly display contents and badges of native js data structures, when they are within a native cljs data structure.<br>[#46](https://github.com/paintparty/fireworks/issues/46)<br>
[Completed](https://github.com/paintparty/fireworks/pull/86)  
```Clojure
(? [#js {:a 1 :b 2}
    (new js/Set #js["foo" "bar"])
    (into-array [1 2 3])
    (new js/Map #js[#js[3 1] #js[4 2]])
    (new js/Int8Array #js[1 2 3])])
```

<br>
<br>
<br>

The latest release of Fireworks is [`v0.16.1`](https://clojars.org/io.github.paintparty/fireworks/versions/0.13.0), which features the enhancements listed above.
