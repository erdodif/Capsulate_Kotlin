package com.erdodif.capsulate.lang.specification.coc.context

import com.erdodif.capsulate.lang.specification.coc.App
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Definition
import com.erdodif.capsulate.lang.specification.coc.Lam
import com.erdodif.capsulate.lang.specification.coc.Let
import com.erdodif.capsulate.lang.specification.coc.Prod
import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.PropProd
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.SetProd
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.TypeProd
import com.erdodif.capsulate.lang.specification.coc.Variable
import com.erdodif.capsulate.lang.specification.coc.norm

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
        require(declarations.none{variable.name == it.name}){
            "Cannot enrich the context with ${variable}, conflicting in context:\n$this"
        }
        declarations.add(variable)
    }

    /**
     * Defines `[name] := [value] : [type]`
     *
     * This ensures W-Global/Local-Def as well as Var/Const
     */
    fun define(name: String, value: Sort, type: Sort): Definition {
        require(declarations.none{ it.name == name}) {
            "Cannot enrich the context with ${name}, conflicting ${
                if (this[name] is Assumption) "assumption"
                else "definition"
            } in context:\n$this"
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
        require(declarations.none{ it.name == name}) {
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
