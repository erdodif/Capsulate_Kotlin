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


### Ideas

- [LatexView with Katex and WebView()](https://github.com/judemanutd/KaTeXView)
- Export into vector, html embed, LaTex?
- System colors, where totally unconventional
  - `kreadconfig6 --group Colors:Button --key ForegroundActive` plasma only way 
  - Might be an `xdg` solution for a more general approach on linux