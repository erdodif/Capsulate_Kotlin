package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.specification.Type

interface Statement {
    fun evaluate(env: Env)
}

data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<Statement>,
    val statementsFalse: ArrayList<Statement>
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

data object Skip : Statement {
    override fun evaluate(env: Env) {}
}

data object Abort : Statement {
    override fun evaluate(env: Env) { TODO("End run in error") }
}

data class Return(val value: Exp<*>): Statement{
    override fun evaluate(env: Env) {
        TODO("Produce value")
    }
}

data class While(val condition: Exp<*>, val statements: ArrayList<Statement>) : Statement {
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

data class DoWhile(val condition: Exp<*>, val statements: ArrayList<Statement>) : Statement {
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

data class Assign(val id: String, val value: Exp<*>) : Statement {
    override fun evaluate(env: Env) = env.set(id, value.evaluate(env))
}

data class Select(val id: String, val set: Type): Statement{
    override fun evaluate(env: Env) {
        TODO("Implement 'Zsák objektum'")
    }
}

data class ParallelAssign(val assigns: ArrayList<Pair<String, Exp<*>>>) : Statement {
    override fun evaluate(env: Env) {
        for (assign in assigns) env.set(assign.first, assign.second.evaluate(env))
    }
}

data class Expression(val expression: Exp<*>) : Statement {
    override fun evaluate(env: Env) {
        expression.evaluate(env)
    }
}

data class LineError(val content: String)

data class Parallel(val blocks: ArrayList<ArrayList<Statement>>) : Statement {
    override fun evaluate(env: Env) {
        TODO("Will need an event loop for that")
    }
}

data class Wait(val condition: Exp<*>) : Statement {
    override fun evaluate(env: Env) {
        TODO("Will need an event loop for that")
    }
}
