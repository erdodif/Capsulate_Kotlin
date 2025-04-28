package com.erdodif.capsulate.parcelable

import android.os.Bundle
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationContext
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.evaluation.PendingFunctionEvaluation
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.ParallelAssign
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.Index
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.Return
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.bg
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class StatementParcelTest {

    val pos = MatchPos.ZERO

    @Test
    fun parallel_assign_in_bundle() {
        val underTest = ParallelAssign(
            listOf(
                Index("a") to IntLit(2, pos),
                Index("b") to IntLit(3, pos),
            ), pos
        )
        val bundle = Bundle()
        bundle.putParcelable("underTest", underTest)
        val result = bundle.getParcelable("underTest", underTest::class.java)
        assertIs<ParallelAssign>(result)
        assertEquals(2, result.assigns.size)
        val (a, b) = result.assigns
        assertIs<IntLit>(a.second)
        assertIs<IntLit>(b.second)
        assertEquals("a", a.first.id)
        assertEquals("b", b.first.id)
        assertTrue(a.first.indexers.isEmpty())
        assertTrue(b.first.indexers.isEmpty())
    }

    @Test
    fun index_to_be_Parcelable_in_bundle() {
        val underTest = Index("a", IntLit(1.bg, pos), IntLit(2.bg, pos))
        val bundle = Bundle()
        bundle.putParcelable("underTest", underTest)
        val result = bundle.getParcelable("underTest", Index::class.java)
        assertIs<Index>(result)
        assertEquals("a", result.id)
        assertEquals(2, result.indexers.size)
        val (first, second) = result.indexers
        assertIs<IntLit>(first)
        assertIs<IntLit>(second)
        assertEquals(1.bg, first.value)
        assertEquals(2.bg, second.value)
    }

    @Test
    fun pending_function_to_bundle() {
        val underTest = PendingFunctionEvaluation(
            PendingExpression(
                FunctionCall(
                    "fun", listOf(),
                    MatchPos.ZERO
                ),
                Function("fun", listOf(), listOf(Return(IntLit(1, MatchPos.ZERO), MatchPos.ZERO))),
            ) {
                Left(it)
            }
        ) {
            Finished
        }
        val bundle = Bundle()
        bundle.putParcelable("underTest", underTest)
        val result = bundle.getParcelable("underTest", PendingFunctionEvaluation::class.java)
        assertIs<PendingFunctionEvaluation<*>>(result)
        val actual = result.expression.onValue(Env(), VWhole(1))
        assertEquals(1.bg, assertIs<Left<VWhole>>(actual).value.value)
    }

    @Test
    fun eval_context_to_bundle() {
        val parserResult =
            assertIs<Pass<List<Statement>>>(parseProgram("function f() return 0\na := f()"))
        val underTest = EvaluationContext(
            Env(functions = parserResult.state.functions),
            EvalSequence(parserResult.value)
        )
        //-- Bundle Initial State
        val bundle1 = Bundle()
        bundle1.putParcelable("underTest", underTest)
        val result1 = bundle1.getParcelable("underTest", EvaluationContext::class.java)
        assertIs<EvaluationContext>(result1)
        //-- Bundle on first step's State
        result1.step()
        val bundle2 = Bundle()
        bundle2.putParcelable("underTest", underTest)
        val result2 = bundle2.getParcelable("underTest", EvaluationContext::class.java)
        assertIs<EvaluationContext>(result2)
        //-- Bundle on pending function evaluation State
        result2.step()
        val bundle3 = Bundle()
        bundle3.putParcelable("underTest", underTest)
        val result3 = bundle3.getParcelable("underTest", EvaluationContext::class.java)
        assertIs<EvaluationContext>(result3)
    }
}