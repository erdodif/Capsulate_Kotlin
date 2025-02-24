package com.erdodif.capsulate.pages.screen

import com.erdodif.capsulate.pages.screen.PresetScreen.State
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data class PresetScreen(val preset: Preset) : Screen {
    @Stable
    data class State(val eventHandler: (Event) -> Unit) : CircuitUiState
    @Immutable
    sealed interface Event : CircuitUiEvent {
        data object Close : Event
    }
}

data class PresetPresenter(
    private val screen: PresetScreen,
    private val navigator: Navigator
) : Presenter<State> {

    object Factory :
        Presenter.Factory by screenPresenterFactory<PresetScreen>(::PresetPresenter)

    @Composable
    override fun present(): State {
        return State { close -> navigator.pop() }
    }

}


