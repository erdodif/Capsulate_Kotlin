package com.erdodif.capsulate.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.lang.grammar.BoolLit
import com.erdodif.capsulate.lang.grammar.Comment
import com.erdodif.capsulate.lang.grammar.IntLit
import com.erdodif.capsulate.lang.grammar.KeyWord
import com.erdodif.capsulate.lang.grammar.LineEnd
import com.erdodif.capsulate.lang.grammar.LineError
import com.erdodif.capsulate.lang.grammar.StrLit
import com.erdodif.capsulate.lang.grammar.Symbol
import com.erdodif.capsulate.lang.grammar.Token
import com.erdodif.capsulate.lang.grammar.Variable
import com.erdodif.capsulate.lang.grammar.tokenizeProgram
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.get
import kotlinx.coroutines.launch

class CodeHighlight private constructor(
    val constant: Color,
    val variable: Color,
    val number: Color,
    val string: Color,
    val stringEscape: Color,
    val operator: Color,
    val parenthesis: Color,
    val error: Color,
    val comment: Color,
    val control: Color
) {

    class Builder private constructor() {
        var constant: Color = Color(117, 0, 255)
        var variable: Color = Color(173, 105, 209)
        var number: Color = Color(23, 182, 188)
        var string: Color = Color(29, 119, 47)
        var stringEscape: Color = Color(217, 217, 26)
        var operator: Color = Color(217, 26, 185)
        var parenthesis: Color = Color(237, 237, 237)
        var error: Color = Color(217, 26, 26)
        var comment: Color = Color(90, 90, 90)
        var control: Color = Color(218, 121, 42)

        companion object {
            operator fun invoke(it: (Builder.() -> Unit)): Builder {
                val builder = Builder()
                builder.it()
                return builder
            }
        }

        fun build(): CodeHighlight = CodeHighlight(
            constant = constant,
            variable = variable,
            number = number,
            string = string,
            stringEscape = stringEscape,
            operator = operator,
            parenthesis = parenthesis,
            error = error,
            comment = comment,
            control = control
        )
    }

    fun Token.style(): FontStyle {
        return when (this) {
            is Comment -> FontStyle.Italic
            else -> FontStyle.Normal
        }
    }

    fun Token.weight(): FontWeight {
        return when (this) {
            is Comment -> FontWeight.Light
            is Symbol -> FontWeight.Medium
            else -> FontWeight.Normal
        }
    }

    fun Token.highlight(): Color {
        return when (this) {
            is BoolLit -> constant
            is Variable -> variable
            is KeyWord -> control
            is IntLit -> number
            is StrLit -> string
            is LineEnd -> parenthesis
            is Symbol -> {
                if (this.id == '(' || this.id == ')') parenthesis
                else operator
            }

            is Comment -> comment
            else -> error
        }
    }

    fun visualTransformation(code: String, tokenStream: ParserResult<ArrayList<Token>>) =
        VisualTransformation {
            if (tokenStream is Fail<*>) {
                TransformedText(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red, background = Color.Black))
                    { append(code) }
                }, OffsetMapping.Identity)
            } else {
                tokenStream as Pass
                TransformedText(buildAnnotatedString {
                    for (token in tokenStream.value) {
                        withStyle(
                            SpanStyle(
                                color = token.highlight(),
                                fontWeight = token.weight(),
                                fontStyle = token.style()
                            )
                        ) {
                            append(code[token.match.start, token.match.end])
                        }
                    }
                }, OffsetMapping.Identity)
            }
        }
}

val defaultCodeHighLight = CodeHighlight.Builder {}.build()

fun lineBreakPositions(str: String): List<Int> = str.mapIndexed { pos, it ->
    if (it == '\n') return@mapIndexed pos
    else null
}.filterNotNull()

@OptIn(ExperimentalTextApi::class)
@Composable
fun CodeEditor(
    code: String = "",
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val tokenStream= tokenizeProgram(code)
    defaultCodeHighLight.apply {
        val textStyle = TextStyle(fontFamily = fonts, fontSize = 18.sp)
        val lineBreaks = lineBreakPositions(code)
        var lineCountContent by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        val str = AnnotatedString.Builder(code.length).apply {
            withStyle(SpanStyle()) { append(code) }
            pushUrlAnnotation(UrlAnnotation("http://google.com"))
        }.toAnnotatedString()
        val transform by remember(code, tokenStream){ derivedStateOf{ visualTransformation(code, tokenStream) }}
        BasicTextField(
            value = str.text, onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = modifier.background(Color(44, 44, 44)),
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
        ) {
            Row(Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
                Text(
                    text = lineCountContent,
                    modifier = Modifier.padding(3.dp, 0.dp, 2.dp, 0.dp),
                    fontSize = textStyle.fontSize,
                    fontFamily = textStyle.fontFamily,
                    color = Color(80, 80, 80)
                )
                Spacer(
                    Modifier.padding(4.dp, 1.dp)
                        .background(Color(37, 37, 37, 200))
                        .fillMaxHeight()
                        .width(2.dp)
                )
                it()
            }
        }
    }
}
