package com.erdodif.capsulate.utility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.erdodif.capsulate.lang.program.grammar.BoolLit
import com.erdodif.capsulate.lang.program.grammar.Comment
import com.erdodif.capsulate.lang.program.grammar.IntLit
import com.erdodif.capsulate.lang.program.grammar.KeyWord
import com.erdodif.capsulate.lang.program.grammar.LineEnd
import com.erdodif.capsulate.lang.program.grammar.StrLit
import com.erdodif.capsulate.lang.program.grammar.Symbol
import com.erdodif.capsulate.lang.program.grammar.Token
import com.erdodif.capsulate.lang.program.grammar.Variable
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.get

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
