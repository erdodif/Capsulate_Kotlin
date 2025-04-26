package com.erdodif.capsulate.lang.program.grammar.expression

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.KRawValue
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.toInt
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlinx.serialization.Serializable

@KParcelize
@Serializable
data class Index(val id: String, val indexers: @KRawValue List<Exp<Value>>) : Exp<Value> {
    constructor(id: String, vararg indexes: Exp<Value>) : this(id, indexes.toList())

    override fun getType(assumptions: Map<String, Type>): Type =
        when (val assume = assumptions[id]) {
            is ARRAY -> assume.contentType
            else -> NEVER
        }

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: Environment): Either<Value, PendingExpression<Value, Value>> =
        indexers.withRawValue(context) { indexers ->
            if (indexers.any { it !is VNum<*> || it.value !is BigInteger }) {
                error(
                    "Cannot index with non-numbers! Got: " + indexers
                        .mapIndexed { index, a -> a to index }
                        .filter { it.first !is VNum<*> }
                        .joinToString(postfix = ".") { (value, index) -> "${value.type} at ${index + 1}" }

                )
            }
            indexers as List<VNum<BigInteger>>
            if (indexers.any { it.value.compareTo(Int.MAX_VALUE.toBigInteger()) > 0 }) {
                error(
                    "Cannot index with numbers larger than the platform's maximum number " +
                            "(which is ${Int.MAX_VALUE})! Got: " + indexers
                        .mapIndexed { index, a -> a to index }
                        .filter { it.first.value.compareTo(Int.MAX_VALUE.toBigInteger()) > 0 }
                        .joinToString(postfix = ".") { (value, index) -> "${value.value} at ${index + 1}" }

                )
            }
            when (val param = context.get(id)) {
                is Left -> if (indexers.isEmpty()) param.value else
                    when (val value =
                        param.value) {
                        is VArray<*> -> value.get(indexes = indexers.map { (it.value as BigInteger).toInt() }
                            .toIntArray())

                        else -> error("Can't index non-array ($id : ${value.type})")
                    }

                is Right -> error("Can't index on missing parameter '$id'")
            }
        }

    override fun toString(state: ParserState, parentStrength: Int): String =
        id + indexers.joinToString(separator = "") { "[${it.toString(state, parentStrength)}]" }
}
