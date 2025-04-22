@file:Suppress("UNCHECKED_CAST")

package com.erdodif.capsulate.lang.evaluation.context

import com.erdodif.capsulate.assertAborted
import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.expression.WHOLE
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Add
import com.erdodif.capsulate.lang.program.grammar.expression.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Sub
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.Return
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.performStep
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FunctionTest {
    val pos = MatchPos.ZERO

    @Test
    fun `function breaks on missing return`() {
        val function =
            Function<Value>("x", listOf(), listOf(Assign("a", IntLit(0, pos), match = pos)))
        val underTest = FunctionCall<Value>("x", listOf(), pos)
        var context = EvaluationContext(Env(listOf(function)), Expression(underTest, pos))
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(2)
        context.assertAborted()
    }

    @Test
    fun `niladic function call`() {
        val function = Function<Value>("x", listOf(), listOf(Return(IntLit(0, pos), match = pos)))
        val underTest = FunctionCall<Value>("x", listOf(), pos)
        var context = EvaluationContext(
            Env(listOf(function)), Expression(underTest, pos)
        )
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(2)
        context.assertFinished()
    }

    @Test
    fun `niladic function assign`() {
        val function = Function<Value>("x", listOf(), listOf(Return(IntLit(0, pos), match = pos)))
        val underTest = FunctionCall<Value>("x", listOf(), pos)
        var context = EvaluationContext(Env(listOf(function)), Assign("a", underTest, pos))
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(2)
        assertNull(context.functionOngoing)
        assertEquals(0.bg, (context.env.parameters[0].value as VNum<*>).value)
        context.assertFinished()
    }

    @Test
    fun `niladic function in simple expression`() {
        val function = Function<VNum<*>>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val underTest = FunctionCall(function, listOf(), pos)
        var context = EvaluationContext(
            Env(listOf(function as Function<Value>)), Assign(
                "a", BinaryCalculation<VNum<*>, VNum<*>>(
                    underTest, IntLit(3, pos) as Exp<VNum<*>>, Add
                ), pos
            )
        )
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(2)
        assertNull(context.functionOngoing)
        assertEquals(5.bg, (context.env.parameters[0].value as VNum<*>).value)
        context.assertFinished()
    }

    @Test
    fun `niladic function in expression with another function`() {
        val constant1 =
            Function<VNum<*>>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val constant2 =
            Function<VNum<*>>("y", listOf(), listOf(Return(IntLit(3, pos), match = pos)))
        val underTest1 = FunctionCall(constant1, listOf(), pos)
        val underTest2 = FunctionCall(constant2, listOf(), pos)
        var context = EvaluationContext(
            Env(listOf(constant1, constant2) as List<Function<Value>>),
            Assign("a", BinaryCalculation<VNum<*>, VNum<*>>(underTest1, underTest2, Add), pos)
        )
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(6)
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5.bg, (context.env.parameters[0].value as VNum<*>).value)
        context.assertFinished()
    }

    @Test
    fun `monodic function inside another function`() {
        val constant = Function<VNum<*>>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val function = Function<VNum<*>>(
            "f", listOf(Variable("a", pos)), listOf(
                Return(
                    BinaryCalculation<VNum<*>, VNum<*>>(
                        IntLit(3, pos) as Exp<VNum<*>>, Variable("a", pos) as Exp<VNum<*>>, Add
                    ), match = pos
                )
            )
        )
        val underTest =
            FunctionCall(function, listOf(FunctionCall(constant, listOf(), pos) as Exp<Value>), pos)
        var context = EvaluationContext(
            Env(listOf(function, constant) as List<Function<Value>>), Assign("a", underTest, pos)
        )
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(4)
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5.bg, (context.env.parameters[0].value as VNum<*>).value)
        context.assertFinished()
    }

    @Test
    fun `monodic function inside another function sub`() {
        val constant =
            Function<VNum<*>>("x", listOf(), listOf(Return(IntLit(-2, pos), match = pos)))
        val function = Function<VNum<*>>(
            "f", listOf(Variable("a", pos)), listOf(
                Return(
                    BinaryCalculation<VNum<*>, VNum<*>>(
                        IntLit(3, pos) as Exp<VNum<*>>, Variable("a", pos) as Exp<VNum<*>>, Sub
                    ), match = pos
                )
            )
        )
        val underTest =
            FunctionCall(function, listOf(FunctionCall(constant, listOf(), pos) as Exp<Value>), pos)
        var context =
            EvaluationContext(Env(listOf(function, constant)), Assign("a", underTest, pos))
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(5)
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5.bg, (context.env.parameters[0].value as VNum<BigInteger>).value)
        context.assertFinished()
    }

    @Test
    fun `test whether the operator returns the first operand call`() {
        val constant2 =
            Function<VNum<*>>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val constant3 =
            Function<VNum<*>>("y", listOf(), listOf(Return(IntLit(3, pos), match = pos)))
        val underTest2 = FunctionCall(constant2, listOf(), pos)
        val underTest3 = FunctionCall(constant3, listOf(), pos)
        val context = EvaluationContext(
            Env(listOf(constant2, constant3)),
            Assign("a", BinaryCalculation(underTest2, underTest3, Add), pos)
        )
        context.performStep(1)
        assertNotNull(context.functionOngoing)
        context.performStep(5)
        assertNull(context.functionOngoing)
        assertEquals(1, context.env.parameters.size)
        assertEquals(5.bg, (context.env.parameters[0].value as VNum<*>).value)
        context.assertFinished()
    }

    @Test
    fun `isolated function fails to access variable`() {
        val function = Function<VNum<*>>(
            "y", listOf(), listOf(
                Assign("x", Variable("a", pos), pos), Return(IntLit(3, pos), match = pos)
            )
        )
        val call = FunctionCall(function, listOf(), pos)
        val underTest = EvaluationContext(
            Env(listOf(function), values = listOf(Parameter("a", WHOLE, VWhole(3)))),
            EvalSequence(Assign("b", call, pos))
        )
        assertEquals(1, underTest.env.parameters.size)
        assertIs<VWhole>(underTest.env.parameters[0].value)
        underTest.performStep(1)
        assertNotNull(underTest.functionOngoing)
        assertNull(underTest.error)
        underTest.performStep(2)
        underTest.assertAborted()
        assertEquals(1, underTest.env.parameters.size)
    }

    @Test
    fun `isolated function shadows variable`() {
        val function = Function<VNum<*>>(
            "f", listOf(Variable("a", pos)), listOf(
                Assign("b", Variable("a", pos), pos),
                Assign("a", IntLit(2, pos), pos),
                Assign("c", StrLit("test", pos), pos),
                Return(
                    BinaryCalculation<VNum<*>, VNum<*>>(
                        Variable("a", pos) as Exp<VNum<*>>, Variable("b", pos) as Exp<VNum<*>>, Add
                    ),
                    pos
                )
            )
        )
        val call = FunctionCall(function, listOf(IntLit(5, pos)), pos)
        var underTest = EvaluationContext(
            Env(
                functions = listOf(function),
                values = listOf(
                    Parameter("a", WHOLE, VWhole(4)),
                    Parameter("b", WHOLE, VWhole(3)),
                )
            ),
            EvalSequence(
                Assign("x", call, pos)
            )
        )
        underTest.performStep(6)
        assertNull(underTest.head)
        assertNull(underTest.functionOngoing)
        assertEquals(3, underTest.env.parameters.size)
        val (a, b, x) = underTest.env.parameters
        assertIs<WHOLE>(a.type)
        assertIs<WHOLE>(b.type)
        assertIs<WHOLE>(x.type)
        assertEquals(4.toBigInteger(), assertIs<VWhole>(a.value).value)
        assertEquals(3.toBigInteger(), assertIs<VWhole>(b.value).value)
        assertEquals("x", x.id)
        assertEquals(7.toBigInteger(), assertIs<VWhole>(x.value).value)
        underTest.assertFinished()
    }
}
