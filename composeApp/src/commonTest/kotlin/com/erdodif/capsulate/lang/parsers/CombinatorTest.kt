package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.program.grammar.and
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.not
import com.erdodif.capsulate.lang.program.grammar.or
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class CombinatorTest {
    @Test
    fun `and passes`(){
        and(char('c'), char('r')) pass "cr" match {it.first == 'c' && it.second == 'r'}
    }

    @Test
    fun `and fails on first failure`(){
        and(char('c'), char('r')) fail "xr" at 1
    }

    @Test
    fun `and fails on second failure`() {
        and(char('c'), char('r')) fail "cx" at 2
    }

    @Test
    fun `or passes first at position`() {
        or(char('c'), char('r')) pass "c" at MatchPos(0,1)
    }

    @Test
    fun `or passes second at position`() {
        or(char('c'), char('r')) pass "r" at MatchPos(0,1)
    }
    @Test
    fun `or passes with first value`(){
        or(char('c'), char('r')) pass "c" match { (it as Left).value == 'c'}
    }

    @Test
    fun `or passes with second value`(){
        or(char('c'), char('r')) pass "r"  match { (it as Right).value == 'r'}
    }

    @Test
    fun `or fails`(){
        or(char('c'), char('r')) fail "x"
    }

    @Test
    fun `not fails on success`(){
        not(char('c')) fail "c" at 1
    }

    @Test
    fun `not passes`() {
        not(char('c')) pass "x"
    }

    @Test
    fun `not resets position`() {
        not(char('c')) pass "x" at MatchPos(0,0)
    }

}