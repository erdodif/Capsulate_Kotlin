package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.lang.evaluation.MockEnvironments.Companion.intEnv
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.NatLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AssignTest {
    private val pos = MatchPos.ZERO

    @Test
    fun `assign in empty environment`() {
        val env = Environment.EMPTY
        val result1 = Assign("a", IntLit(0, pos), pos).evaluate(env)
        assertIs<Finished>(result1)
        val result2 = Assign("b", NatLit(0U, pos), pos).evaluate(env)
        assertIs<Finished>(result2)
        val result3 = Assign("c", StrLit("text", pos), pos).evaluate(env)
        assertIs<Finished>(result3)
        assertEquals(3, env.parameters.size)
        assertEquals(0, (env.parameters[0].value as VWhole).value)
        assertEquals(0, (env.parameters[1].value as VNat).value)
        assertEquals("text", (env.parameters[2].value as VStr).value)
    }

    @Test
    fun `assign overrides existing value`() {
        val env = intEnv()
        val result1 = Assign("a", IntLit(10, pos), pos).evaluate(env)
        assertIs<Finished>(result1)
        assertEquals(10, (env.parameters[0].value as VWhole).value)
    }

    @Test
    fun `assign parameter to existing parameter`() {
        val env = intEnv()
        val result1 = Assign("a", Variable("b", pos), pos).evaluate(env)
        assertIs<Finished>(result1)
        assertEquals(3, env.parameters.size)
        assertEquals(4, (env.parameters[0].value as VNat).value)
        assertEquals(4, (env.parameters[1].value as VNat).value)
        assertEquals(7, (env.parameters[2].value as VNat).value)
        val result2 = Assign("a", Variable("c", pos), pos).evaluate(env)
        assertIs<Finished>(result2)
        assertEquals(7, (env.parameters[0].value as VNat).value)
        assertEquals(4, (env.parameters[1].value as VNat).value)
        assertEquals(7, (env.parameters[2].value as VNat).value)
    }
}
