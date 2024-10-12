package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.structogram.composables.caseIndicator
import com.erdodif.capsulate.structogram.composables.commandPlaceHolder
import com.erdodif.capsulate.structogram.composables.elseIndicator
import com.erdodif.capsulate.utility.dim
import com.erdodif.capsulate.utility.onDpSize

class SwitchStatement(
    val blocks: Array<Block>,
    statement: com.erdodif.capsulate.lang.grammar.Statement
) : Statement(statement) {
    @Composable
    override fun Show(modifier: Modifier, draggable: Boolean) {
        val density = LocalDensity.current
        var size by remember { mutableStateOf(DpSize.Zero) }
        var dragging by remember { mutableStateOf(false) }
        val blocks by remember {
            derivedStateOf { blocks.takeWhile { it.condition != "else" }.toTypedArray() }
        }
        val elseBranch by remember { derivedStateOf { this.blocks.firstOrNull { it.condition == "else" } } }
        Row(
            modifier.dim(dragging).clip(RectangleShape).fillMaxWidth().height(IntrinsicSize.Min)
                .onDpSize(density) { size = it }) {
            var maxHeight by remember { mutableStateOf(0.dp) }
            StackWithSeparator(blocks, {
                Column(Modifier.weight(1f, true)) {
                    DraggableArea(Modifier, draggable, size) { drag ->
                        dragging = drag
                        Row(Modifier) {
                            StatementText(
                                it.condition, modifier = Modifier
                                    .onSizeChanged {
                                        maxHeight = max(
                                            maxHeight,
                                            (it.height.toFloat() / density.density).dp
                                        )
                                    }
                                    .caseIndicator()
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = maxHeight)
                                    .padding(Theme.casePadding)
                            )
                        }
                    }
                    HorizontalBorder()
                    StackWithSeparator(it.statements, {
                        it.Show(Modifier.fillMaxWidth(), draggable)
                    }, {
                        commandPlaceHolder(Modifier.fillMaxWidth())
                    }
                    ) { HorizontalBorder() }
                }
            }, {
                DraggableArea(
                    Modifier.fillMaxWidth().defaultMinSize(40.dp, 25.dp),
                    draggable,
                    size
                ) {}
            }) { VerticalBorder() }
            if (elseBranch != null) {
                VerticalBorder()
                Column(Modifier.weight(1f, true)) {
                    DraggableArea(Modifier, draggable, size) { dragging ->
                        Row(Modifier.dim(dragging)) {
                            StatementText(
                                "", modifier = Modifier
                                    .onSizeChanged {
                                        maxHeight =
                                            max(
                                                maxHeight,
                                                (it.height.toFloat() / density.density).dp
                                            )
                                    }
                                    .elseIndicator()
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = maxHeight)
                                    .padding(Theme.elsePadding)
                            )
                        }
                    }
                    HorizontalBorder()
                    StackWithSeparator(elseBranch!!.statements, {
                        it.Show(Modifier.fillMaxWidth(), draggable)
                    }, {
                        commandPlaceHolder(Modifier.fillMaxWidth())
                    }
                    ) { HorizontalBorder() }
                }
            }
        }
    }
}

open class Block(var condition: String, var statements: StatementList = arrayOf())
