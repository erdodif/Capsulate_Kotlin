package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.leftAssoc
import com.erdodif.capsulate.lang.program.grammar.nonAssoc
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.rightAssoc

enum class Association {
    LEFT,
    RIGHT,
    NONE
}

enum class Fixation {
    PREFIX,
    POSTFIX
}

interface Calculation<out T, in R> {
    fun evaluate(context: R): T
}

data class UnaryCalculation<T, R>(
    val param: Calculation<T, R>,
    val label: String = "~",
    val fixation: Fixation,
    val operation: R.(T) -> T
) : Calculation<T, R> {
    constructor(first: Calculation<T, R>, operator: UnaryOperator<T, R>) : this(
        first,
        operator.label,
        operator.fixation,
        operator.operation
    )

    override fun evaluate(context: R): T = context.operation(param.evaluate(context))
}

data class BinaryCalculation<T, R>(
    val first: Calculation<T, R>,
    val second: Calculation<T, R>,
    val label: String = "∘",
    val operation: R.(T, T) -> T
) : Calculation<T, R> {
    constructor(
        first: Calculation<T, R>,
        second: Calculation<T, R>,
        operator: BinaryOperator<T, R>
    ) : this(
        first,
        second,
        operator.label,
        operator.operation
    )

    override fun evaluate(context: R): T =
        context.operation(first.evaluate(context), second.evaluate(context))
}


open class UnaryOperator<T, R>(
    bindingStrength: Int,
    label: String = "~",
    operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: R.(T) -> T
) : Operator<Calculation<T, R>>(bindingStrength, label, operatorParser) {
    override fun parse(strongerParser: Parser<Calculation<T, R>>): Parser<Calculation<T, R>> = {
        orEither(when (fixation) {
            Fixation.PREFIX -> right(operatorParser, strongerParser) / {
                UnaryCalculation(it, this@UnaryOperator)
            }

            Fixation.POSTFIX -> left(strongerParser, operatorParser) / {
                UnaryCalculation(it, this@UnaryOperator)
            }
        }, strongerParser)()
    }
}

open class BinaryOperator<T, R>(
    bindingStrength: Int,
    label: String = "∘",
    operatorParser: Parser<*>,
    val association: Association,
    val operation: R.(T, T) -> T
) : Operator<Calculation<T, R>>(bindingStrength, label, operatorParser) {
    override fun parse(strongerParser: Parser<Calculation<T, R>>): Parser<Calculation<T, R>> =
        when (association) {
            Association.LEFT -> leftAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                strongerParser,
                operatorParser
            )

            Association.RIGHT -> rightAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                strongerParser,
                operatorParser
            )

            Association.NONE -> nonAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                strongerParser,
                operatorParser
            )
        }
}
