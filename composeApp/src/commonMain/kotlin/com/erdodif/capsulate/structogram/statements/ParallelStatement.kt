package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.lang.grammar.Parallel
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder

class ParallelStatement(
    statement: com.erdodif.capsulate.lang.grammar.Statement,
    private vararg var blocks: StatementList
) : Statement(statement) {
    constructor(
        blocks: ArrayList<ArrayList<Statement>>,
        statement: com.erdodif.capsulate.lang.grammar.Statement
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
    override fun Content(modifier: Modifier, draggable: Boolean) =
        Row(modifier.background(MaterialTheme.colorScheme.primary).height(IntrinsicSize.Min).fillMaxWidth()) {
            StackWithSeparator(
                blocks,
                {
                    Column(Modifier.weight(1f, true)) {
                        StackWithSeparator(
                            it,
                            { statement -> statement.Content(Modifier.fillMaxWidth(),draggable) }) {
                            HorizontalBorder()
                        }
                    }
                }) {
                VerticalBorder()
                Spacer(Modifier.width(Theme.borderWidth))
                VerticalBorder()
            }
        }
}