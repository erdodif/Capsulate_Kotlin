@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.int
import com.erdodif.capsulate.lang.program.grammar.keywords
import com.erdodif.capsulate.lang.program.grammar.lineBreak
import com.erdodif.capsulate.lang.program.grammar.lineEnd
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.natural
import com.erdodif.capsulate.lang.program.grammar.reservedChars
import com.erdodif.capsulate.lang.program.grammar.satisfy
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.lang.program.grammar.stringCaseLess
import com.erdodif.capsulate.lang.program.grammar.whiteSpace
import com.erdodif.capsulate.lang.program.grammar.whiteSpaceChars

val pLineBreak: Parser<Char> = charOf(lineBreak)
val pLineEnd: Parser<Char> = charOf(lineEnd)

/**
 * Matches the given [parser], then removes the whitespaces
 */
inline fun <T> tok(crossinline parser: Parser<T>): Parser<T> = {
    val firstResult = parser()
    if (firstResult is Pass) {
        many(whiteSpace)()
        Pass(firstResult.value, this, firstResult.match)
    } else {
        firstResult
    }
}


/**
 * Looks for non reserved char
 */
val freeChar: Parser<Char> =
    satisfy { it !in whiteSpaceChars && it !in reservedChars && it !in lineEnd }

/**
 * Looks for a word made of non reserved characters
 */
val freeWord: Parser<String> = {
    when (val result = some(freeChar)()) {
        is Pass ->
            if (result.value.first().isDigit()) {
                Fail("Word cannot start with a digit!", result.state)
            } else {
                Pass(buildString { result.value.map(::append) }, result.state, result.match)
            }

        is Fail -> result
    }
}

inline fun _char(char: Char): Parser<Char> = tok(char(char))

inline fun _keyword(string: String): Parser<String> = tok(stringCaseLess(string))

val _anyKeyword: Parser<String> = asum(*keywords.map { _keyword(it) }.toTypedArray())

val reservedChar: Parser<Char> = charOf(reservedChars)
val _reservedChar: Parser<Char> = tok(reservedChar)

val _nonKeyword: Parser<String> = tok(freeWord)[{
    if (it.value in keywords) {
        fail("The word '${it.value}' is reserved!")
    } else {
        it
    }
}]

val _natural: Parser<UInt> = tok(natural)

val _integer: Parser<Int> = tok(int)

val _lineEnd: Parser<Char> = tok(pLineEnd)
val _lineBreak: Parser<Char> = tok(pLineBreak)
