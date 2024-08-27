package com.erdodif.capsulate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.Structogram.Composables.StatementPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    MaterialTheme(colorScheme = resolveColors()) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary), horizontalAlignment = Alignment.CenterHorizontally) {
            StatementPreview()
        }
    }
}