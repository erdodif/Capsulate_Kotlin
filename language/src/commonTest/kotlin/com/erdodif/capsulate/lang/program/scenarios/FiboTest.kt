package com.erdodif.capsulate.lang.program.scenarios

import com.erdodif.capsulate.assertFinished
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.sFunction
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.performStep
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FiboTest {

    val fiboSlow = """function fibo(a) {
        |  if a ≤ 2{
        |    return 1
        |  } else {
        |    x := fibo(a - 1)
        |    y := fibo(a - 2)
        |    return x + y
        |  }
        |}""".trimMargin()

    val fiboFast = """function fib(x, y, c) {
        |  if c = 0{ return x } else { return fib(y, x + y, c - 1) }
        |}
        |function fibo(n) {
        |  return fib(0, 1, n)
        |}""".trimMargin()

    @Test
    fun `try fibonacci 1`() {
        val result =
            assertIs<Pass<List<Statement>>>(parseProgram("$fiboSlow\nprogram Main\na := fibo(1)"))
        val startEnv =
            Env(functions = listOf(assertIs<Pass<Function<Value>>>(sFunction(ParserState(fiboSlow))).value))
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(4)
        underTest1.assertFinished()
    }

    @Test
    fun `try fibonacci 2`() {
        val result =
            assertIs<Pass<List<Statement>>>(parseProgram("$fiboSlow\nprogram Main\na := fibo(2)"))
        val startEnv =
            Env(functions = listOf(assertIs<Pass<Function<Value>>>(sFunction(ParserState(fiboSlow))).value))
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(4)
        underTest1.assertFinished()
    }

    @Test
    fun `try fibonacci 3`() {
        val result =
            assertIs<Pass<List<Statement>>>(parseProgram("$fiboSlow\nprogram Main\na := fibo(3)"))
        val startEnv =
            Env(functions = listOf(assertIs<Pass<Function<Value>>>(sFunction(ParserState(fiboSlow))).value))
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(12)
        underTest1.assertFinished()
    }

    @Test
    fun `try fibonacci 10`() {
        val result =
            assertIs<Pass<List<Statement>>>(parseProgram("$fiboSlow\nprogram Main\na := fibo(10)"))
        val startEnv =
            Env(functions = listOf(assertIs<Pass<Function<Value>>>(sFunction(ParserState(fiboSlow))).value))
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(436)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(55.bg, a.value)
        underTest1.assertFinished()
    }

    @Test
    fun `try fibonacci 20`() {
        val result =
            assertIs<Pass<List<Statement>>>(parseProgram("$fiboSlow\nprogram Main\na := fibo(20)"))
        val startEnv =
            Env(functions = listOf(assertIs<Pass<Function<Value>>>(sFunction(ParserState(fiboSlow))).value))
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(54116)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(6765.bg, a.value)
        underTest1.assertFinished()
    }

    @Test
    fun `try fast fibonacci 10`() {
        val result = assertPass(parseProgram("$fiboFast\nprogram Main\na := fibo(10)"))
        val startEnv = Env(functions = result.state.functions)
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(36)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(55.bg, a.value)
        underTest1.assertFinished()
    }

    @Test
    fun `try fast fibonacci 20`() {
        val result = assertPass(parseProgram("$fiboFast\nprogram Main\na := fibo(20)"))
        val startEnv = Env(functions = result.state.functions)
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(66)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(6765.bg, a.value)
        underTest1.assertFinished()
    }

    @Test
    fun `try fast fibonacci 300`() {
        val result = assertPass(parseProgram("$fiboFast\nprogram Main\na := fibo(300)"))
        val startEnv = Env(functions = result.state.functions)
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(906)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(
            "222232244629420445529739893461909967206666939096499764990979600".toBigInteger(),
            a.value
        )
        underTest1.assertFinished()
    }

    @Test
    fun `try fast fibonacci 1000`() {
        val result = assertPass(parseProgram("$fiboFast\nprogram Main\na := fibo(1000)"))
        val startEnv = Env(functions = result.state.functions)
        val underTest1 = EvaluationContext(
            startEnv,
            EvalSequence(result.value)
        )
        underTest1.performStep(3006)
        val a = underTest1.env.parameters.first().value
        assertIs<VWhole>(a)
        assertEquals(
            ("434665576869374564356885276750406258025646605173717804024" +
                    "8172908953655541794905189040387984007925516929592" +
                    "2593080322634775209689623239873322471161642996440" +
                    "906533187938298969649928516003704476137795166849228875").toBigInteger(),
            a.value
        )
        underTest1.assertFinished()
    }
}
