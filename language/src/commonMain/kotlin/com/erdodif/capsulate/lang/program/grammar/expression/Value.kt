package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.BigIntParceler
import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.KTypeParceler
import com.ionspin.kotlin.bignum.BigNumber
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

interface Value : KParcelable {
    override operator fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    val type: Type
}

@Serializable
sealed interface VNum<T : BigNumber<T>> : Value {
    override val type: NUM
    val value: BigNumber<T>
}

@KParcelize
@JvmInline
@Serializable
value class VNat(
    @KTypeParceler<BigInteger, BigIntParceler>
    @Serializable(with = BigIntSerializer::class)
    override val value: BigInteger
) : VNum<@Serializable(with = BigIntSerializer::class) BigInteger> { // ℕ
    constructor(value: String) : this(value.toBigInteger())
    constructor(value: Int) : this(value.toBigInteger())

    init {
        require(!value.isNegative) {
            "Natural number cannot be negative"
        }
    }

    override fun toString(): String = value.toString()
    override val type: NAT
        get() = NAT
}

@KParcelize
@JvmInline
@Serializable
value class VWhole(
    @KTypeParceler<BigInteger, BigIntParceler>
    @Serializable(with = BigIntSerializer::class)
    override val value: BigInteger
) : VNum<@Serializable(with = BigIntSerializer::class) BigInteger> { // ℤ
    constructor(value: String) : this(value.toBigInteger())
    constructor(value: Int) : this(value.toBigInteger())

    override fun toString(): String = value.toString()
    override val type: WHOLE
        get() = WHOLE
}

@KParcelize
@JvmInline
@Serializable
value class VChr(val value: Char) : Value {  // ℂ
    override fun toString(): String = value.toString()
    override val type: CHAR
        get() = CHAR
}

@KParcelize
@JvmInline
@Serializable
value class VStr(val value: String) : Value { // 𝕊
    override fun toString(): String = value.toString()
    override val type: STRING
        get() = STRING
}

@KParcelize
@JvmInline
@Serializable
value class VBool(val value: Boolean) : Value { // 𝔹
    override fun toString(): String = value.toString()
    override val type: BOOL
        get() = BOOL
}

@KParcelize
@Serializable
data object UNSET : Value {
    override val type: NEVER
        get() = NEVER
}

@KParcelize
@Serializable
@Suppress("UNCHECKED_CAST")
data class VArray<T : Value>(
    @Contextual private val value: Array<T?>,
    override val type: ARRAY
) : Value {
    init {
        require(type.contentType == (value.firstOrNull()?.type ?: NEVER)) {
            "Array created with inconsistent type " +
                    "(given $type, but assumed ${value.firstOrNull()?.type ?: NEVER} from first value)"
        }
    }

    constructor(value: Array<T?>) :
            this(value, ARRAY(value.firstOrNull()?.type ?: NEVER, value.size))

    constructor(size: Int, contentType: Type) :
            this(arrayOfNulls<Any?>(size) as Array<T?>, ARRAY(contentType, size))

    val size: Int
        get() = value.size
    val contentType: Type
        get() = type.contentType

    @KIgnoredOnParcel
    val depth: Int = ((value.first() as? VArray<*>)?.depth ?: 0) + 1

    fun unsafeGet(index: Int): T = value[index] ?: error("Value uninitialized at [$index]")

    private fun requireValidIndex(vararg indexes: Int): Unit = when {
        indexes.isEmpty() -> error("Missing index for $type")
        depth < indexes.size -> error(
            "The array isn't this deep! Asked for ${indexes.size} dimensions, this array is only $depth levels deep."
        )

        indexes.first() < 1 -> error(
            "VArray index must be positive (given ${indexes.first()})!"
        )

        indexes.first() > value.size -> error(
            "VArray index out of bounds (given ${indexes.first()}, max index ${value.size})!"
        )

        indexes.size > 1 && value[indexes.first() - 1] !is VArray<*> -> error(
            "Internal type is not a VArray! (${value[indexes.first() - 1]?.type ?: NEVER})"
        )

        else -> Unit
    }

    operator fun get(vararg indexes: Int): Value = if (indexes.size == 1) {
        requireValidIndex(indexes.first())
        value[indexes.first() - 1] ?: UNSET
    } else {
        requireValidIndex(*indexes)
        (value[indexes.first() - 1] as? VArray<*>)?.get(*indexes.drop(1).toIntArray()) ?: UNSET
    }

    operator fun set(vararg indexes: Int, value: T) {
        if (type.typeOnLevel(indexes.size) != value.type) {
            error(
                "Type mismatch! " +
                        "(This array has type of ${type.typeOnLevel(indexes.size)} " +
                        "on level ${indexes.size}, but the value is ${value.type})"
            )
        }
        if (indexes.size == 1) {
            this.value[indexes.first() - 1] = value
        } else {
            requireValidIndex(*indexes)
            (this.value[indexes.first() - 1] as? VArray<T>)?.set(
                indexes = indexes.drop(1).toIntArray(),
                value = value
            )
        }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || other !is Value || other !is VArray<*> ||
                type != other.type -> false

        else -> value.contentEquals(other.value)
    }

    override fun toString(): String = value.joinToString(prefix = "[", postfix = "]")
    override fun hashCode(): Int = type.hashCode() * 3100 + value.contentHashCode()

}
