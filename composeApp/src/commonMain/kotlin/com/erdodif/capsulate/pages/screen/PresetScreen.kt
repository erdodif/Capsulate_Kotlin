package com.erdodif.capsulate.pages.screen

import com.erdodif.capsulate.pages.screen.PresetScreen.State
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.presets.Demo
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@KParcelize
data class PresetScreen(val preset: Preset) : Screen {
    @Stable
    data class State(
        val presets: List<Pair<Demo, Either<Structogram, Fail>?>>,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data object Close : Event
    }
}

data class PresetPresenter(
    private val screen: PresetScreen,
    private val navigator: Navigator
) : Presenter<State> {

    object Factory :
        Presenter.Factory by screenPresenterFactory<PresetScreen>(::PresetPresenter)

    @Composable
    override fun present(): State {
        val structograms = remember {
            mutableStateListOf<Either<Structogram, Fail>?>(
                *arrayOfNulls(screen.preset.demos.size)
            )
        }
        val scope = rememberCoroutineScope()
        LaunchedEffect(screen.preset.demos) {
            scope.launch(Dispatchers.IO) {
                screen.preset.demos.forEachIndexed { i, demo ->
                    structograms[i] = Structogram.fromString(demo.code)
                }
            }
        }
        return State(screen.preset.demos.zip(structograms)) { close -> navigator.pop() }
    }

}


