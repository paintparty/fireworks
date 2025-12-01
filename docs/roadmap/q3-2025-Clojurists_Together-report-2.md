## Fireworks: Jeremiah Coyle

Q3 2025 Report No. 2, Published Dec 1, 2025

<br>

I'm happy to report that 5 of the primary goals and 6 of the secondary goals were achieved in Q3. Many thanks to Clojurists Together for supporting this work!

Summary of goals achieved in Q3:

<br>

**Primary goals**
  - Add support for automatic detection of the 3 levels of color support (16-color, 256-color, or Truecolor), using an approach similar to [Chalk](https://github.com/chalk/supports-color).<br>[#42](https://github.com/paintparty/fireworks/issues/42)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)
  
  - Support call-site option to disable all truncation and ellipsis<br>
[#14](https://github.com/paintparty/fireworks/issues/14)<br>
[Completed](https://github.com/paintparty/fireworks/commit/d1232b7fe3d522f751009c2cccc8aeca87966d34)

  - Documentation of interactive workflow. 

  - VS Code Integration <br>
[Completed](https://github.com/paintparty/fireworks/blob/main/docs/editors/vscode/vscode.md)

  - Cursive / IntelliJ Integration <br>
[Completed](https://github.com/paintparty/fireworks/blob/main/docs/editors/cursive/cursive.md)

  - Emacs Integration<br>
In progress. Work on this will commence and once a sufficient amount of data from the use of the Joyride and Cursive implementations is gathered. This will inform any unforeseen details about ergonomics and/or implementation details.

<br>

**Secondary goals**

  - Allow for call-site changes to the label color.<br>[#53](https://github.com/paintparty/fireworks/issues/53)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)<br>
`(? {:label-color :red} (+ 1 1))`

<br>

  - Flag for eliding truncation and ellipsis at callsite <br>
[#77](https://github.com/paintparty/fireworks/issues/77)<br>
[Completed](https://github.com/paintparty/fireworks/pull/77)<br>
`(? :+ my-coll)`, as shorthand for: <br> `(? {:truncation? false} my-coll)`

<br>

  - Add option to produce bold output.<br>[#70](https://github.com/paintparty/fireworks/issues/70)<br>
[Completed](https://github.com/paintparty/fireworks/pull/76)<br>
`(? {:bold? true} (+ 1 1))`

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
[Completed on feature branch 0.16.1](https://github.com/paintparty/fireworks/pull/86)  

<br>
<br>
<br>

The latest release of Fireworks is [`v0.16.0`](https://clojars.org/io.github.paintparty/fireworks/versions/0.13.0), which features the enhancements listed above.
