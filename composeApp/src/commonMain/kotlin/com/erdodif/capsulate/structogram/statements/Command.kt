package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme

class Command (var text: String)  : Statement() {
    @Composable
    override fun show(modifier: Modifier) = Row(modifier){
        StatementText(text, false, Modifier.fillMaxWidth().padding(Theme.commandPadding))
    }
}