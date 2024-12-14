package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.context.GlobalEnvironment
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.pass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefinitionTest {
    var context: Context = GlobalEnvironment()
    val definition: Parser<Sort> = {
        definition(context)()
    }

    @BeforeTest
    fun setup(){
        context = GlobalEnvironment(Assumption("Nat", Type(0)))
        context.add(context.assume("0", context["Nat"]!!))
    }


    @Test
    fun `definition passes`() {
        definition pass "a := 0 : Nat"
        definition pass "b := a : Nat"
        assertEquals(context["a"]!!.type, Assumption("Nat",Type(0)))
        //assertEquals((env["a"] as Definition).value, )
        assertEquals(context["b"]!!.type, Assumption("Nat",Type(0)))
        //assertEquals((env["b"] as Definition).value, "a")
    }
}