package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvalSequence
import com.erdodif.capsulate.lang.util.EvaluationContext
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
        val activeStatement: AnyUniqueStatement?,
        val env: Env,
        val stepCount: Int,
        val eventHandler: (Event) -> Unit,
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
        var debug by remember {
            mutableStateOf(
                EvaluationContext(
                    Env.empty,
                    EvalSequence(screen.structogram.program.map { it.statement }),
                )
            )
        }
        var envState by remember { mutableStateOf(Env.empty to 0) }
        var statement: AnyUniqueStatement? by remember { mutableStateOf(screen.structogram.program.first()) }
        println(debug)
        return State(screen.structogram, statement, envState.first,envState.second ) { event ->
            when (event) {
                is Event.StepForward -> {
                    debug.step()
                    envState = debug.env to 1
                    statement = debug.head?.unique()
                }
            }
        }
    }
}
