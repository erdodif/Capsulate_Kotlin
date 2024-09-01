package com.erdodif.capsulate

import com.erdodif.capsulate.lang.Fail
import com.erdodif.capsulate.lang.ParserResult
import com.erdodif.capsulate.lang.Pass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

inline fun <T>assertPass(value: ParserResult<T>) =
    assertTrue("Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}") {
        value is Pass<T>
    }

inline fun <T>assertFail(value: ParserResult<T>) =
    assertTrue("Expected Fail, but Passed with value: ${(value as? Pass)?.value}") {
        value is Fail<T>
    }

inline fun <T> assertValue(expected: T, result: ParserResult<T>) {
    assertPass(result)
    result as Pass
    assertEquals(
        expected,
        result.value,
        "Expected Pass result of $expected, but got ${result.value}"
    )
}