package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.ChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.Index
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.NatLit
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.WHOLE
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos.Constants.ZERO
import com.erdodif.capsulate.lang.util.bg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class IndexerTest {


    @Test
    fun `indexer out of bounds`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        assertFailsWith<IllegalStateException> {
            Index("a", IntLit(0, ZERO)).evaluate(env)
        }
        assertFailsWith<IllegalStateException> {
            Index("a", IntLit(1, ZERO), IntLit(1, ZERO)).evaluate(env)
        }
        assertFailsWith<IllegalStateException> {
            Index("a", IntLit(4, ZERO)).evaluate(env)
        }
    }

    @Test
    fun `indexer straight up invalid`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        assertFailsWith<IllegalStateException> {
            Index("a", ChrLit('c', ZERO)).evaluate(env)
        }
    }

    @Test
    fun `indexer without depth acts like basic label`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(
            values = listOf(
                Parameter("a", value.type, value),
                Parameter("b", WHOLE, VWhole(2))
            )
        )
        val result1 = Index("a").evaluate(env)
        assertIs<Left<VArray<*>>>(result1)
        val result2 = Index("b").evaluate(env)
        assertIs<Left<VWhole>>(result2)
    }

    @Test
    fun `indexer correct on the first depth with shallow intArray`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        val result = Index("a", IntLit(1, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(1.bg, result.value.value)
    }

    @Test
    fun `indexer correct on the first depth with nested intArray`() {
        val value =
            VArray<VArray<VWhole>>(arrayOf(VArray(arrayOf(VWhole(1), VWhole(2), VWhole(3)))))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        val result = Index("a", IntLit(1, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VArray<VWhole>>(result.value)
        assertEquals(VWhole(1), result.value[1])
        assertEquals(VWhole(2), result.value[2])
        assertEquals(VWhole(3), result.value[3])
    }

    @Test
    fun `indexer correct on the second depth`() {
        val value =
            VArray<VArray<VWhole>>(arrayOf(VArray(arrayOf(VWhole(1), VWhole(2), VWhole(3)))))
        val env = Env(values = listOf(Parameter("a", value.type, value)))
        val result = Index("a", IntLit(1, ZERO), NatLit(2, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(2.bg, result.value.value)
    }
}
