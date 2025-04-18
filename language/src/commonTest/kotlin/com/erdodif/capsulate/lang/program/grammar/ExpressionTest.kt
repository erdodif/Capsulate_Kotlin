package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.expression.pBoolLit
import com.erdodif.capsulate.lang.program.grammar.expression.pIntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pStrLit
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.match
import com.erdodif.capsulate.pass
import kotlin.test.Test

class ExpressionTest {

    @Test
    fun boolLit_pass() {
        topLevel(pBoolLit) pass "true" match { it.value }
        topLevel(pBoolLit) pass "false" match { !it.value }
    }

    @Test
    fun intLit_pass() {
        topLevel(pIntLit) pass "1234" match { it.value == 1234.bg }
        topLevel(pIntLit) pass "-1234" match { it.value == (-1234).bg }
    }

    @Test
    fun intLit_fail() {
        topLevel(pIntLit) fail "12 34"
        topLevel(pIntLit) fail "- 1234"
    }

    @Test
    fun variable_pass() {
        topLevel(pVariable) pass "var "
        topLevel(pVariable) pass "var2 "
    }

    @Test
    fun variable_fail() {
        topLevel(pVariable) fail "2var "
        topLevel(pVariable) fail "var var"
    }

    @Test
    fun strLit_pass() {
        topLevel(pStrLit) pass "\"asd\" " match { it.value == "asd" }
        topLevel(pStrLit) pass "\"as df\" " match { it.value == "as df" }
        topLevel(pStrLit) pass "\"a\\\\sd\" " match { it.value == "a\\sd" }
        topLevel(pStrLit) pass "\"a\\\"sd\" " match { it.value == "a\"sd" }
    }

    @Test
    fun strLit_fail() {
        topLevel(pStrLit) fail "\"asd "
        topLevel(pStrLit) fail "asd\" "
        topLevel(pStrLit) fail "asd "
        topLevel(pStrLit) fail "\"a\"sd\""
        topLevel(pStrLit) fail "\"asd\"\""
    }

}
