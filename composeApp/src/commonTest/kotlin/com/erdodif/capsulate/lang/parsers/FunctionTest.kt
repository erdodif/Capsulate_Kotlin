package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
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
    fun `function fails empty niladic function`() {
        topLevel(sFunction) fail "function xy(){}\n" at 13
        topLevel(sFunction) fail "function xy(){\n}" at 13
        topLevel(sFunction) fail "function xy()\n{}" at 14
        topLevel(sFunction) fail "function xy()\n{\n}" at 14
    }

    @Test
    fun `function passes single return and abort niladic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.isEmpty() && it.value.body.count() == 1
        }
        topLevel(sFunction) pass "function xy() return 0\n" matches condition at 23
        topLevel(sFunction) pass "function xy() return \"\"" matches condition at 23
        topLevel(sFunction) pass "function xy() return 1 + 2" matches condition at 26
        topLevel(sFunction) pass "function xy() abort" matches condition at 19
    }

    @Test
    fun `function passes niladic block function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.isEmpty() && it.value.body.isNotEmpty()
        }
        topLevel(sFunction) pass "function xy() { return 0 }\n" matches condition at 27
        topLevel(sFunction) pass "function xy() {skip;return \"\"}" matches condition at 30
        topLevel(sFunction) pass "function xy() {skip\nskip;skip}" matches condition at 30
        topLevel(sFunction) pass "function xy() {abort}" matches condition at 21
    }

    @Test
    fun `function fails empty monodic function`() {
        topLevel(sFunction) fail "function xy(a){}\n" at 14
        topLevel(sFunction) fail "function xy(a){\n}" at 14
        topLevel(sFunction) fail "function xy(a)\n{}" at 15
        topLevel(sFunction) fail "function xy(a)\n{\n}" at 15
    }

    @Test
    fun `function parses single return and single abort monodic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.count() == 1 && it.value.parameters[0].id == "a"
        }
        topLevel(sFunction) pass "function xy(a) return a\n" matches condition at 24
        topLevel(sFunction) pass "function xy(a) return 0" matches condition at 23
        topLevel(sFunction) pass "function xy(a) return \"\"" matches condition at 24
        topLevel(sFunction) pass "function xy(a) abort" matches condition at 20
    }

    @Test
    fun `function fails empty dyadic function`() {
        topLevel(sFunction) fail "function xy(a,b){}\n" at 16
        topLevel(sFunction) fail "function xy(a ,b){\n}" at 17
        topLevel(sFunction) fail "function xy(a, b)\n{}" at 18
        topLevel(sFunction) fail "function xy( a , b )\n{\n}" at 21
    }

    @Test
    fun `function passes single return and abort dyadic function`() {
        val condition: (Pass<Function<Value>>) -> Boolean = {
            it.value.name == "xy" && it.value.parameters.count() == 2 &&
                    it.value.parameters[0].id == "a" && it.value.parameters[1].id == "b"
        }
        topLevel(sFunction) pass "function xy(a,b) return 0\n" matches condition at 26
        topLevel(sFunction) pass "function xy(a ,b ) return a + b" matches condition at 31
        topLevel(sFunction) pass "function xy( a, b) return a" matches condition at 27
        topLevel(sFunction) pass "function xy( a,b ) return b" matches condition at 27
        topLevel(sFunction) pass "function xy(a,b) abort" matches condition at 22
        topLevel(sFunction) pass "function xy( a , b ) abort" matches condition at 26
    }
}
