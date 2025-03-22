package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import kotlin.uuid.ExperimentalUuidApi
import com.erdodif.capsulate.pages.screen.DebugScreen.State

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
class DebugPage : Ui<State> {

    companion object Factory : Ui.Factory by screenUiFactory<DebugScreen>(::DebugPage)

    @Composable
    override fun Content(state: State, modifier: Modifier) {
        Scaffold(modifier = modifier.fillMaxSize(), bottomBar = { Stats(state) }) { paddingValues ->
            StructogramList(state, paddingValues)
        }
        if (state.error != null) {
            ErrorDialog(state)
        }
    }

    @Composable
    private fun StructogramList(state: State, paddingValues: PaddingValues) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(paddingValues)) {
            val itemModifier = Modifier.width(maxWidth).height(maxHeight)
                .padding(15.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(15.dp)
                )
                .padding(15.dp)
            LazyRow(
                state = state.strucListState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = rememberSnapFlingBehavior(state.strucListState)
            ) {
                item {
                    Box(itemModifier) {
                        state.structogram.Content(
                            modifier = Modifier.fillMaxWidth(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
                items(state.structogram.methods) { method ->
                    Box(itemModifier) {
                        method.asStructogram().Content(
                            modifier = Modifier.fillMaxWidth(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
                items(state.structogram.functions) { function ->
                    Box(itemModifier) {
                        function.asStructogram().Content(
                            modifier = Modifier.fillMaxWidth(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CallStack(trace: List<EvaluationContext.StackTraceEntry>) {
        val listState = rememberLazyListState()
        val textModifier = Modifier.padding(horizontal = 5.dp)
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(trace) { entry ->
                Column(
                    Modifier
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            RoundedCornerShape(5.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(5.dp))
                        .widthIn(min = 100.dp, max = 350.dp)
                        .height(150.dp)
                        .padding(5.dp)
                ) {
                    Text(entry.scope)
                    LazyColumn {
                        items(entry.variables) { variable ->
                            Row {
                                Text(
                                    "${variable.id} : ${variable.type.label}",
                                    modifier = textModifier
                                )
                                Text(
                                    variable.value.toString(),
                                    modifier = textModifier,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (entry.variables.isEmpty()) {
                            item {
                                Text(
                                    "Empty Environment!",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    @Composable
    private fun Stats(state: State) {
        Column(
            Modifier.safeDrawingPadding().padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CallStack(state.stackTrace)
            Column {
                Text("Seed: ${state.seed}")
                if (state.activeStatement == null && !state.functionOngoing) {
                    Row {
                        TextButton({ state.eventHandler(Event.Reset) }) { Text("Reset") }
                        TextButton({ state.eventHandler(Event.ResetRenew) }) { Text("Reset with new seed") }
                        TextButton({ state.eventHandler(Event.Close) }) { Text("Close") }
                    }
                    Text(
                        "Finished in ${state.stepCount + 1} steps!",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                } else {
                    Text(state.activeStatement.toString())
                    Text(
                        "Steps taken: ${state.stepCount + 1}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row {
                        TextButton({ state.eventHandler(Event.StepForward) }) { Text("Step forward") }
                        TextButton({ state.eventHandler(Event.StepOver) }) { Text("Step over") }
                        TextButton({ state.eventHandler(Event.Close) }) { Text("Close") }
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorDialog(state: State) {
        val scrollState = rememberScrollState(0)
        BasicAlertDialog({ state.eventHandler(Event.Close) }, Modifier) {
            Column(
                Modifier.padding(50.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(15.dp)
            ) {
                Text(
                    "Evaluation aborted with reason:",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    state.error ?: "N/A",
                    modifier = Modifier
                        .padding(2.dp, 15.dp)
                        .fillMaxWidth()
                        .heightIn(25.dp, 125.dp)
                        .scrollable(scrollState, Orientation.Vertical)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            RoundedCornerShape(5.dp)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(5.dp)
                        ).padding(10.dp),
                    color = MaterialTheme.colorScheme.error
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button({ state.eventHandler(Event.Close) }) { Text("Close") }
                    Button({ state.eventHandler(Event.Reset) }) { Text("Rerun") }
                }
            }
        }
    }
}
