package com.erdodif.capsulate.pages.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.lang.program.grammar.expression.VArray
import com.erdodif.capsulate.pages.screen.DebugScreen.State
import com.erdodif.capsulate.utility.layout.WindowLazyList

@Composable
fun CallStack(state: State) = WindowLazyList(
    heightRow = 150.dp,
    widthColumn = 250.dp,
) {
    callStackElements(state)
}

private fun LazyListScope.callStackElements(state: State) {
    val textModifier = Modifier.padding(horizontal = 5.dp)
    items(state.stackTrace, { it.hashCode() }) { entry ->
        Column(
            Modifier
                .padding(2.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(5.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(5.dp))
                .widthIn(min = 100.dp, max = 350.dp)
                .heightIn(min = 100.dp, max = 150.dp)
                .padding(5.dp)
        ) {
            Text(entry.scope)
            LazyColumn {
                items(entry.variables) { variable ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            buildAnnotatedString {
                                val value = variable.value
                                if (value is VArray<*>) {
                                    append("${variable.id} : ${value.type.primitiveType.label}")
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color(57, 57, 57, 50).compositeOver(
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 12.sp
                                        )
                                    ) {
                                        for (size in value.type.dimensions) {
                                            append('(')
                                            append(size.toString())
                                            append(')')
                                        }
                                    }
                                } else {
                                    append("${variable.id} : ${variable.type.label}")
                                }
                            },
                            modifier = textModifier
                        )
                        Text(
                            variable.value.toString(),
                            modifier = textModifier,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (entry.variables.isEmpty()) {
                    item {
                        Text(
                            "Empty Environment!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

        }
    }
}
