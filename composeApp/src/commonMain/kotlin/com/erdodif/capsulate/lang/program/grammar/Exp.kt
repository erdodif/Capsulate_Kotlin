package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.lang.program.grammar.operator.OperatorTable
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parameter
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._integer
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times

interface Exp<T : Value> {
    fun evaluate(env: Env): T
    fun toString(state: ParserState): String
}

open class Token(val match: MatchPos) {
    inline fun matchedToken(parserState: ParserState): String =
        parserState.input[match.start, match.end]
}

class KeyWord(val id: String, match: MatchPos) : Token(match)
class Symbol(val id: Char, match: MatchPos) : Token(match)
class LineEnd(val char: Char, match: MatchPos) : Token(match)
class Comment(val content: String, match: MatchPos) : Token(match)

val pComment: Parser<Comment> = orEither(
    right(
        (char('/') + _char('/')),
        left(many(right(not(char('\n')), anyChar)), or(_char('\n'), EOF))
    ),
    right(
        and(char('/'), _char('*')),
        left(many(right(not(char('*') + _char('/')), anyChar)), char('*') + _char('/'))
    )
) * { it, pos -> Comment(it.asString(), pos) }

class StrLit(val value: String, match: MatchPos) : Exp<VStr>, Token(match) {
    override fun evaluate(env: Env): VStr = VStr(value)
    override fun toString(state: ParserState): String = state[match]
}

class IntLit(val value: Int, match: MatchPos) : Exp<VWhole>, Token(match) {
    override fun evaluate(env: Env): VWhole = VWhole(value)
    override fun toString(state: ParserState): String = state[match]
}

class NatLit(val value: UInt, match: MatchPos) : Exp<VNat>, Token(match) {
    override fun evaluate(env: Env): VNat = VNat(value)
    override fun toString(state: ParserState): String = state[match]
}

class BoolLit(val value: Boolean, match: MatchPos) : Exp<VBool>, Token(match) {
    override fun evaluate(env: Env): VBool = VBool(value)
    override fun toString(state: ParserState): String = state[match]
}

class Variable(val id: String, match: MatchPos) : Exp<Value>, Token(match) {
    override fun evaluate(env: Env): Value {
        val param = env.get(id)
        if (param is Left) {
            return param.value.value
        } else {
            throw RuntimeException("Variable '$id' is not defined!")
        }
    }

    override fun toString(state: ParserState): String = state[match]
}


val pStrLit: Parser<StrLit> = middle(
    char('"'),
    many(orEither(right(char('\\'), anyChar), right(not(char('"')), anyChar))),
    _char('"')
) * { res, pos -> StrLit(res.asString(), pos) }

val pIntLit: Parser<IntLit> = _integer * { it, pos -> IntLit(it, pos) }

val pBoolLit: Parser<BoolLit> =
    or(_keyword("true"), _keyword("false")) * { it, pos -> BoolLit(it is Left<*>, pos) }

val pVariable: Parser<Variable> = _nonKeyword[{
    if (it.value[0].isDigit()) fail("Variable name can't start with digit!")
    else pass(it.match.start, Variable(it.value, it.match))
}]

val litOrder: Array<Parser<Exp<*>>> = arrayOf(
    pIntLit,
    pBoolLit,
    pStrLit,
    pVariable
)

typealias ExParser = Parser<Exp<*>>

inline fun pAtom(): ExParser = {
    // Can't be directly assigned, or else the pExp reference ------v___v would be null
    asum(*litOrder, middle(_char('('), pExp, _char(')')))()
}

val pExp: ExParser = OperatorTable().parser(pAtom())
