package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.assertTrue
import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.operator.BinaryCalculation
import com.erdodif.capsulate.lang.program.grammar.expression.operator.BinaryOperator
import com.erdodif.capsulate.lang.program.grammar.expression.operator.UnaryCalculation
import com.erdodif.capsulate.lang.program.grammar.expression.operator.UnaryOperator
import com.erdodif.capsulate.lang.program.evaluation.Env
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Association
import com.erdodif.capsulate.lang.program.grammar.expression.operator.Fixation
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.program.grammar.expression.operator.OperatorTable
import com.erdodif.capsulate.lang.util.Left
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
        private data class TestExp(val matchedChar: Char) : Exp<TestValue> {
            override fun evaluate(context: Env) = Left(TestValue(matchedChar))
            override fun toString(state: ParserState, parentStrength: Int) = matchedChar.toString()
            override fun equals(other: Any?) =
                (other is TestExp && other.matchedChar == matchedChar) ||
                        (other is TestValue && other.char == matchedChar)

            override fun hashCode(): Int = matchedChar.hashCode()
        }

        val strongLeft = BinaryOperator<TestValue, TestValue>(20, "/", _char('/'), Association.LEFT)
        { _, _ -> TestValue('/') }
        val weakLeft = BinaryOperator<TestValue, TestValue>(10, "-", _char('-'), Association.LEFT)
        { _, _ -> TestValue('-') }
        val strongRight =
            BinaryOperator<TestValue, TestValue>(20, "*", _char('*'), Association.RIGHT)
            { _, _ -> TestValue('*') }
        val weakRight = BinaryOperator<TestValue, TestValue>(10, "+", _char('+'), Association.RIGHT)
        { _, _ -> TestValue('+') }
        val unaryPreLeft = UnaryOperator<TestValue, TestValue>(20, "-", _char('-'), Fixation.PREFIX)
        { _ -> TestValue('~') }
        val unaryPre = UnaryOperator<TestValue, TestValue>(20, "@", _char('@'), Fixation.PREFIX)
        { _ -> TestValue('~') }
        val unaryPostLeft =
            UnaryOperator<TestValue, TestValue>(20, "~*", _char('*'), Fixation.POSTFIX)
            { _ -> TestValue('~') }
        val unaryPost = UnaryOperator<TestValue, TestValue>(20, "#", _char('#'), Fixation.POSTFIX)
        { _ -> TestValue('~') }
        val strongNone = BinaryOperator<TestValue, TestValue>(10, "_", _char('_'), Association.NONE)
        { _, _ -> TestValue('_') }
        val weakNone = BinaryOperator<TestValue, TestValue>(5, "=", _char('='), Association.NONE)
        { _, _ -> TestValue('=') }

        val operatorsConflict = OperatorTable(
            strongLeft, strongRight, weakLeft, weakRight, unaryPreLeft,
            unaryPre, unaryPostLeft, unaryPost, strongNone, weakNone
        )
        val testAtom: Parser<Exp<TestValue>> = tok(
            asum(
                char('a'),
                char('b'),
                char('c'),
                char('d')
            )
        ) / { TestExp(it) }
        val operatorsSimple =
            OperatorTable<TestValue>(weakLeft, weakRight, weakNone, unaryPre, unaryPost)

        fun OperatorTable<TestValue>.parse(text: String) =
            ParserState(text).parse(topLevel(parser(testAtom)))

        fun expectForSingle(
            label: String,
            firstChar: Char = 'a',
            secondChar: Char = 'b'
        ): (Pass<*>) -> Boolean = {
            val calc = it.value as BinaryCalculation<*, *>
            calc.operator.label == label &&
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
            val calc1 = it.value as BinaryCalculation<*, *>
            val calc2 = calc1.first as BinaryCalculation<*, *>
            calc1.operator.label == label &&
                    calc2.operator.label == labelSecond &&
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
            val calc1 = it.value as BinaryCalculation<*, *>
            val calc2 = calc1.second as BinaryCalculation<*, *>
            calc1.operator.label == label &&
                    calc2.operator.label == labelSecond &&
                    (calc1.first as TestExp).matchedChar == firstChar &&
                    (calc2.first as TestExp).matchedChar == secondChar &&
                    (calc2.second as TestExp).matchedChar == thirdChar
        }

        infix fun OperatorTable<TestValue>.pass(text: String): Pass<Exp<TestValue>> =
            this.parse(text).let(::assertPass)

        infix fun OperatorTable<TestValue>.fail(text: String): Fail =
            this.parse(text).let(::assertFail)
    }

    @Test
    fun single_value_Pass() {
        assertTrue(
            { it.value is TestExp && it.value.matchedChar == 'a' },
            operatorsSimple.parser(testAtom)(ParserState("a"))
        )
        assertTrue(
            { it.value is TestExp && it.value.matchedChar == 'b' },
            operatorsConflict.parser(testAtom)(ParserState("b"))
        )
    }

    @Test
    fun simple_value_Fail() {
        operatorsSimple fail "a b " at 2
        operatorsConflict fail "a b " at 2
    }

    @Test
    fun weakRight_Pass_single() {
        val expectedPredicate: (Pass<Exp<TestValue>>) -> Boolean = expectForSingle("+")
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
    fun unaryPre_Pass() {
        val expectPredicate: (Pass<Exp<TestValue>>) -> Boolean =
            { it.value is UnaryCalculation<*, *> && ((it.value as UnaryCalculation<*, *>).param as TestExp).matchedChar == 'a' }
        operatorsSimple pass "@a" matches expectPredicate
        operatorsSimple pass "@ a" matches expectPredicate
        operatorsSimple pass "@  a " matches expectPredicate
    }

    @Test
    fun unaryPre_Fail() {
        operatorsSimple fail "a@" at 1
        operatorsSimple fail "a @" at 2
        operatorsSimple fail "a @ b" at 2
    }

    @Test
    fun unaryPost_Pass() {
        operatorsSimple pass "a#" at 2
        operatorsSimple pass "a #" at 3
    }

    @Test
    fun unaryPost_Fail() {
        operatorsSimple fail "#a" at 1
        operatorsSimple fail "a\n #" at 1
        operatorsSimple fail "b # a " at 4
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
        operatorsSimple fail "a =" at 2
        operatorsSimple fail "= a" at 0
        operatorsSimple fail "a + b = a - b = c" at 17
    }

    @Test
    fun `multiple unary on the same operand`(){
        operatorsConflict pass "-a"
        operatorsConflict pass "@a"
        operatorsConflict pass "-@a"
        operatorsConflict pass "@-a"
        operatorsConflict pass "a*"
        operatorsConflict pass "a#"
        operatorsConflict pass "a*#"
        operatorsConflict pass "a#*"
    }
}