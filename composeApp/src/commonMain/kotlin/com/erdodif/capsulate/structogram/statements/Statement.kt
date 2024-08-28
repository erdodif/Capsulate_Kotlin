package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

typealias StatementList = Array<Statement>

abstract class Statement {
    @Composable abstract fun show(modifier: Modifier)
    @Composable fun show() = show(Modifier)
}