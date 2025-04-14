package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.grammar.expression.KeyWord
import com.erdodif.capsulate.lang.program.grammar.expression.LineEnd
import com.erdodif.capsulate.lang.program.grammar.expression.Symbol
import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.expression.pAssumption
import com.erdodif.capsulate.lang.program.grammar.expression.pBoolLit
import com.erdodif.capsulate.lang.program.grammar.expression.pChrLit
import com.erdodif.capsulate.lang.program.grammar.expression.pComment
import com.erdodif.capsulate.lang.program.grammar.expression.pIntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pStrLit
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.program.grammar.function.sFunction
import com.erdodif.capsulate.lang.program.grammar.function.sMethod
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
import com.erdodif.capsulate.lang.util.splitEither
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.util.tok

data class NamedProgram(
    val name: String?,
    val statements: List<Statement>,
    val methods: List<Method>,
    val functions: List<Function<Value>>
) {
    constructor(name: String?, statements: List<Statement>, declarations: Declarations) : this(
        name,
        statements,
        declarations.methods,
        declarations.functions
    )
}

data class Declarations(val methods: List<Method>, val functions: List<Function<Value>>)

data class HalfNamedProgram(
    val name: String?,
    val statements: List<Statement>,
    val methods: List<Method>,
    val functions: List<Function<Value>>,
    val errors: List<LineError>
) {
    constructor(
        name: String?,
        statements: List<Statement>,
        innerErrors: List<LineError>,
        halfDeclarations: HalfDeclarations
    ) : this(
        name,
        statements,
        halfDeclarations.methods,
        halfDeclarations.functions,
        halfDeclarations.errors + innerErrors
    )
}

data class HalfDeclarations(
    val methods: List<Method>,
    val functions: List<Function<Value>>,
    val errors: List<LineError>
)

val sNamed: Parser<Pair<String?, List<Statement>>> = delimit(
    optional(right(_keyword("program"), _nonKeyword)) + many(
        right(many(_lineEnd), statement)
    )
)

val sHalfNamed: Parser<Triple<String?, List<Statement>, List<LineError>>> = delimit(
    optional(right(_keyword("program"), _nonKeyword)) + many(
        right(many(_lineEnd), or(statement, sError))
    )
) / { (name, options) ->
    val (statements, errors) = options.splitEither()
    Triple(name, statements, errors)
}

val halfProgram: Parser<HalfNamedProgram> =
    topLevel(delimit(many(or(sMethod,sFunction))) + sHalfNamed) / { (decs, prog) ->
        val (methods, functions) = decs.splitEither()
        val (name, statements, progErrors) = prog
        HalfNamedProgram(name, statements, methods, functions, progErrors)
    }

val program: Parser<NamedProgram> =
    topLevel(delimit(many(or(sMethod, sFunction))) + sNamed) / { decs, (name, statements) ->
        val (methods, functions) = decs.splitEither()
        NamedProgram(name, statements, methods, functions)
    }


fun parseProgram(input: String): ParserResult<List<Statement>> =
    ParserState(input).parse(topLevel(program).div { it.statements })

fun tokenizeProgram(input: String): ParserResult<List<Token>> =
    ParserState(input)
        .parse(
            topLevel(
                many(
                    asum(
                        (_lineEnd * { char, pos -> LineEnd(char, pos) }) as Parser<Token>,
                        pAssumption as Parser<Token>,
                        pVariable as Parser<Token>,
                        pComment as Parser<Token>,
                        pIntLit as Parser<Token>,
                        pBoolLit as Parser<Token>,
                        pStrLit as Parser<Token>,
                        pChrLit as Parser<Token>,
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
