package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.awaitIndicator

class AwaitStatement(
    var condition: String
) : Statement() {
    @Composable
    override fun show(modifier: Modifier) = StatementText(
        condition, modifier = modifier.fillMaxWidth().awaitIndicator().padding(Theme.commandPadding)
    )
}