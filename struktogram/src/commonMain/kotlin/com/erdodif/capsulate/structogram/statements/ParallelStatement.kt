@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.statement
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.utility.theme.HorizontalBorder
import com.erdodif.capsulate.utility.StackWithSeparator
import com.erdodif.capsulate.utility.theme.Theme
import com.erdodif.capsulate.utility.theme.VerticalBorder
import com.erdodif.capsulate.utility.theme.commandPlaceHolder
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class ParallelStatement(
    override val statement: Parallel,
    private vararg var blocks: Array<ComposableStatement<*>>
) : ComposableStatement<Parallel>(statement) {
    constructor(
        blocks: ArrayList<ArrayList<ComposableStatement<*>>>,
        statement: Parallel
    ) : this(
        statement,
        *blocks.map { it.toTypedArray() }.toTypedArray()
    )

    constructor(statement: Parallel, state: ParserState) : this(
        statement,
        *statement.blocks.map { block ->
            block.map { statement ->
                fromStatement(state, statement)
            }.toTypedArray()
        }.toTypedArray()
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var isDragging by remember { mutableStateOf(false) }
        Column {
            if (!isDragging && draggable) {
                DropTarget(LocalDraggingStatement.current, statement.match.start)
            }
            Row(
                modifier.dim(isDragging).background(MaterialTheme.colorScheme.primary)
                    .defaultMinSize(Dp.Unspecified, 30.dp).height(IntrinsicSize.Min)
                    .fillMaxWidth().onGloballyPositioned {
                        with(density) {
                            size = DpSize(it.size.width.toDp(), it.size.height.toDp())
                        }
                    }
            ) {
                StackWithSeparator(
                    blocks,
                    {
                        Column(Modifier.weight(1f, true).defaultMinSize(30.dp, Dp.Unspecified)) {
                            StackWithSeparator(
                                it,
                                { statement ->
                                    statement.Show(
                                        Modifier.fillMaxWidth(),
                                        draggable && !isDragging,
                                        activeStatement
                                    )
                                }, {
                                    commandPlaceHolder(Modifier)
                                }) {
                                HorizontalBorder()
                            }
                        }
                    }) {
                    DraggableArea(
                        Modifier.width(Theme.borderWidth * 4),
                        draggable,
                        size
                    ) { dragging ->
                        isDragging = dragging
                        Row(
                            Modifier.width(Theme.borderWidth * 4).conditional(
                                Modifier.background(
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                statement.id == activeStatement
                            },
                            horizontalArrangement = Arrangement.Center
                        ) {
                            VerticalBorder()
                            Spacer(Modifier.width(Theme.borderWidth * 2))
                            VerticalBorder()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ParallelPreview() = PreviewColumn {
    val pos = MatchPos.ZERO
    val parserState = ParserState("{ } | { }")
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    labeled("From { } | { }") {
        ComposableStatement.fromStatement(
            parserState,
            (with(parserState) { statement() } as Pass).value)
            .Show(modifier, false, null)
    }
    val inner = Skip(pos)
    val statement = Parallel(arrayListOf(arrayListOf(inner), arrayListOf(Abort(pos))), pos)
    labeled("Raw") {
        ParallelStatement(
            statement,
            arrayOf<ComposableStatement<*>>()
        ).Show(modifier, false, null)
    }
    labeled("Active") {
        ParallelStatement(
            statement,
            arrayOf<ComposableStatement<*>>(Command("S_1", inner)),
            arrayOf<ComposableStatement<*>>(Command("S_2", Abort(pos))),
        ).Show(modifier, false, statement.id)
    }
    labeled("Active inner") {
        ParallelStatement(
            statement,
            arrayOf<ComposableStatement<*>>(Command("S_1", inner)),
            arrayOf<ComposableStatement<*>>(Command("S_2", Abort(pos))),
        ).Show(modifier, false, inner.id)
    }
}
