@file:Suppress("UNUSED", "NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.SuccessParser
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.getEither
import com.erdodif.capsulate.lang.util.times

inline val anyChar: Parser<Char>
    get() = {
        if (position >= input.length) {
            fail("Can't mach any char, EOF reached")
        } else {
            position++
            pass(position - 1, input[position - 1])
        }
    }

/**
 * Matches the given [char]
 */
inline fun char(char: Char): Parser<Char> = {
    val start = position
    if (position >= input.length) {
        fail("Can't match for ${char}, EOF reached")
    } else {
        position += 1
        if (input[position - 1] == char) {
            pass(start, input[position - 1])
        } else {
            fail("Expected '$char', but got'${input[position - 1]}'")
        }
    }
}

/**
 * Matches the given [string]
 *
 * Case insensitive
 */
inline fun stringCaseLess(string: String): Parser<String> = {
    val start = position
    if (position + string.length > input.length) {
        fail("EOF reached")
    } else {
        var i = 0
        while (i < string.length && string[i].lowercaseChar() == input[position + i].lowercaseChar()) {
            i++
        }
        position += i
        if (i < string.length) {
            fail("Expected '${string[i]}' in word \"${string}\"(index: $i), but found '${input[position]}'")
        } else {
            pass(start, string)
        }
    }
}

/**
 * Matches the given [string]
 *
 * Case sensitive
 */
inline fun string(string: String): Parser<String> = {
    val start = position
    if (position + string.length > input.length) {
        position = input.length
        Fail("EOF reached", this)
    } else {
        var i = 0
        while (i < string.length && string[i] == input[position + i]) {
            i++
        }
        position += i
        if (i < string.length) {
            fail("Expected '${string[i]}' in word \"${string}\"(index: $i), but found '${input[position]}'")
        } else {
            pass(start, string)
        }
    }
}

/**
 * In case the [parser] fails, the state gets reset, and returns null
 */
inline fun <T> optional(crossinline parser: Parser<T>): SuccessParser<T?> = {
    val pos = position
    val result = parser()
    if (result is Fail) {
        position = pos
    }
    pass(pos, (result as? Pass<T>)?.value)
}

/**
 * Expects one or more match of the given parser
 *
 * Will fail on no match, the last unsuccessful state gets reset
 */
inline fun <reified T> some(crossinline parser: Parser<T>): Parser<List<T>> = {
    val start = position
    val matches = ArrayList<T>()
    var match: ParserResult<T>
    var pos: Int
    do {
        pos = position
        match = parser()
        if (match is Pass) matches.add(match.value)
    } while (match is Pass)
    position = pos
    if (matches.isEmpty()) {
        match as Fail
    } else {
        pass(start, matches)
    }
}

/**
 * Matches as much as possible
 *
 * Will not fail on no match and the last unsuccessful state gets reset
 */
inline fun <reified T> many(crossinline parser: Parser<T>): SuccessParser<List<T>> = {
    val start = position
    val matches = ArrayList<T>()
    var match: ParserResult<T>
    var pos: Int
    do {
        pos = position
        match = parser()
        if (match is Pass) matches.add(match.value)
    } while (match is Pass)
    position = pos
    pass(start, matches)
}

/**
 * Inverts the outcome of the match
 *
 * On [parser] fail, the state's position is reset, and returns [Unit]
 */
inline fun <T> not(crossinline parser: Parser<T>): Parser<Unit> = {
    val pos = position
    val result: ParserResult<T> = parser()
    if (result is Pass) {
        fail("Expected to not match ${result.value}, but did happen")
    } else {
        position = pos
        pass(pos)
    }
}

/**
 * Tries to match the first one at first, and returns the second try on fail
 */
inline fun <T, R> or(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<R>
): Parser<Either<T, R>> = {
    val pos = position
    when (val result1 = parser1()) {
        is Pass -> pass(pos, Left(result1.value))
        else -> {
            position = pos
            when (val result2 = parser2()) {
                is Pass -> pass(pos, Right(result2.value))
                else -> {
                    fail("${(result1 as Fail).reason}; ${(result2 as Fail).reason}")
                }
            }
        }
    }
}

/**
 * Tries to match the given parsers and merges the return type
 */
inline fun <T> orEither(crossinline parser1: Parser<T>, crossinline parser2: Parser<T>): Parser<T> =
    {
        when (val result: ParserResult<Either<T, T>> = or(parser1, parser2)()) {
            is Pass -> pass(result.match.start, result.value.getEither())
            is Fail -> result
        }
    }

/**
 * Matches both the given parsers in order
 */
inline fun <T, R> and(
    crossinline parser1: Parser<T>, crossinline parser2: ParserState .(Pass<T>) -> Parser<R>
): Parser<Pair<T, R>> = {
    when (val result1 = parser1()) {
        is Fail -> result1
        is Pass -> parser2(result1)[{ pass(result1.match.start, Pair(result1.value, it.value)) }]()
    }
}

/**
 * Matches both the given parsers in order
 */
inline fun <T, R> and(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<R>
): Parser<Pair<T, R>> = {
    when (val res1 = parser1()) {
        is Fail -> res1
        is Pass -> {
            when (val res2 = parser2()) {
                is Fail -> res2
                is Pass -> pass(res1.match.start, res1.value to res2.value)
            }
        }
    }
}

/**
 * Pairs the given [parser] with it's match position, so it can be use in a nested parser
 */
inline fun <T> record(crossinline parser: Parser<T>): Parser<Pair<T, MatchPos>> = {
    when (val res = parser()) {
        is Fail -> res
        is Pass -> Pass(res.value to res.match, this, res.match)
    }
}

/**
 * @see [and]
 */
inline operator fun <T, R> Parser<T>.plus(crossinline other: Parser<R>): Parser<Pair<T, R>> =
    and(this, other)

/**
 * @see [and]
 */
inline operator fun <T, R> Parser<T>.plus(
    crossinline other: ParserState .(Pass<T>) -> Parser<R>
): Parser<Pair<T, R>> = and(this, other)

/**
 * Matches both the parsers, discarding the second value
 */
inline fun <T> left(crossinline parser1: Parser<T>, crossinline parser2: Parser<*>): Parser<T> = {
    val result1 = parser1()
    if (result1 is Fail) {
        result1
    } else {
        val result2 = parser2()
        if (result2 is Fail) {
            result2
        } else {
            result1 as Pass
            result2 as Pass
            Pass(result1.value, this, MatchPos(result1.match.start, result2.match.end))
        }
    }
}

/**
 * Matches both the parsers, discarding the first value
 */
inline fun <T> right(crossinline parser1: Parser<*>, crossinline parser2: Parser<T>): Parser<T> = {
    val result1 = parser1()
    if (result1 is Fail) {
        result1
    } else {
        val result2 = parser2()
        if (result2 is Fail) {
            result2
        } else {
            result1 as Pass
            result2 as Pass
            Pass(result2.value, this, MatchPos(result1.match.start, result2.match.end))
        }
    }
}

/**
 * Matches both three parsers, discarding the first and second values
 */
inline fun <T> middle(
    crossinline left: Parser<*>, crossinline a: Parser<T>, crossinline right: Parser<*>
): Parser<T> {
    return right(left, left(a, right))
}

/**
 * Matches the given parser at least [min] times but [max] times at max
 *
 * Will fail if the [min] number isn't reached
 */
inline fun <reified T> between(
    min: Int, max: Int, crossinline parser: Parser<T>
): Parser<Array<T>> = {
    val start = position
    require(min <= max)
    val matches = ArrayList<T>()
    var match: ParserResult<T>
    var index = 0
    var pos: Int
    do {
        pos = position
        match = parser()
        if (match is Pass) {
            matches.add(match.value)
            index++
        } else {
            position = pos
        }
    } while (match is Pass && index < max)
    if (index < min) {
        match as Fail
        fail("Attempt ${index}/$min [max:$max] failed with the error: (${match.reason})")
    } else {
        pass(start, matches.toTypedArray())
    }
}

/**
 * Matches the given parser exactly [n] times
 *
 * Will fail if can't match [n] times
 */
inline fun <reified T> exactly(
    n: Int, crossinline parser: Parser<T>
): Parser<Array<T>> = {
    val start = position
    val matches = arrayOfNulls<T>(n)
    var match: ParserResult<T>
    var index = 0
    do {
        match = parser()
        if (match is Pass) {
            matches[index] = (match.value)
            index++
        }
    } while (match is Pass && index != n)
    if (index < n) {
        fail("Attempt ${index + 1}/$n [exact] failed with the error: (${(match as Fail).reason})")
    } else {
        @Suppress("UNCHECKED_CAST") pass(start, matches as Array<T>)
    }
}

/**
 * Asserts the End Of File
 * */
val EOF: Parser<Unit> = {
    if (position == input.length) {
        pass(position)
    } else {
        fail("Expected EOF, but ${input.length - position} chars to go")
    }
}

/**
 * Matches characters according the given predicate
 */
inline fun satisfy(
    crossinline predicate: (Char) -> Boolean
): Parser<Char> = anyChar[{
    if (predicate(it.value)) it else Fail(
        "Failed the given predicate on char ${it.value}", it.state
    )
}]

/**
 * Looks for decimal digit
 */
val digit: Parser<Short> = satisfy { it in '0'..'9' } / { (it.code - '0'.code).toShort() }

/**
 * Looks for a non-negative integer
 */
val natural: Parser<UInt> = some(digit) / { it.fold(0) { a, b -> a * 10 + b.toInt() }.toUInt() }

/**
 * Looks for a signed integer
 */
val int: Parser<Int> = and(optional(char('-')), natural) / { (sign, num) ->
    if (sign == null) num.toInt() else -(num.toInt())
}

/**
 * Looks for simple whitespace characters
 */
val whiteSpace: Parser<Unit> = or(char(' '), char('\t'))[{ Pass(Unit, it.state, it.match) }]
// asum(whiteSpaceChars.map{char(it)}.toTypedArray())[{ Pass(Unit, it.state, it.match) }, { it.to() }]

/**
 * Matches for a [parser] with [delimiter] delimiter, keeping only [parser]'s match
 *
 * Expects at least one delimiter (as well as two parser match around)
 */
inline fun <reified T> delimited2(
    crossinline parser: Parser<T>, crossinline delimiter: Parser<*>
): Parser<List<T>> = (some(left(parser, delimiter)) + parser) / {
    val ret = it.first.toMutableList()
    ret.add(it.second)
    ret.toList()
}

/**
 * Matches for a [parser] with [delimiter] delimiter, keeping only [parser]'s match
 *
 * Result cannot be empty
 */
inline fun <reified T> delimited(
    crossinline parser: Parser<T>, crossinline delimiter: Parser<*>
): Parser<List<T>> = (many(left(parser, delimiter)) + parser) / {
    it.first + it.second
}

/**
 * Right Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainr1(value: Parser<T>, func: Parser<(T, T) -> T>): Parser<T> = (value + { vMatch ->
    orEither((func + chainr1(value, func)) / {
        it.first(
            vMatch.value, it.second
        )
    }) { pass(vMatch.match.start, vMatch.value) }
}) * { a, _ -> a.second }

/**
 * Left Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainl1(
    value: Parser<T>, func: Parser<(T, T) -> T>
): Parser<T> = value[{ valueFirst ->
    var pos = position
    var res = (func + value)()
    var acc = valueFirst.value
    while (res is Pass) {
        acc = res.value.first(acc, res.value.second)
        pos = position
        res = (func + value)()
    }
    position = pos
    pass(valueFirst.match.start, acc)
}]

fun <T> rightAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> =
    chainr1(parser, left({ pass(position, func) }, separator))

fun <T> leftAssoc(
    func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>
): Parser<T> = chainl1(parser, left({ pass(position, func) }, separator))

inline fun <reified T> nonAssoc(
    crossinline func: (T, T) -> T,
    crossinline parser: Parser<T>,
    crossinline separator: Parser<*>
): Parser<T> = {
    when (val result1 = parser()) {
        is Pass -> {
            val firstPos = position
            when (val result2 = right(separator, parser)()) {
                is Pass -> {
                    val secondPos = position
                    if (right(separator, parser)() is Pass) {
                        Fail("Too many association found.", this)
                    } else {
                        position = secondPos
                        Pass(
                            func(result1.value, result2.value), this,
                            MatchPos(result1.match.start, result2.match.end)
                        )
                    }
                }

                is Fail -> {
                    position = firstPos
                    result1
                }
            }
        }

        is Fail -> result1
    }
}

/**
 * Asserts the parser does match the whole input file
 */
inline fun <T> topLevel(crossinline parser: Parser<T>): Parser<T> =
    middle(many(whiteSpace), record(parser), EOF)[{
        Pass(it.value.first, it.state, it.value.second)
    }]
