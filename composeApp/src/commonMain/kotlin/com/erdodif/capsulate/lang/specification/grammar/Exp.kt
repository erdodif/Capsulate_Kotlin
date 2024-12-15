package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.program.grammar.Token
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.or
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Definition
import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util.Right
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

// Missing {Set, Prop}
val sSort: Parser<Sort> = orEither(
    right(_keyword("Type"), middle(_char('('), _natural, _char(')'))) / { CocType(it.toInt()) },
    orEither(_keyword("Prop") / { Prop }, _keyword("Set") / { Set })
)
val sType: Parser<Type> = _nonKeyword * { tok, pos -> Type(tok, pos) }
val sConst: Parser<Const> = _nonKeyword * { tok, pos -> Const(tok, pos) }

fun assumption(context: Context): Parser<Assumption> = (left(sVar, _char(':')) + or(sSort, sType))[{
    val (variable, typeOrLabel) = it.value
    try {
        //TODO: Type label should be parsed as expression...
        val type: Sort? = when (typeOrLabel) {
            is Right<Type> -> context[typeOrLabel.value.name]
            is Left<Sort> -> typeOrLabel.value
        }
        if(type == null && typeOrLabel is Right<Type>){
            fail("${typeOrLabel.value.name} cannot be found in the context:\n$context")
        }
        else{
            val assumption = context.assume(variable.name, type!!)
            context.add(assumption)
            Pass(
                assumption,
                this,
                it.match
            )
        }
    } catch (e: Exception) {
        println(e)
        fail(e.message ?: "Error while assuming $variable")
    }
}]

fun definition(context: Context): Parser<Definition> =
    (left(sVar, _keyword(":=")) + left(_nonKeyword, _char(':')) + or(sSort, sType))[{
        val (def, typeOrLabel) = it.value
        val (variable, value) = def
        try {
            val type: Sort? = when (typeOrLabel) {
                is Right<Type> -> context[typeOrLabel.value.name]
                is Left<Sort> -> typeOrLabel.value
            }
            if(type == null && typeOrLabel is Right<Type>){
                fail("${typeOrLabel.value.name} cannot be found in the context:\n$context")
            }
            else{
                // TODO: Parse value (and probably type later)
                val definition = context.define(variable.name, context[value]!!, type!!)
                context.add(definition)
                Pass(
                    definition,
                    this,
                    it.match
                )
            }
        } catch (e: Exception) {
            println(e)
            fail(e.message ?: "Error while defining $variable")
        }
    }]

