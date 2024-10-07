package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder

class ParallelStatement(private vararg var blocks: StatementList) : Statement() {
    constructor(blocks: ArrayList<ArrayList<Statement>>) : this(
        *blocks.map { it.toTypedArray() }.toTypedArray()
    )

    @Composable
    override fun show(modifier: Modifier) = Row(Modifier.height(IntrinsicSize.Min)) {
        StackWithSeparator(
            blocks,
            {
                Column(Modifier.width(IntrinsicSize.Min)) {
                    StackWithSeparator(
                        it,
                        { statement -> statement.show(Modifier.fillMaxWidth()) }) {
                        HorizontalBorder()
                    }
                }
            }) {
            VerticalBorder()
            Spacer(Modifier.width(Theme.borderWidth))
            VerticalBorder()
        }
    }
}