package com.erdodif.capsulate.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.composables.CodeEditor
import com.erdodif.capsulate.lang.grammar.halfProgram
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Statement
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui

@KParcelize
object EditorScreen : Screen {
    data class State(
        val code: String,
        val structogram: Structogram?,
        val showCode: Boolean,
        val showStructogram: Boolean,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class TextInput(val code: String) : Event()
        data object Close : Event()
        data object ToggleCode : Event()
        data object ToggleStructogram : Event()
    }
}

class EditorPresenter(val navigator: Navigator, val initialText: String) :
    Presenter<EditorScreen.State> {
    @Composable
    override fun present(): EditorScreen.State {
        var actualText by remember { mutableStateOf(initialText) }
        var showCode by remember { mutableStateOf(true) }
        var showStructogram by remember { mutableStateOf(true) }
        var structogram: Structogram? by remember { mutableStateOf(null) }
        return EditorScreen.State(actualText, structogram, showCode, showStructogram) { event ->
            when (event) {
                is EditorScreen.Event.TextInput -> {
                    actualText = event.code
                    val result = ParserState(event.code).parse(halfProgram)
                    structogram =
                        Structogram.fromStatements(*((result as Pass<*>).value as List<*>)
                            .filterNot { it is Right<*, *> }
                            .map {
                                it as Left<*, *>
                                Statement.fromTokenized(
                                    ParserState(event.code),
                                    it.value as com.erdodif.capsulate.lang.grammar.Statement
                                )
                            }.toTypedArray()
                        )
                }

                is EditorScreen.Event.ToggleCode -> showCode = !showCode
                is EditorScreen.Event.ToggleStructogram -> showStructogram = !showStructogram
                is EditorScreen.Event.Close -> navigator.pop()
            }
        }
    }

    data class Factory(val initialText: String) : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<*>? =
            when (screen) {
                is EditorScreen -> EditorPresenter(navigator, initialText)
                else -> null
            }
    }

}

data object EditorPage : Ui<EditorScreen.State> {
    @Composable
    override fun Content(state: EditorScreen.State, modifier: Modifier) {
        Scaffold(
            modifier,
            bottomBar = {
                BottomAppBar {
                    Button(
                        { state.eventHandler(EditorScreen.Event.ToggleCode) },
                        Modifier.padding(5.dp, 1.dp)
                    ) {
                        Text("Code")
                    }
                    Button(
                        { state.eventHandler(EditorScreen.Event.ToggleStructogram) },
                        Modifier.padding(5.dp, 1.dp)
                    ) {
                        Text("Struk")
                    }
                    Button(
                        { state.eventHandler(EditorScreen.Event.Close) },
                        Modifier.padding(5.dp, 1.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        ) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.showCode)
                    CodeEditor(
                        state.code,
                        Modifier.weight(1f, false).fillMaxWidth().defaultMinSize(30.dp, 64.dp)
                    ) { state.eventHandler(EditorScreen.Event.TextInput(it)) }
                if (state.showCode && state.showStructogram)
                    Spacer(
                        Modifier.fillMaxWidth().padding(3.dp, 2.dp)
                            .background(MaterialTheme.colorScheme.surface).height(3.dp)
                    )
                if (state.showStructogram)
                    if (state.structogram != null) {
                        state.structogram.content(
                            Modifier.heightIn(
                                10.dp,
                                if (state.showCode) 300.dp else Dp.Unspecified
                            ).verticalScroll(
                                rememberScrollState()
                            )
                        )
                    } else {
                        Text("Error", Modifier.fillMaxWidth())
                    }
            }
        }
    }
}
