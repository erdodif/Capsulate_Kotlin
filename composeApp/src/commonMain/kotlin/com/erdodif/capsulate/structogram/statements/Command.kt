package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.lang.grammar.Abort
import com.erdodif.capsulate.lang.grammar.Assign
import com.erdodif.capsulate.lang.grammar.Expression
import com.erdodif.capsulate.lang.grammar.ParallelAssign
import com.erdodif.capsulate.lang.grammar.Return
import com.erdodif.capsulate.lang.grammar.Skip
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme

class Command(var text: String, statement: com.erdodif.capsulate.lang.grammar.Statement) :
    Statement(statement) {
    constructor(statement: com.erdodif.capsulate.lang.grammar.Statement, state: ParserState) : this(
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
    override fun Content(modifier: Modifier, draggable: Boolean) = Row(modifier) {
        StatementText(text, false, Modifier.fillMaxWidth().padding(Theme.commandPadding))
    }
}