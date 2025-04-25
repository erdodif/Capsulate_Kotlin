package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.NEVER
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.Pattern
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlin.random.Random

@KParcelize
data class Parameter(val id: String, val type: Type, var value: Value) : KParcelable {
    override fun toString(): String {
        return "#$id = $value : $type"
    }
}

@Serializable
sealed interface Environment : KParcelable {
    val functions: Map<String, Function<Value>>
    val methods: Map<Pattern, Method>
    val seed: Int
    val random: Random
    val parameters: ImmutableList<Parameter>
    val assumptions: Map<String, Type>

    /** Determines whether the asked variable is defined in this context */
    fun present(id: String): Boolean

    /** Returns the variable with the given label (or [Right] if not found)*/
    fun getParameter(id: String): Either<Parameter, Unit>

    /** Returns the variable's value (or indexed value, if index is given)*/
    fun get(id: String, vararg indexes: Int): Either<Value, Unit>

    /**
     * Get the type of the parameter with the given [id], and depth, if given
     *
     * On depth 0, the parameter's own type is returned
     *
     * Below 0, the type will be [NEVER]
     */
    fun typeOf(id: String, depth: Int = 0): Type

    /**
     * Sets the variable's content to the desired value
     *
     * If the variable is missing (and not array-accessed), it will be added into the context
     *
     * In case at least an indexer is given, said variable cannot be missing (and must be an array)
     */
    fun set(id: String, value: Value, vararg indexes: Int)

    /**
     * Create a Proxy Environment based on this one
     *
     * Every parameter that is present in the [renames] will be available in the proxy environment,
     * while the remaining parameters will be shadowed
     */
    fun proxyWith(renames: Map<String, String>): ProxyEnv = ProxyEnv(renames, this)

    override fun toString(): String
}

/**
 * Renames are stored as Map, where the key is the original name and the value is the new name
 */
@KParcelize
@Serializable
data class ProxyEnv(val renames: Map<String, String>, val env: Environment) : Environment {
    override val random: Random
        get() = env.random
    override val seed: Int
        get() = env.seed
    override val functions: Map<String, Function<Value>>
        get() = env.functions
    override val methods: Map<Pattern, Method>
        get() = env.methods
    override val assumptions: Map<String, Type>
        get() = (shadowEnv.assumptions + env.assumptions.filter { renames.containsKey(it.key) })
    private val shadowEnv = Env(seed = env.seed)

    @KIgnoredOnParcel
    private val newNames = renames.map { it.value to it.key }.associate { it }

    override val parameters: ImmutableList<Parameter>
        get() = (shadowEnv.parameters + env.parameters.mapNotNull {
            if (renames.containsKey(it.id)) it.copy(id = "⦃${it.id}⩬${renames[it.id]}⦄") else null
        }).toImmutableList()

    override fun present(id: String): Boolean =
        newNames.containsKey(id) || shadowEnv.present(id)

    override fun getParameter(id: String): Either<Parameter, Unit> =
        if (newNames[id] != null) env.getParameter(newNames[id]!!) else shadowEnv.getParameter(id)

    override fun get(id: String, vararg indexes: Int) =
        if (newNames[id] != null) env.get(newNames[id]!!) else shadowEnv.get(id, indexes = indexes)

    override fun typeOf(id: String, depth: Int) =
        if (newNames[id] != null) env.typeOf(newNames[id]!!, depth) else shadowEnv.typeOf(id, depth)

    @Suppress("UNCHECKED_CAST")
    override fun set(id: String, value: Value, vararg indexes: Int) = if (newNames[id] != null) {
        env.set(newNames[id]!!, value, indexes = indexes)
    } else {
        shadowEnv.set(id, value, indexes = indexes)
    }

}

@KParcelize
@Serializable
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

    @KIgnoredOnParcel
    override val random = Random(seed)

    override val parameters: ImmutableList<Parameter>
        get() = values.toImmutableList()

    override val assumptions: Map<String, Type>
        get() = values.associate { it.id to it.type }

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

    override fun getParameter(id: String): Either<Parameter, Unit> = if (present(id)) {
        val pos = values.indexOfFirst { it.id == id }
        Left(Parameter(values[pos].id, values[pos].type, values[pos].value))
    } else Right(Unit)

    override fun get(id: String, vararg indexes: Int): Either<Value, Unit> = when {
        !present(id) -> Right(Unit)
        indexes.isEmpty() -> Left(unSafeGet(id))
        else -> when (val array = unSafeGet(id)) {
            is VArray<*> -> Left(array.get(indexes = indexes))
            else -> Right(Unit)
        }
    }

    override fun typeOf(id: String, depth: Int) = if (depth == 0) {
        values.find { it.id == id }?.type
    } else {
        ((get(id) as? Left<*>)?.value as? VArray<*>)?.type?.typeOnLevel(depth)
    } ?: NEVER

    @Suppress("UNCHECKED_CAST")
    override fun set(
        id: String,
        value: Value,
        vararg indexes: Int
    ) {
        val result = get(id)
        if (indexes.isEmpty()) {
            if (assumptions.contains(id) && assumptions[id] != value.type) {
                error("Type mismatch! ($id has type of ${assumptions[id]}, but value got ${value.type})")
            }
            when (result) {
                is Left -> values[values.indexOfFirst { it.id == id }].value = value
                is Right -> values.add(Parameter(id, value.type, value))
            }
        } else if (result is Right) {
            error("Cannot find parameter to index into (missing $id)")
        } else {
            val array = (result as Left).value
            when {
                array !is VArray<*> -> error("Parameter named $id is not an array (namely ${array.type})")
                array.size < indexes.first() || indexes.first() < 1 ->
                    error("Index out of bounds (asked for $indexes in an array size of ${array.size})")

                else -> {
                    val firstIndex = values.indexOfFirst { it.id == id }
                    val theArray = values[firstIndex].value as VArray<Value>
                    theArray.set(indexes = indexes, value = value)
                    values[firstIndex].value = theArray.copy()
                }
            }
        }
    }

    override fun toString(): String {
        return values.toString()
    }
}
