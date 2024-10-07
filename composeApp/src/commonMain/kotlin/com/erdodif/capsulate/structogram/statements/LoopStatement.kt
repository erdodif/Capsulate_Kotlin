package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder

@Composable
private fun Condition(text: String) =
    StatementText(text, modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(Theme.commandPadding))


class LoopStatement(
    var condition: String,
    var statements: StatementList = arrayOf(),
    var inOrder: Boolean = true
) : Statement() {
    @Composable
    override fun show(modifier: Modifier) =
        Column(modifier.height(IntrinsicSize.Min).background(MaterialTheme.colorScheme.primary).fillMaxWidth()) {
            if (inOrder) Condition(condition)
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Spacer(Modifier.width(32.dp).fillMaxHeight())
                VerticalBorder()
                Column(Modifier.fillMaxWidth()) {
                    if (inOrder) HorizontalBorder()
                    StackWithSeparator(
                        statements,
                        {
                            it.show(
                                Modifier.fillMaxWidth()
                            )
                        }) { HorizontalBorder() }
                    if (!inOrder) HorizontalBorder()
                }
            }
            if (!inOrder) Condition(condition)
        }
}