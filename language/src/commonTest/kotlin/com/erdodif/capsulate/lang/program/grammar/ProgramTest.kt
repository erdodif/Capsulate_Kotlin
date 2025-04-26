package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.grammar.expression.Index
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.util.bg
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
            assertEquals("a", a.label.id)
            assertIs<IntLit>(a.value)
            assertEquals(1.bg, a.value.value)
            assertEquals(3, a.value.match.start)
            assertEquals(4, a.value.match.end)
            assertIs<Assign>(b)
            assertEquals("b", b.label.id)
            assertIs<StrLit>(b.value)
            assertEquals("2", b.value.value)
            assertIs<Assign>(c)
            assertEquals("c", c.label.id)
            assertIs<Index>(c.value)
            assertEquals("a", c.value.id)
        }
    }

    @Test
    fun `subsequent assignment keeps labels intact`() {
        with((topLevel(program) pass "a:=1\nb:=\"2\"\nc:=a").value.statements) {
            assertEquals(3, size)
            val (a, b, c) = this
            assertIs<Assign>(a)
            assertEquals("a", a.label.id)
            assertIs<IntLit>(a.value)
            assertEquals(1.bg, a.value.value)
            assertIs<Assign>(b)
            assertEquals("b", b.label.id)
            assertIs<StrLit>(b.value)
            assertEquals("2", b.value.value)
            assertIs<Assign>(c)
            assertEquals("c", c.label.id)
            assertIs<Index>(c.value)
            assertEquals("a", c.value.id)
        }
    }
}
