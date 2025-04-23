package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.grammar.expression.ARRAY
import com.erdodif.capsulate.lang.program.grammar.expression.ArrayLit
import com.erdodif.capsulate.lang.program.grammar.expression.ChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.NatLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.lang.program.grammar.expression.VChr
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.WHOLE
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.utils.EMPTY_ENVIRONMENT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LiteralTest {
    private val pos = MatchPos.ZERO

    @Test
    fun `evaluate NatLit`() {
        val result = NatLit(0, pos).evaluate(EMPTY_ENVIRONMENT)
        assertIs<Left<*>>(result)
        assertIs<VNat>(result.value)
        assertEquals(0.bg, result.value.value)
    }

    @Test
    fun `evaluate IntLit`() {
        val result = IntLit(0, pos).evaluate(EMPTY_ENVIRONMENT)
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(0.bg, result.value.value)
    }

    @Test
    fun `evaluate StrLit`() {
        val result = StrLit("text", pos).evaluate(EMPTY_ENVIRONMENT)
        assertIs<Left<*>>(result)
        assertIs<VStr>(result.value)
        assertEquals("text", result.value.value)
    }

    @Test
    fun `evaluate ChrLit`() {
        val result = ChrLit('c', pos).evaluate(EMPTY_ENVIRONMENT)
        assertIs<Left<*>>(result)
        assertIs<VChr>(result.value)
        assertEquals('c', result.value.value)
    }

    @Test
    fun `evaluate ArrayLit on the first level`() {
        val result = assertIs<Left<*>>(
            ArrayLit<VNum<*>>(
                arrayOf<Exp<VNum<*>>>(
                    IntLit(1, pos),
                    IntLit(3, pos),
                    IntLit(5, pos)
                ), pos
            )
                .evaluate(EMPTY_ENVIRONMENT)
        ).value
        assertIs<VArray<VNum<*>>>(result)
        assertEquals(ARRAY(WHOLE, 3), result.type)
        assertEquals(3, result.size)
        assertEquals(1, result.depth)
        assertIs<WHOLE>(result.contentType)
        assertIs<WHOLE>(result.type.primitiveType)
        val first = assertIs<VWhole>(result[1])
        assertEquals(1.bg, first.value)
        val second = assertIs<VWhole>(result[2])
        assertEquals(3.bg, second.value)
        val third = assertIs<VWhole>(result[3])
        assertEquals(5.bg, third.value)
    }

    @Test
    fun `evaluate ArrayLit on the second level`() {
        val result = assertIs<Left<*>>(
            ArrayLit<VArray<VNum<*>>>(
                arrayOf(
                    ArrayLit(arrayOf<Exp<VNum<*>>>(IntLit(1, pos), IntLit(2, pos)), pos),
                    ArrayLit(arrayOf<Exp<VNum<*>>>(IntLit(3, pos), IntLit(4, pos)), pos),
                    ArrayLit(arrayOf<Exp<VNum<*>>>(IntLit(5, pos), IntLit(6, pos)), pos)
                ), pos
            )
                .evaluate(EMPTY_ENVIRONMENT)
        ).value
        assertIs<VArray<VNum<*>>>(result)
        assertEquals(ARRAY(ARRAY(WHOLE, 2), 3), result.type)
        assertEquals(3, result.size)
        assertEquals(2, result.depth)
        assertIs<ARRAY>(result.contentType)
        assertIs<WHOLE>(result.type.primitiveType)
        val first = assertIs<VArray<VWhole>>(result[1])
        val second = assertIs<VArray<VWhole>>(result[2])
        val third = assertIs<VArray<VWhole>>(result[3])
        assertEquals(1.bg, assertIs<VWhole>(first[1]).value)
        assertEquals(first[1], result[1, 1])
        assertEquals(first[1], result[1, 1])
        assertEquals(3.bg, assertIs<VWhole>(second[1]).value)
        assertEquals(second[1], result[2, 1])
        assertEquals(second[2], result[2, 2])
        assertEquals(5.bg, assertIs<VWhole>(third[1]).value)
        assertEquals(third[1], result[3, 1])
        assertEquals(third[2], result[3, 2])
    }

}
