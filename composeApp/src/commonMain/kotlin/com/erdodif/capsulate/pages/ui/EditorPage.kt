package com.erdodif.capsulate.pages.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.StatementDragProvider
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.pages.screen.EditorScreen
import com.erdodif.capsulate.presets.presets
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.CodeEditor
import com.erdodif.capsulate.utility.PreviewTheme
import com.erdodif.capsulate.utility.StatementDrawer
import com.erdodif.capsulate.utility.UnicodeOverlay
import com.erdodif.capsulate.utility.screenUiFactory
import com.erdodif.capsulate.utility.max
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.runBlocking
import kotlin.uuid.ExperimentalUuidApi


class EditorPage() : Ui<EditorScreen.State> {

    companion object Factory : Ui.Factory by screenUiFactory<EditorScreen>(::EditorPage)

    @Composable
    override fun Content(state: EditorScreen.State, modifier: Modifier) {
        val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val imePaddingValues = WindowInsets.ime.asPaddingValues()
        StatementDragProvider {
            DragAndDropContainer(LocalDraggingStatement.current.state) {
                Scaffold(
                    modifier,
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = { bottomBar().Content(state, Modifier) }
                ) { innerPadding ->
                    if (state.loading) {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center).size(60.dp),
                                strokeWidth = 5.dp,
                                color = MaterialTheme.colorScheme.onSecondary,
                                trackColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        ContentWithOverlays(
                            (Modifier.padding(max(innerPadding, imePaddingValues))).fillMaxSize()
                        ) {
                            Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
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
                                            .background(MaterialTheme.colorScheme.surface)
                                            .height(3.dp)
                                    )
                                structogram().Content(state, Modifier.weight(2f, false))
                                if (keyboardUp && !state.input) {
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
    }

}

@OptIn(ExperimentalUuidApi::class)
internal fun structogram(): Ui<EditorScreen.State> = ui { state, modifier ->
    val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    if (state.showStructogram)
        Column(
            modifier.fillMaxWidth()
                .heightIn(
                    10.dp,
                    if (state.showCode) 1200.dp else Dp.Unspecified
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.structogram != null) {
                state.structogram.Content(
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
}

internal fun codeEdit(): Ui<EditorScreen.State> =
    ui { state, modifier ->
        if (state.showCode)
            CodeEditor(
                state.code,
                modifier,
                { state.eventHandler(EditorScreen.Event.OpenUnicodeInput) }
            ) {
                state.eventHandler(EditorScreen.Event.TextInput(it))
            }
        if (state.input)
            OverlayEffect(state) {
                val result = show(UnicodeOverlay(false))
                if (result == 0.toChar()) {
                    state.eventHandler(EditorScreen.Event.CloseUnicodeInput)
                    return@OverlayEffect
                } else {
                    val code = state.code
                    state.eventHandler(EditorScreen.Event.TextInput(code.copy(buildString {
                        append(code.text.substring(0, code.selection.start))
                        append(result)
                        append(code.text.substring(code.selection.start, code.text.length))
                    }, TextRange(code.selection.start + 1))))
                    state.eventHandler(EditorScreen.Event.CloseUnicodeInput)
                }
            }
    }

internal fun bottomBar(): Ui<EditorScreen.State> = ui { state, modifier ->
    BottomAppBar {
        val scrollState = rememberScrollState(0)
        Row(
            modifier.fillMaxWidth().horizontalScroll(scrollState),
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
                Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("X") // STOPSHIP: Locale
            }
            Button(
                { state.eventHandler(EditorScreen.Event.Run) },
                Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("Run") // STOPSHIP: Locale
            }
            Button(
                { state.eventHandler(EditorScreen.Event.Save) },
                Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("Save file") // STOPSHIP: Locale
            }
        }
    }
}

@Preview
@Composable
fun EditorPagePreview() = PreviewTheme {
    val code = TextFieldValue(presets[1].demos[2].code)
    val structorgram =
        (runBlocking { Structogram.fromString(code.text) } as Left<Structogram>).value
    EditorPage().Content(
        EditorScreen.State(
            code,
            structorgram,
            true,
            false,
            true,
            false,
            OpenFile(null),
            false
        ) {}, Modifier.fillMaxSize()
    )
}
