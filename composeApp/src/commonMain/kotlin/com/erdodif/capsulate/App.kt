package com.erdodif.capsulate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.pages.screen.EmptyScreen
import com.erdodif.capsulate.pages.screen.EmptyScreenPresenter
import com.erdodif.capsulate.pages.screen.ProjectPresenter
import com.erdodif.capsulate.pages.screen.ProjectScreen
import com.erdodif.capsulate.pages.ui.emptyPage
import com.erdodif.capsulate.pages.ui.projectPage
import com.erdodif.capsulate.structogram.composables.Theme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.screen.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview

val defaultScreenError: @Composable (Screen, Modifier) -> Unit = { screen, modifier ->
    Column(modifier, verticalArrangement = Arrangement.Center) {
        Text(
            "Asked screen unreachable ($screen)",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(10.dp),
            MaterialTheme.colorScheme.error
        )
    }
}


@Composable
@Preview
fun App() {
    val backStack = rememberSaveableBackStack(root = EmptyScreen)
    val navigator = rememberCircuitNavigator(backStack) {
        // Handle close
    }
    val circuit = Circuit.Builder()
        .addPresenterFactory(EmptyScreenPresenter.Factory)
        .addUi<EmptyScreen, EmptyScreen.State> { state, modifier ->
            emptyPage().Content(state, modifier)
        }
        .addPresenterFactory(ProjectPresenter.Factory)
        .addUi<ProjectScreen, ProjectScreen.State> { state, modifier ->
            projectPage().Content(state, modifier)
        }
        .build()
    MaterialTheme(colorScheme = resolveColors()) {
        Theme.initialize()
        Column(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircuitCompositionLocals(circuit) {
                NavigableCircuitContent(
                    navigator = navigator,
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    unavailableRoute = defaultScreenError
                )
            }
        }
    }
}
