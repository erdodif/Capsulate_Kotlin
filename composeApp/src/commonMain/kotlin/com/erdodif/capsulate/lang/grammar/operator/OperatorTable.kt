package com.erdodif.capsulate.lang.grammar.operator

import com.erdodif.capsulate.lang.Env
import com.erdodif.capsulate.lang.Exp
import com.erdodif.capsulate.lang.Parser
import com.erdodif.capsulate.lang.ParserResult
import com.erdodif.capsulate.lang.ParserState
import com.erdodif.capsulate.lang.Value
import com.erdodif.capsulate.lang.leftAssoc
import com.erdodif.capsulate.lang.nonAssoc
import com.erdodif.capsulate.lang.pExp
import com.erdodif.capsulate.lang.rightAssoc

enum class Association {
    LEFT,
    RIGHT,
    NONE
}

enum class Fixation {
    PREFIX,
    INFIX,
    POSTFIX,
    MIXFIX
}

class UnaryCalculation(
    val param: Exp<*>,
    val label: String = "∘",
    val fixation: Fixation,
    val operation: Env.(Exp<*>) -> Value
) : Exp<Value> {
    override fun evaluate(env: Env): Value {
        env.run {
            return operation(param)
        }
    }

    override fun toString(state: ParserState): String =
        if (fixation == Fixation.PREFIX) label + param.toString(state) else param.toString(state) + label
}

class BinaryCalculation(
    val first: Exp<*>,
    val second: Exp<*>,
    val label: String = "∘",
    val operation: Env.(Exp<*>, Exp<*>) -> Value
) : Exp<Value> {
    override fun evaluate(env: Env): Value {
        env.run {
            return operation(first, second)
        }
    }

    override fun toString(state: ParserState): String =
        first.toString(state) + label + second.toString(state)
}

abstract class Operator(
    val bindingStrength: Int,
    val label: String,
    val parser: Parser<*>
){
    operator fun compareTo(other: Operator) =
        this.bindingStrength.compareTo(other.bindingStrength)
    abstract fun ParserState.parse(): ParserResult<Exp<*>>
}

open class UnaryOperator(
    bindingStrength: Int,
    label: String,
    parser: Parser<*>,
    val fixation: Fixation,
    val operation: Env.(Exp<*>) -> Value
) : Operator(bindingStrength, label, parser) { // parse TODO
    override fun ParserState.parse(): ParserResult<Exp<*>> {
        TODO("Not yet implemented")
    }
}

open class BinaryOperator(
    bindingStrength: Int,
    label: String,
    parser: Parser<*>,
    val fixation: Fixation,
    val association: Association,
    val operation: Env.(Exp<*>, Exp<*>) -> Value
) : Operator(bindingStrength, label, parser) {
    override fun ParserState.parse(): ParserResult<Exp<*>> = when (association) {
        Association.LEFT -> leftAssoc(
            { a, b -> BinaryCalculation(a, b, label, operation) },
            pExp,
            parser
        )()

        Association.RIGHT -> rightAssoc(
            { a, b -> BinaryCalculation(a, b, label, operation) },
            pExp,
            parser
        )()

        Association.NONE -> nonAssoc(
            { a, b -> BinaryCalculation(a, b, label, operation) },
            pExp,
            parser
        )()
    }
}

typealias OperatorTable = List<BinaryOperator>