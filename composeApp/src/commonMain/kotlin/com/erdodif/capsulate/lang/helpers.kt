package com.erdodif.capsulate.lang

fun Array<Char>.asString(): String{
    val boby = StringBuilder()
    for(char in this){
        boby.append(char)
    }
    return boby.toString()
}

fun List<Char>.asString(): String{
    val boby = StringBuilder()
    for(char in this){
        boby.append(char)
    }
    return boby.toString()
}

inline fun <T> Either<T, T>.getEither(): T = if (this is Left<T, T>) {
    this.value
} else {
    (this as Right<T, T>).value
}
