package com.erdodif.capsulate.pages.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialog(state: State) {
    BasicAlertDialog({ state.eventHandler(Event.Close) }, Modifier) {
        Column(
            Modifier.padding(50.dp).background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(10.dp)
            ).padding(15.dp)
        ) {
            Text("Evaluation aborted with reason:", color = MaterialTheme.colorScheme.onSurface)
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
