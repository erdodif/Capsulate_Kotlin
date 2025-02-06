package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Statement
import kotlin.random.Random

sealed interface EvaluationResult : KParcelable

@KParcelize
data class EvalSequence(val statements: ArrayDeque<Statement>) : EvaluationResult, Statement {
    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Env): EvaluationResult {
        val result = statements.removeFirst().evaluate(env)
        if (statements.isEmpty()) {
            return result
        }
        if (statements.size == 1 && result is Finished) {
            return SingleStatement(statements.first())
        }
        return when (result) {
            is Finished -> this
            is AbortEvaluation -> result
            is SingleStatement -> {
                statements.addFirst(result.next)
                this
            }
            is EvalSequence -> {
                statements.addAll(0, result.statements)
                this
            }
        }
    }
}

@KParcelize data class SingleStatement(val next: Statement) : EvaluationResult

@KParcelize data object Finished : EvaluationResult

@KParcelize data class AbortEvaluation(val reason: String = "") : EvaluationResult

@KParcelize
data class EvaluationContext(
    val env: Env,
    private var currentStatement: Statement?,
    val seed: Int = Random.nextInt(),
) : KParcelable {
    val random = Random(seed)
    val entries: ArrayList<Statement> = arrayListOf()
    private var atomicOngoing: Atomic? = null
    val head: Statement?
        get() = atomicOngoing ?: currentStatement

    fun step(): EvaluationContext {
        if (atomicOngoing == null && currentStatement == null) {
            if (entries.isEmpty()) {
                return this
            } else {
                currentStatement = entries.removeAt(random.nextInt(entries.size))
            }
        }
        val stack =
            if (atomicOngoing != null) {
                val oldStatement = atomicOngoing!!
                atomicOngoing = null
                oldStatement.evaluate(env)
            } else {
                currentStatement!!.evaluate(env)
            }
        when (stack) {
            Finished -> {}
            is AbortEvaluation -> TODO()
            is EvalSequence -> entries.add(stack)
            is SingleStatement -> {
                if (stack.next is Atomic) {
                    atomicOngoing = stack.next
                } else {
                    entries.add(stack.next)
                }
            }
        }
        currentStatement =
            if (entries.isEmpty()) null else entries.removeAt(random.nextInt(entries.size))
        return this
    }
}
