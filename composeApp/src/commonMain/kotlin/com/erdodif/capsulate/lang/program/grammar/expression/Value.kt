package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.fold
import kotlin.jvm.JvmInline

interface Value : KParcelable {
    override operator fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

sealed interface VNum : Value {
    val value: Int
}

@KParcelize
@JvmInline
value class VNat(private val _value: UInt) : VNum {   // ℕ
    override val value: Int
        get() = _value.toInt()

    override fun toString(): String = value.toString()
}

@KParcelize
@JvmInline
value class VWhole(override val value: Int) : VNum { // ZZ
    override fun toString(): String = value.toString()
}

@KParcelize
@JvmInline
value class VStr(val value: String) : Value {  // 𝕊
    override fun toString(): String = value.toString()
}

@KParcelize
@JvmInline
value class VBool(val value: Boolean) : Value {
    override fun toString(): String = value.toString()
}

/**
 * Value to
 */
data object UNSET : Value

@KParcelize
data class VArray<T : Value>(
    private val value: Array<Either<T, UNSET>>,
    val type: Type
) : Value {
    constructor(size: Int, type: Type) : this(Array(size) { Right(UNSET) }, type)

    fun unsafeGet(index: Int): T =
        value[index].fold(
            { it },
            { throw IllegalStateException("Value uninitialized at [$index]") })

    operator fun get(index: Int): Value = value[index].fold({ it }, { it })

    operator fun set(index: Int, value: T) {
        this.value[index] = Left(value)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || other !is Value || other !is VArray<*> ||
                type != other.type -> false

        else -> value.contentEquals(other.value)
    }

    override fun hashCode(): Int = value.contentHashCode()
}

@KParcelize
data object UNSET : Value

@KParcelize
data class VArray<T : Value>(
    private val value: Array<T?>,
    val type: Type
) : Value {
    constructor(size: Int, type: Type) : this(arrayOfNulls<Any?>(size) as Array<T?>, type)

    fun unsafeGet(index: Int): T = value[index] ?: error("Value uninitialized at [$index]")

    operator fun get(index: Int): Value = value[index] ?: UNSET

    operator fun set(index: Int, value: T) {
        this.value[index] = value
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || other !is Value || other !is VArray<*> ||
                type != other.type -> false

        else -> value.contentEquals(other.value)
    }

    override fun hashCode(): Int = value.contentHashCode()
}

@KParcelize
@JvmInline
value class VCharacter(val value: Char) : Value {
    override fun toString(): String = value.toString()
}   // ℂ

enum class Type(vararg val labels: String) {
    NAT("ℕ", "Nat"),
    WHOLE("ℤ", "Whole", "Integer"),
    STRING("𝕊", "String"),
    BOOL("𝔹", "Boolean"),
    CHAR("ℂ", "Char"),
    FILE("File"),
    ARRAY("Array"),
    STREAM("Stream"),
    TUPLE("Pair"),
    SET("Set"),
    NEVER("⊥", "Never");

    val label: String
        get() = labels.first()
}

fun Value.type(): Type = when (this) {
    is VNat -> Type.NAT
    is VWhole -> Type.WHOLE
    is VStr -> Type.STRING
    is VBool -> Type.BOOL
    is VCharacter -> Type.CHAR
    is VArray<*> -> Type.ARRAY
    /*is VFile -> Type.FILE
    is VArray -> Type.ARRAY
    is VStream -> Type.STREAM
    is VTuple -> Type.TUPLE
    is VSet -> Type.SET*/
    else -> Type.NEVER
}
