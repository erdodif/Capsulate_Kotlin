package com.erdodif.capsulate.Structogram.Statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class AwaitStatement(
    condition: String,
    statements: StatementList = arrayOf()
) : Statement(){
    @Composable
    override fun show(modifier: Modifier) {
        TODO("Not yet implemented")
    }
}