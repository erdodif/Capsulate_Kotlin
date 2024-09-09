package com.erdodif.capsulate.lang

interface Exp

class TMPEXP :Exp
class TMPSTMT :Statement

data class StrLit(val value: String) : Exp
data class IntLit(val value: Int) : Exp
data class BoolLit(val value: Boolean) : Exp
data class Variable(val id: String) : Exp

data class Add(val first: Exp, val second: Exp) : Exp
data class Sub(val first: Exp, val second: Exp) : Exp
data class Mul(val first: Exp, val second: Exp) : Exp
data class Div(val first: Exp, val second: Exp) : Exp
data class Floor(val expression: Exp) : Exp
data class Ceil(val expression: Exp) : Exp
data class Equal(val first: Exp, val second: Exp) : Exp
data class And(val first: Exp, val second: Exp) : Exp
data class Or(val first: Exp, val second: Exp) : Exp
data class Not(val expression: Exp) : Exp
data class FunctionCall(val id:String, val arguments:ArrayList<Exp>) : Exp

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

val helper: (Exp, Exp) -> Exp = { a: Exp, b:Exp -> Add(a, b) }
val pAdd: Parser<Exp> = chainr1(pAtom, left({ pass(helper) }, char('+')))

// Ordering and implementation missing
val pMul :Parser<Exp> = TODO()
val pDiv :Parser<Exp> = TODO()
val pFloor :Parser<Exp> = TODO()
val pCeil :Parser<Exp> = TODO()
val pEqual :Parser<Exp> = TODO()
val pAnd :Parser<Exp> = TODO()
val pOr :Parser<Exp> = TODO()
val pNot :Parser<Exp> = TODO()
val pFunctionCall :Parser<Exp> = TODO()

val pExp: Parser<Exp> = pAdd