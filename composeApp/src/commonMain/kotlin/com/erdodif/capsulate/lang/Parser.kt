@file:Suppress("MemberVisibilityCanBePrivate", "UNUSED")

package com.erdodif.capsulate.lang

const val reservedChars = "()[]{}|$.?+-*/\"\' \t\n"

abstract class ParserResult<T>{
    /**
     * Calls the given transformation [lambda] if [Pass], or does nothing on [Fail]
     */
    inline fun <R>transform(lambda: (T) -> R) : ParserResult<R> =
        if (this is Pass){
            Pass(lambda(value), state)
        }
        else{
            this as Fail
            this.into()
        }
}

data class Pass<T>(val value: T, val state: ParserState) : ParserResult<T>()

data class Fail<T>(val reason: String, val state: ParserState) : ParserResult<T>() {
    @Suppress("UNCHECKED_CAST")
    fun <R> into(): Fail<R> = this as Fail<R>
}

interface Either<T, R>
data class Left<T, R>(val value: T) : Either<T, R>
data class Right<T, R>(val value: R) : Either<T, R>

/**
 * A function that tries to create T value from the current [ParserState] context
 */
typealias Parser<T> = ParserState.() -> ParserResult<T>

/**
 * A [Parser] that will always pass
 */
typealias SuccessParser<T> = ParserState.() -> Pass<T>

/**
 * Calls the given transformation [lambda] on the result if the parser passes
 */
inline fun <T,R>Parser<T>.transform(crossinline lambda: (T) -> R) : Parser<R> = {
    val res = this@transform()
    if (res is Pass) res.transform(lambda)
    else (res as Fail).into()
}

/**
 * Tries to apply the clean [lambda] if the result is present and not null
 */
inline fun <T,R>Parser<T?>.applyIf(crossinline lambda: (T) -> R): Parser<R?> = transform{
    if( it != null) lambda(it) else null
}

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



