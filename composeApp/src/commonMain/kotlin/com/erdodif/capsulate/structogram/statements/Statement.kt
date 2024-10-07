package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.lang.grammar.Abort
import com.erdodif.capsulate.lang.grammar.Assign
import com.erdodif.capsulate.lang.grammar.DoWhile
import com.erdodif.capsulate.lang.grammar.Expression
import com.erdodif.capsulate.lang.grammar.If
import com.erdodif.capsulate.lang.grammar.Parallel
import com.erdodif.capsulate.lang.grammar.ParallelAssign
import com.erdodif.capsulate.lang.grammar.Return
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.grammar.Skip
import com.erdodif.capsulate.lang.grammar.Wait
import com.erdodif.capsulate.lang.grammar.While

typealias StatementList = Array<Statement>

abstract class Statement {
    @Composable
    abstract fun show(modifier: Modifier)

    @Composable
    fun show() = show(Modifier)

    companion object { // TODO, should one statement implement it's representation
        fun fromTokenized(
            state: ParserState,
            statement: com.erdodif.capsulate.lang.grammar.Statement
        ): Statement = when (statement) {
            is If -> IfStatement(
                statement.condition.toString(state),
                statement.statementsTrue.map { fromTokenized(state, it) }.toTypedArray(),
                statement.statementsFalse.map { fromTokenized(state, it) }.toTypedArray()
            )

            is Skip -> Command("SKIP")
            is Abort -> Command("ABORT")
            is Wait -> AwaitStatement(statement.condition.toString(state))
            is Return -> Command("RETURN ${statement.value.toString(state)}")
            is Assign -> Command(statement.id + ":=" + statement.value.toString(state))
            is ParallelAssign -> Command(statement.assigns.map { it.first }.toString() + ":=" +
                    statement.assigns.map { it.second.toString(state) }.toString()
            )

            is While -> LoopStatement(
                statement.condition.toString(state),
                statement.statements.map { fromTokenized(state, it) }.toTypedArray()
            )

            is DoWhile -> LoopStatement(
                statement.condition.toString(state),
                statement.statements.map { fromTokenized(state, it) }.toTypedArray(),
                false
            )

            is Parallel -> ParallelStatement(*statement.blocks.map{ block ->
                   block.map { statement ->
                       fromTokenized(state, statement)
                   }.toTypedArray()
            }.toTypedArray())

            is Expression -> Command("EXP: ${statement.expression.toString(state)}")
            else -> Command("UNSUPPORTED $statement")
        }
    }
}