@file:Suppress("NOTHING_TO_INLINE")

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

    override fun toString(): String = when (operator.fixation) {
        Fixation.PREFIX -> "UN({${operator.label}} $param)"
        Fixation.POSTFIX -> "UN($param {${operator.label}})"
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
            Left(operator.operation(context, a, b))
        }

    override fun toString(state: ParserState): String =
        "${first.toString(state)} ${operator.label} ${second.toString(state)}"
    //TODO: pls use binding strength to use braces '(' and ')'

    override fun toString(): String = "BIN($first {${operator.label}} $second)"
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
    fun producer(a: Exp<*>): UnaryCalculation<T, R> =
        UnaryCalculation<T, R>(a as Exp<R>, this@UnaryOperator)

    @Suppress("OVERRIDE_BY_INLINE")
    final override inline fun parse(crossinline strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
        when (fixation) {
            Fixation.PREFIX -> orEither(
                right(operatorParser, strongerParser) / { producer(it) },
                strongerParser
            )

            Fixation.POSTFIX -> orEither(
                left(strongerParser, operatorParser) / { producer(it) },
                strongerParser
            )
        }
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
    fun producer(a: Exp<*>, b: Exp<*>): BinaryCalculation<T, R> =
        BinaryCalculation<T, R>(a as Exp<R>, b as Exp<R>, this@BinaryOperator)

    @Suppress("OVERRIDE_BY_INLINE")
    final override inline fun parse(crossinline strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
        when (association) {
            Association.LEFT -> leftAssoc(::producer, strongerParser, operatorParser)
            Association.RIGHT -> rightAssoc(::producer, strongerParser, operatorParser)
            Association.NONE -> nonAssoc(::producer, strongerParser, operatorParser)
        }

}
