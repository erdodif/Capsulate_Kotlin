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

    val depth: Int = ((value.first() as? VArray<*>)?.depth ?: 0) + 1

    fun unsafeGet(index: Int): T = value[index] ?: error("Value uninitialized at [$index]")

    operator fun get(index: Int): Value = value[index] ?: UNSET
    operator fun get(vararg index: Int): Value = when {
        index.size == 1 -> value[index.first()] ?: UNSET
        depth > index.size -> error(
            "The array isn't this deep! Asked for ${index.size} dimensions, this array is only $depth levels deep."
        )

        index.first() < 1 -> error(
            "Array index must be positive (given ${index.first()})!"
        )

        index.first() > value.size -> error(
            "Array index out of bounds (given ${index.first()}, max index ${value.size})!"
        )

        value[index.first()] !is VArray<*> -> error(
            "Internal type is not an Array! (${value[index.first()]?.type ?: NEVER})"
        )

        else -> (value[index.first()] as? VArray<*>)?.get(*index.drop(1).toIntArray()) ?: UNSET
    }

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
                if (indexers.any { it.value < 1 }) {
                    error(
                        "Indexers must be positive! Got: " + indexers
                            .mapIndexed { index, a -> a to index }
                            .filter { it.first.value < 1 }
                            .joinToString(postfix = ".") { (value, index) -> "$value at $index" }
                    )
                }
                when (val param = context.get(id)) {
                    is Left -> when (val value = param.value.value) {
                        is VArray<*> -> {
                            if (indexers[0].value > value.value.size) {
                                error(
                                    "Index out of bounds (asked for ${indexers[0].value} " +
                                            "in an array with size of ${value.value.size})"
                                )
                            } else {
                                (value.value[indexers[0].value] as? VArray<*>)?.get(
                                    *indexers.drop(1).map(VNum::value).toIntArray()
                                ) as Value
                            }
                        }

                        else -> error("Can't index non-array ($id : ${value.type})")
                    }

                    is Right -> error("Can't index on missing parameter '$id'")
                }
            }


        override fun toString(
            state: ParserState,
            parentStrength: Int
        ): String =
            id + indexers.joinToString(separator = "") { "[${it.toString(state, parentStrength)}]" }
    }
}

fun <T: Value> VArray<VArray<T>>.set(value: T, vararg indexes: Int){
    TODO("Not yet implemented!")
}
