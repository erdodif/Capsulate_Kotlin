package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState

sealed class RawValue<T : Value>(override val match: MatchPos) : Exp<T>, Token(match) {
    abstract fun get(context: Env): T
    final override fun evaluate(context: Env): Either<T, PendingExpression<Value, T>> =
        Left(get(context))
}

@KParcelize
class StrLit(val value: String, override val match: MatchPos) : RawValue<VStr>(match) {
    override fun get(context: Env): VStr = VStr(value)
    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
    override fun toString(): String = "StrLit:$value"
}

@KParcelize
class IntLit(val value: Int, override val match: MatchPos) : RawValue<VWhole>(match) {
    override fun get(context: Env): VWhole = VWhole(value)
    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
    override fun toString(): String = "IntLit:$value"
}

@KParcelize
class NatLit(val value: UInt, override val match: MatchPos) : RawValue<VNat>(match) {
    override fun get(context: Env): VNat = VNat(value)
    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
    override fun toString(): String = "NatLit:$value"
}

@KParcelize
class BoolLit(val value: Boolean, override val match: MatchPos) : RawValue<VBool>(match) {
    override fun get(context: Env): VBool = VBool(value)
    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
    override fun toString(): String = "BoolLit:$value"
}

@KParcelize
class Variable(val id: String, override val match: MatchPos) : RawValue<Value>(match) {
    override fun get(context: Env): Value {
        val param = context.get(id)
        if (param is Left) {
            return param.value.value
        } else {
            throw RuntimeException("Variable '$id' is not defined!")
        }
    }

    override fun toString(state: ParserState, parentStrength: Int): String = state[match]
    override fun toString(): String = "Variable:$id"
}