@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.If
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.structogram.composables.caseIndicator
import com.erdodif.capsulate.structogram.composables.commandPlaceHolder
import com.erdodif.capsulate.structogram.composables.elseIndicator
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.onDpSize
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
open class IfStatement(
    var condition: String,
    var trueBranch: List<ComposableStatement<*>> = listOf(),
    var falseBranch: List<ComposableStatement<*>> = listOf(),
    override val statement: If
) : ComposableStatement<If>(statement) {
    constructor(statement: If, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statementsTrue.map { fromStatement(state, it) },
        statement.statementsFalse.map { fromStatement(state, it) },
        statement
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var conditionHeight by remember { mutableStateOf(0.dp) }
        var dragging by remember { mutableStateOf(false) }
        Column(
            modifier.dim(dragging).onDpSize(density) { size = it }.clip(RectangleShape)
                .fillMaxWidth().defaultMinSize(Dp.Unspecified, max(conditionHeight + 10.dp, 50.dp))

        ) {
            DraggableArea(Modifier, draggable, size) {
                dragging = it
                StatementText(
                    condition,
                    modifier = Modifier.fillMaxWidth()
                        .conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) {
                            statement.id == activeStatement
                        }.caseIndicator().elseIndicator().padding(Theme.ifPadding)
                )
            }
            HorizontalBorder()
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary).fillMaxWidth()
                    .onDpSize(density) { conditionHeight = it.height }.height(IntrinsicSize.Min)
            ) {
                Column(
                    Modifier.defaultMinSize(Dp.Unspecified, 25.dp).weight(1f, true)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    StackWithSeparator(
                        trueBranch,
                        {
                            it.Show(Modifier.fillMaxWidth(), draggable, activeStatement)
                        },
                        {
                            commandPlaceHolder(
                                Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 25.dp)
                            )
                        }) { HorizontalBorder() }
                }
                VerticalBorder()
                Column(
                    Modifier.defaultMinSize(Dp.Unspecified, 20.dp).weight(1f, true)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    StackWithSeparator(
                        falseBranch,
                        {
                            it.Show(Modifier.fillMaxWidth(), draggable, activeStatement)
                        },
                        {
                            commandPlaceHolder(
                                Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 25.dp)
                            )
                        }) { HorizontalBorder() }
                }
            }

        }
    }
}

@Preview
@Composable
fun IfPreview() = PreviewColumn(width = 400.dp) {
    val statement =
        IfStatement(
            "condition",
            listOf(
                Command("true statement 1", Skip()),
                Command("true statement 2", Skip())
            ),
            listOf(
                Command("false statement 1", Skip()),
                Command("false statement 2", Skip())
            ),
            If(BoolLit(true, MatchPos.ZERO), arrayListOf(), arrayListOf())
        )
    val modifier = Modifier.fillMaxWidth().border(Theme.borderWidth, Theme.borderColor)
    labeled("Regular If") { statement.Show(modifier, false, null) }
    labeled("Active If") { statement.Show(modifier, false, statement.statement.id) }
}
