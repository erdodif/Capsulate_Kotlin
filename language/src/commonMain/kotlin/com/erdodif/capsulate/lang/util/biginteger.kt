package com.erdodif.capsulate.lang.util

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

val Int.bg : BigInteger
    get() = toBigInteger()

val BigInteger.isInt: Boolean
    get() = compareTo(Int.MAX_VALUE.toBigInteger()) <= 0
            && compareTo(Int.MIN_VALUE.toBigInteger()) >= 0

fun BigInteger.toInt(): Int {
    require(compareTo(Int.MAX_VALUE.toBigInteger()) <= 0) {
        "Can't convert to Int, value too big: $this"
    }
    require(compareTo(Int.MIN_VALUE.toBigInteger()) >= 0) {
        "Can't convert to Int, value too small: $this"
    }
    return intValue(true)
}

fun BigInteger.toIntOrNull(): Int? = if (isInt) toInt() else null

fun BigInteger.truncToInt(): Int = when {
    compareTo(Int.MAX_VALUE.toBigInteger()) > 0 -> Int.MAX_VALUE
    compareTo(Int.MIN_VALUE.toBigInteger()) < 0 -> Int.MIN_VALUE
    else -> toInt()
}

fun List<BigInteger>.toIntArray(): List<Int> = map(BigInteger::toInt)
fun List<BigInteger>.filterInt(): List<Int> = mapNotNull(BigInteger::toIntOrNull)
fun List<Int>.toBigIntArray(): List<BigInteger> = map(Int::toBigInteger)
