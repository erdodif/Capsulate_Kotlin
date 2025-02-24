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

- Finish class definitions
  - representation & `show()` functions
- Drag & Drop 
  - with drop-zones
  - borders adjusts
  - transfer animated
- Serialization
- Export into image

### Ideas

- [LatexView with Katex and WebView()](https://github.com/judemanutd/KaTeXView)
- Export into vector, html embed, LaTex?
- System colors, where totally unconventional
  - `kreadconfig6 --group Colors:Button --key ForegroundActive` plasma only way 
  - Might be an `xdg` solution for a more general approach on linux
- Step-by-step options
  - evaluate expressions in the current context
  - automatic and manual branch-selection for the parallel scheduler
