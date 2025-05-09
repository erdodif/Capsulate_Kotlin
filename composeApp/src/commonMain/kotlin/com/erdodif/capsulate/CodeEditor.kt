package com.erdodif.capsulate

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.tokenizeProgram
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.utility.defaultCodeHighLight
import kotlinx.coroutines.launch

fun lineBreakPositions(str: String): List<Int> = str.mapIndexed { pos, char ->
    if (char == '\n') return@mapIndexed pos
    else null
}.filterNotNull()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    code: TextFieldValue = TextFieldValue(""),
    tokenizationResult: ParserResult<List<Token>> = tokenizeProgram(code.text),
    focusRequester: FocusRequester = FocusRequester(),
    onBackSlashEnter: () -> Unit = {},
    onValueChange: ((TextFieldValue) -> Unit)? = null
) {
    val text = code.text
    defaultCodeHighLight.apply {
        val textStyle = TextStyle(fontFamily = fonts, fontSize = 18.sp)
        val lineBreaks = lineBreakPositions(code.text)
        var lineCountContent by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState(0)
        val transform by remember(code, tokenizationResult) {
            derivedStateOf {
                visualTransformation(text, tokenizationResult)
            }
        }
        BasicTextField(
            value = code,
            readOnly = onValueChange == null,
            onValueChange = {
                if (onValueChange == null) {
                    code.copy(selection = it.selection)
                } else {
                    onValueChange.invoke(it)
                }
            },
            textStyle = textStyle,
            modifier = modifier.background(
                if (isSystemInDarkTheme()) Color(44, 44, 44)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ).onInterceptKeyBeforeSoftKeyboard {
                if (it.utf16CodePoint.toChar() == '\\' && onValueChange != null) {
                    coroutineScope.launch { onBackSlashEnter() }
                    true
                } else {
                    false
                }
            }.onPreviewKeyEvent {
                if (it.utf16CodePoint.toChar() == '\\' && onValueChange != null) {
                    coroutineScope.launch { onBackSlashEnter() }
                    true
                } else {
                    false
                }
            }.focusRequester(focusRequester),
            cursorBrush = SolidColor(Color.Cyan),
            onTextLayout = {
                coroutineScope.launch {
                    lineCountContent = ""
                    var i = 0
                    var lastLine = 0
                    lineBreaks.map { pos ->
                        i++
                        val currentLine = it.getLineForOffset(pos + 1)
                        lineCountContent += "$i" + "\n".repeat(currentLine - lastLine)
                        lastLine = currentLine
                    }
                    lineCountContent += "${i + 1}"
                }
            },
            visualTransformation = transform
        ) { textField ->
            Column(Modifier.verticalScroll(scrollState)) {
                Row(Modifier) {
                    Text(
                        text = lineCountContent,
                        modifier = Modifier.padding(3.dp, 0.dp, 2.dp, 0.dp),
                        fontSize = textStyle.fontSize,
                        fontFamily = textStyle.fontFamily,
                        overflow = TextOverflow.Visible,
                        color = Color(80, 80, 80)
                    )
                    Spacer(
                        Modifier.padding(4.dp, 1.dp)
                            .background(Color(37, 37, 37, 200))
                            .width(2.dp)
                    )
                    textField()
                }
            }
        }
    }
}
