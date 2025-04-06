package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.between
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.exactly
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class QuantityTest {

    @Test
    fun `optional passes char on empty string`() {
        optional(char('c')) pass "" withValue null at 0 withMatch (0 to 0)
    }

    @Test
    fun `optional passes char`() {
        optional(char('c')) pass "cr" withValue 'c' at 1 withMatch (0 to 1)
    }

    @Test
    fun `optional resets position`() {
        optional(char('c')) pass "sd" at 0 withMatch (0 to 0)
    }

    @Test
    fun `some fails no match`() {
        some(char('c')) fail "r" at 0
    }

    @Test
    fun `some pass resets position`() {
        some(char('c')) pass "ccr" match { it.size == 2 && it[0] == 'c' && it[1] == 'c' } at 2
    }

    @Test
    fun `many passes no match`() {
        many(char('c')) pass "" withMatch (0 to 0) at 0
        many(char('c')) pass "r" withMatch (0 to 0) at 0
    }

    @Test
    fun `many resets position`() {
        many(char('c')) pass "ccr" match { it.size == 2 && it[0] == 'c' && it[1] == 'c' } at 2 withMatch (0 to 2)
    }

    @Test
    fun `between fails few`() {
        between(2, 3, char('c')) fail "cr"
    }

    @Test
    fun `between resets position`() {
        between(2, 4, char('c')) pass "cccrr" match { it.size == 3 } withMatch (0 to 3)
    }

    @Test
    fun `exactly fails on too few`() {
        exactly(2, char('c')) fail "cr" at 2
    }

    @Test
    fun `exactly passes early on many`() {
        exactly(2, char('c')) pass "cccr" at 2 withMatch (0 to 2)
    }

    @Test
    fun `exactly passes`() {
        exactly(2, char('c')) pass "ccrr" match { it.size == 2 && it[0] == 'c' }
    }
}
