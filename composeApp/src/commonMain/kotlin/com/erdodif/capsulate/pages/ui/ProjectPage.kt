package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.ui.Ui
import org.jetbrains.compose.resources.stringResource

class ProjectPage : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<ProjectScreen>(::ProjectPage)

    @Composable
    override fun Content(
        state: State, modifier: Modifier
    ) {
        Column(modifier) {
            LazyRow(
                Modifier.padding(3.dp).background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                if (state.opened.file == null) {
                    item {
                        Button(
                            modifier = Modifier,
                            onClick = {},
                            enabled = false,
                            shape = RectangleShape,
                            colors = ButtonColors(
                                contentColor = Color.Unspecified,
                                containerColor = Color.Unspecified,
                                disabledContentColor = MaterialTheme.colorScheme.tertiary,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                "New File",
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(1.dp, 2.dp),
                                color = MaterialTheme.colorScheme.tertiary
                            ) // STOPSHIP - Locale
                        }
                    }
                }
                items(state.project.listFiles()) {
                    Button(
                        modifier = Modifier,
                        onClick = { state.eventHandler(Event.OpenAFile(it.getName())) },
                        shape = RectangleShape,
                        enabled = state.opened.file != it,
                        colors = ButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(it.getName(), Modifier.padding(1.dp, 2.dp))
                    }
                }
                if (state.opened.file == null) {
                    item {
                        IconButton({state.eventHandler(Event.New)}){
                            Icon(Icons.Filled.Add, "")
                        }
                    }
                }
            }
            Column(Modifier.fillMaxSize()) {
                val circuit = Circuit.Builder().addPresenterFactory(EditorPresenter.Factory)
                    .addUiFactory(EditorPage.Factory).addPresenterFactory(DebugPresenter.Factory)
                    .addUiFactory(DebugPage.Factory).build()
                val backStack = rememberSaveableBackStack(root = EditorScreen(state.opened))
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
