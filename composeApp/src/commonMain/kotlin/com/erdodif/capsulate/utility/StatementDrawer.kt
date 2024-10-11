package com.erdodif.capsulate.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.grammar.Statement
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.SwitchStatementWithElse

val tmpStatement = object : Statement {
    override fun evaluate(env: Env) {
        TODO("Won't be implemented")
    }
}

private val statements = listOf(
    Command("a", tmpStatement),
    IfStatement(
        "a", arrayOf(Command("1", tmpStatement)), arrayOf(
            Command(
                "2",
                tmpStatement
            )
        ), tmpStatement
    ),
    SwitchStatementWithElse(
        arrayOf(
            Block("a", arrayOf(Command("1", tmpStatement))),
            Block("b", arrayOf(Command("2", tmpStatement)))
        ), arrayOf(Command("3", tmpStatement)),
        tmpStatement
    ),
    LoopStatement("d", arrayOf(Command("1", tmpStatement)), true, tmpStatement),
    LoopStatement("d", arrayOf(Command("1", tmpStatement)), false, tmpStatement),
    AwaitStatement("g", tmpStatement),
    ParallelStatement(
        arrayListOf(
            arrayListOf(Command("a", tmpStatement)), arrayListOf(
                Command(
                    "b",
                    tmpStatement
                )
            )
        ), tmpStatement
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
