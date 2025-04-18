package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

sealed class RawValue<out T : Value>(override val match: MatchPos) : Exp<T>, Token(match) {
    abstract fun get(context: Environment): T
    final override fun evaluate(context: Environment): Either<T, PendingExpression<Value, T>> =
        Left(get(context))

    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
}

@KParcelize
data class ChrLit(val value: Char, override val match: MatchPos) : RawValue<VChr>(match) {
    override fun getType(assumptions: Map<String, Type>): CHAR = CHAR
    override fun get(context: Environment): VChr = VChr(value)
    override fun toString(): String = "ChrLit:$value"
}

@KParcelize
data class StrLit(val value: String, override val match: MatchPos) : RawValue<VStr>(match) {
    override fun getType(assumptions: Map<String, Type>): STRING = STRING
    override fun get(context: Environment): VStr = VStr(value)
    override fun toString(): String = "StrLit:$value"
}

@KParcelize
data class IntLit(
    val value: @com.erdodif.capsulate.RawValue BigInteger,
    override val match: MatchPos
) : RawValue<VWhole>(match) {
    constructor(value: Int, match: MatchPos) : this(value.toBigInteger(), match)

    override fun getType(assumptions: Map<String, Type>): WHOLE = WHOLE
    override fun get(context: Environment): VWhole = VWhole(value)
    override fun toString(): String = "IntLit:$value"
}

@KParcelize
data class NatLit(
    val value: @com.erdodif.capsulate.RawValue BigInteger,
    override val match: MatchPos
) : RawValue<VNat>(match) {
    constructor(value: Int, match: MatchPos) : this(value.toBigInteger(), match)

    override fun getType(assumptions: Map<String, Type>): NAT = NAT
    override fun get(context: Environment): VNat = VNat(value)
    override fun toString(): String = "NatLit:$value"
}

@KParcelize
data class BoolLit(val value: Boolean, override val match: MatchPos) : RawValue<VBool>(match) {
    override fun getType(assumptions: Map<String, Type>): BOOL = BOOL
    override fun get(context: Environment): VBool = VBool(value)
    override fun toString(): String = "BoolLit:$value"
}

@KParcelize
data class ArrayLit<T : Value>(val value: Array<Exp<T>>, val match: MatchPos) : Exp<VArray<T>> {
    override fun getType(assumptions: Map<String, Type>): ARRAY =
        ARRAY(value.first().getType(assumptions), value.size)

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: Environment): Either<VArray<T>, PendingExpression<Value, VArray<T>>> =
        value.toList().withValue(context) { a: List<T> ->
            Left(VArray<Value>(a.toTypedArray()) as VArray<T>)
        }

    override fun toString(
        state: ParserState,
        parentStrength: Int
    ): String = value.joinToString(prefix = "[", postfix = "]")
    { it.toString(state, parentStrength) }

    override fun toString(): String = "ArrayLit: [${value.joinToString()}]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ArrayLit<*>
        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int = value.contentHashCode()
}

@KParcelize
data class Variable(val id: String, override val match: MatchPos) : RawValue<Value>(match) {
    override fun getType(assumptions: Map<String, Type>): Type = assumptions[this.id] ?: NEVER
    override fun get(context: Environment): Value {
        val param = context.get(id)
        if (param is Left) {
            return param.value
        } else {
            error("Variable '$id' is not defined!")
        }
    }

    override fun toString(): String = "Variable:$id"
}
