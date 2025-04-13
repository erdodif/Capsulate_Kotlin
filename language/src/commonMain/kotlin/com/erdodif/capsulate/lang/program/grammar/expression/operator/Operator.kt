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
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.asum
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
abstract class UnaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String = "~",
    override val operatorParser: Parser<*>,
    val fixation: Fixation,
    val operation: @Serializable Environment.(R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    @Suppress("UNCHECKED_CAST")
    private fun producer(a: Exp<*>): UnaryCalculation<T, R> =
        UnaryCalculation<T, R>(a as Exp<R>, this@UnaryOperator)

    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> = when (fixation) {
        Fixation.PREFIX ->
            orEither(right(operatorParser, strongerParser) / { producer(it) }, strongerParser)

        Fixation.POSTFIX ->
            orEither(left(strongerParser, operatorParser) / { producer(it) }, strongerParser)
    }

    override fun toString(): String = when(fixation){
        Fixation.PREFIX -> "$label\$ $bindingStrength"
        Fixation.POSTFIX -> "\$$label $bindingStrength"
    }

    abstract fun type(paramType: Type): Type
}

@KParcelize
abstract class BinaryOperator<T : Value, R : Value>(
    override val bindingStrength: Int,
    override val label: String,
    override val operatorParser: Parser<*>,
    val association: Association,
    val operation: @Serializable Environment.(R, R) -> T
) : Operator<Exp<T>>(bindingStrength, label, operatorParser), KParcelable {

    @Suppress("UNCHECKED_CAST")
    private fun producer(a: Exp<*>, b: Exp<*>): BinaryCalculation<T, R> =
        BinaryCalculation<T, R>(a as Exp<R>, b as Exp<R>, this@BinaryOperator)

    override fun parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> = when (association) {
        Association.LEFT -> leftAssoc(::producer, strongerParser, operatorParser)
        Association.RIGHT -> rightAssoc(::producer, strongerParser, operatorParser)
        Association.NONE -> nonAssoc(::producer, strongerParser, operatorParser)
    }

    override fun toString(): String = when(association){
        Association.LEFT -> "\$$label\$ L$bindingStrength"
        Association.RIGHT -> "\$$label\$ R$bindingStrength"
        Association.NONE -> "\$$label\$ N$bindingStrength"
    }
    abstract fun type(firstType: Type, secondType: Type): Type

}

fun <T : Value> List<BinaryOperator<T, *>>.build(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> =
    when {
        isEmpty() -> strongerParser
        else -> this.drop(1).build(first().parse(strongerParser))
    }

@Suppress("UNCHECKED_CAST")
fun <T : Value> List<Operator<Exp<T>>>.parse(strongerParser: Parser<Exp<T>>): Parser<Exp<T>> = {
    val pres = filter { it is UnaryOperator<*, *> && it.fixation == Fixation.PREFIX }
        .map { it as UnaryOperator<T, *> }
    val posts = filter { it is UnaryOperator<*, *> && it.fixation == Fixation.POSTFIX }
        .map { it as UnaryOperator<T, *> }
    val bis = filter { it is BinaryOperator<*, *> }
        .map { it as BinaryOperator<T, *> }
    val preResults =
        many(asum(*pres.mapIndexed { i, op -> op.operatorParser / { i } }.toTypedArray()))()
    val midResult = bis.build(strongerParser)()
    val postResults =
        many(asum(*posts.mapIndexed { i, op -> op.operatorParser / { i } }.toTypedArray()))()

    when (midResult) {
        is Pass<*> -> {
            var res = midResult.value
            preResults.value.reversed().forEach { index ->
                res = UnaryCalculation<Value, Value>(
                    res as Exp<Value>,
                    pres[index] as UnaryOperator<Value, Value>
                )
            }
            postResults.value.reversed().forEach { index ->
                res = UnaryCalculation<Value, Value>(
                    res as Exp<Value>,
                    posts[index] as UnaryOperator<Value, Value>
                )
            }
            Pass(res as Exp<T>, this, MatchPos(preResults.match.start, postResults.match.end))
        }

        is Fail -> midResult
    }
}

inline fun <T> List<T>.split(crossinline predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    var second: List<T>
    val first: List<T> = first@ buildList<T> {
        second = second@ buildList<T> {
            this.forEach { if (predicate(it)) first@ add(it) else second@ add(it) }
        }
    }
    return first to second
}

