package com.erdodif.capsulate.lang.specification.coc.context

import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.Variable

/**
 * A context that depends on another(global) context hiding constants on re-declaration
 *
 * If a variable is not found locally, the "global" context will be queried
 */
class LocalContext(private val context: GlobalEnvironment, vararg declaration: Variable) :
    Context(declaration.toMutableList()) {
    override operator fun get(name: String): Variable? =
        declarations.find { it.name == name } ?: context[name]

    operator fun plus(other: LocalContext): LocalContext {
        require(declarations.none { it in other.declarations }) {
            "The two contexts share name on an element, which is unacceptable"
        }
        return LocalContext(context, *(declarations + other.declarations).toTypedArray())
    }

    override fun wellFormed(type: Sort): Boolean {
        return (type is Variable &&
                (this[type.name] != null || context[type.name] != null))
                || type is Type || type == Set || type == Prop
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
        if(declarations.isNotEmpty()){
            sb.deleteRange(sb.length - 2, sb.length - 1)
        }
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
