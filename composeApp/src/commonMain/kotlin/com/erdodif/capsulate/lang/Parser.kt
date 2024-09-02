@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED")

package com.erdodif.capsulate.lang

const val reservedChars = "()[]{}|$.?+-*/\"\' \t\n"

interface ParserResult<T>

data class Pass<T>(val value: T, val state: ParserState) : ParserResult<T>

data class Fail<T>(val reason: String, val state: ParserState) : ParserResult<T> {
    @Suppress("UNCHECKED_CAST")
    fun <R> into(): Fail<R> = this as Fail<R>
}

interface Either<T, R>
data class Left<T, R>(val value: T) : Either<T, R>
data class Right<T, R>(val value: R) : Either<T, R>

typealias Parser<T> = ParserState.() -> ParserResult<T>

class ParserState(val input: String) {
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

    val pass: Pass<Unit> = Pass(Unit, this)
    fun <T> pass(value: T): Pass<T> = Pass(value, this)
    fun <T> fail(reason: String): Fail<T> = Fail(reason, this)
}



