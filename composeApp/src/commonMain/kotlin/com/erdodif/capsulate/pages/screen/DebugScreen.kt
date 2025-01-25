package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.DebugEnv
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
class DebugScreen(val program: List<Statement>): Screen {
    constructor(structogram: Structogram): this(structogram.statements.map { it.statement })

    data class State(val env: DebugEnv, val eventHandler: (Event) -> Unit): CircuitUiState

    sealed interface Event : CircuitUiEvent{
        object StepForward: Event
    }
}

class DebugPresenter(val screen: DebugScreen): Presenter<DebugScreen.State>{

    companion object Factory: Presenter.Factory by screenPresenterFactory<DebugScreen>(::DebugPresenter)

    @Composable
    override fun present(): DebugScreen.State {
        var debugEnv by remember{ mutableStateOf(DebugEnv(Env.empty, screen.program)) }
        return State(debugEnv){ event ->
            when(event){
                is Event.StepForward -> {}
            }
        }
    }

}