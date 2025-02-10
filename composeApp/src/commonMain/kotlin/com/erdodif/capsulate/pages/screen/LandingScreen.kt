package com.erdodif.capsulate.pages.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.project.OpenFile
import com.erdodif.capsulate.project.Project
import com.erdodif.capsulate.project.extensions
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.file_open_failed
import com.erdodif.capsulate.resources.folder_open_failed
import com.erdodif.capsulate.resources.open_file
import com.erdodif.capsulate.resources.open_folder
import com.erdodif.capsulate.utility.screenPresenterFactory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zwander.kotlin.file.filekit.toKmpFile
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@KParcelize
data object LandingScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    data class State(
        val presetVisible: Boolean,
        val bottomSheetState: SheetState,
        val snackbarHostState: SnackbarHostState,
        val eventHandler: (Event) -> Unit
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object OpenFile : Event()
        data object OpenFolder : Event()
        data object ToEmptyProject : Event()
        data object OpenPresetModal : Event()
        data object ClosePresetModal : Event()
        data class SelectPreset(val preset: Preset) : Event()
    }
}

class LandingPresenter(
    private val screen: LandingScreen,
    private val navigator: Navigator
) : Presenter<LandingScreen.State> {

    companion object Factory :
        Presenter.Factory by screenPresenterFactory<LandingScreen>(::LandingPresenter)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun present(): LandingScreen.State {
        var presetVisible by remember { mutableStateOf(false) }
        var sheetState = rememberModalBottomSheetState()
        val coroutineScope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }
        val failFolderMessage = stringResource(Res.string.folder_open_failed)
        val failFileMessage = stringResource(Res.string.file_open_failed)
        val directoryPicker = rememberDirectoryPickerLauncher(
            stringResource(Res.string.open_folder), "."
        ) {
            if (it != null) {
                Napier.d { it.path.toString() }
                navigator.goTo(ProjectScreen(Project(it)))
            } else {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(failFolderMessage)
                }
            }
        }
        val filePicker = rememberFilePickerLauncher(
            PickerType.File(extensions.toList()),
            stringResource(Res.string.open_file)
        ) {
            if (it != null) {
                Napier.d { it.path.toString() }
                val project = Project()
                project.openFiles.add(OpenFile(it.toKmpFile()))
                navigator.goTo(ProjectScreen(project))
            } else {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(failFileMessage)
                }
            }
        }
        return LandingScreen.State(presetVisible, sheetState, snackBarHostState) { event ->
            when (event) {
                is LandingScreen.Event.ToEmptyProject -> navigator.goTo(ProjectScreen(Project()))
                is LandingScreen.Event.OpenPresetModal -> presetVisible = true
                is LandingScreen.Event.ClosePresetModal -> presetVisible = false
                is LandingScreen.Event.SelectPreset -> {
                    coroutineScope.launch {
                        sheetState.hide()
                        presetVisible = false
                        navigator.goTo(PresetScreen(event.preset))
                    }
                }

                is LandingScreen.Event.OpenFile -> filePicker.launch()
                is LandingScreen.Event.OpenFolder -> directoryPicker.launch()
            }
        }
    }
}
