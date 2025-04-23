package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.id
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.util.MatchPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class WhileTest {
    val pos = MatchPos.ZERO

    @Test
    fun `while finishes on failed condition`() {
        val underTest = While(BoolLit(false, pos), listOf(Skip(2.id, pos)), 1.id, pos)
        val result = underTest.evaluate(Environment.EMPTY)
        assertIs<Finished>(result)
    }

    @Test
    fun `while repeats on condition`() {
        val underTest = While(
            BoolLit(true, pos),
            listOf(Skip(2.id, pos), Abort(3.id, pos), Skip(4.id, pos)),
            1.id, pos
        )
        val result = underTest.evaluate(Environment.EMPTY)
        assertIs<EvalSequence>(result)
        assertEquals(4, result.statements.size)
        assertIs<Skip>(result.statements[0])
        assertIs<Abort>(result.statements[1])
        assertIs<Skip>(result.statements[2])
        assertIs<While>(result.statements[3])
        assertEquals(2.id, result.statements[0].id)
        assertEquals(3.id, result.statements[1].id)
        assertEquals(4.id, result.statements[2].id)
        assertEquals(underTest, result.statements[3])
    }
}
