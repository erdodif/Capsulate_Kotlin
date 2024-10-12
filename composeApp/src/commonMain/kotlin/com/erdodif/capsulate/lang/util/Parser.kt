@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED")

package com.erdodif.capsulate.lang.util

data class MatchPos(val start: Int, val end: Int)

sealed interface Either<T, R>
data class Left<T, R>(val value: T) : Either<T, R>
data class Right<T, R>(val value: R) : Either<T, R>

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

    inline fun <T> fail(reason: String): Fail<T> = Fail(reason, this)

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

sealed class ParserResult<T>(open val state: ParserState) {
    /**
     * Calls the given transformation [lambda] if [Pass], or does nothing on [Fail]
     */
    inline fun <R> fMap(crossinline lambda: ParserState.(T) -> R): ParserResult<R> =
        if (this is Pass) {
            Pass(lambda(state, value), state, match)
        } else {
            this as Fail
            this.to()
        }

    /**
     * Calls the given transformation [lambda] using the match [MatchPos] as extra context.
     *
     * Transforms the result on [Pass], or does nothing on [Fail]
     */
    inline fun <R> fMapPos(crossinline lambda: ParserState.(T, MatchPos) -> R): ParserResult<R> =
        if (this is Pass) {
            Pass(lambda(state, value, match), state, match)
        } else {
            this as Fail
            this.to()
        }
}

data class Pass<T>(val value: T, override val state: ParserState, val match: MatchPos) :
    ParserResult<T>(state) {
    override fun toString(): String = "Pass(value: $value, matched:${state[match]}, state: $state)"
}

data class Fail<T>(val reason: String, override val state: ParserState) : ParserResult<T>(state) {
    @Suppress("UNCHECKED_CAST")
    fun <R> to(): Fail<R> = this as Fail<R>

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
    val res = this@fMap()
    if (res is Pass) res.fMap(lambda)
    else (res as Fail).to()
}

/**
 * Calls the given transformation [lambda] on the result if the parser passes
 */
inline fun <T, R> Parser<T>.fMapPos(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R> =
    {
        val res = this@fMapPos()
        if (res is Pass) res.fMapPos(lambda)
        else (res as Fail).to()
    }

inline operator fun <T, R> Parser<T>.times(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R> =
    fMapPos(lambda)

inline operator fun <T, R> Parser<T>.div(crossinline lambda: ParserState.(T) -> R): Parser<R> =
    fMap(lambda)

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<T>) -> ParserResult<R>,
    crossinline onFail: ParserState.(Fail<T>) -> ParserResult<R>
): Parser<R> = {
    val res = this@get()
    if (res is Pass) {
        onPass(res)
    } else onFail(res as Fail)
}

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<T>) -> ParserResult<R>
): Parser<R> = {
    val res = this@get()
    if (res is Pass) {
        onPass(res)
    } else (res as Fail).to()
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
        if (it != null) lambda(it, pos) else null
    }

inline fun <T> asum(vararg parsers: Parser<T>): Parser<T> = {
    val pos = position
    var result: ParserResult<T> = fail("Nothing matched")
    for (factory in parsers) {
        val tmpResult = factory()
        if (tmpResult is Fail<T>) {
            result = fail("${tmpResult.reason};\n${(result as Fail).reason}")
            position = pos
        } else {
            result = tmpResult
            break
        }
    }
    result
}