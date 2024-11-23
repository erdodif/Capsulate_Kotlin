package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.program.grammar.and
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.tok
import com.erdodif.capsulate.pass
import kotlin.test.Test

class TokenizedTest {
    @Test
    fun `tok passes eof on empty`(){
        tok(EOF) pass ""
    }

    @Test
    fun `tok fails eof with whitespace`(){
        tok(EOF) pass "  "
        topLevel(tok(EOF)) pass "  "
    }

    @Test
    fun `tok passes constant pass with whitespace`(){
        tok { pass(0,Unit) } pass " "
    }

    @Test
    fun `tok passes char exact`(){
        tok(char('c')) pass "c"
    }

    @Test
    fun `tok passes char with whitespace`(){
        tok(char('c')) pass "c  "
        topLevel(tok(char('c'))) pass "c  "
    }

    @Test
    fun `tok passes two char parser`(){
        and(tok(char('c')), char('r')) pass "c  r"
    }
}