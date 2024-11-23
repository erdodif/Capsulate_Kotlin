package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.digit
import com.erdodif.capsulate.lang.program.grammar.satisfy
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CharTest {
    @Test
    fun `char matches a single character`() {
        char('c') pass "c" at MatchPos(0,1)
    }

    @Test
    fun `char returns a single character`() {
        char('c') pass "c" withValue 'c'
    }

    @Test
    fun `char fails empty string`() {
        char('c') fail ""
    }

    @Test
    fun `digit passes with correct value`() {
        digit pass "0" withValue 0
        digit pass "1" withValue 1
        digit pass "2" withValue 2
        digit pass "3" withValue 3
        digit pass "4" withValue 4
        digit pass "5" withValue 5
        digit pass "6" withValue 6
        digit pass "7" withValue 7
        digit pass "8" withValue 8
        digit pass "9" withValue 9
    }

    @Test
    fun `digit fails`() {
        digit fail "a" at 1
        digit fail "_" at 1
        digit fail "" at 0
    }

    @Test
    fun `satisfy passes constant true`() {
        satisfy { true } pass "c"
        satisfy { true } pass "."
    }

    @Test
    fun `satisfy fails constant true on empty`(){
        satisfy { true } fail ""
    }

    @Test
    fun `satisfy fails constant false`() {
        satisfy { false } fail ""
    }

    @Test
    fun `satisfy passes value into function call correctly`() {
        satisfy {
            assertEquals('c', it)
            true
        } pass "c"
    }
}