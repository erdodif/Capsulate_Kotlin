package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.project.Project
import com.erdodif.capsulate.saver.OpenFileSaver
import com.erdodif.capsulate.saver.mutableSaverOf
import com.erdodif.capsulate.saver.stateListSaver
import com.erdodif.capsulate.utility.ChannelRepository
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Navigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data class ProjectScreen(val project: Project) : Screen {
    @Stable
    class State(
        val project: Project,
        val opened: OpenFile,
        val editorNavigator: Navigator,
        val editorBackStack: SaveableBackStack,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data object Close : Event
        data class OpenAFile(val name: String) : Event
        data class OpenN(val index: Int) : Event
        data object New : Event
    }
}

class ProjectPresenter(
    private val screen: ProjectScreen,
    private val navigator: Navigator
) : Presenter<ProjectScreen.State> {
    object Factory : Presenter.Factory by screenPresenterFactory<ProjectScreen>(::ProjectPresenter)

    // NOTE: The backstack and the navigator must be recreated on `opened` change
    // or else the navigation breaks

    @Composable
    override fun present(): ProjectScreen.State {
        var opened by rememberSaveable(saver = mutableSaverOf(OpenFileSaver)) {
            mutableStateOf(screen.project.openFiles.firstOrNull() ?: screen.project.openEmptyFile())
        }
        val channel: ChannelRepository.ChannelEntry<OpenFile> =
            remember(opened) { ChannelRepository.getNewChannel() }
        val openFiles = rememberSaveable(saver = stateListSaver<OpenFile>()) {
            mutableStateListOf<OpenFile>(*screen.project.openFiles.toTypedArray())
        }
        val backStack = remember(opened) { SaveableBackStack(root = EditorScreen(opened, channel)) }
        val editorNavigator = remember(backStack) { Navigator(backStack, navigator::pop) }
        val project = Project(screen.project.directory, openFiles)
        LaunchedEffect(channel) {
            opened = channel.receive()
        }
        return ProjectScreen.State(project, opened, editorNavigator, backStack) { event ->
            when (event) {
                is Event.Close -> navigator.pop()
                is Event.OpenAFile -> {
                    opened = project.getFile(event.name)
                    editorNavigator.resetRoot(EditorScreen(project.getFile(event.name), channel))
                }

                is Event.New -> {
                    opened = OpenFile()
                    openFiles.add(opened)
                    editorNavigator.resetRoot(EditorScreen(opened, channel))
                }

                is Event.OpenN -> {
                    opened = project.openFiles[event.index]
                    editorNavigator.resetRoot(EditorScreen(opened, channel))
                }
            }
        }
    }

}

