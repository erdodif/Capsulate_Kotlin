package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Add
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Div
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Mul
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Sub
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class OperatorTest {

    @Test
    fun `Add over VNat`() {
        val result = Add.operation(Env.EMPTY, VNat(2U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(5, result.value)
    }

    @Test
    fun `Add over VWhole`() {
        val result = Add.operation(Env.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(-1, result.value)
    }

    @Test
    fun `Sub over VNat`() {
        val result = Sub.operation(Env.EMPTY, VNat(5U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(2, result.value)
    }

    @Test
    fun `Sub over VWhole`() {
        val result = Sub.operation(Env.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(5, result.value)
    }

    @Test
    fun `Mul over VNat`() {
        val result = Mul.operation(Env.EMPTY, VNat(5U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(15, result.value)
    }

    @Test
    fun `Mul over VWhole`() {
        val result = Mul.operation(Env.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(-6, result.value)
    }

    @Test
    fun `Div over VNat`() {
        val result = Div.operation(Env.EMPTY, VNat(2U), VNat(2U))
        assertIs<VNum>(result)
        assertEquals(1, result.value)
    }

    @Test
    fun `Div over VWhole`() {
        val result = Div.operation(Env.EMPTY, VWhole(5), VWhole(-2))
        assertIs<VNum>(result)
        assertEquals(-2, result.value)
    }

    @Test
    fun `Div fails on division by zero`() {
        assertFails("Division by Zero!") { Div.operation(Env.EMPTY, VWhole(2), VWhole(0)) }
    }

}