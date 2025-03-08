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
import com.erdodif.capsulate.lang.program.grammar.expression.BoolLit
import com.erdodif.capsulate.lang.program.grammar.expression.Comment
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.KeyWord
import com.erdodif.capsulate.lang.program.grammar.expression.LineEnd
import com.erdodif.capsulate.lang.program.grammar.expression.StrLit
import com.erdodif.capsulate.lang.program.grammar.expression.Symbol
import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.fold
import com.erdodif.capsulate.lang.util.get
import kotlin.math.max

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

    val Token.spanStyle: SpanStyle
        get() = SpanStyle(color = highlight(), fontWeight = weight(), fontStyle = style())

    fun visualTransformation(code: String, tokenStream: ParserResult<ArrayList<Token>>) =
        VisualTransformation {
            tokenStream.toEither().fold({ tokens ->
                TransformedText(buildAnnotatedString {
                    withStyle(Token(MatchPos(0, code.length)).spanStyle) {
                        append(code[0, tokens.firstOrNull()?.match?.start ?: max(0,code.length - 1)])
                    }
                    println(code)
                    for (token in tokens) {
                        withStyle(token.spanStyle) {
                            println(token)
                            append(code[token.match.start, token.match.end])
                        }
                    }
                }, OffsetMapping.Identity)
            }) {
                TransformedText(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red, background = Color.Black))
                    { append(code) }
                }, OffsetMapping.Identity)
            }
        }
}

val defaultCodeHighLight = CodeHighlight.Builder {}.build()
