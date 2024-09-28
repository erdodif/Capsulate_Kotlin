package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._anyKeyword
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._lineEnd
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.freeChar
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.reservedChar
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.util.tok

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

class Skip : Statement {
    override fun evaluate(env: Env) {}
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

fun <T> delimit(parser: Parser<T>): Parser<T> =
    left(parser, many(_lineEnd))

val ParserState.statement: Parser<Statement>
    get() = asum(
        sExpression, sSkip, sAssign, sParallelAssign, sIf, sWhile, sDoWhile
    )

val program: Parser<ArrayList<Statement>> =
    //{ right(many(_lineEnd),delimited(statement, some(_lineEnd)))() }
    { many(middle(many(_lineEnd), delimit(statement), many(_lineEnd)))() }

val sError: Parser<LineError> =
    delimit(some(satisfy { it !in lineEnd })) / { LineError(it.asString()) }

val sSkip: Parser<Statement> = delimit(_keyword("skip")) / { Skip() }

val sExpression: Parser<Statement> = delimit(pExp) / { Expression(it) }

val sIf: Parser<Statement> = (middle(_keyword("if"), pExp, many(_char('\n'))) +
        middle(_char('{'), program, delimit(_char('}') + many(_char('\n')))) + right(
    _keyword("else") +  many(_char('\n')), middle(_char('{'), program, delimit(_char('}')))
)) / { If(it.first.first, it.first.second, it.second) }

val sWhile: Parser<Statement> =
    (middle(_keyword("while"), pExp, many(_char('\n'))) + middle(_char('{'), program, delimit(_char('}')))) / {
        While(
            it.first,
            it.second
        )
    }

val sDoWhile: Parser<Statement> = right(
    _keyword("do") + many(_char('\n')),
    middle(_char('{'), program, right(_char('}'), many(_char('\n')))) + delimit(right(_keyword("while"), pExp))
) / {
    DoWhile(it.second, it.first)
}

val sParallelAssign: Parser<Statement> =
    (delimited(_nonKeyword, _char(',')) + right(tok(string(":=")), delimit(delimited(pExp, _char(',')))))[{
        if (it.value.second.size != it.value.first.size) {
            fail(
                "The number of parameters does not match the number of values to assign."
            )
        } else pass(
            it.match.start, ParallelAssign(it.value.first.zip(it.value.second) as ArrayList)
        )
    }]

val sAssign: Parser<Statement> = delimit(_nonKeyword + right(_keyword(":="), pExp)) / {
    Assign(it.first, it.second)
}

val halfProgram: Parser<ArrayList<Either<Statement, LineError>>> = {
    orEither(
        topLevel(many(right(many(_lineEnd), or(statement, sError)))),
        topLevel(many(_lineEnd) / { arrayListOf() })
    )()
}

class ParseException(reason: String) : Exception(reason)

fun parseProgram(input: String): ParserResult<ArrayList<Statement>> =
    ParserState(input).parse(topLevel(program))

@Suppress("UNCHECKED_CAST")
fun tokenizeProgram(input: String): ParserResult<ArrayList<Token>> =
    ParserState(input).parse(topLevel(many(asum(
        _lineEnd * { it, pos -> LineEnd(it, pos) },
        pVariable as Parser<Token>,
        pComment as Parser<Token>,
        pIntLit as Parser<Token>,
        pBoolLit as Parser<Token>,
        pStrLit as Parser<Token>,
        _anyKeyword * { it, pos -> KeyWord(it, pos) },
        tok(reservedChar) * { it, pos -> Symbol(it, pos) },
        tok(some(freeChar))* { _, pos -> Token(pos)}
    ))))


fun Env.runProgram(statements: ArrayList<Statement>) {
    for (statement in statements) statement.evaluate(this)
}
