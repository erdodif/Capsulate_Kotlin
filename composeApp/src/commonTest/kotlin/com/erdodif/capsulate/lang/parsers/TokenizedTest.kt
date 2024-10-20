package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.program.grammar.EOF
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.program.grammar.and
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.util.tok
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
    fun tok_pass_chars() = assertPass(ParserState("c  r").parse(and(tok(char('c')), char('r'))))
}