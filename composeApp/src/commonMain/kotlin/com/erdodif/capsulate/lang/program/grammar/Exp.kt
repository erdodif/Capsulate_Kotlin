@file:Suppress("NOTHING_TO_INLINE")

package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.operator.builtInOperatorTable
import com.erdodif.capsulate.lang.util.Env
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._integer
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times

interface Exp<T : Value>: KParcelable {
    fun evaluate(context: Env): T
    fun toString(state: ParserState): String
}

@KParcelize
open class Token(open val match: MatchPos): KParcelable {
    inline fun matchedToken(parserState: ParserState): String =
        parserState.input[match.start, match.end]
}

@KParcelize
class KeyWord(val id: String,override val match: MatchPos) : Token(match)
@KParcelize
class Symbol(val id: Char,override val match: MatchPos) : Token(match)
@KParcelize
class LineEnd(val char: Char,override val match: MatchPos) : Token(match)
@KParcelize
class Comment(val content: String,override val match: MatchPos) : Token(match)

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

@KParcelize
class StrLit(val value: String,override val match: MatchPos) : Exp<VStr>, Token(match) {
    override fun evaluate(context: Env): VStr = VStr(value)
    override fun toString(state: ParserState): String = state[match]
}

@KParcelize
class IntLit(val value: Int,override val  match: MatchPos) : Exp<VWhole>, Token(match) {
    override fun evaluate(context: Env): VWhole = VWhole(value)
    override fun toString(state: ParserState): String = state[match]
}

@KParcelize
class NatLit(val value: UInt,override val  match: MatchPos) : Exp<VNat>, Token(match) {
    override fun evaluate(context: Env): VNat = VNat(value)
    override fun toString(state: ParserState): String = state[match]
}

@KParcelize
class BoolLit(val value: Boolean,override val  match: MatchPos) : Exp<VBool>, Token(match) {
    override fun evaluate(context: Env): VBool = VBool(value)
    override fun toString(state: ParserState): String = state[match]
}

@KParcelize
class Variable(val id: String,override val  match: MatchPos) : Exp<Value>, Token(match) {
    override fun evaluate(context: Env): Value {
        val param = context.get(id)
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

typealias ExParser = Parser<Exp<Value>>

@Suppress("UNCHECKED_CAST")
inline fun pAtom(): ExParser = {
    // Can't be directly assigned, or else the pExp reference ------v___v would be null
    asum(*litOrder, middle(_char('('), pExp, _char(')')))() as ParserResult<Exp<Value>>
}

val pExp: Parser<Exp<Value>> = builtInOperatorTable.parser(pAtom())
