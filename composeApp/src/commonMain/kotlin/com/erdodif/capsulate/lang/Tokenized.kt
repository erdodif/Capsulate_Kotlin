package com.erdodif.capsulate.lang


val pLineEnd: Parser<Char> = asum(lineEnd.map { char(it) }.toTypedArray())

/**
 * Removes the whitespaces, then match the given [parser]
 */
inline fun <T> tok(crossinline parser: Parser<T>): Parser<T> = left(parser, many(whiteSpace))


/**
 * Looks for non reserved char
 */
val freeChar: Parser<Char> = satisfy { it !in reservedChars && it !in lineEnd }

/**
 * Looks for a word made of non reserved characters
 */
val freeWord: Parser<String> = some(freeChar) / { it.asString() }

inline fun _char(char: Char): Parser<Char> = tok(char(char))

inline fun _keyword(string: String): Parser<String> = tok(string(string))

val _anyKeyword: Parser<String> = asum(keywords.map { _keyword(it) }.toTypedArray())

val _reservedChar: Parser<Char> = asum(reservedChars.map { _char(it) }.toTypedArray())

val _nonKeyword: Parser<String> = {
    val result: ParserResult<String> = tok(freeWord)()
    if (result is Pass && keywords.contains((result).value)) {
        fail("The word '${result.value}' is reserved!")
    } else {
        result
    }
}

val _natural: Parser<UInt> = tok(natural)

val _integer: Parser<Int> = tok(int)

val _lineEnd: Parser<Char> = tok(pLineEnd)
