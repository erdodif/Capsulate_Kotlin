package com.erdodif.capsulate.structogram.statements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.If
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.program.grammar.When
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.program.grammar.function.MethodCall
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.onMobile
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.structogram.LocalStructogramDropHandler
import com.erdodif.capsulate.structogram.StatementDragState
import com.erdodif.capsulate.utility.theme.HorizontalBorder
import com.erdodif.capsulate.utility.theme.Theme
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.erdodif.capsulate.lang.program.grammar.Statement as GrammarStatement

@KParcelize
@Serializable
@OptIn(ExperimentalUuidApi::class)
sealed class ComposableStatement<T : GrammarStatement>(open val statement: T) : KParcelable {
    open fun toString(state: ParserState) = state[statement.match]

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
        val iconSize = DpSize(100.dp, 65.dp)
        val offset = (size - iconSize) / 2
        DraggableItem(
            modifier = if (draggable) modifier else
                modifier.pointerHoverIcon(PointerIcon.Hand, true),
            key = this@ComposableStatement,
            state = state.state,
            data = this@ComposableStatement,
            dragAfterLongPress = onMobile,
            draggableContent = {
                val active =
                    remember { MutableTransitionState(false).apply { targetState = true } }
                val animationSpec: AnimationSpec<*> = tween<Any>(150)
                AnimatedVisibility(
                    active,
                    Modifier.requiredSize(iconSize).offset(offset.width, offset.height)
                        .alpha(if (state.preview) 0.15f else 1f),
                    enter = scaleIn(animationSpec as FiniteAnimationSpec<Float>, .5f)
                ) {
                    this@ComposableStatement.Show(
                        Modifier.fillMaxSize().shadow(10.dp, clip = false)
                            .border(Theme.borderWidth, Theme.borderColor), false
                    )
                }
            },
            content = { content(isDragging) })
    } else content(false)

    @Composable
    abstract fun Show(
        modifier: Modifier = Modifier,
        draggable: Boolean = false,
        activeStatement: Uuid? = null
    )

    protected val StatementDragState.draggingInProgress
        get() = this.draggedItem != null && this.draggedItem != this@ComposableStatement

    @Composable
    protected fun DropTarget(state: StatementDragState, pos: Int) {
        if (state.draggingInProgress && state.draggedItem?.data != this) {
            val coroScope = rememberCoroutineScope()
            var job: Job? by remember { mutableStateOf(null) }
            val dropHandler = LocalStructogramDropHandler.current
            Box(
                Modifier.dropTarget(
                    key = this.statement,
                    state = state.state,
                    onDragEnter = {
                        job = coroScope.launch {
                            delay(150)
                            state.preview = true
                        }
                    },
                    onDrop = {
                        Logger.i {
                            "Dropped (${it.data.statement.match.start},${it.data.statement.match.end}) -> " +
                                    "$pos ${it.data.statement}"
                        }
                        dropHandler(it.data to pos)
                    },
                    onDragExit = { job?.cancel(); state.preview = false },
                ).defaultMinSize(minHeight = 20.dp).background(Color.Magenta)
            ) {
                Column {
                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        visible = state.preview && state.draggedItem != null &&
                                state.state.hoveredDropTargetKey == statement,
                        enter = expandIn(tween(300), Alignment.Center),
                        exit = fadeOut(tween(0))
                    ) {
                        state.draggedItem!!.data.Show(
                            Modifier.background(Color.Gray).alpha(0.6f),
                            false
                        )
                    }
                    if (state.preview && state.draggedItem != null && state.state.hoveredDropTargetKey == statement) {
                        HorizontalBorder()
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(2.dp)
                                .background(Color.Magenta.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun fromStatement(state: ParserState, statement: GrammarStatement): ComposableStatement<*> =
            when (statement) {
                is Atomic -> AtomicStatement(statement, state)
                is If -> IfStatement(statement, state)
                is When -> WhenStatement(statement, state)
                is Wait -> AwaitStatement(statement, state)
                is While -> LoopStatement(statement, state)
                is DoWhile -> LoopStatement(statement, state)
                is Parallel -> ParallelStatement(statement, state)
                is MethodCall -> MethodCallStatement(statement, state)
                else -> Command(statement, state)
            }
    }
}
