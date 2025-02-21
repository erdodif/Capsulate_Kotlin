@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar.function

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.Variable
import com.erdodif.capsulate.lang.program.grammar.delimit
import com.erdodif.capsulate.lang.program.grammar.delimited
import com.erdodif.capsulate.lang.program.grammar.delimited2
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.pExp
import com.erdodif.capsulate.lang.program.grammar.pVariable
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.statementOrBlock
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvaluationResult
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@KParcelize
data class Function<T>(
    val name: String,
    val parameters: List<Variable>,
    val body: List<Statement>
) : KParcelable {

    override fun toString(): String =
        "Function $name, (${parameters.count()} parameters) { ... ${body.count()} statements}"

}

@KParcelize
class FunctionCall<T : Value>(
    val function: Function<T>,
    val match: MatchPos
) : Exp<T> {
    override fun evaluate(env: Env): T {
        TODO("Function evaluation context must be thought through")
    }

    override fun toString(state: ParserState): String = state[match]
}

@KParcelize
data class Return @OptIn(ExperimentalUuidApi::class) constructor(
    val value: Value,
    override val id: Uuid = Uuid.random()
) : Statement(id) {
    override fun evaluate(env: Env): EvaluationResult {
        TODO("Not yet implemented")
    }

}

val sFunction: Parser<Function<Value>> = (delimit(
    right(_keyword("function"), _nonKeyword) + middle(
        _char('('),
        optional(delimited(pVariable, _char(','))),
        _char(')')
    )
) + statementOrBlock) / { name, params, body ->
    val function = Function<Value>(name, params ?: emptyList(), body)
    functions.add(function)
    function
}

val sFunctionCall: Parser<Exp<Value>> =
    delimit((_nonKeyword + middle(_char('('), delimited(pExp, _char(',')), _char(')'))))[{
        val (name, params) = it.value
        val function =
            functions.firstOrNull { it.name == name && it.parameters.count() == params.count() }
        if (function == null) {
            fail("Can't find any function named '$name'")
        } else {
            pass(it.match.start, FunctionCall<Value>(function, it.match))
        }
    }]

