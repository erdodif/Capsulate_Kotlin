package com.erdodif.capsulate.lang.specification.coc

// https://cs.ru.nl/~freek/courses/tt-2019/public/coq-manual-4.4.pdf
// Left at Var typing rule

/**
 * A set of Terms that makes sure that type-inference rules are enforced during construction
 *
 * A context can tell whether it's well formed or not
 */
abstract class Context(protected val declarations: MutableList<Variable>) {
    operator fun get(index: Int): Variable = declarations[index]
    abstract operator fun get(name: String): Sort?
    abstract fun has(label: String, type: Sort): Boolean
    abstract fun fresh(label: String): Boolean
    abstract val usedLabels: List<String>

    abstract fun wellFormed(type: Sort): Boolean
    abstract fun wellFormed(): Boolean
    open fun add(variable: Variable) {
        declarations.add(variable)
    }

    /**
     * Defines `[name] := [value] : [type]`
     *
     * This ensures W-Global/Local-Def as well as Var/Const
     */
    fun define(name: String, value: Sort, type: Sort): Definition {
        require(this[name] == null) {
            "The name '$name' is already within this context!\n$this"
        }
        require(value.hasType(type)) {
            "Type of ($value : ${value.type}) cannot be inferred as $type!\n$this"
        }
        return Definition(name, value, type)
    }

    /**
     * Assumes that `[name] : [type]`
     *
     * This ensures W-Global/Local-Assum as well as Var/Const
     */
    fun assume(name: String, type: Sort): Assumption {
        require(this[name] == null) {
            "Cannot enrich the context with ${name}, conflicting ${
                if (this[name] is Assumption) "assumption"
                else "definition"
            } in context:\n$this"
        }
        require(type.typeInS()) {
            "Type (${type.type}) used in ${
                if (type is Definition) "definition"
                else "assumption"
            } should not be on level(0)!\n$this"
        }
        require(wellFormed(type)) {
            "Given ($type) is not well formed within this context!\n$this"
        }
        return Assumption(name, type)
    }

    fun Sort.hasType(other: Sort): Boolean = norm(this).type == norm(other)

    /**
     * `S ≡ {Prop, Set, Type(i) | i ∈ N}`
     */
    private fun Sort.typeInS(): Boolean =
        (type is Type && (type as Type).level != 0) || type.typeInS()

    /**
     * `E[Γ] ⊢ ∀x:T, U:Prop`
     */
    fun ProdProp(x: String, t: Sort, u: Sort): Boolean =
        this[x] == null && t.typeInS() && withVar(assume(x, t)) {
            u.hasType(Prop)
        }

    /**
     * `E[Γ] ⊢ ∀x:T, U:Set`
     */
    fun ProdSet(x: String, t: Sort, u: Sort): Boolean =
        this[x] == null && t.typeInS() && withVar(assume(x, t)) {
            u.hasType(Set)
        }

    /**
     * `E[Γ] ⊢ ∀x:T, U:Type(i)`
     */
    fun ProdType(x: String, t: Sort, u: Sort, level: Int): Boolean =
        this[x] == null && t.typeInS() && withVar(assume(x, t)) {
            u.hasType(Type(level))
        }

    /**
     * Creates a new reference of this [Context], temporarily enriched with the given [variable]
     *
     * Through this reference, accessing [variable] can work as per usual,
     * yet the original definition is not modified at all, so removing the [variable] when done is not needed
     */
    fun enriched(variable: Variable): Context {
        require(this[variable.name] == null) {
            "Cannot (temporarily) enrich the context with ${variable.name}, conflicting ${
                if (this[variable.name] is Assumption) "assumption"
                else "definition"
            } in (original) context:\n$this"
        }
        require(variable.typeInS()) {
            "Type (${variable.type}) used in ${
                if (variable is Definition) "definition"
                else "assumption"
            } should not be on level(0)!\n$this"
        }
        val lam = object : Context(mutableListOf(variable)) {
            val hidden = this@Context
            override fun get(name: String): Sort? =
                if (name == variable.name) variable else hidden[name]

            override fun has(label: String, type: Sort): Boolean =
                if (label == variable.name) {
                    variable.hasType(type)
                } else hidden.has(label, type)

            override fun fresh(label: String): Boolean =
                label == variable.name || hidden.fresh(label)

            override val usedLabels: List<String>
                get() = hidden.usedLabels + variable.name

            override fun wellFormed(type: Sort): Boolean = hidden.wellFormed(type)

            override fun wellFormed(): Boolean = hidden.wellFormed()
            override fun add(variable: Variable) {
                throw UnsupportedOperationException(
                    "Shouldn't enrich original context through a temporarily enriched context!"
                )
            }
        }
        return lam
    }

    /**
     * Act as a [with] block, enriches this context with the given [variable] in the [block]
     */
    fun <T> withVar(variable: Variable, block: Context.() -> T): T = with(enriched(variable), block)

    /**
     * Defines a Product type
     *
     * This ensures Prod-Prop, Prod-Set, Prod-Type
     */
    fun prod(label: String, type: String, term: Sort): Prod {
        val tp = this[type]!!
        require(this[label] == null) {
            "Type re-declared here:\n$this"
        }
        return when (term.type) {
            is Set -> {
                require(ProdSet(label,tp, term)){
                    "ProdSet rule violated, term ($term) cannot be inferred as Set in:\n$this"
                }
                SetProd(Assumption(label, tp), term)
            }
            is Type -> {
                require(ProdType(label,tp, term, ((term.type as Type).level))){
                    "ProdType rule violated, term ($term) cannot be inferred as ${term.type} in:\n$this"
                }
                TypeProd(Assumption(label, tp), term, (term as Type).level)
            }
            is Prop -> {
                require(ProdProp(label,tp, term)){
                    "ProdSProp rule violated, term ($term) cannot be inferred as Prop in:\n$this"
                }
                PropProd(Assumption(label, tp), term)
            }
            else -> throw IllegalArgumentException("Product cannot be made out of $tp, context:\n$this")
        }
    }

    /**
     * Adds a lambda abstraction with it's corresponding [Prod] type
     *
     * This ensures Lam
     * `E[Γ] ⊢ λx:T.t : ∀x:T,U`
     */
    fun lam(name: String, varType: Sort, term: Sort, prodType: Sort): Lam {
        require(this[name] == null) {
            "Re-declared variable $name for lambda expression in:\n$this"
        }
        require(wellFormed(prodType)) {
            "Type $prodType is not well formed in context:\n$this"
        }
        require(wellFormed(varType)) {
            "Type $varType is not well formed in context:\n$this"
        }
        require(withVar(Assumption(name, term)) { wellFormed(term) && term.hasType(prodType.type) })
        require(prodType is Prod) {
            "Type mismatch, $term should be Product type here:\n$this"
        }
        return Lam(Assumption(name, term), term, prodType)
    }

    /**
     * Applies the given [value] to [lam]
     */
    fun app(lambda: String, value: String): App {
        require(this[lambda] != null || this[lambda] !is Lam) {
            "Function $lambda not found here:\n$this"
        }
        require(this[value] != null || this[value] !is Variable) {
            "Variable $value not found here:\n$this"
        }
        return App(this[lambda] as Lam, this[value] as Variable)
    }


    /**
     * Uses Let..in.. notation to make a temporal declaration in a [term] that does not appear in [Context]
     *
     * This ensures Let
     */
    fun let(definition: Definition, term: Sort, type: Sort): Let {
        require(this[definition.name] == null) {
            "Re-declared ${this[definition.name]} here:\n$this"
        }
        require(withVar(definition){term.hasType(type)})
        return Let(definition, term, type)
    }
}

/**
 * Context, which shall be well founded on it's own (cannot depend on other contexts)
 */
class GlobalEnvironment(vararg declaration: Variable) : Context(declaration.toMutableList()) {
    override operator fun get(name: String): Sort? = declarations.find { it.name == name }

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

    /**
     * `E[Γ] ⊢ c:T`
     */
    fun Const(c: String, t: Sort): Boolean = this[c] != null
            && this[c]!!.type == t

    override fun has(label: String, type: Sort): Boolean = Const(label, type)
    override fun fresh(label: String): Boolean = declarations.none { it.name == label }
    override val usedLabels: List<String>
        get() = declarations.map { it.name }.toList()

}

/**
 * A context that depends on another(global) context hiding constants on re-declaration
 *
 * If a variable is not found locally, the "global" context will be queried
 */
class LocalContext(private val context: GlobalEnvironment, vararg declaration: Variable) :
    Context(declaration.toMutableList()) {
    override operator fun get(name: String): Sort? =
        declarations.find { it.name == name } ?: context[name]

    operator fun plus(other: LocalContext): LocalContext {
        require(declarations.none { it in other.declarations }) {
            "The two contexts share name on an element, which is unacceptable"
        }
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

    /**
     *  `E[Γ] ⊢ x:T`
     */
    fun Var(x: String, t: Sort): Boolean = declarations.firstOrNull { it.name == x }?.type == t

    override fun has(label: String, type: Sort): Boolean =
        Var(label, type) || context.Const(label, type)

    override fun fresh(label: String): Boolean =
        declarations.none { it.name == label } && context.fresh(label)
    override val usedLabels: List<String>
        get() = declarations.map { it.name }.union(context.usedLabels).toList()
}
