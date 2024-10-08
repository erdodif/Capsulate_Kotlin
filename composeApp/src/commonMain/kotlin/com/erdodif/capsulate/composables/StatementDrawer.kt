package com.erdodif.capsulate.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.globalDragState
import com.erdodif.capsulate.lang.grammar.Expression
import com.erdodif.capsulate.lang.grammar.Statement
import com.erdodif.capsulate.lang.grammar.StrLit
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.SwitchStatementWithElse
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState

@Composable
fun StatementDrawer(modifier: Modifier = Modifier) {
    val statements by remember {
        mutableStateOf(
            listOf(
                Command("a"),
                IfStatement("a", arrayOf(Command("1")), arrayOf(Command("2"))),
                SwitchStatementWithElse(
                    arrayOf(
                        Block("a", arrayOf(Command("1"))),
                        Block("b", arrayOf(Command("2")))
                    ), arrayOf(Command("3"))
                ),
                LoopStatement("d", arrayOf(Command("1"))),
                LoopStatement("d", arrayOf(Command("1")), inOrder = false),
                AwaitStatement("g"),
                ParallelStatement(arrayListOf(arrayListOf(Command("a")), arrayListOf(Command("b"))))
            )
        )
    }
    LazyColumn(modifier.padding(25.dp, 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        items(statements) { statement ->
            val state = globalDragState.current
            DraggableItem(Modifier, key = statement, state = state,
                data = Expression(
                    StrLit("TODO", MatchPos(0, 0))
                ),
                dragAfterLongPress = true,
                draggableContent = {
                    statement.show(
                        Modifier.border(Theme.borderWidth, Theme.borderColor).fillMaxWidth()
                    )
                }) {
                statement.show(
                    Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor)
                        .fillMaxWidth()
                )
            }
        }
    }
}