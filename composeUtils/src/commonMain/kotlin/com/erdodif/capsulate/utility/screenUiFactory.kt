package com.erdodif.capsulate.utility

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui

inline fun <reified T : Screen> screenUiFactory(
    crossinline constructor: () -> Ui<*>
): Ui.Factory = Ui.Factory { screen, _ ->
    when (screen) {
        is T -> constructor()
        else -> null
    }
}

inline fun <reified T : Screen> screenUiFactory(
    crossinline constructor: (T) -> Ui<*>
): Ui.Factory = Ui.Factory { screen, _ ->
    when (screen) {
        is T -> constructor(screen)
        else -> null
    }
}

inline fun <reified T : Screen> screenUiFactory(
    crossinline constructor: (T, CircuitContext) -> Ui<*>
): Ui.Factory = Ui.Factory { screen, context ->
    when (screen) {
        is T -> constructor(screen, context)
        else -> null
    }
}
