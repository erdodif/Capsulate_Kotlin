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
import com.erdodif.capsulate.lang.program.grammar.expression.DependentExp
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
@Serializable
data class UnaryCalculation<T : Value, R : Value>(
    val param: Exp<R>,
    val label: String = "∘",
    val fixation: Fixation,
    val operation: @Serializable Env.(R) -> T
) : Exp<T> {
    constructor(first: Exp<R>, operator: UnaryOperator<T, R>) : this(
        first,
        operator.label,
        operator.fixation,
        operator.operation
    )

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: Env): Either<T, DependentExp<*, T>> =
        param.withRawValue(context) {
            context.operation(it)
        }

    override fun toString(state: ParserState): String =
        when (fixation) {
            Fixation.PREFIX -> "$label${param.toString(state)}"
            Fixation.POSTFIX -> "${param.toString(state)}$label"
        }
}

@KParcelize
@Serializable
data class BinaryCalculation<T : Value, R : Value>(
    val first: Exp<R>,
    val second: Exp<R>,
    val label: String = "∘",
    val operation: @Serializable Env.(R, R) -> T
) : Exp<T>, KParcelable {
    constructor(first: Exp<R>, second: Exp<R>, operator: BinaryOperator<T, R>) : this(
        first,
        second,
        operator.label,
        operator.operation
    )

    override fun evaluate(context: Env): Either<T, DependentExp<*, T>> =
        /*first.withRawValue(context){
            println(it.toString())
            it as T
        }*/
        (first to second).withValue(context) { a, b ->
            Left(operation(a, b))
        }

    override fun toString(state: ParserState): String =
        "$label(${first.toString(state)} $label ${second.toString(state)})"
}


@KParcelize
@Serializable
open class UnaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String = "~",
    override val operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: @Serializable Env.(R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    @Suppress("UNCHECKED_CAST")
    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
        orEither(when (fixation) {
            Fixation.PREFIX -> right(operatorParser, strongerParser) / {
                UnaryCalculation<T, R>(it as Exp<R>, this@UnaryOperator)
            }

            Fixation.POSTFIX -> left(strongerParser, operatorParser) / {
                UnaryCalculation<T, R>(it as Exp<R>, this@UnaryOperator)
            }
        }, strongerParser)
}

@KParcelize
@Serializable
open class BinaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String,
    override val operatorParser: Parser<*>,
    val association: Association,
    val operation: @Serializable Env.(R, R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    //@Suppress("UNCHECKED_CAST")
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
