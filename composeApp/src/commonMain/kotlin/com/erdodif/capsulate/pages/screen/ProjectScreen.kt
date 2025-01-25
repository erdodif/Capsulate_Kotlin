package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.project.Project
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import io.github.vinceglb.filekit.core.PlatformDirectory

@KParcelize
data object ProjectScreen : Screen {
    class State(
        val project: Project? = null,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class ProjectSelected(val path: PlatformDirectory) : Event
        data object Close : Event
    }
}

class ProjectPresenter(
    private val screen: ProjectScreen,
    private val navigator: Navigator,
    private var project: Project? = null
) : Presenter<ProjectScreen.State> {
    object Factory :
        Presenter.Factory by screenPresenterFactory<ProjectScreen>({ screen, navigator ->
            ProjectPresenter(screen, navigator, null)
        })

    @Composable
    override fun present(): ProjectScreen.State {
        var project by remember { mutableStateOf(project) }
        return ProjectScreen.State(project) { event ->
            when (event) {
                is ProjectScreen.Event.ProjectSelected -> project = Project(event.path)
                is ProjectScreen.Event.Close -> navigator.pop()
            }
        }
    }

}

