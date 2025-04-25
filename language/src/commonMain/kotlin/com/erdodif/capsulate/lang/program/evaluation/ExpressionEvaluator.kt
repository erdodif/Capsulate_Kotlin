package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmSerializableLambda
import kotlin.math.exp

@Serializable
interface ExpressionEvaluator<in V : Value> {
    fun onValue(context: Environment, value: V): EvaluationResult

    fun Exp<V>.join(context: Environment): EvaluationResult = try {
        when (val result = evaluate(context)) {
            is Left -> onValue(context, result.value)
            is Right -> PendingFunctionEvaluation<V>(result.value, ::onValue)
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression: $e")
    }
}

@Serializable
interface AdditionalExpressionEvaluator<in V : Value, in T> {
    fun onValue(context: Environment, value: V, extra: T): EvaluationResult

    fun Exp<V>.join(context: Environment, extra: T): EvaluationResult = try {
        when (val result = evaluate(context)) {
            is Left -> onValue(context, result.value, extra)
            is Right -> PendingFunctionEvaluation<V>(result.value)
            @JvmSerializableLambda { context, value ->
                onValue(context, value, extra)
            }
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression: $e")
    }
}

@Serializable
interface ExpressionListEvaluator<V : Value> : ExpressionEvaluator<V> {

    override fun onValue(context: Environment, value: V): EvaluationResult {
        drop(1).joinAll(context) { values ->
            onEvery(this, buildList { add(it); addAll(values) })
        }
    }

    fun onEvery(context: Environment, values: List<V>): EvaluationResult

    fun <T : Value> List<Exp<T>>.joinAll(
        context: Environment,
        onEvery: @Serializable Environment.(List<T>) -> EvaluationResult
    ): EvaluationResult = if (isEmpty()) onEvery(context, emptyList()) else
        this[0].join(context) @JvmSerializableLambda {
            this@joinAll.drop(1).joinAll(context) { values ->
                onEvery(this, buildList { add(it); addAll(values) })
            }
        }
}

@Serializable
interface AdditionalExpressionListEvaluator<in V : Value, in T> :
    AdditionalExpressionEvaluator<V, Pair<List<Exp<V>>, T>> {

    override fun onValue(
        context: Environment,
        value: V,
        extra: Pair<List<Exp<V>>, T>
    ): EvaluationResult =
        extra.first.joinAll(context, buildList { add(value); addAll(extra.first) } to extra.second)


    fun onEvery(context: Environment, values: List<V>, extra: T): EvaluationResult

    fun List<Exp<V>>.joinAll(
        context: Environment,
        extra: Pair<List<V>, T>
    ): EvaluationResult = if (isEmpty()) onEvery(context, extra.first, extra.second) else
        this[0].join(context, drop(1) to extra.second)
}