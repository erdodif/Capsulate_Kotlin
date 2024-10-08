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

abstract class Statement(val statement: com.erdodif.capsulate.lang.grammar.Statement) {
    @Composable
    abstract fun show(modifier: Modifier)

    @Composable
    fun show() = show(Modifier)

    companion object { // TODO, should one statement implement it's representation
        fun fromStatement(
            state: ParserState,
            statement: com.erdodif.capsulate.lang.grammar.Statement
        ): Statement = when (statement) {
            is If -> IfStatement(statement, state)
            is Wait -> AwaitStatement(statement, state)
            is While -> LoopStatement(statement, state)
            is DoWhile -> LoopStatement(statement,state)
            is Parallel -> ParallelStatement(statement, state)
            is Expression -> Command("EXP: ${statement.expression.toString(state)}", statement)

            is Skip -> Command("SKIP", statement)
            is Abort -> Command("ABORT", statement)
            is Return -> Command("RETURN ${statement.value.toString(state)}", statement)
            is Assign -> Command(statement.id + ":=" + statement.value.toString(state), statement)
            is ParallelAssign -> Command(statement.assigns.map { it.first }.toString() + ":=" +
                    statement.assigns.map { it.second.toString(state) }.toString(), statement
            )

            else -> Command("UNSUPPORTED $statement", statement)
        }
    }
}