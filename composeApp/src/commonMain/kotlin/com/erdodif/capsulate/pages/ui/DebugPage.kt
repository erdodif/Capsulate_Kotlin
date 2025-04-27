package com.erdodif.capsulate.pages.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import kotlin.uuid.ExperimentalUuidApi
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.pages.ui.debug.CallStack
import com.erdodif.capsulate.pages.ui.debug.ErrorDialog
import com.erdodif.capsulate.pages.ui.debug.Stats
import com.erdodif.capsulate.utility.imageExportable
import com.erdodif.capsulate.utility.layout.ScrollableLazyRow
import com.erdodif.capsulate.utility.layout.WindowWidthLayout
import com.slack.circuit.sharedelements.SharedElementTransitionScope

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
class DebugPage : Ui<State> {

    companion object Factory : Ui.Factory by screenUiFactory<DebugScreen>(::DebugPage)

    @Composable
    override fun Content(state: State, modifier: Modifier) {
        WindowWidthLayout(
            {
                Row {
                    Stats(state)
                    StructogramList(state, Modifier.weight(1f))
                    CallStack(state)
                }
            },
            {
                Scaffold(modifier = modifier.fillMaxSize(), bottomBar = {
                    Column {
                        CallStack(state)
                        Stats(state)
                    }
                }) { paddingValues ->
                    StructogramList(
                        state,
                        Modifier.fillMaxSize().padding(paddingValues)
                    )
                }
            }
        )
        if (state.error != null) {
            ErrorDialog(state)
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
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
            ScrollableLazyRow(
                state = state.strucListState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = rememberSnapFlingBehavior(state.strucListState)
            ) {
                item {
                    SharedElementTransitionScope {
                        Box(itemModifier.verticalScroll(rememberScrollState())) {
                            state.structogram.Content(
                                modifier = Modifier.fillMaxWidth().sharedElement(
                                    rememberSharedContentState(state.structogram),
                                    requireAnimatedScope(
                                        SharedElementTransitionScope.AnimatedScope.Navigation
                                    )
                                ).imageExportable(),
                                draggable = false,
                                activeStatement = state.activeStatement,
                            )
                        }
                    }
                }
                items(state.structogram.methods) { method ->
                    Box(itemModifier.verticalScroll(rememberScrollState())) {
                        method.asStructogram().Content(
                            modifier = Modifier.fillMaxWidth().imageExportable(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
                items(state.structogram.functions) { function ->
                    Box(itemModifier.verticalScroll(rememberScrollState())) {
                        function.asStructogram().Content(
                            modifier = Modifier.fillMaxWidth().imageExportable(),
                            draggable = false,
                            activeStatement = state.activeStatement,
                        )
                    }
                }
            }
        }
    }
}
