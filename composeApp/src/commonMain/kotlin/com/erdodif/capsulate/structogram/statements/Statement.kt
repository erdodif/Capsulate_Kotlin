package com.erdodif.capsulate.structogram.statements

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.grammar.DoWhile
import com.erdodif.capsulate.lang.grammar.If
import com.erdodif.capsulate.lang.grammar.Parallel
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.grammar.Wait
import com.erdodif.capsulate.lang.grammar.While
import com.erdodif.capsulate.onMobile
import com.erdodif.capsulate.structogram.composables.Theme
import com.mohamedrejeb.compose.dnd.drag.DraggableItem

typealias StatementList = Array<Statement>

abstract class Statement(val statement: com.erdodif.capsulate.lang.grammar.Statement) {

    @Suppress("UNCHECKED_CAST")
    @Composable
    fun Draggable(modifier: Modifier) {
        val state = LocalDraggingStatement.current
        val dragging = state.draggedItem?.data == statement
        val animationSpec: AnimationSpec<*> = tween<Any>(500)
        val elevation by animateFloatAsState(
            if (dragging) 12f else 0f,
            animationSpec as AnimationSpec<Float>
        )
        val scale by animateFloatAsState(
            if (dragging) 1.1f else 1f,
            animationSpec as AnimationSpec<Float>
        )
        DraggableItem(modifier, key = this, state = state,
            data = this@Statement,
            dragAfterLongPress = onMobile,
            draggableContent = {
                this@Statement.Content(
                    Modifier
                        .graphicsLayer {
                            shadowElevation = elevation
                            scaleX = scale
                            scaleY = scale
                        }
                        .border(Theme.borderWidth, Theme.borderColor),
                    true
                )
            },
            content = {
                this@Statement.Content(
                    Modifier
                        .alpha(if (state.draggedItem?.data == statement) 0.2f else 1f)
                        .fillMaxWidth(),
                    true
                )
            })
    }

    @Composable
    internal abstract fun Content(modifier: Modifier, draggable: Boolean)

    @Composable
    fun Show(modifier: Modifier, draggable: Boolean) {
        if (draggable)
            Draggable(modifier)
        else
            Content(modifier, draggable)
    }

    companion object {
        fun fromStatement(
            state: ParserState,
            statement: com.erdodif.capsulate.lang.grammar.Statement
        ): Statement = when (statement) {
            is If -> IfStatement(statement, state)
            is Wait -> AwaitStatement(statement, state)
            is While -> LoopStatement(statement, state)
            is DoWhile -> LoopStatement(statement, state)
            is Parallel -> ParallelStatement(statement, state)
            else -> Command(statement, state)
        }
    }
}