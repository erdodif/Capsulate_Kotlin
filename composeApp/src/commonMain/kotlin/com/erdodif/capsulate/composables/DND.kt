package com.erdodif.capsulate.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Draggable2DState
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize

data class DragInfo<T>(
    var offset: Offset,
    var size: IntSize,
    var content: T
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> Modifier.handleDragState(
    valueOnPass: T,
    onDrag: (DragInfo<T>) -> Unit,
    dragState: State<DragInfo<T>>,
    interactionSource: MutableInteractionSource
): Modifier {
    val state by remember {
        mutableStateOf(Draggable2DState({ dragState.value.offset = it }))
    }
    return this.onGloballyPositioned {
        dragState.value.offset = it.positionInRoot(); dragState.value.size = it.size
    }
        .draggable2D(state, true, interactionSource,
            onDragStarted = {
                onDrag(dragState.value.copy(dragState.value.offset,dragState.value.size, valueOnPass))
            }, onDragStopped = {

            })


}
