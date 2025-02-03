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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.defaultScreenError
import com.erdodif.capsulate.pages.screen.DebugPresenter
import com.erdodif.capsulate.pages.screen.EditorPresenter
import com.erdodif.capsulate.pages.screen.EditorScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.pages.screen.ProjectScreen.State
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.resources.open_folder
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.ui.Ui
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import org.jetbrains.compose.resources.stringResource

class ProjectPage : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<ProjectScreen>(::ProjectPage)

    @Composable
    override fun Content(
        state: State,
        modifier: Modifier
    ) {
        val picker = rememberDirectoryPickerLauncher(stringResource(Res.string.open_folder), ".") {
            if (it != null) {
                Napier.e { it.path.toString() }
                state.eventHandler(Event.ProjectSelected(it))
            } else {
                state.eventHandler(Event.Close)
            }
        }
        if (state.project == null) {
            picker.launch()
            return
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
                    .addUiFactory(EditorPage.Factory)
                    .addPresenterFactory(DebugPresenter.Factory)
                    .addUiFactory(DebugPage.Factory)
                    .build()
                val backStack = rememberSaveableBackStack(root = EditorScreen(""))
                val navigator = rememberCircuitNavigator(backStack) {
                    state.eventHandler(Event.Close)
                }
                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                        modifier = Modifier.fillMaxSize(),
                        unavailableRoute = defaultScreenError
                    )
                }
                Button({ state.eventHandler(Event.Close) }) { Text(stringResource(Res.string.close)) }
            }
        }
    }
}
