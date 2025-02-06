package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.lang.program.grammar.Statement
import io.github.aakira.napier.Napier

sealed interface EvaluationStack {
    /**
     * Evaluates the "first" statement from the
     */
    fun evaluate(env: Env): EvaluationStack
}

data object EmptyEvaluationStack : EvaluationStack {
    override fun evaluate(env: Env) = this
}

data class SingleEvaluationStack(val nextStatement: Statement) : EvaluationStack {
    override fun evaluate(env: Env) = nextStatement.evaluate(env)
}

data class AtomicEvaluationStack(private val _statements: ArrayDeque<Statement>) : EvaluationStack {

    val statements: List<Statement>
        get() = _statements

    override fun evaluate(env: Env): EvaluationStack {
        val first = _statements.removeFirst()
        if (_statements.isEmpty()) {
            return EmptyEvaluationStack
        }
        return when (val stack = _statements.first().evaluate(env)) {
            is EmptyEvaluationStack -> this
            is SingleEvaluationStack -> {
                _statements.addFirst(stack.nextStatement)
                this
            }

            is AtomicEvaluationStack -> {
                Napier.d {
                    "Atomic evaluation on statement $first generated another Atomic "
                }
                _statements.addAll(0, stack.statements)
                this
            }
        }
    }
}

data class ParallelEvaluationStack(val stacks: ArrayDeque<EvaluationStack>) : EvaluationStack {

    override fun evaluate(env: Env): EvaluationStack {
        if (env.deterministic) {
            var i = 0
            while(i < stacks.size){
                when (val stack = stacks[i]) {
                    is EmptyEvaluationStack -> {

                    }
                    is SingleEvaluationStack -> {
                        stack = stack.nextStatement.evaluate(env)
                    }
                }
            }
        } else {

        }
    }

}

class Stack{
    val entryPoints: ArrayDeque<Stack>
}
