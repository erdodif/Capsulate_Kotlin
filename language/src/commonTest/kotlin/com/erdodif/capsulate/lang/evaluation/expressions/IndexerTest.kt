package com.erdodif.capsulate.lang.evaluation.expressions

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.ChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.util.MatchPos.Constants.ZERO
import kotlin.test.Test
import kotlin.test.assertFailsWith

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
            VArray.Index("a", IntLit(2, ZERO)).evaluate(env)
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
    fun `indexer correct on the first depth`() {
        //TODO: FINISH
    }

    @Test
    fun `indexer correct on the second depth`() {
        //TODO: FINISH
    }
}