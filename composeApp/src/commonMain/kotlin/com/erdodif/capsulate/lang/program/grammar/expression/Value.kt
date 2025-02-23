package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import kotlin.jvm.JvmInline

interface Value: KParcelable {
    override operator fun equals(other: Any?): Boolean
}

interface VNum : Value {
    val value: Int
}

@KParcelize
@JvmInline
value class VNat(private val _value: UInt) : VNum {   // ℕ
    override val value: Int
        get() = _value.toInt()
}

@KParcelize
@JvmInline
value class VWhole(override val value: Int) : VNum   // ZZ

@KParcelize
@JvmInline
value class VStr(val value: String) : Value   // 𝕊

@KParcelize
@JvmInline
value class VBool(val value: Boolean) : Value

@KParcelize
@JvmInline
value class VCharacter(val value: Char) : Value   // ℂ
enum class Type {
    NAT,
    WHOLE,
    STRING,
    BOOL,
    CHAR,
    FILE,
    ARRAY,
    STREAM,
    TUPLE,
    SET,
    NEVER,
}

fun Value.type(): Type = when (this) {
    is VNat -> Type.NAT
    is VWhole -> Type.WHOLE
    is VStr -> Type.STRING
    is VBool -> Type.BOOL
    is VCharacter -> Type.CHAR
    /*is VFile -> Type.FILE
    is VArray -> Type.ARRAY
    is VStream -> Type.STREAM
    is VTuple -> Type.TUPLE
    is VSet -> Type.SET*/
    else -> Type.NEVER
}