package com.erdodif.capsulate.lang.evaluation

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.NAT
import com.erdodif.capsulate.lang.program.grammar.expression.NEVER
import com.erdodif.capsulate.lang.program.grammar.expression.STRING
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.Value

object MockEnvironments {
    @KParcelize
    private object NeverValue : Value {
        override fun hashCode(): Int = 0
        override val type: Type
            get() = NEVER

        override fun equals(other: Any?): Boolean =
            error("Equals called the never parameter!")
    }

    fun neverEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("_", NEVER, NeverValue)
        )
    )

    fun intEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("a", NAT, VNat(1U)),
            Parameter("b", NAT, VNat(4U)),
            Parameter("c", NAT, VNat(7U))
        )
    )

    fun stringEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("a", STRING, VStr("text")),
        )
    )
}
