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
import com.erdodif.capsulate.lang.program.grammar.halfProgram
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.structogram.composables.HorizontalBorder
import com.erdodif.capsulate.structogram.composables.StackWithSeparator
import com.erdodif.capsulate.structogram.composables.Theme
import com.erdodif.capsulate.structogram.statements.Statement
import com.erdodif.capsulate.structogram.statements.StatementList

class Structogram {
    var statements: StatementList

    private constructor(statements: StatementList) {
        this.statements = statements
    }

    private constructor(statements: List<Statement>) {
        this.statements = statements.toTypedArray()
    }

    @Composable
    fun content(modifier: Modifier = Modifier, draggable: Boolean = false) = key(this, draggable) {
        Column(
            modifier.width(IntrinsicSize.Min).border(Theme.borderWidth, Theme.borderColor)
                .padding(Theme.borderWidth, 0.dp)
        ) {
            Spacer(Modifier.height(Theme.borderWidth))
            StackWithSeparator(
                statements,
                { it.Show(Modifier.fillMaxWidth(), draggable) }) { HorizontalBorder() }
        }
    }

    companion object {
        fun fromString(text: String): Either<Structogram, Fail> {
            val parserState = ParserState(text)
            val result = halfProgram(parserState)
            val parsedStatements =
                ((result as? Pass<*>)?.value as List<*>?)?.filterNot { it is Right<*> }
                    ?.map {
                        it as Left<*>
                        Statement.fromStatement(
                            parserState,
                            it.value as com.erdodif.capsulate.lang.program.grammar.Statement
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