package com.erdodif.capsulate.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.project.Project
import com.erdodif.capsulate.structogram.composables.StatementPreview
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.io.files.Path

@KParcelize
data object ProjectScreen : Screen{
    class State(val project: Project?,val eventHandler: (Event) -> Unit): CircuitUiState{

    }
    sealed class Event: CircuitUiEvent {
        data class ProjectSelected(val project: Project) : Event()
        data object Close: Event()
    }
}

class ProjectPresenter(
    private val screen: ProjectScreen,
    private val navigator: Navigator,
    private var project: Project?
) : Presenter<ProjectScreen.State> {
    @Composable
    override fun present(): ProjectScreen.State {
        var project by remember { mutableStateOf(project) }
        return ProjectScreen.State(project){
            when(it){
                is ProjectScreen.Event.ProjectSelected -> project = it.project
                is ProjectScreen.Event.Close -> navigator.pop()
            }
        }
    }

    object Factory : Presenter.Factory {
        override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? {
            return if (screen is ProjectScreen) {
                ProjectPresenter(screen, navigator,null)
            }else null
        }
    }
}

data object ProjectPage: Ui<ProjectScreen.State>{
    @Composable
    override fun Content(state: ProjectScreen.State, modifier: Modifier) {
        val picker = rememberDirectoryPickerLauncher("Open Project"){
            state.eventHandler(ProjectScreen.Event.ProjectSelected(Project(Path(it?.path.toString()))))
        }
        if(state.project == null){
            picker.launch()
            return
        }
        Button({state.eventHandler(ProjectScreen.Event.Close)}){Text("Close")}
        Column{
            Text(state.project.directory.toString())
            StatementPreview()
        }
    }
}
