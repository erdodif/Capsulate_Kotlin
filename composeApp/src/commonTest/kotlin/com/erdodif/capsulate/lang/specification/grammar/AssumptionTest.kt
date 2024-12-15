package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.specification.assertWithContext
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.context.GlobalEnvironment
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
    }

    @Test
    fun `assumption passes on s ∈ S`() = assertWithContext(context) {
        assumption pass "A : Set" withType Set
        assumption pass "B : Prop" withType Prop
        assumption pass "C : Type(1)" withType Type(1)
    }

    @Test
    fun `assumption fails s ∉ S`() = assertWithContext(context) {
        assumption fail "A : Nat"
        assumption fail "Nat : Prop"
        assumption fail "A : Type(0)"
    }

}