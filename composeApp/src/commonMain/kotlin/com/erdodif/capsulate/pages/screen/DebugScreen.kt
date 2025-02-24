package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
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
        val seed: Int,
        val error: String?,
        val overlayStructogram: Structogram?,
        val eventHandler: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object StepForward : Event
        data object StepOver : Event
        data object Reset : Event
        data object ResetRenew : Event
        data object Close : Event
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
        var error: String? by remember { mutableStateOf(null) }
        val envState by remember(step) { derivedStateOf { debug.env } }
        val statement: Uuid? by remember(step) { derivedStateOf { debug.head?.id } }
        val ongoing: Structogram? by remember(step) {
            derivedStateOf {
                screen.structogram.functions.firstOrNull { it.function == debug.functionOngoing?.expression?.call?.function }
                    ?.asStructogram()
            }
        }
        return State(
            screen.structogram,
            statement,
            envState,
            step,
            debug.seed,
            error,
            ongoing
        ) { event ->
            when (event) {
                is Event.StepForward -> {
                    if (debug.head != null) {
                        debug = debug.step()
                        step = step + 1
                        if (debug.error != null) {
                            error = debug.error
                        }
                    }
                }

                is Event.Reset -> {
                    debug = EvaluationContext(
                        Env.empty,
                        EvalSequence(screen.structogram.program),
                        debug.seed
                    )
                    step = 0
                    error = null
                }

                is Event.ResetRenew -> {
                    debug = EvaluationContext(Env.empty, EvalSequence(screen.structogram.program))
                    step = 0
                    error = null
                }
                is Event.StepOver ->{
                    while(debug.functionOngoing != null && debug.error == null){
                        debug = debug.step()
                        step = step + 1
                    }
                    if (debug.error != null) {
                        error = debug.error
                    }
                }

                is Event.Close -> navigator.pop()
            }
        }
    }
}
