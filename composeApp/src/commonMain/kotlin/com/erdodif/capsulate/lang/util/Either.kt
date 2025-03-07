package com.erdodif.capsulate.lang.util

sealed interface Either<out T, out R>

data class Left<out T>(val value: T) : Either<T, Nothing>
data class Right<out R>(val value: R) : Either<Nothing, R>

inline operator fun <T, R, S> Either<T, R>.get(
    crossinline left: (T) -> S,
    crossinline right: (R) -> S
): S = when (this) {
    is Left<T> -> left(this.value)
    is Right<R> -> right(this.value)
}

/**
 * Calls either [onLeft] or [onRight] to produce the result `S`
 */
inline fun <T, R, S> Either<T, R>.fold(
    crossinline onLeft: (T) -> S,
    crossinline onRight: (R) -> S
): S = when (this) {
    is Left<T> -> onLeft(this.value)
    is Right<R> -> onRight(this.value)
}

/**
 * Calls [onRight] so the value can be safely extracted as a value of [Left]
 */
inline fun <T,R>Either<T,R>.recover(onRight: (R) -> T) : T = when(this){
    is Left<T> -> this.value
    is Right<R> -> onRight(this.value)
}

inline fun <T,R,S> Either<T,R>.map(onLeft: (T) -> S): Either<S,R> = when(this){
    is Left<T> -> Left(onLeft(value))
    is Right<*> -> this
}

inline fun <T,R,S> Either<T,R>.mapError(onRight: (R) -> S): Either<T,S> = when(this){
    is Left<*> -> this
    is Right<R> -> Right(onRight(value))
}
