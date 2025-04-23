package com.erdodif.capsulate.lang.evaluation.statements

import com.erdodif.capsulate.id
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.program.grammar.expression.BOOL
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class DoWhileTest {
    val pos = MatchPos.ZERO

    /**
     * A Boolean expression that must not be evaluated
     */
    private object NeverCondition : Exp<VBool> {
        override fun getType(assumptions: Map<String, Type>) = BOOL
        override fun toString(state: ParserState, parentStrength: Int) = ""
        override fun evaluate(context: Environment) =
            throw AssertionError("Condition's value has been evaluated!")
    }

    @Test
    fun `do while unwraps with modified self regardless of condition`() {
        val underTest = DoWhile(
            NeverCondition,
            listOf(Skip(2.id, pos), Abort(3.id, pos), Skip(4.id, pos)),
            1.id, MatchPos(1, 2)
        )
        val result = underTest.evaluate(Environment.EMPTY)
        assertIs<EvalSequence>(result)
        assertEquals(4, result.statements.size)
        assertIs<Skip>(result.statements[0])
        assertIs<Abort>(result.statements[1])
        assertIs<Skip>(result.statements[2])
        val self = assertIs<While>(result.statements[3])
        assertEquals(2.id, result.statements[0].id)
        assertEquals(3.id, result.statements[1].id)
        assertEquals(4.id, result.statements[2].id)
        assertEquals(underTest.condition, self.condition)
        assertEquals(underTest.id, self.id)
        assertEquals(underTest.statements, self.statements)
        assertContentEquals(underTest.statements, self.statements)
        assertEquals(MatchPos.ZERO, self.match)
    }
}
