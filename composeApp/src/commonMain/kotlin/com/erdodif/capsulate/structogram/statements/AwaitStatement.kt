package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
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
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.awaitIndicator
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.onDpSize

@KParcelize
class AwaitStatement(
    var condition: String,
    override val statement: Wait
) : ComposableStatement<Wait>(statement) {
    constructor(statement: Wait, state: ParserState) : this(
        statement.condition.toString(state),
        statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Statement?
    ) {
        var size by remember { mutableStateOf(DpSize.Zero) }
        val density = LocalDensity.current
        DraggableArea(Modifier, draggable, size) { dragging ->
            Row(
                modifier.onDpSize(density) { size = it }.dim(dragging)

            ) {
                StatementText(
                    condition,
                    modifier = Modifier.conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) {
                        statement == activeStatement
                    }.clip(RectangleShape).fillMaxSize().awaitIndicator()
                        .padding(Theme.commandPadding)
                )
            }
        }
    }
}

@Preview
@Composable
fun AwaitPreview() = PreviewColumn {
    val statement = AwaitStatement("guard",Wait(BoolLit(false, MatchPos.ZERO), Atomic(listOf(Skip()))))
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    labeled("Regular") { statement.Show(modifier, false, null) }
    labeled("Active") { statement.Show(modifier, false, statement.statement) }
}
