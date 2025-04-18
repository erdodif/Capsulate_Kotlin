package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.ChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.NatLit
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos.Constants.ZERO
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
            VArray.Index("a", IntLit(0, ZERO)).evaluate(env)
        }
        assertFailsWith<IllegalStateException> {
            VArray.Index("a", IntLit(1, ZERO), IntLit(1, ZERO)).evaluate(env)
        }
        assertFailsWith<IllegalStateException> {
            VArray.Index("a", IntLit(4, ZERO)).evaluate(env)
        }
    }

    @Test
    fun `indexer straight up invalid`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        assertFailsWith<IllegalStateException> {
            VArray.Index("a").evaluate(env)
        }
        assertFailsWith<IllegalStateException> {
            VArray.Index("a", ChrLit('c', ZERO)).evaluate(env)
        }
    }

    @Test
    fun `indexer correct on the first depth with shallow intArray`() {
        val value = VArray<VWhole>(arrayOf(VWhole(1), VWhole(2), VWhole(3)))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        val result = VArray.Index("a", IntLit(1, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(1, result.value.value)
    }

    @Test
    fun `indexer correct on the first depth with nested intArray`() {
        val value = VArray<VArray<VWhole>>(arrayOf(VArray(arrayOf(VWhole(1), VWhole(2), VWhole(3)))))
        val env = Env(values = listOf(Parameter("a", value.type, value)))

        val result = VArray.Index("a", IntLit(1, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VArray<VWhole>>(result.value)
        assertEquals(VWhole(1), result.value[1])
        assertEquals(VWhole(2), result.value[2])
        assertEquals(VWhole(3), result.value[3])
    }

    @Test
    fun `indexer correct on the second depth`() {
        val value = VArray<VArray<VWhole>>(arrayOf(VArray(arrayOf(VWhole(1), VWhole(2), VWhole(3)))))
        val env = Env(values = listOf(Parameter("a", value.type, value)))
        val result = VArray.Index("a", IntLit(1, ZERO), NatLit(2u, ZERO)).evaluate(env)
        assertIs<Left<*>>(result)
        assertIs<VWhole>(result.value)
        assertEquals(2, result.value.value)
    }
}