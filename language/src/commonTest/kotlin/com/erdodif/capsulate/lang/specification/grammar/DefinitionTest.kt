package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.assertWithContext
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.context.GlobalEnvironment
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.pass
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefinitionTest {
    var context: Context = GlobalEnvironment()
    val definition: Parser<Sort>
        get() = context.definition()


    @BeforeTest
    fun setup() {
        context = GlobalEnvironment(
            Assumption("Nat", Type(1)),
            Assumption("0", Assumption("Nat", Type(1)))
        )
    }

    @Test
    fun `definition passes`() = assertWithContext(context) {
        definition pass "a := 0 : Nat" withTypeLabel "Nat"
        definition pass "b := a : Nat" withTypeLabel "Nat"
    }
}
