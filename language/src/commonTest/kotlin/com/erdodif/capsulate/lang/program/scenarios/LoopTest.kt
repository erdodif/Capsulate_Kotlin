package com.erdodif.capsulate.lang.program.scenarios

import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.utils.EMPTY_ENVIRONMENT
import com.erdodif.capsulate.performStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoopTest {

    @Test
    fun `long while loop`() {
        val code = "a := 0\nwhile(a < 1000000) { a := a + 1}"
        val result = assertIs<Pass<List<Statement>>>(parseProgram(code))
        val underTest1 = EvaluationContext(
            EMPTY_ENVIRONMENT,
            EvalSequence(result.value)
        )
        underTest1.performStep(2_000_002)
        val value = underTest1.env.parameters.first().value
        assertIs<VWhole>(value)
        assertEquals(1_000_000.bg, value.value)
        underTest1.assertFinished()
    }

    @Test
    fun `long do while loop`() {
        val code = "a := 0\ndo { a := a + 1} while(a < 1000000)"
        val result = assertIs<Pass<List<Statement>>>(parseProgram(code))
        val underTest1 = EvaluationContext(
            EMPTY_ENVIRONMENT,
            EvalSequence(result.value)
        )
        underTest1.performStep(2_000_002)
        val value = underTest1.env.parameters.first().value
        assertIs<VWhole>(value)
        assertEquals(1_000_000.bg, value.value)
        underTest1.assertFinished()
    }
}
