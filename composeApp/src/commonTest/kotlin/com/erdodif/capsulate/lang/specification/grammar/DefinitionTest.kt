package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Context
import com.erdodif.capsulate.lang.specification.coc.Definition
import com.erdodif.capsulate.lang.specification.coc.GlobalEnvironment
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.pass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefinitionTest {
    var env: Context = GlobalEnvironment(Assumption("Nat", Type(0)), Assumption("0", Type(0)))
    val definition = definition(env)

    @BeforeTest
    fun before(){
        env = GlobalEnvironment(Assumption("Nat", Type(0)), Assumption("0", Type(0)))
    }

    @Test
    fun `definition passes`() {
        definition pass "a := 0 : Nat"
        definition pass "b := a : Nat"
        assertEquals(env["a"]!!.type, Assumption("Nat",Type(0)))
        assertEquals((env["a"] as Definition).value, "0")
        assertEquals(env["b"]!!.type, Assumption("Nat",Type(0)))
        assertEquals((env["b"] as Definition).value, "a")
    }
}