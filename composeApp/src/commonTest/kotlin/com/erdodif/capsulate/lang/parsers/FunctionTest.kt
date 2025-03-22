package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.sFunction
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.matches
import com.erdodif.capsulate.pass
import kotlin.test.Test

class FunctionTest {


    @Test
    fun `function parses empty niladic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.isEmpty()
        }
        topLevel(sFunction) pass "function xy(){}\n" matches condition at 16
        topLevel(sFunction) pass "function xy(){\n}" matches condition at 16
        topLevel(sFunction) pass "function xy()\n{}" matches condition at 16
        topLevel(sFunction) pass "function xy()\n{\n}" matches condition at 17
    }

    @Test
    fun `function parses empty monodic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.count() == 1 && it.value.parameters[0].id == "a"
        }
        topLevel(sFunction) pass "function xy(a){}\n" matches condition at 17
        topLevel(sFunction) pass "function xy(a){\n}" matches condition at 17
        topLevel(sFunction) pass "function xy(a)\n{}" matches condition at 17
        topLevel(sFunction) pass "function xy(a)\n{\n}" matches condition at 18
    }

    @Test
    fun `function parses empty dyadic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.count() == 2 &&
                    it.value.parameters[0].id == "a" && it.value.parameters[1].id == "b"
        }
        topLevel(sFunction) pass "function xy(a,b){}\n" matches condition at 19
        topLevel(sFunction) pass "function xy(a ,b){\n}" matches condition at 20
        topLevel(sFunction) pass "function xy(a, b)\n{}" matches condition at 20
        topLevel(sFunction) pass "function xy( a , b )\n{\n}" matches condition at 24
    }
}
