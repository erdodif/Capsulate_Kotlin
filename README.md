# Capsulate

Kotlin Multiplatform project to create, edit and lint ELTE special code blocks

## Platform

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),/home/erdodif
[Kotlin/Wasm](https://kotl.in/wasm/)…

### Gradle tasks

#### Web: 

> `:composeApp:wasmJsBrowserDevelopmentRun`

## State

### TODOs

- scopes can't have `{` and  `}` in one line with a single statement
  - so far it has to be delimited by either `;` or `\n`
- Finish class definitions
  - representation & `show()` functions
- Drag & Drop 
  - with drop-zones
  - borders adjusts
  - transfer animated
- Create prototype language
  - AST + Parse
  - Interpreter + Quick run
- Serialization
- Export into image
- [Test android & IOS](https://maestro.mobile.dev/)

### Parallel dilemma

- Parallel statement and await evaluation will need a scheduler
- The scheduler can start executing any block if it's `Q` evaluates to `true`
- Awaits will basically give back the torch  to the scheduler if the guard condition is false

> By definition, if the scheduler has blocks waiting while no other block can run, 
a deadlock happened

Naive approach is a stack-based evaluation-queue

- So the scheduler will start to read one of the available blocks
- When picking a block, one must make sure, it's Q evaluates to `true`
- When a `wait` statement appears with it's guard condition evaluated as `false`
  - the scheduller should put the remaining statements into a new block with it's `Q` set to the `wait`'s guard condition


### Ideas

- [LatexView with Katex and WebView()](https://github.com/judemanutd/KaTeXView)
- Export into vector, html embed, LaTex?
- System colors, where totally unconventional
  - `kreadconfig6 --group Colors:Button --key ForegroundActive` plasma only way 
  - Might be an `xdg` solution for a more general approach on linux
- Step-by-step options
  - evaluate expressions in the current context
  - automatic and manual branch-selection for the parallel scheduler
