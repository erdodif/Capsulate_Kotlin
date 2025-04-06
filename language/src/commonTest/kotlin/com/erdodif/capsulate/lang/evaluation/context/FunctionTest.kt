@file:Suppress("UNCHECKED_CAST")

package com.erdodif.capsulate.lang.evaluation.context

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Add
import com.erdodif.capsulate.lang.program.grammar.expression.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Sub
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.Return
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

class FunctionTest {
    val pos = MatchPos.ZERO

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `niladic function call`() {
        val function =
            Function<Value>("x", listOf(), listOf(Return(IntLit(0, pos), match = pos)))
        val underTest = FunctionCall<Value>("x", listOf(), pos)
        var context =
            EvaluationContext(
                Env(listOf(function)), Expression(underTest, pos)
            )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        assertNull(context.functionOngoing)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `niladic function assign`() {
        val function =
            Function<Value>("x", listOf(), listOf(Return(IntLit(0, pos), match = pos)))
        val underTest = FunctionCall<Value>("x", listOf(), pos)
        var context = EvaluationContext(Env(listOf(function)), Assign("a", underTest, pos))
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(0, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `niladic function in simple expression`() {
        val function =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val underTest = FunctionCall(function, listOf(), pos)
        var context =
            EvaluationContext(
                Env(listOf(function as Function<Value>)),
                Assign(
                    "a",
                    BinaryCalculation<VNum, VNum>(underTest, IntLit(3, pos) as Exp<VNum>, Add),
                    pos
                )
            )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(5, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `niladic function in expression with another function`() {
        val constant1 =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val constant2 =
            Function<VNum>("y", listOf(), listOf(Return(IntLit(3, pos), match = pos)))
        val underTest1 = FunctionCall(constant1, listOf(), pos)
        val underTest2 = FunctionCall(constant2, listOf(), pos)
        var context =
            EvaluationContext(
                Env(listOf(constant1, constant2)),
                Assign("a", BinaryCalculation<VNum, VNum>(underTest1, underTest2, Add), pos)
            )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        context.step()
        context.step()
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `monodic function inside another function`() {
        val constant =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val function =
            Function<VNum>(
                "f", listOf(Variable("a", pos)), listOf(
                    Return(
                        BinaryCalculation<VNum, VNum>(
                            IntLit(3, pos) as Exp<VNum>,
                            Variable("a", pos) as Exp<VNum>,
                            Add
                        ), match = pos
                    )
                )
            )
        val underTest =
            FunctionCall(function, listOf(FunctionCall(constant, listOf(), pos) as Exp<Value>), pos)
        var context =
            EvaluationContext(Env(listOf(function, constant)), Assign("a", underTest, pos))
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `monodic function inside another function sub`() {
        val constant =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(-2, pos), match = pos)))
        val function =
            Function<VNum>(
                "f", listOf(Variable("a", pos)), listOf(
                    Return(
                        BinaryCalculation<VNum, VNum>(
                            IntLit(3, pos) as Exp<VNum>,
                            Variable("a", pos) as Exp<VNum>,
                            Sub
                        ), match = pos
                    )
                )
            )
        val underTest =
            FunctionCall(function, listOf(FunctionCall(constant, listOf(), pos) as Exp<Value>), pos)
        var context =
            EvaluationContext(Env(listOf(function, constant)), Assign("a", underTest, pos))
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        context.step()
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `test whether the operator returns the first operand call`() {
        val constant2 =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val constant3 =
            Function<VNum>("y", listOf(), listOf(Return(IntLit(3, pos), match = pos)))
        val underTest2 = FunctionCall(constant2, listOf(), pos)
        val underTest3 = FunctionCall(constant3, listOf(), pos)
        val context = EvaluationContext(
            Env(listOf(constant2, constant3)),
            Assign("a", BinaryCalculation(underTest2, underTest3, Add), pos)
        )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        context.step()
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5, (context.env.parameters[0].value as VNum).value)
    }
}
