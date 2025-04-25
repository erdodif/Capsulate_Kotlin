package com.erdodif.capsulate.lang.program.scenarios

import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.performStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FunctionTest {
    @Test
    fun `call in expression`() {
        val parserResult = assertIs<Pass<List<Statement>>>(
            parseProgram(
                "function x(y) {return y * 10}\n" +
                        "a := 1 + 2 + x(3) + x(4)"
            )
        )
        val underTest = EvaluationContext(
            Env(functions = parserResult.state.functions),
            EvalSequence(parserResult.value)
        )
        underTest.performStep(5)
        underTest.assertFinished()
        val value = underTest.env.parameters.first().value
        assertIs<VWhole>(value)
        assertEquals(73.bg, value.value)
    }
}
