package com.erdodif.capsulate.Structogram.Statements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.Structogram.Composables.StatementText
import com.erdodif.capsulate.Structogram.Composables.commandPlaceHolder

open class IfStatement (
    var condition: String,
    var trueBranch: StatementList = arrayOf(),
    var falseBranch:StatementList = arrayOf()
) :Statement() {
    @Composable
    override fun show(modifier: Modifier) = Column(modifier.fillMaxWidth()){
        StatementText(condition, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Start) {
            Column(Modifier.height(IntrinsicSize.Max)) {
                if (trueBranch.isEmpty()) commandPlaceHolder()
                for (statement in trueBranch) {statement.show()}
            }
            Column(Modifier.height(IntrinsicSize.Max)) {
                if (trueBranch.isEmpty()) commandPlaceHolder()
                for (statement in falseBranch) {statement.show()}
            }
        }
    }
}