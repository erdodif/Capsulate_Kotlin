package com.erdodif.capsulate.lang

import com.erdodif.capsulate.assertPass
import kotlin.test.Test
import kotlin.test.assertEquals

class LiteralTest {
    @Test
    fun pStrLit_pass_char_only(){
        val result = ParserState("\"s\"").parse(pStrLit)
        assertPass(result)
        result as Pass
        assertEquals("s",(result.value as StrLit).value)
    }

    @Test
    fun pStrLit_pass_empty_string(){
        val result = ParserState("\"\"").parse(pStrLit)
        assertPass(result)
        result as Pass
        assertEquals("",(result.value as StrLit).value)
    }

    @Test
    fun pStrLit_pass_escape(){
        val result = ParserState("\"\\\"s \\e\"").parse(pStrLit)
        assertPass(result)
        result as Pass
        assertEquals("\"s e",(result.value as StrLit).value)
    }


}


