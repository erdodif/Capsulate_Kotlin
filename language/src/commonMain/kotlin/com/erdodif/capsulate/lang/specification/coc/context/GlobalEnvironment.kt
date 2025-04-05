package com.erdodif.capsulate.lang.specification.coc.context

import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Type
import com.erdodif.capsulate.lang.specification.coc.Variable


/**
 * Context, which shall be well founded on it's own (cannot depend on other contexts)
 */
class GlobalEnvironment(vararg declaration: Variable) : Context(declaration.toMutableList()) {
    override operator fun get(name: String): Variable? = declarations.find { it.name == name }

    override fun wellFormed(type: Sort): Boolean {
        return type is Variable && this[type.name] != null || type is Type || type == Set || type == Prop
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
        if (declarations.isNotEmpty()) {
            sb.deleteRange(sb.length - 2, sb.length - 1)
        }
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
