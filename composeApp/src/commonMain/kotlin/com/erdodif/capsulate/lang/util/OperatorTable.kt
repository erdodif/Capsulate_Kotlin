package com.erdodif.capsulate.lang.util

abstract class Operator<T>(
    open val bindingStrength: Int,
    open val label: String,
    open val operatorParser: Parser<*>
) {
    operator fun compareTo(other: Operator<T>) =
        this.bindingStrength.compareTo(other.bindingStrength)

    abstract fun parse(strongerParser: Parser<T>): Parser<T>
}

class OperatorTable<T>(private var operators: List<Operator<T>>) {
    constructor(vararg operators: Operator<T>) : this(operators.toList())

    init {
        operators = operators.sortedBy { it.bindingStrength }
    }

    operator fun get(index: Int, atomParser: Parser<T>): Parser<T> =
        if (index > operators.lastIndex - 1) {
            atomParser
        } else {
            operators[index].parse(get(index + 1, atomParser))
        }

    fun parser(atomParser: Parser<T>): Parser<T> = this[0, atomParser]

    fun verboseParser(atomParser: Parser<T>): Parser<T> = {
        val stringBuilder = StringBuilder()
        var lastResult: ParserResult<T> = fail("OperatorTable empty.")
        for (i in operators.indices) {
            lastResult = get(i, atomParser)()
            if (lastResult is Pass<*>) break
            lastResult as Fail
            stringBuilder.append("\n'${operators[i].label}' (strength: ${operators[i].bindingStrength}) failed at ${lastResult.state.position} with reason: ${lastResult.reason}")
        }
        lastResult
    }
}