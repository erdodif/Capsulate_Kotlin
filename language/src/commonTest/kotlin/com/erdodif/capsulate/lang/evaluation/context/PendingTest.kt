@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.evaluation.context

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.FunctionState
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.expression.NEVER
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.Return
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

class PendingTest {
    @KParcelize
    private object TestValue : Value {
        override fun equals(other: Any?): Boolean = false
        override fun hashCode(): Int = 0
        override val type: Type
            get() = NEVER
    }

    @Test
    fun `single return statement with correct value`() {
        val function = Function<Value>(
            "",
            listOf(),
            listOf(Return(StrLit("value", MatchPos.ZERO), match = MatchPos.ZERO))
        )
        val exp = FunctionState(
            Environment.EMPTY, PendingExpression<Value, Value>(
                FunctionCall(function, listOf(), MatchPos.ZERO), function
            ) { Left(TestValue) })
        exp.step()
        assertNull(exp.context.error)
        assertNull(exp.context.functionOngoing)
        assertNull(exp.context.head)
        val returnValue = exp.context.returnValue
        assertNotNull(returnValue)
        assertIs<VStr>(returnValue)
        assertEquals("value", returnValue.value)
    }

    @Test
    fun `single abort statement propagates`() {
        val function = Function<Value>(
            "",
            listOf(),
            listOf(Abort(MatchPos.ZERO))
        )
        val exp = FunctionState(
            Environment.EMPTY, PendingExpression<Value, Value>(
                FunctionCall<Value>(function, listOf(), MatchPos.ZERO), function
            ) { Left(TestValue) })
        exp.step()
        assertNull(exp.context.functionOngoing)
        assertNull(exp.context.head)
        assertNull(exp.context.returnValue)
        assertNotNull(exp.context.error)
    }

    @Test
    fun `empty statementList aborts`() {
        val function = Function<Value>(
            "",
            listOf(),
            listOf()
        )
        val exp = FunctionState(
            Environment.EMPTY, PendingExpression<Value, Value>(
                FunctionCall<Value>(function, listOf(), MatchPos.ZERO), function
            ) { Left(TestValue) })
        exp.step()
        assertNull(exp.context.functionOngoing)
        assertNull(exp.context.head)
        assertNull(exp.context.returnValue)
        assertNotNull(exp.context.error)
    }
}
