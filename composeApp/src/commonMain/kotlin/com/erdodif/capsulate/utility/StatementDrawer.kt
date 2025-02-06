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
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.DoWhile
import com.erdodif.capsulate.lang.program.grammar.If
import com.erdodif.capsulate.lang.program.grammar.IntLit
import com.erdodif.capsulate.lang.program.grammar.Parallel
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique
import com.erdodif.capsulate.lang.program.grammar.Wait
import com.erdodif.capsulate.lang.program.grammar.When
import com.erdodif.capsulate.lang.program.grammar.While
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.WhenStatement

private val match = MatchPos(0, 1)
private val boolLit = BoolLit(true, match)
private val assign = Assign("a", IntLit(0, match)).unique()

private val statements = listOf(
    Command("statement",assign),
    IfStatement(
        "if", statement = If(boolLit, arrayListOf(), arrayListOf()).unique()
    ),
    WhenStatement(
        arrayOf(
            Block("switch"),
            Block("case"),
            Block("else")
        ),
        statement = When(mutableListOf(boolLit to listOf(), boolLit to listOf()), listOf()).unique()
    ),
    LoopStatement("while", listOf(), true, While(boolLit,arrayListOf()).unique()),
    LoopStatement("do while", listOf(), false, DoWhile(boolLit, arrayListOf()).unique()),
    AwaitStatement("await", Wait(boolLit, Atomic(listOf())).unique()),
    ParallelStatement(
        Parallel(arrayListOf(arrayListOf(assign.statement),arrayListOf(assign.statement))).unique(),
        arrayOf(Command("", Skip.unique()), Command(" ", Skip.unique()))
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
                Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor), true
            )
        }
    }
}
