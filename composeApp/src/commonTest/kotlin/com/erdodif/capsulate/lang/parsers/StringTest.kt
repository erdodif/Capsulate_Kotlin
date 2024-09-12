package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPassAt
import com.erdodif.capsulate.assertValue
import com.erdodif.capsulate.lang.MatchPos
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.string
import kotlin.test.Test

class StringTest {
    @Test
    fun string_fail_empty_string() = assertFail(ParserState("").parse(string("text")))

    @Test
    fun string_fail_shorter_string() = assertFail(ParserState("tex").parse(string("text")))

    @Test
    fun string_fail_longer_string() = assertFail(ParserState("texti").parse(string("text")))

    @Test
    fun string_fail_mismatched_string() {
        assertFail(ParserState("texz").parse(string("text")))
        assertFail(ParserState("zext").parse(string("text")))
    }

    @Test
    fun string_pass_string() = assertValue("text", ParserState("text").parse(string("text")))

    @Test
    fun string_pass_at_string() = assertPassAt(
        ParserState("text as").parse(string("text")),
        MatchPos(0, 4)
    )
}