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
    override val type: NUM
    val value: Int
}

@KParcelize
@JvmInline
value class VNat(private val _value: UInt) : VNum {   // ℕ
    override val value: Int
        get() = _value.toInt()

    override fun toString(): String = value.toString()
    override val type: NAT
        get() = NAT
}

@KParcelize
@JvmInline
value class VWhole(override val value: Int) : VNum { // ℤ
    override fun toString(): String = value.toString()
    override val type: WHOLE
        get() = WHOLE
}

@KParcelize
@JvmInline
value class VChr(val value: Char) : Value {  // ℂ
    override fun toString(): String = value.toString()
    override val type: CHAR
        get() = CHAR
}

@KParcelize
@JvmInline
value class VStr(val value: String) : Value {  // 𝕊
    override fun toString(): String = value.toString()
    override val type: STRING
        get() = STRING
}

@KParcelize
@JvmInline
value class VBool(val value: Boolean) : Value { // 𝔹
    override fun toString(): String = value.toString()
    override val type: BOOL
        get() = BOOL
}

@KParcelize
data object UNSET : Value {
    override val type: NEVER
        get() = NEVER
}

@KParcelize
@Suppress("UNCHECKED_CAST")
data class VArray<T : Value>(
    private val value: Array<T?>,
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

    val depth: Int = ((value.first() as? VArray<*>)?.depth ?: 0) + 1

    fun unsafeGet(index: Int): T = value[index] ?: error("Value uninitialized at [$index]")

    private fun requireValidIndex(vararg indexes: Int): Unit = when {
        indexes.isEmpty() -> error("Missing index for $type")
        depth > indexes.size -> error(
            "The array isn't this deep! Asked for ${indexes.size} dimensions, this array is only $depth levels deep."
        )

        indexes.first() < 1 -> error(
            "VArray index must be positive (given ${indexes.first()})!"
        )

        indexes.first() > value.size -> error(
            "VArray index out of bounds (given ${indexes.first()}, max index ${value.size})!"
        )

        value[indexes.first() - 1] !is VArray<*> -> error(
            "Internal type is not a VArray! (${value[indexes.first() - 1]?.type ?: NEVER})"
        )

        else -> Unit
    }

    operator fun get(vararg indexes: Int): Value = if (indexes.size == 1) {
        value[indexes.first() - 1] ?: UNSET
    } else {
        requireValidIndex(*indexes)
        (value[indexes.first() - 1] as? VArray<*>)?.get(*indexes.drop(1).toIntArray()) ?: UNSET
    }

    operator fun set(vararg indexes: Int, value: T) {
        if (type.typeOnLevel(indexes.size) != value.type) {
            error("Type mismatch! " +
                    "(This array has type of ${type.typeOnLevel(indexes.size)} " +
                    "on level ${indexes.size}, but the value is ${value.type})")
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

    @KParcelize
    data class Index(val id: String, val indexers: List<Exp<Value>>) : Exp<Value> {
        override fun getType(assumptions: Map<String, Type>): Type =
            when (val assume = assumptions[id]) {
                is ARRAY -> assume.contentType
                else -> NEVER
            }

        override fun evaluate(context: Environment): Either<Value, PendingExpression<Value, Value>> =
            indexers.withRawValue(context) { indexers ->
                if (indexers.any { it !is VNum }) {
                    error(
                        "Cannot index with non-numbers! Got: " + indexers
                            .mapIndexed { index, a -> a to index }
                            .filter { it.first !is VNum }
                            .joinToString(postfix = ".") { (value, index) -> "${value.type} at ${index + 1}" }

                    )
                }
                indexers as List<VNum>
                when (val param = context.get(id)) {
                    is Left -> when (val value = param.value) {
                        is VArray<*> -> value.get(indexes = indexers.map{it.value}.toIntArray())
                        else -> error("Can't index non-array ($id : ${value.type})")
                    }

                    is Right -> error("Can't index on missing parameter '$id'")
                }
            }

        override fun toString(state: ParserState, parentStrength: Int): String =
            id + indexers.joinToString(separator = "") { "[${it.toString(state, parentStrength)}]" }
    }
}
