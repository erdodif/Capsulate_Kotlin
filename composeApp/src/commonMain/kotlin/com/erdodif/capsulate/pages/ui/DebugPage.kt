package com.erdodif.capsulate.pages.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import kotlin.uuid.ExperimentalUuidApi
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.resources.pause
import com.erdodif.capsulate.resources.play
import com.erdodif.capsulate.resources.random
import com.erdodif.capsulate.resources.reset
import com.erdodif.capsulate.resources.reset_new_seed
import com.erdodif.capsulate.resources.step_forward
import com.erdodif.capsulate.resources.step_over
import com.erdodif.capsulate.resources.stop
import com.erdodif.capsulate.utility.IconTextButton
import com.erdodif.capsulate.utility.layout.ScrollableLazyRow
import com.slack.circuit.sharedelements.SharedElementTransitionScope

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

    @OptIn(ExperimentalSharedTransitionApi::class)
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
            ScrollableLazyRow(
                state = state.strucListState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = rememberSnapFlingBehavior(state.strucListState)
            ) {
                item {
                    SharedElementTransitionScope {
                        Box(itemModifier.verticalScroll(rememberScrollState())) {
                            state.structogram.Content(
                                modifier = Modifier.fillMaxWidth().sharedElement(
                                    rememberSharedContentState(state.structogram),
                                    requireAnimatedScope(
                                        SharedElementTransitionScope.AnimatedScope.Navigation
                                    )
                                ),
                                draggable = false,
                                activeStatement = state.activeStatement,
                            )
                        }
                    }
                }
                items(state.structogram.methods) { method ->
                    Box(itemModifier.verticalScroll(rememberScrollState())) {
                        method.asStructogram().Content(
                            modifier = Modifier.fillMaxWidth(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
                items(state.structogram.functions) { function ->
                    Box(itemModifier.verticalScroll(rememberScrollState())) {
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
    private fun CallStack(state: State) {
        val listState = rememberLazyListState()
        val textModifier = Modifier.padding(horizontal = 5.dp)
        ScrollableLazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
        ) {
            items(state.stackTrace, { it.hashCode() }) { entry ->
                Column(
                    Modifier
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            RoundedCornerShape(5.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(5.dp))
                        .widthIn(min = 100.dp, max = 350.dp)
                        .heightIn(min = 100.dp, max = 150.dp)
                        .padding(5.dp)
                ) {
                    Text(entry.scope)
                    LazyColumn {
                        items(entry.variables) { variable ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    buildAnnotatedString {
                                        val value = variable.value
                                        if (value is VArray<*>) {
                                            append("${variable.id} : ${value.type.primitiveType.label}")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(57, 57, 57, 50).compositeOver(
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    fontStyle = FontStyle.Italic,
                                                    fontSize = 12.sp
                                                )
                                            ) {
                                                for (size in value.type.dimensions) {
                                                    append('(')
                                                    append(size.toString())
                                                    append(')')
                                                }
                                            }
                                        } else {
                                            append("${variable.id} : ${variable.type.label}")
                                        }
                                    },
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
            CallStack(state)
            Column {
                Text("Seed: ${state.seed}")
                if (state.activeStatement == null && !state.functionOngoing) {
                    Text(
                        "Finished in ${state.stepCount + 1} steps!",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Row {
                        IconTextButton(Res.drawable.reset, Res.string.reset) {
                            state.eventHandler(Event.Reset)
                        }
                        IconTextButton(Res.drawable.random, Res.string.reset_new_seed) {
                            state.eventHandler(Event.ResetRenew)
                        }
                        IconTextButton(Res.drawable.close, Res.string.close) {
                            state.eventHandler(Event.Close)
                        }
                    }
                } else {
                    Text(
                        "Steps taken: ${state.stepCount + 1}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row {
                        if (state.evalLoading) {
                            IconTextButton(Res.drawable.pause, Res.string.stop) {
                                state.eventHandler(Event.Pause)
                            }
                        } else {
                            IconTextButton(
                                Res.drawable.play,
                                Res.string.step_forward,
                                onLongClick = {
                                    state.eventHandler(Event.Run)
                                }) {
                                state.eventHandler(Event.StepForward)
                            }
                        }
                        IconTextButton(
                            Res.drawable.step_over,
                            Res.string.step_over,
                            enabled = !state.evalLoading
                        ) {
                            state.eventHandler(Event.StepOver)
                        }
                        IconTextButton(Res.drawable.close, Res.string.close) {
                            state.eventHandler(Event.Close)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorDialog(state: State) {
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
                        .heightIn(25.dp, 250.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            RoundedCornerShape(5.dp)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(5.dp)
                        ).padding(10.dp)
                        .verticalScroll(rememberScrollState()),
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
