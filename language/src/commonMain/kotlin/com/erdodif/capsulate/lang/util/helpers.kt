@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.util

fun List<Char>.asString(): String{
    val boby = StringBuilder()
    for(char in this){
        boby.append(char)
    }
    return boby.toString()
}

inline fun <T> Either<T, T>.getEither(): T = if (this is Left<T>) {
    this.value
} else {
    (this as Right<T>).value
}

operator fun String.get(start:Int, end:Int): String = this.substring(start..<end)
