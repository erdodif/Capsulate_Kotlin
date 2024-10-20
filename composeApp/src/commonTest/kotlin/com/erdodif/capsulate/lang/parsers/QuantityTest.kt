package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPassAt
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.program.grammar.between
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.exactly
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.some
import kotlin.test.Test
import kotlin.test.assertEquals

class QuantityTest {

    @Test
    fun optional_pass_char_empty() = assertValue(null, ParserState("").parse(optional(char('c'))))

    @Test
    fun optional_pass_char() = assertValue('c', ParserState("cr").parse(optional(char('c'))))

    @Test
    fun optional_fail_reset() {
        val state = ParserState("sd")
        assertValue(null, state.parse(optional(char('c'))))
        assertEquals(0, state.position)
    }

    @Test
    fun some_fail_char() = assertFail(ParserState("r").parse(some(char('c'))))

    @Test
    fun some_pass_reset() {
        val state = ParserState("ccr")
        val result = state.parse(some(char('c')))
        assertPassAt(result, MatchPos(0, 2))
        result as Pass
        assertEquals(2, result.value.size)
        assertEquals('c', result.value[0])
        assertEquals('c', result.value[1])
        assertEquals(2, state.position)
    }

    @Test
    fun many_pass_empty() {
        val result = ParserState("").parse(many(char('c')))
        assertPassAt(result, MatchPos(0, 0))
        result as Pass
        assertEquals(0, result.value.size)
    }

    @Test
    fun many_pass_reset() {
        val state = ParserState("ccr")
        val result = state.parse(many(char('c')))
        assertPassAt(result, MatchPos(0, 2))
        result as Pass
        assertEquals(2, result.value.size)
        assertEquals('c', result.value[0])
        assertEquals('c', result.value[1])
        assertEquals(2, state.position)
    }

    @Test
    fun between_fail_few() = assertFail(
        ParserState("cr").parse(between(2, 3, char('c')))
    )

    @Test
    fun between_pass_reset() {
        val state = ParserState("cccrr")
        val result = state.parse(between(2, 4, char('c')))
        assertPassAt(result, MatchPos(0, 3))
        result as Pass
        assertEquals(3, result.value.size)
        assertEquals('c', result.value[2])
        assertEquals(3, state.position)
    }

    @Test
    fun exactly_fail_few() = assertFail(
        ParserState("cr").parse(exactly(2, char('c')))
    )

    @Test
    fun exactly_pass_much() {
        val state = ParserState("cccr")
        val result = state.parse(exactly(2, char('c')))
        assertPassAt(result, MatchPos(0, 2))
        result as Pass
        assertEquals(2, result.value.size)
        assertEquals('c', result.value[1])
        assertEquals(2, state.position)
    }

    @Test
    fun exactly_pass() {
        val state = ParserState("ccrr")
        val result = state.parse(exactly(2, char('c')))
        assertPassAt(result, MatchPos(0, 2))
        result as Pass
        assertEquals(2, result.value.size)
        assertEquals('c', result.value[1])
        assertEquals(2, state.position)
    }

}