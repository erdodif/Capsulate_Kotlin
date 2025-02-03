package com.erdodif.capsulate.structogram.statements

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
import com.erdodif.capsulate.lang.program.grammar.When
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
import com.erdodif.capsulate.utility.labeled
import com.erdodif.capsulate.utility.PreviewColumn
import com.erdodif.capsulate.utility.conditional
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize
import kotlin.collections.listOf

@KParcelize
class WhenStatement(
    val blocks: Array<Block>, override val statement: UniqueStatement<When>
) : Statement<When>(statement) {
    constructor(statement: When, state: ParserState) : this(statement.blocks.map { block ->
        Block(
            block.first.toString(state),
            block.second.map { fromStatement(state, it.unique()) }
        )
    }.toMutableList().also { blocks ->
        if (statement.elseBlock != null) blocks.add(
            Block(
                "else", statement.elseBlock.map {
                    fromStatement(state, it.unique())
                }
            )
        )
    }.toTypedArray(), statement.unique())

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: AnyUniqueStatement?
    ) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var dragging by remember { mutableStateOf(false) }
        val blocks by remember {
            derivedStateOf { blocks.takeWhile { it.condition != "else" }.toTypedArray() }
        }
        val elseBranch by remember { derivedStateOf { this.blocks.firstOrNull { it.condition == "else" } } }
        Row(modifier.dim(dragging).clip(RectangleShape).fillMaxWidth().height(IntrinsicSize.Min)
            .onDpSize(density) { size = it }
            .conditional(Modifier.background(MaterialTheme.colorScheme.tertiary)) { statement == activeStatement }
        ) {
            var maxHeight by remember { mutableStateOf(0.dp) }
            StackWithSeparator(blocks, {
                Column(Modifier.weight(1f, true)) {
                    DraggableArea(Modifier, draggable, size) { drag ->
                        dragging = drag
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            StatementText(
                                it.condition, modifier = Modifier.onSizeChanged {
                                    maxHeight = max(
                                        maxHeight, (it.height.toFloat() / density.density).dp
                                    )
                                }.caseIndicator().fillMaxWidth()
                                    .defaultMinSize(minHeight = maxHeight)
                                    .padding(Theme.casePadding)
                            )
                        }
                    }
                    HorizontalBorder()
                    Column(Modifier.fillMaxSize()) {
                        StackWithSeparator(it.statements, {
                            it.Show(Modifier.fillMaxSize(), draggable)
                        }, {
                            commandPlaceHolder(Modifier.fillMaxSize())
                        }) { HorizontalBorder() }
                    }
                }
            }, {
                DraggableArea(
                    Modifier.fillMaxWidth().defaultMinSize(40.dp, 25.dp), draggable, size
                ) {}
            }) { VerticalBorder() }
            if (elseBranch != null) {
                VerticalBorder()
                Column(Modifier.weight(1f, true)) {
                    DraggableArea(Modifier, draggable, size) { dragging ->
                        Row(Modifier.dim(dragging)) {
                            StatementText(
                                "", modifier = Modifier.onSizeChanged {
                                    maxHeight = max(
                                        maxHeight, (it.height.toFloat() / density.density).dp
                                    )
                                }.elseIndicator().fillMaxWidth()
                                    .defaultMinSize(minHeight = maxHeight)
                                    .padding(Theme.elsePadding)
                            )
                        }
                    }
                    HorizontalBorder()
                    StackWithSeparator(elseBranch!!.statements, {
                        it.Show(Modifier.fillMaxWidth().weight(1f, true), draggable)
                    }, {
                        commandPlaceHolder(Modifier.fillMaxWidth().weight(1f, true))
                    }) { HorizontalBorder() }
                }
            }
        }
    }
}

@KParcelize
class Block(
    var condition: String,
    var statements: List<Statement<*>> = listOf()
) : KParcelable

@Preview
@Composable
fun WhenPreview() = PreviewColumn(width= 400.dp) {
    val parserState = ParserState("")
    val statement = WhenStatement(When(listOf(BoolLit(false, MatchPos.ZERO) to listOf(Skip)), listOf()), parserState)
    labeled("When with else") { statement.Show(Modifier.fillMaxWidth(), false, null) }
    labeled("When with else active") { statement.Show(Modifier.fillMaxWidth(), false, statement.statement) }
}

