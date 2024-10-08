package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.grammar.DoWhile
import com.erdodif.capsulate.lang.grammar.While
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder

@Composable
private fun Condition(text: String) =
    StatementText(
        text,
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(Theme.commandPadding)
    )


class LoopStatement(
    var condition: String,
    var statements: StatementList = arrayOf(),
    var inOrder: Boolean = true,
    statement: com.erdodif.capsulate.lang.grammar.Statement
) : Statement(statement) {
    constructor(statement: While, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it) }.toTypedArray(),
        true,
        statement
    )

    constructor(statement: DoWhile, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statements.map { fromStatement(state, it) }.toTypedArray(),
        false,
        statement
    )

    @Composable
    override fun Content(modifier: Modifier, draggable: Boolean) =
        Column(
            modifier.fillMaxWidth().height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            if (inOrder) Condition(condition)
            Row(Modifier.weight(1f).fillMaxWidth()) {
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
            if (!inOrder) Condition(condition)
        }
}