@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.program.grammar.Assign
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.If
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.program.grammar.When
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AtomicStatement
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.DropStatement
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.WhenStatement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val statements = listOf(
    DropStatement("skip\n"),
    DropStatement("if true {\n  skip\n} else {\n  skip\n}\n"),
    DropStatement("when{\n  case1: skip,\n  case2: skip,\n  else: skip\n}\n"),
    DropStatement("while true {\n  skip\n}\n"),
    DropStatement("do {\n  skip\n} while true\n"),
    DropStatement("await condition {\n  skip\n}\n"),
    DropStatement("{ skip }|{ skip }"),
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun StatementDrawer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.padding(25.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(statements) { statement ->
            statement.Show(
                Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor), true
            )
        }
    }
}
