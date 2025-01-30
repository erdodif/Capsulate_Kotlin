package com.erdodif.capsulate.lang.program

import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.util.Env

data class DebugEnv(
    var env: Env,
    val program: List<Statement>,
    private val head: Int = 0
) {
    val currentStatement: Statement
        get() = program[head]

    fun step(): DebugEnv {
        currentStatement.evaluate(env)
        val env = DebugEnv(
            env,
            program,
            head + 1 // TODO : overflow
        )
        println(env)
        return env
    }

    override fun toString(): String {
        return "Debug[$env] #$head ($program)"
    }
}
