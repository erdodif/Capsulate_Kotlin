package com.erdodif.capsulate.lang

/**
 * Removes the whitespaces, then match the given [parser]
 */
inline fun <T> tok(crossinline parser: Parser<T>): Parser<T> = left(parser, many(whiteSpace))


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

inline fun _char(char:Char) : Parser<Char> = tok(char(char))

inline fun _keyword(string :String) : Parser<String> = tok(string(string))

val _nonKeyword: Parser<String> = {
    val result: ParserResult<String> = tok(freeWord)()
    if(result is Pass && keywords.contains((result).value)){
        fail("The word '${result.value}' is reserved!")
    }
    else{
        result
    }
}

val _natural : Parser<UInt> = tok(natural)


