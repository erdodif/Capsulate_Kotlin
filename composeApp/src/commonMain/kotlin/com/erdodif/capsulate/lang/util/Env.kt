package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Type
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.type
import kotlin.random.Random
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@KParcelize
data class Parameter(val id: String, val type: Type, var value: Value): KParcelable {
    override fun toString(): String {
        return "#$id = $value : $type"
    }
}

@KParcelize
data class Env(
    private val values: MutableList<Parameter>,
    val deterministic: Boolean = false,
    private val seed: Int = Random.nextInt(),
): KParcelable {
    val random = Random(seed)

    val parameters: ImmutableList<Parameter>
        get() = values.toImmutableList()

    fun copy(): Env {
        return Env(values.map { it.copy() }.toMutableList(), deterministic)
    }

    /** Determines whether the asked variable is defined in this context */
    fun present(id: String): Boolean = values.any { it.id == id }

    /**
     * Does not perform check before accessing the variable
     *
     * If the variable is undefined, [NullPointerException] will occur
     *
     * ## USE WITH CAUTION
     */
    private fun unSafeGet(id: String): Value {
        return values.find { it.id == id }!!.value
    }

    /** Returns the variable's value */
    fun get(id: String): Either<Parameter, Unit> =
        if (present(id)) {
            val pos = values.indexOfFirst { it.id == id }
            Left(Parameter(values[pos].id, values[pos].type, values[pos].value))
        } else Right(Unit)

    /** Returns the variable's value */
    fun getValue(id: String): Either<Value, Unit> =
        if (present(id)) Left(unSafeGet(id)) else Right(Unit)

    fun typeOf(id: String): Type =
        if (present(id)) values.find { it.id == id }!!.type else Type.NEVER

    /**
     * Sets the variable's content to the desired value
     *
     * If the variable is missing, it will be added into the context
     */
    fun set(id: String, value: Value) {
        if (present(id)) values[values.indexOfFirst { it.id == id }].value = value
        else values.add(Parameter(id, value.type(), value))
    }

    companion object {
        val empty: Env
            get() = Env(mutableListOf())
    }

    override fun toString(): String {
        return values.toString()
    }
}

class ErrorContext
