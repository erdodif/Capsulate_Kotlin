package com.erdodif.capsulate.utility

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

inline fun <reified T : Screen> screenPresenterFactory(
    crossinline constructor: () -> Presenter<*>
): Presenter.Factory = Presenter.Factory { screen, _, _ ->
    return@Factory when (screen) {
        is T -> constructor()
        else -> null
    }
}

inline fun <reified T : Screen> screenPresenterFactory(
    crossinline constructor: (T) -> Presenter<*>
): Presenter.Factory = Presenter.Factory { screen, _, _ ->
    return@Factory when (screen) {
        is T -> constructor(screen)
        else -> null
    }
}

inline fun <reified T : Screen> screenPresenterFactory(
    crossinline constructor: (T, Navigator)-> Presenter<*>
): Presenter.Factory = Presenter.Factory { screen, navigator, _ ->
    return@Factory when (screen) {
        is T -> constructor(screen, navigator)
        else -> null
    }
}


inline fun <reified T : Screen> screenPresenterFactory(
    crossinline constructor: (T, Navigator, CircuitContext) -> Presenter<*>
): Presenter.Factory = Presenter.Factory { screen, navigator, context ->
    return@Factory when (screen) {
        is T -> constructor(screen, navigator, context)
        else -> null
    }
}
