package com.erdodif.capsulate.lang.specification.coc

// https://cs.ru.nl/~freek/courses/tt-2019/public/coq-manual-4.4.pdf
// Left at Var typing rule

/**
 * A set of Terms that makes sure that type-inference rules are enforced during construction
 *
 * A context can tell whether it's well formed or not
 */
abstract class Context(protected val declarations: MutableList<Sort>) {
    operator fun get(index: Int): Sort = declarations[index]
    abstract operator fun get(name: String): Sort?

    abstract fun wellFormed(type: Sort): Boolean
    abstract fun wellFormed(): Boolean

    /**
     * Adds the given definition [name] := [value] : [type]
     *
     * This ensures W-Global/Local-Def as well as Var/Const
     */
    fun define(name: String, value: String, type: String): Definition {
        require(this[name] == null) {
            "The name '$name' is already within this context!\n$this"
        }
        require(this[type] != null) {
            "Type definition not found here:\n$this"
        }
        val tp = this[type]!!
        val source = this[value]
        require(source != null) {
            "Definition for value ($source) cannot be found here:\n$this"
        }
        require(source.hasType(tp)) {
            "Type of ($source : ${source.type}) cannot be inferred as $tp!\n$this"
        }
        val definition = Definition(name, value, tp)
        declarations.add(definition)
        return definition
    }

    /**
     * Adds the given assumption [name] : [type]
     *
     * This ensures W-Global/Local-Assum as well as Var/Const
     */
    fun assume(name: String, type: String): Assumption {
        require(this[name] == null) {
            "The name '$name' is already within this context!\n$this"
        }
        require(this[type] != null) {
            "Type definition not found here:\n$this"
        }
        val tp = this[type]!!
        require(wellFormed(tp) || tp in this) {
            "Given ($type) is not well formed within this context!\n$this"
        }
        val assumption = Assumption(name, tp)
        declarations.add(assumption)
        return assumption
    }

    /**
     * Defines a Product type
     *
     * This ensures Prod-Prop, Prod-Set, Prod-Type
     */
    fun prod(label:String, type: String): Prod{
        //TODO("This is Type inference territory E[Γ]⊢∀x:T, U:s  s∈{Prop,Set,Type}")
        require(this[type] != null) {
            "Type definition not found here:\n$this"
        }
        val tp = this[type]!!
        require(this[label] == null){
            "Type re-declared here:\n$this"
        }
        val prod = when(tp){
            is Set -> SetProd(label)
            is Type -> TypeProd(label, tp.level)
            is Prop -> PropProd(label)
            else -> throw IllegalArgumentException("Product cannot be made out of $tp, context:\n$this")
        }
        declarations.add(prod)
        return prod
    }

    /**
     * Adds a lambda abstraction with it's corresponding [Prod] type
     *
     * This ensures Lam
     */
    fun lam(name: String, type: String, term: String): Lam {
        //TODO("This is Type inference territory E[Γ::(x:T)]⊢U:Type(i)")
        require(this[name] == null) {
            "Re-declared variable $name for lambda expression in:\n$this"
        }
        require(this[type] != null){
            "Type for $name ($type) not found here:\n$this"
        }
        require(this[term] != null){
            "Type $term not found here:\n$this"
        }
        require(this[term] is Prod){
            "Type mismatch, $term should be Product type here:\n$this"
        }
        val prodType = this[term] as Prod
        val tp = this[type]!!
        val lam = Lam(name, tp, prodType)
        declarations.add(lam)
        return lam
    }

    /**
     * Applies the given [value] to [lam]
     *
     * This ensures App
     */
    private fun app(lambda: String, value: String): App{
        //TODO("This is rewrite territory E[Γ]⊢(t u):T{x/u}")
        require(this[lambda] != null || this[lambda] !is Lam){
            "Function $lambda not found here:\n$this"
        }
        require(this[value] != null || this[value] !is Variable){
            "Variable $value not found here:\n$this"
        }
        val app = App(this[lambda] as Lam, this[value] as Variable)
        declarations.add(app)
        return app
    }


    /**
     * Uses Let..in.. notation to make a temporal declaration in a [term] that does not appear in [Context]
     *
     * This ensures Let
     */
    private fun let(definition: Definition, term: Sort): Let{
        //TODO("This is rewrite and Type inference territory E[Γ]⊢let x:=t:T in u:U{x/t}")
        require(this[definition.name] == null){
            "Re-declared ${this[definition.name]} here:\n$this"
        }
        val let = Let(definition, term)
        declarations.add(let)
        return let
    }

    /**
     * Determines whether the given [sort] is in this context
     */
    operator fun contains(sort: Sort): Boolean = when (sort) {
        is Variable -> get(sort.name) != null
        is Type -> true
        is Set -> true
        is Prop -> true
        is App -> TODO()
        is Lam -> TODO()
        is Let -> TODO()
        is Prod -> TODO()
    }

    fun Sort.hasType(other: Sort): Boolean {
        TODO("This is Convertibility territory")
    }
}

/**
 * Context, which shall be well founded on it's own (cannot depend on other contexts)
 */
class GlobalEnvironment(vararg declaration: Sort) : Context(declaration.toMutableList()) {
    override operator fun get(name: String): Sort? = declarations.find {it is Variable && it.name == name}

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
}

/**
 * A context that depends on another(global) context hiding constants on re-declaration
 *
 * If a variable is not found locally, the "global" context will be queried
 */
class LocalContext(private val context: GlobalEnvironment, vararg declaration: Sort) :
    Context(declaration.toMutableList()) {
    override operator fun get(name: String): Sort? =
        declarations.find { it is Variable && it.name == name } ?: context[name]

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
}
