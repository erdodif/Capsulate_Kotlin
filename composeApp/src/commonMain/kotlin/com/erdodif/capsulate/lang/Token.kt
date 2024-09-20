package com.erdodif.capsulate.lang

interface Exp<T : Value> {
    fun evaluate(env: Env): T
}

open class Token(val match: MatchPos) {
    inline val ParserState.matchedToken: String
        get() = input[match.start, match.end]
}

class KeyWord(val id: String, match: MatchPos): Token(match)
class Symbol(val id: Char, match: MatchPos): Token(match)
class LineEnd(val char: Char, match: MatchPos): Token(match)
class Comment(val content: String, match: MatchPos): Token(match)

val pComment: Parser<Comment> = orEither(
        right(and(char('/'), _char('/')),left(many(right(not(char('\n')), anyChar)),or(char('\n'), EOF))),
        right(and(char('/'), _char('*')),left(many(right(not(and(char('*'), _char('/'))), anyChar)), and(char('*'), _char('/'))))
    ) * {it, pos -> Comment(it.asString(), pos)}


class StrLit(val value: String, match: MatchPos) : Exp<VStr>, Token(match) {
    override fun evaluate(env: Env): VStr = VStr(value)
}

class IntLit(val value: Int, match: MatchPos) : Exp<VWhole>, Token(match) {
    override fun evaluate(env: Env): VWhole = VWhole(value)
}

class NatLit(val value: UInt, match: MatchPos) : Exp<VNat>, Token(match) {
    override fun evaluate(env: Env): VNat = VNat(value)
}

class BoolLit(val value: Boolean, match: MatchPos) : Exp<VBool>, Token(match) {
    override fun evaluate(env: Env): VBool = VBool(value)
}

class Variable(val id: String, match: MatchPos) : Exp<Value>, Token(match) {
    override fun evaluate(env: Env): Value {
        val param = env.get(id)
        if (param is Left) {
            return (param as Left<Parameter, *>).value.value
        } else {
            throw RuntimeException("Variable '$id' is not defined!")
        }
    }
}

class Add(val first: Exp<*>, val second: Exp<*>) : Exp<VNum> {
    override fun evaluate(env: Env): VNum {
        val resultFirst = first.evaluate(env)
        val resultSecond = second.evaluate(env)
        if (resultFirst !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (resultSecond !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (resultFirst.type() != resultSecond.type()) {
            throw RuntimeException("Operands must have the same type ('${resultFirst.type()}' + '${resultSecond.type()}' is not allowed by design)")
        }
        if (resultFirst !is VNat) {
            return VNat((resultFirst.value + resultSecond.value).toUInt())
        }
        return VWhole(resultFirst.value + resultSecond.value)
    }
}

data class Sub(val first: Exp<*>, val second: Exp<*>) : Exp<VNum> {
    override fun evaluate(env: Env): VNum {
        val resultFirst = first.evaluate(env)
        val resultSecond = second.evaluate(env)
        if (resultFirst !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (resultSecond !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (resultFirst.type() != resultSecond.type()) {
            throw RuntimeException("Operands must have the same type ('${resultFirst.type()}' - '${resultSecond.type()}' is not allowed by design)")
        }
        if (resultFirst !is VNat) {
            if (resultFirst.value < resultSecond.value) {
                throw RuntimeException("Natural underflow!")
            }
            return VNat((resultFirst.value - resultSecond.value).toUInt())
        }
        return VWhole(resultFirst.value - resultSecond.value)
    }
}

data class Mul(val first: Exp<*>, val second: Exp<*>) : Exp<VNum> {
    override fun evaluate(env: Env): VNum {
        val resultFirst = first.evaluate(env)
        val resultSecond = second.evaluate(env)
        if (resultFirst !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (resultSecond !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (resultFirst.type() != resultSecond.type()) {
            throw RuntimeException("Operands must have the same type ('${resultFirst.type()}' * '${resultSecond.type()}' is not allowed by design)")
        }
        if (resultFirst !is VNat) {
            return VNat((resultFirst.value * resultSecond.value).toUInt())
        }
        return VWhole(resultFirst.value * resultSecond.value)
    }
}

data class Div(val first: Exp<*>, val second: Exp<*>) : Exp<VNum> {
    override fun evaluate(env: Env): VNum {
        val resultFirst = first.evaluate(env)
        val resultSecond = second.evaluate(env)
        if (resultFirst !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (resultSecond !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (resultFirst.type() != resultSecond.type()) {
            throw RuntimeException("Operands must have the same type ('${resultFirst.type()}' / '${resultSecond.type()}' is not allowed by design)")
        }
        if (resultFirst !is VNat) {
            return VNat((resultFirst.value / resultSecond.value).toUInt())
        }
        return VWhole(resultFirst.value / resultSecond.value)
    }
}

data class Equal(val first: Exp<*>, val second: Exp<*>) : Exp<VBool> {
    constructor(expressions: Pair<Exp<*>, Exp<*>>) : this(expressions.first, expressions.second)

    override fun evaluate(env: Env): VBool {
        val value1 = first.evaluate(env)
        val value2 = second.evaluate(env)
        if (value1.type() != value2.type()) {
            return VBool(false)
        }
        return VBool(value1 == value2)
    }
}

data class And(val first: Exp<*>, val second: Exp<*>) : Exp<VBool> {
    override fun evaluate(env: Env): VBool {
        val value1 = first.evaluate(env)
        if (value1 !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        val value2 = second.evaluate(env)
        if (value2 !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        return VBool(value1.value && value2.value)
    }
}

data class Or(val first: Exp<*>, val second: Exp<*>) : Exp<VBool> {
    override fun evaluate(env: Env): VBool {
        val value1 = first.evaluate(env)
        if (value1 !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        val value2 = second.evaluate(env)
        if (value2 !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        return VBool(value1.value || value2.value)
    }
}

data class Not(val expression: Exp<*>) : Exp<VBool> {
    override fun evaluate(env: Env): VBool {
        val res = expression.evaluate(env)
        if (res is VBool) {
            return VBool(!res.value)
        } else {
            throw RuntimeException("Type must be Logical")
        }
    }
}

data class FunctionCall(val id: String, val arguments: ArrayList<Exp<*>>) : Exp<Nothing> {
    override fun evaluate(env: Env): Nothing = TODO()
}

val pStrLit: Parser<StrLit> = middle(
    char('"'),
    many(orEither(right(char('\\'), anyChar), right(not(char('"')), anyChar))),
    _char('"')
) * { res, pos -> StrLit(res.asString(),pos) }

val pIntLit: Parser<IntLit> = _integer * { it, pos -> IntLit(it, pos)}

val pBoolLit: Parser<BoolLit> =
    or(_keyword("true"), _keyword("false")) * { it, pos -> BoolLit(it is Left<*, *>, pos) }

val pVariable: Parser<Variable> = _nonKeyword * { it, pos -> Variable(it, pos)}

val litOrder: Array<Parser<*>> = arrayOf(
    pIntLit,
    pBoolLit,
    pStrLit,
    pVariable,
    // { middle(char('('), pExp , char(')'))() } //TODO: Circular dependency has to be resolved with pExp
)

typealias ExParser = Parser<Exp<*>>

@Suppress("UNCHECKED_CAST")
val pAtom: ExParser = asum(litOrder as Array<Parser<Exp<*>>>)

val pEqual: ExParser = (pAtom + right(_char('='), pAtom)) / {it -> Equal(it)}

//val helper: (Exp, Exp) -> Exp =
// Ordering and implementation missing
val pMul: ExParser =
    chainr1(pEqual, left({ pass(position,::Mul)  }, _char('*')))
val pDiv: ExParser =
    chainl1(pMul, left({pass(position,::Div) }, _char('/')))
val pAdd: ExParser =
    chainr1(pDiv, left({ pass(position,::Add)  }, _char('+')))
val pSub: Parser<Exp<*>> =
    chainl1(pAdd, left({ pass(position,::Sub)  }, _char('-')))
val pOr: ExParser =
    chainl1(pDiv, left({ pass(position,::Or)  }, _char('|')))
val pAnd: ExParser =
    chainl1(pOr, left({ pass(position,::And) }, _char('&')))
val pNot: ExParser =
    right(_char('!'), pOr) / { Not(it) }
val pFunctionCall: ExParser = { fail("NOT IMPLEMENTED") }

val pExp: ExParser = pNot as Parser<Exp<*>>
