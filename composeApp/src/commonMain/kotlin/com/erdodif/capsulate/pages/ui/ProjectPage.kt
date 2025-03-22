package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.defaultScreenError
import com.erdodif.capsulate.lang.util.valueOrNull
import com.erdodif.capsulate.pages.screen.ProjectScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.pages.screen.ProjectScreen.State
import com.erdodif.capsulate.utility.layout.ScrollableLazyRow
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.ui.Ui
import kotlin.uuid.ExperimentalUuidApi

private val regularColors: ButtonColors
    @Composable get() = ButtonColors(
        contentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
    )
private val openedColors: ButtonColors
    @Composable get() = ButtonColors(
        contentColor = MaterialTheme.colorScheme.tertiary,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
    )

class ProjectPage : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<ProjectScreen>(::ProjectPage)

    @Composable
    override fun Content(
        state: State, modifier: Modifier
    ) {
        Scaffold(
            modifier = modifier,
            topBar = { TopBar(state) }
        ) { paddingValues ->
            NavigableCircuitContent(
                navigator = state.editorNavigator,
                backStack = state.editorBackStack,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                unavailableRoute = defaultScreenError
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Composable
    private fun TopBar(state: State) {
        ScrollableLazyRow(
            modifier = Modifier.padding(3.dp).background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(state.project.openFiles) { index, openFile ->
                val color = if (openFile.hasFile) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.tertiary
                Button(
                    modifier = Modifier,
                    onClick = { state.eventHandler(Event.OpenN(index)) },
                    shape = RectangleShape,
                    enabled = state.opened.file.valueOrNull != openFile,
                    colors = if (state.opened == openFile) openedColors else regularColors
                ) {
                    Text(
                        (openFile.file.valueOrNull?.getName()
                            ?: "New File (${state.project.namelessCount(index)})") + " *",
                        modifier = Modifier.padding(1.dp, 2.dp),
                        color = color
                    ) // STOPSHIP - Locale
                }
            }
            items(state.project.listFiles().filter {
                it.getName() !in state.project.openFiles.mapNotNull {
                    it.file.valueOrNull?.getName()
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
    }
}
