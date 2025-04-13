package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.VNat
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.program.grammar.expression.VWhole
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.type
import com.erdodif.capsulate.lang.program.grammar.or
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword

@KParcelize
data object Add : BinaryOperator<VNum, VNum>(
    14,
    "+",
    _char('+'),
    Association.RIGHT,
    { a, b ->
        if (a is VNat && b is VNat) {
            VNat((a.value + b.value).toUInt())
        } else {
            VWhole(a.value + b.value)
        }
    }
) {
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == Type.NAT && secondType == Type.NAT) Type.NAT else Type.WHOLE
}

@KParcelize
data object Sub : BinaryOperator<VNum, VNum>(
    12,
    "-",
    _char('-'),
    Association.LEFT,
    { a, b -> VWhole(a.value - b.value) }
) {
    override fun type(firstType: Type, secondType: Type): Type = Type.WHOLE
}

@KParcelize
data object Mul : BinaryOperator<VNum, VNum>(
    18,
    "*",
    _char('*'),
    Association.RIGHT,
    { a, b ->
        if (a is VNat) {
            VNat((a.value * b.value).toUInt())
        } else {
            VWhole(a.value * b.value)
        }
    }
){
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == Type.NAT && secondType == Type.NAT) Type.NAT else Type.WHOLE
}

@KParcelize
data object Div : BinaryOperator<VNum, VNum>(
    16,
    "/",
    _char('/'),
    Association.LEFT,
    { a, b ->
        if (b.value == 0) {
            error("Division by Zero!")
        }
        if (a is VNat) {
            VNat((a.value / b.value).toUInt())
        } else {
            VWhole(a.value / b.value)
        }
    }
){
    override fun type(firstType: Type, secondType: Type): Type =
        if (firstType == Type.NAT && secondType == Type.NAT) Type.NAT else Type.WHOLE
}

@KParcelize
data object Larger : BinaryOperator<VBool, VNum>(
    5,
    ">",
    _char('>'),
    Association.NONE,
    { a, b -> VBool(a.value > b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object Smaller : BinaryOperator<VBool, VNum>(
    5,
    "<",
    _char('<'),
    Association.NONE,
    { a, b -> VBool(a.value < b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object LargerEq : BinaryOperator<VBool, VNum>(
    5,
    "≥",
    or(_keyword(">="), _char('≥')),
    Association.NONE,
    { a, b -> VBool(a.value >= b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object SmallerEq : BinaryOperator<VBool, VNum>(
    5,
    "≤",
    or(_keyword("<="), _char('≤')),
    Association.NONE,
    { a, b -> VBool(a.value <= b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object Equal : BinaryOperator<Value, Value>(
    4,
    "=",
    _char('='),
    Association.NONE,
    { a, b ->
        if (a.type() != b.type()) {
            VBool(false)
        } else {
            VBool(a == b)
        }
    }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
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
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object And : BinaryOperator<VBool, VBool>(
    6,
    "∧",
    orEither(_char('&'), _char('∧')),
    Association.LEFT,
    { a, b -> VBool(a.value && b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object Or : BinaryOperator<VBool, VBool>(
    5,
    "∨",
    orEither(_char('|'), _char('v')),
    Association.LEFT,
    { a, b -> VBool(a.value || b.value) }
){
    override fun type(firstType: Type, secondType: Type): Type = Type.BOOL
}

@KParcelize
data object Sign : UnaryOperator<VNum, VWhole>(
    20,
    "-",
    _char('-'),
    Fixation.PREFIX,
    { VWhole(-it.value) }
){
    override fun type(param: Type): Type = Type.WHOLE
}

@KParcelize
data object Not : UnaryOperator<VBool, VBool>(
    20,
    "¬",
    orEither(_char('!'), _char('¬')),
    Fixation.PREFIX,
    { VBool(!it.value) }
){
    override fun type(param: Type): Type = Type.BOOL
}

val builtInOperators = arrayListOf(
    Add, Sub, Sign, Mul, Div,
    And, Or, Not,
    SmallerEq, LargerEq, Smaller, Larger, Equal, NotEqual
)

@Suppress("UNCHECKED_CAST")
val builtInOperatorTable: OperatorTable<Value> =
    OperatorTable(builtInOperators as List<Operator<Exp<Value>>>)
