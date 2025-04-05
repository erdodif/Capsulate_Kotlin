package com.erdodif.capsulate.lang.specification.grammar

import com.erdodif.capsulate.lang.program.grammar.expression.Token
import com.erdodif.capsulate.lang.program.grammar.left
import com.erdodif.capsulate.lang.program.grammar.middle
import com.erdodif.capsulate.lang.program.grammar.orEither
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.string
import com.erdodif.capsulate.lang.specification.coc.App
import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Definition
import com.erdodif.capsulate.lang.specification.coc.Lam
import com.erdodif.capsulate.lang.specification.coc.Prod
import com.erdodif.capsulate.lang.specification.coc.Prop
import com.erdodif.capsulate.lang.specification.coc.PropProd
import com.erdodif.capsulate.lang.specification.coc.Set
import com.erdodif.capsulate.lang.specification.coc.SetProd
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.TypeProd
import com.erdodif.capsulate.lang.specification.coc.Variable
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.norm
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._char
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._natural
import com.erdodif.capsulate.lang.util._nonKeyword
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.div
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.times
import com.erdodif.capsulate.lang.specification.coc.Type as CocType

data class Var(val name: String, override val match: MatchPos) : Token(match)
data class Const(val name: String, override val match: MatchPos) : Token(match)
data class Type(val name: String, override val match: MatchPos) : Token(match)

//TODO: wellFormed Checks in parsers might not be needed, since the contexts' methods must cover violations

val sVar: Parser<Var> = _nonKeyword * { tok, pos -> Var(tok, pos) }

// Missing {Set, Prop}
val sSort: Parser<Sort> = orEither(
    right(_keyword("Type"), middle(_char('('), _natural, _char(')'))) / { CocType(it.toInt()) },
    orEither(_keyword("Prop") / { Prop }, _keyword("Set") / { Set })
)
val sType: Parser<Type> = _nonKeyword * { tok, pos -> Type(tok, pos) }

fun Context.sConst(): Parser<Variable> = sVar[{
    val variable = this@sConst[it.value.name]
    if (variable == null) {
        fail("Variable ${it.value} not found in context:\n${this@sConst}")
    } else {
        Pass(variable, this, it.match)
    }
}]

fun Context.sTerm(): Parser<Sort> = {
    asum(
        lambda(),
        forall(),
        assumption(),
        definition(),
        shortDefinition(),
        sSort,
        sConst(),
        app()
    )()
}

fun Context.app(): Parser<App> = (sTerm() + middle(_char('('), sTerm(), _char(')')))[{
    val (f, x) = it.value
    val app = app(f, x)
    Pass(app, this, it.match)
}]

fun Context.lambda(): Parser<Lam> =
    (right(orEither(_keyword("λ"), string("\\")), _nonKeyword) +
            right(_char(':'), sTerm()) + right(_char(','), sTerm()))[{
        val (assumption, type) = it.value
        val (x, term) = assumption
        val prod = lam(
            x, type, term, when (term) {
                Prop -> PropProd(Assumption(x, type), term.type)
                Set -> SetProd(Assumption(x, type), term.type)
                else -> TypeProd(Assumption(x, type), term.type, (term.type as CocType).level)
                //                                                TODO: Look^
            }
        )
        Pass(prod, this, it.match)
    }]

fun Context.forall(): Parser<Prod> =
    (right(orEither(_keyword("∀"), _keyword("forall")), _nonKeyword) +
            right(_char(':'), sTerm()) +
            right(_char(','), sTerm()))[{
        val (assumption, type) = it.value
        val (x, term) = assumption
        val prod = prod(x, type, term)
        Pass(prod, this, it.match)
    }]

fun Context.assumption(): Parser<Assumption> = (left(sVar, _char(':')) + sTerm())[{
    val (variable, type) = it.value
    try {
        if (!wellFormed(type)) {
            fail("$type isn't well formed in the context:\n${this@assumption}")
        } else {
            val assumption = assume(variable.name, type)
            add(assumption)
            Pass(
                assumption,
                this,
                it.match
            )
        }
    } catch (e: Exception) {
        fail(e.message ?: "Error while assuming $variable")
    }
}]

fun Context.shortDefinition(): Parser<Definition> =
    (left(sVar, _keyword(":=")) + sTerm())[{
        val (variable, value) = it.value
        try {
            val type = norm(value).type
            val definition = define(variable.name, value, type)
            add(definition)
            Pass(definition, this, it.match)
        } catch (e: Exception) {
            fail(e.message ?: "Error while defining $variable")
        }
    }]

fun Context.definition(): Parser<Definition> =
    (left(sVar, _keyword(":=")) + left(sTerm(), _char(':')) + sTerm())[{
        val (def, type) = it.value
        val (variable, value) = def
        try {
            when {
                !wellFormed(type) -> {
                    fail("Type for $variable ($type) isn't well formed in the context:\n${this@definition}")
                }

                !wellFormed(value) -> {
                    fail("Type for $variable ($value) isn't well formed in the context:\n${this@definition}")
                }

                else -> {
                    val definition = define(variable.name, value, type)
                    add(definition)
                    Pass(
                        definition,
                        this,
                        it.match
                    )
                }
            }
        } catch (e: Exception) {
            fail(e.message ?: "Error while defining $variable")
        }
    }]
