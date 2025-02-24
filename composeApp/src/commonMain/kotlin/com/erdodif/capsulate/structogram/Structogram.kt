package com.erdodif.capsulate.structogram

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.halfProgram
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.composables.VerticalBorder
import com.erdodif.capsulate.structogram.statements.ComposableStatement
import com.erdodif.capsulate.utility.PreviewTheme
import kotlinx.coroutines.runBlocking
import com.erdodif.capsulate.lang.program.grammar.Statement as GrammarStatement
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KParcelize
@Serializable
class Structogram private constructor(
    var statements: Array<ComposableStatement<*>>,
    val name: String? = null,
    val functions: Array<ComposableFunction> = emptyArray(),
    val methods: Array<ComposableMethod> = emptyArray()
) : KParcelable {
    val program: List<Statement>
        get() = statements.map { it.statement }

    private constructor(statements: List<ComposableStatement<*>>) : this(statements.toTypedArray())


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        draggable: Boolean = false,
        activeStatement: Uuid? = null
    ) = key(this, draggable, activeStatement) {
        Column(modifier) {
            if (name != null) {
                Text(
                    text = name,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(24.dp)
                        ).defaultMinSize(100.dp, 25.dp)
                        .border(
                            Theme.borderWidth,
                            Theme.borderColor,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(Theme.commandPadding)
                        .padding(2.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
                VerticalDivider(
                    modifier = Modifier.height(25.dp).align(Alignment.CenterHorizontally),
                    thickness = Theme.borderWidth,
                    color = Theme.borderColor
                )
            }
            Column(
                Modifier.fillMaxWidth().width(IntrinsicSize.Max)
                    .border(Theme.borderWidth, Theme.borderColor)
                    .padding(Theme.borderWidth, 0.dp)
            ) {
                Spacer(Modifier.height(Theme.borderWidth))
                StackWithSeparator(
                    statements,
                    {
                        it.Show(
                            Modifier.fillMaxWidth(),
                            draggable,
                            activeStatement
                        )
                    }) { HorizontalBorder() }
            }
        }
    }

    companion object {
        suspend fun fromString(text: String): Either<Structogram, Fail> {
            val parserState = ParserState(text)
            val result = halfProgram(parserState)
            return if (result is Pass) {
                val functions = mutableListOf<ComposableFunction>()
                val methods = mutableListOf<ComposableMethod>()
                val (declarations, namedProgram) = result.value
                declarations.map {
                    it[{ method ->
                        methods.add(ComposableMethod(method, parserState))
                    }, { function ->
                        functions.add(ComposableFunction(function, parserState))
                    }]
                }
                val parsedStatements = namedProgram.second.filterNot { it is Right<*> }
                    .map {
                        yield()
                        it as Left<*>
                        val statement: GrammarStatement = it.value as GrammarStatement
                        ComposableStatement.fromStatement(
                            parserState,
                            statement
                        )
                    }.toTypedArray()
                if (parsedStatements.isNotEmpty()) {
                    Left(
                        Structogram(
                            parsedStatements,
                            namedProgram.first,
                            functions.toTypedArray(),
                            methods.toTypedArray()
                        )
                    )
                } else {
                    Right(Fail("No statement matched!", parserState))
                }
            } else {
                Right(result as Fail)
            }
        }

        fun fromStatements(
            vararg statements: ComposableStatement<*>,
            name: String? = null
        ): Structogram {
            return Structogram(statements.toList().toTypedArray(), name = name)
        }

        fun fromStatements(
            statements: List<ComposableStatement<*>>,
            name: String? = null
        ): Structogram {
            return Structogram(statements.toTypedArray(), name = name)
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalUuidApi::class)
fun StructogramPreview() = PreviewTheme {
    lateinit var structogram: Structogram
    runBlocking {
        Structogram.fromString("s")[{
            structogram = it
        }, {
            throw Exception(it.reason)
        }]
    }
    structogram.Content(Modifier, false, null)
}