package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.evaluation.MockEnvironments.Companion.emptyEnv
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.NatLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LiteralTest {
    private val pos = MatchPos.ZERO

    @Test
    fun `evaluate NatLit`() {
        val result = NatLit(0U, pos).evaluate(emptyEnv())
        assertIs<Left<*>>(result)
        assertIs<VNat>(result.value)
        assertEquals(0, result.value.value)
    }

    @Test
    fun `evaluate IntLit`() {
        val result = IntLit(0, pos).evaluate(emptyEnv())
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(0, result.value.value)
    }

    @Test
    fun `evaluate StrLit`() {
        val result = StrLit("text", pos).evaluate(emptyEnv())
        assertIs<Left<*>>(result)
        assertIs<VStr>(result.value)
        assertEquals("text", result.value.value)
    }
}