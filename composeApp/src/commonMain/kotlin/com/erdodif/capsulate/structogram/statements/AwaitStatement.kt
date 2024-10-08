package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.lang.grammar.Wait
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.composables.StatementText
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.awaitIndicator

class AwaitStatement(
    var condition: String,
    statement: com.erdodif.capsulate.lang.grammar.Statement
) : Statement(statement) {
    constructor(statement: Wait, state:ParserState): this(statement.condition.toString(state), statement)

    @Composable
    override fun show(modifier: Modifier) = Row(modifier) {
        StatementText(
            condition,
            modifier = Modifier.fillMaxWidth().awaitIndicator().padding(Theme.commandPadding)
        )
    }
}