package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.program.grammar.string
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class StringTest {
    @Test
    fun `string fails on empty string`(){
        string("text") fail ""
    }

    @Test
    fun `string fails on shorter string`(){
        string("text") fail "tex"
    }

    @Test
    fun `string fails on mismatched string`() {
        string("text") fail "texz" at 4
        string("text") fail "zext" at 0
    }

    @Test
    fun `string passes with value`(){
        string("text") pass "text" withValue "text"
    }

    @Test
    fun `string passes at location`(){
        string("text") pass "text as" at MatchPos(0,4)
    }
}