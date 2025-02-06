package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.AbortEvaluation
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.EvaluationResult
import com.erdodif.capsulate.lang.util.Finished
import com.erdodif.capsulate.lang.util.EvalSequence
import com.erdodif.capsulate.lang.util.SingleStatement
import kotlin.collections.plus

interface Statement : KParcelable {
    fun evaluate(env: Env): EvaluationResult
}

@KParcelize
data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<out Statement>,
    val statementsFalse: ArrayList<out Statement>,
) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        val result = condition.evaluate(env)
        return if (result is VBool) {
            if (result.value) {
                EvalSequence(statementsTrue)
            } else {
                EvalSequence(statementsTrue)
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
) : Statement {

    override fun evaluate(env: Env): EvaluationResult { // TODO: allow non-determinism
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
data object Skip : Statement {
    override fun evaluate(env: Env) = Finished
}

@KParcelize
data object Abort : Statement {
    override fun evaluate(env: Env) = AbortEvaluation("Abort has been called!")
}

abstract class Loop(open val condition: Exp<*>, open val statements: ArrayList<out Statement>) :
    Statement

@KParcelize
data class While(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
) : Loop(condition, statements) {
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
) : Loop(condition, statements) {
    override fun evaluate(env: Env): EvaluationResult =
        EvalSequence(statements + While(condition, statements))
}

@KParcelize
data class Assign(val id: String, val value: Exp<*>) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        env.set(id, value.evaluate(env))
        return Finished
    }
}

@KParcelize
data class Select(val id: String, val set: String /*Specification: Type*/) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        TODO("Implement 'Zsák objektum'")
    }
}

@KParcelize
data class ParallelAssign(val assigns: ArrayList<Pair<String, Exp<Value>>>) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        for (assign in assigns) env.set(assign.first, assign.second.evaluate(env))
        return Finished
    }
}

@KParcelize
data class Expression(val expression: Exp<Value>) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        expression.evaluate(env)
        return Finished
    }
}

@KParcelize data class LineError(val content: String) : KParcelable

@KParcelize
data class Parallel(val blocks: ArrayList<out ArrayList<out Statement>>) : Statement {
    override fun evaluate(env: Env): EvaluationResult {
        val newBlocks: MutableList<ArrayDeque<Statement>> =
            blocks.map { ArrayDeque(it) }.toMutableList()
        val index = env.random.nextInt(newBlocks.size)
        val list = newBlocks[index]
        return when (val result = list.removeFirst().evaluate(env)) {
            Finished -> {
                if (list.isEmpty()) {
                    newBlocks.removeAt(index)
                }
                SingleStatement(this)
            }
            is AbortEvaluation -> result
            is SingleStatement -> {
                list.addFirst(result.next)
                SingleStatement(this)
            }
            is EvalSequence -> {
                list.addAll(0, result.statements)
                SingleStatement(this)
            }
        }
    }
}

@KParcelize
data class Atomic(val statements: ArrayDeque<Statement>) : Statement {
    constructor(statements: List<Statement>) : this(ArrayDeque(statements))

    override fun evaluate(env: Env): EvaluationResult =
        when (val result = statements.removeFirst().evaluate(env)) {
            is Finished -> SingleStatement(this)
            is AbortEvaluation -> result
            is SingleStatement -> {
                statements.addFirst(result.next)
                SingleStatement(this)
            }
            is EvalSequence -> {
                statements.addAll(0, result.statements)
                SingleStatement(this)
            }
        }
}

@KParcelize
data class Wait(val condition: Exp<*>, val atomic: Atomic) : Statement {
    override fun evaluate(env: Env): EvaluationResult =
        when (val result = condition.evaluate(env)) {
            is VBool -> {
                if (result.value) {
                    SingleStatement(atomic)
                } else {
                    SingleStatement(this)
                }
            }
            else -> AbortEvaluation("Condition must be a logical expression")
        }
}
