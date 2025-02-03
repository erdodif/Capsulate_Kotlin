package com.erdodif.capsulate.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.WhenStatement

val tmpStatement = UniqueStatement<Nothing>(TODO())

private val statements = listOf(
    Command("statement", tmpStatement),
    IfStatement(
        "if", statement = tmpStatement
    ),
    WhenStatement(
        arrayOf(
            Block("switch"),
            Block("case"),
            Block("else")
        ),
        tmpStatement
    ),
    LoopStatement("while", listOf(), true, tmpStatement),
    LoopStatement("do while", listOf(), false, tmpStatement),
    AwaitStatement("await", tmpStatement),
    ParallelStatement(
        tmpStatement,
        arrayOf()
    )
)

@Composable
fun StatementDrawer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.padding(25.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(statements) { statement ->
            statement.Show(
                Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor),true
            )
        }
    }
}
