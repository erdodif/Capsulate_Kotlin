package com.erdodif.capsulate.lang.specification.coc

interface CNum
data object CZero : CNum
data object COne : CNum

enum class CTwo : CNum {
    tt,
    ff;

    fun Tt() = when (this) {
        tt -> COne
        ff -> CZero
    }
}

infix fun <R> Any.X(second: R): Pair<Any, R> = Pair(this, second)

typealias Name = String
typealias Scope = List<Name>

val done
    get() = Cover(listOf(), listOf(), listOf())

data class Cover(private val _left: Scope, private val _right: Scope, private val _global: Scope) {
    val left: Cover  // TODO, _global[0] might be stg else
        get() = copy(_left + _global[0], _right, _global)
    val right: Cover
        get() = copy(_left, _right + _global[0], _global)
    val both: Cover
        get() = copy(_left + _global[0], _right + _global[0], _global)
}

/*sealed class Term(val scope: Scope) {
    data class Var(val name: Name) : Term(listOf(name))
    data class Lam(val name: Name, val expression: Term) : Term(expression.scope + name)
    data class Lam_(val name: Name, val expression: Term) : Term(expression.scope) // Parameter not used
    data class App(val cover: Cover, val left: Term, val right: Term) :
        Term(left.scope + right.scope) // TODO

}

val ex: Term = Term.Lam("f", Term.Lam("x", Term.App(done.left.right, Term.Var("f"), Term.Var("x"))))
*/
