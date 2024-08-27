package com.erdodif.capsulate.Structogram

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.Structogram.Statements.Statement
import com.erdodif.capsulate.Structogram.Statements.StatementList

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
        Column(modifier.width(IntrinsicSize.Min).border(3.dp, MaterialTheme.colorScheme.onPrimary)) {
            for (statement in statements) {
                statement.show(Modifier.fillMaxWidth())
            }
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