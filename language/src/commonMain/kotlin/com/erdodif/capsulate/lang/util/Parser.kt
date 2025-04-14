@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED", "NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.grammar.LineError
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import kotlin.math.min

open class ParserState(
    val input: String,
    functions: List<Function<Value>> = listOf(),
    methods: List<Method> = listOf(),
    assumptions: List<Pair<String, Type>> = listOf()
) {
    var allowReturn: Boolean = false
        protected set

    val functions: MutableList<Function<Value>> = functions.toMutableList()
    val methods: MutableList<Method> = methods.toMutableList()
    val assumptions: MutableMap<String, Type> = assumptions.toMap().toMutableMap()
    val semanticErrors: MutableList<LineError> = ArrayList<LineError>()
    val line: Int
        get() = input.substring(0, position).count { it == '\n' } + 1
    var currentFunctionLabel: String? = null

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
            append(input.substring(0..<min(position, input.length)))
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

    internal inline fun <T> withReturn(label:String,crossinline parser: Parser<T>): ParserResult<T> {
        allowReturn = true
        currentFunctionLabel = label
        val result = parser()
        currentFunctionLabel = null
        allowReturn = false
        return result
    }

    inline fun raiseError(cause: String) = semanticErrors.add(LineError(cause, line))

    operator fun get(start: Int, end: Int): String = input[start, end]
    operator fun get(match: MatchPos): String = input[match.start, match.end]
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

    inline fun <R> fold(crossinline onPass: (Pass<T>) -> R, crossinline onFail: (Fail) -> R): R =
        when (this) {
            is Pass -> onPass(this)
            is Fail -> onFail(this)
        }

    inline fun mapFail(crossinline onFail: (Fail) -> Fail): ParserResult<T> = fold({ it }, onFail)

    fun toEither(): Either<T, String> = when (this) {
        is Pass -> Left(this.value)
        is Fail -> Right(this.reason)
    }

    fun passOrNull(): Pass<T>? = when (this) {
        is Pass -> this
        is Fail -> null
    }

    fun failOrNull(): Fail? = when (this) {
        is Fail -> this
        is Pass -> null
    }
}

data class Pass<out T>(val value: T, override val state: ParserState, val match: MatchPos) :
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

inline operator fun <A, R> Parser<A>.times(crossinline lambda: ParserState.(A, MatchPos) -> R): Parser<R> =
    fMapPos(lambda)

inline operator fun <A, R> Parser<A>.div(crossinline lambda: ParserState.(A) -> R): Parser<R> =
    fMap(lambda)

inline operator fun <A, B, R> Parser<Pair<A, B>>.div(crossinline lambda: ParserState.(A, B) -> R): Parser<R> =
    fMap { lambda(it.first, it.second) }

inline operator fun <A, B, C, R> Parser<Pair<Pair<A, B>, C>>.div(
    crossinline lambda: ParserState.(A, B, C) -> R
): Parser<R> = fMap { lambda(it.first.first, it.first.second, it.second) }

inline operator fun <A, B, C, D, R> Parser<Pair<Pair<Pair<A, B>, C>, D>>.div(
    crossinline lambda: ParserState.(A, B, C, D) -> R
): Parser<R> =
    fMap { lambda(it.first.first.first, it.first.first.second, it.first.second, it.second) }

// Until nested destructuring is supported by kotlin
inline operator fun <A, B, C, D, E, R> Parser<Pair<Pair<Pair<Pair<A, B>, C>, D>, E>>.div(
    crossinline lambda: ParserState.(A, B, C, D, E) -> R
): Parser<R> = fMap {
    lambda(
        it.first.first.first.first, it.first.first.first.second, it.first.first.second,
        it.first.second,
        it.second
    )
}

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<T>) -> ParserResult<R>,
    crossinline onFail: ParserState.(Fail) -> ParserResult<R>
): Parser<R> = {
    val res = this@get()
    if (res is Pass) {
        onPass(res)
    } else onFail(res as Fail)
}

inline operator fun <T, R> Parser<T>.get(
    crossinline onPass: ParserState.(Pass<T>) -> ParserResult<R>
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
@Suppress("UNCHECKED_CAST")
inline fun <T, R> Parser<T?>.applyIfPos(crossinline lambda: ParserState.(T, MatchPos) -> R): Parser<R?> =
    fMapPos { result, pos ->
        if (result == null) null else lambda(result as T, pos)
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
