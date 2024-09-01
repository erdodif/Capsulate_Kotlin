package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.EOF
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.char
import com.erdodif.capsulate.lang.topLevel
import kotlin.test.Test

class TopLevelTest {
    @Test
    fun eof_pass_empty_string() {
        ParserState("").run(EOF)
    }

    @Test
    fun eof_fail_single_character() = assertFail(ParserState("c").parse(EOF))

    @Test
    fun topLevel_pass_eof_empty() = assertPass(ParserState("").parse(topLevel(EOF)))

    @Test
    fun topLevel_pass_char_only() = assertValue('c',ParserState("c").parse(topLevel(char('c'))))

    @Test
    fun topLevel_fail_eof_char() = assertFail(ParserState("c").parse(topLevel(EOF)))

    @Test
    fun topLevel_fail_char_empty() = assertFail(ParserState("").parse(topLevel(char('c'))))

}