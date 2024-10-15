package com.erdodif.capsulate.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.slack.circuit.overlay.Overlay
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnicodeInputField(onAccept: (Char) -> Unit, onCancel: () -> Unit = {}) {
    val focusRequester = remember { FocusRequester() }
    //Do Overlay, Dialog has serious limitations considering imePadding
    Dialog(onDismissRequest = onCancel) {
        var value by remember { mutableStateOf(TextFieldValue("\\", TextRange(1))) }
        val match = escapes[value.text.substring(1)]
        Column(Modifier.imePadding(), horizontalAlignment = Alignment.Start) {
            Row(
                Modifier.fillMaxWidth().weight(if (match != null) 1f else 3f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onCancel) {
                    Icon(Icons.Filled.Close, "Close")
                }
            }
            if (match != null) {
                FlowRow(
                    Modifier.weight(2f).clip(RectangleShape)
                        .verticalScroll(rememberScrollState(0), true),
                    horizontalArrangement = Arrangement.Start
                ) {
                    match.forEach { char ->
                        Text(
                            char.toString(),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(2.dp)
                                .clickable { onAccept(char) }
                                .width(40.dp)
                                .padding(1.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(5.dp)
                                ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(Modifier.height(5.dp))
                BasicTextField(
                    value,
                    {
                        if (it.text.isEmpty()) {
                            onCancel()
                            return@BasicTextField
                        }
                        value = if (it.selection.start == 0) {
                            it.copy(it.text, selection = TextRange(1, max(it.selection.end, 1)))
                        } else {
                            it
                        }
                    },
                    Modifier.weight(4f).defaultMinSize(150.dp, Dp.Unspecified)
                        .padding(10.dp)
                        .border(
                            2.dp,
                            if (match != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            RoundedCornerShape(5.dp)
                        )
                        .focusRequester(focusRequester)
                        .background(Color.Transparent),
                    singleLine = true,
                    enabled = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (match != null) onAccept(match[0]) else onCancel() }
                    ),
                    textStyle = TextStyle(
                        color = if (match != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        fontSize = 30.sp
                    ),

                    )
                if (match != null) {
                    Text(
                        match[0].toString(),
                        fontSize = 40.sp,
                        modifier = Modifier
                            .clickable { onAccept(match[0]) }
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    Spacer(
                        Modifier.weight(1f).background(MaterialTheme.colorScheme.error)
                            .height(30.dp)
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}