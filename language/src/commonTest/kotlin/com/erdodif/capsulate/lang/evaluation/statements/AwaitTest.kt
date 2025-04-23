package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.id
import com.erdodif.capsulate.lang.program.evaluation.AtomicEvaluation
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.SingleStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AwaitTest {
    val pos = MatchPos.ZERO

    @Test
    fun `await emits itself on failed condition`() {
        val underTest = Wait(
            BoolLit(false, pos),
            Atomic(ArrayDeque(listOf(Skip(3.id, pos))), 2.id, pos),
            1.id,
            pos
        )
        val result = underTest.evaluate(Environment.EMPTY)
        assertIs<SingleStatement>(result)
        val statement = result.next
        assertIs<Wait>(statement)
        assertEquals(1.id, statement.id)
        assertEquals(2.id, statement.atomic.id)
        assertEquals(3.id, statement.atomic.statements.first().id)
    }

    @Test
    fun `await emits the inner statements on passed condition`() {
        val underTest = Wait(
            BoolLit(true, pos),
            Atomic(ArrayDeque(listOf(Skip(3.id, pos), Abort(4.id, pos))), 2.id, pos),
            1.id,
            pos
        )
        val result = underTest.evaluate(Environment.EMPTY)
        assertIs<AtomicEvaluation>(result)
        assertEquals(2, result.statements.size)
        assertEquals(3.id, result.statements.first().id)
        assertEquals(4.id, result.statements.last().id)
    }
}