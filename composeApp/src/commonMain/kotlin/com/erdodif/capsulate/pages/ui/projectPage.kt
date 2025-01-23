package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.defaultScreenError
import com.erdodif.capsulate.pages.screen.EditorPresenter
import com.erdodif.capsulate.pages.screen.EditorScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher

fun projectPage(): Ui<ProjectScreen.State> = ui { state, modifier ->
    val picker = rememberDirectoryPickerLauncher("Open Project", ".") { // STOPSHIP: Locale
        if (it != null) {
            Napier.e { it.path.toString() }
            state.eventHandler(ProjectScreen.Event.ProjectSelected(it))
        } else {
            state.eventHandler(ProjectScreen.Event.Close)
        }
    }
    if (state.project == null) {
        picker.launch()
        return@ui
    }
    Column(modifier) {
        LazyRow(
            Modifier.padding(3.dp).background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            items(state.project.listFiles()) {
                Text(it, Modifier.padding(5.dp, 10.dp))
            }
        }
        Column(Modifier.fillMaxSize()) {
            val circuit = Circuit.Builder()
                .addPresenterFactory(EditorPresenter.Factory)
                .addUi<EditorScreen, EditorScreen.State> { state, modifier ->
                    editorPage().Content(state, modifier)
                }.build()
            val backStack = rememberSaveableBackStack(root = EditorScreen(""))
            val navigator = rememberCircuitNavigator(backStack) {
                state.eventHandler(ProjectScreen.Event.Close)
            }
            CircuitCompositionLocals(circuit) {
                NavigableCircuitContent(
                    navigator = navigator,
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    unavailableRoute = defaultScreenError
                )
            }
            Button({ state.eventHandler(ProjectScreen.Event.Close) }) { Text("Close") } // STOPSHIP: Locale
        }
    }
}
