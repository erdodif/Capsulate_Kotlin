package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertFailsAt
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.assertPassAt
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.grammar.and
import com.erdodif.capsulate.lang.grammar.char
import com.erdodif.capsulate.lang.grammar.not
import com.erdodif.capsulate.lang.grammar.or
import kotlin.test.Test
import kotlin.test.assertEquals

class CombinatorTest {
    @Test
    fun and_pass(){
        val result = ParserState("cr").parse(and(char('c'), char('r')))
        assertPassAt(result, MatchPos(0,2))
        result as Pass
        assertEquals('c', result.value.first)
        assertEquals('r', result.value.second)
    }

    @Test
    fun and_fail_first() = assertFailsAt(
        1,
        ParserState("xr"),
        and(char('c'), char('r'))
    )

    @Test
    fun and_fail_second() = assertFailsAt(
        2,
        ParserState("cx"),
        and(char('c'), char('r'))
    )

    @Test
    fun or_pass_first_at() = assertPassAt(
        ParserState("c").parse(or(char('c'), char('r'))),
        MatchPos(0,1)
    )

    @Test
    fun or_pass_second_at() = assertPassAt(
        ParserState("r").parse(or(char('c'), char('r'))),
        MatchPos(0,1)
    )

    @Test
    fun or_pass_first() = assertValue(Left('c'), ParserState("c").parse(or(char('c'), char('c'))))

    @Test
    fun or_pass_second() = assertValue(Right('r'), ParserState("r").parse(or(char('c'), char('r'))))

    @Test
    fun or_fail() = assertFail(ParserState("x").parse(or(char('c'), char('r'))))

    @Test
    fun not_fail() = assertFail(ParserState("c").parse(not(char('c'))))

    @Test
    fun not_pass() = assertPassAt(ParserState("x").parse(not(char('c'))), MatchPos(0,0))

    @Test
    fun not_reset() {
        val state = ParserState("x")
        assertPass(state.parse(not(char('c'))))
        assertEquals(0, state.position)
    }

}