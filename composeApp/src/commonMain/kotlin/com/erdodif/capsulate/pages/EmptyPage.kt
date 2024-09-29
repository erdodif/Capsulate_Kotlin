package com.erdodif.capsulate.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen

@KParcelize
data object EmptyScreen : Screen {
    data class State(val eventHandler: (Event) -> Unit) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object ToProjectPage : Event()
    }
}

class EmptyScreenPresenter(private val screen: EmptyScreen, private val navigator: Navigator) :
    Presenter<EmptyScreen.State> {

    @Composable
    override fun present(): EmptyScreen.State {
        return EmptyScreen.State { event ->
            when (event) {
                is EmptyScreen.Event.ToProjectPage -> navigator.goTo(ProjectScreen)
            }
        }
    }

    object Factory : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<*>? {
            return if (screen is EmptyScreen) {
                EmptyScreenPresenter(screen, navigator)
            } else null
        }
    }
}

@Composable
fun EmptyPage(state: EmptyScreen.State, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Button(
            { state.eventHandler(EmptyScreen.Event.ToProjectPage) },
            Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Open Project") }
    }
}
