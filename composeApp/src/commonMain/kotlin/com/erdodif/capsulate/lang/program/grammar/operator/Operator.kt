package com.erdodif.capsulate.lang.program.grammar.operator

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.leftAssoc
import com.erdodif.capsulate.lang.program.grammar.nonAssoc
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.rightAssoc
import com.erdodif.capsulate.lang.util.Association
import com.erdodif.capsulate.lang.util.Calculation
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Fixation
import com.erdodif.capsulate.lang.util.Operator
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.div
import kotlinx.serialization.Serializable

@KParcelize
@Serializable
data class UnaryCalculation(
    val param: Exp<Value>,
    val label: String = "∘",
    val fixation: Fixation,
    val operation: @Serializable Env.(Exp<Value>) -> Value
) : Calculation<Value, Env>, Exp<Value>  {
    constructor(first: Exp<Value>, operator: UnaryOperator) : this(
        first,
        operator.label,
        operator.fixation,
        operator.operation
    )

    override fun evaluate(context: Env): Value = context.operation(param)

    override fun toString(state: ParserState): String =
        when(fixation){
            Fixation.PREFIX -> "$label${param.toString(state)}"
            Fixation.POSTFIX -> "${param.toString(state)}$label"
        }
}

@KParcelize
@Serializable
data class BinaryCalculation(
    val first: Exp<Value>,
    val second: Exp<Value>,
    val label: String = "∘",
    val operation: @Serializable Env.(Exp<Value>, Exp<Value>) -> Value
) : Calculation<Value, Env> , Exp<Value> , KParcelable{
    constructor(first: Exp<Value>, second: Exp<Value>, operator: BinaryOperator) : this(
        first,
        second,
        operator.label,
        operator.operation
    )

    override fun evaluate(context: Env): Value = context.operation(first, second)

    override fun toString(state: ParserState): String =
        "${first.toString(state)} $label ${second.toString(state)}"
}


@KParcelize
@Serializable
open class UnaryOperator(
    override val bindingStrength: Int,
    override val label: String = "~",
    override val operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: @Serializable Env.(Exp<Value>) -> Value
) : Operator<Exp<Value>>(bindingStrength, label, operatorParser), KParcelable{
    override fun parse(strongerParser: Parser<Exp<Value>>): Parser<Exp<Value>> =
        orEither(when (fixation) {
            Fixation.PREFIX -> right(operatorParser, strongerParser) / {
                UnaryCalculation(it, this@UnaryOperator)
            }

            Fixation.POSTFIX -> left(strongerParser, operatorParser) / {
                UnaryCalculation(it, this@UnaryOperator)
            }
        }, strongerParser)
}

@KParcelize
@Serializable
open class BinaryOperator(
    override val bindingStrength: Int,
    override val label: String,
    override val operatorParser: Parser<*>,
    val association: Association,
    val operation: @Serializable Env.(Exp<Value>, Exp<Value>) -> Value
) : Operator<Exp<Value>>(bindingStrength, label, operatorParser), KParcelable {
    override fun parse(strongerParser: Parser<Exp<Value>>): Parser<Exp<Value>> =
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
