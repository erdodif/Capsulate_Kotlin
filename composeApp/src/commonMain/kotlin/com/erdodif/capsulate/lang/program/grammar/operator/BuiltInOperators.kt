package com.erdodif.capsulate.lang.program.grammar.operator

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.program.grammar.VBool
import com.erdodif.capsulate.lang.program.grammar.VNat
import com.erdodif.capsulate.lang.program.grammar.VNum
import com.erdodif.capsulate.lang.program.grammar.VWhole
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.type
import com.erdodif.capsulate.lang.util.Association
import com.erdodif.capsulate.lang.util.Calculation
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Fixation
import com.erdodif.capsulate.lang.util.Operator
import com.erdodif.capsulate.lang.util.OperatorTable
import com.erdodif.capsulate.lang.util._char

@KParcelize
object Add : BinaryOperator(
    14,
    "+",
    _char('+'),
    Association.RIGHT,
    { a, b ->
        val resultFirst = a.evaluate(this)
        val resultSecond = b.evaluate(this)

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
            VNat((resultFirst.value + resultSecond.value).toUInt())
        } else {
            VWhole(resultFirst.value + resultSecond.value)
        }
    }
)

@KParcelize
object Sub : BinaryOperator(
    12,
    "-",
    _char('-'),
    Association.LEFT,
    { a, b ->
        val resultFirst = a.evaluate(this)
        val resultSecond = b.evaluate(this)

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
            VNat((resultFirst.value + resultSecond.value).toUInt())
        } else {
            VWhole(resultFirst.value + resultSecond.value)
        }
    }
)

@KParcelize
object Mul : BinaryOperator(
    18,
    "*",
    _char('*'),
    Association.RIGHT,
    { a, b ->
        val resultFirst = a.evaluate(this)
        val resultSecond = b.evaluate(this)
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
            VNat((resultFirst.value * resultSecond.value).toUInt())
        } else {
            VWhole(resultFirst.value * resultSecond.value)
        }
    }
)

@KParcelize
object Div : BinaryOperator(
    16,
    "/",
    _char('/'),
    Association.LEFT,
    { a, b ->
        val resultFirst = a.evaluate(this)
        val resultSecond = b.evaluate(this)
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
            VNat((resultFirst.value * resultSecond.value).toUInt())
        } else {
            VWhole(resultFirst.value * resultSecond.value)
        }
    }
)

@KParcelize
object Equal : BinaryOperator(
    4,
    "=",
    _char('='),
    Association.NONE,
    { a, b ->
        val value1 = a.evaluate(this)
        val value2 = b.evaluate(this)
        if (value1.type() != value2.type()) {
            VBool(false)
        } else {
            VBool(value1 == value2)
        }
    }
)

@KParcelize
object And: BinaryOperator(
    6,
    "&",
    _char('&'),
    Association.LEFT,
    { a, b ->
        val value1 = a.evaluate(this)
        if (value1 !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        val value2 = b.evaluate(this)
        if (value2 !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        else{
            VBool(value1.value && value2.value)
        }
    }
)

@KParcelize
object Or: BinaryOperator(
    5,
    "|",
    _char('|'),
    Association.LEFT,
    { a, b ->
        val value1 = a.evaluate(this)
        if (value1 !is VBool) {
            throw RuntimeException("First operand must be Logical")
        }
        val value2 = b.evaluate(this)
        if (value2 !is VBool) {
            throw RuntimeException("Second operand must be Logical")
        }
        else{
            VBool(value1.value || value2.value)
        }
    }
)

@KParcelize
object Sign : UnaryOperator(
    20,
    "-",
    _char('-'),
    Fixation.PREFIX,
    {
        val res = it.evaluate(this)
        if (res is VNum) {
            VWhole(- res.value)
        } else {
            throw RuntimeException("Type must be a Number")
        }
    }
)

@KParcelize
object Not : UnaryOperator(
    20,
    "!",
    _char('!'),
    Fixation.PREFIX,
    {
        val res = it.evaluate(this)
        if (res is VBool) {
            VBool(!res.value)
        } else {
            throw RuntimeException("Type must be Logical")
        }
    }
)

@KParcelize
object Factorial : UnaryOperator(
    20,
    "!",
    _char('!'),
    Fixation.POSTFIX,
    {
        val res = it.evaluate(this)
        if (res is VNum) {
            val range =
            if (res.value > 0){
                1..res.value
            }
            else if (res.value < 0){
                res.value..<0
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

// FunctionCall TODO

val builtInOperators = arrayListOf(Add, Sub, Sign, Mul, Div, And, Or, Not, Equal)

@Suppress("UNCHECKED_CAST")
val builtInOperatorTable: OperatorTable<Exp<Value>> = OperatorTable(builtInOperators as ArrayList<Operator<Exp<Value>>>)
