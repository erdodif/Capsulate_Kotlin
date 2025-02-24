@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.evaluation.AbortEvaluation
import com.erdodif.capsulate.lang.program.evaluation.AtomicEvaluation
import com.erdodif.capsulate.lang.program.evaluation.DependentEvaluation
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.evaluation.ParallelEvaluation
import com.erdodif.capsulate.lang.program.evaluation.SingleStatement
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Right
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class Statement(open val id: Uuid = Uuid.random(), open val match: MatchPos) :
    KParcelable {
    abstract fun evaluate(env: Env): EvaluationResult
    override fun equals(other: Any?): Boolean = other is Statement && other.id == id
    override fun hashCode(): Int = id.hashCode()

    fun <R : Value> Exp<R>.join(
        context: Env,
        onValue: Env.(R) -> EvaluationResult
    ): EvaluationResult = try {
        when (val result = evaluate(context)) {
            is Left -> onValue(context, result.value)
            is Right -> DependentEvaluation(result.value, onValue)
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression: $e")
    }

    fun List<Exp<Value>>.joinAll(
        context: Env,
        onEvery: Env.(List<Value>) -> EvaluationResult
    ): EvaluationResult = if (isEmpty()) onEvery(context, emptyList()) else
        this[0].join(context) {
            this@joinAll.drop(1).joinAll(context) { values ->
                onEvery(this, buildList { add(it); addAll(values) })
            }
        }
}

@KParcelize
data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<out Statement>,
    val statementsFalse: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(
        condition: Exp<*>,
        statementsTrue: List<Statement>,
        statementsFalse: List<Statement>,
        match: MatchPos
    ) : this(condition, ArrayList(statementsTrue), ArrayList(statementsFalse), Uuid.random(), match)

    constructor(
        condition: Exp<*>,
        statementsTrue: ArrayList<Statement>,
        statementsFalse: ArrayList<Statement>,
        match: MatchPos
    ) : this(condition, statementsTrue, statementsFalse, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult = condition.join(env) {
        when {
            it is VBool && it.value -> EvalSequence(statementsTrue)
            it is VBool -> EvalSequence(statementsFalse)
            else -> AbortEvaluation("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class When(
    val blocks: MutableList<Pair<Exp<*>, List<Statement>>>,
    val elseBlock: List<Statement>? = null,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(
        block: List<Pair<Exp<*>, List<Statement>>>,
        elseBlock: List<Statement>? = null,
        match: MatchPos
    ) : this(block.toMutableList(), elseBlock, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult {
        val source = blocks.removeAt(env.random.nextInt(blocks.size))
        return source.first.join(env) {
            if (it is VBool) {
                when {
                    it.value -> EvalSequence(source.second)
                    blocks.isEmpty() ->
                        AbortEvaluation("When conditions exhausted, Abort happens by definition")

                    else -> SingleStatement(this@When)
                }
            } else {
                AbortEvaluation("Condition must be a logical expression")
            }
        }
    }
}

@KParcelize
data class Skip(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Env) = Finished
}

@KParcelize
data class Abort(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Env) = AbortEvaluation("Abort has been called!")
}

abstract class Loop(
    open val condition: Exp<*>,
    open val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match)

@KParcelize
data class While(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult = condition.join(env) {
        when (it) {
            is VBool -> {
                if (it.value) {
                    EvalSequence(statements + this@While)
                } else {
                    Finished
                }
            }

            else -> AbortEvaluation("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class DoWhile(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult =
        EvalSequence(statements + While(condition, statements, MatchPos.ZERO))
}

@KParcelize
data class Assign(
    val label: String, val value: Exp<*>, override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(label: String, value: Exp<*>, match: MatchPos) :
            this(label, value, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult = value.join(env) {
        env.set(label, it)
        Finished
    }
}

@KParcelize
data class Select(
    val label: String, val set: String /*Specification: Type*/,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(label: String, set: String, match: MatchPos) :
            this(label, set, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult {
        TODO("Implement 'Zsák objektum'")
    }
}

@KParcelize
data class ParallelAssign(
    val assigns: ArrayList<Pair<String, Exp<Value>>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(assigns: ArrayList<Pair<String, Exp<Value>>>, match: MatchPos) :
            this(assigns, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult =
        assigns.map { it.second }.joinAll(env) {
            for (assign in assigns.map { it.first }.zip(it)) env.set(
                assign.first,
                assign.second
            )
            Finished
        }
}

@KParcelize
data class Expression(
    val expression: Exp<Value>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(expression: Exp<Value>, match: MatchPos) : this(expression, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult {
        return try {
            expression.evaluate(env)
            Finished
        } catch (e: Exception) {
            AbortEvaluation(e.message ?: "Error while evaluating Expression!")
        }
    }
}

@KParcelize
data class LineError(val content: String) : KParcelable

@KParcelize
data class Parallel(
    val blocks: ArrayList<out ArrayList<out Statement>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(blocks: ArrayList<out ArrayList<out Statement>>, match: MatchPos) :
            this(blocks, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult =
        ParallelEvaluation(blocks.map { EvalSequence(it) })
}

@KParcelize
data class Atomic(
    val statements: ArrayDeque<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(statements: List<Statement>, match: MatchPos) :
            this(ArrayDeque(statements), Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult = AtomicEvaluation(this.statements)
}

@KParcelize
data class Wait(
    val condition: Exp<*>,
    val atomic: Atomic,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(condition: Exp<*>, atomic: Atomic, match: MatchPos) :
            this(condition, atomic, Uuid.random(), match)

    override fun evaluate(env: Env): EvaluationResult =
        condition.join(env) {
            when (it) {
                is VBool -> {
                    if (it.value) {
                        AtomicEvaluation(atomic.statements)
                    } else {
                        SingleStatement(this@Wait)
                    }
                }

                else -> AbortEvaluation("Condition must be a logical expression")
            }
        }
}
