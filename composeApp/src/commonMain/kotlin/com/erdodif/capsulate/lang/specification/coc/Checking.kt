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
class Prop : Sort {
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
abstract class Variable(open val name: String, override val type: Sort) : Sort

/**
 * Variable with value and type
 */
data class Definition(override val name: String, val value: String, override val type: Sort) :
    Variable(name, type) {
    override fun toString(): String = "$name := $value : $type"
    override fun rewrite(label: String, value: Sort): Sort = TODO("Finish Rewrite!")
}

/**
 * Variable with type
 */
data class Assumption(override val name: String, override val type: Sort) : Variable(name, type) {

    override fun toString(): String = "$name : $type"
    override fun rewrite(label: String, value: Sort): Sort =
        (if (label == label) value else Assumption(name, value.rewrite(label, value)))
}

/**
 * Product type (forall)
 */
abstract class Prod(open val label: String) : Sort {
    override fun rewrite(label: String, value: Sort): Sort = TODO("Finish Rewrite!")
}

data class PropProd(override val label: String) : Prod(label) {
    override val type: Sort
        get() = Prop()
}

data class SetProd(override val label: String) : Prod(label) {
    override val type: Sort
        get() = Set()
}

data class TypeProd(override val label: String, val level: Int) : Prod(label) {
    override val type: Sort
        get() = Type(level)
}

// TODO Term for Lambda expression
/**
 * Lambda abstractions as in lambda calculus
 */
data class Lam(val label: String, val term: Sort, val productType: Prod) : Sort {
    override val type: Sort
        get() = productType

    override fun rewrite(label: String, value: Sort): Sort = TODO("Finish Rewrite!")
}

/**
 * Application of [x] on [f]
 */
data class App(val f: Lam, val x: Variable) : Sort {
    override val type: Sort
        get() = TODO("This is Rewrite territory! E[Γ]⊢(t u):T{x/u}")

    override fun rewrite(label: String, value: Sort): Sort = TODO("Finish Rewrite!")
}

// TODO Term for let .. in .. notation
/**
 * Let .. in .. notation
 *
 * Will be well formed if with the given [definition], [term] can be rewritten at the occurrences in [definition]
 */
data class Let(val definition: Definition, val term: Sort) : Sort {

    override val type: Sort
        get() = term.rewrite(definition.name, TODO("Until ${definition.value} is a Sort..."))

    override fun rewrite(label: String, value: Sort): Sort = TODO("Finish Rewrite!")
}

