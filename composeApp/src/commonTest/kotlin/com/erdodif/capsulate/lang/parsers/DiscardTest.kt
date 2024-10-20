package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.right
import kotlin.test.Test

class DiscardTest {
    @Test
    fun left_pass_char() = assertValue('c', ParserState("cr").parse(left(char('c'), char('r'))))

    @Test
    fun left_fail_char_first() = assertFail(ParserState("xr").parse(left(char('c'), char('r'))))

    @Test
    fun left_fail_char_second() = assertFail(ParserState("cx").parse(left(char('c'), char('r'))))

    @Test
    fun right_pass_char() = assertValue('r', ParserState("cr").parse(right(char('c'), char('r'))))

    @Test
    fun right_fail_char_first() = assertFail(ParserState("xr").parse(right(char('c'), char('r'))))

    @Test
    fun right_fail_char_second() = assertFail(ParserState("cx").parse(right(char('c'), char('r'))))

    @Test
    fun middle_pass_char() =
        assertValue('c', ParserState("lcr").parse(middle(char('l'), char('c'), char('r'))))

    @Test
    fun middle_fail_char_left() =
        assertFail(ParserState("xcr").parse(middle(char('l'), char('c'), char('r'))))

    @Test
    fun middle_fail_char_middle() =
        assertFail(ParserState("lxr").parse(middle(char('l'), char('c'), char('r'))))

    @Test
    fun middle_fail_char_right() =
        assertFail(ParserState("lcx").parse(middle(char('l'), char('c'), char('r'))))

}