package com.erdodif.capsulate.structogram

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.halfProgram
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.Statement
import com.erdodif.capsulate.structogram.statements.StatementList
import com.erdodif.capsulate.lang.program.grammar.Statement as GrammarStatement
import kotlinx.coroutines.yield

@KParcelize
class Structogram private constructor(var statements: StatementList) : KParcelable {
    val program: List<GrammarStatement>
        get() = statements.map { it.statement }

    private constructor(statements: List<Statement>) : this(statements.toTypedArray())


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        draggable: Boolean = false,
        activeStatement: GrammarStatement? = null
    ) = key(this, draggable, activeStatement) {
        Column(
            modifier.width(IntrinsicSize.Min).border(Theme.borderWidth, Theme.borderColor)
                .padding(Theme.borderWidth, 0.dp)
        ) {
            Spacer(Modifier.height(Theme.borderWidth))
            StackWithSeparator(
                statements,
                { it.Show(Modifier.fillMaxWidth(), draggable, activeStatement) }) { HorizontalBorder() }
        }
    }

    companion object {
        suspend fun fromString(text: String): Either<Structogram, Fail> {
            val parserState = ParserState(text)
            val result = halfProgram(parserState)
            val parsedStatements =
                ((result as? Pass<*>)?.value as List<*>?)?.filterNot { it is Right<*> }
                    ?.map {
                        yield()
                        it as Left<*>
                        Statement.fromStatement(
                            parserState,
                            it.value as GrammarStatement
                        )
                    }?.toTypedArray()
            return if (parsedStatements?.isNotEmpty() == true) {
                Left(Structogram(parsedStatements))
            } else {
                Right(result as? Fail ?: Fail("No statement matched!", parserState))
            }
        }


        fun fromStatements(vararg statements: Statement): Structogram {
            return Structogram(statements.asList())
        }

        fun fromAST(): Structogram {
            TODO()
        }
    }
}