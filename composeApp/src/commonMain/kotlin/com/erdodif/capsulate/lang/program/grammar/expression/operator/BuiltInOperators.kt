package com.erdodif.capsulate.lang.program.grammar.expression.operator

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
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

// TODO - CAST ON ASSIGNMENT SIDE

@KParcelize
object Add : BinaryOperator<VNum, VNum>(
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
)

@KParcelize
object Sub : BinaryOperator<VNum, VNum>(
    12,
    "-",
    _char('-'),
    Association.LEFT,
    { a, b -> VWhole(a.value - b.value) }
)

@KParcelize
object Mul : BinaryOperator<VNum, VNum>(
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
)

@KParcelize
object Div : BinaryOperator<VNum, VNum>(
    16,
    "/",
    _char('/'),
    Association.LEFT,
    { a, b ->
        if (b.value == 0) {
            throw RuntimeException("Division by Zero!")
        }
        if (a is VNat) {
            VNat((a.value / b.value).toUInt())
        } else {
            VWhole(a.value / b.value)
        }
    }
)

@KParcelize
object Larger : BinaryOperator<VBool, VNum>(
    5,
    ">",
    _char('>'),
    Association.NONE,
    { a, b -> VBool(a.value > b.value) }
)

@KParcelize
object Smaller : BinaryOperator<VBool, VNum>(
    5,
    "<",
    _char('<'),
    Association.NONE,
    { a, b -> VBool(a.value < b.value) }
)

@KParcelize
object LargerEq : BinaryOperator<VBool, VNum>(
    5,
    "≥",
    or(_keyword(">="), _char('≥')),
    Association.NONE,
    { a, b -> VBool(a.value >= b.value) }
)

@KParcelize
object SmallerEq : BinaryOperator<VBool, VNum>(
    5,
    "≤",
    or(_keyword("<="), _char('≤')),
    Association.NONE,
    { a, b -> VBool(a.value <= b.value) }
)

@KParcelize
object Equal : BinaryOperator<Value, Value>(
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
)

@KParcelize
object And : BinaryOperator<VBool, VBool>(
    6,
    "∧",
    orEither(_char('&'),_char('∧')),
    Association.LEFT,
    { a, b -> VBool(a.value && b.value) }
)

@KParcelize
object Or : BinaryOperator<VBool, VBool>(
    5,
    "∨",
    orEither(_char('|'), _char('v')),
    Association.LEFT,
    { a, b -> VBool(a.value || b.value) }
)

@KParcelize
object Sign : UnaryOperator<VNum, VWhole>(
    20,
    "-",
    _char('-'),
    Fixation.PREFIX,
    { VWhole(-it.value) }
)

@KParcelize
object Not : UnaryOperator<VBool, VBool>(
    20,
    "¬",
    orEither(_char('!'),_char('¬')),
    Fixation.PREFIX,
    { VBool(!it.value) }
)

val builtInOperators = arrayListOf(
    Add, Sub, Sign, Mul, Div,
    And, Or, Not,
    SmallerEq, LargerEq, Smaller, Larger, Equal
)

@Suppress("UNCHECKED_CAST")
val builtInOperatorTable: OperatorTable<Exp<Value>> =
    OperatorTable(builtInOperators as ArrayList<Operator<Exp<Value>>>)
