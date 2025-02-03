package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.util.Env
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias AnyUniqueStatement = UniqueStatement<Statement>

/**
 * A uniquely identifiable Statement class which allows equality checks
 */
@OptIn(ExperimentalUuidApi::class)
@KParcelize
@Serializable
data class UniqueStatement<out T : Statement>(
    val statement: T,
    val id: Uuid
) : KParcelable {
    constructor(statement: T) : this(
        statement,
        Uuid.random()
    ) //Only to leave the experimental OptIn here

    fun evaluate(env: Env) = statement.evaluate(env)

    override fun equals(other: Any?): Boolean = other is UniqueStatement<*> && id == other.id

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun <T : Statement> T.unique(): UniqueStatement<T> = UniqueStatement<T>(this)
    }
}