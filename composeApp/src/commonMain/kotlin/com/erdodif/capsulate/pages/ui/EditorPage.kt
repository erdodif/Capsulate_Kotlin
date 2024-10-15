package com.erdodif.capsulate.pages.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.StatementDragProvider
import com.erdodif.capsulate.pages.screen.EditorScreen
import com.erdodif.capsulate.utility.CodeEditor
import com.erdodif.capsulate.utility.StatementDrawer
import com.erdodif.capsulate.utility.UnicodeInputField
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui

data object EditorPage : Ui<EditorScreen.State> {
    @Composable
    override fun Content(state: EditorScreen.State, modifier: Modifier) {
        val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        StatementDragProvider {
            DragAndDropContainer(LocalDraggingStatement.current.state) {
                Scaffold(
                    modifier,
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = { bottomBar().Content(state, Modifier) }
                ) { innerPadding ->
                    Column(
                        (if (keyboardUp) Modifier.imePadding() else Modifier.padding(innerPadding)).fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        codeEdit().Content(
                            state, Modifier.weight(3f, false).fillMaxWidth()
                                .defaultMinSize(30.dp, 64.dp)
                        )
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
                                Button({
                                    state.eventHandler(EditorScreen.Event.OpenUnicodeInput)
                                }) { Text("\\escape") }
                            }
                        }
                    }
                }
            }
        }
    }
}


internal fun codeEdit(): Ui<EditorScreen.State> = ui { state, modifier ->
    if (state.showCode)
        CodeEditor(
            state.code,
            modifier,
        ) {
            state.eventHandler(EditorScreen.Event.TextInput(it))
        }
    if (state.input)
        UnicodeInputField(
            {
                val code = state.code
                state.eventHandler(EditorScreen.Event.TextInput(code.copy(buildString {
                    append(code.text.substring(0, code.selection.start))
                    append(it)
                    append(code.text.substring(code.selection.start, code.text.length))
                }, TextRange(code.selection.start + 1))))
                state.eventHandler(EditorScreen.Event.CloseUnicodeInput)
            },
            { state.eventHandler(EditorScreen.Event.CloseUnicodeInput) }
        )
}

internal fun bottomBar(): Ui<EditorScreen.State> = ui { state, modifier ->
    BottomAppBar {
        Row(
            modifier.fillMaxWidth(),
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
