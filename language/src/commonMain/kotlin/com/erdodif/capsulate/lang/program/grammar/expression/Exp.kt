@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.program.grammar.and
import com.erdodif.capsulate.lang.program.grammar.anyChar
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.not
import com.erdodif.capsulate.lang.program.grammar.expression.operator.builtInOperatorTable
import com.erdodif.capsulate.lang.program.grammar.or
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.delimited
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.sFunctionCall
import com.erdodif.capsulate.lang.program.grammar.pIndex
import com.erdodif.capsulate.lang.program.grammar.pType
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._integer
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KParcelize
open class PendingExpression<R : Value, out T : Value>(
    open val call: FunctionCall<R>,
    open val function: Function<R>,
    open val onValue: @Serializable Environment.(R) -> Either<T, PendingExpression<Value, T>>
) : KParcelable {

    fun <S : Value> map(transform: Environment.(T) -> S): PendingExpression<R, S> =
        PendingExpression(call, function) {
            when (val result = onValue(it)) {
                is Left -> Left(transform(this, result.value))
                is Right -> Right(result.value.map(transform))
            }
        }

    fun <S : Value> addTransform(
        transform: Environment.(T) -> Either<S, PendingExpression<Value, S>>
    ): PendingExpression<R, S> = PendingExpression(call, function) _env@{
        when (val result = onValue(it)) {
            is Left -> transform(this, result.value)
            is Right -> Right(result.value.addTransform(transform))
        }
    }
}

interface Exp<out T : Value> : KParcelable {
    fun getType(assumptions: Map<String, Type>): Type
    fun evaluate(context: Environment): Either<T, PendingExpression<Value, T>>
    fun toString(state: ParserState, parentStrength: Int = 0): String
}

fun <T : Value, R : Value> List<Exp<T>>.withRawValue(
    env: Environment,
    onValue: Environment.(List<T>) -> R
) = withRawValue(env, emptyList(), onValue)

private fun <T : Value, R : Value> List<Exp<T>>.withRawValue(
    env: Environment,
    accumulated: List<T>,
    onValue: Environment.(List<T>) -> R
): Either<R, PendingExpression<Value, R>> = if (this.isEmpty()) {
    Left(onValue(env, accumulated))
} else {
    first().withValue(env) tmp@{
        this@withRawValue.drop(1).withRawValue(env, accumulated + it, onValue)
    }
}

fun <T : Value, R : Value> List<Exp<T>>.withValue(
    env: Environment,
    onValue: Environment.(List<T>) -> Either<R, PendingExpression<Value, R>>
) = withValue(env, emptyList(), onValue)

private fun <T : Value, R : Value> List<Exp<T>>.withValue(
    env: Environment,
    accumulated: List<T>,
    onValue: Environment.(List<T>) -> Either<R, PendingExpression<Value, R>>
): Either<R, PendingExpression<Value, R>> = if (this.isEmpty()) {
    onValue(env, accumulated)
} else {
    first().withValue(env) tmp@{ this@withValue.drop(1).withValue(env, accumulated + it, onValue) }
}

fun <R : Value, T : Value> Exp<T>.withRawValue(
    env: Environment,
    onValue: Environment.(T) -> R
): Either<R, PendingExpression<Value, R>> =
    when (val result = evaluate(env)) {
        is Right -> Right(result.value.map(onValue))
        is Left -> Left(onValue(env, result.value))
    }

fun <R : Value, T : Value> Exp<T>.withValue(
    env: Environment,
    onValue: Environment.(T) -> Either<R, PendingExpression<Value, R>>
): Either<R, PendingExpression<Value, R>> =
    when (val result = evaluate(env)) {
        is Left -> onValue(env, result.value)
        is Right -> Right(result.value.addTransform(onValue))
    }


fun <R : Value, T : Value, S : Value> Pair<Exp<T>, Exp<S>>.withValue(
    env: Environment,
    onValue: Environment.(T, S) -> Either<R, PendingExpression<Value, R>>
): Either<R, PendingExpression<Value, R>> = first.withValue(env) { a ->
    second.withValue(env) { b ->
        onValue(a, b)
    }
}

@KParcelize
@Serializable
@SerialName("token")
open class Token(open val match: MatchPos) : KParcelable {
    inline fun matchedToken(parserState: ParserState): String =
        parserState.input[match.start, match.end]

    open fun copy(match: MatchPos): Token = Token(match)
}

@KParcelize
data class KeyWord(val id: String, override val match: MatchPos) : Token(match) {
    override fun copy(match: MatchPos): Token = copy(id = id, match = match)
}

@KParcelize
data class Symbol(val id: Char, override val match: MatchPos) : Token(match) {
    override fun copy(match: MatchPos): Token = copy(id = id, match = match)
}

@KParcelize
data class LineEnd(val char: Char, override val match: MatchPos) : Token(match) {
    override fun copy(match: MatchPos): Token = copy(char = char, match = match)
}

@KParcelize
data class Comment(val content: String, override val match: MatchPos) : Token(match) {
    override fun copy(match: MatchPos): Token = copy(content = content, match = match)
}

@KParcelize
data class Assume(val id: String, val type: Type, override val match: MatchPos) : Token(match) {
    override fun copy(match: MatchPos): Token = copy(id = id, match = match)
}


val pComment: Parser<Comment> = orEither(
    right(
        (char('/') + _char('/')),
        left(many(right(not(char('\n')), anyChar)), or(_char('\n'), EOF))
    ),
    right(
        and(char('/'), _char('*')),
        left(many(right(not(char('*') + _char('/')), anyChar)), char('*') + _char('/'))
    )
) * { comment, pos -> Comment(comment.asString(), pos) }

val pChrLit: Parser<ChrLit> = middle(
    char('\''),
    orEither(right(char('\\'), anyChar), right(not(char('\'')), anyChar)),
    _char('\'')
) * { res, pos -> ChrLit(res, pos) }

val pStrLit: Parser<StrLit> = middle(
    char('"'),
    many(orEither(right(char('\\'), anyChar), right(not(char('"')), anyChar))),
    _char('"')
) * { res, pos -> StrLit(res.asString(), pos) }
val pIntLit: Parser<IntLit> = _integer * { lit, pos -> IntLit(lit, pos) }
val pBoolLit: Parser<BoolLit> =
    or(_keyword("true"), _keyword("false")) * { lit, pos -> BoolLit(lit is Left<*>, pos) }
val pVariable: Parser<Variable> = _nonKeyword[{
    if (it.value[0].isDigit()) fail("Variable name can't start with digit!")
    else pass(it.match.start, Variable(it.value, it.match))
}]

val pAssumption: Parser<Assume> = {
    (pVariable + right(_keyword("is"), pType))[{ (value, _, match) ->
        val (variable, type) = value
        assumptions[variable.id] = type
        pass(match.start, Assume(variable.id, type, match))
    }]()
}

val pArrayLit: Parser<ArrayLit<Value>> = {
    middle(_char('['), delimited(pExp, _char(',')), _char(']'))[{ (values, state, match) ->
        if (values.isNotEmpty() && values.any {
                it.getType(this.assumptions) != values.first().getType(this.assumptions)
            }) {
            Fail(
                "Type mismatch in Array literal, found types: ${
                    values.map { it.getType(this.assumptions) }.distinct().joinToString()
                }", state
            )
        } else {
            Pass(ArrayLit(values.toTypedArray(), match), state, match)
        }
    }]()
}

//val pIndex: Parser<Index> = or()

val litOrder: Array<Parser<Exp<*>>> = arrayOf(
    pIntLit,
    pBoolLit,
    pChrLit,
    pStrLit,
    pVariable,
    pArrayLit
)

typealias ExParser = Parser<Exp<Value>>

@Suppress("UNCHECKED_CAST", "SpreadOperator")
inline fun pAtom(): ExParser = {
    // Can't be directly assigned, or else the pExp reference -|
    //                                               v___v-----| would be null
    asum(
        sFunctionCall, pIndex, *litOrder, middle(_char('('), pExp, _char(')'))
    )()
}

val pExp: Parser<Exp<Value>> = builtInOperatorTable.parser(pAtom())
