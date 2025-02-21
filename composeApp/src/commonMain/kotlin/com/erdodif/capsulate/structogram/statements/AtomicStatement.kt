@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.labeled
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class AtomicStatement(
    val statements: List<ComposableStatement<*>>,
    override val statement: Atomic
) : ComposableStatement<Atomic>(statement) {

    constructor(statement: Atomic, parserState: ParserState) : this(
        statement.statements.map { fromStatement(parserState, it) }.toList(),
        statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        val background = if (activeStatement == statement.id) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }
        val innerModifier = Modifier.fillMaxWidth().border(Theme.borderWidth / 2, Theme.borderColor)
        Column(
            modifier
                .background(background)
                .padding(Theme.borderWidth * 3)
                .border(Theme.borderWidth, Theme.borderColor)
        ) {
            if (statements.isEmpty()) Command("", Skip()).Show(innerModifier)
            for (statement in statements) {
                statement.Show(
                    innerModifier,
                    draggable,
                    activeStatement
                )
            }
        }
    }

}

@Preview
@Composable
fun AtomicPreview() = PreviewColumn {
    val stmt = Atomic(listOf(Skip()))
    val inner = Command("A", Skip())
    val modifier = Modifier.border(Theme.borderWidth, Theme.borderColor)
    labeled("Regular") {
        AtomicStatement(listOf(inner, Command("B", Skip()), Command("C", Skip())), stmt).Show(
            modifier
        )
    }
    labeled("Outer active") {
        AtomicStatement(listOf(inner, Command("B", Skip()), Command("C", Skip())), stmt).Show(
            modifier,
            activeStatement = stmt.id
        )
    }
    labeled("Inner active") {
        AtomicStatement(listOf(inner, Command("B", Skip()), Command("C", Skip())), stmt).Show(
            modifier,
            activeStatement = inner.statement.id
        )
    }
}
