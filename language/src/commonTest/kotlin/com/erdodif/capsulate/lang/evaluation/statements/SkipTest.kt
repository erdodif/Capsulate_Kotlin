package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.lang.evaluation.MockEnvironments.intEnv
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.utils.EMPTY_ENVIRONMENT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SkipTest {

    @Test
    fun `skip on empty environment`() {
        val env = EMPTY_ENVIRONMENT
        val result = Skip(MatchPos.ZERO).evaluate(env)
        assertIs<Finished>(result)
        assertEquals(0, env.parameters.size)
        assertEquals(0, env.functions.size)
        assertEquals(0, env.methods.size)
    }

    @Test
    fun `skip on non-empty environment`() {
        val env = intEnv()
        val result = Skip(MatchPos.ZERO).evaluate(env)
        assertIs<Finished>(result)
        assertEquals(3, env.parameters.size)
        assertEquals(0, env.functions.size)
        assertEquals(0, env.methods.size)
        assertEquals(1.bg, (env.parameters[0].value as VNat).value)
        assertEquals(4.bg, (env.parameters[1].value as VNat).value)
        assertEquals(7.bg, (env.parameters[2].value as VNat).value)
    }
}
