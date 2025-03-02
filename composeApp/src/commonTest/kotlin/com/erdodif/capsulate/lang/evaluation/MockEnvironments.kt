package com.erdodif.capsulate.lang.evaluation

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.Parameter
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VStr
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.util.MatchPos

class MockEnvironments {
    companion object {
        @KParcelize
        private object NeverValue : Value {
            override fun equals(other: Any?): Boolean =
                throw IllegalStateException("Equals called the never parameter!")
        }

        fun emptyEnv(): Env = Env.empty
        fun neverEnv(): Env = Env(
            mapOf(), mapOf(), mutableListOf(
                Parameter("_", Type.NEVER, NeverValue)
            )
        )

        fun intEnv(): Env = Env(
            mapOf(), mapOf(), mutableListOf(
                Parameter("a", Type.NAT, VNat(1U)),
                Parameter("b", Type.NAT, VNat(4U)),
                Parameter("c", Type.NAT, VNat(7U))
            )
        )

        fun stringEnv(): Env = Env(
            mapOf(), mapOf(), mutableListOf(
                Parameter("a", Type.STRING, VStr("text")),
            )
        )
    }
}