package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.lang.Assign
import com.erdodif.capsulate.lang.DoWhile
import com.erdodif.capsulate.lang.Expression
import com.erdodif.capsulate.lang.If
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.Skip
import com.erdodif.capsulate.lang.While

typealias StatementList = Array<Statement>

abstract class Statement {
    @Composable
    abstract fun show(modifier: Modifier)
    @Composable
    fun show() = show(Modifier)

    companion object { // TODO, should one statement implement it's representation
        fun fromTokenized(
            state: ParserState,
            statement: com.erdodif.capsulate.lang.Statement
        ): Statement = when (statement) {
            is If -> IfStatement(statement.condition.toString(),
                statement.statementsTrue.map { fromTokenized(state, it) }.toTypedArray(),
                statement.statementsFalse.map { fromTokenized(state, it) }.toTypedArray()
            )

            is Skip -> Command("SKIP")
            is Assign -> Command(statement.id + ":=" + statement.value.toString(state))
            is While -> LoopStatement(
                statement.condition.toString(state),
                statement.statements.map { fromTokenized(state, it) }.toTypedArray()
            )
            is DoWhile -> LoopStatement(
                statement.condition.toString(state),
                statement.statements.map { fromTokenized(state, it) }.toTypedArray()
            )

            is Expression -> Command("EXP: ${statement.expression.toString(state)}")
            else -> Command("UNSUPPORTED $statement")
        }
    }
}