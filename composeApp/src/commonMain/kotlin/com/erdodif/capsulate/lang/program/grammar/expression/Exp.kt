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
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.function.sFunctionCall
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
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
open class PendingExpression<R : Value, T : Value>(
    open val call: FunctionCall<R>,
    open val onValue: @Serializable Env.(R) -> Either<T, PendingExpression<Value, T>>
) : KParcelable {

    fun <S : Value> map(transform: Env.(T) -> S): PendingExpression<R, S> =
        PendingExpression(call) {
            when (val result = onValue(it)) {
                is Left -> Left(transform(this, result.value))
                is Right -> Right(result.value.map(transform))
            }
        }

    fun <S : Value> addTransform(transform: Env.(T) -> Either<S, PendingExpression<Value, S>>): PendingExpression<R, S> =
        PendingExpression(call) _env@{
            when (val result = onValue(it)) {
                is Left -> transform(this, result.value)
                is Right -> Right(result.value.addTransform(transform))
            }
        }

}

interface Exp<T : Value> : KParcelable {

    fun evaluate(context: Env): Either<T, PendingExpression<Value, T>>
    fun toString(state: ParserState): String
}

fun <R : Value, T : Value> Exp<T>.withRawValue(
    env: Env,
    onValue: Env.(T) -> R
): Either<R, PendingExpression<Value, R>> =
    when (val result = evaluate(env)) {
        is Right -> Right(result.value.map(onValue))
        is Left -> Left(onValue(env, result.value))
    }

fun <R : Value, T : Value> Exp<T>.withValue(
    env: Env,
    onValue: Env.(T) -> Either<R, PendingExpression<Value, R>>
): Either<R, PendingExpression<Value, R>> =
    when (val result = evaluate(env)) {
        is Left -> onValue(env, result.value)
        is Right -> Right(result.value.addTransform(onValue))
    }


fun <R : Value, T : Value, S : Value> Pair<Exp<T>, Exp<S>>.withValue(
    env: Env,
    onValue: Env.(T, S) -> Either<R, PendingExpression<Value, R>>
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

val pComment: Parser<Comment> = orEither(
    right(
        (char('/') + _char('/')),
        left(many(right(not(char('\n')), anyChar)), or(_char('\n'), EOF))
    ),
    right(
        and(char('/'), _char('*')),
        left(many(right(not(char('*') + _char('/')), anyChar)), char('*') + _char('/'))
    )
) * { it, pos -> Comment(it.asString(), pos) }


val pStrLit: Parser<StrLit> = middle(
    char('"'),
    many(orEither(right(char('\\'), anyChar), right(not(char('"')), anyChar))),
    _char('"')
) * { res, pos -> StrLit(res.asString(), pos) }
val pIntLit: Parser<IntLit> = _integer * { it, pos -> IntLit(it, pos) }
val pBoolLit: Parser<BoolLit> =
    or(_keyword("true"), _keyword("false")) * { it, pos -> BoolLit(it is Left<*>, pos) }
val pVariable: Parser<Variable> = _nonKeyword[{
    if (it.value[0].isDigit()) fail("Variable name can't start with digit!")
    else pass(it.match.start, Variable(it.value, it.match))
}]
val litOrder: Array<Parser<Exp<*>>> = arrayOf(
    pIntLit,
    pBoolLit,
    pStrLit,
    pVariable
)
typealias ExParser = Parser<Exp<Value>>

@Suppress("UNCHECKED_CAST")
inline fun pAtom(): ExParser = {
    // Can't be directly assigned, or else the pExp reference -|
    //                                               v___v-----| would be null
    asum(
        sFunctionCall, *litOrder, middle(_char('('), pExp, _char(')'))
    )() as ParserResult<Exp<Value>>
}

val pExp: Parser<Exp<Value>> = builtInOperatorTable.parser(pAtom())