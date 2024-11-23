package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.pass
import kotlin.test.Test

class TopLevelTest {
    @Test
    fun `EOF passes on an empty string`() {
        EOF pass "" at 0
    }

    @Test
    fun `EOF fails on a single character`(){
        EOF fail "c"
    }

    @Test
    fun `topLevel passes with EOF on empty string`() {
        topLevel(EOF) pass "" at 0
    }

    @Test
    fun `topLevel passes char exact`(){
        assertValue('c', ParserState("c").parse(topLevel(char('c'))))
    }

    @Test
    fun `topLevel fails with eof on single character`(){
        topLevel(EOF) fail "c"
    }

    @Test
    fun `topLevel fails with char on empty string`(){
        topLevel(char('c')) fail ""
    }

}