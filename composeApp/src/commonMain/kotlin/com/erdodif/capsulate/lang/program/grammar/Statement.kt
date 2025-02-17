@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.AbortEvaluation
import com.erdodif.capsulate.lang.util.AtomicEvaluation
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvalSequence
import com.erdodif.capsulate.lang.util.EvaluationResult
import com.erdodif.capsulate.lang.util.Finished
import com.erdodif.capsulate.lang.util.ParallelEvaluation
import com.erdodif.capsulate.lang.util.SingleStatement
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class Statement(open val id: Uuid = Uuid.random()) : KParcelable {
    abstract fun evaluate(env: Env): EvaluationResult
    override fun equals(other: Any?): Boolean = other is Statement && other.id == id
    override fun hashCode(): Int = id.hashCode()
}

@KParcelize
data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<out Statement>,
    val statementsFalse: ArrayList<out Statement>,
    override val id: Uuid
) : Statement(id) {
    constructor(
        condition: Exp<*>,
        statementsTrue: List<Statement>,
        statementsFalse: List<Statement>
    ) : this(condition, ArrayList(statementsTrue), ArrayList(statementsFalse), Uuid.random())

    constructor(
        condition: Exp<*>,
        statementsTrue: ArrayList<Statement>,
        statementsFalse: ArrayList<Statement>
    ) : this(condition, statementsTrue, statementsFalse, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        val result = condition.evaluate(env)
        return if (result is VBool) {
            if (result.value) {
                EvalSequence(statementsTrue)
            } else {
                EvalSequence(statementsFalse)
            }
        } else {
            AbortEvaluation("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class When(
    val blocks: MutableList<Pair<Exp<*>, List<Statement>>>,
    val elseBlock: List<Statement>? = null,
    override val id: Uuid
) : Statement(id) {
    constructor(block: List<Pair<Exp<*>, List<Statement>>>, elseBlock: List<Statement>? = null) :
            this(block.toMutableList(), elseBlock, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        val source =
            if (env.deterministic) blocks.removeFirst()
            else blocks.removeAt(env.random.nextInt(blocks.size))
        return when (val result = source.first.evaluate(env)) {
            is VBool -> {
                when {
                    result.value -> EvalSequence(source.second)
                    blocks.isEmpty() ->
                        AbortEvaluation("When conditions exhausted, Abort happens by definition")

                    else -> SingleStatement(this)
                }
            }

            else -> {
                AbortEvaluation("Condition must be a logical expression")
            }
        }
    }
}

@KParcelize
data class Skip(override val id: Uuid) : Statement(id) {
    constructor() : this(Uuid.random())

    override fun evaluate(env: Env) = Finished
}

@KParcelize
data class Abort(override val id: Uuid) : Statement(id) {
    constructor() : this(Uuid.random())

    override fun evaluate(env: Env) = AbortEvaluation("Abort has been called!")
}

abstract class Loop(
    open val condition: Exp<*>, open val statements: ArrayList<out Statement>, override val id: Uuid
) : Statement(id)

@KParcelize
data class While(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid
) : Loop(condition, statements, id) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>) :
            this(condition, statements, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult =
        when (val result = condition.evaluate(env)) {
            is VBool -> {
                if (result.value) {
                    EvalSequence(statements + this)
                } else {
                    Finished
                }
            }

            else -> AbortEvaluation("Condition must be a logical expression")
        }
}

@KParcelize
data class DoWhile(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid
) : Loop(condition, statements, id) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>) :
            this(condition, statements, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult =
        EvalSequence(statements + While(condition, statements))
}

@KParcelize
data class Assign(val label: String, val value: Exp<*>, override val id: Uuid) : Statement(id) {
    constructor(label: String, value: Exp<*>) : this(label, value, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        env.set(label, value.evaluate(env))
        return Finished
    }
}

@KParcelize
data class Select(
    val label: String, val set: String /*Specification: Type*/,
    override val id: Uuid
) : Statement(id) {
    constructor(label: String, set: String) : this(label, set, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        TODO("Implement 'Zsák objektum'")
    }
}

@KParcelize
data class ParallelAssign(
    val assigns: ArrayList<Pair<String, Exp<Value>>>,
    override val id: Uuid
) : Statement(id) {
    constructor(assigns: ArrayList<Pair<String, Exp<Value>>>) : this(assigns, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        for (assign in assigns) env.set(assign.first, assign.second.evaluate(env))
        return Finished
    }
}

@KParcelize
data class Expression(
    val expression: Exp<Value>,
    override val id: Uuid
) : Statement(id) {
    constructor(expression: Exp<Value>) : this(expression, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult {
        expression.evaluate(env)
        return Finished
    }
}

@KParcelize
data class LineError(val content: String) : KParcelable

@KParcelize
data class Parallel(
    val blocks: ArrayList<out ArrayList<out Statement>>,
    override val id: Uuid
) : Statement(id) {
    constructor(blocks: ArrayList<out ArrayList<out Statement>>) : this(blocks, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult =
        ParallelEvaluation(blocks.map { EvalSequence(it) })
}

@KParcelize
data class Atomic(
    val statements: ArrayDeque<Statement>,
    override val id: Uuid
) : Statement(id) {
    constructor(statements: List<Statement>) : this(ArrayDeque(statements), Uuid.random())

    override fun evaluate(env: Env): EvaluationResult = AtomicEvaluation(this.statements)
}

@KParcelize
data class Wait(
    val condition: Exp<*>,
    val atomic: Atomic,
    override val id: Uuid
) : Statement(id) {
    constructor(condition: Exp<*>, atomic: Atomic) : this(condition, atomic, Uuid.random())

    override fun evaluate(env: Env): EvaluationResult =
        when (val result = condition.evaluate(env)) {
            is VBool -> {
                if (result.value) {
                    AtomicEvaluation(atomic.statements)
                } else {
                    SingleStatement(this)
                }
            }

            else -> AbortEvaluation("Condition must be a logical expression")
        }
}
