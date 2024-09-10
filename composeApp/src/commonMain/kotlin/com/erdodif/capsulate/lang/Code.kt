package com.erdodif.capsulate.lang

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlin.random.Random

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
        var variable: Color = Color(158, 102, 255)
        var number: Color = Color(11, 166, 177)
        var string: Color = Color(29, 119, 47)
        var operator: Color = Color(217, 26, 185)
        var parenthesis: Color = Color(237, 237, 237)
        var error: Color = Color(217, 26, 26)
        var comment: Color = Color(51, 51, 51)
        var control: Color = Color(241, 134, 69)

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

    /*fun Token.highlight(): Color {
        return when (this.type) {
            TokenType.INT -> control
            TokenType.STR -> control
            TokenType.LET -> control
            TokenType.IF -> control
            TokenType.INT_LIT -> number
            TokenType.STR_LIT -> string
            TokenType.OPEN_PAR -> parenthesis
            TokenType.CLOSE_PAR -> parenthesis
            TokenType.UNMATCHED -> error
        }
    }*/

}

val defaultCodeHighLight = CodeHighlight.Builder {

}.build()


@Composable
fun Code(code: String, modifier: Modifier = Modifier) {
    defaultCodeHighLight.apply {
        Text(code, color = Color.Gray)
        val result = parseProgram(code)
        //ParserResult<ArrayList<Exp<*>>> = ParserState(code)
            //.run{
            //topLevel(right(many(whiteSpace),many(pAtom)))()
        //}
        if(result is Fail<*>){
            Column {
                Text(result.reason, color = Color.Red)
                Text(result.state.toString(), color = Color(241,134,69))
            }
            return
        }
        result as Pass
        Text(
            buildAnnotatedString {
                /*withStyle(
                    style = SpanStyle(
                        color = Color.Magenta,
                        background = Color.Black
                    )
                ) {
                    //append((result as Pass).value.size.toString() + "\n")
                    var boby = StringBuilder()
                    result.value.map{boby.append(it.toString() + "\n")}
                    append(boby.toString())
                }*/
                //for (token in (result as Pass).value) {
                for (token in result.value) {
                    //append(token.toString())

                    withStyle(
                        style = SpanStyle(
                            color = when(token){
                                is Skip -> Color.Gray
                                is If -> Color.Magenta
                                is Assign -> Color.Blue
                                is Expression -> Color.Cyan
                                else -> Color.Red
                            },
                            background = Color.Black
                        )
                    ) {
                        append(token.toString())
                    }
                    append(" ")
                }
            }
        )
    }
}