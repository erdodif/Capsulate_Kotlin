package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Type(open vararg val labels: String) : KParcelable {
    @KIgnoredOnParcel
    open val label: String
        get() = labels.firstOrNull() ?: "${this::class}"

    override fun toString(): String = label

    companion object {
        fun fromLabel(label: String): Type? = when (label) {
            in NAT.labels -> NAT
            in WHOLE.labels -> WHOLE
            in STRING.labels -> STRING
            in CHAR.labels -> CHAR
            /* CURRENTLY UNSUPPORTED:
            label in FILE.labels -> FILE
            label in STREAM.labels -> STREAM
            label in TUPLE.labels -> TUPLE
            label in SET.labels -> SET
            */
            in NEVER.labels -> NEVER
            else -> if (label.startsWith("Array(") && label.endsWith(")")) {
                val trunc = label.drop(6).dropLast(1).split(',')
                if (trunc.count() != 2) {
                    null
                } else {
                    val type = fromLabel(trunc[0])
                    val count = trunc[1].toIntOrNull()
                    if (type != null && count != null) ARRAY(type, count) else null
                }
            } else {
                null
            }
        }
    }
}

@Serializable
sealed class NUM(@SerialName("numlabels") override vararg val labels: String) : Type(*labels)

@KParcelize
@Serializable
data object NAT : NUM("ℕ", "Nat")

@KParcelize
@Serializable
data object WHOLE : NUM("ℤ", "Whole", "Int", "Integer")

@KParcelize
@Serializable
data object STRING : Type("𝕊", "String")

@KParcelize
@Serializable
data object BOOL : Type("𝔹", "Bool", "Boolean")

@KParcelize
@Serializable
data object CHAR : Type("ℂ", "Char")

@KParcelize
@Serializable
data object NEVER : Type("⊥", "Bot", "Never")

@KParcelize
@Serializable
data class ARRAY(val contentType: Type, val size: Int) : Type("Array") {
    @KIgnoredOnParcel
    override val label: String
        get() = "Array(${contentType.label})"

    val primitiveType: Type
        get() = if (contentType is ARRAY) contentType.primitiveType else contentType
    val dimensions: List<Int>
        get() = if (contentType is ARRAY) listOf(size) + contentType.dimensions else listOf(size)

    fun typeOnLevel(level: Int): Type = when (level) {
        0 -> this
        1 -> contentType
        else -> (contentType as? ARRAY)?.typeOnLevel(level - 1) ?: NEVER
    }

    override fun equals(other: Any?): Boolean =
        other is ARRAY && other.contentType == contentType && other.size == size

    override fun hashCode(): Int = label.hashCode()
    override fun toString(): String =
        primitiveType.label + dimensions.reversed().joinToString(separator = "") { "($it)" }
}

/*Not yet supported*/
@KParcelize
@Serializable
data object FILE : Type("File")

@KParcelize
@Serializable
data object STREAM : Type("Stream")

@KParcelize
@Serializable
data object TUPLE : Type("Pair")

@KParcelize
@Serializable
data object SET : Type("Set")
