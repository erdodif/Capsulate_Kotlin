package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Add
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Div
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Equal
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Larger
import com.erdodif.capsulate.lang.program.grammar.expression.operator.LargerEq
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Mul
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Smaller
import com.erdodif.capsulate.lang.program.grammar.expression.operator.SmallerEq
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Sub
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class OperatorTest {

    @Test
    fun `Add over VNat`() {
        val result = Add.operation(Environment.EMPTY, VNat(2U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(5, result.value)
    }

    @Test
    fun `Add over VWhole`() {
        val result = Add.operation(Environment.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(-1, result.value)
    }

    @Test
    fun `Sub over VNat`() {
        val result = Sub.operation(Environment.EMPTY, VNat(5U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(2, result.value)
    }

    @Test
    fun `Sub over VWhole`() {
        val result = Sub.operation(Environment.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(5, result.value)
    }

    @Test
    fun `Mul over VNat`() {
        val result = Mul.operation(Environment.EMPTY, VNat(5U), VNat(3U))
        assertIs<VNum>(result)
        assertEquals(15, result.value)
    }

    @Test
    fun `Mul over VWhole`() {
        val result = Mul.operation(Environment.EMPTY, VWhole(2), VWhole(-3))
        assertIs<VNum>(result)
        assertEquals(-6, result.value)
    }

    @Test
    fun `Div over VNat`() {
        val result = Div.operation(Environment.EMPTY, VNat(2U), VNat(2U))
        assertIs<VNum>(result)
        assertEquals(1, result.value)
    }

    @Test
    fun `Div over VWhole`() {
        val result = Div.operation(Environment.EMPTY, VWhole(5), VWhole(-2))
        assertIs<VNum>(result)
        assertEquals(-2, result.value)
    }

    @Test
    fun `Div fails on division by zero`() {
        assertFails("Division by Zero!") { Div.operation(Environment.EMPTY, VWhole(2), VWhole(0)) }
    }

    @Test
    fun `Equal over different types`() {
        val result = Equal.operation(Environment.EMPTY, VWhole(3), VStr("Hello"))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `Equal over same type but different value`() {
        val result = Equal.operation(Environment.EMPTY, VWhole(2), VWhole(3))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `Equal over same values`() {
        val result = Equal.operation(Environment.EMPTY, VWhole(2), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `Larger when the first argument is larger`() {
        val result = Larger.operation(Environment.EMPTY, VWhole(3), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `Larger when the first argument is smaller`() {
        val result = Larger.operation(Environment.EMPTY, VWhole(2), VWhole(3))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `Larger when the arguments are equal`() {
        val result = Larger.operation(Environment.EMPTY, VWhole(2), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `LargerEq when the first argument is larger`() {
        val result = LargerEq.operation(Environment.EMPTY, VWhole(3), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `LargerEq when the first argument is smaller`() {
        val result = LargerEq.operation(Environment.EMPTY, VWhole(2), VWhole(3))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `LargerEq when the arguments are equal`() {
        val result = LargerEq.operation(Environment.EMPTY, VWhole(2), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `Smaller when the first argument is larger`() {
        val result = Smaller.operation(Environment.EMPTY, VWhole(3), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `Smaller when the first argument is smaller`() {
        val result = Smaller.operation(Environment.EMPTY, VWhole(2), VWhole(3))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `Smaller when the arguments are equal`() {
        val result = Smaller.operation(Environment.EMPTY, VWhole(2), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `SmallerEq when the first argument is larger`() {
        val result = SmallerEq.operation(Environment.EMPTY, VWhole(3), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(false, result.value)
    }

    @Test
    fun `SmallerEq when the first argument is smaller`() {
        val result = SmallerEq.operation(Environment.EMPTY, VWhole(2), VWhole(3))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }

    @Test
    fun `SmallerEq when the arguments are equal`() {
        val result = SmallerEq.operation(Environment.EMPTY, VWhole(2), VWhole(2))
        assertIs<VBool>(result)
        assertEquals(true, result.value)
    }
}
