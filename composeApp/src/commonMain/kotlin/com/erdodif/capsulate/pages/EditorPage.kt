package com.erdodif.capsulate.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.StatementDragProvider
import com.erdodif.capsulate.StatementDragState
import com.erdodif.capsulate.lang.grammar.Expression
import com.erdodif.capsulate.lang.grammar.Skip
import com.erdodif.capsulate.lang.grammar.Variable
import com.erdodif.capsulate.utility.CodeEditor
import com.erdodif.capsulate.utility.StatementDrawer
import com.erdodif.capsulate.lang.grammar.halfProgram
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.Statement
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
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
        val dragStatements: Boolean,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class TextInput(val code: String) : Event()
        data object Close : Event()
        data object ToggleCode : Event()
        data object ToggleStructogram : Event()
        data object ToggleStatementDrag : Event()
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
        var dragStatements by remember { mutableStateOf(false) }
        return EditorScreen.State(
            actualText,
            structogram,
            showCode,
            showStructogram,
            dragStatements
        ) { event ->
            when (event) {
                is EditorScreen.Event.TextInput -> {
                    actualText = event.code
                    val result = ParserState(event.code).parse(halfProgram)
                    structogram =
                        Structogram.fromStatements(*((result as? Pass<*>)?.value as List<*>?)
                            ?.filterNot { it is Right<*, *> }
                            ?.map {
                                it as Left<*, *>
                                Statement.fromStatement(
                                    ParserState(event.code),
                                    it.value as com.erdodif.capsulate.lang.grammar.Statement
                                )
                            }?.toTypedArray() ?: arrayOf(
                            Command((result as Fail).reason, Skip)
                        )
                        )
                }

                is EditorScreen.Event.ToggleCode -> showCode = !showCode
                is EditorScreen.Event.ToggleStructogram -> showStructogram = !showStructogram
                is EditorScreen.Event.Close -> navigator.pop()
                is EditorScreen.Event.ToggleStatementDrag -> dragStatements = !dragStatements
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
        val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        StatementDragProvider{
            DragAndDropContainer(LocalDraggingStatement.current.state) {
                Scaffold(
                    modifier,
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = {
                        BottomAppBar {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    Modifier.padding(5.dp, 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Code") // STOPSHIP: Locale
                                    Switch(
                                        state.showCode,
                                        { state.eventHandler(EditorScreen.Event.ToggleCode) },
                                        Modifier.padding(5.dp, 1.dp)
                                    )
                                }
                                Row(
                                    Modifier.padding(5.dp, 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Struk") // STOPSHIP: Locale
                                    Switch(
                                        state.showStructogram,
                                        { state.eventHandler(EditorScreen.Event.ToggleStructogram) },
                                        Modifier.padding(5.dp, 1.dp)
                                    )
                                }
                                Row(
                                    Modifier.padding(5.dp, 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Drag") // STOPSHIP: Locale
                                    Switch(
                                        state.dragStatements,
                                        { state.eventHandler(EditorScreen.Event.ToggleStatementDrag) },
                                        Modifier.padding(5.dp, 1.dp)
                                    )
                                }
                                Button(
                                    { state.eventHandler(EditorScreen.Event.Close) },
                                    Modifier.padding(5.dp, 1.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("X") // STOPSHIP: Locale
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        (if (keyboardUp) Modifier.imePadding() else Modifier.padding(innerPadding)).fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (state.showCode)
                            CodeEditor(
                                state.code,
                                Modifier.weight(3f, false).fillMaxWidth()
                                    .defaultMinSize(30.dp, 64.dp)
                            ) { state.eventHandler(EditorScreen.Event.TextInput(it)) }
                        if (state.showCode && state.showStructogram)
                            Spacer(
                                Modifier.fillMaxWidth().padding(3.dp, 2.dp)
                                    .background(MaterialTheme.colorScheme.surface).height(3.dp)
                            )
                        if (state.showStructogram)
                            Column(
                                Modifier.fillMaxWidth().weight(2f, false)
                                    .heightIn(
                                        10.dp,
                                        if (state.showCode) 1200.dp else Dp.Unspecified
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (state.structogram != null) {
                                    state.structogram.content(
                                        Modifier.verticalScroll(
                                            rememberScrollState()
                                        ),
                                        state.dragStatements
                                    )
                                } else {
                                    Text("Error", Modifier.fillMaxWidth())
                                }
                                if (!keyboardUp && state.dragStatements) {
                                    StatementDrawer(
                                        Modifier.heightIn(100.dp, 400.dp)
                                            .padding(20.dp)
                                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    )
                                }
                            }
                        if (keyboardUp) {
                            Row(Modifier.fillMaxWidth()) {
                                Button({}) { Text("\\escape") }
                            }
                        }
                    }
                }
            }
        }
    }
}
