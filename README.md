# Capsulate

Kotlin Multiplatform project to create, edit and lint ELTE special code blocks

## Platform

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform)
[Kotlin/Wasm](https://kotl.in/wasm/)…

### Gradle tasks

#### Web: 

> `:composeApp:wasmJsBrowserDevelopmentRun`

## State

### TODOs

- Finish SwitchStatement
  - Parse & fromTokenized() 
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

### CIC Milestones

#### COC Milestones

[quick reminder](https://coq.inria.fr/doc/v8.9/refman/language/cic.html)

##### Inference rules

> Variable might need to store it's value in a Sort, not a String

[x] W-Empty
[x] W-Local-Assum
[x] W-Local-Def
[x] W-Global-Assum
[x] W-Global-Def
[x] Ax-Prop
[x] Ax-Set
[x] Ax-Type
[x] Var
[x] Const
[ ] Prod-Prop
[ ] Prod-Set
[ ] Prod-Type
[ ] Lam
[ ] App
[ ] Let

For the constructors to work, type inference algorithm must be implemented, as well as the local context shall be temporarily enriched with definitions just to prove a point.

Rewriting rules must be implemented for app and let

##### Conversion rules

[ ] β-reduction
[ ] ι-reduction
[ ] δ-reduction
[ ] ζ-reduction
[ ] η-expansion

After all this, Convertibility as well as subtyping should be doable (since it is just a bunch of reduction rules applied after one another)

> Once all of this is done, the inductive objects must be doable

#### Well-formed inductive defs

TBD
