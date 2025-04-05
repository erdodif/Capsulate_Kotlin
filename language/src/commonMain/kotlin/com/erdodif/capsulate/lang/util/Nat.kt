package com.erdodif.capsulate.lang.util

sealed class Nat private constructor(){
    abstract val value: Int

    fun toInt(): Int = when(this) {
        is Zero -> 0
        is Suc<*> -> 1 + oneLess.value
    }

    data object Zero : Nat() {
        override val value = 0
    }

    data class Suc<out T : Nat>(val oneLess: T) : Nat() {
        override val value = 1 + oneLess.value
    }

    operator fun compareTo(other:Nat): Int = this.value - other.value

    override operator fun equals(other: Any?): Boolean = when(other){
        is Nat -> this.value == other.value
        is Number -> this.value == other.toInt()
        else -> false
    }

    override fun hashCode(): Int = value

    companion object Utils {
        fun fromInt(intValue: Int) : Nat{
            require(intValue > 0) { "Negatives aren't Natural ($intValue supplied)" }
            return fromUInt(intValue.toUInt())
        }

        fun fromUInt(uIntValue: UInt): Nat = when (uIntValue) {
            0u -> Zero
            else -> Suc(fromUInt(uIntValue - 1u))
        }
    }
}
