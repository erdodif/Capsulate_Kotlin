package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import com.erdodif.capsulate.KParcelize
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data object SettingsScreen : Screen {
    class State(val eventHandler: (Event) -> Unit) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object Exit : Event()
    }
}

class SettingsScreenPresenter(private val screen: SettingsScreen, private val navigator: Navigator) :
    Presenter<SettingsScreen.State> {

    @Composable
    override fun present(): SettingsScreen.State {
        return SettingsScreen.State { event ->
            when (event) {
                is SettingsScreen.Event.Exit -> navigator.pop()
            }
        }
    }


    object Factory : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<*>? {
            return if (screen is SettingsScreen) {
                SettingsScreenPresenter(screen,navigator)
            } else null
        }
    }

}
