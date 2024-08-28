package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.structogram.composables.caseIndicator
import com.erdodif.capsulate.structogram.composables.elseIndicator

open class SwitchStatement(var blocks: Array<Block>) : Statement() {
// TODO: Needs to be a Layout, because the measurement phase can fix the one frame lag on resize

    @Composable
    override fun show(modifier: Modifier) = Row(Modifier.height(IntrinsicSize.Min)) {
        var maxHeight by remember { mutableStateOf(0.dp) }
        StackWithSeparator(blocks, {
            Column(Modifier.width(IntrinsicSize.Min)) {
                StatementText(
                    it.condition, modifier = Modifier.caseIndicator().fillMaxWidth().padding(
                        Theme.casePadding
                    ).onSizeChanged {
                        maxHeight = max(maxHeight, it.height.dp)
                    }
                )
                HorizontalBorder()
                StackWithSeparator(it.statements, {
                    it.show()
                }
                ) { HorizontalBorder() }
            }
        }) { VerticalBorder() }
    }
}

class SwitchStatementWithElse(blocks: Array<Block>, var elseBranch: StatementList) :
    SwitchStatement(blocks) {
    @Composable
    override fun show(modifier: Modifier) = Row(Modifier.height(IntrinsicSize.Min)) {
        var maxHeight by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current.density
        StackWithSeparator(blocks, {
            Column(Modifier.width(IntrinsicSize.Min)) {
                StatementText(
                    it.condition, modifier = Modifier
                        .onSizeChanged { maxHeight = max(maxHeight, (it.height.toFloat() / density).dp) }
                        .caseIndicator()
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = maxHeight)
                        .padding(Theme.casePadding)
                )
                HorizontalBorder()
                StackWithSeparator(it.statements, {
                    it.show()
                }
                ) { HorizontalBorder() }
            }
        }) { VerticalBorder() }
        VerticalBorder()
        Column(Modifier.width(IntrinsicSize.Min)) {
            StatementText(
                "", modifier = Modifier
                    .onSizeChanged { maxHeight = max(maxHeight, (it.height.toFloat() / density).dp) }
                    .elseIndicator()
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = maxHeight)
                    .padding(Theme.elsePadding)
            )
            HorizontalBorder()
            StackWithSeparator(elseBranch, {
                it.show()
            }
            ) { HorizontalBorder() }
        }
    }
}

open class Block(var condition: String, var statements: StatementList)
