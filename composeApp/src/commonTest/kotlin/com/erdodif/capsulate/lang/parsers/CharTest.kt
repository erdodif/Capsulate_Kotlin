package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.digit
import com.erdodif.capsulate.lang.program.grammar.satisfy
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import com.erdodif.capsulate.withValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CharTest {
    @Test
    fun `char matches a single character`() {
        char('c') pass "c" withMatch (0 to 1)
    }

    @Test
    fun `char returns a single character`() {
        char('c') pass "c" withValue 'c' at 1 withMatch (0 to 1)
    }

    @Test
    fun `char fails empty string`() {
        char('c') fail "" at 0
    }

    @Test
    fun `digit passes with correct value`() {
        digit pass "0" withValue 0 at 1 withMatch (0 to 1)
        digit pass "1" withValue 1 at 1 withMatch (0 to 1)
        digit pass "2" withValue 2 at 1 withMatch (0 to 1)
        digit pass "3" withValue 3 at 1 withMatch (0 to 1)
        digit pass "4" withValue 4 at 1 withMatch (0 to 1)
        digit pass "5" withValue 5 at 1 withMatch (0 to 1)
        digit pass "6" withValue 6 at 1 withMatch (0 to 1)
        digit pass "7" withValue 7 at 1 withMatch (0 to 1)
        digit pass "8" withValue 8 at 1 withMatch (0 to 1)
        digit pass "9" withValue 9 at 1 withMatch (0 to 1)
    }

    @Test
    fun `digit fails`() {
        digit fail "a" at 1
        digit fail "_" at 1
        digit fail "" at 0
    }

    @Test
    fun `satisfy passes constant true`() {
        satisfy { true } pass "c" at 1 withMatch (0 to 1)
        satisfy { true } pass "." at 1 withMatch (0 to 1)
    }

    @Test
    fun `satisfy fails constant true on empty`(){
        satisfy { true } fail "" at 0
    }

    @Test
    fun `satisfy fails constant false`() {
        satisfy { false } fail "s" at 1
    }

    @Test
    fun `satisfy passes value into function call correctly`() {
        satisfy {
            assertEquals('c', it)
            true
        } pass "c"
    }
}
