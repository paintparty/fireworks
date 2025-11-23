# Using Fireworks with VSCode + Joyride

Requirements: [VSCode](https://code.visualstudio.com/) + [Joyride](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.joyride).

<br>

You can optimize the ergonomics of using Firework's print-and-return macros by setting up a Joyride user script in VSCode. These commands can be bound to the shortcut of your choosing and will execute the wrapping and unwrapping of the current form, based on cursor location.

<br>

The **`toggle-fireworks.cljs`** Joyride script, for example, would transform:<br>
 `(+ 1 1)`<br>
  into<br>
 `(? (+ 1 1))`<br>

Conversely, it would transform `(? (+ 1 1))` into `(+ 1 1)`.

If the `?` symbol is the current form (within an existing wrapped form), the **`toggle-fireworks.cljs`**
will toggle the symbol to `!?` (silent version of `?`), and vice-versa.

<br>

The **`toggle-fireworks-tap.cljs`** Joyride script, for example, would transform:<br>
 `(+ 1 1)`<br>
  into<br>
 `(?> (+ 1 1))`<br>

Conversely, it would transform `(?> (+ 1 1))` into `(+ 1 1)`.

If the `?>` symbol is the current form (within an existing wrapped form), the **`toggle-fireworks-tap.cljs`**
script will toggle the symbol to `!?>` (silent version of `?>`), and vice-versa.

<br>

### Setting up the Fireworks Joyride user scripts

<br>

1) Install the [Joyride extension](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.joyride) in your VSCode. 

<br>

2) Familiarize yourself with the basic concepts of how Joyride works, and how it
executes user scripts, where they live on your computer, etc. [Start here](https://github.com/BetterThanTomorrow/joyride?tab=readme-ov-file#quickest-start---start-here---install-joyride)

<br>

3) Copy the `toggle-fireworks.cljs` and `toggle-fireworks-tap.cljs` files into your `/Users/<username>/.config/joyride/scripts`.
These scripts can be found [here](https://github.com/paintparty/fireworks/integrations/vscode/joyride/scripts). 

<br>

### Setting up keybindings

Open the command pallette to find the ***Preferences: Open Keyboard Shortcuts (JSON)***.

Change the `"key"` value of the shortcuts to suite your workflow.

```JSON
{
  "key": "cmd+'",
  "command": "joyride.runUserScript",
  "args": "toggle_fireworks.cljs"
},

{
  "key": "shift+cmd+'",
  "command": "joyride.runUserScript",
  "args": "toggle_fireworks_tap.cljs"
}
```
