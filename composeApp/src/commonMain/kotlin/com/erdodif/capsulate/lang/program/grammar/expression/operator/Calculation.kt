package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.withRawValue
import com.erdodif.capsulate.lang.program.grammar.expression.withValue
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState


@KParcelize
data class UnaryCalculation<T : Value, R : Value>(
    val param: Exp<R>,
    val operator: UnaryOperator<T, R>
) : Exp<T> {

    override fun evaluate(context: Env): Either<T, PendingExpression<Value, T>> =
        param.withRawValue(context) { a: R -> operator.operation(context, a) }

    override fun toString(state: ParserState, parentStrength: Int): String =
        if (parentStrength >= operator.bindingStrength)
            when (operator.fixation) {
                Fixation.PREFIX -> "(${operator.label}${
                    param.toString(state, operator.bindingStrength)
                })"

                Fixation.POSTFIX -> "(${
                    param.toString(state, operator.bindingStrength)
                }${operator.label})"
            }
        else when (operator.fixation) {
            Fixation.PREFIX -> "${operator.label}${param.toString(state, operator.bindingStrength)}"
            Fixation.POSTFIX -> "${
                param.toString(state, operator.bindingStrength)
            }${operator.label}"
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
            Left(
                operator.operation(context, a, b)
            )
        }

    override fun toString(state: ParserState, parentStrength: Int): String =
        if (parentStrength >= operator.bindingStrength)
            "(${first.toString(state, operator.bindingStrength)} ${operator.label} ${
                second.toString(state, operator.bindingStrength)
            })"
        else
            "${first.toString(state, operator.bindingStrength)} ${operator.label} ${
                second.toString(state, operator.bindingStrength)
            }"

    override fun toString(): String = "BIN($first {${operator.label}} $second)"
}
