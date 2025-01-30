package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.DebugEnv
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.runBlocking

@KParcelize
class DebugScreen(val structogram: Structogram) : Screen {

    data class State(val structogram: Structogram, val env: DebugEnv, val eventHandler: (Event) -> Unit) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        object StepForward : Event
    }
}

class DebugPresenter(val screen: DebugScreen) : Presenter<State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<DebugScreen>(::DebugPresenter)

    @Composable
    override fun present(): State {
        var debugEnv = DebugEnv(Env.empty, screen.structogram.program)
        return State(screen.structogram,debugEnv) { event ->
            when (event) {
                is Event.StepForward -> runBlocking {debugEnv = debugEnv.step()}
            }
        }
    }

}