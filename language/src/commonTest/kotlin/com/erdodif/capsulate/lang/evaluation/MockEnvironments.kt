package com.erdodif.capsulate.lang.evaluation

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.Value

object MockEnvironments {
    @KParcelize
    private object NeverValue : Value {
        override fun hashCode(): Int = 0
        override fun equals(other: Any?): Boolean =
            error("Equals called the never parameter!")
    }

    fun neverEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("_", Type.NEVER, NeverValue)
        )
    )

    fun intEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("a", Type.NAT, VNat(1U)),
            Parameter("b", Type.NAT, VNat(4U)),
            Parameter("c", Type.NAT, VNat(7U))
        )
    )

    fun stringEnv(): Environment = Env(
        mapOf(), mapOf(), mutableListOf(
            Parameter("a", Type.STRING, VStr("text")),
        )
    )
}
