package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.erdodif.capsulate.structogram.statements.ComposableStatement
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.slack.circuit.retained.rememberRetained

class StatementDragState(
    val state: DragAndDropState<ComposableStatement<*>> = DragAndDropState(),
    preview: Boolean = false,
    private val onPreviewChange: (Boolean) -> Unit = {},
) {
    var preview: Boolean = preview
        set(value) = onPreviewChange(value)

    val draggedItem: DraggedItemState<ComposableStatement<*>>?
        get() = state.draggedItem

    val dragAmount: Offset?
        get() = state.draggedItem?.dragAmount

    val data: ComposableStatement<*>?
        get() = state.draggedItem?.data
}

val LocalDraggingStatement = compositionLocalOf { StatementDragState() }

@Composable
fun StatementDragProvider(block: @Composable () -> Unit) {
    val state by rememberRetained { mutableStateOf(DragAndDropState<ComposableStatement<*>>()) }
    var preview by rememberSaveable { mutableStateOf(false) }
    val statementDragState = StatementDragState(state, preview, { preview = it })
    CompositionLocalProvider(LocalDraggingStatement provides statementDragState, block)
}
