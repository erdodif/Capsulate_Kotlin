@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.asum

abstract class Operator<T>(
    open val bindingStrength: Int,
    open val label: String,
    open val operatorParser: Parser<*>
) {
    operator fun compareTo(other: Operator<T>) =
        this.bindingStrength.compareTo(other.bindingStrength)

    abstract fun parse(strongerParser: Parser<T>): Parser<T>
}

class OperatorTable<T>(var operators: List<Operator<T>>, atomParser: Parser<T>) {
    constructor(vararg operators: Operator<T>, atomParser: Parser<T>) : this(operators.toList(), atomParser)
    val parsers: ArrayDeque<Parser<T>> = ArrayDeque(operators.size +1)

    init {
        operators = operators.sortedBy { it.bindingStrength }
        parsers.add(0, atomParser)
        operators.reversed().mapIndexed { i, operator ->
            parsers.add(0, operator.parse(parsers.first()))
        }
    }


    inline fun parser(): Parser<T> = asum<T>(*parsers.toTypedArray())

    inline fun verboseParser(): Parser<T> = {
        val stringBuilder = StringBuilder()
        var lastResult: ParserResult<T> = fail("OperatorTable empty.")
        for (i in operators.indices) {
            lastResult = parsers[i]()
            if (lastResult is Pass<*>) break
            lastResult as Fail
            stringBuilder.append("\n'${operators[i].label}' " +
                    "(strength: ${operators[i].bindingStrength}) " +
                    "failed at ${lastResult.state.position} " +
                    "with reason: ${lastResult.reason}")
        }
        lastResult
    }
}