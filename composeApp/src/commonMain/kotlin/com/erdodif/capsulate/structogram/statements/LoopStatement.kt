@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.Loop
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
private fun Condition(text: String, modifier: Modifier = Modifier, active: Boolean) =
    StatementText(
        text,
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)
            .conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) { active }
            .padding(Theme.commandPadding)
    )

@KParcelize
class LoopStatement(
    var condition: String,
    var statements: List<ComposableStatement<*>> = listOf(),
    var inOrder: Boolean = true,
    override val statement: Loop
) : ComposableStatement<Loop>(statement) {
    constructor(statement: While, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it) },
        true,
        statement
    )

    constructor(statement: DoWhile, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it) },
        false,
        statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var dragging by remember { mutableStateOf(false) }
        val active = statement.id == activeStatement
        val backgroundColor = if (active) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primary
        Column(
            modifier.dim(dragging).fillMaxWidth()
                .background(backgroundColor).onDpSize(density) { size = it }
        ) {
            if (!dragging && draggable && statements.isNotEmpty()) {
                DropTarget(LocalDraggingStatement.current, statement.match.start)
            }
            if (inOrder) {
                DraggableArea(Modifier.fillMaxWidth(), draggable, size)
                {
                    dragging = it
                    Condition(condition, Modifier.fillMaxWidth(), active)
                }
                HorizontalBorder(Modifier.padding(start = 32.dp))
            }
            Row(Modifier.height(IntrinsicSize.Min)) {
                if (draggable && !dragging) {
                    DraggableArea(Modifier.width(32.dp).fillMaxHeight(), draggable, size)
                    {
                        dragging = it
                    }
                } else {
                    Spacer(Modifier.width(32.dp))
                }
                VerticalBorder()
                Column(Modifier.fillMaxWidth()) {
                    StackWithSeparator(
                        statements,
                        {
                            it.Show(
                                Modifier.fillMaxWidth(),
                                draggable && !dragging,
                                activeStatement
                            )
                        }) { HorizontalBorder() }
                    if(statements.isEmpty()){
                        DropTarget(LocalDraggingStatement.current, statement.match.end - 2)
                    }
                }
            }
            if (!inOrder) {
                HorizontalBorder(Modifier.padding(start = 32.dp))
                DraggableArea(Modifier.fillMaxWidth(), draggable, size)
                {
                    dragging = it
                    Condition(condition, Modifier.fillMaxWidth(), active)
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoopPreview() {
    val pos = MatchPos.ZERO
    val tmpWhile =
        While(BoolLit(true, MatchPos(0, "condition".length)), arrayListOf(Skip(pos)), pos)
    val tmpDoWhile =
        While(BoolLit(true, MatchPos(0, "condition".length)), arrayListOf(Skip(pos)), pos)
    val statements = listOf(
        Command("statement 1", Skip(pos)),
        Command("statement 2", Skip(pos)),
        Command("...", Skip(pos)),
        Command("statement n", Skip(pos))
    )
    val whileStatement = LoopStatement("condition", statements, true, tmpWhile)
    val doWhileStatement = LoopStatement("condition", statements, false, tmpDoWhile)
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    PreviewColumn {
        labeled("While regular") { whileStatement.Show(modifier, false, null) }
        labeled("While active") {
            whileStatement.Show(
                modifier,
                false,
                whileStatement.statement.id
            )
        }
        labeled("DoWhile regular") { doWhileStatement.Show(modifier, false, null) }
        labeled("DoWhile active") {
            doWhileStatement.Show(
                modifier,
                false,
                doWhileStatement.statement.id
            )
        }
    }
}
