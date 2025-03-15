package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        if (state.overlayStructogram != null) {
            FunctionModal(state)
        }
        Scaffold(bottomBar = {
            Stats(state)
        }) {
            state.structogram.Content(
                modifier = Modifier.fillMaxWidth(),
                draggable = false,
                activeStatement = state.activeStatement,
            )
        }
        if (state.error != null) {
            ErrorDialog(state)
        }
    }

    @Composable
    private fun FunctionModal(state: State){
        ModalBottomSheet({ state.eventHandler(Event.StepOver) }) {
            Column(Modifier, verticalArrangement = Arrangement.SpaceBetween) {
                state.overlayStructogram?.Content(
                    Modifier.scrollable(rememberScrollState(0), Orientation.Vertical)
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 250.dp),
                    false,
                    state.activeStatement
                )
                Row(Modifier.fillMaxWidth()) {
                    Button({ state.eventHandler(Event.StepForward) }) { Text("Step forward") }
                    Button({ state.eventHandler(Event.StepOver) }) { Text("Step over") }
                    Button({ state.eventHandler(Event.Close) }) { Text("Close") }
                }
            }
        }
    }


    @Composable
    private fun Stats(state: State){
        Column(Modifier.safeContentPadding(), verticalArrangement = Arrangement.SpaceBetween) {
            if (state.env.parameters.isEmpty()) {
                Text("() : Empty Environment!", color = MaterialTheme.colorScheme.error)
            } else {
                Column(Modifier.border(1.dp, Color.Red).padding(5.dp)) {
                    for (parameter in state.env.parameters) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(parameter.id)
                            Text(parameter.value.toString())
                            Text(parameter.type.toString())
                        }
                    }
                }
            }
            Column {
                Text("Seed: ${state.seed}")
                if (state.activeStatement == null) {
                    Row {
                        Button({ state.eventHandler(Event.Reset) }) { Text("Reset") }
                        Button({ state.eventHandler(Event.ResetRenew) }) { Text("Reset with new seed") }
                        Button({ state.eventHandler(Event.Close) }) { Text("Close") }
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
                        Button({ state.eventHandler(Event.StepForward) }) { Text("Step forward") }
                        Button({ state.eventHandler(Event.StepOver) }) { Text("Step over") }
                        Button({ state.eventHandler(Event.Close) }) { Text("Close") }
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorDialog(state: State){
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
