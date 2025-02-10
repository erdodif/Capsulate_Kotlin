package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvalSequence
import com.erdodif.capsulate.lang.util.EvaluationContext
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class DebugScreen(val structogram: Structogram) : Screen {

    data class State @OptIn(ExperimentalUuidApi::class) constructor(
        val structogram: Structogram,
        val activeStatement: Uuid?,
        val env: Env,
        val stepCount: Int,
        val eventHandler: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        object StepForward : Event
        object Reset : Event
        object Close : Event
    }
}

class DebugPresenter(val screen: DebugScreen, val navigator: Navigator) : Presenter<State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<DebugScreen>(::DebugPresenter)

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    override fun present(): State {
        var step by remember { mutableStateOf(0) }
        var debug by remember {
            mutableStateOf(
                EvaluationContext(
                    Env.empty,
                    EvalSequence(screen.structogram.program),
                )
            )
        }
        val envState by remember(step) { derivedStateOf { debug.env } }
        val statement: Uuid? by remember(step) { derivedStateOf { debug.head?.id } }
        return State(screen.structogram, statement, envState, step) { event ->
            when (event) {
                is Event.StepForward -> {
                    if (debug.head != null) {
                        debug = debug.step()
                        step = step + 1
                    }
                }

                is Event.Reset -> {
                    debug = EvaluationContext(Env.empty, EvalSequence(screen.structogram.program))
                    step = 0
                }
                is Event.Close -> navigator.pop()
            }
        }
    }
}
