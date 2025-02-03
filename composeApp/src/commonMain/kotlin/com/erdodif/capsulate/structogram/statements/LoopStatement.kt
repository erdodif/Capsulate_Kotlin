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
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.Loop
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
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
    var statements: List<Statement<*>> = listOf(),
    var inOrder: Boolean = true,
    override val statement: UniqueStatement<Loop>
) : Statement<Loop>(statement) {
    constructor(statement: While, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it.unique()) },
        true,
        statement.unique()
    )

    constructor(statement: DoWhile, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it.unique()) },
        false,
        statement.unique()
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: AnyUniqueStatement?
    ) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var dragging by remember { mutableStateOf(false) }
        val active = statement == activeStatement
        val backgroundColor = if (active) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primary
        Column(
            modifier.dim(dragging).fillMaxWidth().height(IntrinsicSize.Min)
                .background(backgroundColor).onDpSize(density) { size = it }
        ) {
            if (inOrder) { // TODO: This does not work
                DraggableArea(Modifier.fillMaxWidth(), draggable, size)
                {
                    dragging = it
                    Condition(condition, Modifier.fillMaxWidth(), active)
                }
            }
            Row(Modifier.weight(1f, true)) {
                Spacer(Modifier.width(32.dp).fillMaxHeight())
                VerticalBorder()
                Column(Modifier.fillMaxWidth()) {
                    if (inOrder) HorizontalBorder()
                    StackWithSeparator(
                        statements,
                        {
                            it.Show(Modifier.fillMaxWidth(), draggable)
                        }) { HorizontalBorder() }
                    if (!inOrder) HorizontalBorder()
                }
            }
            if (!inOrder) {
                DraggableArea(Modifier.fillMaxWidth(), draggable, size)
                {
                    Condition(condition, Modifier.fillMaxWidth(), active)
                }
            }
        }
    }
}

@Preview
@Composable
fun LoopPreview() {
    val tmpWhile = While(BoolLit(true, MatchPos(0, "condition".length)), arrayListOf(Skip))
    val tmpDoWhile = While(BoolLit(true, MatchPos(0, "condition".length)), arrayListOf(Skip))
    val statements = listOf(
        Command("statement 1", Skip.unique()),
        Command("statement 2", Skip.unique()),
        Command("...", Skip.unique()),
        Command("statement n", Skip.unique())
    )
    val whileStatement = LoopStatement("condition", statements, true, tmpWhile.unique())
    val doWhileStatement = LoopStatement("condition", statements, false, tmpDoWhile.unique())
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    PreviewColumn {
        labeled("While regular") { whileStatement.Show(modifier, false, null)}
        labeled("While active") { whileStatement.Show(modifier, false, whileStatement.statement)}
        labeled("DoWhile regular") { doWhileStatement.Show(modifier, false, null)}
        labeled("DoWhile active") { doWhileStatement.Show(modifier, false, doWhileStatement.statement)}
    }
}
