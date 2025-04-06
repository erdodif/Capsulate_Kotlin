package com.erdodif.capsulate.lang.util

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import kotlinx.serialization.Serializable

/**
 * Match position on a [ParserState], it's range is [[start],[end]) (the end is excluded)
 */
@KParcelize
@Serializable
data class MatchPos(val start: Int, val end: Int) : KParcelable {
    val length : Int
        get() = end - start

    companion object Constants {
        val ZERO = MatchPos(0, 0)
    }

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Pair<*, *> -> start == other.first && end == other.second
            else -> super.equals(other)
        }

    override fun hashCode(): Int = (start to end).hashCode()
}

operator fun String.get(pos: MatchPos): String = get(pos.start, pos.end)
