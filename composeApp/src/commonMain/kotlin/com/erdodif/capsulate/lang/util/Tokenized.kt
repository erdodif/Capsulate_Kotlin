@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.int
import com.erdodif.capsulate.lang.program.grammar.keywords
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.lineBreak
import com.erdodif.capsulate.lang.program.grammar.lineEnd
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.natural
import com.erdodif.capsulate.lang.program.grammar.not
import com.erdodif.capsulate.lang.program.grammar.reservedChars
import com.erdodif.capsulate.lang.program.grammar.satisfy
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.lang.program.grammar.stringCaseLess
import com.erdodif.capsulate.lang.program.grammar.whiteSpace
import com.erdodif.capsulate.lang.program.grammar.whiteSpaceChars

val pLineBreak: Parser<Char> = asum(*lineBreak.map { char(it) }.toTypedArray())
val pLineEnd: Parser<Char> = asum(*lineEnd.map { char(it) }.toTypedArray())

/**
 * Matches the given [parser], then removes the whitespaces
 */
inline fun <T> tok(crossinline parser: Parser<T>): Parser<T> = {
    val firstResult = parser()
    if(firstResult is Pass){
        many(whiteSpace)()
        firstResult.copy(state = this)
    }
    else{
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
val freeWord: Parser<String> = some(freeChar) / { it.asString() }

inline fun _char(char: Char): Parser<Char> = tok(char(char))

inline fun _keyword(string: String): Parser<String> = tok(left(stringCaseLess(string), not(freeChar)))

val _anyKeyword: Parser<String> = asum(*keywords.map { _keyword(it) }.toTypedArray())

val reservedChar: Parser<Char> = asum(*reservedChars.map { char(it) }.toTypedArray())
val _reservedChar: Parser<Char> = tok(reservedChar)

val _nonKeyword: Parser<String> = tok(freeWord)[{
    if ( it.value in keywords) {
        fail("The word '${it.value}' is reserved!")
    } else {
        it
    }
}]

val _natural: Parser<UInt> = tok(natural)

val _integer: Parser<Int> = tok(int)

val _lineEnd: Parser<Char> = tok(pLineEnd)
val _lineBreak: Parser<Char> = tok(pLineBreak)
