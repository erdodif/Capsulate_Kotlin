package com.erdodif.capsulate.structogram.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.CodeEditor
import com.erdodif.capsulate.structogram.statements.Command
import com.erdodif.capsulate.structogram.statements.IfStatement
import com.erdodif.capsulate.structogram.statements.LoopStatement
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.AwaitStatement
import com.erdodif.capsulate.structogram.statements.Block
import com.erdodif.capsulate.structogram.statements.ParallelStatement
import com.erdodif.capsulate.structogram.statements.SwitchStatement
import com.erdodif.capsulate.structogram.statements.SwitchStatementWithElse

@Composable
fun StatementPreview() = LazyColumn(
    Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(4.dp, 10.dp)
) {
    item{
        Box (Modifier.background(Color(0,0,0,70)).padding(10.dp)){
            CodeEditor()
        }
        Spacer(Modifier.height(10.dp))
    }

    item{
        Box (Modifier.background(Color(0,0,0,70)).padding(10.dp)){
            CodeEditor("a := 2;")
        }
        Spacer(Modifier.height(10.dp))
    }


    item{
        Box (Modifier.background(Color(0,0,0,70)).padding(10.dp)){
            CodeEditor("if 0 = 1 { skip; } " +
                    "\nelse { skip; };")
        }
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            ParallelStatement(
            arrayOf(LoopStatement(
                "if true",
                arrayOf(Command("Hello"), Command("World"), Command("World2"))
            )),
            arrayOf(AwaitStatement("When possible"),Command("Say Hi")),
            arrayOf(LoopStatement(
                "if true",
                arrayOf(Command("Hello"), Command("Make it possible")),
                false
            ))),
        ).content()
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            SwitchStatement(
                arrayOf(
                    Block("Yes", arrayOf(Command("Say no"))),
                    Block("No", arrayOf(Command("Say yes"))),
                    Block("Maybe", arrayOf(Command("IDK"), Command("Run!"))),
                )
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            AwaitStatement("So..."),
            LoopStatement(
                "if true",
                arrayOf(
                    SwitchStatementWithElse(
                        arrayOf(
                            Block("Yes", arrayOf(Command("Say no"))),
                            Block("No", arrayOf(Command("Say yes"))),
                            Block("Maybe\nE", arrayOf(Command("IDK"), Command("Run!"))),
                        ),
                        arrayOf(Command("Meh"))
                    )
                ),
                false
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }


    item {
        Structogram.fromStatements(
            Command("So..."),
            IfStatement(
                "if true",
                arrayOf(Command("HelloHello Bello")),
                arrayOf(
                    LoopStatement(
                        "if true",
                        arrayOf(Command("Hello Bello World"), AwaitStatement("World")),
                    )
                )
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }

    item {
        Structogram.fromStatements(
            Command("So..."),
            IfStatement(
                "if you are so really true to yourself",
                arrayOf(Command("HelloHello Bello")),
                arrayOf(
                    Command("World, The big one"),
                    Command("World, The big two"),
                    Command("World, The big three"),
                )
            )
        ).content()
        Spacer(Modifier.height(10.dp))
    }
}

