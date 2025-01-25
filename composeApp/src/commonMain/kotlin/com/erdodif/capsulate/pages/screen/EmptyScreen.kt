package com.erdodif.capsulate.pages.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch

@KParcelize
data object EmptyScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    data class State(
        val presetVisible: Boolean,
        val bottomSheetState: SheetState,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object ToProjectPage : Event()
        data object OpenPresetModal : Event()
        data object ClosePresetModal : Event()
        data class SelectPreset(val preset: Preset) : Event()
    }
}

class EmptyScreenPresenter(
    private val screen: EmptyScreen,
    private val navigator: Navigator
) : Presenter<EmptyScreen.State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<EmptyScreen>(::EmptyScreenPresenter)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun present(): EmptyScreen.State {
        var presetVisible by remember { mutableStateOf(false) }
        var sheetState = rememberModalBottomSheetState()
        val coroutineScope = rememberCoroutineScope()
        return EmptyScreen.State(presetVisible, sheetState) { event ->
            when (event) {
                is EmptyScreen.Event.ToProjectPage -> navigator.goTo(ProjectScreen)
                is EmptyScreen.Event.OpenPresetModal -> presetVisible = true
                is EmptyScreen.Event.ClosePresetModal -> presetVisible = false
                is EmptyScreen.Event.SelectPreset -> {
                    coroutineScope.launch {
                        sheetState.hide()
                        presetVisible = false
                        navigator.goTo(PresetScreen(event.preset))
                    }
                }
            }
        }
    }
}
