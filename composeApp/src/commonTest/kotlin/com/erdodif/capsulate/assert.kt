package com.erdodif.capsulate

import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

inline fun <T> assertPass(value: ParserResult<T>) =
    assertTrue("Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}\nState=${value.state}") {
        value is Pass<T>
    }

inline fun <T> assertPassAt(value: ParserResult<T>, at: MatchPos) {
    assertTrue(
        "Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}\nState=${value.state}"
    ) {
        value is Pass<T>
    }
    assertEquals(
        at,
        (value as Pass).match,
        "Expected match[${at.start}, ${at.end}], actual: match[${
            value.match.start
        }, ${value.match.end}]\nState=${value.state}"
    )
}

inline fun <T> assertFail(value: ParserResult<T>) {
    if (value is Pass) {
        if (value.value is Exp<*>) {
            throw AssertionError(
                "Expected Fail, but Passed with value: ${
                    (value.value as Exp<*>).toString(value.state)
                }\nState=${value.state}"
            )
        } else {
            throw AssertionError(
                "Expected Fail, but Passed with value: ${
                    value.value
                }\nState=${value.state}"
            )
        }
    }
}


inline fun <T> assertFailsAt(
    expectedIndex: Int, initialState: ParserState, crossinline parser: Parser<T>
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
        expected, result.value, "Expected Pass result of $expected, but got ${result.value}"
    )
}

inline fun <T> assertTrue(expected: (Pass<T>) -> Boolean, result: ParserResult<T>) {
    assertPass(result)
    assertTrue(
        expected(result as Pass),
        "Expected calculation to pass, it didn't on value: ${result.value}"
    )
}

inline fun <T> assertFalse(expected: (Pass<T>) -> Boolean, result: ParserResult<T>) {
    assertPass(result)
    assertTrue(
        !expected(result as Pass),
        "Expected calculation to fail, it did pass on value: ${result.value}"
    )
}

infix fun <T> Parser<T>.pass(text: String) = assertPass(ParserState(text).parse(this))

infix fun <T> Parser<T>.fail(text: String) = assertFail(ParserState(text).parse(this))

infix fun <T> Parser<T>.value(value: Pair<String, T>) =
    assertValue(value.second, ParserState(value.first).parse(this))
