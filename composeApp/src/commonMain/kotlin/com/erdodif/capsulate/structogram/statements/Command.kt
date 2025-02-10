@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.ParallelAssign
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.erdodif.capsulate.lang.program.grammar.Statement as GrammarStatement

@KParcelize
class Command(
    var text: String,
    override val statement: GrammarStatement
) : ComposableStatement<GrammarStatement>(statement) {
    constructor(
        statement: GrammarStatement,
        state: ParserState
    ) : this(
        when (statement) {
            is Skip -> "SKIP"
            is Abort -> "ABORT"
            is Expression -> "EXP: ${statement.expression.toString(state)}"
            is Assign -> "${statement.label} := ${statement.value.toString(state)}"
            is ParallelAssign -> statement.assigns.map { it.first }.toString() + " := " +
                    statement.assigns.map { it.second.toString(state) }.toString()

            else -> "UNSUPPORTED $statement"
        }, statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) =
        key(this) {
            var size by remember { mutableStateOf(DpSize.Zero) }
            val density = LocalDensity.current
            if (draggable)
                DraggableArea(modifier, draggable, size) { dragging ->
                    StatementText(
                        text,
                        false,
                        Modifier.fillMaxSize().onDpSize(density) { size = it }.dim(dragging)
                            .conditional(
                                Modifier.background(MaterialTheme.colorScheme.tertiary)
                                    .border(3.dp, MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                statement.id == activeStatement
                            }.padding(Theme.commandPadding)
                    )
                    DropTarget(LocalDraggingStatement.current)
                }
            else {
                Column(modifier) {
                    StatementText(
                        text,
                        false,
                        Modifier.fillMaxWidth().conditional(
                            Modifier.background(MaterialTheme.colorScheme.tertiary)
                                .border(3.dp, MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            statement.id == activeStatement
                        } .padding(Theme.commandPadding)
                    )
                }
            }
        }
}

@Preview
@Composable
fun CommandPreview() {
    val command = Command("statement", Skip())
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    PreviewColumn {
        labeled("Regular") { command.Show(modifier, false, null) }
        labeled("Active") { command.Show(modifier, false, command.statement.id) }
    }
}
