package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class TopLevelTest {
    @Test
    fun `EOF passes on an empty string`() {
        EOF pass "" at 0 withMatch (0 to 0)
    }

    @Test
    fun `EOF fails on a single character`(){
        EOF fail "c" at 0
    }

    @Test
    fun `topLevel passes with EOF on empty string`() {
        topLevel(EOF) pass "" at 0 withMatch (0 to 0)
    }

    @Test
    fun `topLevel passes char exact`(){
        topLevel(char('c')) pass "c" withValue 'c' at 1 withMatch (0 to 1)
    }

    @Test
    fun `topLevel fails with eof on single character`(){
        topLevel(EOF) fail "c" at 0
    }

    @Test
    fun `topLevel fails with char on empty string`(){
        topLevel(char('c')) fail "" at 0
    }

}