package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.erdodif.capsulate.structogram.statements.Statement
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState

class StatementDragState(
    val state: DragAndDropState<Statement> = DragAndDropState(),
    preview: Boolean = false,
    private val onPreviewChange: (Boolean) -> Unit = {}
) {
    var preview: Boolean = preview
        set(value) = onPreviewChange(value)

    fun changePreview(preview: Boolean) = onPreviewChange(preview)

    val draggedItem: DraggedItemState<Statement>?
        get() = state.draggedItem

    val dragAmount: Offset?
        get() = state.draggedItem?.dragAmount

    val data: Statement?
        get() = state.draggedItem?.data
}

val LocalDraggingStatement = compositionLocalOf { StatementDragState() }

@Composable
fun StatementDragProvider(block: @Composable () -> Unit) {
    val state by remember { mutableStateOf(DragAndDropState<Statement>()) }
    var preview by remember { mutableStateOf(false) }
    val statementDragState = StatementDragState(state, preview) { preview = it }
    CompositionLocalProvider(LocalDraggingStatement provides statementDragState) {
        block()
    }
}
