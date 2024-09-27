package com.erdodif.capsulate.structogram.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.composables.CodeEditor
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.grammar.LineError
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.grammar.halfProgram
import com.erdodif.capsulate.lang.grammar.parseProgram
import com.erdodif.capsulate.lang.grammar.tokenizeProgram
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.structogram.statements.Statement
import io.github.aakira.napier.Napier

@Composable
fun StatementPreview() = LazyColumn(
    Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(4.dp, 10.dp)
) {
    item {
        var code by rememberSaveable {
            mutableStateOf(
                "if 0 {\n  skip\n  a := \"sad\\\"as\" \n" +
                        "} \nelse { skip; }\nwhile true {skip;}"
            )
        }
        val result by remember { derivedStateOf { ParserState(code).parse(halfProgram) } }
        Box(Modifier.background(Color(0, 0, 0, 70)).padding(10.dp)) {
            CodeEditor(code) { code = it }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                Napier.d {
                    parseProgram(code).toString()
                }
            }
        ) {
            Text("Log content")
        }
        Column(Modifier.background(Color(46, 46, 46, 100))) {
            if (result is Pass) {
                Structogram.fromStatements(*(result as Pass<List<Either<Statement, LineError>>>).value.filterNot { it is Right<*, *> }
                    .map {
                        it as Left<*, *>
                        Statement.fromTokenized(
                            ParserState(code),
                            it.value as com.erdodif.capsulate.lang.grammar.Statement
                        )
                    }.toTypedArray()).content()
                for (res in (result as Pass<List<Either<Statement, LineError>>>).value.iterator()) {
                    if (res is Left<*, *>) {
                        Text(res.value.toString(), color = Color.Magenta)
                    } else {
                        res as Right<*, LineError>
                        Text(res.value.content, color = Color(140, 34, 17))
                    }
                }
            } else {
                Text((result as Fail).reason, color = Color.Red)
            }
        }
        Spacer(Modifier.height(10.dp))
    }

}

