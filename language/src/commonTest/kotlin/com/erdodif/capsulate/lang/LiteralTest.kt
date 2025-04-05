package com.erdodif.capsulate.lang

import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.pStrLit
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import kotlin.test.Test

class LiteralTest {
    @Test
    fun `pStrLit passes with one char in the literal`() {
        pStrLit pass "\"s\" " match { it.value == "s" } withMatch (0 to 3) at 4
    }

    @Test
    fun `pStrLit passes empty string literal`() {
        pStrLit pass "\"\" " match { it.value == "" } at 3
    }

    @Test
    fun `pStrLit passes escaped string literal`() {
        pStrLit pass "\"\\\"s \\e\"" match { it.value == "\"s e" } at 8
    }

}


