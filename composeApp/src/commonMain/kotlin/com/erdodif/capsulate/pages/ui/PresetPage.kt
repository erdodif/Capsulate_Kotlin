package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import com.erdodif.capsulate.CodeEditor
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.pages.screen.PresetScreen
import com.erdodif.capsulate.pages.screen.PresetScreen.Event
import com.erdodif.capsulate.pages.screen.PresetScreen.State
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.IconTextButton
import com.erdodif.capsulate.utility.imageExportable
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

class PresetPage(private val preset: Preset) : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<PresetScreen>(
        { screen -> PresetPage(screen.preset) }
    )

    @OptIn(ExperimentalUuidApi::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content(state: State, modifier: Modifier) {
        Scaffold(
            modifier = Modifier.padding(15.dp),
            floatingActionButton = {
                IconTextButton(Res.drawable.close, Res.string.close) {
                    state.eventHandler(Event.Close)
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            val windowInfo = currentWindowAdaptiveInfo()
            Column(Modifier.padding(padding)) {
                Text(
                    preset.headerText,
                    Modifier,
                    MaterialTheme.colorScheme.onSurface,
                    24.sp,
                    style = MaterialTheme.typography.headlineLarge,
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(
                        if (windowInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 1
                        else 2
                    ),
                    Modifier.fillMaxSize(),
                    verticalItemSpacing = 10.dp,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(preset.demos) {
                        Column(
                            Modifier.fillMaxWidth().background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(20.dp)
                            ).padding(8.dp)
                        ) {
                            Text(it.description)
                            CodeEditor(
                                modifier = Modifier.heightIn(max = 350.dp).fillMaxWidth(),
                                code = TextFieldValue(it.code)
                            )
                            val coroutineScope = rememberCoroutineScope()
                            var result: Either<Structogram, Fail>? by remember {
                                mutableStateOf(null)
                            }
                            LaunchedEffect(it.code) {
                                result = null
                                coroutineScope.launch(Dispatchers.IO) {
                                    result = Structogram.fromString(it.code)
                                }
                            }
                            when (result) {
                                null -> CircularProgressIndicator()
                                is Left<Structogram> -> {
                                    val scope = rememberCoroutineScope()
                                    val graphicsLayer: GraphicsLayer = rememberGraphicsLayer()
                                    var makeImage by remember { mutableStateOf(false) }
                                    (result as Left<Structogram>).value.Content(
                                        Modifier
                                            .combinedClickable(
                                                enabled = true,
                                                onClick = {},
                                                onLongClick = {
                                                    makeImage = true
                                                })
                                            .imageExportable(
                                                scope,
                                                graphicsLayer,
                                                makeImage && scope.isActive
                                            )
                                    )
                                    SideEffect {
                                        makeImage = false
                                    }
                                }

                                is Right<Fail> -> Text(
                                    (result as Right<Fail>).value.reason,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
