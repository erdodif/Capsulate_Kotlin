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
import com.erdodif.capsulate.lang.program.grammar.right
import com.erdodif.capsulate.lang.program.grammar.string
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
import com.erdodif.capsulate.lang.util.isWordChar
import com.erdodif.capsulate.lang.util.tok

@KParcelize
data class Pattern(
    val prefix: String?,
    val delimiters: List<String>,
    val variables: List<Variable>,
    val postfix: String?
) : KParcelable {
    fun toPatternString(): String = buildString {
        prefix?.apply(::append)
        delimiters.forEachIndexed { i, d -> append("\$${i + 1}$d") }
        if (variables.isNotEmpty()) {
            append("\$${delimiters.count() + 1}")
        }
        postfix?.apply(::append)
    }

    override fun toString(): String = buildString {
        prefix?.split("_")?.map { append(it) }
        if(variables.isNotEmpty()) append(" ")
        delimiters.forEachIndexed { i, d -> append("${variables[i].id} $d ") }
        if (variables.isNotEmpty()) {
            append(variables.last().id)
        }
        postfix?.split("_")?.map { append(" $it") }
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
        // Postfix-only patterns can only be made compile time, good for built in patterns
        a.postfix != b.postfix -> (a.postfix?.length ?: 0) - (b.postfix?.length ?: 0)
        // More variables are stronger
        a.variables.count() != b.variables.count() -> a.variables.count() - b.variables.count()
        // If all else fails, look at the delimiter length
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
    val post = delimiters.getOrNull(variables.count() - 1)
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

private fun ParserState.trySubWords(word: String): ParserResult<Unit> {
    val start = this.position
    for (subWord in word.split("_")) {
        if (!isWordChar(subWord.lastOrNull() ?: ' ')) {
            val result = tryParse(tok(string(subWord)))
            if (result is Fail) {
                return result
            }

        } else {
            val result = tryParse(_keyword(subWord))
            if (result is Fail) {
                return result
            }
        }
    }
    return pass(start)
}

fun ParserState.sSinglePattern(pattern: Pattern): ParserResult<List<Variable>> {
    var start = this.position
    var params: MutableList<Variable> = mutableListOf()
    val failed = "\"Pattern \"${pattern.toPatternString()}\" failed"
    if (pattern.prefix != null) {
        trySubWords(pattern.prefix).apply {
            if (this is Fail) {
                return copy("$failed on prefix \"${pattern.prefix}\" with reason:" + reason)
            }
        }
    }
    if (pattern.variables.isNotEmpty()) {
        when (val variableResult = tryParse(pVariable)) {
            is Pass -> params.add(variableResult.value)
            is Fail -> return fail("$failed on first variable with reason: ${variableResult.reason}")
        }
        for (delimiter in pattern.delimiters) {
            trySubWords(delimiter).apply {
                if (this is Fail) {
                    return fail("Pattern \"${pattern.toPatternString()}\" failed on delimiter \"${delimiter}\"")
                }
            }
            when (val variableResult = tryParse(pVariable)) {
                is Pass -> params.add(variableResult.value)
                is Fail -> return fail("$failed on variable with reason: ${variableResult.reason}")
            }
        }
    }

    if (pattern.postfix != null) {
        trySubWords(pattern.postfix).apply {
            if (this is Fail) {
                return copy("$failed on postfix \"${pattern.postfix}\" with reason:" + reason)
            }
        }
    }
    return if (params.count() == pattern.variables.count()) {
        pass(start, params)
    } else {
        return fail(
            "$failed to find enough variables " +
                    "(expected ${pattern.variables.count()}, bot got ${params.count()})."
        )
    }
}

val sKnownPattern: Parser<Pair<Pattern, List<Variable>>> = {
    val start = this.position
    var out: ParserResult<Pair<Pattern, List<Variable>>> =
        this.fail(
            "Failed to match with any of the known patterns, the patterns in order: ${
                methods.map { it.pattern }.sortedWith(patternComparator.reversed())
                    .joinToString { "\"${it.toString(this)}\"" }
            }"
        )
    for (pattern in methods.map { it.pattern }.sortedWith(patternComparator.reversed())) {
        when (val result = sSinglePattern(pattern)) {
            is Pass -> {
                out = Pass(pattern to result.value, this, result.match)
                break
            }

            is Fail -> {
                this.position = start
            }
        }
    }
    out
}