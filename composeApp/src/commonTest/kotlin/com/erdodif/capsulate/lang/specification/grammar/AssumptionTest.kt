package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.GlobalEnvironment
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withValue
import kotlin.test.Test

class AssumptionTest {
    val assumption = assumption(GlobalEnvironment(Assumption("Nat", Type(1))))

    @Test
    fun `assumption passes`(){
        assumption pass "a : Nat" withValue Assumption("a", Type(0))
    }
}