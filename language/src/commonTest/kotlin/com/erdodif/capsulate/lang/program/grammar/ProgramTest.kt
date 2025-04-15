package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.util.errorOrNull
import com.erdodif.capsulate.pass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProgramTest {

    @Test
    fun `halfProgram subsequent assignments`() {
        with((topLevel(halfProgram) pass "a:=1\nb:=\"2\"\nc:=a").value.statements) {
            assertEquals(3, size)
            val (a, b, c) = this
            assertIs<Assign>(a)
            assertEquals("a", a.label.errorOrNull)
            assertIs<IntLit>(a.value)
            assertEquals(1, a.value.value)
            assertEquals(3, a.value.match.start)
            assertEquals(4, a.value.match.end)
            assertIs<Assign>(b)
            assertEquals("b", b.label.errorOrNull)
            assertIs<StrLit>(b.value)
            assertEquals("2", b.value.value)
            assertIs<Assign>(c)
            assertEquals("c", c.label.errorOrNull)
            assertIs<Variable>(c.value)
            assertEquals("a", c.value.id)
        }
    }

    @Test
    fun `subsequent assignment keeps labels intact`() {
        with((topLevel(program) pass "a:=1\nb:=\"2\"\nc:=a").value.statements) {
            assertEquals(3, size)
            val (a, b, c) = this
            assertIs<Assign>(a)
            assertEquals("a", a.label.errorOrNull)
            assertIs<IntLit>(a.value)
            assertEquals(1, a.value.value)
            assertIs<Assign>(b)
            assertEquals("b", b.label.errorOrNull)
            assertIs<StrLit>(b.value)
            assertEquals("2", b.value.value)
            assertIs<Assign>(c)
            assertEquals("c", c.label.errorOrNull)
            assertIs<Variable>(c.value)
            assertEquals("a", c.value.id)
        }
    }
}
