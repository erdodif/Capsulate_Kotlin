package com.erdodif.capsulate.Structogram.Statements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class LoopStatement(var inOrder: Boolean = true, var statements: StatementList) : Statement() {
    @Composable
    override fun show(modifier: Modifier) = Column(modifier) {

    }
}