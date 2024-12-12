package com.erdodif.capsulate.lang.specification.coc

//
// https://cs.ru.nl/~freek/courses/tt-2019/public/coq-manual-4.4.pdf
// Left at Var typing rule

abstract class Variable(open val name: String, override val type: Sort) : Sort
data class Definition(override val name: String, val value: String, override val type: Sort) :
    Variable(name, type) {
    override fun toString(): String = "$name := $value : $type"
}

// TODO: Have {forall, lambda, app, const}

data class Assumption(override val name: String, override val type: Sort) : Variable(name, type) {

    override fun toString(): String = "$name : $type"
}

sealed interface Sort {
    override fun equals(other: Any?): Boolean

    val type: Sort

    override fun toString(): String
}

/**
 * Type sets of which elements can not be extracted as a value
 */
class Prop(val label: String) : Sort {
    override fun equals(other: Any?): Boolean = other is Prop && other.label == label

    /**
     * Gets the type of Prop, which is (by definition) Type(1)
     *
     * This value ensures Ax-Prop
     */
    override val type: Sort
        get() = Type(1)

    override fun toString(): String = "Prop"
}

/**
 * Elements of this set can be extracted as values
 */
class Set : Sort {
    override fun equals(other: Any?): Boolean = other is Set

    /**
     * Gets the type of Set, which is (by definition) Type(1)
     *
     * This value ensures Ax-Set
     */
    override val type: Sort
        get() = Type(1)

    override fun toString(): String = "Set"
}

/**
 * Set to represent the infinite sort of sets
 *
 * A Set[level] will always be a part of a Set on the next level
 */
data class Type(val level: Int) : Sort {
    override fun equals(other: Any?): Boolean = other is Type && other.level == level

    override fun hashCode(): Int = level

    /**
     * Gets the type of Type(n), which is (by definition) Type(n+1)
     *
     * The infinite set of types is needed so Type:Type kinds of contradictions are not possible
     *
     * This value ensures Ax-Type
     */
    override val type: Sort
        get() = Type(level + 1)

    override fun toString(): String = "Type($level)"
}

abstract class Context(protected val declarations: MutableList<Variable>) {
    operator fun get(index: Int): Variable = declarations[index]
    abstract operator fun get(name: String): Variable?


    abstract fun wellFormed(type: Sort): Boolean
    abstract fun wellFormed(): Boolean
    abstract fun Sort.hasType(other: Sort): Boolean

    fun define(name: String, value: String, type: String): Definition {
        require(this[type] != null){
            "Type definition not found here:\n$this"
        }
        return define(name, value, this[type]!!)
    }

    /**
     * Adds the given definition [name] := [value] : [type]
     *
     * This ensures W-Global/Local-Def
     */
    fun define(name: String, value: String, type: Sort): Definition {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within this context!\n$this"
        }
        val source = this[value]
        require(source != null) {
            "Definition for value ($source) cannot be found here:\n$this"
        }
        require(source.hasType(type)) {
            "Type of ($source : ${source.type}) cannot be inferred as $type!\n$this"
        }
        val definition = Definition(name, value, type)
        declarations.add(definition)
        return definition
    }

    fun assume(name: String, type: String): Assumption {
        require(this[type] != null){
            "Type definition not found here:\n$this"
        }
        return assume(name, this[type]!!)
    }

    /**
     * Adds the given assumption [name] : [type]
     *
     * This ensures W-Global/Local-Assum
     */
    fun assume(name: String, type: Sort): Assumption {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within this context!\n$this"
        }
        require(wellFormed(type) || type in this){
            "Given ($type) is not well formed within this context!\n$this"
        }
        val assumption = Assumption(name, type)
        declarations.add(assumption)
        return assumption
    }

    /**
     * Determines whether the given [sort] is in this context
     */
    operator fun contains(sort: Sort): Boolean = when (sort) {
        is Variable -> get(sort.name) != null
        is Type -> true
        is Set -> true
        is Prop -> true
    }
}

/**
 * Context, which shall be well founded on it's own (cannot depend on other contexts)
 */
class GlobalEnvironment(vararg declaration: Variable) : Context(declaration.toMutableList()) {
    override operator fun get(name: String): Variable? = declarations.find { it.name == name }

    override fun wellFormed(type: Sort): Boolean {
        return type is Variable && this[type.name] != null || type is Type
    }

    /**
     * Ensures that the Global environment is Well Formed
     *
     * For that, one must ensure that all(!) declarations are well formed
     *
     * By definition, an empty context is Well Formed
     *
     * This function ensures W-Empty
     */
    override fun wellFormed(): Boolean = declarations.all(::wellFormed)

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('(')
        for (def in declarations) {
            sb.append(def.toString())
            sb.append(", ")
        }
        sb.deleteRange(sb.length - 2, sb.length - 1)
        sb.append(')')
        return sb.toString()
    }

    override fun Sort.hasType(other: Sort): Boolean {
        TODO("This is Convertibility territory")
    }
}

/**
 * A context that depends on another(global) context hiding constants on re-declaration
 *
 * If a variable is not found locally, the "global" context will be queried
 */
class LocalContext(private val context: GlobalEnvironment, vararg declaration: Variable) :
    Context(declaration.toMutableList()) {
    override operator fun get(name: String): Variable? = declarations.find { it.name == name } ?: context[name]

    operator fun plus(other: LocalContext): LocalContext {
        require(declarations.none { it in other.declarations }) {
            "The two contexts share name on an element, which is unacceptable"
        }
        // TODO: Consider renaming variables
        return LocalContext(context, *(declarations + other.declarations).toTypedArray())
    }

    override fun wellFormed(type: Sort): Boolean {
        return (type is Variable && (this[type.name] != null || context[type.name] != null)) || type is Type
    }

    /**
     * Ensures that the current Local context is Well Formed
     *
     * For that, one must ensure that both the global environment as well as all(!) declarations are well formed
     *
     * By definition, an empty context is Well Formed
     *
     * This function ensures W-Empty
     */
    override fun wellFormed(): Boolean = declarations.all(::wellFormed) && context.wellFormed()

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(context.toString())
        sb.append('[')
        for (def in declarations) {
            sb.append(def.toString())
            sb.append(", ")
        }
        sb.deleteRange(sb.length - 2, sb.length - 1)
        sb.append(']')
        return sb.toString()
    }

    override fun Sort.hasType(other: Sort): Boolean {
        TODO("This is Convertibility territory")
    }
}
