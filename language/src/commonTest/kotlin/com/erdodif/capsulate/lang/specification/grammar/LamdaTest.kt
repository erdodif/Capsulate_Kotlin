package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.context.GlobalEnvironment
import com.erdodif.capsulate.lang.util.Parser
import kotlin.test.BeforeTest
import kotlin.test.Test

class LamdaTest {
    var context: Context = GlobalEnvironment()
    val lambda: Parser<Sort>
        get() = context.lambda()

    @BeforeTest
    fun setup(){
        context = GlobalEnvironment(
            Assumption("Nat", Type(1)),
            Assumption("0", Assumption("Nat", Type(1)))
        )
    }

    @Test
    fun `lambda should pass`(){
        ""
    }
}
