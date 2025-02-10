package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.project.Project
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlinx.coroutines.runBlocking

@KParcelize
data class ProjectScreen(val project: Project) : Screen {
    class State(
        val project: Project,
        val opened: OpenFile,
        val editorNavigator: Navigator,
        val editorBackStack: SaveableBackStack,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class ProjectSelected(val path: PlatformDirectory) : Event
        data object Close : Event
        data class OpenAFile(val name: String) : Event
        data object New : Event
    }
}

class ProjectPresenter(
    private val screen: ProjectScreen,
    private val navigator: Navigator
) : Presenter<ProjectScreen.State> {
    object Factory : Presenter.Factory by screenPresenterFactory<ProjectScreen>(::ProjectPresenter)

    @Composable
    override fun present(): ProjectScreen.State {
        var project by remember { mutableStateOf(screen.project) }
        // the backstack and the state must point to the same object or else the navigation breaks
        val init = remember{ screen.project.openFiles.firstOrNull() ?: OpenFile()}
        var opened by remember { mutableStateOf(init) }
        val backStack = rememberSaveableBackStack(root = EditorScreen(init))
        val editorNavigator = rememberCircuitNavigator(backStack, navigator::pop)
        return ProjectScreen.State(project, opened, editorNavigator, backStack) { event ->
            when (event) {
                is Event.ProjectSelected -> project = Project(event.path)
                is Event.Close -> navigator.pop()
                is Event.OpenAFile -> project.getFile(event.name).apply {
                    opened = project.getFile(event.name)
                    editorNavigator.resetRoot(EditorScreen(this))
                }

                is Event.New -> {
                    opened = OpenFile()
                    project.openFiles.add(opened)
                    editorNavigator.resetRoot(EditorScreen(opened))
                }
            }
        }
    }

}

