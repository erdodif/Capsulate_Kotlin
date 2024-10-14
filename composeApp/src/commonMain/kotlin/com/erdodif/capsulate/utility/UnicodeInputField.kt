package com.erdodif.capsulate.utility

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun UnicodeInputField(onAccept: (Char) -> Unit, onCancel: () -> Unit = {}) {
    Dialog(onDismissRequest = onCancel) {
        var value by remember { mutableStateOf("\\") }
        val match = escapes[value.substring(1)]
        Column {
            if (match != null) {
                Text("${match[0]}: ($value)", color = Color.Magenta)
                LazyRow {
                    items(match.toCharArray().toTypedArray()) { char ->
                        Button({ onAccept(char) }) {
                            Text(char.toString())
                        }
                    }
                }
            } else {
                Text(value, color = Color.Red)
            }
            BasicTextField(
                value, { value = it; if (it.isEmpty()) onCancel() },
                Modifier.size(150.dp, 30.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp)),
                singleLine = true,
                enabled = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrect = false,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (match != null) onAccept(match[0]) else onCancel() }
                )
            )
        }
    }
}