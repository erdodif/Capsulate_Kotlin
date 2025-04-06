package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.program.grammar.and
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.tok
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import kotlin.test.Test

class TokenizedTest {
    @Test
    fun `tok passes eof on empty`() {
        tok(EOF) pass "" withMatch (0 to 0)
    }

    @Test
    fun `topLevel tokenized nothing passes on whitespace`() {
        topLevel(tok{ pass(0)}) pass "  " withMatch (0 to 2)
    }

    @Test
    fun `tok fails eof with whitespace`() {
        tok(EOF) fail "  " at 0
    }

    @Test
    fun `tok passes constant pass with whitespace`() {
        tok { pass(0, Unit) } pass " " at 1 withMatch (0 to 0)
    }

    @Test
    fun `tok passes char exact`() {
        tok(char('c')) pass "c" at 1 withMatch (0 to 1)
    }

    @Test
    fun `tok passes char with whitespace`() {
        tok(char('c')) pass "c  " at 3 withMatch (0 to 1)
        topLevel(tok(char('c'))) pass "c  " at 3 withMatch (0 to 1)
    }

    @Test
    fun `tok passes two char parser`() {
        and(tok(char('c')), char('r')) pass "c  r" at 4 withMatch (0 to 4)
    }
}
