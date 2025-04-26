package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.KRawValue
import kotlinx.serialization.Serializable

@Serializable
sealed interface Either<out T, out R> : KParcelable

@Serializable
@KParcelize
data class Left<out T>(val value: @KRawValue T) : Either<T, Nothing>

@Serializable
@KParcelize
data class Right<out R>(val value: @KRawValue R) : Either<Nothing, R>

inline operator fun <T, R, S> Either<T, R>.get(
    crossinline left: (T) -> S,
    crossinline right: (R) -> S
): S = when (this) {
    is Left<T> -> left(this.value)
    is Right<R> -> right(this.value)
}

/**
 * Splits the list into two parts, based on whether the value is [Left] or [Right]
 * (split as [Pair.first] for [Left] and [Pair.second] for [Right])
 *
 * The order between the [Left] values and [Right] values are respected,
 * yet the original order can't be restored from this state
 */
fun <T, R> List<Either<T, R>>.splitEither():Pair<List<T>, List<R>> =
    filterIsInstance<Left<T>>().map { it.value } to filterIsInstance<Right<R>>().map { it.value }

/**
 * Returns a sublist that only contains instances of [Left] in the original list
 *
 * This keeps the order between instances
 */
fun <T, R> List<Either<T, R>>.filterLeft():  List<T> =
    filterIsInstance<Left<T>>().map { it.value }

/**
 * Returns a sublist that only contains instances of [Right] in the original list
 *
 * This keeps the order between instances
 */
fun <T, R> List<Either<T, R>>.filterRight(): List<R> =
    filterIsInstance<Right<R>>().map { it.value }
/**
 * Returns the "value" ([Left]) if possible
 *
 * This function does not make sense when [T] is nullable
 */
val <T, R> Either<T, R>.valueOrNull: T?
    get() = if (this is Left) this.value else null

/**
 * Returns the "error" ([Right]) if possible
 *
 * This function does not make sense when [R] is nullable
 */
val <T, R> Either<T, R>.errorOrNull: R?
    get() = if (this is Right) this.value else null

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
inline fun <T, R> Either<T, R>.recover(onRight: (R) -> T): T = when (this) {
    is Left<T> -> this.value
    is Right<R> -> onRight(this.value)
}

inline fun <T, R, S> Either<T, R>.map(onLeft: (T) -> S): Either<S, R> = when (this) {
    is Left<T> -> Left(onLeft(value))
    is Right<*> -> this
}

inline fun <T, R, S> Either<T, R>.mapError(onRight: (R) -> S): Either<T, S> = when (this) {
    is Left<*> -> this
    is Right<R> -> Right(onRight(value))
}
