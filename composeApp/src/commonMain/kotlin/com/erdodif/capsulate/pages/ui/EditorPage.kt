package com.erdodif.capsulate.pages.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.CodeEditor
import com.erdodif.capsulate.StatementDrawer
import com.erdodif.capsulate.UnicodeOverlay
import com.erdodif.capsulate.lang.program.grammar.tokenizeProgram
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.pages.screen.EditorScreen
import com.erdodif.capsulate.pages.screen.EditorScreen.Event
import com.erdodif.capsulate.pages.screen.EditorScreen.State
import com.erdodif.capsulate.presets.presets
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.resources.code
import com.erdodif.capsulate.resources.drag
import com.erdodif.capsulate.resources.format
import com.erdodif.capsulate.resources.lightning
import com.erdodif.capsulate.resources.run
import com.erdodif.capsulate.resources.save
import com.erdodif.capsulate.resources.save_file
import com.erdodif.capsulate.resources.struk
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.structogram.StatementDragProvider
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.IconTextButton
import com.erdodif.capsulate.utility.PreviewTheme
import com.erdodif.capsulate.utility.imageExportable
import com.erdodif.capsulate.utility.layout.ScrollableLazyRow
import com.erdodif.capsulate.utility.screenUiFactory
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

class EditorPage : Ui<State> {

    companion object Factory : Ui.Factory by screenUiFactory<EditorScreen>(::EditorPage)

    @Composable
    override fun Content(state: State, modifier: Modifier) {
        val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        ContentWithOverlays(Modifier.fillMaxSize()) {
            StatementDragProvider {
                DragAndDropContainer(LocalDraggingStatement.current.state) {
                    Scaffold(
                        modifier,
                        contentWindowInsets = WindowInsets.ime,
                        bottomBar = { BottomBar(state, Modifier) }
                    ) { innerPadding ->
                        if (state.loading) {
                            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center).size(60.dp),
                                    strokeWidth = 5.dp,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    trackColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                Column(
                                    Modifier.padding(innerPadding),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    codeEdit().Content(
                                        state,
                                        Modifier.fillMaxWidth().weight(1f)
                                    )
                                    if (!state.dragStatements && !state.showStructogram) {
                                        StructogramList(state, Modifier.weight(1f).padding(0.dp))
                                    } else {
                                        structogram().Content(
                                            state,
                                            Modifier.weight(1f).heightIn(250.dp, 500.dp)
                                        )
                                    }
                                }
                                if (keyboardUp && !state.input) {
                                    KeyBoardExtension(state)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomBar(state: State, modifier: Modifier) {
        BottomAppBar {
            ScrollableLazyRow(
                modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Row(
                        Modifier.padding(5.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.code))
                        Switch(
                            state.showCode,
                            { state.eventHandler(Event.ToggleCode) },
                            Modifier.padding(5.dp, 1.dp)
                        )
                    }
                }
                item {
                    Row(
                        Modifier.padding(5.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.struk))
                        Switch(
                            state.showStructogram,
                            { state.eventHandler(Event.ToggleStructogram) },
                            Modifier.padding(5.dp, 1.dp)
                        )
                    }
                }
                if (state.showStructogram) {
                    item {
                        Row(
                            Modifier.padding(5.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(Res.string.drag))
                            Switch(
                                state.dragStatements,
                                { state.eventHandler(Event.ToggleStatementDrag) },
                                Modifier.padding(5.dp, 1.dp)
                            )
                        }
                    }
                }
                item {
                    OutlinedIconButton(
                        { state.eventHandler(Event.Close) },
                        Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Icon(
                            painterResource(Res.drawable.close),
                            stringResource(Res.string.close),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                item {
                    IconTextButton(Res.drawable.format, Res.string.format) {
                        state.eventHandler(Event.Format)
                    }
                }
                item {
                    IconTextButton(Res.drawable.lightning, Res.string.run) {
                        state.eventHandler(Event.Run)
                    }
                }
                item {
                    IconTextButton(Res.drawable.save, Res.string.save_file) {
                        state.eventHandler(Event.Save)
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.KeyBoardExtension(state: State) {
        Row(
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .align(Alignment.BottomCenter)
                .imePadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button({
                state.eventHandler(Event.OpenUnicodeInput)
            }) { Text("\\escape") }
            Button({
                state.eventHandler(Event.Format)
            }) { Text("Format") }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalUuidApi::class)
    @Composable
    private fun StructogramList(state: State, modifier: Modifier) {
        BoxWithConstraints(modifier) {
            val itemModifier = Modifier.width(maxWidth).height(maxHeight)
                .padding(15.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(15.dp)
                )
                .padding(15.dp)
            if (state.structogram != null) {
                val listState = rememberLazyListState()
                ScrollableLazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    flingBehavior = rememberSnapFlingBehavior(listState)
                ) {
                    item(state.structogram) {
                        SharedElementTransitionScope {
                            Box(itemModifier.verticalScroll(rememberScrollState())) {
                                state.structogram.Content(
                                    modifier = Modifier.fillMaxWidth().sharedElement(
                                        rememberSharedContentState(state.structogram),
                                        requireAnimatedScope(
                                            SharedElementTransitionScope.AnimatedScope.Navigation
                                        )
                                    ).imageExportable(),
                                    state.dragStatements,
                                    null,
                                    {
                                        state.eventHandler(
                                            Event.DroppedStatement(
                                                it.first,
                                                it.second
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    items(state.structogram.methods) { method ->
                        Box(itemModifier.verticalScroll(rememberScrollState())) {
                            method.asStructogram().Content(
                                modifier = Modifier.fillMaxWidth().imageExportable(),
                                draggable = false,
                            )
                        }
                    }
                    items(state.structogram.functions) { function ->
                        Box(itemModifier.verticalScroll(rememberScrollState())) {
                            function.asStructogram().Content(
                                modifier = Modifier.fillMaxWidth().imageExportable(),
                                draggable = false,
                            )
                        }
                    }
                }
            } else {
                Text("Error", Modifier.fillMaxWidth())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
    internal fun structogram(): Ui<State> = ui { state, modifier ->
        val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        Column(
            modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.structogram != null) {
                SharedElementTransitionScope {
                    state.structogram.Content(
                        Modifier.horizontalScroll(
                            rememberScrollState()
                        ).sharedElement(
                            rememberSharedContentState(state.structogram),
                            requireAnimatedScope(
                                SharedElementTransitionScope.AnimatedScope.Navigation
                            )
                        ).let { if (state.dragStatements) it else it.imageExportable() },
                        state.dragStatements,
                        null,
                        { state.eventHandler(Event.DroppedStatement(it.first, it.second)) }
                    )
                }
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

    internal fun codeEdit(): Ui<State> = ui { state, modifier ->
        if (state.showCode)
            Column(modifier) {
                CodeEditor(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp),
                    state.code,
                    state.tokenized,
                    state.focusRequester,
                    { state.eventHandler(Event.OpenUnicodeInput) }
                ) {
                    state.eventHandler(Event.TextInput(it))
                }
            }
        if (state.input)
            OverlayEffect(state) {
                val result = show(UnicodeOverlay(false))
                if (result.isEmpty()) {
                    state.eventHandler(Event.CloseUnicodeInput)
                    return@OverlayEffect
                } else {
                    val code = state.code
                    state.eventHandler(Event.TextInput(code.copy(buildString {
                        append(code.text.substring(0, code.selection.start))
                        append(result)
                        append(code.text.substring(code.selection.start, code.text.length))
                    }, TextRange(code.selection.start + result.length))))
                    state.eventHandler(Event.CloseUnicodeInput)
                }
            }
    }

    @Preview
    @Composable
    private fun EditorPagePreview() = PreviewTheme {
        val code = TextFieldValue(presets[1].demos[2].code)
        val structorgram =
            (runBlocking { Structogram.fromString(code.text) } as Left<Structogram>).value
        EditorPage().Content(
            State(
                code,
                tokenizeProgram(code.text),
                structorgram,
                true,
                false,
                FocusRequester(),
                true,
                false,
                OpenFile(),
                false
            ) {}, Modifier.fillMaxSize()
        )
    }
}
