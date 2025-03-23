package com.erdodif.capsulate.pages.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.evaluation.PendingMethodEvaluation
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.structogram.ComposableFunction
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class DebugScreen(val structogram: Structogram) : Screen {

    @Stable
    data class State @OptIn(ExperimentalUuidApi::class) constructor(
        val structogram: Structogram,
        val strucListState: LazyListState,
        val activeStatement: Uuid?,
        val env: Environment,
        val stepCount: Int,
        val seed: Int,
        val error: String?,
        val functionOngoing: Boolean,
        val evalLoading: Boolean,
        val stackTrace: List<EvaluationContext.StackTraceEntry>,
        val eventHandler: (Event) -> Unit,
    ) : CircuitUiState

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data object StepForward : Event
        data object StepOver : Event
        data object Reset : Event
        data object ResetRenew : Event
        data object Pause : Event
        data object Close : Event
    }
}

class DebugPresenter(val screen: DebugScreen, val navigator: Navigator) : Presenter<State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<DebugScreen>(::DebugPresenter)

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    override fun present(): State {
        val listState = rememberLazyListState()
        var step by remember { mutableStateOf(0) }
        var debug by remember {
            mutableStateOf(
                EvaluationContext(
                    Environment.fromStructogram(screen.structogram),
                    EvalSequence(screen.structogram.program)
                )
            )
        }
        var error: String? by remember { mutableStateOf(null) }
        val envState by remember(step) { derivedStateOf { debug.env } }
        val statement: Uuid? by remember(step) {
            derivedStateOf { debug.functionOngoing?.head?.id ?: debug.head?.id }
        }
        val functionOngoing: ComposableFunction? by remember(step) {
            derivedStateOf {
                screen.structogram.functions.firstOrNull {
                    it.function == debug.functionOngoing?.expression?.function
                }
            }
        }
        val scope = rememberCoroutineScope()
        var executionJob: Job? by remember { mutableStateOf(null) }
        var evalLoading by remember { mutableStateOf(false) }
        var stackTrace: List<EvaluationContext.StackTraceEntry> by remember {
            mutableStateOf(debug.getCallStack(screen.structogram.name ?: "Program"))
        }
        return State(
            screen.structogram,
            listState,
            statement,
            envState,
            step,
            debug.seed,
            error,
            debug.functionOngoing != null,
            evalLoading,
            stackTrace
        ) { event ->
            when (event) {
                is Event.StepForward -> {
                    executionJob?.cancel()
                    executionJob = null
                    evalLoading = false
                    if (debug.head != null || debug.functionOngoing != null) {
                        debug = debug.step()
                        step = step + 1
                        if (debug.error != null) {
                            error = debug.error
                        }
                        stackTrace = debug.getCallStack(screen.structogram.name ?: "Program")
                    }
                }

                is Event.Reset -> {
                    executionJob?.cancel()
                    executionJob = null
                    evalLoading = false
                    debug = EvaluationContext(
                        Environment.fromStructogram(screen.structogram, debug.env.seed),
                        EvalSequence(screen.structogram.program),
                        debug.seed
                    )
                    stackTrace = debug.getCallStack(screen.structogram.name ?: "Program")
                    step = 0
                    error = null
                }

                is Event.ResetRenew -> {
                    executionJob?.cancel()
                    executionJob = null
                    evalLoading = false
                    debug = EvaluationContext(
                        Environment.fromStructogram(screen.structogram),
                        EvalSequence(screen.structogram.program)
                    )
                    stackTrace = debug.getCallStack(screen.structogram.name ?: "Program")
                    step = 0
                    error = null
                }

                is Event.StepOver -> {
                    evalLoading = true
                    executionJob?.cancel()
                    executionJob = scope.launch(Dispatchers.IO) {
                        do {
                            debug = debug.step()
                            step = step + 1
                            if (debug.error != null) {
                                error = debug.error
                            }
                            stackTrace = debug.getCallStack(screen.structogram.name ?: "Program")
                            yield()
                        } while (debug.functionOngoing != null && debug.error == null)
                        evalLoading = false
                    }
                }

                is Event.Pause -> {
                    executionJob?.cancel()
                    executionJob = null
                    evalLoading = false
                }

                is Event.Close -> {
                    executionJob?.cancel()
                    executionJob = null
                    evalLoading = false
                    navigator.pop()
                }
            }
            scope.launch {
                if (functionOngoing != null) {
                    listState.animateScrollToItem(
                        screen.structogram.methods.count() + 1 +
                                screen.structogram.functions.indexOfFirst { it.function == functionOngoing?.function })
                } else {
                    val active = (debug.head as? PendingMethodEvaluation)
                        ?: (debug.head as? EvalSequence)?.statements?.firstOrNull() as? PendingMethodEvaluation
                    if (active != null) {
                        listState.animateScrollToItem(
                            screen.structogram.methods.indexOfFirst { it.method == active.method } + 1
                        )
                    } else {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
}
