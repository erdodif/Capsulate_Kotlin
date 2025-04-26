package com.erdodif.capsulate.lang.program.scenarios

import com.erdodif.capsulate.assertAborted
import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.performStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

const val maxSteps = 1000

class ParallelTest {
    @Test
    fun `parallel with await`() {
        val parserResult =
            parseProgram("a := 3;{await a = 2 {a := 1}}|{await a = 1 {b := a}}|{a := 2}")
        assertIs<Pass<List<Statement>>>(parserResult)
        val underTest = EvaluationContext(Env(), EvalSequence(parserResult.value))
        var tries = 0
        while (underTest.head != null && tries < maxSteps) {
            underTest.performStep(1)
            tries++
        }
        underTest.assertFinished()
        val value = underTest.env.get("b")
        assertIs<Left<VWhole>>(value)
        assertEquals(1.bg, value.value.value)
    }

    @Test
    fun `parallel with deadlock`() {
        val parserResult =
            parseProgram("a := 3;{await a = 2 {a := 1}}|{await a = 1 {b := a}}|{await a = 1 {a := 2}}")
        assertIs<Pass<List<Statement>>>(parserResult)
        val underTest = EvaluationContext(Env(), EvalSequence(parserResult.value))
        var tries = 0
        while (underTest.head != null && tries < maxSteps) {
            underTest.performStep(1)
            tries++
        }
        underTest.assertAborted()
    }
}
