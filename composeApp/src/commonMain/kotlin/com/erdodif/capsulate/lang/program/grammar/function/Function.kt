@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar.function

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.delimit
import com.erdodif.capsulate.lang.program.grammar.delimited
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.expression.pExp
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.statementOrBlock
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.program.evaluation.ReturnEvaluation
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
data class Function<T>(
    val name: String,
    val parameters: List<Variable>,
    val body: List<Statement>,
) : KParcelable {

    override fun toString(): String =
        "$name(params: ${parameters.count()}) { statements: ${body.count()} }"

    fun getHead(): String = buildString {
        append("$name(")
        if (parameters.isNotEmpty()) {
            for (i in 0..<parameters.count() - 1) {
                append(parameters[i].id)
                append(", ")
            }
            append(parameters[parameters.count() - 1].id)
        }
        append(')')
    }

    fun onFormat(formatting: Formatting, state: ParserState): Int = with(formatting) {
        print(parameters.joinToString(", ", prefix = "function $name(", postfix = ") {") { it.id })
        val result = preFormat { body.fencedForEach { it.onFormat(this, state) } }
        val lines = result.count()
        if (lines == 0) {
            appendAll(result)
            print(" }")
        } else {
            indent {
                appendAll(result)
            }
            append("}")
        }
    }
}

@KParcelize
class FunctionCall<T : Value>(
    val function: Function<T>,
    val values: List<Exp<Value>>,
    val match: MatchPos
) : Exp<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: Environment): Right<PendingExpression<Value, T>> =
        Right(PendingExpression(this as FunctionCall<Value>) { Left(it as T) })

    override fun toString(state: ParserState, parentStrength: Int): String =
        "${function.name}(" + buildString {
            values.forEach {
                append(it.toString(state))
                append(", ")
            }
        }.dropLast(2) + ")"
}

val sFunction: Parser<Function<Value>> = (delimit(
    right(_keyword("function"), _nonKeyword) + middle(
        _char('('),
        optional(delimited(pVariable, _char(','))),
        _char(')')
    )
) + delimit(statementOrBlock)) / { name, params, body ->
    val function = Function<Value>(name, params ?: emptyList(), body)
    functions.add(function)
    function
}


@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class Return<T : Value> @OptIn(ExperimentalUuidApi::class) constructor(
    val value: Exp<T>,
    override val id: Uuid = Uuid.random(),
    override val match: MatchPos
) : Statement(id, match) {
    @OptIn(ExperimentalUuidApi::class)
    override fun evaluate(env: Environment): EvaluationResult = value.join(env) { returnValue: T ->
        ReturnEvaluation(returnValue)
    }

    override fun Formatting.format(state: ParserState): Int =
        print("return " + value.toString(state))
}

val sReturn: Parser<Statement> = right(_keyword("return"), pExp) * { value, pos ->
    Return<Value>(value, match = pos)
}

val sFunctionCall: Parser<Exp<Value>> = {
    (_nonKeyword + middle(_char('('), optional(delimited(pExp, _char(','))), _char(')')))[{
        val (name, params) = it.value
        val function =
            functions.firstOrNull {
                it.name == name && it.parameters.count() == (params?.count() ?: 0)
            }
        if (function == null) {
            fail("Can't find any function named '$name'")
        } else {
            pass(it.match.start, FunctionCall<Value>(function, params ?: emptyList(), it.match))
        }
    }]()
}


