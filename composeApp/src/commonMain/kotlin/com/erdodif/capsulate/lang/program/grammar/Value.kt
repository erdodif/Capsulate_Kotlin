package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize

interface Value: KParcelable {
    override operator fun equals(other: Any?): Boolean
}

abstract class VNum : Value {
    abstract val value: Int
}

@KParcelize
data class VNat(private val _value: UInt) : VNum() {   // ℕ
    override val value: Int
        get() = _value.toInt()
}

@KParcelize
data class VWhole(override val value: Int) : VNum()   // ZZ
@KParcelize
data class VStr(val value: String) : Value   // 𝕊
@KParcelize
data class VBool(val value: Boolean) : Value
@KParcelize
data class VCharacter(val value: Char) : Value   // ℂ
/*TODO:
class VFile: Value   // sx,dx,x : read
class VArray: Value // _ ^ⁿ
class VStream: Value // _ ^*
class VTuple: Value // ( _ , _ )
class VSet: Value    //
*/

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
