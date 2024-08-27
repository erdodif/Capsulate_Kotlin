package com.erdodif.capsulate.Structogram.Composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.Structogram.Statements.Command
import com.erdodif.capsulate.Structogram.Statements.IfStatement
import com.erdodif.capsulate.Structogram.Structogram

@Composable
fun StatementPreview() = LazyColumn(
    Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(2.dp,10.dp)
) {
    item {
        Structogram.fromStatements(Command("Hello")).content()
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            IfStatement(
                "if true",
                arrayOf(Command("Hello")),
                arrayOf(Command("World"))
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            Command("So..."),
            IfStatement(
                "if true",
                arrayOf(Command("Hello")),
                arrayOf(Command("World"))
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }
}

