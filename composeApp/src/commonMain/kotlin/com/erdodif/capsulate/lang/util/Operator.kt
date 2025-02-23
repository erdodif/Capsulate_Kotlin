package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.expression.DependentExp
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Operator
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.leftAssoc
import com.erdodif.capsulate.lang.program.grammar.nonAssoc
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.rightAssoc

/*
data class UnaryCalculation<T : Value>(
    val param: Exp<T>,
    val label: String = "~",
    val fixation: Fixation,
    val operation: Env.(T) -> Either<T, DependentExp<*, T>>
) : Exp<T> {
    constructor(first: Exp<T>, operator: UnaryOperator<T>) : this(
        first,
        Operator.label,
        operator.fixation,
        operator.operation
    )

    override fun evaluate(context: Env): Either<T, DependentExp<*, T>> =
        when (val result = param.evaluate(context)) {
            is Left -> context.operation(result.value)
            is Right -> Right(result.value + { this.operation(it) })
        }

    override fun toString(state: ParserState): String = "$label${param.toString(state)}"

}

data class BinaryCalculation<T : Value>(
    val first: Exp<T>,
    val second: Exp<T>,
    val label: String = "∘",
    val operation: Env.(T, T) -> Either<T, DependentExp<*, T>>
) : Exp<T> {
    constructor(
        first: Exp<T>,
        second: Exp<T>,
        operator: BinaryOperator<T>
    ) : this(
        first,
        second,
        Operator.label,
        operator.operation
    )

    override fun evaluate(context: Env): Either<T, DependentExp<*, T>> =

        context.operation(first.evaluate(context), second.evaluate(context))


    override fun toString(state: ParserState): String =
        "${first.toString(state)} $label ${second.toString(state)}"
}


open class UnaryOperator<T : Value>(
    bindingStrength: Int,
    label: String = "~",
    operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: Env.(T) -> Either<T, DependentExp<*, T>>
) : Operator<Exp<T>>(bindingStrength, label, operatorParser) {
    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> = {
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

open class BinaryOperator<T : Value>(
    bindingStrength: Int,
    label: String = "∘",
    operatorParser: Parser<*>,
    val association: Association,
    val operation: Env.(T, T) -> Either<T, DependentExp<*, T>>
) : Operator<Exp<T>>(bindingStrength, label, operatorParser) {
    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
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
*/
