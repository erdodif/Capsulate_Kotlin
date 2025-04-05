package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.type
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.Pattern
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

@KParcelize
data class Parameter(val id: String, val type: Type, var value: Value) : KParcelable {
    override fun toString(): String {
        return "#$id = $value : $type"
    }
}

sealed interface Environment : KParcelable {
    val functions: Map<String, Function<Value>>
    val methods: Map<Pattern, Method>
    val seed: Int
    val random: Random
    val parameters: ImmutableList<Parameter>

    /** Determines whether the asked variable is defined in this context */
    fun present(id: String): Boolean

    /** Returns the variable's value */
    fun get(id: String): Either<Parameter, Unit>

    /** Returns the variable's value */
    fun getValue(id: String): Either<Value, Unit>
    fun typeOf(id: String): Type

    /**
     * Sets the variable's content to the desired value
     *
     * If the variable is missing, it will be added into the context
     */
    fun set(id: String, value: Value)

    companion object {
        val EMPTY: Env
            get() = Env(mapOf(), mapOf(), mutableListOf())
    }

    override fun toString(): String

    fun proxyWith(renames: Map<String, String>): ProxyEnv = ProxyEnv(renames, this)

}

/**
 * Renames are stored as Map, where the key is the original name and the value is the new name
 */
@KParcelize
data class ProxyEnv(val renames: Map<String, String>, val env: Environment) : Environment {
    override val random: Random
        get() = env.random
    override val seed: Int
        get() = env.seed
    override val functions: Map<String, Function<Value>>
        get() = env.functions
    override val methods: Map<Pattern, Method>
        get() = env.methods

    private val newNames = renames.map { it.value to it.key }.associate { it }

    private val values: MutableList<Parameter> = mutableListOf()

    override val parameters: ImmutableList<Parameter>
        get() = (values.map { it.copy("${it.id}⭑") } + env.parameters.mapNotNull {
            if (renames.containsKey(it.id)) it.copy(id = "⦃${it.id}⩬${renames[it.id]}⦄") else null
        }).toImmutableList()

    override fun present(id: String): Boolean =
        newNames.containsKey(id) || values.any { it.id == id }

    override fun get(id: String): Either<Parameter, Unit> =
        if (newNames[id] != null) {
            env.get(newNames[id]!!)
        } else {
            when (val result = values.find { it.id == id }) {
                null -> Right(Unit)
                else -> Left(result)
            }
        }

    override fun getValue(id: String): Either<Value, Unit> =
        if (newNames[id] != null) {
            env.getValue(newNames[id]!!)
        } else {
            when (val result = values.find { it.id == id }) {
                null -> Right(Unit)
                else -> Left(result.value)
            }
        }

    override fun typeOf(id: String): Type =
        if (newNames[id] != null) {
            env.typeOf(newNames[id]!!)
        } else {
            values.find { it.id == id }?.type ?: Type.NEVER
        }

    override fun set(id: String, value: Value) {
        when {
            newNames[id] != null -> env.set(newNames[id]!!, value)
            present(id) -> values[values.indexOfFirst { it.id == id }].value = value
            else -> values.add(Parameter(id, value.type(), value))
        }
    }

}

@KParcelize
data class Env(
    override val functions: Map<String, Function<Value>>,
    override val methods: Map<Pattern, Method>,
    private val values: MutableList<Parameter>,
    override val seed: Int = Random.Default.nextInt(),
) : KParcelable, Environment {
    constructor(
        functions: List<Function<Value>> = emptyList(),
        methods: List<Method> = emptyList(),
        values: List<Parameter> = emptyList(),
        seed: Int = Random.Default.nextInt()
    ) : this(
        functions.associateBy { it.name },
        methods.associateBy { it.pattern },
        values.toMutableList(),
        seed
    )

    override val random = Random(seed)

    override val parameters: ImmutableList<Parameter>
        get() = values.toImmutableList()

    fun copy(): Env {
        return Env(
            functions,
            methods,
            values.map { it.copy() }.toMutableList(),
            seed
        )
    }

    /** Determines whether the asked variable is defined in this context */
    override fun present(id: String): Boolean = values.any { it.id == id }

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
    override fun get(id: String): Either<Parameter, Unit> =
        if (present(id)) {
            val pos = values.indexOfFirst { it.id == id }
            Left(Parameter(values[pos].id, values[pos].type, values[pos].value))
        } else Right(Unit)

    /** Returns the variable's value */
    override fun getValue(id: String): Either<Value, Unit> =
        if (present(id)) Left(unSafeGet(id)) else Right(Unit)

    override fun typeOf(id: String): Type =
        if (present(id)) values.find { it.id == id }!!.type else Type.NEVER

    /**
     * Sets the variable's content to the desired value
     *
     * If the variable is missing, it will be added into the context
     */
    override fun set(id: String, value: Value) {
        if (present(id)) values[values.indexOfFirst { it.id == id }].value = value
        else values.add(Parameter(id, value.type(), value))
    }

    override fun toString(): String {
        return values.toString()
    }
}
