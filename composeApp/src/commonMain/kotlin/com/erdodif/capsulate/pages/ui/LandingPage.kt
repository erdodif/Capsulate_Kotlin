package com.erdodif.capsulate.pages.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowWidthSizeClass.Companion.COMPACT as COMPACT_WIDTH
import androidx.window.core.layout.WindowHeightSizeClass.Companion.COMPACT as COMPACT_HEIGHT
import com.erdodif.capsulate.pages.screen.LandingScreen
import com.erdodif.capsulate.pages.screen.ProjectScreen.Event
import com.erdodif.capsulate.presets.presets
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import com.erdodif.capsulate.resources.ic_logo_foreground_monochrome_paddingless
import com.erdodif.capsulate.resources.open
import com.erdodif.capsulate.resources.open_file
import com.erdodif.capsulate.resources.open_folder
import com.erdodif.capsulate.resources.open_preset
import com.erdodif.capsulate.resources.start_empty
import com.erdodif.capsulate.utility.PreviewTheme
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
class LandingPage() : Ui<LandingScreen.State> {

    companion object Factory : Ui.Factory by screenUiFactory<LandingScreen>(::LandingPage)

    @Composable
    override fun Content(state: LandingScreen.State, modifier: Modifier) {
        Column(
            modifier = modifier.background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(Res.drawable.ic_logo_foreground_monochrome_paddingless),
                stringResource(Res.string.app_name),
                Modifier.size(160.dp).padding(10.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                stringResource(Res.string.app_name),
                Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
            Column(
                Modifier.defaultMinSize(10.dp, 300.dp).fillMaxWidth().padding(40.dp, 10.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(30.dp)
            ) {
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenFile) },
                    Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(Res.string.open_file)) }
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenFolder) },
                    Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(Res.string.open_folder)) }
                Button(
                    { state.eventHandler(LandingScreen.Event.ToEmptyProject) },
                    Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(Res.string.start_empty)) }
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenPresetModal) },
                    Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(Res.string.open_preset)) }
                if (state.presetVisible) {
                    val windowSize = currentWindowAdaptiveInfo()
                    if (windowSize.windowSizeClass.windowWidthSizeClass == COMPACT_WIDTH ||
                        windowSize.windowSizeClass.windowHeightSizeClass == COMPACT_HEIGHT
                    ) {
                        ModalBottomSheet(
                            onDismissRequest = { state.eventHandler(LandingScreen.Event.ClosePresetModal) },
                            sheetState = state.bottomSheetState,
                            sheetMaxWidth = 400.dp
                        ) {
                            ModalContent(state)
                        }
                    } else {
                        Dialog(
                            onDismissRequest = { state.eventHandler(LandingScreen.Event.ClosePresetModal) },
                            properties = DialogProperties()
                        ) {
                            Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
                                ModalContent(state)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    protected fun ModalContent(state: LandingScreen.State) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(presets, { it.headerText }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(it.headerText, color = MaterialTheme.colorScheme.onSurface)
                    Button(
                        { state.eventHandler(LandingScreen.Event.SelectPreset(it)) },
                    ) { Text(stringResource(Res.string.open)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EmptyPagePreview() = PreviewTheme {
    LandingPage().Content(
        LandingScreen.State(false, rememberModalBottomSheetState(), SnackbarHostState(), { _ -> }),
        Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EmptyPageOpenModelPreview() = PreviewTheme {
    val state = rememberModalBottomSheetState()
    runBlocking { state.show() }
    LandingPage().Content(
        LandingScreen.State(true, state, SnackbarHostState(), { _ -> }),
        Modifier.fillMaxSize()
    )
}
