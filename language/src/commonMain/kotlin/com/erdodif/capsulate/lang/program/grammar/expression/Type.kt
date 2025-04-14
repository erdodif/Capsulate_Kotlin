package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize

sealed class Type(open vararg val labels: String): KParcelable {
    @KIgnoredOnParcel
    open val label: String
        get() = labels.firstOrNull() ?: "${this::class}"
}

sealed class NUM(override vararg val labels: String) : Type(*labels)
@KParcelize
data object NAT : NUM("ℕ", "Nat")
@KParcelize
data object WHOLE : NUM("ℤ", "Whole", "Integer")

@KParcelize
data object STRING : Type("𝕊", "String")
@KParcelize
data object BOOL : Type("𝔹", "Boolean")
@KParcelize
data object CHAR : Type("ℂ", "Char")
@KParcelize
data object NEVER : Type("⊥", "Never")
@KParcelize
data class ARRAY(val contentType: Type, val size:Int) : Type("Array") {
    @KIgnoredOnParcel
    override val label: String
        get() = "Array(${contentType.label})"

    fun typeOnLevel(level: Int): Type = when(level){
        0 -> this
        1 -> contentType
        else -> (contentType as? ARRAY)?.typeOnLevel(level-1) ?: NEVER
    }

    override fun equals(other: Any?): Boolean = other is ARRAY && other.contentType == contentType
    override fun hashCode(): Int = label.hashCode()
}

/*Not yet supported*/
@KParcelize
data object FILE : Type("File")
@KParcelize
data object STREAM : Type("Stream")
@KParcelize
data object TUPLE : Type("Pair")
@KParcelize
data object SET : Type("Set")
