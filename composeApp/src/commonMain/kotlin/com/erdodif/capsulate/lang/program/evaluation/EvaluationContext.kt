package com.erdodif.capsulate.lang.program.evaluation

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import kotlin.random.Random

@KParcelize
data class EvaluationContext(
    var env: Env,
    private var currentStatement: Statement?,
    val seed: Int = Random.Default.nextInt(),
) : KParcelable {
    @KIgnoredOnParcel
    val random = Random(seed)
    val entries: ArrayList<Statement> = arrayListOf()
    var functionOngoing: PendingFunctionEvaluation<*>? = null
        private set
    private var atomicOngoing: EvaluationContext? = null
    val head: Statement?
        get() = functionOngoing?.head ?: atomicOngoing?.head ?: currentStatement

    var error: String? = null
    var returnValue: Value? = null

    fun step(): EvaluationContext {
        val function = functionOngoing
        if (function != null) {
            when (val result = function.evaluate(env)) {
                is PendingFunctionEvaluation<*> -> this.functionOngoing = result
                else -> {
                    this.functionOngoing = null
                    handleResult(result)
                }
            }
            return this.copy()
        }
        val atomic = atomicOngoing
        if (atomic?.head == null) {
            this.atomicOngoing = null
        }
        if (atomic != null) {
            atomic.step()
            env = atomic.env
            return this.copy()
        }
        val current = currentStatement
        if (current == null) {
            if (entries.isEmpty()) {
                return this.copy()
            } else {
                currentStatement = entries.removeAt(random.nextInt(entries.size))
            }
        } else if (current is EvalSequence) {
            val next = current.statements[0]
            if (next is Parallel || next is Atomic) {
                current.evaluate(env)
                if (current.statements.count() > 0) {
                    entries.add(current)
                }
                currentStatement = next
            }
        }
        handleResult(current!!.evaluate(env))
        return this.copy()
    }

    private fun handleResult(stack: EvaluationResult) {
        when (stack) {
            is Finished -> {}
            is ReturnEvaluation<*> -> returnValue = stack.value
            is AbortEvaluation -> {
                error = stack.reason
                entries.clear()
                atomicOngoing = null
                currentStatement = null
            }

            is EvalSequence -> entries.add(stack)
            is AtomicEvaluation -> atomicOngoing =
                EvaluationContext(env, EvalSequence(stack.statements), seed)

            is SingleStatement -> entries.add(stack.next)
            is ParallelEvaluation -> entries.addAll(stack.entries)
            is PendingFunctionEvaluation<*> -> functionOngoing = stack
        }
        currentStatement =
            if (entries.isEmpty()) null else entries.removeAt(random.nextInt(entries.size))
    }
}
