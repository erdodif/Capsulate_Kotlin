package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme

class Command (var text: String)  : Statement() {
    @Composable
    override fun show(modifier: Modifier) {
        StatementText(text, false, modifier.width(IntrinsicSize.Max).padding(Theme.commandPadding))
    }
}