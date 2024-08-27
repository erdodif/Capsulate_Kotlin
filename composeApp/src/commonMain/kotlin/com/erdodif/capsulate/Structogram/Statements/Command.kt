package com.erdodif.capsulate.Structogram.Statements

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.Structogram.Composables.StatementText

class Command (var text: String)  : Statement() {
    @Composable
    override fun show(modifier: Modifier) {
        StatementText(text, false, modifier.width(IntrinsicSize.Max))
    }
}