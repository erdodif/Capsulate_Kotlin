package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.utils.id
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.ParallelEvaluation
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.utils.EMPTY_ENVIRONMENT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
class ParallelTest {
    val pos = MatchPos.ZERO


    @Test
    fun `unobtainable empty block returns correctly`() {
        val underTest = Parallel(listOf(), 1.id, pos)
        val result = underTest.evaluate(EMPTY_ENVIRONMENT)
        assertIs<ParallelEvaluation>(result)
        assertEquals(0, result.entries.size)
    }

    @Test
    fun `single block parallel returns correctly`() {
        val underTest = Parallel(listOf(listOf(Skip(2.id, pos), Skip(3.id, pos))), 1.id, pos)
        val result = underTest.evaluate(EMPTY_ENVIRONMENT)
        assertIs<ParallelEvaluation>(result)
        assertEquals(1, result.entries.size)
        val sequence = result.entries.first()
        assertIs<EvalSequence>(sequence)
        assertEquals(2, sequence.statements.size)
        assertEquals(2.id, sequence.statements.first().id)
        assertEquals(3.id, sequence.statements.last().id)
    }

    @Test
    fun `multiple block parallel returns correctly`() {
        val underTest = Parallel(
            listOf(
                listOf(Skip(2.id, pos), Skip(3.id, pos)),
                listOf(Skip(4.id, pos)),
                listOf(Skip(5.id, pos), Skip(6.id, pos), Skip(7.id, pos))
            ), 1.id, pos
        )
        val result = underTest.evaluate(EMPTY_ENVIRONMENT)
        assertIs<ParallelEvaluation>(result)
        assertEquals(3, result.entries.size)
        val (sequence1, sequence2, sequence3) = result.entries
        assertIs<EvalSequence>(sequence1)
        assertEquals(2, sequence1.statements.size)
        assertEquals(2.id, sequence1.statements.first().id)
        assertEquals(3.id, sequence1.statements.last().id)
        assertIs<Skip>(sequence2)
        assertEquals(4.id, sequence2.id)
        assertIs<EvalSequence>(sequence3)
        assertEquals(3, sequence3.statements.size)
        assertEquals(5.id, sequence3.statements[0].id)
        assertEquals(6.id, sequence3.statements[1].id)
        assertEquals(7.id, sequence3.statements[2].id)
    }
}
