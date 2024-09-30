package com.erdodif.capsulate.lang.grammar.operator

import com.erdodif.capsulate.lang.grammar.Exp
import com.erdodif.capsulate.lang.grammar.Value
import com.erdodif.capsulate.lang.grammar.left
import com.erdodif.capsulate.lang.grammar.leftAssoc
import com.erdodif.capsulate.lang.grammar.nonAssoc
import com.erdodif.capsulate.lang.grammar.orEither
import com.erdodif.capsulate.lang.grammar.right
import com.erdodif.capsulate.lang.grammar.rightAssoc
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.div

enum class Association {
    LEFT,
    RIGHT,
    NONE
}

enum class Fixation {
    PREFIX,
    POSTFIX
}

class UnaryCalculation(
    val param: Exp<*>,
    val label: String = "∘",
    val fixation: Fixation,
    val operation: Env.(Exp<*>) -> Value
) : Exp<Value> {
    constructor(first: Exp<*>, operator: UnaryOperator) : this(
        first,
        operator.label,
        operator.fixation,
        operator.operation
    )

    override fun evaluate(env: Env): Value {
        env.run {
            return operation(param)
        }
    }

    override fun toString(state: ParserState): String =
        if (fixation == Fixation.PREFIX) "($label ${param.toString(state)})" else "(${
            param.toString(
                state
            )
        } $label)"
}

class BinaryCalculation(
    val first: Exp<*>,
    val second: Exp<*>,
    val label: String = "∘",
    val operation: Env.(Exp<*>, Exp<*>) -> Value
) : Exp<Value> {
    constructor(first: Exp<*>, second: Exp<*>, operator: BinaryOperator) : this(
        first,
        second,
        operator.label,
        operator.operation
    )

    override fun evaluate(env: Env): Value {
        env.run {
            return operation(first, second)
        }
    }

    override fun toString(state: ParserState): String =
        "(${first.toString(state)} $label ${second.toString(state)})"
}

abstract class Operator(
    val bindingStrength: Int,
    val label: String,
    val operatorParser: Parser<*>
) {
    operator fun compareTo(other: Operator) =
        this.bindingStrength.compareTo(other.bindingStrength)

    abstract fun parse(weakerParser: Parser<Exp<*>>): Parser<Exp<*>>
}

open class UnaryOperator(
    bindingStrength: Int,
    label: String = "~",
    operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: Env.(Exp<*>) -> Value
) : Operator(bindingStrength, label, operatorParser) {
    override fun parse(weakerParser: Parser<Exp<*>>): Parser<Exp<*>> = orEither(when (fixation) {
        Fixation.PREFIX -> right(operatorParser, weakerParser) / {
            UnaryCalculation(it, this@UnaryOperator)
        }

        Fixation.POSTFIX -> left(weakerParser, operatorParser) / {
            UnaryCalculation(it, this@UnaryOperator)
        }
    }, weakerParser)
}

open class BinaryOperator(
    bindingStrength: Int,
    label: String,
    operatorParser: Parser<*>,
    val association: Association,
    val operation: Env.(Exp<*>, Exp<*>) -> Value
) : Operator(bindingStrength, label, operatorParser) {
    override fun parse(weakerParser: Parser<Exp<*>>): Parser<Exp<*>> =
        when (association) {
            Association.LEFT -> leftAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                weakerParser,
                operatorParser
            )

            Association.RIGHT -> rightAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                weakerParser,
                operatorParser
            )

            Association.NONE -> nonAssoc(
                { a, b -> BinaryCalculation(a, b, this@BinaryOperator) },
                weakerParser,
                operatorParser
            )
        }
}

class OperatorTable(private var operators: List<Operator> = builtInOperators) {
    constructor(vararg operators: Operator) : this(operators.toList())

    init {
        operators = operators.sortedByDescending { it.bindingStrength }
    }

    operator fun get(index: Int, atomParser: Parser<Exp<*>>): Parser<Exp<*>> =
        if (index > operators.lastIndex - 1) {
            atomParser
        } else {
            operators[index].parse(get(index + 1, atomParser))
        }

    companion object {
        val builtInOperators =
            arrayListOf(Add, Sub, Sign, Mul, Div, And, Or, Not, Equal)
    }

    fun parser(atomParser: Parser<Exp<*>>): Parser<Exp<*>> = this[0, atomParser]

    fun verboseParser(atomParser: Parser<Exp<*>>): Parser<Exp<*>> = {
        val stringBuilder = StringBuilder()
        var lastResult: ParserResult<Exp<*>> = fail("OperatorTable empty.")
        for (i in operators.indices) {
            lastResult = get(i, atomParser)()
            if (lastResult is Pass<*>) break
            lastResult as Fail
            stringBuilder.append("\n'${operators[i].label}' (strength: ${operators[i].bindingStrength}) failed at ${lastResult.state.position} with reason: ${lastResult.reason}")
        }
        lastResult
    }
}