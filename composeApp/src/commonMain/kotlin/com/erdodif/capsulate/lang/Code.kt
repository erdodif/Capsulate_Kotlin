package com.erdodif.capsulate.lang

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.erdodif.capsulate.composables.fonts

class CodeHighlight private constructor(
    val constant: Color,
    val variable: Color,
    val number: Color,
    val string: Color,
    val operator: Color,
    val parenthesis: Color,
    val error: Color,
    val comment: Color,
    val control: Color
) {

    class Builder private constructor() {
        var constant: Color = Color(117, 0, 255)
        var variable: Color = Color(151, 117, 169)
        var number: Color = Color(31, 152, 156)
        var string: Color = Color(29, 119, 47)
        var operator: Color = Color(217, 26, 185)
        var parenthesis: Color = Color(237, 237, 237)
        var error: Color = Color(217, 26, 26)
        var comment: Color = Color(51, 51, 51)
        var control: Color = Color(203, 119, 50)

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
            is Symbol -> {
                if (this.id == '(' || this.id == ')') parenthesis
                else operator
            }

            is Comment -> comment
            else -> error
        }
    }

}

val defaultCodeHighLight = CodeHighlight.Builder{}.build()

@Composable
fun CodeEditor(startCode: String = "",modifier: Modifier = Modifier) {
    defaultCodeHighLight.apply {
        var code by rememberSaveable { mutableStateOf(startCode) }
        BasicTextField(value = code, onValueChange = { code = it },
            textStyle = TextStyle(fontFamily = fonts),
            modifier = Modifier.background(Color.Transparent).then(modifier),
            cursorBrush = SolidColor(Color.Cyan),
            visualTransformation = object : VisualTransformation {
                override fun filter(text: AnnotatedString): TransformedText {
                    val result = tokenizeProgram(code)
                    if (result is Fail<*>) {
                        return TransformedText(buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Red,
                                    background = Color.Black
                                )
                            ) { append(code) }
                        }, OffsetMapping.Identity)
                    }
                    result as Pass
                    return TransformedText(buildAnnotatedString {
                        for (token in result.value) withStyle(
                            style = SpanStyle(
                                color = token.highlight(),
                                background = Color.Black
                            )
                        ) {
                            append(code[token.match.start, token.match.end])
                        }
                    }, OffsetMapping.Identity)
                }
            })
    }
}
