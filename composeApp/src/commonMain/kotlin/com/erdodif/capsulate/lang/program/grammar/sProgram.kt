package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.expression.KeyWord
import com.erdodif.capsulate.lang.program.grammar.expression.LineEnd
import com.erdodif.capsulate.lang.program.grammar.expression.Symbol
import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.pBoolLit
import com.erdodif.capsulate.lang.program.grammar.expression.pComment
import com.erdodif.capsulate.lang.program.grammar.expression.pIntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pStrLit
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.sFunction
import com.erdodif.capsulate.lang.program.grammar.function.sMethod
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._anyKeyword
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._lineEnd
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.freeChar
import com.erdodif.capsulate.lang.util.reservedChar
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.util.tok

typealias NamedProgram = Pair<String?, ArrayList<Statement>>
typealias NamedHalfProgram = Pair<String?, ArrayList<Either<Statement, LineError>>>
typealias Declarations = ArrayList<Either<Method, Function<Value>>>

val sNamed: Parser<NamedProgram> = delimit(
    optional(right(_keyword("program"), _nonKeyword)) + many(
        right(many(_lineEnd), statement)
    )
)

val sHalfNamed: Parser<NamedHalfProgram> = delimit(
    optional(right(_keyword("program"), _nonKeyword)) + many(
        right(many(_lineEnd), or(statement, sError))
    )
)


val halfProgram: Parser<Pair<Declarations, NamedHalfProgram>> =
    topLevel(delimit(many(or(sMethod, sFunction))) + sHalfNamed)

val program: Parser<Pair<Declarations, NamedProgram>> =
    topLevel(delimit(many(or(sMethod, sFunction))) + sNamed)



fun parseProgram(input: String): ParserResult<ArrayList<Statement>> =
    ParserState(input).parse(topLevel(program).div{ it.second.second })

fun tokenizeProgram(input: String): ParserResult<List<Token>> =
    ParserState(input)
        .parse(
            topLevel(
                many(
                    asum(
                        (_lineEnd * { char, pos -> LineEnd(char, pos) }) as Parser<Token>,
                        pVariable as Parser<Token>,
                        pComment as Parser<Token>,
                        pIntLit as Parser<Token>,
                        pBoolLit as Parser<Token>,
                        pStrLit as Parser<Token>,
                        (_anyKeyword * { keyword, pos -> KeyWord(keyword, pos) }) as Parser<Token>,
                        (tok(reservedChar) * { char, pos -> Symbol(char, pos) }) as Parser<Token>,
                        (tok(some(freeChar)) * { _, pos -> Token(pos) }),
                    )
                )
            )
        )

fun Environment.runProgram(statements: List<Statement>) {
    for (statement in statements) statement.evaluate(this)
}
