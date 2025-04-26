package com.erdodif.capsulate.parcelable

import android.os.Bundle
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.erdodif.capsulate.lang.program.grammar.ParallelAssign
import com.erdodif.capsulate.lang.program.grammar.expression.Index
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.bg
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class StatementParcelTest {

    val pos = MatchPos.ZERO

    @Test
    fun parallel_assign_in_bundle() {
        val underTest = ParallelAssign(
            listOf(
                Index("a") to IntLit(2, pos),
                Index("b") to IntLit(3, pos),
            ), pos
        )
        val bundle = Bundle()
        bundle.putParcelable("underTest", underTest)
        val result = bundle.getParcelable("underTest", underTest::class.java)
        assertIs<ParallelAssign>(result)
        assertEquals(2, result.assigns.size)
        val (a, b) = result.assigns
        assertIs<IntLit>(a.second)
        assertIs<IntLit>(b.second)
        assertEquals("a", a.first.id)
        assertEquals("b", b.first.id)
        assertTrue(a.first.indexers.isEmpty())
        assertTrue(b.first.indexers.isEmpty())
    }

    @Test
    fun index_to_be_Parcelable_in_bundle() {
        val underTest = Index("a", IntLit(1.bg, pos), IntLit(2.bg, pos))
        val bundle = Bundle()
        bundle.putParcelable("underTest", underTest)
        val result = bundle.getParcelable("underTest", Index::class.java)
        assertIs<Index>(result)
        assertEquals("a", result.id)
        assertEquals(2, result.indexers.size)
        val (first, second) = result.indexers
        assertIs<IntLit>(first)
        assertIs<IntLit>(second)
        assertEquals(1.bg, first.value)
        assertEquals(2.bg, second.value)
    }
}