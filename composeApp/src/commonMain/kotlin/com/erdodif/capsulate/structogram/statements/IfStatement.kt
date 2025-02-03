package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
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
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.If
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.structogram.composables.caseIndicator
import com.erdodif.capsulate.structogram.composables.commandPlaceHolder
import com.erdodif.capsulate.structogram.composables.elseIndicator
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize

@KParcelize
open class IfStatement(
    var condition: String,
    var trueBranch: List<Statement<*>> = listOf(),
    var falseBranch: List<Statement<*>> = listOf(),
    override val statement: UniqueStatement<If>
) : Statement<If>(statement) {
    constructor(statement: If, state: ParserState) : this(
        statement.condition.toString(state),
        statement.statementsTrue.map { fromStatement(state, it.unique()) },
        statement.statementsFalse.map { fromStatement(state, it.unique()) },
        statement.unique()
    )

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: AnyUniqueStatement?
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
                    modifier = Modifier.fillMaxWidth().caseIndicator().elseIndicator()
                        .conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) {
                            statement == activeStatement
                        }.padding(Theme.ifPadding)
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
