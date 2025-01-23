package com.erdodif.capsulate.pages.screen

import com.erdodif.capsulate.pages.screen.PresetScreen.State
import androidx.compose.runtime.Composable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.presets.Preset
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data class PresetScreen(val preset: Preset) : Screen {
    data class State(val eventHandler: (Event) -> Unit) : CircuitUiState
    sealed class Event : CircuitUiEvent {
        data object Close : Event()
    }
}

data class PresetPresenter(private val screen: PresetScreen, private val navigator: Navigator) :
    Presenter<State> {

    @Composable
    override fun present(): State {
        return State { close -> navigator.pop() }
    }

    object Factory : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<*>? = when (screen) {
            is PresetScreen -> PresetPresenter(screen, navigator)
            else -> null
        }
    }
}


