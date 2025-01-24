package com.erdodif.capsulate.utility

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

inline fun <reified T : Screen, R : Presenter<*>> screenPresenterFactory(
    crossinline constructor: () -> R
): Presenter.Factory = Presenter.Factory { screen, _, _ ->
    return@Factory when (screen) {
        is T -> constructor()
        else -> null
    }
}

inline fun <reified T : Screen, R : Presenter<*>> screenPresenterFactory(
    crossinline constructor: (T) -> R
): Presenter.Factory = Presenter.Factory { screen, _, _ ->
    return@Factory when (screen) {
        is T -> constructor(screen)
        else -> null
    }
}

inline fun <reified T : Screen, R : Presenter<*>> screenPresenterFactory(
    crossinline constructor: (T, Navigator) -> R
): Presenter.Factory = Presenter.Factory { screen, navigator, _ ->
    return@Factory when (screen) {
        is T -> constructor(screen, navigator)
        else -> null
    }
}


inline fun <reified T : Screen, R : Presenter<*>> screenPresenterFactory(
    crossinline constructor: (T, Navigator, CircuitContext) -> R
): Presenter.Factory = Presenter.Factory { screen, navigator, context ->
    return@Factory when (screen) {
        is T -> constructor(screen, navigator, context)
        else -> null
    }
}