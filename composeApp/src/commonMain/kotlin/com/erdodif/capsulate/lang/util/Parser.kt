@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED")

package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize

@KParcelize
data class MatchPos(val start: Int, val end: Int) : KParcelable{
    companion object Constants{
        val ZERO = MatchPos(0,0)
    }
}

sealed interface Either<out T, out R>

typealias Value<T> = Left<T>
typealias Error<R> = Right<R>

data class Left<out T>(val value: T) : Either<T, Nothing>
data class Right<out R>(val value: R) : Either<Nothing, R>

inline operator fun <T, R, S> Either<T, R>.get(
    crossinline left: (Left<T>) -> S,
    crossinline right: (Right<R>) -> S
): S = when (this) {
    is Left<T> -> left(this)
    is Right<R> -> right(this)
}

open class ParserState(val input: String) {
    var position: Int = 0

    /**
     * Runs the parser on this context
     */
    fun <T> parse(parser: Parser<T>): ParserResult<T> = parser()

    /**
     * Tries to run the parser
     *
     * On failure, the position is reset
     */
    fun <T> tryParse(parser: Parser<T>): ParserResult<T> {
        val pos = position
        val result = parser()
        if (result is Fail) {
            position = pos
        }
        return result
    }

    inline fun pass(start: Int): Pass<Unit> = Pass(Unit, this, MatchPos(start, position))

    inline fun <T> pass(start: Int, value: T): Pass<T> =
        Pass(value, this, MatchPos(start, position))

    inline fun fail(reason: String): Fail = Fail(reason, this)

    override fun toString(): String = buildString {
        append("pos: $position, text:\"")
        if (position > 1) {
            append(input.substring(0..<position))
        }
        if (position < input.length) {
            append(input[position])
            append("⃰")
            if (position < input.length - 1) {
                append(input.substring(position + 1))
            }
            append('"')
        } else {
            append("\"<")
        }
    }

    operator fun get(start: Int, end: Int): String = input[start, end]
    operator fun get(match: MatchPos): String = input.get(match.start, match.end)
}

sealed class ParserResult<out T>(open val state: ParserState) {
    /**
     * Calls the given transformation [lambda] if [Pass], or does nothing on [Fail]
     */
    inline fun <R> fMap(crossinline lambda: ParserState.(T) -> R): ParserResult<R> = when (this) {
        is Pass -> Pass(lambda(state, value), state, match)
        is Fail -> this
    }

    /**
     * Calls the given transformation [lambda] using the match [MatchPos] as extra context.
     *
     * Transforms the result on [Pass], or does nothing on [Fail]
     */
    inline fun <R> fMapPos(crossinline lambda: ParserState.(T, MatchPos) -> R): ParserResult<R> =
        when (this) {
            is Pass -> Pass(lambda(state, value, match), state, match)
            is Fail -> this
        }
}

data class Pass<T>(val value: T, override val state: ParserState, val match: MatchPos) :
    ParserResult<T>(state) {
    override fun toString(): String = "Pass(value: $value, matched:${state[match]}, state: $state)"
}

data class Fail(val reason: String, override val state: ParserState) :
    ParserResult<Nothing>(state) {
    override fun toString(): String = "Fail(reason: $reason, state: ($state))"
}

/**
 * A function that tries to create T value from the current [ParserState] context
 */
typealias Parser<T> = ParserState.() -> ParserResult<T>
/**
 * A function that tries to create T value from the current [ParserState] context
 */
typealias ParserWith<T, R> = ParserState.(R) -> ParserResult<T>
/**
 * A [Parser] that will always pass
 */
typealias SuccessParser<T> = ParserState.() -> Pass<T>

/**
 * Calls the given transformation [lambda] on the result if the parser passes
 */
inline fun <T, R> Parser<T>.fMap(crossinline lambda: ParserState.(T) -> R): Parser<R> = {
    when (val res = this@fMap()) {
        is Pass -> res.fMap(lambda)
        is Fail -> res
    }
}

/**
 * Calls the given transformation [lambda] on the result if the parser passes
 */
inline fun <T, R> Parser<T>.fMapPos(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R> =
    {
        when (val res = this@fMapPos()) {
            is Pass -> res.fMapPos(lambda)
            is Fail -> res
        }
    }

inline operator fun <T, R> Parser<T>.times(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R> =
    fMapPos(lambda)

inline operator fun <T, R> Parser<T>.div(crossinline lambda: ParserState.(T) -> R): Parser<R> =
    fMap(lambda)

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<out T>) -> ParserResult<R>,
    crossinline onFail: ParserState.(Fail) -> ParserResult<R>
): Parser<R> = {
    val res = this@get()
    if (res is Pass) {
        onPass(res)
    } else onFail(res as Fail)
}

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<out T>) -> ParserResult<R>
): Parser<R> = {
    when (val res = this@get()) {
        is Pass -> onPass(res)
        is Fail -> res
    }
}

/**
 * Tries to apply the clean [lambda] if the result is present and not null
 */
inline fun <T, R> Parser<T?>.applyIf(crossinline lambda: ParserState.(T) -> R): Parser<R?> = fMap {
    if (it != null) lambda(it) else null
}

/**
 * Tries to apply the clean [lambda] if the result is present and not null [MatchPos] for extra context
 */
inline fun <T, R> Parser<T?>.applyIfPos(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R?> =
    fMapPos { it, pos ->
        @Suppress("UNCHECKED_CAST")
        if (it == null) null else lambda(it as T, pos)
    }

inline fun <T> asum(vararg parsers: Parser<T>): Parser<T> = {
    val pos = position
    var result: ParserResult<T> = fail("Nothing matched")
    for (factory in parsers) {
        when (val tmpResult = factory()) {
            is Fail -> {
                result = fail("${tmpResult.reason};\n${(result as Fail).reason}")
                position = pos
            }

            else -> {
                result = tmpResult
                break
            }
        }
    }
    result
}