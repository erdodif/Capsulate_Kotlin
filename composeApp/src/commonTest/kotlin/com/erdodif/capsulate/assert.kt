package com.erdodif.capsulate

import com.erdodif.capsulate.lang.Fail
import com.erdodif.capsulate.lang.MatchPos
import com.erdodif.capsulate.lang.Parser
import com.erdodif.capsulate.lang.ParserResult
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.Pass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

inline fun <T> assertPass(value: ParserResult<T>) =
    assertTrue("Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}") {
        value is Pass<T>
    }

inline fun <T> assertPassAt(value: ParserResult<T>, at: MatchPos) {
    assertTrue("Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}") {
        value is Pass<T>
    }
    assertEquals(
        at,
        (value as Pass).match,
        "Expected match[${at.start}, ${at.end}], actual: match[${value.match.start}, ${value.match.end}]"
    )
}

inline fun <T> assertFail(value: ParserResult<T>) =
    assertTrue("Expected Fail, but Passed with value: ${(value as? Pass)?.value}") {
        value is Fail<T>
    }

inline fun <T> assertFailsAt(
    expectedIndex: Int,
    initialState: ParserState,
    crossinline parser: Parser<T>
) {
    val result = initialState.parse { parser() }
    assertTrue("Expected Fail at $expectedIndex, but Passed with value: ${(result as? Pass)?.value}") {
        result is Fail<T>
    }
    assertEquals(
        expectedIndex,
        initialState.position,
        "Expected position to be $expectedIndex, but is ${initialState.position}"
    )
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