package com.erdodif.capsulate.lang.specification.coc

import com.erdodif.capsulate.lang.specification.grammar.Var

//
// https://cs.ru.nl/~freek/courses/tt-2019/public/coq-manual-4.4.pdf
// Left at Var typing rule

abstract class Variable(open val name: String, override val type: Sort) : Sort
data class Definition(override val name: String, val value: String, override val type: Sort) :
    Variable(name, type)

data class Assumption(override val name: String, override val type: Sort) : Variable(name, type)

interface Sort {
    override fun equals(other: Any?): Boolean

    val type:Sort
}

/**
 * Type sets of which elements can not be extracted as a value
 */
class Prop(val label: String) : Sort {
    override fun equals(other: Any?): Boolean = other is Prop && other.label == label

    override val type: Sort
        get() = Type(1)
}

/**
 * Proposition for which the value is irrelevant and should be ignored
 */
class SProp(val label: String) : Sort {
    override fun equals(other: Any?): Boolean = other is SProp && other.label == label

    override val type: Sort
        get() = Type(1)
}

/**
 * Elements of this set can be extracted as values
 */
class Set: Sort {
    override fun equals(other: Any?): Boolean = other is Set

    override val type:Sort
        get() = Type(1)
}

/**
 * Set to represent the infinite sort of sets
 *
 * A Set[level] will always be a part of a Set on the next level
 */
data class Type(val level: Int) : Sort {
    override fun equals(other: Any?): Boolean = other is Type && other.level == level

    override fun hashCode(): Int = level

    override val type:Sort
        get() = Type(level + 1)
}

interface Context{
    operator fun get(index: Int): Variable
    operator fun get(name:String): Variable?
    fun define(name: String, value: String, type: Sort): Definition
    fun assume(name: String, type: Sort) : Assumption
    fun wellFormed(type: Sort) : Boolean
    fun wellFormed(): Boolean
}

class GlobalEnvironment(vararg declaration: Variable): Context{
    private val declarations: MutableList<Variable> = declaration.toMutableList()

    override operator fun get(index: Int): Variable = declarations[index]
    override operator fun get(name:String): Variable? = declarations.find{it.name == name}

    /**
     * Adds the given definition [name] := [value] : [type]
     *
     * This ensures W-Global-Def
     */
    override fun define(name: String, value: String, type: Sort): Definition {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within the this local context!"
        }
        val source = this[name]
        require(source != null) {
            "The name '$name' is already within the this local context!"
        }
        require(source.type == type){
            "The value given has type mismatch!"
        }
        val definition = Definition(name, value, type)
        declarations.add(definition)
        return definition
    }

    /**
     * Adds the given assumption [name] : [type]
     *
     * This ensures W-Global-Assum
     */
    override fun assume(name: String, type: Sort) : Assumption {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within the this local context!"
        }
        val assumption = Assumption(name, type)
        declarations.add(assumption)
        return assumption
    }

    override fun wellFormed(type: Sort): Boolean {
        return type is Variable && this[type.name] != null || type is Type
    }

    override fun wellFormed(): Boolean = declarations.all(::wellFormed)
}

/**
 * A local context within the global environment
 */
class LocalContext(private val context: GlobalEnvironment, vararg declaration: Variable): Context {
    private val declarations: MutableList<Variable> = declaration.toMutableList()
    override operator fun get(index: Int): Variable = declarations[index]
    override operator fun get(name:String): Variable? = declarations.find{it.name == name}

    /**
     * Adds the given definition [name] := [value] : [type]
     *
     * This ensures W-Local-Def
     */
    override fun define(name: String, value: String, type: Sort) : Definition {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within the this local context!"
        }
        val source = this[name]
        require(source == null) {
            "The name '$name' is already within the this local context!"
        }
        val definition = Definition(name, value, type)
        declarations.add(definition)
        return definition
    }

    /**
     * Adds the given assumption [name] : [type]
     *
     * This ensures W-Local-Assum
     */
    override fun assume(name: String, type: Sort) : Assumption {
        require(declarations.none { it.name == name }) {
            "The name '$name' is already within the this local context!"
        }
        val assumption = Assumption(name, type)
        declarations.add(assumption)
        return assumption
    }

    /**
     *
     */
    fun typeOf(name: String): Sort {
        val def = declarations.find { it.name == name }
        require(def != null) {
            "Variable with name '$name' not found"
        }
        return def.type
    }

    operator fun plus(other: LocalContext): LocalContext {
        require(declarations.none { it in other.declarations }) {
            "The two contexts share name on an element, which is unacceptable"
        }
        // TODO: Consider renaming variables
        return LocalContext(context, *(declarations + other.declarations).toTypedArray())
    }

    override fun wellFormed(type: Sort): Boolean {
        return (type is Variable && (this[type.name] != null || context[type.name] != null) ) || type is Type
    }

    override fun wellFormed(): Boolean = declarations.all(::wellFormed) && context.wellFormed()
}
