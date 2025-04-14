package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import kotlin.jvm.JvmInline

interface Value : KParcelable {
    override operator fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    val type: Type
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
    override val type: Type
        get() = NAT
}

@KParcelize
@JvmInline
value class VWhole(override val value: Int) : VNum { // ℤ
    override fun toString(): String = value.toString()
    override val type: Type
        get() = WHOLE
}

@KParcelize
@JvmInline
value class VChr(val value: Char) : Value {  // ℂ
    override fun toString(): String = value.toString()
    override val type: Type
        get() = CHAR
}

@KParcelize
@JvmInline
value class VStr(val value: String) : Value {  // 𝕊
    override fun toString(): String = value.toString()
    override val type: Type
        get() = STRING
}

@KParcelize
@JvmInline
value class VBool(val value: Boolean) : Value { // 𝔹
    override fun toString(): String = value.toString()
    override val type: Type
        get() = BOOL
}

@KParcelize
data object UNSET : Value {
    override val type: Type
        get() = NEVER
}

@KParcelize
@Suppress("UNCHECKED_CAST")
data class VArray<T : Value>(
    private val value: Array<T?>,
    override val type: Type
) : Value {
    constructor(size: Int, type: Type) : this(arrayOfNulls<Any?>(size) as Array<T?>, type)

    val size: Int
        get() = value.size

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

    override fun toString(): String = value.joinToString(prefix = "[", postfix = "]")
    override fun hashCode(): Int = value.contentHashCode()

    @KParcelize
    data class Index(val id: String, val indexer: Exp<Value>) : Exp<Value> {
        override fun getType(assumptions: Map<String, Type>): Type =
            when (val assume = assumptions[id]) {
                is ARRAY -> assume.contentType
                else -> NEVER
            }

        override fun evaluate(context: Environment): Either<Value, PendingExpression<Value, Value>> =
            indexer.withRawValue(context) {
                if (it is VNat) {
                    when (val param = context.get(id)) {
                        is Left -> when (val value = param.value.value) {
                            is VArray<*> -> {
                                if (it.value >= value.value.size) {
                                    error(
                                        "Index out of bounds (asked for ${it.value} " +
                                                "in an array with size of ${value.value.size})"
                                    )
                                } else {
                                    value.value[it.value] as Value
                                }
                            }

                            else -> error("Can't index non-array ($id : ${value.type})")
                        }

                        is Right -> error("Can't index on missing parameter '$id'")
                    }
                } else {
                    error(
                        "Can't index with anything except a Natural number (got ${
                            indexer.getType(context.assumptions)
                        })"
                    )
                }
            }

        override fun toString(
            state: ParserState,
            parentStrength: Int
        ): String = "$id[${indexer.toString(state, parentStrength)}]"

    }
}
