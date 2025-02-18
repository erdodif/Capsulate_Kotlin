@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar.function

import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.Variable
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.not
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.lang.program.grammar.whiteSpace
import com.erdodif.capsulate.lang.util.AbortEvaluation
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvalSequence
import com.erdodif.capsulate.lang.util.EvaluationResult
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.freeWord
import com.erdodif.capsulate.lang.util.tok
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Pattern(val prefix: String?, val delimiters: List<String>, val postfix: String?) {
    override fun toString(): String {
        return buildString {
            append(prefix)
            delimiters.forEachIndexed { i, d -> append("$d\$${i + 1}") }
            append("\$${delimiters.count() + 1}")
            append(postfix)
        }
    }
}

data class Function<T>(
    val name: String,
    val parameters: List<Value>
) {

}

class FunctionCall<T : Value>(
    val function: Function<T>
) : Exp<T> {
    override fun evaluate(env: Env): T {
        TODO("Function evaluation context must be thought through")
    }

    override fun toString(state: ParserState): String {
        TODO("Not yet implemented")
    }
}

/*
Exaple:
    $sx, $dx, $x : read  ==> Pattern(null, [',';',']. ": read")
*/

val sPattern: Parser<Pattern> = (optional(tok(freeWord)) + many(
    tok(right(char('$'), _nonKeyword)) + tok(some(not(asum(char('$'), whiteSpace))))
) + tok(
    right(char('$'), _nonKeyword)
) + tok(optional(freeWord))) / { prefix, delimited, trailing, postfix ->
    Pattern(prefix, delimited.map { it.first } + trailing, postfix)
}

val sKnownPattern: Parser<Pair<Pattern, List<Variable>>> = {
    val start = this.position
    var params: MutableList<Variable>
    var out: Pair<Pattern, List<Variable>>? = null
    for (pattern in patterns) {
        if (pattern.prefix != null && tryParse(_keyword(pattern.prefix)) is Fail) {
            continue
        }
        params = mutableListOf<Variable>()
        for (delimiter in pattern.delimiters) {
            if (tryParse(_keyword(delimiter)) is Fail) {
                break
            }
            val delimiterResult = tryParse(freeWord)
            if (delimiterResult is Pass) {
                params.add(Variable(delimiterResult.value, delimiterResult.match))
            } else {
                break
            }
        }
        if (params.count() == pattern.delimiters.count() && (pattern.postfix == null || tryParse(_keyword(pattern.postfix)) is Fail)) {
            out = pattern to params
        }
    }
    if (out != null) {
        this.pass(start, out)
    } else {
        this.fail("Failed to match with any of the known patterns")
    }
}

val sFunction: Parser<Function<*>> = TODO()
   // (right(_keyword("function:"), delimit(sPattern)) + statementOrBlock) / { Function() }

val sFunctionCall: Parser<Statement> = TODO()

class Method()

class MethodCall(val pattern: Pattern, override val id: Uuid = Uuid.random()) : Statement() {
    override fun evaluate(env: Env): EvaluationResult {
        val method = env.methods[pattern]
        return if (method == null) {
            AbortEvaluation("Definition for method $pattern not found")
        } else {
            EvalSequence(method.toList())
        }
    }
}

val sMethod: Parser<Method> = TODO()
//(or(_keyword("function:"), _keyword("method:")) + statementOrBlock) / { Function() }

val sMethodCall: Parser<Statement> = TODO()
// (_nonKeyword + _char('(') + _char(')')) / { TODO() }
