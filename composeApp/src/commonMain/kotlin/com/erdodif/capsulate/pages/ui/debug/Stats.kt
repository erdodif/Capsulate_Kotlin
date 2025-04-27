package com.erdodif.capsulate.pages.ui.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.erdodif.capsulate.pages.screen.DebugScreen.Event
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.resources.pause
import com.erdodif.capsulate.resources.play
import com.erdodif.capsulate.resources.random
import com.erdodif.capsulate.resources.reset
import com.erdodif.capsulate.resources.reset_new_seed
import com.erdodif.capsulate.resources.step_forward
import com.erdodif.capsulate.resources.step_over
import com.erdodif.capsulate.resources.stop
import com.erdodif.capsulate.utility.IconTextButton
import com.erdodif.capsulate.utility.layout.WindowLazyList
import kotlin.uuid.ExperimentalUuidApi


private fun LazyListScope.finishedButtons(state: State) {
    item {
        IconTextButton(Res.drawable.reset, Res.string.reset) {
            state.eventHandler(Event.Reset)
        }
    }
    item {
        IconTextButton(Res.drawable.random, Res.string.reset_new_seed) {
            state.eventHandler(Event.ResetRenew)
        }
    }
    item {
        IconTextButton(Res.drawable.close, Res.string.close) {
            state.eventHandler(Event.Close)
        }
    }
}

private fun LazyListScope.ongoingButtons(state: State) {
    if (state.evalLoading) {
        item {
            IconTextButton(Res.drawable.pause, Res.string.stop) {
                state.eventHandler(Event.Pause)
            }
        }
    } else {
        item {
            IconTextButton(
                Res.drawable.play,
                Res.string.step_forward,
                onLongClick = {
                    state.eventHandler(Event.Run)
                }) {
                state.eventHandler(Event.StepForward)
            }
        }
    }
    item {
        IconTextButton(
            Res.drawable.step_over,
            Res.string.step_over,
            enabled = !state.evalLoading
        ) {
            state.eventHandler(Event.StepOver)
        }
    }
    item {
        IconTextButton(Res.drawable.close, Res.string.close) {
            state.eventHandler(Event.Close)
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun Stats(state: State) {
    Column {
        Text("Seed: ${state.seed}")
        if (state.activeStatement == null && !state.functionOngoing) {
            Text(
                "Finished in ${state.stepCount + 1} steps!",
                color = MaterialTheme.colorScheme.tertiary,
            )
            WindowLazyList {
                finishedButtons(state)
            }
        } else {
            Text(
                "Steps taken: ${state.stepCount + 1}",
                color = MaterialTheme.colorScheme.secondary
            )
            WindowLazyList {
                ongoingButtons(state)
            }
        }
    }
}
