package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data object SettingsScreen : Screen {
    @Stable
    class State(val eventHandler: (Event) -> Unit) : CircuitUiState

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data object Exit : Event
    }
}

class SettingsScreenPresenter(
    private val screen: SettingsScreen,
    private val navigator: Navigator
) : Presenter<SettingsScreen.State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<SettingsScreen>(::SettingsScreenPresenter)

    @Composable
    override fun present(): SettingsScreen.State {
        return SettingsScreen.State { event ->
            when (event) {
                is SettingsScreen.Event.Exit -> navigator.pop()
            }
        }
    }


}
