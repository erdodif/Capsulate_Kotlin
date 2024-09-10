package com.erdodif.capsulate.lang

interface Exp<T : Value> {
    fun evaluate(env: Env): T
}

data class StrLit(val value: String) : Exp<VStr> {
    override fun evaluate(env: Env): VStr = VStr(value)
}

data class IntLit(val value: Int) : Exp<VWhole> {
    override fun evaluate(env: Env): VWhole = VWhole(value)
}

data class NatLit(val value: UInt) : Exp<VNat> {
    override fun evaluate(env: Env): VNat = VNat(value)
}

data class BoolLit(val value: Boolean) : Exp<VBool> {
    override fun evaluate(env: Env): VBool = VBool(value)
}

data class Variable(val id: String) : Exp<Value> {
    override fun evaluate(env: Env): Value {
        val param = env.get(id)
        if (param is Left) {
            return (param as Left<Parameter, *>).value.value
        } else {
            throw RuntimeException("Variable '$id' is not defined!")
        }
    }
}

data class Add(val first: Exp<*>, val second: Exp<*>) : Exp<VNum> {
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
) * { StrLit(it.asString()) }

val pIntLit: Parser<IntLit> = {
    val isNegative = (optional(_char('-'))() as Pass).value == null
    val digitMatch = some(digit)()
    if (digitMatch is Fail<*>) {
        digitMatch.into()
    } else {
        var number: Int = 0
        for (digit in (digitMatch as Pass).value) {
            number *= 10
            number += digit
        }
        pass(IntLit(if (isNegative) -number else number))
    }
}
val pBoolLit: Parser<BoolLit> =
    or(_keyword("true"), _keyword("false")) * { BoolLit(it is Left<*, *>) }

val pVariable: Parser<Variable> = _nonKeyword * { Variable(it) }

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

val pEqual: ExParser = (pAtom + right(_char('='), pAtom)) * { Equal(it.first, it.second) }

//val helper: (Exp, Exp) -> Exp =
// Ordering and implementation missing
val pMul: ExParser =
    chainr1(pEqual, left({ pass { a, b -> Mul(a, b) } }, _char('*')))
val pDiv: ExParser =
    chainl1(pMul, left({ pass { a, b -> Sub(a, b) } }, _char('/')))
val pAdd: ExParser =
    chainr1(pDiv , left({ pass { a: Exp<*>, b: Exp<*> -> Add(a, b) } }, _char('+')))
val pSub: Parser<Exp<*>> =
    chainl1(pAdd, left({ pass { a: Exp<*>, b: Exp<*> -> Sub(a, b) } }, _char('-')))
val pOr: ExParser =
    chainl1(pDiv, left({ pass { a: Exp<*>, b: Exp<*> -> Or(a, b) } }, _char('|')))
val pAnd: ExParser =
    chainl1(pOr, left({ pass { a: Exp<*>, b: Exp<*> -> Or(a, b) } }, _char('&')))
val pNot: ExParser =
    right(_char('!'), pOr) * { Not(it) }
val pFunctionCall: ExParser = { fail("NOT IMPLEMENTED") }

val pExp: ExParser = pNot as Parser<Exp<*>>
