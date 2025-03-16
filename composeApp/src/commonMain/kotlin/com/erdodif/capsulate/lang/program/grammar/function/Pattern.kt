package com.erdodif.capsulate.lang.program.grammar.function

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Variable
import com.erdodif.capsulate.lang.program.grammar.anyChar
import com.erdodif.capsulate.lang.program.grammar.char
import com.erdodif.capsulate.lang.program.grammar.many
import com.erdodif.capsulate.lang.program.grammar.not
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.program.grammar.plus
import com.erdodif.capsulate.lang.program.grammar.program
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.whiteSpace
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Parser
import com.erdodif.capsulate.lang.util.ParserResult
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.lang.util._anyKeyword
import com.erdodif.capsulate.lang.util._keyword
import com.erdodif.capsulate.lang.util._lineEnd
import com.erdodif.capsulate.lang.util.asString
import com.erdodif.capsulate.lang.util.asum
import com.erdodif.capsulate.lang.util.get
import com.erdodif.capsulate.lang.util.tok

@KParcelize
data class Pattern(
    val prefix: String?,
    val delimiters: List<String>,
    val variables: List<Variable>,
    val postfix: String?
) : KParcelable {
    override fun toString(): String {
        return buildString {
            append("Pattern: ")
            prefix?.apply(::append)
            delimiters.forEachIndexed { i, d -> append("\$${i + 1}$d") }
            if (variables.isNotEmpty()) {
                append("\$${delimiters.count() + 1}")
            }
            postfix?.apply(::append)
            append("\nVariables: ")
            variables.forEach { append(it.id, " ") }
        }
    }

    fun toString(state: ParserState): String = buildString {
        append(prefix ?: "")
        variables.zip(delimiters).map { (variable, delim) ->
            append('$')
            append(variable.toString(state))
            append(delim)
        }
        if (variables.count() > delimiters.count()) {
            append('$')
            append(variables.last().toString(state))
        }
        if (variables.count() < delimiters.count()) {
            append(delimiters.last().toString())
        }
        append(postfix ?: "")
    }
}

val patternComparator = Comparator<Pattern> { a, b ->
    when {
        // Prefix must come first or else 'constant' method calls would be weaker
        a.prefix != b.prefix -> (a.prefix?.length ?: 0) - (b.prefix?.length ?: 0)
        a.postfix != b.postfix -> (a.postfix?.length ?: 0) - (b.postfix?.length ?: 0)
        a.variables.count() != b.variables.count() -> a.variables.count() - b.variables.count()
        else -> a.delimiters.flatMap { it.asIterable() }
            .count() - b.delimiters.flatMap { it.asIterable() }.count()
    }
}

val sPattern: Parser<Pattern> = (many(
    tok(
        right(
            not(asum(_anyKeyword, char('$'), char('{'), char('}'), whiteSpace, _lineEnd)), anyChar
        )
    )
) + many(
    tok(right(char('$'), pVariable)) +
            tok(
                many(
                    right(
                        not(
                            asum(_anyKeyword, char('$'), char('{'), char('}'), whiteSpace, _lineEnd)
                        ),
                        anyChar
                    )
                )
            )
))[{
    val (prefix, mixed) = it.value
    val variables = mixed.map { it.first }
    val delimiters = mixed.map { it.second.asString() }
    val post = delimiters.getOrNull(variables.count()-1)
    if (variables.isEmpty() && prefix.isEmpty()) {
        Fail("Matched an empty Pattern!", it.state)
    } else if (prefix.isEmpty() && delimiters.take(variables.count()).all { it.count() == 0 }) {
        Fail("Only variables matched! ($variables)", it.state)
    } else {
        Pass(
            Pattern(
                if (prefix.isEmpty()) null else prefix.asString(),
                if (variables.isEmpty()) emptyList() else delimiters.take(variables.count() - 1),
                variables,
                post
            ),
            it.state,
            it.match
        )
    }
}]

private fun ParserState.tryPattern(pattern: Pattern): List<Variable>? {
    var start = this.position
    var params: MutableList<Variable> = mutableListOf()
    if (pattern.prefix != null) {
        for (subWord in pattern.prefix.split("_")) {
            if (tryParse(_keyword(subWord)) is Fail) {
                this.position = start
                return null
            }
        }
    }
    if (pattern.variables.isNotEmpty()) {
        val delimiterResult = tryParse(pVariable)
        if (delimiterResult is Pass) {
            params.add(delimiterResult.value)
        } else {
            this.position = start
            return null
        }
        for (delimiter in pattern.delimiters) {
            for (subWord in delimiter.split("_")) {
                if (tryParse(_keyword(subWord)) is Fail) {
                    this.position = start
                    return null
                }
            }
            val delimiterResult = tryParse(pVariable)
            if (delimiterResult is Pass) {
                params.add(delimiterResult.value)
            } else {
                this.position = start
                return null
            }
        }
    }

    if (pattern.postfix != null) {
        for (subWord in pattern.postfix.split("_")) {
            if (tryParse(_keyword(subWord)) is Fail) {
                this.position = start
                return null
            }
        }
    }
    return if (params.count() == pattern.variables.count()) {
        params
    } else {
        null
    }
}

val sKnownPattern: Parser<Pair<Pattern, List<Variable>>> = {
    val start = this.position
    var out: ParserResult<Pair<Pattern, List<Variable>>> =
        this.fail("Failed to match with any of the known patterns")
    for (pattern in methods.map { it.pattern }.sortedWith(patternComparator.reversed())) {
        val result = tryPattern(pattern)
        if (result != null) {
            out = this.pass(start, pattern to result)
            break
        } else {
            this.position = start
        }
    }
    out
}