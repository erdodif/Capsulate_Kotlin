package com.erdodif.capsulate.pages.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowWidthSizeClass.Companion.COMPACT as COMPACT_WIDTH
import androidx.window.core.layout.WindowHeightSizeClass.Companion.COMPACT as COMPACT_HEIGHT
import com.erdodif.capsulate.pages.screen.LandingScreen
import com.erdodif.capsulate.presets.presets
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import com.erdodif.capsulate.resources.ic_logo_foreground_monochrome_paddingless
import com.erdodif.capsulate.resources.ic_logo_foreground_paddingless
import com.erdodif.capsulate.resources.open
import com.erdodif.capsulate.resources.open_file
import com.erdodif.capsulate.resources.open_folder
import com.erdodif.capsulate.resources.open_preset
import com.erdodif.capsulate.resources.start_empty
import com.erdodif.capsulate.utility.PreviewTheme
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
class LandingPage : Ui<LandingScreen.State> {

    companion object Factory : Ui.Factory by screenUiFactory<LandingScreen>(::LandingPage)

    @Composable
    override fun Content(state: LandingScreen.State, modifier: Modifier) {
        val windowSize = currentWindowAdaptiveInfo()
        if (windowSize.windowSizeClass.windowWidthSizeClass == COMPACT_WIDTH) {
            Column(
                modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Logo()
                Selector(state)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Selector(state)
                Logo()
            }
        }
    }

    @Composable
    private fun Logo() {
        val info = currentWindowAdaptiveInfo()
        val logoSize = if (
            info.windowSizeClass.windowWidthSizeClass == COMPACT_WIDTH &&
            info.windowSizeClass.windowHeightSizeClass == COMPACT_HEIGHT
        ) {
            70.dp
        } else {
            160.dp
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(logoSize)) {
                Icon(
                    painterResource(Res.drawable.ic_logo_foreground_monochrome_paddingless),
                    stringResource(Res.string.app_name),
                    Modifier.size(logoSize - 2.dp).padding(0.dp, 10.dp).offset(x = 0.dp, y = 2.dp)
                        .blur(2.dp),
                    tint = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
                )
                Icon(
                    painterResource(Res.drawable.ic_logo_foreground_paddingless),
                    stringResource(Res.string.app_name),
                    Modifier.size(logoSize - 2.dp).padding(0.dp, 10.dp),
                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        .compositeOver(MaterialTheme.colorScheme.primary)
                )
                Icon(
                    painterResource(Res.drawable.ic_logo_foreground_monochrome_paddingless),
                    stringResource(Res.string.app_name),
                    Modifier.size(logoSize - 2.dp).padding(0.dp, 10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                stringResource(Res.string.app_name),
                Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun Selector(state: LandingScreen.State) {
        val windowSize = currentWindowAdaptiveInfo()
        val buttonModifier = Modifier.fillMaxWidth().padding(2.dp)
        LazyColumn(
            Modifier
                .defaultMinSize(10.dp, 150.dp)
                .padding(80.dp, 20.dp)
                .shadow(10.dp, RoundedCornerShape(10.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(10.dp)
                )
                .padding(30.dp)
                .widthIn(25.dp, 150.dp)
        ) {
            item {
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenFile) },
                    buttonModifier,
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ) { Text(stringResource(Res.string.open_file)) }
            }
            item {
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenFolder) },
                    buttonModifier,
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ) { Text(stringResource(Res.string.open_folder)) }
            }
            item {
                Button(
                    { state.eventHandler(LandingScreen.Event.ToEmptyProject) },
                    buttonModifier,
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ) { Text(stringResource(Res.string.start_empty)) }
            }
            item {
                Button(
                    { state.eventHandler(LandingScreen.Event.OpenPresetModal) },
                    buttonModifier,
                    elevation = ButtonDefaults.buttonElevation(5.dp)
                ) { Text(stringResource(Res.string.open_preset)) }
            }
            item {
                if (state.presetVisible) {
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
                            AnimatedVisibility(true, enter = scaleIn()) {
                                Box(
                                    Modifier.background(
                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                        RoundedCornerShape(10.dp)
                                    )
                                ) {
                                    ModalContent(state)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ModalContent(state: LandingScreen.State) {
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
private fun EmptyPagePreview() = PreviewTheme {
    LandingPage().Content(
        LandingScreen.State(false, rememberModalBottomSheetState(), SnackbarHostState(), { _ -> }),
        Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun EmptyPageOpenModelPreview() = PreviewTheme {
    val state = rememberModalBottomSheetState()
    runBlocking { state.show() }
    LandingPage().Content(
        LandingScreen.State(true, state, SnackbarHostState(), { _ -> }),
        Modifier.fillMaxSize()
    )
}
