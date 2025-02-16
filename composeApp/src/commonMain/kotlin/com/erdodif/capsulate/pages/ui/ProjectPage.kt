package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.defaultScreenError
import com.erdodif.capsulate.pages.screen.ProjectScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.pages.screen.ProjectScreen.State
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.utility.layout.ScrollableLazyRow
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.ui.Ui
import org.jetbrains.compose.resources.stringResource

class ProjectPage : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<ProjectScreen>(::ProjectPage)

    @Composable
    override fun Content(
        state: State, modifier: Modifier
    ) {
        val regularColors = ButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
        val temporalColors = ButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.tertiary,
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
        Column(modifier) {
            ScrollableLazyRow(
                modifier = Modifier.padding(3.dp).background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.project.openFiles.isEmpty()) {
                    item {
                        Button(
                            modifier = Modifier,
                            onClick = {},
                            enabled = false,
                            shape = RectangleShape,
                            colors = temporalColors
                        ) {
                            Text(
                                "New File",
                                modifier = Modifier.padding(1.dp, 2.dp),
                                color = MaterialTheme.colorScheme.tertiary
                            ) // STOPSHIP - Locale
                        }
                    }
                }
                var nameless = 0
                itemsIndexed(state.project.openFiles) { index, openFile ->
                    val color = if (openFile.file != null) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                    Button(
                        modifier = Modifier,
                        onClick = { state.eventHandler(Event.OpenN(index)) },
                        shape = RectangleShape,
                        enabled = state.opened.file != openFile,
                        colors = regularColors
                    ) {
                        Text(
                            openFile.file?.getName() ?: "New File (${nameless++})",
                            modifier = Modifier.padding(1.dp, 2.dp),
                            color = color
                        ) // STOPSHIP - Locale
                    }
                }
                items(state.project.listFiles().filter {
                    it.getName() !in state.project.openFiles.map {
                        it.file?.getName() ?: ""
                    }
                }) {
                    Button(
                        modifier = Modifier,
                        onClick = { state.eventHandler(Event.OpenAFile(it.getName())) },
                        shape = RectangleShape,
                        enabled = state.opened.file != it,
                        colors = regularColors
                    ) {
                        Text(it.getName(), Modifier.padding(1.dp, 2.dp))
                    }
                }
                item {
                    IconButton({ state.eventHandler(Event.New) }) {
                        Icon(Icons.Filled.Add, "")
                    }
                }
            }
            Column(Modifier.fillMaxSize()) {
                NavigableCircuitContent(
                    navigator = state.editorNavigator,
                    backStack = state.editorBackStack,
                    modifier = Modifier.fillMaxSize(),
                    unavailableRoute = defaultScreenError
                )
                Button({ state.eventHandler(Event.Close) }) { Text(stringResource(Res.string.close)) }
            }
        }
    }
}
