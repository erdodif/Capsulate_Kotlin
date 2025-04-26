package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.assert
import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.util.bg
import com.erdodif.capsulate.pass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IndexTest {

    private val index = topLevel(pIndex)
    private val assign = topLevel(sAssign)

    @Test
    fun `indexer parses int alone`() {
        index pass "a[1]" assert {
            assertEquals("a", it.value.id)
            assertEquals(1, it.value.indexers.size)
            val index = it.value.indexers.first()
            assertIs<IntLit>(index)
            assertEquals(1.bg, index.value)
        } at 4
    }

    @Test
    fun `indexer parses int double`() {
        index pass "a[1][3]" assert {
            assertEquals("a", it.value.id)
            assertEquals(2, it.value.indexers.size)
            val (first, second) = it.value.indexers
            assertIs<IntLit>(first)
            assertEquals(1.bg, first.value)
            assertIs<IntLit>(second)
            assertEquals(3.bg, second.value)
        } at 7
    }

    @Test
    fun `indexer passes without indexer`() {
        index pass "a" at 1
    }

    @Test
    fun `assignment passes with indexer at the right position`(){
        assign pass "a[1] := 2" at 9
        assign pass "a[1][2] := b[1]" at 15
    }

    @Test
    fun `assignment passes with array literal`(){
        assign pass "a[1] := [2,3]" at 13
        assign pass "a[1][2] := [1,2,3,4]" at 20
    }
}
