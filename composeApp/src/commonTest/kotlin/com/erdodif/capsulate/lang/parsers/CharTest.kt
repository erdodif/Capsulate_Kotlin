package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.assertPassAt
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.digit
import com.erdodif.capsulate.lang.program.grammar.satisfy
import kotlin.test.Test
import kotlin.test.assertEquals

class CharTest {
    @Test
    fun char_match_single_character() = assertPassAt(
        ParserState("c").parse(char('c')), MatchPos(0, 1)
    )

    @Test
    fun char_returns_single_character() = assertValue(
        'c',
        ParserState("c").parse(char('c'))
    )

    @Test
    fun char_fails_empty_string_run() = assertFail(ParserState("").run { char('c')() })

    @Test
    fun char_fails_empty_string_parse() = assertFail(ParserState("").parse(char('c')))

    @Test
    fun digit_pass() {
        assertValue(0, ParserState("0").parse(digit))
        assertValue(1, ParserState("1").parse(digit))
        assertValue(2, ParserState("2").parse(digit))
        assertValue(3, ParserState("3").parse(digit))
        assertValue(4, ParserState("4").parse(digit))
        assertValue(5, ParserState("5").parse(digit))
        assertValue(6, ParserState("6").parse(digit))
        assertValue(7, ParserState("7").parse(digit))
        assertValue(8, ParserState("8").parse(digit))
        assertValue(9, ParserState("9").parse(digit))
    }

    @Test
    fun digit_fail() {
        assertFail(ParserState("a").parse(digit))
        assertFail(ParserState("_").parse(digit))
        assertFail(ParserState("").parse(digit))
    }

    @Test
    fun satisfy_pass_true() {
        assertPass(ParserState("c").parse(satisfy { true }))
        assertPass(ParserState(".").parse(satisfy { true }))
    }

    @Test
    fun satisfy_fail_true_empty() =
        assertFail(ParserState("").parse(satisfy { true }))

    @Test
    fun satisfy_fail_false() =
        assertFail(ParserState("c").parse(satisfy { false }))

    @Test
    fun satisfy_pass_func() = assertPass(ParserState("c").parse(satisfy {
        assertEquals('c', it)
        true
    }))
}