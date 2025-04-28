package com.erdodif.capsulate.lang.program.evaluation

import co.touchlab.kermit.Logger
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.PendingExpression
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.FunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.fold
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed interface EvaluationResult : KParcelable

@Serializable
class FunctionState<R : Value, T : Value>
private constructor(
    val env: Environment,
    @SerialName("self_call")
    override val call: FunctionCall<R>,
    @SerialName("self_function")
    override val function: Function<R>,
    @SerialName("self_on_value")
    override val onValue: @Serializable (Environment.(R) -> Either<T, PendingExpression<Value, T>>)
) : PendingExpression<R, T>(call, function, onValue) {
    constructor(env: Environment, exp: PendingExpression<R, T>) :
            this(env, exp.call, exp.function, exp.onValue)

    val context = EvaluationContext(
        env, when (function.body.size) {
            0 -> null
            1 -> function.body[0]
            else -> EvalSequence(function.body)
        }
    )

    val head: Statement?
        get() = context.head

    @Suppress("UNCHECKED_CAST")
    fun step(): Either<R, EvaluationResult> {
        if (context.head != null) {
            context.step()
        }
        return when {
            context.returnValue != null -> Left(context.returnValue as R)
            context.error != null -> Right(AbortEvaluation.logged(context.error!!))
            context.head == null -> Right(
                AbortEvaluation.logged(
                    "Function execution ended, no return statement found on the way."
                )
            ).also { context.error = it.value.reason }

            else -> Right(SingleStatement(context.head!!))
        }
    }

    override fun toString(): String {
        return "FuncState(" +
                "calls on: ${call.name}, " +
                "error: ${context.error}, " +
                "value: ${context.returnValue}, " +
                "head: ${context.head})"
    }
}

@KParcelize
@Serializable
data class PendingMethodEvaluation(
    val method: Method, val context: EvaluationContext
) : EvaluationResult, Statement(match = MatchPos.ZERO) {
    @OptIn(ExperimentalUuidApi::class)
    override val id: Uuid
        get() = context.head?.id ?: super.id
    val head: Statement?
        get() = context.head

    constructor(method: Method, env: ProxyEnv) :
            this(method, EvaluationContext(env, EvalSequence(method.program)))

    override fun evaluate(env: Environment): EvaluationResult {
        context.step()
        return when {
            context.error != null -> AbortEvaluation(context.error!!)
            head == null -> Finished
            else -> this
        }
    }

    override fun Formatting.format(state: ParserState): Int = error("formatted method: $this")
}

@KParcelize
@Serializable
data class PendingFunctionEvaluation<T : Value>(
    val expression: PendingExpression<Value, T>,
    val callback: @Serializable Environment.(T) -> EvaluationResult
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

                is Left -> expression.onValue(env, result.value).fold({ callback(env, it) })
                { PendingFunctionEvaluation(it, callback) }
            }

            else -> expression.call.values.joinAll(env) {
                val newEnv = Env(
                    env.functions,
                    env.methods,
                    expression.function.parameters.zip(it)
                        .map { (param, value) -> Parameter(param.id, value.type, value) }
                        .toMutableList(),
                    env.seed)
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
        if (expression is FunctionState) expression.context.getCallStack(expression.call.name)
        else emptyList()
}

@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class EvalSequence(val statements: ArrayDeque<Statement>) : EvaluationResult,
    Statement(match = MatchPos.ZERO) {
    override val id: Uuid
        get() = statements.firstOrNull()?.id ?: Uuid.NIL

    constructor(vararg statements: Statement) : this(ArrayDeque(statements.toList()))
    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Environment): EvaluationResult {
        val result = (if (statements.isNotEmpty()) statements.removeAt(0) else Skip(MatchPos.ZERO))
            .evaluate(env) // https://youtrack.jetbrains.com/issue/KT-71375/Prevent-Kotlins-removeFirst-and-removeLast-from-causing-crashes-on-Android-14-and-below-after-upgrading-to-Android-API-Level-35#:~:text=removeLast()%20extension%20functions.,running%20Android%2014%20or%20lower
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
@Serializable
data class SingleStatement(val next: Statement) : EvaluationResult

@KParcelize
@Serializable
data object Finished : EvaluationResult

@KParcelize
@Serializable
data class ReturnEvaluation<T : Value>(val value: T) : EvaluationResult

@KParcelize
@Serializable
data class AbortEvaluation(val reason: String = "") : EvaluationResult {
    companion object {
        fun logged(reason: String): AbortEvaluation =
            AbortEvaluation(reason).apply { Logger.e { reason } }
    }
}

@KParcelize
@Serializable
data class AtomicEvaluation(val statements: List<Statement>) : EvaluationResult

@KParcelize
@Serializable
data class ParallelEvaluation(val entries: List<Statement>) : EvaluationResult
