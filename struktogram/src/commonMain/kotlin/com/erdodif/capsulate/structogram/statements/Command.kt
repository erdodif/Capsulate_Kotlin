@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.ParallelAssign
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.function.MethodCall
import com.erdodif.capsulate.lang.program.grammar.function.Return
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.utility.theme.StatementText
import com.erdodif.capsulate.utility.theme.Theme
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize
import org.jetbrains.compose.ui.tooling.preview.Preview
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
            is Assign -> statement.getFormat(state)
            is ParallelAssign -> statement.getFormat(state)
            is MethodCall -> statement.getFormat(state)
            is Return<*> -> "RETURN ${statement.value.toString(state)}"
            else -> "UNSUPPORTED $statement"
        }, statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) = key(this) {
        var size by remember { mutableStateOf(DpSize.Zero) }
        val density = LocalDensity.current
        DraggableArea(modifier, draggable, size) { dragging ->
            Column(Modifier.height(IntrinsicSize.Min)) {
                if (!dragging && draggable) {
                    DropTarget(LocalDraggingStatement.current, statement.match.start)
                }
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
            }
        }
    }
}

@Preview
@Composable
private fun CommandPreview() {
    val command = Command("statement", Skip(MatchPos.ZERO))
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    PreviewColumn {
        labeled("Regular") { command.Show(modifier, false, null) }
        labeled("Active") { command.Show(modifier, false, command.statement.id) }
    }
}
