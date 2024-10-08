package com.erdodif.capsulate

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.erdodif.capsulate.structogram.composables.DragState
import com.erdodif.capsulate.structogram.statements.Statement
import com.mohamedrejeb.compose.dnd.DragAndDropState

val LocalDraggingStatement: CompositionLocal<DragAndDropState<Statement>> =
    compositionLocalOf { DragAndDropState() }

val LocalDragTargetState = compositionLocalOf { DragState() }

