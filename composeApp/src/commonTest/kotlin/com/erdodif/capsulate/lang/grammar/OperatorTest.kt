package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertTrue
import com.erdodif.capsulate.lang.program.grammar.Exp
import com.erdodif.capsulate.lang.program.grammar.Value
import com.erdodif.capsulate.lang.program.grammar.operator.Association
import com.erdodif.capsulate.lang.program.grammar.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.operator.BinaryOperator
import com.erdodif.capsulate.lang.program.grammar.operator.Fixation
import com.erdodif.capsulate.lang.program.grammar.operator.OperatorTable
import com.erdodif.capsulate.lang.program.grammar.operator.UnaryCalculation
import com.erdodif.capsulate.lang.program.grammar.operator.UnaryOperator
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.freeChar
import com.erdodif.capsulate.lang.util.tok
import kotlin.test.Test

class OperatorTest {

    private companion object {
        private data class TestValue(val char: Char) : Value {
            override fun equals(other: Any?) =
                (other is TestExp && other.matchedChar == char) ||
                        (other is TestValue && other.char == char)

            override fun hashCode() = char.hashCode()
        }

        private data class TestExp(val matchedChar: Char) : Exp<Value> {
            override fun evaluate(env: Env) = TestValue(matchedChar)
            override fun toString(state: ParserState) = matchedChar.toString()
            override fun equals(other: Any?) =
                (other is TestExp && other.matchedChar == matchedChar) ||
                        (other is TestValue && other.char == matchedChar)

            override fun hashCode(): Int = matchedChar.hashCode()
        }

        val strongLeft = BinaryOperator(20, "/", _char('/'), Association.LEFT)
        { _, _ -> TestValue('/') }
        val weakLeft = BinaryOperator(10, "-", _char('-'), Association.LEFT)
        { _, _ -> TestValue('-') }
        val strongRight = BinaryOperator(20, "*", _char('*'), Association.RIGHT)
        { _, _ -> TestValue('*') }
        val weakRight = BinaryOperator(10, "+", _char('+'), Association.RIGHT)
        { _, _ -> TestValue('+') }
        val unaryPreLeft = UnaryOperator(20, "-", _char('-'), Fixation.PREFIX)
        { _ -> TestValue('~') }
        val unaryPre = UnaryOperator(20, "~", _char('@'), Fixation.PREFIX)
        { _ -> TestValue('~') }
        val unaryPostLeft = UnaryOperator(20, "~", _char('*'), Fixation.POSTFIX)
        { _ -> TestValue('~') }
        val unaryPost = UnaryOperator(20, "~", _char('#'), Fixation.POSTFIX)
        { _ -> TestValue('~') }
        val strongNone = BinaryOperator(10, "_", _char('_'), Association.NONE)
        { _, _ -> TestValue('_') }
        val weakNone = BinaryOperator(5, "=", _char('='), Association.NONE)
        { _, _ -> TestValue('=') }

        val operatorsConflict = OperatorTable(
            strongLeft, strongRight, weakLeft, weakRight, unaryPreLeft,
            unaryPre, unaryPostLeft, unaryPost, strongNone, weakNone
        )
        val operatorsSimple = OperatorTable(weakLeft, weakRight, weakNone, unaryPre, unaryPost)
        val testAtom: Parser<Exp<*>> = tok(freeChar) / { TestExp(it) }

        fun OperatorTable.parse(text: String) =
            ParserState(text).parse(topLevel(verboseParser(testAtom)))

        fun expectForSingle(
            label: String,
            firstChar: Char = 'a',
            secondChar: Char = 'b'
        ): (Pass<*>) -> Boolean = {
            val calc = it.value as BinaryCalculation
            calc.label == label &&
                    (calc.first as TestExp).matchedChar == firstChar &&
                    (calc.second as TestExp).matchedChar == secondChar
        }

        fun expectForChainLeft(
            label: String,
            labelSecond: String = label,
            firstChar: Char = 'a',
            secondChar: Char = 'b',
            thirdChar: Char = 'c'
        ): (Pass<*>) -> Boolean = {
            val calc1 = it.value as BinaryCalculation
            val calc2 = calc1.first as BinaryCalculation
            calc1.label == label &&
                    calc2.label == labelSecond &&
                    (calc2.first as TestExp).matchedChar == firstChar &&
                    (calc2.second as TestExp).matchedChar == secondChar &&
                    (calc1.second as TestExp).matchedChar == thirdChar
        }

        fun expectForChainRight(
            label: String,
            labelSecond: String = label,
            firstChar: Char = 'a',
            secondChar: Char = 'b',
            thirdChar: Char = 'c'
        ): (Pass<*>) -> Boolean = {
            val calc1 = it.value as BinaryCalculation
            val calc2 = calc1.second as BinaryCalculation
            calc1.label == label &&
                    calc2.label == labelSecond &&
                    (calc1.first as TestExp).matchedChar == firstChar &&
                    (calc2.first as TestExp).matchedChar == secondChar &&
                    (calc2.second as TestExp).matchedChar == thirdChar
        }
    }

    @Test
    fun single_value_Pass() {
        assertTrue(
            { it.value is TestExp && (it.value as TestExp).matchedChar == 'a' },
            operatorsSimple.parse("a ")
        )
        assertTrue(
            { it.value is TestExp && (it.value as TestExp).matchedChar == 'b' },
            operatorsConflict.parse("b ")
        )
    }

    @Test
    fun simple_value_Fail() {
        assertFail(operatorsSimple.parse("a b "))
        assertFail(operatorsConflict.parse("a b "))
    }

    @Test
    fun weakRight_Pass_single() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForSingle("+")
        assertTrue(expectedPredicate, operatorsSimple.parse("a + b "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a +b "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a+ b "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a+b "))
    }

    @Test
    fun weakRight_Pass_chained() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForChainRight("+")
        assertTrue(expectedPredicate, operatorsSimple.parse("a + b + c "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a +b+c "))
    }

    @Test
    fun weakRight_Fail() {
        assertFail(operatorsSimple.parse("a++b"))
        assertFail(operatorsSimple.parse("a+b++c"))
        assertFail(operatorsSimple.parse("a+b c "))
        assertFail(operatorsSimple.parse("a b + c "))
        assertFail(operatorsSimple.parse("a\n + c "))
        assertFail(operatorsSimple.parse("a+ c\n"))
        assertFail(operatorsSimple.parse("+ c"))
        assertFail(operatorsSimple.parse("+c"))
    }

    @Test
    fun weakLeft_Pass_single() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForSingle("-")
        assertTrue(expectedPredicate, operatorsSimple.parse("a - b "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a -b"))
        assertTrue(expectedPredicate, operatorsSimple.parse("a- b"))
        assertTrue(expectedPredicate, operatorsSimple.parse("a-b"))
    }

    @Test
    fun weakLeft_Pass_chained() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForChainLeft("-")
        assertTrue(expectedPredicate, operatorsSimple.parse("a - b - c "))
        assertTrue(expectedPredicate, operatorsSimple.parse("a -b-c "))
    }

    @Test
    fun weakLeft_Fail() {
        assertFail(operatorsSimple.parse("a-+b"))
        assertFail(operatorsSimple.parse("a-+b-c"))
        assertFail(operatorsSimple.parse("a-b c"))
        assertFail(operatorsSimple.parse("a b - c"))
        assertFail(operatorsSimple.parse("a\n - c"))
        assertFail(operatorsSimple.parse("a- c\n"))
        assertFail(operatorsSimple.parse("- c"))
        assertFail(operatorsSimple.parse("-c"))
    }

    @Test
    fun unaryPreLeft_Pass() {
        val expectPredicate: (Pass<Exp<*>>) -> Boolean =
            { it.value is UnaryCalculation && ((it.value as UnaryCalculation).param as TestExp).matchedChar == 'a' }
        assertTrue(expectPredicate, operatorsSimple.parse("@a"))
        assertTrue(expectPredicate, operatorsSimple.parse("@ a"))
        assertTrue(expectPredicate, operatorsSimple.parse("@  a "))
    }

    @Test
    fun unaryPostRight_Fail() {
        assertFail(operatorsSimple.parse("a@"))
        assertFail(operatorsSimple.parse("@\n a"))
        assertFail(operatorsSimple.parse("b @ a "))
    }

    @Test
    fun weakNone_Pass() {
        val expectPredicate: (Pass<Exp<*>>) -> Boolean = { true }
        assertTrue(expectPredicate, operatorsSimple.parse("a = b"))
        assertTrue(expectPredicate, operatorsSimple.parse("a = b + c"))
        assertTrue(expectPredicate, operatorsSimple.parse("a + b = c "))
        assertTrue(expectPredicate, operatorsSimple.parse("a + b = c + d"))
        assertTrue(expectPredicate, operatorsSimple.parse("a = @d"))
    }

    @Test
    fun weakNone_Fail() {
        assertFail(operatorsSimple.parse("a = b = c"))
        assertFail(operatorsSimple.parse("a ="))
        assertFail(operatorsSimple.parse("= a"))
        assertFail(operatorsSimple.parse("a + b = a - b = c"))
    }

    //TODO: Mixing binding strengths
}