@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.awaitIndicator
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.onDpSize
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class AwaitStatement(
    var condition: String,
    val atomic: ComposableStatement<*>,
    override val statement: Wait
) : ComposableStatement<Wait>(statement) {
    constructor(statement: Wait, state: ParserState) : this(
        statement.condition.toString(state),
        fromStatement(state, statement.atomic),
        statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        var size by remember { mutableStateOf(DpSize.Zero) }
        val density = LocalDensity.current
        var isDragging by remember { mutableStateOf(false) }
        Column(modifier.fillMaxSize().onDpSize(density) { size = it }) {
            DraggableArea(Modifier, draggable, size) { dragging ->
                isDragging = dragging
                StatementText(
                    condition,
                    modifier = Modifier.dim(dragging)
                        .conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) {
                            statement.id == activeStatement
                        }.clip(RectangleShape).fillMaxWidth().awaitIndicator()
                        .padding(Theme.commandPadding)
                )
                if (!dragging && draggable) {
                    DropTarget(LocalDraggingStatement.current, statement.match.start)
                }
            }
            HorizontalBorder()
            atomic.Show(Modifier, draggable && !isDragging, activeStatement)
        }
    }
}

@Preview
@Composable
private fun AwaitPreview() = PreviewColumn {
    val pos = MatchPos.ZERO
    val atom = Atomic(listOf(Skip(pos)), pos)
    val inner = Command("A", Skip(pos))
    val statement = AwaitStatement(
        "guard",
        AtomicStatement(listOf(inner, Command("B", Skip(pos)), Command("C", Skip(pos))), atom),
        Wait(BoolLit(false, MatchPos.ZERO), atom, pos)
    )
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    labeled("Regular") { statement.Show(modifier, false, null) }
    labeled("Active") { statement.Show(modifier, false, statement.statement.id) }
    labeled("Active await") { statement.Show(modifier, false, atom.id) }
    labeled("Active inner") { statement.Show(modifier, false, inner.statement.id) }
}
