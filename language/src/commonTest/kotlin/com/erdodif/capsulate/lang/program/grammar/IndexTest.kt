package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.assert
import com.erdodif.capsulate.at
import com.erdodif.capsulate.fail
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.pass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IndexTest {

    val index = topLevel(pIndex)

    @Test
    fun `indexer parses int alone`() {
        index pass "a[1]" assert {
            assertEquals("a", it.value.id)
            assertIs<IntLit>(it.value.indexer)
            assertEquals(1, it.value.indexer.value)
        } at 4
    }

    @Test
    fun `indexer parses int double`() {// TODO: Align test when multiple assignment is possible
        index pass "a[1][2]" assert {
            assertEquals("a", it.value.id)
            assertIs<IntLit>(it.value.indexer)
            assertEquals(1, it.value.indexer.value)
        } at 7
    }

    @Test
    fun `indexer fails on missing indexer`() {
        index fail "a" at 1
    }

    /* TODO: Continue
    @Test
    fun `assignment passes with indexer`(){
        sAssign
    }
    */
}