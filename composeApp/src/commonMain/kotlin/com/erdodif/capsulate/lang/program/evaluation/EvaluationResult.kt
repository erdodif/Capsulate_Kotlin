package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.type
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.fold
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface EvaluationResult : KParcelable

class FunctionState<R : Value, T : Value>(
    val env: Environment,
    exp: PendingExpression<R, T>
) : PendingExpression<R, T>(exp.call, exp.onValue) {
    val context = EvaluationContext(
        env, when (call.function.body.size) {
            0 -> null
            1 -> call.function.body[0]
            else -> EvalSequence(call.function.body)
        }
    )

    val head: Statement?
        get() = context.head

    @OptIn(ExperimentalUuidApi::class)
    @Suppress("UNCHECKED_CAST")
    fun step(): Either<T, EvaluationResult> {
        if (context.returnValue != null) {
            return Left(context.returnValue as T)
        }
        if (context.head != null) {
            context.step()
        } else {
            context.error = "Function execution ended, no return statement found on the way."
        }
        return when {
            context.returnValue != null -> onValue(env, context.returnValue as R)
                .fold({ Left(it) }) { Right(PendingFunctionEvaluation(it) { ReturnEvaluation(it) }) }

            context.error != null -> Right(AbortEvaluation(context.error!!))
            else -> Right(SingleStatement(context.head!!))
        }
    }

    override fun toString(): String {
        return "FuncState(calls on: ${call.function.name}, error: ${context.error}, value: ${context.returnValue}, head: ${context.head})"
    }
}

@KParcelize
data class PendingMethodEvaluation(
    val method: Method,
    val context: EvaluationContext
) : EvaluationResult, Statement(match = MatchPos.ZERO) {
    @OptIn(ExperimentalUuidApi::class)
    override val id: Uuid
        get() = context.head?.id ?: super.id
    val head: Statement?
        get() = context.head

    constructor(method: Method, env: ProxyEnv) :
            this(method, EvaluationContext(env, EvalSequence(method.program), env.seed))

    override fun evaluate(env: Environment): EvaluationResult {
        context.step()
        return when {
            context.error != null -> AbortEvaluation(context.error!!)
            context.head == null -> Finished
            else -> this
        }
    }

    override fun Formatting.format(state: ParserState): Int = error("formatted method: $this")
}

@KParcelize
data class PendingFunctionEvaluation<T : Value>(
    val expression: PendingExpression<Value, T>,
    val callback: Environment.(T) -> EvaluationResult
) : EvaluationResult, Statement(match = MatchPos.ZERO) {
    val head: Statement?
        get() = if (expression is FunctionState) expression.head else Skip(MatchPos.ZERO)

    override fun evaluate(env: Environment): EvaluationResult = try {
        when (expression) {
            is FunctionState -> when (val result = expression.step()) {
                is Right -> when (result.value) {
                    is AbortEvaluation -> result.value
                    else -> this.copy()
                }

                is Left -> expression.onValue(env, result.value).fold({
                    callback(env, result.value)
                }) { PendingFunctionEvaluation(it, callback) }
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
                PendingFunctionEvaluation(FunctionState(newEnv, expression), callback)
            }
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression! $e")
    }

    operator fun plus(transform: (EvaluationResult) -> EvaluationResult): PendingFunctionEvaluation<T> =
        copy(callback = { transform(callback(it)) })

    /**
     * This is an internal representation, formatting makes no sense
     */
    override fun Formatting.format(state: ParserState): Int = error("formatted function: $this")

    fun getCallStack(): List<EvaluationContext.StackTraceEntry> =
        if (expression is FunctionState) expression.context.getCallStack(expression.call.function.name)
        else emptyList()
}

@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class EvalSequence(val statements: ArrayDeque<Statement>) : EvaluationResult,
    Statement(match = MatchPos.ZERO) {
    override val id: Uuid
        get() = statements.first().id

    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Environment): EvaluationResult {
        val result = statements.removeFirst().evaluate(env)
        return if (statements.isEmpty()) {
            result
        } else {
            handleResult(result)
        }
    }

    private fun handleResult(result: EvaluationResult): EvaluationResult = when (result) {
        is PendingFunctionEvaluation<*> -> result + ::handleResult

        is AbortEvaluation -> result

        is SingleStatement -> {
            statements.addFirst(result.next)
            this
        }

        is EvalSequence -> {
            statements.addAll(0, result.statements)
            this
        }

        is PendingMethodEvaluation -> {
            statements.add(0, result)
            this
        }

        else -> when {
            statements.isEmpty() -> Finished
            statements.count() == 1 -> SingleStatement(statements.first())
            else -> this
        }
    }

    /**
     * This is an internal representation, formatting makes no sense
     */
    override fun Formatting.format(state: ParserState): Int = error("formatted: $this")
}

@KParcelize
data class SingleStatement(val next: Statement) : EvaluationResult

@KParcelize
data object Finished : EvaluationResult

@KParcelize
data class ReturnEvaluation<T : Value>(val value: T) : EvaluationResult

@KParcelize
data class AbortEvaluation(val reason: String = "") : EvaluationResult

@KParcelize
data class AtomicEvaluation(val statements: List<Statement>) : EvaluationResult

@KParcelize
data class ParallelEvaluation(val entries: List<Statement>) : EvaluationResult
