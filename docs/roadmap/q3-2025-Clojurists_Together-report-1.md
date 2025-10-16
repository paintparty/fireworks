## Fireworks: Jeremiah Coyle

Q3 2025 Report No. 1, Published October 15, 2025

I'm happy to report that 2 of the primary goals and 2 of the secondary goals were achieved in the first half of Q3. The remainder of the primary goals will be prioritized for the second half of Q3, and hopefully there will be time to knockout the rest of the secondary goals. Many thanks to Clojurists Together for supporting this work!

Summary of goals achieved in the first half of Q3:

<br>

- **Primary goals**

### Automatic color support detection
Add support for automatic detection of the 3 levels of color support (16-color, 256-color, or Truecolor), using an approach similar to [Chalk](https://github.com/chalk/supports-color).<br>[#42](https://github.com/paintparty/fireworks/issues/42)
[Completed](https://github.com/paintparty/fireworks/pull/76)
  
### Call-site options for quick formatting changes.
Support call-site option to disable all truncation and ellipsis
<br>
[#14](https://github.com/paintparty/fireworks/issues/14)
[Completed](https://github.com/paintparty/fireworks/commit/d1232b7fe3d522f751009c2cccc8aeca87966d34)

<br>

### Option for changing label color at callsite
Allow for quick call-site changes to the label color for Fireworks output.<br>[#53](https://github.com/paintparty/fireworks/issues/53)
[Completed](https://github.com/paintparty/fireworks/pull/76)

### Support bold output
Allow for quick call-site changes to the produce bold Fireworks output.<br>[#70](https://github.com/paintparty/fireworks/issues/70)
[Completed](https://github.com/paintparty/fireworks/pull/76)


The latest release is [`v0.13.0`](https://clojars.org/io.github.paintparty/fireworks/versions/0.13.0), which features the enhancements listed above.
