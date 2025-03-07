package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.lang.evaluation.MockEnvironments.Companion.intEnv
import com.erdodif.capsulate.lang.program.evaluation.AbortEvaluation
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AbortTest {

    @Test
    fun `abort on empty environment`() {
        val env = Env.EMPTY
        val result = Abort(MatchPos.ZERO).evaluate(env)
        assertIs<AbortEvaluation>(result)
        assertEquals(0, env.parameters.size)
        assertEquals(0, env.functions.size)
        assertEquals(0, env.methods.size)
    }

    @Test
    fun `abort on non-empty environment`() {
        val env = intEnv()
        val result = Abort(MatchPos.ZERO).evaluate(env)
        assertIs<AbortEvaluation>(result)
        assertEquals(3, env.parameters.size)
        assertEquals(0, env.functions.size)
        assertEquals(0, env.methods.size)
        assertEquals(1, (env.parameters[0].value as VNat).value)
        assertEquals(4, (env.parameters[1].value as VNat).value)
        assertEquals(7, (env.parameters[2].value as VNat).value)
    }
}