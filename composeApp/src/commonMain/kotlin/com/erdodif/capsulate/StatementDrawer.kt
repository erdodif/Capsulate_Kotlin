@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.structogram.statements.DropStatement
import com.erdodif.capsulate.utility.theme.Theme
import kotlin.math.max
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun StatementDrawer(modifier: Modifier = Modifier) {
    val statementModifier =
        Modifier.padding(0.dp, 10.dp).border(Theme.borderWidth, Theme.borderColor)
    var whenCount by remember { mutableStateOf(2) }
    var parallelCount by remember { mutableStateOf(2) }
    LazyColumn(
        modifier.padding(25.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = LocalDraggingStatement.current.dragAmount == null
    ) {
        item { DropStatement("skip\n").Show(statementModifier) }
        item { DropStatement("if true {\n  skip\n} else {\n  skip\n}\n").Show(statementModifier) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    IconButton({ whenCount = min(whenCount + 1, 5) }) {
                        Icon(
                            Icons.Rounded.KeyboardArrowUp, "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton({ whenCount = max(whenCount - 1, 1) }) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown, "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                key(whenCount) {
                    DropStatement(
                        buildString {
                            append("when{\n")
                            for (i in 0..whenCount) {
                                append("  case${i + 1}: { skip },\n")
                            }
                            append("\n}\n")
                        }).Show(statementModifier)
                }

            }

        }
        item { DropStatement("while true {\n  skip\n}\n").Show(statementModifier) }
        item { DropStatement("do {\n  skip\n} while true\n").Show(statementModifier) }
        item { DropStatement("await condition {\n  skip\n}\n").Show(statementModifier) }
        item {
            Column {
                DropStatement(
                    buildString {
                        for (i in 0..parallelCount) {
                            append("{ skip }")
                            append("|")
                        }
                    }.dropLast(1)
                ).Show(statementModifier)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton({ parallelCount = max(parallelCount - 1, 1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft, "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton({ parallelCount = min(parallelCount + 1, 5) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight, "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
