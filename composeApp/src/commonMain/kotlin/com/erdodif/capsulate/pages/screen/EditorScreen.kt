package com.erdodif.capsulate.pages.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

@KParcelize
data class EditorScreen(val initialText: String) : Screen {
    data class State(
        val code: TextFieldValue,
        val structogram: Structogram?,
        val showCode: Boolean,
        val input: Boolean,
        val showStructogram: Boolean,
        val dragStatements: Boolean,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class TextInput(val code: TextFieldValue) : Event
        data object Close : Event
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
        var inputValue by remember {
            mutableStateOf(
                TextFieldValue(screen.initialText, TextRange.Zero)
            )
        }
        var showCode by remember { mutableStateOf(true) }
        var showStructogram by remember { mutableStateOf(true) }
        var structogram: Structogram by remember {
            mutableStateOf(Structogram.fromStatements(Command("", Skip.unique())))
        }
        var dragStatements by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        return EditorScreen.State(
            inputValue, structogram, showCode, input, showStructogram, dragStatements
        ) { event ->
            when (event) {
                is EditorScreen.Event.TextInput -> {
                    inputValue = event.code
                    coroutineScope.launch{
                        initStructogram(inputValue.text){
                            structogram = when (it) {
                                is Left -> it.value
                                is Right -> Structogram.fromStatements(Command(it.value.reason, Skip.unique()))
                            }
                        }
                    }
                }

                is EditorScreen.Event.ToggleCode -> showCode = !showCode
                is EditorScreen.Event.ToggleStructogram -> showStructogram = !showStructogram
                is EditorScreen.Event.Close -> navigator.pop()
                is EditorScreen.Event.Run -> navigator.goTo(DebugScreen(structogram))
                is EditorScreen.Event.ToggleStatementDrag -> dragStatements = !dragStatements
                is EditorScreen.Event.OpenUnicodeInput -> input = true
                is EditorScreen.Event.CloseUnicodeInput -> input = false
            }
        }
    }


    private suspend fun initStructogram(
        input: String,
        onResult: (Either<Structogram, Fail>) -> Unit
    ) = withContext(Dispatchers.Default) {
        var structogram: Either<Structogram, Fail>
        val time = measureTime {
            structogram = Structogram.fromString(input)
        }
        println("Structogram built in ${time}")
        onResult(structogram)
    }

}
