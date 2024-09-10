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
data class Expression(val expression: Exp<*>): Statement{
    override fun evaluate(env: Env) {
        expression.evaluate(env)
    }
}
data class LineError(val content: String) : Statement{
    override fun evaluate(env: Env) {
        throw RuntimeException("Tried to run lineError as code: \"$content\"")
    }
}

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

fun ParserState.statement(): ParserResult<Statement> {
    return asum(
        arrayOf(
            sSkip,
            sIf,
            sWhile,
            sDoWhile,
            sAssign,
            sParallelAssign,
            sExpression,
            sError
        )
    )()
}

val program: Parser<ArrayList<Statement>> = { some(left({ statement() }, or(_char(';'), EOF)))() }

val sError: Parser<Statement> = some(satisfy { it !in ";\n" }) * { LineError(it.asString()) }

val sSkip: Parser<Statement> = _keyword("skip") * { Skip() }

val sExpression: Parser<Statement> = pExp * { Expression(it) }

val sIf: Parser<Statement> = (
        right(_keyword("if"), pExp) + middle(
            _char('{'), program, _char('}')) + right(
                _keyword("else"), middle(_char('{'), program, _char('}'))
            )
        ) * { If(it.first.first, it.first.second, it.second) }
/*
{
val condition: ParserResult<Exp<*>> = right(_keyword("if"), pExp)()
if (condition is Fail) {
    condition.into()
} else {
    val programTrue: ParserResult<ArrayList<Statement>> =
        middle(_char('{'), program, _char('}'))()
    if (programTrue is Fail) {
        programTrue.into()
    } else {
        val elseW = _keyword("else")()
        if (elseW is Fail) {
            elseW.into()
        } else {
            val programFalse: ParserResult<ArrayList<Statement>> =
                middle(_char('{'), program, _char('}'))()
            if (programFalse is Fail) {
                programFalse.into()
            } else {
                pass(
                    If(
                        (condition as Pass).value,
                        (programTrue as Pass).value,
                        (programFalse as Pass).value
                    )
                )
            }
        }
    }
}
}*/

val sWhile: Parser<Statement> = (
        right(_keyword("while"), pExp) + middle(_char('{'), program, _char('}'))
        ) * { While(it.first, it.second) }

val sDoWhile: Parser<Statement> =
    right(
        _keyword("do"),
        middle(_char('{'), program, _char('}')) + right(_keyword("while"), pExp)
    ) * {
        DoWhile(it.second, it.first)
    }

val sParallelAssign: Parser<Statement> = {
    val words = delimited(_nonKeyword, char(','))()
    if (words is Fail<*>) {
        words.into()
    } else {
        words as Pass
        val assignW = _keyword(":=")
        if (assignW is Fail<*>) {
            assignW.into()
        } else {
            val values = delimited(pExp, char(','))()
            if (values is Fail<*>) values.into()
            else if (words.value.size != (values as Pass).value.size) fail("The number of parameters does not match the number of values to assign.")
            else pass(ParallelAssign(words.value.zip(values.value) as ArrayList))
        }
    }
}

val sAssign: Parser<Statement> = and(_nonKeyword, right(_keyword(":="), pExp)) * {
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

fun Env.runProgram(statements: ArrayList<Statement>) {
    for (statement in statements) statement.evaluate(this)
}
