package com.erdodif.capsulate.lang

interface Exp

class TMPEXP :Exp
class TMPSTMT :Statement

data class StrLit(val value: String) : Exp
data class IntLit(val value: Int) : Exp
data class BoolLit(val value: Boolean) : Exp
data class Variable(val id: String) : Exp
data class LamLit(val lambda: Nothing)
data class Add(val first: Exp, val second: Exp)

val pStrLit: Parser<StrLit> = {
    val result: ParserResult<ArrayList<Char>> = middle(
        char('"'),
        many(orEither(right(char('\\'), anyChar), right(not(char('"')), anyChar))),
        _char('"')
    )()
    if (result is Pass) {
        pass(StrLit((result.value).asString()))
    } else {
        result as Fail<*>
        result.into()
    }
}
val pIntLit: Parser<IntLit> = {
    val isNegative = (optional(_char('-'))() as Pass).value == null
    val digitMatch = some(digit)()
    if (digitMatch is Fail<*>) {
        digitMatch.into()
    } else {
        var number: Int = 0
        for (digit in (digitMatch as Pass<Array<Short>>).value) {
            number *= 10
            number += digit
        }
        pass(IntLit(if (isNegative) -number else number))
    }
}
val pBoolLit: Parser<BoolLit> = {
    val result: ParserResult<Either<String,String>> = or(_keyword("true"), _keyword("false"))()
    if (result is Fail<*>) {
        result.into()
    } else {
        pass(BoolLit((result as Pass<*>).value is Left<*, *>))
    }
}
val pVariable: Parser<Variable> = {
    val result: ParserResult<String> = _nonKeyword()
    if(result is Fail){
        result.into()
    }
    else{
        pass(Variable((result as Pass).value))
    }
}
//val pLamLit: Parser<Exp> = TODO()

inline fun <T>asum(parsers: Array<Parser<T>>) : Parser<T> = {
    val pos = position
    var result: ParserResult<T> = fail("Nothing matched")
    for (factory in parsers) {
        val tmpResult = factory()
        if (tmpResult is Fail) {
            position = pos
        } else {
            result = tmpResult
            break
        }
    }
    result
}

val litOrder: Array<Parser<*>> = arrayOf<Parser<*>>(
    pIntLit,
    pBoolLit,
    //pLamLit,
    pStrLit,
    pVariable,
    //middle(char('('), pExp , char(')'))
)

@Suppress("UNCHECKED_CAST")
val pAtom: Parser<Exp> = asum(litOrder as Array<Parser<Exp>>)

@Suppress("UNCHECKED_CAST")
val helper: (Exp, Exp) -> Exp = { a: Exp, b:Exp -> Add(a, b) } as (Exp, Exp) -> Exp
val pAdd: Parser<Exp> = chainr1(pAtom, left({ pass(helper) }, char('+')))

val pExp: Parser<Exp> = pAdd

interface Statement

data class If(val expression:Exp, val statements: Array<Statement>) : Statement
data class While(val expression:Exp, val statements: Array<Statement>) : Statement
data class Assign(val id: String, val value:Exp) : Statement

/*
val statement: Parser<Statement> = TODO()//asum(arrayOf(sIf, sWhile, sAssign))
val program: Parser<Array<Statement>> = delimited(statement, _char(';'))

val sIf : Parser<Statement> = {
    val condition: ParserResult<Exp> = right(_keyword("if"), pExp)()
    val program: ParserResult<Array<Statement>> = middle(_char('{'), program , _char('}'))()
    when {
        condition is Fail<*> -> condition.into()
        program is Fail<*> -> program.into()
        else -> pass(If((condition as Pass).value,(program as Pass).value))
    }
}

//TODO
val sWhile : Parser<Statement> = left({pass(TMPSTMT())}, freeChar)
val sAssign : Parser<Statement> = left({pass(TMPSTMT())}, freeChar)

class ParseException(reason: String): Exception(reason)

fun parseProgram(input: String): Array<Statement>{
    val result: ParserResult<Array<Statement>> = ParserState(input).run{
        topLevel(program)()
    }
    if(result is Fail){
        throw ParseException(result.reason)
    }
    else{
        return (result as Pass).value
    }
}
*/