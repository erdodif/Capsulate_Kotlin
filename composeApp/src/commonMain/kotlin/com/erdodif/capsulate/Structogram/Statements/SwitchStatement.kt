package com.erdodif.capsulate.Structogram.Statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

open class SwitchStatement(var blocks: Array<Block>) : Statement() {
    @Composable
    override fun show(modifier: Modifier) {
        TODO("Not yet implemented")
    }
}

class SwitchStatementWithElse(blocks: Array<Block>, elseBranch: Block) : SwitchStatement(blocks) {
    @Composable
    override fun show(modifier: Modifier) {
        TODO("Not yet implemented")
    }
}

open class Block(var condition: String, var statements: StatementList)
