package com.erdodif.capsulate.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.LocalDraggingStatement
import com.erdodif.capsulate.lang.grammar.DoWhile
import com.erdodif.capsulate.lang.grammar.Expression
import com.erdodif.capsulate.lang.grammar.Statement
import com.erdodif.capsulate.lang.grammar.StrLit
import com.erdodif.capsulate.lang.grammar.Variable
import com.erdodif.capsulate.lang.grammar.While
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.onMobile
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.SwitchStatementWithElse
import com.mohamedrejeb.compose.dnd.drag.DraggableItem

val tmpStatement = object : Statement {
    override fun evaluate(env: Env) {
        TODO("Won't be implemented")
    }
}

@Composable
fun StatementDrawer(modifier: Modifier = Modifier) {
    val statements by remember {
        mutableStateOf(
            listOf(
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
        )
    }
    LazyColumn(
        modifier.padding(25.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = LocalDraggingStatement.current.draggedItem == null
    ) {
        items(statements) { statement ->
            val state = LocalDraggingStatement.current
            var animate by remember(state) { mutableStateOf(false) }
            DraggableItem(Modifier, key = statement, state = state,
                data = Command(
                    "",
                    Expression(StrLit("TODO", MatchPos(0, 0)))
                ),
                dragAfterLongPress = onMobile,
                draggableContent = {
                    AnimatedVisibility(
                        animate,
                        enter = expandIn(),
                    ) {
                        statement.show(
                            Modifier.border(Theme.borderWidth, Theme.borderColor).fillMaxWidth()
                        )
                    }
                    animate = true
                },
                content = {
                    statement.show(
                        Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor)
                            .fillMaxWidth()
                    )
                })
        }
    }
}
