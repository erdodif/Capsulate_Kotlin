package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Statement
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface EvaluationResult : KParcelable

@OptIn(ExperimentalUuidApi::class)
@KParcelize
data class EvalSequence(val statements: ArrayDeque<Statement>) : EvaluationResult, Statement() {
    override val id: Uuid
        get() = statements.first().id

    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Env): EvaluationResult {
        val result = statements.removeFirst().evaluate(env)
        if (statements.isEmpty()) {
            return result
        }
        return when (result) {
            is AbortEvaluation -> result
            is SingleStatement -> {
                statements.addFirst(result.next)
                this
            }

            is EvalSequence -> {
                statements.addAll(0, result.statements)
                this
            }

            else -> if (statements.isEmpty()) {
                Finished
            } else if (statements.count() == 1) {
                SingleStatement(statements.first())
            } else {
                this
            }

        }
    }
}

@KParcelize
data class SingleStatement(val next: Statement) : EvaluationResult

@KParcelize
data object Finished : EvaluationResult

@KParcelize
data class AbortEvaluation(val reason: String = "") : EvaluationResult

@KParcelize
data class AtomicEvaluation(val statements: List<Statement>) : EvaluationResult

@KParcelize
data class ParallelEvaluation(val entries: List<Statement>) : EvaluationResult

@KParcelize
data class EvaluationContext(
    var env: Env,
    private var currentStatement: Statement?,
    val seed: Int = Random.nextInt(),
) : KParcelable {
    @KIgnoredOnParcel
    val random = Random(seed)
    val entries: ArrayList<Statement> = arrayListOf()
    private var atomicOngoing: EvaluationContext? = null
    val head: Statement?
        get() = atomicOngoing?.head ?: currentStatement

    fun step(): EvaluationContext {
        if (atomicOngoing?.head == null) {
            atomicOngoing = null
        }
        if (atomicOngoing != null) {
            atomicOngoing!!.step()
            env = atomicOngoing!!.env
            return this.copy()
        } else
            if (currentStatement == null) {
                if (entries.isEmpty()) {
                    return this.copy()
                } else {
                    currentStatement = entries.removeAt(random.nextInt(entries.size))
                }
            } else if (currentStatement is EvalSequence) {
                val next = (currentStatement as EvalSequence).statements[0]
                if (next is Parallel || next is Atomic) {
                    (currentStatement as EvalSequence).evaluate(env)
                    if ((currentStatement as EvalSequence).statements.count() > 0) {
                        entries.add(currentStatement as EvalSequence)
                    }
                    currentStatement = next
                }
            }
        when (val stack = currentStatement!!.evaluate(env)) {
            Finished -> {}
            is AbortEvaluation -> TODO()
            is EvalSequence -> entries.add(stack)
            is AtomicEvaluation -> atomicOngoing =
                EvaluationContext(env, EvalSequence(stack.statements), seed)

            is SingleStatement -> entries.add(stack.next)
            is ParallelEvaluation -> entries.addAll(stack.entries)
        }

        currentStatement =
            if (entries.isEmpty()) null else entries.removeAt(random.nextInt(entries.size))
        return this.copy()
    }
}
