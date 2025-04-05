package com.erdodif.capsulate.lang.parsers

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.Pattern
import com.erdodif.capsulate.lang.program.grammar.function.sSinglePattern
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import kotlin.test.Test

class PatternTest {
    private val preOnlySingleWord = Pattern("foo", emptyList(), emptyList(), null)
    private val preOnlyMultiWord = Pattern("foo bar", emptyList(), emptyList(), null)

    private fun variableOf(id: String) = Variable(id, MatchPos.ZERO)
    private fun stateOf(text: String, vararg patterns: Pattern): ParserState =
        ParserState(text, mutableListOf(), patterns.map { Method(it, listOf()) }.toMutableList())

    private infix fun Pattern.pass(text: String) =
        topLevel { sSinglePattern(this@pass) }(stateOf(text, this)).let(::assertPass)

    private infix fun Pattern.fail(text: String) =
        topLevel { sSinglePattern(this@fail) }(stateOf(text, this)).let(::assertFail)

    private infix fun Pattern.passLocal(text: String) =
        (stateOf(text, this).sSinglePattern(this@passLocal)).let(::assertPass)

    private infix fun Pattern.failLocal(text: String) =
        (stateOf(text, this).sSinglePattern(this@failLocal)).let(::assertFail)

    @Test
    fun `pattern pass with only single word prefix`() {
        preOnlySingleWord pass "foo"
        preOnlySingleWord passLocal "foo bar"
    }

    @Test
    fun `pattern pass with only multi word prefix`() {
        preOnlyMultiWord pass "foo bar"
        preOnlyMultiWord passLocal "foo bar baz"
    }

    @Test
    fun `pattern fail with only single word postfix on longer word`() {
        preOnlySingleWord fail "foos"
        preOnlySingleWord failLocal "foos bars"
    }

    @Test
    fun `pattern pass with only single word postfix on shorter word`() {
        preOnlySingleWord fail "fo"
        preOnlySingleWord failLocal "fo bar"
    }

    @Test
    fun `pattern fail with multi word postfix on longer word`() {
        preOnlyMultiWord fail "foos bar"
        preOnlyMultiWord fail "foo bars"
        preOnlyMultiWord failLocal "foos bars"
        preOnlyMultiWord failLocal "foo bars"
    }

    @Test
    fun `pattern pass with multi word postfix on shorter word`() {
        preOnlyMultiWord fail "fo bar"
        preOnlyMultiWord failLocal "fo bar"
        preOnlyMultiWord fail "foo ba"
        preOnlyMultiWord failLocal "foo ba"
    }

    /**
     * Natural occurrence of postfix-only patterns
     * can't happen based on sPattern's behavior,
     * though they are supported for weak built-ins
     */
    @Test
    fun `pattern pass with only postfix`() {
        val postOnlySingleWord = Pattern(null, emptyList(), emptyList(), "foo")
        val postOnlyMultiWord = Pattern(null, emptyList(), emptyList(), "foo bar")
        postOnlySingleWord pass "foo"
        postOnlyMultiWord pass "foo bar"
        postOnlySingleWord passLocal "foo bar"
        postOnlyMultiWord passLocal "foo bar baz"
    }

    @Test
    fun `pattern pass delimiters without pre and postfix`() {
        val pattern = Pattern(null, listOf(","), listOf(variableOf("a"), variableOf("b")), null)
        pattern pass "a,b"
        pattern pass "a,a"
        pattern pass "a, b"
        pattern pass "a, a"
        pattern pass "b, b"
        pattern pass "a, b "
        pattern pass "a , a "
        pattern pass "a , b "
    }

    @Test
    fun `pattern pass delimiters with space`() {
        val pattern = Pattern(null, listOf(","), listOf(variableOf("a"), variableOf("b")), null)
        pattern pass "a,b"
        pattern pass "a, b"
        pattern pass "a , b "
    }

    @Test
    fun `pattern pass delimiters with reserved first char in prefix`() {
        val pattern = Pattern("a,", listOf(","), listOf(variableOf("a"), variableOf("b")), null)
        pattern pass "a,a,b"
        pattern pass "a,b,b"
        pattern pass "a,b , b"
        pattern pass "a, b , b"
        pattern pass "a, b , b "
    }

    @Test
    fun `pattern fails delimiters on split prefix`() {
        val pattern = Pattern("a,", listOf(","), listOf(variableOf("a"), variableOf("b")), null)
        pattern fail "a , a , b"
    }

    @Test
    fun `pattern pass delimiters with reserved first char in postfix`() {
        val pattern = Pattern(null, listOf(","), listOf(variableOf("a"), variableOf("b")), ",b")
        pattern pass "a,a,b"
        pattern pass "a, b,b"
        pattern pass "a ,b,b"
        pattern pass "a, b,b"
    }

    @Test
    fun `pattern pass delimiters with multiword prefix`() {
        val pattern = Pattern(null, listOf(","), listOf(variableOf("a"), variableOf("b")), null)
        pattern pass "a,b"
    }

    @Test
    fun `pattern fails delimiters on split postfix`() {
        val pattern = Pattern("a_,", listOf(","), listOf(variableOf("a"), variableOf("b")), ",c")
        pattern fail "a , a , c"
    }


    @Test
    fun `pattern pass delimiters with multiword postfix`() {
        val pattern = Pattern(null, listOf(","), listOf(variableOf("a"), variableOf("b")), ",_c")
        pattern pass "a, a, c"
        pattern pass "a, a , c"
        pattern pass "a,b , c"
        pattern pass "a ,a , c"
        pattern pass "a , b ,c"
        pattern pass "a , a , c"
    }
}
