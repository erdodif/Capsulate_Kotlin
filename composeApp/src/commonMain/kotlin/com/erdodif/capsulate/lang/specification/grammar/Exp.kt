package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.program.grammar.Token
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Context
import com.erdodif.capsulate.lang.specification.coc.Definition
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._natural
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.specification.coc.Type as CocType

data class Var(val name: String, override val match: MatchPos) : Token(match)
data class Const(val name: String, override val match: MatchPos) : Token(match)
data class Type(val name: String, override val match: MatchPos) : Token(match)

val sVar: Parser<Var> = _nonKeyword * { tok, pos -> Var(tok, pos) }
val sType: Parser<Type> = _nonKeyword * { tok, pos -> Type(tok, pos) }
val sConst: Parser<Const> = _nonKeyword * { tok, pos -> Const(tok, pos) }
val sSort: Parser<CocType> = right(_keyword("Set"), middle(_char('('), _natural, _char(')'))) / { CocType(it.toInt()) }

fun assumption(context: Context): Parser<Assumption> = (left(sVar, _char(':')) + sType)[{
    val (variable, type) = it.value
    try {
        Pass(
            context.assume(variable.name, context[type.name]!!),
            this,
            MatchPos(variable.match.start, type.match.end)
        )
    }
    catch (e : Exception){
        fail(e.message ?: "Error while assuming $variable")
    }
}]

fun definition(context: Context): Parser<Definition> =
    (left(sVar, _keyword(":=")) + left(_nonKeyword, _char(':')) + sType)[{
        val (def, type) = it.value
        val (variable, value) = def
        try {
            Pass(
                context.define(variable.name, context[value]!!, context[type.name]!!),
                this,
                MatchPos(variable.match.start, type.match.end)
            )
        }
        catch (e : Exception){
            fail(e.message ?: "Error while defining $variable")
        }
    }]

