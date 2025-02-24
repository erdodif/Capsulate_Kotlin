package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.pages.screen.EditorScreen.Event
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Command
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
import kotlin.time.measureTime

@KParcelize
data class EditorScreen(val file: OpenFile) : Screen {
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
            mutableStateOf(Structogram.fromStatements(Command("", Skip())))
        }
        var showCode by remember { mutableStateOf(true) }
        var showStructogram by remember { mutableStateOf(true) }
        var dragStatements by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(true) }
        if (loading) {
            LaunchedEffect(Unit) {
                inputValue = inputValue.copy(text = file.load() ?: "")
                initStructogram(inputValue.text) {
                    structogram = when (it) {
                        is Left -> it.value
                        is Right -> Structogram.fromStatements(
                            Command(
                                it.value.reason,
                                Skip()
                            )
                        )
                    }
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
                    coroutineScope.launch {
                        initStructogram(inputValue.text) {
                            structogram = when (it) {
                                is Left -> it.value
                                is Right -> Structogram.fromStatements(
                                    Command(it.value.reason, Skip())
                                )
                            }
                        }
                    }
                }

                is Event.Save -> {
                    coroutineScope.launch {
                        file.save(inputValue.text)
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
            }
        }
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
