package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.assertWithContext
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.context.GlobalEnvironment
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.pass
import kotlin.test.BeforeTest
import kotlin.test.Test

class AssumptionTest {
    var context: Context = GlobalEnvironment()
    val assumption: Parser<Sort> = {
        assumption(context)()
    }

    @BeforeTest
    fun setup() {
        context = GlobalEnvironment(Assumption("Nat", Set))
        context.add(context.assume("0", context["Nat"]!!))
    }

    @Test
    fun `assumption passes`() = assertWithContext({context}) {
        assumption pass "a : Nat"
        context.has("a", context["Nat"]!!)
        //assumption pass "a : Nat" withTypeLabel "Nat" TODO: AssertWithContext does not work (context did not update!)
    }

}