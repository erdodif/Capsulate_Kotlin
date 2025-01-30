package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui

class DebugPage : Ui<DebugScreen.State> {

    companion object Factory : Ui.Factory by screenUiFactory<DebugScreen>(::DebugPage)

    @Composable
    override fun Content(
        state: DebugScreen.State,
        modifier: Modifier
    ) {
        Column {
            state.structogram.content()
            Column {
                for (parameter in state.env.env.parameters) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(parameter.id)
                        Text(parameter.value.toString())
                        Text(parameter.type.toString())
                    }
                }
            }
            Button({
                println(state.env)
            }) { Text("Click") }
            Row {
                Button(
                    { state.eventHandler(Event.StepForward) },
                ) { Text("Step forward") }
            }
        }
        //TODO("Highligted structogram and/or code needed. + The debug controls")
    }
}