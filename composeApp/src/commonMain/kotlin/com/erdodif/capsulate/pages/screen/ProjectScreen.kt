package com.erdodif.capsulate.pages.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.defaultScreenError
import com.erdodif.capsulate.pages.ui.EditorPage
import com.erdodif.capsulate.project.Project
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.PlatformDirectory

@KParcelize
data object ProjectScreen : Screen {
    class State(
        val project: Project? = null,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class ProjectSelected(val path: PlatformDirectory) : Event()
        data object Close : Event()
    }
}

class ProjectPresenter(
    private val screen: ProjectScreen,
    private val navigator: Navigator,
    private var project: Project? = null
) : Presenter<ProjectScreen.State> {
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

    object Factory : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<*>? {
            return if (screen is ProjectScreen) {
                ProjectPresenter(screen, navigator, null)
            } else null
        }
    }
}

data object ProjectPage : Ui<ProjectScreen.State> {
    @Composable
    override fun Content(state: ProjectScreen.State, modifier: Modifier) {
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
                    .addPresenterFactory(EditorPresenter.Factory(""))
                    .addUi<EditorScreen, EditorScreen.State> { state, modifier ->
                        EditorPage.Content(state, modifier)
                    }.build()
                val backStack = rememberSaveableBackStack(root = EditorScreen)
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
}
