package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.leftAssoc
import com.erdodif.capsulate.lang.program.grammar.nonAssoc
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.rightAssoc
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.withRawValue
import com.erdodif.capsulate.lang.program.grammar.expression.withValue
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.div
import kotlinx.serialization.Serializable

enum class Association {
    LEFT,
    RIGHT,
    NONE
}

enum class Fixation {
    PREFIX,
    POSTFIX
}

@KParcelize
data class UnaryCalculation<T : Value, R : Value>(
    val param: Exp<R>,
    val operator: UnaryOperator<T, R>
) : Exp<T> {

    override fun evaluate(context: Env): Either<T, PendingExpression<Value, T>> =
        param.withRawValue(context) { a: R -> operator.operation(context, a) }

    override fun toString(state: ParserState): String =
        when (operator.fixation) {
            Fixation.PREFIX -> "${operator.label}${param.toString(state)}"
            Fixation.POSTFIX -> "${param.toString(state)}${operator.label}"
        }
}

@KParcelize
data class BinaryCalculation<T : Value, R : Value>(
    val first: Exp<R>,
    val second: Exp<R>,
    val operator: BinaryOperator<T, R>
) : Exp<T>, KParcelable {

    override fun evaluate(context: Env): Either<T, PendingExpression<Value, T>> =
        (first to second).withValue(context) { a: R, b: R ->
            Left(
                operator.operation(context, a, b)
            )
        }

    override fun toString(state: ParserState): String =
        "${operator.label}(${first.toString(state)} ${operator.label} ${second.toString(state)})"
}


@KParcelize
open class UnaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String = "~",
    override val operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: @Serializable Env.(R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    @Suppress("UNCHECKED_CAST")
    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
        orEither(
            when (fixation) {
                Fixation.PREFIX -> right(operatorParser, strongerParser) / {
                    UnaryCalculation<T, R>(it as Exp<R>, this@UnaryOperator)
                }

                Fixation.POSTFIX -> left(strongerParser, operatorParser) / {
                    UnaryCalculation<T, R>(it as Exp<R>, this@UnaryOperator)
                }
            }, strongerParser)
}

@KParcelize
open class BinaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String,
    override val operatorParser: Parser<*>,
    val association: Association,
    val operation: @Serializable Env.(R, R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    @Suppress("UNCHECKED_CAST")
    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
        when (association) {
            Association.LEFT -> leftAssoc(
                { a, b ->
                    BinaryCalculation<T, R>(
                        a as Exp<R>,
                        b as Exp<R>,
                        this@BinaryOperator
                    )
                },
                strongerParser,
                operatorParser
            )

            Association.RIGHT -> rightAssoc(
                { a, b ->
                    BinaryCalculation<T, R>(
                        a as Exp<R>,
                        b as Exp<R>,
                        this@BinaryOperator
                    )
                },
                strongerParser,
                operatorParser
            )

            Association.NONE -> nonAssoc(
                { a, b ->
                    BinaryCalculation<T, R>(
                        a as Exp<R>,
                        b as Exp<R>,
                        this@BinaryOperator
                    )
                },
                strongerParser,
                operatorParser
            )
        }
}
