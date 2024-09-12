package com.erdodif.capsulate.lang

import androidx.compose.ui.text.substring

fun ArrayList<Char>.asString(): String{
    val boby = StringBuilder()
    for(char in this){
        boby.append(char)
    }
    return boby.toString()
}

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

operator fun String.get(start:Int, end:Int): String = this.substring(start..<end)
