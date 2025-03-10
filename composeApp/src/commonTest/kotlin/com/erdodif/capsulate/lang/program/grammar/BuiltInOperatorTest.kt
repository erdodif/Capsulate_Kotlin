package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Not
import com.erdodif.capsulate.lang.program.grammar.expression.operator.UnaryCalculation
import com.erdodif.capsulate.lang.program.grammar.expression.operator.builtInOperatorTable
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.matches
import com.erdodif.capsulate.pass
import kotlin.test.Test
import kotlin.test.assertIs

class BuiltInOperatorTest {

    @Test
    fun `addition with subtraction`(){
        builtInOperatorTable.parser() pass "a + b"
    }

    @Test
    fun `negation single`(){
        builtInOperatorTable.parser() pass "!a" matches {
            assertIs<UnaryCalculation<*,*>>(it.value)
            assertIs<Not>(it.value.operator)
            assertIs<Variable>(it.value.param)
            it.value.param.id == "a"
        }
    }
}