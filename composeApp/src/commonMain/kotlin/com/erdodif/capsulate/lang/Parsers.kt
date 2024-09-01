@file:Suppress("UNUSED")

package com.erdodif.capsulate.lang

val anyChar: Parser<Char> = {
    if (position >= input.length) {
        fail("Can't mach any char, EOF reached")
    } else {
        position++
        pass(input[position - 1])
    }
}

/**
 * Matches the given [char]
 */
inline fun char(char: Char): Parser<Char> = {
    if (position >= input.length) {
        fail("Can't match for ${char}, EOF reached")
    } else {
        position += 1
        if (input[position - 1] == char) {
            pass(input[position - 1])
        } else {
            fail("Expected '$char', but got'${input[position - 1]}'")
        }
    }
}

/**
 * Matches the given [string]
 */
inline fun string(string: String): Parser<String> = {
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
            if (input.length <= position || input[position] in reservedChars) {
                pass(string)
            } else {
                fail("String '${string}' isn't over yet (found '${input[position]}')")
            }
        }
    }
}

/**
 * In case the [parser] fails, the state gets reset, and returns null
 */
inline fun <T> optional(crossinline parser: Parser<T>): Parser<T?> = {
    val pos = position
    val result = parser()
    if (result is Fail) {
        position = pos
    }
    pass((result as? Pass<T>)?.value)
}

/**
 * Expects one or more match of the given parser
 *
 * Will fail on no match, the last unsuccessful state gets reset
 */
inline fun <reified T> some(crossinline parser: Parser<T>): Parser<Array<T>> = {
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
        (match as Fail<T>).into()
    } else {
        pass(matches.toTypedArray())
    }
}

/**
 * Matches as much as possible
 *
 * Will not fail on no match and the last unsuccessful state gets reset
 */
inline fun <reified T> many(crossinline parser: Parser<T>): Parser<ArrayList<T>> = {
    val matches = ArrayList<T>()
    var match: ParserResult<T>
    var pos: Int
    do {
        pos = position
        match = parser()
        if (match is Pass) matches.add(match.value)
    } while (match is Pass)
    position = pos
    pass(matches)
}

/**
 * Tries to match the first one at first, and returns the second try on fail
 */
inline fun <T, R> or(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<R>
): Parser<Either<T, R>> = {
    val result1 = parser1()
    val pos = position
    if (result1 is Pass) {
        pass(Left(result1.value))
    } else {
        position = pos
        val result2 = parser2()
        if (result2 is Pass) {
            pass(Right(result2.value))
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
        pass(result.value.getEither())
    } else {
        (result as Fail).into()
    }
}


/**
 * Matches both the given parsers in order
 */
inline fun <T, R> both(
    crossinline parser1: Parser<T>, crossinline parser2: Parser<R>
): Parser<Pair<T, R>> = {
    val result1 = parser1()
    val result2 = parser2()
    when {
        result1 is Fail -> result1.into()
        result2 is Fail -> result2.into()
        else -> pass(Pair((result1 as Pass<T>).value, (result2 as Pass<R>).value))
    }
}

/**
 * Matches both the parsers, discarding the second value
 */
inline fun <T> left(crossinline parser1: Parser<T>, crossinline parser2: Parser<*>): Parser<T> = {
    val result1 = parser1()
    val result2 = parser2()
    if (result1 is Fail<*>) {
        result1.into()
    } else {
        if (result2 is Fail<*>) {
            result2.into()
        } else {
            result1
        }
    }
}

/**
 * Matches both the parsers, discarding the first value
 */
inline fun <T> right(crossinline parser1: Parser<*>, crossinline parser2: Parser<T>): Parser<T> = {
    val result1 = parser1()
    val result2 = parser2()
    if (result1 is Fail<*>) {
        result1.into()
    } else {
        result2
    }
}

/**
 * Matches both three parsers, discarding the first and second values
 */
inline fun <T> middle(
    crossinline left: Parser<*>, crossinline a: Parser<T>, crossinline right: Parser<*>
): Parser<T> = {
    val leftResult = left()
    val result = a()
    val rightResult = right()
    when {
        leftResult is Fail -> leftResult.into()
        result is Fail -> result
        rightResult is Fail -> rightResult.into()
        else -> result
    }
}

/**
 * Matches the given parser at least [min] times but [max] times at max
 *
 * Will fail if the [min] number isn't reached
 */
inline fun <reified T> between(
    min: Int, max: Int, crossinline parser: Parser<T>
): Parser<Array<T>> = {
    require(min <= max)
    val matches = ArrayList<T>()
    var match: ParserResult<T>
    var count = -1
    var pos: Int
    do {
        pos = position
        count++
        match = parser()
        if (match is Pass) matches.add(match.value)
    } while (match is Pass && count < max)
    position = pos
    if (matches.count() < min) {
        fail("Attempt ${count}/$min [max:$max] failed with the error: (${(match as Fail).reason})")
    } else {
        pass(matches.toTypedArray())
    }
}

/**
 * Matches the given parser exactly [n] times
 *
 * Will fail if can't match [n] times
 */
inline fun <reified T> exactly(n: Int, crossinline parser: Parser<T>): Parser<Array<T>> = {
    val matches = arrayOfNulls<T>(n)
    var match: ParserResult<T>
    var index = 0
    do {
        match = parser()
        if (match is Pass){
            matches[index] = (match.value)
            index++
        }
    } while (match is Pass && index != n)
    if (index < n) {
        fail("Attempt ${index + 1}/$n [exact] failed with the error: (${(match as Fail).reason})")
    } else {
        @Suppress("UNCHECKED_CAST") pass(matches as Array<T>)
    }
}

/**
 * Asserts the End Of File
 * */
val EOF: Parser<Unit> = {
    if (position == input.length) {
        pass
    } else {
        fail("Expected EOF, but ${input.length - position} chars to go")
    }
}

/**
 * Matches characters according the given predicate
 */
inline fun satisfy(crossinline predicate: (Char) -> Boolean): Parser<Char> = {
    val result = anyChar()
    if (result is Pass) {
        if (predicate(result.value)) {
            result
        } else {
            fail("Failed the given predicate on char ${result.value}")
        }
    } else {
        result
    }
}

/**
 * Looks for non reserved char
 */
val freeChar: Parser<Char> = satisfy { !reservedChars.contains(it) }

/**
 * Looks for a word made of non reserved characters
 */
val freeWord: Parser<String> = {
    val result = some(satisfy { !reservedChars.contains(it) })()
    if (result is Fail) {
        result.into()
    } else {
        result as Pass
        pass(result.value.asString())
    }
}

/**
 * Looks for decimal digit
 */
val digit: Parser<Short> = {
    val result = satisfy { it in '0'..'9' }()
    if (result is Pass) {
        pass((result.value.code - '0'.code).toShort())
    } else {
        (result as Fail).into()
    }
}

/**
 * Looks for simple whitespace characters
 */
val whiteSpace: Parser<Unit> = {
    val result = or(char(' '), char('\t'))()
    if (result is Fail<*>) {
        result.into()
    } else {
        pass
    }
}

/**
 * Matches for a [parser] with trailing [delim] delimiter, keeping only [parser]'s match
 */
inline fun <reified T> delimited(
    crossinline parser: Parser<T>,
    crossinline delim: Parser<*>
): Parser<ArrayList<T>> = { many(left(parser, delim))() }

// NEEDS TO BE CHECKED TODO
/**
 * Right Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainr1(value: Parser<T>, func: Parser<(T, T) -> T>): Parser<T> = {
    val resultFirst: ParserResult<T> = value()
    if (resultFirst is Fail<*>) {
        resultFirst.into()
    } else {
        val resultValue: T = (resultFirst as Pass).value
        orEither(
            {
                val resFunc: ParserResult<(T, T) -> T> = func()
                val resrec = chainr1(value, func)()
                if (resFunc is Pass && resrec is Pass) {
                    pass(resFunc.value(resultValue, resrec.value))
                } else {
                    fail("")
                }
            },
            { pass(resultValue) }
        )()
    }
}

// NEEDS TO BE CHECKED TODO
/**
 * Left Chaining
 *
 * Tries to recursively(!) call the given [value] parser, and fold it with [func]
 */
fun <T> chainl1(value: Parser<T>, func: Parser<(T, T) -> T>): Parser<T> = TODO()

// NEEDS TO BE CHECKED TODO
fun <T> rightAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> =
    chainr1(parser, left({ pass(func) }, separator))

fun <T> leftAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> = TODO()

fun <T> nonAssoc(func: (T, T) -> T, parser: Parser<T>, separator: Parser<*>): Parser<T> = TODO()

/**
 * Asserts the parser does match the whole input file
 */
inline fun <T> topLevel(crossinline parser: Parser<T>): Parser<T> = {
    many(whiteSpace)()
    val result = parser()
    val eof = EOF()
    when {
        result is Fail -> result
        eof is Fail -> eof.into()
        else -> result
    }
}
