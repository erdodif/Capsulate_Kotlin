package com.erdodif.capsulate.lang

interface Statement

data class If(val expression:Exp, val statements: ArrayList<Statement>) : Statement
data class While(val expression:Exp, val statements: ArrayList<Statement>) : Statement
data class Assign(val id: String, val value:Exp) : Statement
data class Parallel(val blocks: ArrayList<ArrayList<Statement>>) :Statement
data class Wait(val condition: Exp) : Statement

fun ParserState.statement(): ParserResult<Statement> { return asum(arrayOf(sIf, sWhile, sAssign))() }
val program: Parser<ArrayList<Statement>> = delimited({statement()}, _char(';'))

val sIf : Parser<Statement> = {
    val condition: ParserResult<Exp> = right(_keyword("if"), pExp)()
    val program: ParserResult<ArrayList<Statement>> = middle(_char('{'), program , _char('}'))()
    when {
        condition is Fail<*> -> condition.into()
        program is Fail<*> -> program.into()
        else -> pass(If((condition as Pass).value,(program as Pass).value))
    }
}

val sWhile : Parser<Statement> = left({pass(TMPSTMT())}, freeChar)
val sAssign : Parser<Statement> = left({pass(TMPSTMT())}, freeChar)

class ParseException(reason: String): Exception(reason)

fun parseProgram(input: String): ArrayList<Statement>{
    val result: ParserResult<ArrayList<Statement>> = ParserState(input).run{
        topLevel(program)()
    }
    if(result is Fail){
        throw ParseException(result.reason)
    }
    else{
        return (result as Pass).value
    }
}
