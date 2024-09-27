package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.grammar.char
import com.erdodif.capsulate.lang.grammar.string
import com.erdodif.capsulate.lang.grammar.int
import com.erdodif.capsulate.lang.grammar.left
import com.erdodif.capsulate.lang.grammar.many
import com.erdodif.capsulate.lang.grammar.natural
import com.erdodif.capsulate.lang.grammar.satisfy
import com.erdodif.capsulate.lang.grammar.some
import com.erdodif.capsulate.lang.grammar.whiteSpace
import com.erdodif.capsulate.lang.grammar.keywords
import com.erdodif.capsulate.lang.grammar.lineEnd
import com.erdodif.capsulate.lang.grammar.reservedChars
import com.erdodif.capsulate.lang.grammar.whiteSpaceChars


val pLineEnd: Parser<Char> = asum(*lineEnd.map { char(it) }.toTypedArray())

/**
 * Matches the given [parser], then removes the whitespaces
 */
inline fun <T> tok(crossinline parser: Parser<T>): Parser<T> = left(parser, many(whiteSpace))


/**
 * Looks for non reserved char
 */
val freeChar: Parser<Char> = satisfy { it !in whiteSpaceChars && it !in reservedChars && it !in lineEnd }

/**
 * Looks for a word made of non reserved characters
 */
val freeWord: Parser<String> = some(freeChar) / { it.asString() }

inline fun _char(char: Char): Parser<Char> = tok(char(char))

inline fun _keyword(string: String): Parser<String> = tok(string(string))

val _anyKeyword: Parser<String> = asum(*keywords.map { _keyword(it) }.toTypedArray())

val reservedChar: Parser<Char> = asum(*reservedChars.map { char(it) }.toTypedArray())
val _reservedChar: Parser<Char> = tok(reservedChar)

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
