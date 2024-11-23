package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.between
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.exactly
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.optional
import com.erdodif.capsulate.lang.program.grammar.some
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class QuantityTest {

    @Test
    fun `optional passes char on empty string`() {
        optional(char('c')) pass "" withValue null
    }

    @Test
    fun `optional passes char`(){
        optional(char('c')) pass "cr" withValue 'c'
    }

    @Test
    fun `optional resets position`() {
        optional(char('c')) pass "sd" at 0
    }

    @Test
    fun `some fails no match`(){
        some(char('c')) fail "r"
    }

    @Test
    fun `some pass resets position`() {
        some(char('c')) pass "ccr" match {it.size ==2  && it[0] == 'c' && it[1] == 'c'} at 2
    }

    @Test
    fun `many passes no match`() {
        many(char('c')) pass "" at 0
        many(char('c')) pass "r" at 0
    }

    @Test
    fun `many resets position`() {
        many(char('c')) pass "ccr" match {it.size == 2 && it[0] =='c' && it[1] == 'c' } at 2
    }

    @Test
    fun `between fails few`() {
        between(2, 3, char('c')) fail "cr"
    }

    @Test
    fun `between resets position`() {
        between(2, 4, char('c')) pass "cccrr" match {it.size == 3} at MatchPos(0,3)
    }

    @Test
    fun `exactly fails on too few`(){
        exactly(2, char('c')) fail "cr" at 2
    }

    @Test
    fun `exactly passes early on many`() {
        exactly(2, char('c')) pass "cccr" at 2
    }

    @Test
    fun `exactly passes`() {
        exactly(2, char('c')) pass "ccrr" match { it.size == 2 && it[0] == 'c' }
    }

}