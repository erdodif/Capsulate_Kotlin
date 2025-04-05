package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class DiscardTest {
    @Test
    fun `left passes chars first value`(){
        left(char('c'), char('r')) pass "cr" withValue 'c' at 2
    }

    @Test
    fun `left fails when first char fails`(){
        left(char('c'), char('r')) fail "xr" at 1
    }

    @Test
    fun `left fails when second char fails`(){
        left(char('c'), char('r')) fail "cx" at 2
    }

    @Test
    fun `right passes chars second value`(){
        right(char('c'), char('r')) pass "cr" withValue 'r' at 2
    }

    @Test
    fun `right fail char first`(){
        right(char('c'), char('r')) fail "xr" at 1
    }

    @Test
    fun `right fail char second`(){
        right(char('c'), char('r')) fail "cx" at 2
    }

    @Test
    fun `middle passes chars second value`(){
        middle(char('l'), char('c'), char('r')) pass "lcr" withValue 'c' at 3
    }

    @Test
    fun `middle fail char left`(){
        middle(char('l'), char('c'), char('r')) fail "xcr" at 1
    }

    @Test
    fun `middle fail char middle`(){
        middle(char('l'), char('c'), char('r')) fail "lxr" at 2
    }

    @Test
    fun `middle fail char right`(){
        middle(char('l'), char('c'), char('r')) fail "lcx" at 3
    }
}
