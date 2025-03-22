package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.util.Parser

sealed class Operator<T : Exp<*>>(
    open val bindingStrength: Int,
    open val label: String,
    open val operatorParser: Parser<*>
) {
    operator fun compareTo(other: Operator<T>) =
        this.bindingStrength.compareTo(other.bindingStrength)

    abstract fun parse(strongerParser: Parser<T>): Parser<T>
}

class OperatorTable<T : Value>(private var operators: Map<Int, List<Operator<Exp<T>>>>) {
    constructor(vararg operators: Operator<Exp<T>>) : this(operators.groupBy { it.bindingStrength })
    constructor(operators: List<Operator<Exp<T>>>) : this(operators.groupBy { it.bindingStrength })

    operator fun get(index: Int, atomParser: Parser<Exp<T>>): Parser<Exp<T>> =
        (if (index >= operators.keys.count()) {
            atomParser
        } else if (operators[operators.keys.sorted()[index]] == null ||
            operators[operators.keys.sorted()[index]]!!.isEmpty()
        ) {
            get(index + 1, atomParser)
        } else {
            operators[operators.keys.sorted()[index]]!!.parse<T>(
                get(index + 1, atomParser) as Parser<Exp<T>>
            )
        })

    fun parser(atomParser: Parser<Exp<T>>): Parser<Exp<T>> = this[0, atomParser]
}
