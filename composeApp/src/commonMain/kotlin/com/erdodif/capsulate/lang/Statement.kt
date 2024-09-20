package com.erdodif.capsulate.lang

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

val ParserState.statement: Parser<Statement>
    get() = asum(
        arrayOf(
            sSkip,
            sAssign,
            sIf,
            sWhile,
            sDoWhile,
            sParallelAssign,
            sExpression
        )
    )

val lineEnd: Parser<Unit> = tok(or(char(';'), char('\n'))) / {}
val program: Parser<ArrayList<Statement>> = { some(left(statement, or(lineEnd, EOF)))() }

val halfProgram: Parser<ArrayList<Either<Statement, LineError>>> = {
    some(
        or(
            left(statement, or(lineEnd, EOF)), left(
                sError, or(lineEnd, EOF)
            )
        )
    )()
}

val sError: Parser<LineError> = some(satisfy { it !in ";\n" }) / { LineError(it.asString()) }

val sSkip: Parser<Statement> = _keyword("skip") / { Skip() }

val sExpression: Parser<Statement> = pExp / { Expression(it) }

val sIf: Parser<Statement> = (
        right(_keyword("if"), pExp) + middle(
            _char('{'), program, _char('}')
        ) + right(
            _keyword("else"), middle(_char('{'), program, _char('}'))
        )
        ) / { If(it.first.first, it.first.second, it.second) }

val sWhile: Parser<Statement> = (
        right(_keyword("while"), pExp) + middle(_char('{'), program, _char('}'))
        ) / { While(it.first, it.second) }

val sDoWhile: Parser<Statement> =
    right(
        _keyword("do"),
        middle(_char('{'), program, _char('}')) + right(_keyword("while"), pExp)
    ) / {
        DoWhile(it.second, it.first)
    }

val sParallelAssign: Parser<Statement> = {
    val words = delimited(_nonKeyword, char(','))()
    if (words is Fail<*>) {
        words.to()
    } else {
        words as Pass
        val assignW = _keyword(":=")
        if (assignW is Fail<*>) {
            assignW.to()
        } else {
            val values = delimited(pExp, char(','))()
            if (values is Fail<*>) values.to()
            else if (words.value.size != (values as Pass).value.size) {
                fail("The number of parameters does not match the number of values to assign.")
            } else pass(
                words.match.start,
                ParallelAssign(words.value.zip(values.value) as ArrayList)
            )
        }
    }
}

val sAssign: Parser<Statement> = and(_nonKeyword, right(_keyword(":="), pExp)) / {
    Assign(it.first, it.second)
}


class ParseException(reason: String) : Exception(reason)

fun parseProgram(input: String): ParserResult<ArrayList<Statement>> {
    return ParserState(input).run { topLevel(program)() }
    /*
    val result: ParserResult<ArrayList<Statement>> = ParserState(input).run{
        topLevel(program)()
    }
    if(result is Fail){
        return arrayListOf<Statement>()
        //throw ParseException(result.reason)
    }
    else{
        return (result as Pass).value
    }*/
}

fun parseLines(input: String): ParserResult<ArrayList<Either<Statement,LineError>>>{
    return ParserState(input).run(topLevel(halfProgram))
}

fun Env.runProgram(statements: ArrayList<Statement>) {
    for (statement in statements) statement.evaluate(this)
}
