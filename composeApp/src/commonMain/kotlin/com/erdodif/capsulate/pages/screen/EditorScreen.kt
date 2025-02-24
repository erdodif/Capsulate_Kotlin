package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.pages.screen.EditorScreen.Event
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.ComposableStatement
import com.erdodif.capsulate.structogram.statements.DropStatement
import com.erdodif.capsulate.utility.saver.TextFieldValueSaver
import com.erdodif.capsulate.utility.saver.mutableSaverOf
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.time.measureTime
import com.erdodif.capsulate.utility.ChannelRepository.ChannelEntry

@KParcelize
@Serializable
data class EditorScreen(val file: OpenFile, val fileChannel: ChannelEntry<OpenFile>) : Screen {
    @Stable
    data class State(
        val code: TextFieldValue,
        val structogram: Structogram?,
        val showCode: Boolean,
        val input: Boolean,
        val showStructogram: Boolean,
        val dragStatements: Boolean,
        val openFile: OpenFile,
        val loading: Boolean,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data class TextInput(val code: TextFieldValue) : Event
        data object Close : Event
        data object Save : Event
        data object Run : Event
        data object ToggleCode : Event
        data object ToggleStructogram : Event
        data object ToggleStatementDrag : Event
        data object OpenUnicodeInput : Event
        data object CloseUnicodeInput : Event
        data class DroppedStatement(val statement: ComposableStatement<*>, val position: Int) :
            Event
    }
}

class EditorPresenter(val screen: EditorScreen, val navigator: Navigator) :
    Presenter<EditorScreen.State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<EditorScreen>(::EditorPresenter)

    @Composable
    override fun present(): EditorScreen.State {
        val coroutineScope = rememberCoroutineScope()
        val file = remember { screen.file }
        var inputValue by rememberSaveable(saver = mutableSaverOf(TextFieldValueSaver)) {
            mutableStateOf(TextFieldValue("", TextRange.Zero))
        }
        var structogram: Structogram by rememberRetained {
            mutableStateOf(Structogram.fromStatements(Command("", Skip(MatchPos.ZERO))))
        }
        var showCode by remember { mutableStateOf(true) }
        var showStructogram by remember { mutableStateOf(true) }
        var dragStatements by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(true) }
        if (loading) {
            LaunchedEffect(Unit) {
                if (file.content == null) {
                    inputValue = inputValue.copy(text = file.load() ?: "")
                    initStructogram(inputValue.text) {
                        structogram = when (it) {
                            is Left -> it.value
                            is Right -> Structogram.fromStatements(
                                Command(it.value.reason, Skip(MatchPos.ZERO))
                            )
                        }
                    }
                } else {
                    inputValue = inputValue.copy(text = file.content!!)
                }
                loading = false
            }
        }
        return EditorScreen.State(
            inputValue, structogram, showCode, input, showStructogram, dragStatements, file, loading
        ) { event ->
            when (event) {
                is Event.TextInput -> {
                    inputValue = event.code
                    file.content = inputValue.text
                    coroutineScope.launch {
                        screen.fileChannel.send(file)
                    }
                    coroutineScope.launch {
                        initStructogram(inputValue.text) {
                            structogram = when (it) {
                                is Left -> it.value
                                is Right -> Structogram.fromStatements(
                                    Command(it.value.reason, Skip(MatchPos.ZERO))
                                )
                            }
                        }
                    }
                }

                is Event.Save -> {
                    coroutineScope.launch {
                        file.save()
                    }
                }

                is Event.ToggleCode -> showCode = !showCode
                is Event.ToggleStructogram -> showStructogram = !showStructogram
                is Event.Close -> navigator.pop()
                is Event.Run -> {
                    Napier.d { "Navigating" }
                    navigator.goTo(DebugScreen(structogram))
                }

                is Event.ToggleStatementDrag -> dragStatements = !dragStatements
                is Event.OpenUnicodeInput -> input = true
                is Event.CloseUnicodeInput -> input = false
                is Event.DroppedStatement -> {
                    if (event.statement is DropStatement) {
                        inputValue =
                            inputValue.copy(
                                text = pasteText(
                                    inputValue.text,
                                    event.statement.string,
                                    event.position
                                )
                            )
                    } else {
                        inputValue =
                            inputValue.copy(
                                text = cutAndPasteText(
                                    inputValue.text,
                                    event.statement.statement.match,
                                    event.position
                                )
                            )
                    }
                    coroutineScope.launch {
                        initStructogram(inputValue.text) {
                            structogram = when (it) {
                                is Left -> it.value
                                is Right -> Structogram.fromStatements(
                                    Command(it.value.reason, Skip(MatchPos.ZERO))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cutAndPasteText(input: String, word: MatchPos, dest: Int): String =
        when {
            word.end <= dest -> buildString {
                appendRange(input, 0, word.start)
                appendRange(input, word.end, dest)
                appendRange(input, word.start, word.end)
                appendRange(input, dest, input.length)
            }

            dest < word.start -> buildString {
                appendRange(input, 0, dest)
                appendRange(input, word.start, word.end)
                appendRange(input, dest, word.start)
                appendRange(input, word.end, input.length)
            }

            else -> input // Insert some text into itself, good try
        }

    fun pasteText(input: String, content: String, position: Int) = buildString {
        appendRange(input, 0, position)
        append(content)
        appendRange(input, position, input.length)
    }


    private suspend fun initStructogram(
        input: String,
        onResult: (Either<Structogram, Fail>) -> Unit
    ) = withContext(Dispatchers.IO) {
        var structogram: Either<Structogram, Fail>
        val time = measureTime {
            structogram = Structogram.fromString(input)
        }
        if (structogram is Left) {
            val value = structogram.value
            Napier.i(
                "Structogram built in $time" +
                        "${value.statements.count()} statements, " +
                        "${value.methods.count()} methods and " +
                        "${value.functions.count()} functions"
            )
        } else {
            val reason = (structogram as Right).value
            Napier.e("Structogram parse failed in $time with reason: $reason")
        }
        onResult(structogram)
    }

}
