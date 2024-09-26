package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.Pass
import com.erdodif.capsulate.lang.pBoolLit
import com.erdodif.capsulate.lang.pComment
import com.erdodif.capsulate.lang.pIntLit
import com.erdodif.capsulate.lang.pStrLit
import com.erdodif.capsulate.lang.pVariable
import com.erdodif.capsulate.lang.plus
import com.erdodif.capsulate.lang.topLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class Exp {

    @Test
    fun boolLit_pass() {
        assertEquals(true, (ParserState("true ").parse(topLevel(pBoolLit)) as Pass).value.value)
        assertEquals(false, (ParserState("false ").parse(topLevel(pBoolLit)) as Pass).value.value)
    }

    @Test
    fun intLit_pass() {
        assertEquals(1234, (ParserState("1234 ").parse(topLevel(pIntLit)) as Pass).value.value)
        assertEquals(-1234, (ParserState("-1234 ").parse(topLevel(pIntLit)) as Pass).value.value)
    }

    @Test
    fun intLit_fail() {
        assertFail(ParserState("12 34 ").parse(topLevel(pIntLit)))
        assertFail(ParserState("- 1234 ").parse(topLevel(pIntLit)))
    }

    @Test
    fun variable_pass() {
        assertPass(ParserState("var ").parse(topLevel(pVariable)))
        assertPass(ParserState("var2 ").parse(topLevel(pVariable)))
    }

    @Test
    fun variable_fail() {
        assertFail(ParserState("2var ").parse(topLevel(pVariable)))
        assertFail(ParserState("var var").parse(topLevel(pVariable)))
    }

    @Test
    fun strLit_pass() {
        assertEquals("asd", (ParserState("\"asd\" ").parse(topLevel(pStrLit)) as Pass).value.value)
        assertEquals(
            "as df",
            (ParserState("\"as df\" ").parse(topLevel(pStrLit)) as Pass).value.value
        )
        assertEquals(
            "a\\sd",
            (ParserState("\"a\\\\sd\" ").parse(topLevel(pStrLit)) as Pass).value.value
        )
        assertEquals(
            "a\"sd",
            (ParserState("\"a\\\"sd\" ").parse(topLevel(pStrLit)) as Pass).value.value
        )
    }

    @Test
    fun strLit_fail() {
        assertFail(ParserState("\"asd ").parse(topLevel(pStrLit)))
        assertFail(ParserState("asd\" ").parse(topLevel(pStrLit)))
        assertFail(ParserState("asd ").parse(topLevel(pStrLit)))
        assertFail(ParserState("\"a\"sd\"").parse(topLevel(pStrLit)))
        assertFail(ParserState("\"asd\"\"").parse(topLevel(pStrLit)))
    }

    @Test
    fun commentPass(){
        assertPass(ParserState("//asda//s /**/ ").parse(topLevel(pComment)))
        assertPass(ParserState("/**/").parse(topLevel(pComment)))
        assertPass(ParserState("/*asd asd*/").parse(topLevel(pComment)))
        assertPass(ParserState("/*//*/").parse(topLevel(pComment)))
        assertPass(ParserState("/* ** /*/").parse(topLevel(pComment)))
        assertPass(ParserState("/*/* /*/").parse(topLevel(pComment)))
        assertPass(ParserState("//asdasdasdas\na").parse(topLevel(pComment + pVariable)))
        assertPass(ParserState("/*\nasd\nd*/ a").parse(topLevel(pComment + pVariable)))
    }
}
