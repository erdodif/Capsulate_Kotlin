package com.erdodif.capsulate.lang.grammar.operator

import com.erdodif.capsulate.lang.grammar.VBool
import com.erdodif.capsulate.lang.grammar.VNat
import com.erdodif.capsulate.lang.grammar.VNum
import com.erdodif.capsulate.lang.grammar.VWhole
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.grammar.type


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

// FunctionCall TODO
