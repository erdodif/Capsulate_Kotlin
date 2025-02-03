package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
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
class DebugScreen(val structogram: Structogram) : Screen {

    data class State(
        val structogram: Structogram,
        val activeStatement: AnyUniqueStatement,
        val env: Env,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        object StepForward : Event
    }
}

class DebugPresenter(val screen: DebugScreen) : Presenter<State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<DebugScreen>(::DebugPresenter)

    @Composable
    override fun present(): State {
        var head by remember { mutableStateOf(0) }
        var env by remember { mutableStateOf(Env.empty) }
        val statement =
            if (head < screen.structogram.statements.size) screen.structogram.program[head] else Skip.unique()
        return State(screen.structogram, statement, env) { event ->
            when (event) {
                is Event.StepForward -> {
                    statement.evaluate(env)
                    env = env.copy()
                    head = head + 1
                }
            }
        }
    }

}