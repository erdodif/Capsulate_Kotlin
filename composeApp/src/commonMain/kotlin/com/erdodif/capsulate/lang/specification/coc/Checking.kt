package com.erdodif.capsulate.lang.specification.coc

sealed interface Sort {
    val type: Sort

    abstract override fun equals(other: Any?): Boolean
    abstract override fun toString(): String
    fun rewrite(label: String, value: Sort): Sort
}

/**
 * Type sets of which elements can not be extracted as a value
 */
object Prop : Sort {
    override fun equals(other: Any?): Boolean = other is Prop

    /**
     * Gets the type of Prop, which is (by definition) Type(1)
     *
     * This value ensures Ax-Prop
     */
    override val type: Sort
        get() = Type(1)

    override fun toString(): String = "Prop"
    override fun rewrite(label: String, value: Sort): Sort = this
}

/**
 * Elements of this set can be extracted as values
 */
object Set : Sort {
    override fun equals(other: Any?): Boolean = other is Set

    /**
     * Gets the type of Set, which is (by definition) Type(1)
     *
     * This value ensures Ax-Set
     */
    override val type: Sort
        get() = Type(1)

    override fun toString(): String = "Set"
    override fun rewrite(label: String, value: Sort): Sort = this
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
    override fun rewrite(label: String, value: Sort): Sort = this
}

/**
 * Variable in context
 *
 * See it as Var in local, or Const in global context
 */
sealed class Variable(open val name: String, override val type: Sort) : Sort

/**
 * Variable with value and type
 */
data class Definition(override val name: String, val value: Sort, override val type: Sort) :
    Variable(name, type) {
    override fun toString() = "$name := $value : $type"
    override fun rewrite(label: String, value: Sort): Sort = if (label == label) {
        value
    } else {
        Definition(name, this.value.rewrite(label, value), type)
    }
}

/**
 * Variable with type
 */
data class Assumption(override val name: String, override val type: Sort) : Variable(name, type) {

    override fun toString(): String = "$name : $type"
    override fun rewrite(label: String, value: Sort): Sort = if (label == label) value else this
}

/**
 * Product type (forall)
 */
abstract class Prod(open val variable: Assumption, open val term: Sort) : Sort

data class PropProd(override val variable: Assumption, override val term: Sort) :
    Prod(variable, term) {
    override val type: Sort
        get() = Prop

    override fun rewrite(label: String, value: Sort): Sort =
        PropProd(variable, term.rewrite(label, value))
}

data class SetProd(override val variable: Assumption, override val term: Sort) :
    Prod(variable, term) {
    override val type: Sort
        get() = Set

    override fun rewrite(label: String, value: Sort): Sort =
        SetProd(variable, term.rewrite(label, value))
}

data class TypeProd(override val variable: Assumption, override val term: Sort, val level: Int) :
    Prod(variable, term) {
    override val type: Sort
        get() = Type(level)

    override fun rewrite(label: String, value: Sort): Sort =
        TypeProd(variable, term.rewrite(label, value), level)
}

/**
 * Lambda abstractions as in lambda calculus
 */
data class Lam(val variable: Assumption, val term: Sort, val productType: Prod) : Sort {
    override val type: Sort
        get() = productType

    /**
     * Breaks the rewriting chain and returns itself iff the variable subject to rewrite is bound
     */
    override fun rewrite(label: String, value: Sort): Sort =
        if (label == variable.name) this else term.rewrite(label, value)
}

/**
 * Application of [x] on [f]
 *
 * Calling [type] does the rewriting, hence App holds
 * `E[Γ] ⊢ (t u) : T{x/u}`
 */
data class App(val f: Lam, val x: Variable) : Sort {
    override val type: Sort
        get() = f.rewrite(f.variable.name, x)

    override fun rewrite(label: String, value: Sort): Sort =
        App(f, x.rewrite(label, value) as Variable)
}

/**
 * Let .. in .. notation
 *
 * Will be well formed if with the given [definition], [term] can be rewritten at the occurrences in [definition]
 */
data class Let(val definition: Definition, val term: Sort, override val type: Sort) : Sort {

    override fun rewrite(label: String, value: Sort): Sort =
        if (definition.name == label) Let(
            definition.rewrite(label, value) as Definition,
            term.rewrite(label, value),
            type
        )
        else
            Let(definition, term.rewrite(label, value), type)
}
