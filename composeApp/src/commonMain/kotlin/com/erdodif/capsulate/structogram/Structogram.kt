package com.erdodif.capsulate.structogram

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.Statement
import com.erdodif.capsulate.structogram.statements.StatementList

class Structogram {
    var statements: StatementList

    private constructor(statements: StatementList) {
        this.statements = statements
    }

    private constructor(statements: List<Statement>) {
        this.statements = statements.toTypedArray()
    }

    @Composable
    fun content(modifier: Modifier = Modifier) =
        Column(modifier.width(IntrinsicSize.Min).border(Theme.borderWidth, Theme.borderColor).padding(Theme.borderWidth, 0.dp)) {
            Spacer(Modifier.height(Theme.borderWidth))
            StackWithSeparator(
                statements,
                { it.show(Modifier.fillMaxWidth()) }) { HorizontalBorder() }
        }

    companion object {
        fun fromStatements(vararg statements: Statement): Structogram {
            return Structogram(statements.asList())
        }

        fun fromAST(): Structogram {
            TODO()
        }
    }
}