package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui

class DebugPage : Ui<DebugScreen.State> {

    companion object Factory : Ui.Factory by screenUiFactory<DebugScreen>(::DebugPage)

    @Composable
    override fun Content(state: DebugScreen.State, modifier: Modifier) {
        Column(Modifier.safeContentPadding(),verticalArrangement = Arrangement.SpaceBetween) {
            state.structogram.Content(
                modifier = Modifier.fillMaxWidth(),
                draggable = false,
                activeStatement = state.activeStatement,
            )
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
                Row { Button({ state.eventHandler(Event.StepForward) }) { Text("Step forward") } }
                if (state.activeStatement == null) {
                    Text(
                        "Finished in ${state.stepCount+1} steps!",
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                } else {
                    Text("Steps taken: ${state.stepCount+1}", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        // TODO("Highligted structogram and/or code needed. + The debug controls")
    }
}
