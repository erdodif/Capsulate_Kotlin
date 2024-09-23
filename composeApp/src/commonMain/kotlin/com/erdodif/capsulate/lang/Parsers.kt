@file:Suppress("UNUSED")

package com.erdodif.capsulate.lang

val anyChar: Parser<Char> = {
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
 */
inline fun string(string: String): Parser<String> = {
    val start = position
    if (position + string.length > input.length) {
        fail("EOF reached")
    } else {
        var i = 0
        while (i < string.length && string[i] == input[position + i]) {
            i++
        }
        position += i
        if (i < string.length) {
            fail("Expected '${string[i]}' in word \"${string}\"(index: $i), but found '${input[position]}'")
        } else {
            if (input.length <= position || input[position] in reservedChars || input[position] in lineEnd) {
                pass(start, string)
            } else {
                fail("String '${string}' isn't over yet (found '${input[position]}')")
            }
        }
    }
}

inline fun charSeq(string: String): Parser<String> = {
    val start = position
    if (position + string.length > input.length) {
        fail("EOF reached")
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
inline fun <reified T> some(crossinline parser: Parser<T>): Parser<ArrayList<T>> = {
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
        (match as Fail<T>).to()
    } else {
        pass(start, matches)
    }
}

/**
 * Matches as much as possible
 *
 * Will not fail on no match and the last unsuccessful state gets reset
 */
inline fun <reified T> many(crossinline parser: Parser<T>): SuccessParser<ArrayList<T>> = {
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
 * On [parser] fail, the state's position is reset
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
    val result1 = parser1()
    if (result1 is Pass) {
        pass(pos, Left(result1.value))
    } else {
        position = pos
        val result2 = parser2()
        if (result2 is Pass) {
            pass(pos, Right(result2.value))
        } else {
            fail("${(result1 as Fail).reason}; ${(result2 as Fail).reason}")
        }
    }
}

/**
 * Tries to match the given parsers and merges the return type
 */
inline fun <T> orEither(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<T>
): Parser<T> = {
    val result: ParserResult<Either<T, T>> = or(parser1, parser2)()
    if (result is Pass) {
        pass(result.match.start, result.value.getEither())
    } else {
        (result as Fail).to()
    }
}

/**
 * Matches both the given parsers in order
 */
inline fun <T, R> and(
    crossinline parser1: Parser<T>, crossinline parser2: ParserState.(Pass<T>) -> Parser<R>
): Parser<Pair<T, R>> = {
    val result1 = parser1()
    if (result1 is Fail) {
        result1.to()
    } else {
        result1 as Pass<T>
        parser2(result1)[{ pass(result1.match.start, Pair(result1.value, it.value)) }]()
    }
}

inline fun <T, R> and(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<R>
): Parser<Pair<T, R>> = {
    val res1 = parser1()
    if (res1 is Fail<*>) {
        res1.to()
    } else {
        res1 as Pass
        val res2 = parser2()
        if (res2 is Fail<*>) {
            res2.to()
        } else {
            res2 as Pass
            pass(res1.match.start, Pair(res1.value, res2.value))
        }
    }
}

inline operator fun <T, R> Parser<T>.plus(crossinline other: Parser<R>): Parser<Pair<T, R>> =
    and(this, other)

inline operator fun <T, R> Parser<T>.plus(crossinline other: ParserState.(Pass<T>) -> Parser<R>): Parser<Pair<T, R>> =
    and(this, other)

/**
 * Matches both the parsers, discarding the second value
 */
inline fun <T> left(crossinline parser1: Parser<T>, crossinline parser2: Parser<*>): Parser<T> = {
    val result1 = parser1()
    if (result1 is Fail<*>) {
        result1.to()
    } else {
        val result2 = parser2()
        if (result2 is Fail<*>) {
            result2.to()
        } else {
            result1 as Pass
            Pass(result1.value, this, MatchPos(result1.match.start, position))
        }
    }
}

/**
 * Matches both the parsers, discarding the first value
 */
inline fun <T> right(crossinline parser1: Parser<*>, crossinline parser2: Parser<T>): Parser<T> = {
    val result1 = parser1()
    if (result1 is Fail<*>) {
        result1.to()
    } else {
        result1 as Pass
        val result2 = parser2()
        if (result2 is Fail<*>) {
            result2.to()
        } else {
            result2 as Pass
            pass(result1.match.start, result2.value)
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
inline fun <reified T> exactly(n: Int, crossinline parser: Parser<T>): Parser<Array<T>> = {
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
inline fun satisfy(crossinline predicate: (Char) -> Boolean): Parser<Char> = anyChar[{
    if (predicate(it.value)) it else Fail(
        "Failed the given predicate on char ${it.value}",
        it.state
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
val whiteSpace: Parser<Unit> =
    or(char(' '), char('\t'))[{ Pass(Unit, it.state, it.match) }, { it.to() }]

/**
 * Matches for a [parser] with [delim] delimiter, keeping only [parser]'s match
 *
 * Expects at least one match
 */
inline fun <reified T> delimited1(
    crossinline parser: Parser<T>,
    crossinline delim: Parser<*>
): Parser<ArrayList<T>> = (some(left(parser, delim)) + parser) / {
    it.first.apply { this.add(it.second) }
}

/**
 * Matches for a [parser] with [delim] delimiter, keeping only [parser]'s match
 *
 * Result can be empty
 */
inline fun <reified T> delimited(
    crossinline parser: Parser<T>,
    crossinline delim: Parser<*>
): Parser<ArrayList<T>> = (many(left(parser, delim)) + parser) / {
    it.first.apply { this.add(it.second) }
}

// NEEDS TO BE CHECKED TODO
/**
 * Right Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainr1(value: Parser<T>, func: Parser<(T, T) -> T>): Parser<T> =
    (value + { vMatch ->
        orEither(
            (func + chainr1(value, func)) / { it.first(vMatch.value, it.second) }
        ) { pass(vMatch.match.start, value) }
    }) * { a, _ -> a.first }

// NEEDS TO BE CHECKED TODO
/**
 * Left Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainl1(value: Parser<T>, func: Parser<(T, T) -> T>): Parser<T> = {
    val valueFirst = value()
    if (valueFirst is Fail) {
        valueFirst.to()
    } else {
        valueFirst as Pass<T>
        val pos = position
        fun chainh(val1: T, func: Parser<(T, T) -> T>, value: Parser<T>): Parser<T> =
            (func + value)[{ chainh(it.value.first(val1, it.value.second), func, value)() },
                { position = pos; it.to() }]

        val result = chainh(valueFirst.value, func, value)()
        if (result is Fail) {
            position = pos
            valueFirst
        } else {
            result
        }
    }
}

// NEEDS TO BE CHECKED TODO
fun <T> rightAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> =
    chainr1(parser, left({ pass(position, func) }, separator))

fun <T> leftAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> =
    chainl1(parser, left({ pass(position, func) }, separator))

inline fun <reified T> nonAssoc(
    crossinline func: (T, T) -> T,
    noinline parser: Parser<T>,
    crossinline separator: Parser<*>
): Parser<T> =
    delimited(parser, separator)[{
        when (it.value.size) {
            1 -> Pass(it.value[0], it.state, it.match)
            2 -> Pass(func(it.value[0], it.value[1]), it.state, it.match)
            else -> Fail("Too many association found.", it.state)
        }
    }]

/**
 * Asserts the parser does match the whole input file
 */
inline fun <T> topLevel(crossinline parser: Parser<T>): Parser<T> =
    middle(many(whiteSpace), parser, EOF)
