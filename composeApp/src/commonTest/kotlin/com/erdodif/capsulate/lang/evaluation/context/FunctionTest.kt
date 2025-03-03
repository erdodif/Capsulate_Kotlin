package com.erdodif.capsulate.lang.evaluation.context

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.evaluation.Return
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Expression
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Add
import com.erdodif.capsulate.lang.program.grammar.expression.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
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
        val underTest = FunctionCall(function, listOf(), pos)
        var context =
            EvaluationContext(
                Env(
                    mapOf("x" to function.body.toTypedArray()),
                    mapOf(),
                    mutableListOf()
                ), Expression(underTest, pos)
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
        val underTest = FunctionCall(function, listOf(), pos)
        var context =
            EvaluationContext(
                Env(
                    mapOf("x" to function.body.toTypedArray()),
                    mapOf(),
                    mutableListOf()
                ), Assign("a", underTest, pos)
            )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(0, (context.env.parameters[0].value as VNum).value)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `niladic function in expression`() {
        val function =
            Function<VNum>("x", listOf(), listOf(Return(IntLit(2, pos), match = pos)))
        val underTest = FunctionCall(function, listOf(), pos)
        var context =
            EvaluationContext(
                Env(
                    mapOf("x" to function.body.toTypedArray()),
                    mapOf(),
                    mutableListOf()
                ), Assign("a", BinaryCalculation<VNum, VNum>(underTest, underTest, Add), pos)
            )
        context.step()
        assertNotNull(context.functionOngoing)
        context.step()
        context.step()
        assertNull(context.functionOngoing)
        assertEquals(4, (context.env.parameters[0].value as VNum).value)
    }
}