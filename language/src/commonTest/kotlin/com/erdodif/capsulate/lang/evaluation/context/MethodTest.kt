package com.erdodif.capsulate.lang.evaluation.context

import com.erdodif.capsulate.assertAborted
import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.evaluation.PendingMethodEvaluation
import com.erdodif.capsulate.lang.program.evaluation.ProxyEnv
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.expression.CHAR
import com.erdodif.capsulate.lang.program.grammar.expression.ChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.VChr
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.MethodCall
import com.erdodif.capsulate.lang.program.grammar.function.Pattern
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.performStep
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MethodTest {
    val pos = MatchPos.ZERO
    val emptyPattern = Pattern(null, emptyList(), emptyList(), null)

    private fun mockPatternOf(vararg variables: String) =
        Pattern(null, emptyList(), variables.map { Variable(it, pos) }.toList(), null)

    @Test
    fun `method call ends immediately`() {
        val underTest = Method(emptyPattern, listOf())
        val context = EvaluationContext(
            Env(),
            MethodCall(underTest, listOf(), pos)
        )
        context.performStep(2)
        assertNull(context.head)
    }

    @Test
    fun `method call reads given variable`() {
        val underTest = Method(
            mockPatternOf("a"), listOf(Assign("b", Variable("a", pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(values = listOf(Parameter("a", CHAR, VChr('c')))),
            MethodCall(underTest, listOf(Variable("a", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(1, env1.parameters.size)
        assertEquals(1, env1.env.parameters.size)
        context.performStep(1)
        val head2 = context.head
        assertIs<PendingMethodEvaluation>(head2)
        assertEquals(2, head2.context.env.parameters.size)
        val (a, b) = head2.context.env.parameters
        assertEquals('c', assertIs<VChr>(a.value).value)
        assertEquals('c', assertIs<VChr>(b.value).value)
        context.performStep(1)
        context.assertFinished()
    }

    @Test
    fun `method call overrides given variable`() {
        val underTest = Method(
            mockPatternOf("a"), listOf(Assign("a", ChrLit('d', pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(values = listOf(Parameter("a", CHAR, VChr('c')))),
            MethodCall(underTest, listOf(Variable("a", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(1, env1.parameters.size)
        assertEquals(1, env1.env.parameters.size)
        context.performStep(1)
        val head2 = context.head
        assertIs<PendingMethodEvaluation>(head2)
        assertEquals(1, head2.context.env.parameters.size)
        assertEquals('d', assertIs<VChr>(head2.context.env.parameters[0].value).value)
        assertEquals(
            'd',
            assertIs<VChr>(assertIs<ProxyEnv>(head2.context.env).env.parameters[0].value).value
        )
        context.performStep(1)
        context.assertFinished()
        assertEquals('d', assertIs<VChr>(context.env.parameters[0].value).value)
    }

    @Test
    fun `method call writes missing variable`() {
        val underTest = Method(
            mockPatternOf("a"), listOf(Assign("a", ChrLit('d', pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(),
            MethodCall(underTest, listOf(Variable("a", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(0, env1.parameters.size)
        assertEquals(0, env1.env.parameters.size)
        context.performStep(1)
        val head2 = context.head
        assertIs<PendingMethodEvaluation>(head2)
        val env2 = head2.context.env
        assertEquals(1, env2.parameters.size)
        assertEquals('d', assertIs<VChr>(env2.parameters[0].value).value)
        assertEquals('d', assertIs<VChr>(assertIs<ProxyEnv>(env2).env.parameters[0].value).value)
        context.performStep(1)
        context.assertFinished()
        assertEquals('d', assertIs<VChr>(context.env.parameters[0].value).value)
    }

    @Test
    fun `method call fails on missing variable`() {
        val underTest = Method(
            mockPatternOf("a", "b"), listOf(Assign("b", Variable("a", pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(),
            MethodCall(underTest, listOf(Variable("a", pos), Variable("a", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(0, env1.parameters.size)
        assertEquals(0, env1.env.parameters.size)
        context.performStep(1)
        context.assertAborted()
        assertTrue(context.env.parameters.isEmpty())
    }

    @Test
    fun `method call fails on hidden variable`() {
        val underTest = Method(
            mockPatternOf("a"), listOf(Assign("b", Variable("a", pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(values = listOf(Parameter("a", CHAR, VChr('c')))),
            MethodCall(underTest, listOf(Variable("x", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(0, env1.parameters.size)
        assertEquals(1, env1.env.parameters.size)
        context.performStep(1)
        context.assertAborted()
    }

    @Test
    fun `method call shadows not given variable`() {
        val underTest = Method(
            mockPatternOf("a"), listOf(Assign("b", Variable("a", pos), pos), Skip(pos))
        )
        val context = EvaluationContext(
            Env(values = listOf(Parameter("a", CHAR, VChr('c')), Parameter("b", CHAR, VChr('d')))),
            MethodCall(underTest, listOf(Variable("a", pos)), pos)
        )
        context.performStep(1)
        val head1 = context.head
        assertIs<PendingMethodEvaluation>(head1)
        val env1 = head1.context.env
        assertIs<ProxyEnv>(env1)
        assertEquals(1, env1.parameters.size)
        assertEquals(2, env1.env.parameters.size)
        context.performStep(1)
        val env2 = head1.context.env
        assertIs<ProxyEnv>(env2)
        assertEquals(2, env2.parameters.size)
        assertEquals(2, env2.env.parameters.size)
        val (a1, b1) = env2.parameters
        assertEquals('c', assertIs<VChr>(a1.value).value)
        assertEquals('c', assertIs<VChr>(b1.value).value)
        val (a2, b2) = context.env.parameters
        assertEquals('c', assertIs<VChr>(a2.value).value)
        assertEquals('d', assertIs<VChr>(b2.value).value)
        context.performStep(1)
        context.assertFinished()
    }
}
