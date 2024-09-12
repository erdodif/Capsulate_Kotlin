package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.EOF
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.and
import com.erdodif.capsulate.lang.char
import com.erdodif.capsulate.lang.tok
import kotlin.test.Test

class TokenizedTest {
    @Test
    fun tok_pass_eof_empty() = assertPass(ParserState("").parse(tok(EOF)))

    @Test
    fun tok_pass_true_ws() = assertPass(ParserState("  ").parse(tok { pass(0,Unit) }))

    @Test
    fun tok_pass_char_only() = assertPass(ParserState("c").parse(tok(char('c'))))

    @Test
    fun tok_pass_char_ws() = assertPass(ParserState("c  ").parse(tok(char('c'))))

    @Test
    fun tok_pass_chars() = assertPass(ParserState("c  r").parse(and(tok(char('c')),char('r'))))
}