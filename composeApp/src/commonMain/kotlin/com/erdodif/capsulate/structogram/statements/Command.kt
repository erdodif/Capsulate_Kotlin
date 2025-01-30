package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.ParallelAssign
import com.erdodif.capsulate.lang.program.grammar.Return
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize

@KParcelize
class Command(var text: String, override val statement: com.erdodif.capsulate.lang.program.grammar.Statement) :
    Statement(statement) {
    constructor(statement: com.erdodif.capsulate.lang.program.grammar.Statement, state: ParserState) : this(
        when (statement) {
            is Skip -> "SKIP"
            is Abort -> "ABORT"
            is Return -> "RETURN ${statement.value.toString(state)}"
            is Expression -> "EXP: ${statement.expression.toString(state)}"
            is Assign -> "${statement.id} := ${statement.value.toString(state)}"
            is ParallelAssign -> statement.assigns.map { it.first }.toString() + " := " +
                    statement.assigns.map { it.second.toString(state) }.toString()

            else -> "UNSUPPORTED $statement"
        }, statement
    )

    @Composable
    override fun Show(modifier: Modifier, draggable: Boolean) = key(this){
        var size by remember { mutableStateOf(DpSize.Zero) }
        val density = LocalDensity.current
        if (draggable)
            DraggableArea(Modifier, draggable, size) { dragging ->
                StatementText(
                    text,
                    false,
                    Modifier.fillMaxSize().onDpSize(density) { size = it }.dim(dragging)
                        .padding(Theme.commandPadding)
                )
                DropTarget(LocalDraggingStatement.current)
            }
        else {
            StatementText(
                text,
                false,
                modifier.fillMaxWidth().padding(Theme.commandPadding)
            )
        }
    }
}