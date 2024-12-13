package com.erdodif.capsulate.lang.specification.coc

/**
 * Replaces a function application with it's value
 *
 * `E[Γ] ⊢ ((λx:T. t) u) ▷β t{x/u}`
 */
fun Context.reduceBeta(lam: Lam, variable: Variable): Sort {
    return lam.term.rewrite(lam.variable.name, variable)
}

/**
 *
 */
fun Context.reduceIota(): Sort = TODO("Inductive sets are not implemented for this to make sense")

/**
 * Replaces var/const definitions with their corresponding value
 */
fun Context.reduceDelta(variable: Definition): Sort = variable.value

/**
 * Replaces the variable occurrences based on the let..in.. notation
 */
fun Context.reduceZeta(let: Let): Sort = let.term.rewrite(let.definition.name, let.definition)

private class nameIterator() : Iterator<String> {
    var i: Int = 0
    override fun hasNext(): Boolean = true

    override fun next(): String {
        i++
        var j = i
        val sb = StringBuilder()
        while (j != 0) {
            sb.append( 'a'.plus(j % 26))
            j %= 26
        }
        return sb.toString()
    }

}

/**
 * Wraps any prod type with a trivial lambda abstraction
 *
 * Eta reduction is not defined due to it's incompatibility with the cic type system
 */
fun Context.expandEta(lambda: Lam): Sort {
    val ite = nameIterator()
    var next = ite.next()
    while (!fresh(next)) {
        next = ite.next()
    }
    val variable = assume(next, lambda.productType)
    return Lam(variable, App(lambda, variable), lambda.productType)
}

/**
 * `E[Γ] ⊢ t▷u`
 */
fun Context.norm(sort: Sort): Sort = when(sort){
    is Definition -> norm(reduceDelta(sort))
    is App -> norm(reduceBeta(sort.f, sort.x))
    is Let -> norm(reduceZeta(sort))
    is Lam -> norm(expandEta(sort))
    is Assumption -> sort // can't be simpler
    is Prod -> sort
    Prop -> sort
    Set -> sort
    is Type -> sort
}

object Conversion {

}