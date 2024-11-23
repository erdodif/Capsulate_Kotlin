package com.erdodif.capsulate

import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

inline fun <T> assertPass(value: ParserResult<T>): Pass<T> =
    assertIs<Pass<T>>(
        value,
        "Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}\nState=${value.state}"
    )

inline fun <T> assertPassAt(value: ParserResult<T>, at: MatchPos) {
    assertIs<Pass<T>>(
        value,
        "Expected Pass, but got Fail with reason: ${(value as? Fail)?.reason}\nState=${value.state}"
    )
    assertEquals(
        at,
        value.match,
        "Expected match[${at.start}, ${at.end}], actual: match[${
            value.match.start
        }, ${value.match.end}]\nState=${value.state}"
    )
}

inline fun <T> assertFail(value: ParserResult<T>): Fail {
    if (value is Fail) return value
    else throw AssertionError(
        if ((value as Pass<T>).value is Exp<*>) {
            "Expected Fail, but Passed with value: ${
                (value.value as Exp<*>).toString(value.state)
            }\nState=${value.state}"
        } else {
            "Expected Fail, but Passed with value: ${
                value.value
            }\nState=${value.state}"
        }
    )
}

inline fun <T> assertFailsAt(
    expectedIndex: Int, initialState: ParserState, crossinline parser: Parser<T>
) {
    val result = initialState.parse { parser() }
    assertTrue("Expected Fail at $expectedIndex, but Passed with value: ${(result as? Pass)?.value}") {
        result is Fail
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

infix fun <T> Parser<T>.pass(text: String): Pass<T> =
    ParserState(text).parse(this).let(::assertPass)

infix fun <T> Parser<T>.fail(text: String): Fail =
    ParserState(text).parse(this).let(::assertFail)


infix fun <T> ParserResult<T>.at(index: Int): ParserResult<T> = this.also {
    assertTrue("Ended on different position (assumed $index, but got ${it.state.position})")
    { it.state.position == index }
}

infix fun <T> Pass<T>.at(pos: MatchPos): ParserResult<T> = this.also {
    assertTrue("Ended on different position! Assumed (${pos.start}, ${pos.end}), but got (${it.match.start}, ${it.match.end}).")
    { it.match.start == pos.start && it.match.end == pos.end }
}

infix fun <T> ParserResult<T>.withValue(value: T) = this.also {
    assertPass(this)
    this as Pass
    assertTrue("Parser result mismatch, expected $value, but got ${this.value}") { this.value == value }
}

infix fun <T> Parser<T>.value(value: Pair<String, T>) =
    assertValue(value.second, ParserState(value.first).parse(this))

infix fun <T> Pass<T>.matches(predicate: (Pass<T>) -> Boolean) = this.also {
    assertTrue(predicate(this))
}

infix fun <T> Pass<T>.match(predicate: (T) -> Boolean): Pass<T> = this.also{
    assertTrue(predicate(this.value))
}
