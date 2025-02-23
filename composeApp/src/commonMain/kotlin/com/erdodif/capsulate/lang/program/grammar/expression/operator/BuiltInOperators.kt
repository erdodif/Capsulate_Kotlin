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
import com.erdodif.capsulate.lang.util._char
import kotlinx.serialization.Serializable

@KParcelize
@Serializable
object Add : BinaryOperator<VNum, VNum>(
    14,
    "+",
    _char('+'),
    Association.RIGHT,
    { a, b ->
        if (a !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (b !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (a.type() != b.type()) {
            throw RuntimeException("Operands must have the same type ('${a.type()}' + '${b.type()}' is not allowed by design)")
        }
        if (a is VNat) {
            VNat((a.value + b.value).toUInt())
        } else {
            VWhole(a.value + b.value)
        }
    }
)

@KParcelize
@Serializable
object Sub : BinaryOperator<VNum, VNum>(
    12,
    "-",
    _char('-'),
    Association.LEFT,
    { a, b ->
        if (a !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (b !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (a.type() != b.type()) {
            throw RuntimeException("Operands must have the same type ('${a.type()}' - '${b.type()}' is not allowed by design)")
        }
        if (a !is VNat) {
            VNat((a.value + b.value).toUInt())
        } else {
            VWhole(a.value + b.value)
        }
    }
)

@KParcelize
@Serializable
object Mul : BinaryOperator<VNum, VNum>(
    18,
    "*",
    _char('*'),
    Association.RIGHT,
    { a, b ->
        if (a !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (b !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (a.type() != b.type()) {
            throw RuntimeException("Operands must have the same type ('${a.type()}' * '${b.type()}' is not allowed by design)")
        }
        if (a !is VNat) {
            VNat((a.value * b.value).toUInt())
        } else {
            VWhole(a.value * b.value)
        }
    }
)

@KParcelize
@Serializable
object Div : BinaryOperator<VNum, VNum>(
    16,
    "/",
    _char('/'),
    Association.LEFT,
    { a, b ->
        if (a !is VNum) {
            throw RuntimeException("The first operand must be a Number!")
        }
        if (b !is VNum) {
            throw RuntimeException("The second operand must be a Number!")
        }
        if (a.type() != b.type()) {
            throw RuntimeException("Operands must have the same type ('${a.type()}' / '${b.type()}' is not allowed by design)")
        }
        if (a !is VNat) {
            VNat((a.value * b.value).toUInt())
        } else {
            VWhole(a.value * b.value)
        }
    }
)

@KParcelize
@Serializable
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
@Serializable
object And: BinaryOperator<VBool, VBool>(
    6,
    "&",
    _char('&'),
    Association.LEFT,
    { a, b ->
        if (a !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        if (b !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        else{
            VBool(a.value && b.value)
        }
    }
)

@KParcelize
@Serializable
object Or: BinaryOperator<VBool, VBool>(
    5,
    "|",
    or(_char('|'), _char('v')),
    Association.LEFT,
    { a, b ->
        if (a !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        if (b !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        else{
            VBool(a.value || b.value)
        }
    }
)

@KParcelize
@Serializable
object Sign : UnaryOperator<VNum, VWhole>(
    20,
    "-",
    _char('-'),
    Fixation.PREFIX,
    {
        if (it is VNum) {
            VWhole(- it.value)
        } else {
            throw RuntimeException("Type must be a Number")
        }
    }
)

@KParcelize
@Serializable
object Not : UnaryOperator<VBool, VBool>(
    20,
    "!",
    _char('!'),
    Fixation.PREFIX,
    {
        if (it is VBool) {
            VBool(!it.value)
        } else {
            throw RuntimeException("Type must be Logical")
        }
    }
)

@KParcelize
@Serializable
object Factorial : UnaryOperator<VNum, VNum>(
    20,
    "!",
    _char('!'),
    Fixation.POSTFIX,
    {
        if (it is VNum) {
            val range =
            if (it.value > 0){
                1..it.value
            }
            else if (it.value < 0){
                it.value..<0
            }
            else {
                0..0
            }
            val result = range.fold(1, Int::times)
            VWhole(result)
        } else {
            throw RuntimeException("Type must be Logical")
        }
    }
)

val builtInOperators = arrayListOf(Add, Sub, Sign, Mul, Div, And, Or, Not, Equal)

@Suppress("UNCHECKED_CAST")
val builtInOperatorTable: OperatorTable<Exp<Value>> = OperatorTable(builtInOperators as ArrayList<Operator<Exp<Value>>>)
