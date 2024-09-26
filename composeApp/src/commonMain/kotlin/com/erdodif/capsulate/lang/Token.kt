package com.erdodif.capsulate.lang

import com.erdodif.capsulate.lang.grammar.operator.builtInOperators

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
            return (param as Left<Parameter, *>).value.value
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
    or(_keyword("true"), _keyword("false")) * { it, pos -> BoolLit(it is Left<*, *>, pos) }

val pVariable: Parser<Variable> =
    (satisfy { !it.isDigit() && it !in reservedChars } + _nonKeyword) * { it, pos ->
        Variable(
            it.first + it.second,
            pos
        )
    }

val litOrder: Array<Parser<*>> = arrayOf(
    pIntLit,
    pBoolLit,
    pStrLit,
    pVariable,
    // { middle(char('('), pExp , char(')'))() } //TODO: Circular dependency has to be resolved with pExp
)

typealias ExParser = Parser<Exp<*>>

@Suppress("UNCHECKED_CAST")
val pAtom: ExParser = asum(litOrder as Array<Parser<Exp<*>>>)

// TODO: add OperatorTable into pExp
val pExp: ExParser = pAtom//pNot as Parser<Exp<*>>
