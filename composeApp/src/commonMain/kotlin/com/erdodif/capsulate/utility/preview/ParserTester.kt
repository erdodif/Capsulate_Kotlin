package com.erdodif.capsulate.utility.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Skip
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.blockOrParallel
import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.Pattern
import com.erdodif.capsulate.lang.program.grammar.function.sFunction
import com.erdodif.capsulate.lang.program.grammar.function.sFunctionCall
import com.erdodif.capsulate.lang.program.grammar.function.sKnownPattern
import com.erdodif.capsulate.lang.program.grammar.function.sMethod
import com.erdodif.capsulate.lang.program.grammar.function.sMethodCall
import com.erdodif.capsulate.lang.program.grammar.function.sPattern
import com.erdodif.capsulate.lang.program.grammar.nonParallel
import com.erdodif.capsulate.lang.program.grammar.expression.pBoolLit
import com.erdodif.capsulate.lang.program.grammar.expression.pComment
import com.erdodif.capsulate.lang.program.grammar.expression.pExp
import com.erdodif.capsulate.lang.program.grammar.expression.pIntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pStrLit
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.program.grammar.halfProgram
import com.erdodif.capsulate.lang.program.grammar.program
import com.erdodif.capsulate.lang.program.grammar.sAbort
import com.erdodif.capsulate.lang.program.grammar.sAssign
import com.erdodif.capsulate.lang.program.grammar.sAtom
import com.erdodif.capsulate.lang.program.grammar.sDoWhile
import com.erdodif.capsulate.lang.program.grammar.sExpression
import com.erdodif.capsulate.lang.program.grammar.sIf
import com.erdodif.capsulate.lang.program.grammar.sParallel
import com.erdodif.capsulate.lang.program.grammar.sParallelAssign
import com.erdodif.capsulate.lang.program.grammar.sSkip
import com.erdodif.capsulate.lang.program.grammar.sWait
import com.erdodif.capsulate.lang.program.grammar.sWhen
import com.erdodif.capsulate.lang.program.grammar.sWhile
import com.erdodif.capsulate.lang.program.grammar.statement
import com.erdodif.capsulate.lang.program.grammar.statementOrBlock
import com.erdodif.capsulate.lang.program.grammar.tokenizeProgram
import com.erdodif.capsulate.lang.program.grammar.topLevel
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.pLineBreak
import com.erdodif.capsulate.utility.CodeEditor
import com.erdodif.capsulate.utility.PreviewTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private val functions: List<Function<Value>> = listOf()
private val pos = MatchPos.ZERO

private val methods: List<Method> = listOf(
    Method(
        Pattern(
            null,
            listOf(",", ","),
            listOf(
                Variable("sx", MatchPos.ZERO),
                Variable("dx", MatchPos.ZERO),
                Variable("x", MatchPos.ZERO)
            ),
            ":_read"
        ),
        listOf(Skip(pos))
    ),
    Method(
        Pattern("read", listOf(), listOf(), null),
        listOf(Abort(pos))
    ),
    Method(
        Pattern("param", listOf(), listOf(Variable("hm", MatchPos.ZERO)), null),
        listOf(Skip(pos), Skip(pos))
    ),
    Method(
        Pattern("param", listOf(), listOf(Variable("hm", MatchPos.ZERO)), "abor"),
        listOf(Abort(pos))
    )
)

private val parsers: List<Pair<Parser<*>, String>> = listOf(
    pVariable to "variable",
    program to "program",
    halfProgram to "half program",
    pExp to "expression",
    pBoolLit to "boolean literal",
    pIntLit to "integer literal",
    pStrLit to "string literal",
    pLineBreak to "line break",
    pComment to "comment",
    statement to "statement",
    statementOrBlock to "statement or block",
    blockOrParallel to "block or parallel",
    nonParallel to "non parallel",
    sParallel to "parallel",
    sParallelAssign to "parallel assignment",
    sAssign to "assignment",
    sExpression to "expression as statement",
    sSkip to "skip",
    sAbort to "abort",
    sIf to "if",
    sWhen to "where",
    sWhile to "while",
    sDoWhile to "do while",
    sAtom to "atomic",
    sWait to "wait",
    sFunction to "function declaration",
    sMethod to "method declaration",
    sPattern to "pattern",
    sFunctionCall to "function call",
    sMethodCall to "method call",
    sKnownPattern to "known pattern",
)

@Preview
@Composable
fun ParserTester() = PreviewTheme {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var filter by remember { mutableStateOf("") }
    val tokens = tokenizeProgram(input.text)
    Column(Modifier.fillMaxSize().imePadding()) {
        CodeEditor(input, tokens, Modifier.fillMaxWidth().height(100.dp)) { input = it }
        HorizontalDivider()
        Row(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter ")
            BasicTextField(filter, { filter = it }, Modifier.fillMaxWidth())
        }
        HorizontalDivider()
        LazyRow {
            val stream = tokens.passOrNull()?.value
            if(stream != null)
            items(stream) { token: Token ->
                Column {
                    Text(token.matchedToken(ParserState(input.text)).replace(" ","␣"))
                    Text("(${token.match.start},${token.match.end})")
                }
            }
        }
        LazyColumn(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
            items(parsers.filter { filter in it.second }
                .sortedBy { it.second.length }) { (parser, name) ->
                var result: Either<ParserResult<*>, Exception> by remember {
                    mutableStateOf(Right(NullPointerException()))
                }
                LaunchedEffect(input.text, filter) {
                    withContext(Dispatchers.IO) {
                        result = try {
                            Left(
                                with(
                                    ParserState(
                                        input.text,
                                        functions.toMutableList(),
                                        methods.toMutableList()
                                    ), topLevel(parser)
                                )
                            )
                        } catch (e: Exception) {
                            Right(e)
                        }
                    }
                }
                key(result) {

                    Column(Modifier.padding(5.dp)) {
                        Text(
                            name,
                            color = if (result is Left && (result as Left<ParserResult<*>>).value is Pass)
                                MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                        val modifier = Modifier.fillMaxWidth().padding(5.dp).background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(5.dp)
                        ).padding(5.dp)
                        if (result is Left) {

                            when (val parserResult = (result as Left<ParserResult<*>>).value) {
                                is Pass -> Text(
                                    text = parserResult.value.toString(),
                                    modifier = modifier
                                )

                                is Fail -> Text(
                                    text = parserResult.reason.toString(),
                                    modifier = modifier,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Text(
                                text = "Exception while executing!\nReason:",
                                modifier = modifier,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = ((result as Right<*>).value as Exception).message.toString(),
                                modifier = modifier,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                }
            }
            item {
                HorizontalDivider()
            }
            items(parsers.filter { filter !in it.second }
                .sortedBy { it.second }) { (_, name) ->
                Text(name, color = MaterialTheme.colorScheme.background)
            }
        }
    }
}