package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.assertTrue
import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.operator.BinaryOperator
import com.erdodif.capsulate.lang.program.grammar.operator.UnaryCalculation
import com.erdodif.capsulate.lang.program.grammar.operator.UnaryOperator
import com.erdodif.capsulate.lang.util.Association
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Fixation
import com.erdodif.capsulate.lang.util.OperatorTable
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.tok
import com.erdodif.capsulate.matches
import kotlin.test.Test

class OperatorTest {
    private companion object {
        @KParcelize
        private data class TestValue(val char: Char) : Value {
            override fun equals(other: Any?) =
                (other is TestExp && other.matchedChar == char) ||
                        (other is TestValue && other.char == char)

            override fun hashCode() = char.hashCode()
        }

        @KParcelize
        private data class TestExp(val matchedChar: Char) : Exp<Value> {
            override fun evaluate(context: Env) = TestValue(matchedChar)
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
        val strongRight =
            BinaryOperator(20, "*", _char('*'), Association.RIGHT)
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
        val testAtom: Parser<Exp<Value>> = tok(asum(
            char('a'),
            char('b'),
            char('c'),
            char('d')
        )) / { TestExp(it) }

        fun OperatorTable<Exp<Value>>.parse(text: String) =
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

        infix fun OperatorTable<Exp<Value>>.pass(text: String): Pass<Exp<Value>> =
            this.parse(text).let(::assertPass)

        infix fun OperatorTable<Exp<Value>>.fail(text: String): Fail =
            this.parse(text).let(::assertFail)
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
        operatorsSimple fail "a b " at 2
        operatorsConflict fail "a b " at 2
    }

    @Test
    fun weakRight_Pass_single() {
        val expectedPredicate: (Pass<Exp<Value>>) -> Boolean = expectForSingle("+")
        operatorsSimple pass "a + b " matches expectedPredicate
        operatorsSimple pass "a +b " matches expectedPredicate
        operatorsSimple pass "a+ b " matches expectedPredicate
        operatorsSimple pass "a+b " matches expectedPredicate
    }

    @Test
    fun weakRight_Pass_chained() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForChainRight("+")
        operatorsSimple pass "a + b + c " matches expectedPredicate
        operatorsSimple pass "a +b+c " matches expectedPredicate
    }

    @Test
    fun weakRight_Fail() {
        operatorsSimple fail "a++b" at 1
        operatorsSimple fail "a++b" at 1
        operatorsSimple fail "a+b++c" at 3
        operatorsSimple fail "a+b c " at 4
        operatorsSimple fail "a b + c " at 2
        operatorsSimple fail "a\n + c " at 1
        operatorsSimple fail "a+ c\n" at 4
        operatorsSimple fail "+ c" at 0
        operatorsSimple fail "+c" at 0
    }

    @Test
    fun weakLeft_Pass_single() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForSingle("-")
        operatorsSimple pass "a - b " matches expectedPredicate
        operatorsSimple pass "a -b" matches expectedPredicate
        operatorsSimple pass "a- b" matches expectedPredicate
        operatorsSimple pass "a-b" matches expectedPredicate
    }

    @Test
    fun weakLeft_Pass_chained() {
        val expectedPredicate: (Pass<*>) -> Boolean = expectForChainLeft("-")
        operatorsSimple pass "a - b - c " matches expectedPredicate
        operatorsSimple pass "a -b-c " matches expectedPredicate
    }

    @Test
    fun weakLeft_Fail() {
        operatorsSimple fail "a-+b" at 1
        operatorsSimple fail "a-+b-c" at 1
        operatorsSimple fail "a-b c" at 4
        operatorsSimple fail "a b - c" at 2
        operatorsSimple fail "a\n - c" at 1
        operatorsSimple fail "a- c\n" at 4
        operatorsSimple fail "- c" at 0
        operatorsSimple fail "-c" at 0
    }

    @Test
    fun unaryPreLeft_Pass() {
        val expectPredicate: (Pass<Exp<Value>>) -> Boolean =
            { it.value is UnaryCalculation && ((it.value as UnaryCalculation).param as TestExp).matchedChar == 'a' }
        operatorsSimple pass "@a" matches expectPredicate
        operatorsSimple pass "@ a" matches expectPredicate
        operatorsSimple pass "@  a " matches expectPredicate
    }

    @Test
    fun unaryPostRight_Fail() {
        operatorsSimple fail "a@" at 1
        operatorsSimple fail "@\n a" at 0
        operatorsSimple fail "b @ a " at 2
    }

    @Test
    fun weakNone_Pass() {
        operatorsSimple pass "a = b"
        operatorsSimple pass "a = b + c"
        operatorsSimple pass "a + b = c "
        operatorsSimple pass "a + b = c + d"
        operatorsSimple pass "a = @d"
    }

    @Test
    fun weakNone_Fail() {
        operatorsSimple fail "a = b = c" at 9
        operatorsSimple fail "a =" at 3
        operatorsSimple fail "= a" at 0
        operatorsSimple fail "a + b = a - b = c" at 17
    }

    //TODO: Mixing binding strengths
}