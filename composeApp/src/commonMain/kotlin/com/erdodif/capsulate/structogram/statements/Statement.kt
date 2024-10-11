package com.erdodif.capsulate.structogram.statements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.grammar.DoWhile
import com.erdodif.capsulate.lang.grammar.If
import com.erdodif.capsulate.lang.grammar.Parallel
import com.erdodif.capsulate.lang.grammar.Wait
import com.erdodif.capsulate.lang.grammar.While
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.onMobile
import com.erdodif.capsulate.structogram.composables.Theme
import com.mohamedrejeb.compose.dnd.drag.DraggableItem

typealias StatementList = Array<Statement>

abstract class Statement(val statement: com.erdodif.capsulate.lang.grammar.Statement) {

    /**
     * Creates an area where the current statement can be dragged if enabled
     *
     * Draggable areas should not overlap child statement's place, so they can define their drag logic
     */
    @Suppress("UNCHECKED_CAST")
    @Composable
    protected fun DraggableArea(
        modifier: Modifier,
        draggable: Boolean,
        size: DpSize,
        content: @Composable (Boolean) -> Unit = { Box(modifier) }
    ) = if (draggable) {
        val state = LocalDraggingStatement.current
        DraggableItem(
            modifier, key = this@Statement, state = state,
            data = this@Statement,
            dragAfterLongPress = onMobile,
            draggableContent = {
                val active = remember { MutableTransitionState(false).apply { targetState = true } }
                val animationSpec: AnimationSpec<*> = tween<Any>(150)
                AnimatedVisibility(
                    active,
                    Modifier.requiredSize(size.width, size.height),
                    enter = scaleIn(animationSpec as FiniteAnimationSpec<Float>, .5f)
                ) {
                    this@Statement.Show(
                        Modifier
                            .fillMaxSize()
                            .shadow(10.dp, clip = false)
                            .border(Theme.borderWidth, Theme.borderColor),
                        false
                    )
                }
            },
            content = { content(isDragging) }
        )
    } else content(false)

    @Composable
    abstract fun Show(modifier: Modifier, draggable: Boolean)

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