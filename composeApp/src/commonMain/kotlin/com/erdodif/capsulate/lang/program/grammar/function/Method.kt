@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar.function

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.delimit
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.statementOrBlock
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
data class Method(
    val pattern: Pattern,
    val program: List<Statement>
) : KParcelable {

    override fun toString(): String =
        "Method $pattern, { ... ${program.count()} statements}"


    fun onFormat(formatting: Formatting, state: ParserState): Int = with(formatting) {
        print("method " + pattern.toString(state))
        val result = preFormat { program.fencedForEach { it.onFormat(this, state) } }
        val lines = result.count()
        if (lines == 0) {
            appendAll(result)
            print(" }")
        } else {
            breakLine()
            indent {
                appendAll(result)
            }
            printLine("}")
        }
    }

}

@KParcelize
data class MethodCall(
    val method: Method,
    val values: List<Variable>,
    override val match: MatchPos,
    override val id: Uuid = Uuid.random()
) : Statement(id, match) {
    override fun evaluate(env: Env): EvaluationResult {
        return EvalSequence(method.program)
    }

    override fun toString(): String = "Call on $method"

    fun toString(state: ParserState): String = state[match]

    override fun Formatting.format(state: ParserState): Int = print(state[match])

}

val sMethod: Parser<Method> =
    (right(_keyword("method"), sPattern) + delimit(statementOrBlock)) / { pattern, block ->
        val method = Method(pattern, block)
        methods.add(method)
        method
    }

val sMethodCall: Parser<Statement> = delimit(sKnownPattern)[{
    val (pattern, params) = it.value
    val method = methods.firstOrNull { it.pattern == pattern }
    if (method == null) {
        fail("Can't find any method with the given pattern '$pattern'")
    } else {
        pass(it.match.start, MethodCall(method, params, it.match))
    }
}]
