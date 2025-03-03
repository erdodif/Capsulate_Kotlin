package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.DependentExp
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Holder
import com.erdodif.capsulate.lang.program.grammar.expression.RawValue
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.type
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Right
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface EvaluationResult : KParcelable

class FunctionState<R : Value, T : Value>(
    val env: Env,
    exp: DependentExp<R, T>,
) : DependentExp<R, T>(exp.call, exp.onValue) {
    val context = EvaluationContext(env, EvalSequence(call.function.body))
    val head: Statement?
        get() = context.head

    @OptIn(ExperimentalUuidApi::class)
    @Suppress("UNCHECKED_CAST")
    fun step(): Either<T, EvaluationResult> {
        context.step()
        return when {
            context.returnValue != null && context.returnValue is RawValue -> {
                when (val result =
                    onValue(env, (context.returnValue as RawValue<R>).get(context.env))) {
                    is Left -> result
                    is Right -> Right(
                        DependentEvaluation(result.value) { Return(Holder(it), match = MatchPos.ZERO) }
                    )
                }
            }


            context.returnValue != null -> Right(DependentEvaluation(this) {
                Return(context.returnValue!!.evaluate(this) as Exp<T>, match = MatchPos.ZERO)
            })

            context.error != null -> Right(AbortEvaluation(context.error!!))
            context.head == null -> Right(AbortEvaluation("Function execution ended, no return statement found on the way."))
            else -> Right(SingleStatement(context.head!!))
        }
    }

    override fun toString(): String {
        return "FuncState(calls on: ${call.function.name}, error: ${context.error}, value: ${context.returnValue}, head: ${context.head})"
    }
}

@KParcelize
data class DependentEvaluation<T : Value>(
    val expression: DependentExp<*, T>,
    val callback: Env.(T) -> EvaluationResult
) : EvaluationResult, Statement(match = MatchPos.ZERO) {
    val head: Statement?
        get() = if (expression is FunctionState) expression.head else Skip(MatchPos.ZERO)

    override fun evaluate(env: Env): EvaluationResult = try {
        when (expression) {
            is FunctionState -> when (val result = expression.step()) {
                is Right -> when (result.value) {
                    is AbortEvaluation -> result.value
                    else -> this.copy()
                }

                is Left -> callback(env, result.value)
            }

            else -> expression.call.values.joinAll(env) {
                val newEnv = Env(
                    env.functions,
                    env.methods,
                    expression.call.function.parameters.zip(it)
                        .map { (param, value) -> Parameter(param.id, value.type(), value) }
                        .toMutableList(),
                    env.seed
                )
                DependentEvaluation(FunctionState(newEnv, expression), callback)
            }
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression! $e")
    }

    operator fun plus(transform: (EvaluationResult) -> EvaluationResult): DependentEvaluation<T> =
        copy(callback = { transform(callback(it)) })
}

@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class EvalSequence(val statements: ArrayDeque<Statement>) : EvaluationResult,
    Statement(match = MatchPos.ZERO) {
    override val id: Uuid
        get() = statements.first().id

    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Env): EvaluationResult {
        val result = statements.removeFirst().evaluate(env)
        return if (statements.isEmpty()) {
            result
        } else {
            handleResult(result)
        }
    }

    private fun handleResult(result: EvaluationResult): EvaluationResult = when (result) {
        is DependentEvaluation<*> -> result + ::handleResult

        is AbortEvaluation -> result

        is SingleStatement -> {
            statements.addFirst(result.next)
            this
        }

        is EvalSequence -> {
            statements.addAll(0, result.statements)
            this
        }

        else -> when {
            statements.isEmpty() -> Finished
            statements.count() == 1 -> SingleStatement(statements.first())
            else -> this
        }
    }
}

@KParcelize
data class SingleStatement(val next: Statement) : EvaluationResult

@KParcelize
data object Finished : EvaluationResult

@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class Return<T : Value> @OptIn(ExperimentalUuidApi::class) constructor(
    val value: Exp<T>,
    override val id: Uuid = Uuid.random(),
    override val match: MatchPos
) : Statement(id, match), EvaluationResult {
    @OptIn(ExperimentalUuidApi::class)
    override fun evaluate(env: Env): EvaluationResult =
        if (value is RawValue) this else value.join(env) { this@Return.copy(value = Holder(it)) }
}

@KParcelize
data class AbortEvaluation(val reason: String = "") : EvaluationResult

@KParcelize
data class AtomicEvaluation(val statements: List<Statement>) : EvaluationResult

@KParcelize
data class ParallelEvaluation(val entries: List<Statement>) : EvaluationResult
