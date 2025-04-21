package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.BOOL
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.NAT
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.WHOLE
import com.erdodif.capsulate.lang.program.grammar.or
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.ionspin.kotlin.bignum.integer.BigInteger

const val decimalError = "Decimal numbers are unsupported!"

@KParcelize
data object Add : BinaryOperator<VNum<*>, VNum<*>>(
    14,
    "+",
    _char('+'),
    Association.RIGHT,
    { a, b ->
        if (a is VNat && b is VNat) {
            VNat(a.value + b.value)
        } else if (a.value is BigInteger && b.value is BigInteger) {
            VWhole((a.value as BigInteger) + (b.value as BigInteger))
        } else {
            error(decimalError)
        }
    }
) {
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == NAT && secondType == NAT) NAT else WHOLE
}

@KParcelize
data object Sub : BinaryOperator<VNum<*>, VNum<*>>(
    12,
    "-",
    _char('-'),
    Association.LEFT,
    { a, b ->
        require(a.value is BigInteger && b.value is BigInteger) { decimalError }
        VWhole((a.value as BigInteger) - (b.value as BigInteger))
    }
) {
    override fun type(firstType: Type, secondType: Type): Type = WHOLE
}

@KParcelize
data object Mul : BinaryOperator<VNum<*>, VNum<*>>(
    18,
    "*",
    _char('*'),
    Association.RIGHT,
    { a, b ->
        require(a.value is BigInteger && b.value is BigInteger) { decimalError }
        if (a is VNat && b.value is BigInteger) {
            VNat(a.value * (b.value as BigInteger))
        } else if (a.value is BigInteger && b.value is BigInteger) {
            VWhole((a.value as BigInteger) * (b.value as BigInteger))
        } else error(decimalError)
    }
) {
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == NAT && secondType == NAT) NAT else WHOLE
}

@KParcelize
data object Div : BinaryOperator<VNum<*>, VNum<*>>(
    16,
    "/",
    _char('/'),
    Association.LEFT,
    { a, b ->
        require(a.value is BigInteger && b.value is BigInteger) { decimalError }
        if (b.value == BigInteger.ZERO) {
            error("Division by Zero!")
        }
        if (a is VNat) {
            VNat(a.value / (b.value as BigInteger))
        } else {
            VWhole((a.value as BigInteger) / (b.value as BigInteger))
        }
    }
) {
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == NAT && secondType == NAT) NAT else WHOLE
}

@KParcelize
data object Larger : BinaryOperator<VBool, VNum<*>>(
    4,
    ">",
    _char('>'),
    Association.NONE,
    { a, b -> VBool(a.value.compareTo(b.value) > 0) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object Smaller : BinaryOperator<VBool, VNum<*>>(
    4,
    "<",
    _char('<'),
    Association.NONE,
    { a, b -> VBool(a.value.compareTo(b.value) < 0) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object LargerEq : BinaryOperator<VBool, VNum<*>>(
    4,
    "≥",
    or(_keyword(">="), _char('≥')),
    Association.NONE,
    { a, b -> VBool(a.value.compareTo(b.value) >= 0) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object SmallerEq : BinaryOperator<VBool, VNum<*>>(
    4,
    "≤",
    or(_keyword("<="), _char('≤')),
    Association.NONE,
    { a, b -> VBool(a.value.compareTo(b.value) <= 0) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object Equal : BinaryOperator<Value, Value>(
    4,
    "=",
    _char('='),
    Association.NONE,
    { a, b ->
        if (a.type != b.type) {
            VBool(false)
        } else {
            VBool(a == b)
        }
    }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object NotEqual : BinaryOperator<Value, Value>(
    4,
    "≠",
    or(_keyword("!="), _char('≠')),
    Association.NONE,
    { a, b ->
        VBool(a != b)
    }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object And : BinaryOperator<VBool, VBool>(
    3,
    "∧",
    orEither(_char('&'), _char('∧')),
    Association.LEFT,
    { a, b -> VBool(a.value && b.value) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object Or : BinaryOperator<VBool, VBool>(
    2,
    "∨",
    orEither(_char('|'), _char('v')),
    Association.LEFT,
    { a, b -> VBool(a.value || b.value) }
) {
    override fun type(firstType: Type, secondType: Type): Type = BOOL
}

@KParcelize
data object Sign : UnaryOperator<VWhole, VNum<BigInteger>>(
    20,
    "-",
    _char('-'),
    Fixation.PREFIX,
    { VWhole(-it.value) }
) {
    override fun type(paramType: Type): Type = WHOLE
}

@KParcelize
data object Not : UnaryOperator<VBool, VBool>(
    20,
    "¬",
    orEither(_char('!'), _char('¬')),
    Fixation.PREFIX,
    { VBool(!it.value) }
) {
    override fun type(paramType: Type): Type = BOOL
}

val builtInOperators = arrayListOf(
    Add, Sub, Sign, Mul, Div,
    And, Or, Not,
    SmallerEq, LargerEq, Smaller, Larger, Equal, NotEqual
)

@Suppress("UNCHECKED_CAST")
val builtInOperatorTable: OperatorTable<Value> =
    OperatorTable(builtInOperators as List<Operator<Exp<Value>>>)
