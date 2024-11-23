package com.erdodif.capsulate.lang

import com.erdodif.capsulate.lang.program.grammar.pStrLit
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import kotlin.test.Test

class LiteralTest {
    @Test
    fun `pStrLit passes with one char in the literal`(){
        pStrLit pass "\"s\" " match { it.value == "s" }
    }

    @Test
    fun `pStrLit passes empty string literal`(){
        pStrLit pass "\"\" " match { it.value == "" }
    }

    @Test
    fun `pStrLit passes escaped string literal`(){
        pStrLit pass "\"\\\"s \\e\"" match { it.value == "\"s e" }
    }

}


