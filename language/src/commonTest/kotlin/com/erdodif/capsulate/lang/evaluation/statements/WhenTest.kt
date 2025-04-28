package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.utils.id
import com.erdodif.capsulate.lang.program.evaluation.AbortEvaluation
import com.erdodif.capsulate.lang.program.evaluation.SingleStatement
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.When
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.utils.EMPTY_ENVIRONMENT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class WhenTest {
    val pos = MatchPos.ZERO

    @Test
    fun `when aborts on missing condition`() {
        val underTest = When(mutableListOf(), null, 1.id, pos)
        val result = underTest.evaluate(EMPTY_ENVIRONMENT)
        assertIs<AbortEvaluation>(result)
    }

    @Test
    fun `when aborts on the only but failed condition`() {
        val underTest = When(
            mutableListOf(BoolLit(false, pos) to listOf(Skip(2.id, pos))),
            null, 1.id, pos
        )
        val result = underTest.evaluate(EMPTY_ENVIRONMENT)
        assertIs<AbortEvaluation>(result)
    }

    @Test
    fun `when truncates itself on failed condition`() {
        val underTest = When(
            mutableListOf(
                BoolLit(false, pos) to listOf(Skip(2.id, pos)),
                BoolLit(false, pos) to listOf(Skip(3.id, pos)),
            ),
            null, 1.id, pos
        )
        val result = assertIs<SingleStatement>(underTest.evaluate(EMPTY_ENVIRONMENT)).next
        assertIs<When>(result)
        assertEquals(1, result.blocks.size)
        assertEquals(1.id, result.id)
    }
}
