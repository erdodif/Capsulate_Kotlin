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
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.program.evaluation.ReturnEvaluation
import com.erdodif.capsulate.lang.program.grammar.expression.NEVER
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.newLined
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.sAbort
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.lang.program.grammar.statement
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
data class Function<out T : Value>(
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
data class FunctionCall<T : Value>(
    val name: String,
    val values: List<Exp<Value>>,
    val match: MatchPos
) : Exp<T> {
    constructor(function: Function<T>, values: List<Exp<Value>>, match: MatchPos) :
            this(function.name, values, match)

    override fun getType(assumptions: Map<String, Type>): Type =
        assumptions["function:$name:${values.size}"] ?: NEVER

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: Environment): Right<PendingExpression<Value, T>> = Right(
        PendingExpression(this as FunctionCall<Value>, context.functions[name]!!) { Left(it as T) })

    override fun toString(state: ParserState, parentStrength: Int) =
        values.joinToString(
            prefix = "$name(",
            separator = ", ",
            postfix = ")"
        ) { it.toString(state) }
}

val sFunction: Parser<Function<Value>> = {
    delimit(
        right(_keyword("function"), _nonKeyword) + middle(
            _char('('), optional(delimited(pVariable, _char(','))), _char(')')
        )
    )().fold({ (result, state) ->
        val (name, params) = result
        val label = "function:$name:${params?.count() ?: 0}"
        val tmpEnv = ParserState(
            this.input,
            this.functions + Function(name, params ?: emptyList(), listOf()),
            this.methods
        ).also {
            it.position = state.position
        }
        val blocks = tmpEnv.withReturn(
            label, delimit(
                orEither(
                    middle(
                        newLined(_char('{')),
                        some(delimit(statement)),
                        newLined(_char('}'))
                    ), orEither(sReturn, sAbort) / { arrayListOf(it) })
            )
        )
        state.semanticErrors.addAll(tmpEnv.semanticErrors)
        if (tmpEnv.assumptions.contains(label)) {
            state.assumptions[label] = tmpEnv.assumptions[label]!!
        }
        position = tmpEnv.position
        when (blocks) {
            is Pass -> {
                val function = Function<Value>(name, params ?: emptyList(), blocks.value)
                if (functions.any { it.name == name && params?.count() == it.parameters.count() }) {
                    raiseError("Function with ${params?.count() ?: 0} parameters already defined!")
                } else {
                    functions.add(function)
                }
                Pass(function, tmpEnv, MatchPos.ZERO)
            }

            is Fail -> blocks
        }
    }) { it }
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

val sReturn: Parser<Statement> = right(_keyword("return"), pExp)[{ (value, state, pos) ->
    if (allowReturn && currentFunctionLabel != null) {
        if (assumptions[currentFunctionLabel!!] != null) {
            if (assumptions[currentFunctionLabel!!] != value.getType(assumptions)) {
                raiseError(
                    "Type mismatch on return type ${value.getType(assumptions)}," +
                            "when the function is " +
                            "assumed to have return type ${assumptions[currentFunctionLabel!!]}"
                )
            }
        } else {
            assumptions[currentFunctionLabel!!] = value.getType(assumptions)
        }
        Pass(Return<Value>(value, match = pos), state, pos)
    } else Fail("Return is only allowed in a function block!", state)
}]

val sFunctionCall: Parser<Exp<Value>> = {
    (_nonKeyword + middle(_char('('), optional(delimited(pExp, _char(','))), _char(')')))[{
        val (name, params) = it.value
        val function =
            functions.firstOrNull {
                it.name == name && it.parameters.count() == (params?.count() ?: 0)
            }?.name
        if (function == null) {
            fail("Can't find any function named '$name'")
        } else {
            pass(it.match.start, FunctionCall<Value>(function, params ?: emptyList(), it.match))
        }
    }]()
}


