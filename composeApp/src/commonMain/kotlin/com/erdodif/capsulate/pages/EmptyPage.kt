package com.erdodif.capsulate.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import com.erdodif.capsulate.resources.ic_logo_foreground_monochrome_paddingless
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(Res.drawable.ic_logo_foreground_monochrome_paddingless),
            "Logo",
            Modifier.size(160.dp).padding(10.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        ) // STOPSHIP: Locale
        Text(
            stringResource(Res.string.app_name),
            Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp
        )
        Column(
            Modifier.defaultMinSize(10.dp, 300.dp).fillMaxWidth().padding(40.dp,10.dp)
                .background(MaterialTheme.colorScheme.inversePrimary, RoundedCornerShape(10.dp))
                .padding(30.dp)
        ) {
            Button(
                { state.eventHandler(EmptyScreen.Event.ToProjectPage) },
                Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Open Project") }
        }
    }
}
