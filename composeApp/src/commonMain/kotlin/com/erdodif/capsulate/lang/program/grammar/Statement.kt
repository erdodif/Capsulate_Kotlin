package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Env

interface Statement: KParcelable {
    fun evaluate(env: Env)
}

@KParcelize
data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<out Statement>,
    val statementsFalse: ArrayList<out Statement>
) : Statement {
    override fun evaluate(env: Env) {
        val result = condition.evaluate(env)
        if (result is VBool) {
            if (result.value) {
                env.runProgram(statementsTrue)
            } else {
                env.runProgram(statementsFalse)
            }
        } else {
            throw RuntimeException("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class When(
    val blocks: List<Pair<Exp<*>, List<Statement>>>,
    val elseBlock: List<Statement>? = null
) : Statement {
    override fun evaluate(env: Env) { // TODO: allow non-determinism
        if (env.deterministic) {
            var run = false
            for (block in blocks) {
                val result = block.first.evaluate(env)
                if (result is VBool) {
                    if (result.value) {
                        run = true
                        env.runProgram(block.second)
                    }
                } else {
                    throw RuntimeException("Condition must be a logical expression")
                }
            }
            if (!run) {
                if (elseBlock != null) {
                    env.runProgram(elseBlock)
                } else {
                    Abort.evaluate(env)
                }
            }
        } else {
            TODO("Switch statement does not implement non-deterministic evaluation just yet")
        }
    }
}

@KParcelize
data object Skip : Statement {
    override fun evaluate(env: Env) {}
}

@KParcelize
data object Abort : Statement {
    override fun evaluate(env: Env) {
        TODO("End run in error")
    }
}

@KParcelize
data class Return(val value: Exp<*>) : Statement {
    override fun evaluate(env: Env) {
        TODO("Produce value")
    }
}

@KParcelize
data class While(val condition: Exp<*>, val statements: ArrayList<out Statement>) : Statement {
    override fun evaluate(env: Env) {
        var result = condition.evaluate(env)
        while (result is VBool && result.value) {
            env.runProgram(statements)
            result = condition.evaluate(env)
        }
        if (result !is VBool) {
            throw RuntimeException("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class DoWhile(val condition: Exp<*>, val statements: ArrayList<out Statement>) : Statement {
    override fun evaluate(env: Env) {
        var result: Any?
        do {
            env.runProgram(statements)
            result = condition.evaluate(env)
        } while (result is VBool && result.value)
        if (result !is VBool) {
            throw RuntimeException("Condition must be a logical expression")
        }
    }
}

@KParcelize
data class Assign(val id: String, val value: Exp<*>) : Statement {
    override fun evaluate(env: Env) = env.set(id, value.evaluate(env))
}

@KParcelize
data class Select(val id: String, val set: String /*Specification: Type*/) : Statement {
    override fun evaluate(env: Env) {
        TODO("Implement 'Zsák objektum'")
    }
}

@KParcelize
data class ParallelAssign(val assigns: ArrayList<Pair<String, Exp<Value>>>) : Statement {
    override fun evaluate(env: Env) {
        for (assign in assigns) env.set(assign.first, assign.second.evaluate(env))
    }
}

@KParcelize
data class Expression(val expression: Exp<Value>) : Statement {
    override fun evaluate(env: Env) {
        expression.evaluate(env)
    }
}

@KParcelize
data class LineError(val content: String) : KParcelable

@KParcelize
data class Parallel(val blocks: ArrayList<out ArrayList<out Statement>>) : Statement {
    override fun evaluate(env: Env) {
        TODO("Will need an event loop for that")
    }
}

@KParcelize
data class Wait(val condition: Exp<*>) : Statement {
    override fun evaluate(env: Env) {
        TODO("Will need an event loop for that")
    }
}
