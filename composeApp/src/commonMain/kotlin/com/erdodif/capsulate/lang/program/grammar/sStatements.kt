package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.grammar.expression.Type
import com.erdodif.capsulate.lang.program.grammar.expression.pExp
import com.erdodif.capsulate.lang.program.grammar.function.sMethodCall
import com.erdodif.capsulate.lang.program.grammar.function.sReturn
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._lineBreak
import com.erdodif.capsulate.lang.util._lineEnd
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.util.tok

fun <T> delimit(parser: Parser<T>): Parser<T> = left(parser, many(_lineEnd))

/** Allows tokenized line ends to jump to next statement */
fun <T> newLined(parser: Parser<T>): Parser<T> = left(parser, many(_lineBreak))

val nonParallel: Parser<Statement> = {
    asum(
        sMethodCall,
        sReturn,
        sSkip,
        sAbort,
        sWait,
        sSelect,
        sParallelAssign,
        sWhen,
        sIf,
        sWhile,
        sDoWhile,
        sExpression,
        sAtom,
    )()
}

val statement: Parser<Statement> = { orEither(sParallel, nonParallel)() }

val blockOrParallel: (ParserState) -> ParserResult<ArrayList<out Statement>> = { state ->
    orEither(
        newLined(sParallel) / { arrayListOf(it) },
        orEither(
            (newLined(_char('{')) + newLined(_char('}'))) / { arrayListOf() },
            middle(newLined(_char('{')), some(delimit(statement)), newLined(_char('}'))),
        ),
    )(state)
}

val statementOrBlock: Parser<ArrayList<out Statement>> =
    orEither(blockOrParallel, nonParallel / { arrayListOf(it) })

val sError: Parser<LineError> =
    delimit(some(satisfy { it !in lineEnd })) / { LineError(it.asString()) }

val sSkip: Parser<Statement> = delimit(_keyword("skip")) * { _, pos -> Skip(pos) }
val sAbort: Parser<Statement> = delimit(_keyword("abort") * { _, pos -> Abort(pos) })

val sAtom: Parser<Statement> =
    delimit(middle(_keyword("["), blockOrParallel, _keyword("]"))) * { atom, pos ->
        Atomic(atom, pos)
    }
val sWait: Parser<Statement> = delimit(
    right(
        _keyword("await"),
        delimit(pExp) + or(sAtom, record(blockOrParallel))
    ) * { (condition, atomic), pos ->
        Wait(
            condition,
            when (val inner = atomic) {
                is Left -> inner.value as Atomic
                is Right -> Atomic(inner.value.first, inner.value.second)
            },
            pos
        )
    }
)

val sExpression: Parser<Statement> = delimit(pExp) * { exp, pos -> Expression(exp, pos) }

val sIf: Parser<Statement> =
    (right(_keyword("if"), delimit(pExp) + blockOrParallel) +
            right(delimit(_keyword("else")), blockOrParallel)) *
            { (core, falseBranch), pos ->
                val (condition, trueBranch) = core
                If(condition, trueBranch, falseBranch, pos)
            }

val sWhen: Parser<Statement> =
    (middle(
        newLined(_keyword("when")) + newLined(_char('{')),
        (many(left(left(pExp, newLined(_char(':'))) + statementOrBlock, newLined(_char(',')))) +
                optional(left(pExp, newLined(_char(':'))) + statementOrBlock)) +
                optional(
                    middle(
                        newLined(_keyword("else") + _char(':')),
                        statementOrBlock,
                        optional(newLined(_char(','))),
                    )
                ),
        newLined(_char('}')),
    )) * { (statements, elseBlock), pos ->
        val (blocks, trailing) = statements
        if (trailing != null) blocks.add(trailing)
        When(blocks, elseBlock, pos)
    }

val sWhile: Parser<Statement> =
    (middle(
        _keyword("while"), pExp, many(_char('\n'))
    ) + blockOrParallel) * { (condition, block), pos ->
        While(condition, block, pos)
    }

val sDoWhile: Parser<Statement> = right(
    _keyword("do") + many(_char('\n')),
    blockOrParallel + delimit(right(_keyword("while"), pExp)),
) * { (block, condition), pos -> DoWhile(condition, block, pos) }

val sParallelAssign: Parser<Statement> =
    (delimited(_nonKeyword, _char(',')) + right(
        tok(string(":=")),
        delimit(delimited(pExp, _char(',')))
    ))[{
        val (params, values) = it.value
        if (values.count() != params.count())
            fail("The number of parameters does not match the number of values to assign.")
        else if (values.count() == 1){
            pass(it.match.start, Assign(params.first(), values.first(), it.match))
        }
        else{
            pass(
                it.match.start,
                ParallelAssign(params.zip(values) as ArrayList, it.match),
            )
        }
    }]

val sAssign: Parser<Statement> =
    delimit(_nonKeyword + right(_keyword(":="), pExp)) * { (variable, value), pos ->
        Assign(variable, value, pos)
    }

val pType: Parser<Type> = { pass(0, Type.NEVER) } // TODO :get from specification part
val sSelect: Parser<Statement> =
    delimit(_nonKeyword + right(_keyword(":∈"), pType)) * { (label, set), pos ->
        Select(label, set.toString(), pos)
    }

val sParallel: Parser<Statement> = {
    (delimited2(
        middle(newLined(_char('{')), many(delimit(statement)), newLined(_char('}'))),
        _char('|') + many(_lineBreak),
    ) * { stmt, pos -> Parallel(stmt, pos) as Statement })()
}

