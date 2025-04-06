@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.evaluation.AbortEvaluation
import com.erdodif.capsulate.lang.program.evaluation.AtomicEvaluation
import com.erdodif.capsulate.lang.program.evaluation.PendingFunctionEvaluation
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.evaluation.ParallelEvaluation
import com.erdodif.capsulate.lang.program.evaluation.SingleStatement
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import kotlinx.serialization.Serializable
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
abstract class Statement(
    open val id: Uuid = Uuid.random(),
    open val match: MatchPos
) : KParcelable {
    abstract fun evaluate(env: Environment): EvaluationResult
    override fun equals(other: Any?): Boolean = other is Statement && other.id == id
    override fun hashCode(): Int = id.hashCode()
    abstract fun Formatting.format(state: ParserState): Int
    fun onFormat(formatting: Formatting, state: ParserState): Int = formatting.format(state)
    fun getFormat(state: ParserState): String = Formatting(0).apply { format(state) }.finalize()

    fun <R : Value> Exp<R>.join(
        context: Environment,
        onValue: Environment.(R) -> EvaluationResult
    ): EvaluationResult = try {
        when (val result = evaluate(context)) {
            is Left -> onValue(context, result.value)
            is Right -> PendingFunctionEvaluation(result.value, onValue)
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression: $e")
    }

    fun List<Exp<Value>>.joinAll(
        context: Environment,
        onEvery: Environment.(List<Value>) -> EvaluationResult
    ): EvaluationResult = if (isEmpty()) onEvery(context, emptyList()) else
        this[0].join(context) {
            this@joinAll.drop(1).joinAll(context) { values ->
                onEvery(this, buildList { add(it); addAll(values) })
            }
        }
}

@KParcelize
data class If(
    val condition: Exp<*>,
    val statementsTrue: ArrayList<out Statement>,
    val statementsFalse: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(
        condition: Exp<*>,
        statementsTrue: List<Statement>,
        statementsFalse: List<Statement>,
        match: MatchPos
    ) : this(condition, ArrayList(statementsTrue), ArrayList(statementsFalse), Uuid.random(), match)

    constructor(
        condition: Exp<*>,
        statementsTrue: ArrayList<Statement>,
        statementsFalse: ArrayList<Statement>,
        match: MatchPos
    ) : this(condition, statementsTrue, statementsFalse, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = condition.join(env) {
        when {
            it is VBool && it.value -> EvalSequence(statementsTrue)
            it is VBool -> EvalSequence(statementsFalse)
            else -> AbortEvaluation("Condition must be a logical expression")
        }
    }

    override fun Formatting.format(state: ParserState): Int {
        val resultTrue = preFormat {
            statementsTrue.fencedForEach {
                it.onFormat(
                    this@preFormat,
                    state
                )
            }
        }
        val resultFalse = preFormat {
            statementsFalse.fencedForEach {
                it.onFormat(
                    this@preFormat,
                    state
                )
            }
        }
        print("if ")
        print(condition.toString(state))
        if (resultTrue.count() == 1 && resultFalse.count() == 1) {
            print("{ ")
            print(resultTrue.first().first)
            print(" } else { ")
            print(resultFalse.first().first)
            print(" }")
            return 1
        } else {
            print("{")
            indent {
                appendAll(resultTrue)
            }
            append(" }")
            print(" else {")
            indent {
                appendAll(resultFalse)
            }
            append("}")
            return resultTrue.count() + resultFalse.count() + 4
        }
    }
}

@KParcelize
data class When(
    val blocks: MutableList<Pair<Exp<*>, List<Statement>>>,
    val elseBlock: List<Statement>? = null,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(
        block: List<Pair<Exp<*>, List<Statement>>>,
        elseBlock: List<Statement>? = null,
        match: MatchPos
    ) : this(block.toMutableList(), elseBlock, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult {
        val source = blocks.removeAt(env.random.nextInt(blocks.size))
        return source.first.join(env) {
            if (it is VBool) {
                when {
                    it.value -> EvalSequence(source.second)
                    blocks.isEmpty() ->
                        AbortEvaluation("When conditions exhausted, Abort happens by definition")

                    else -> SingleStatement(this@When)
                }
            } else {
                AbortEvaluation("Condition must be a logical expression")
            }
        }
    }

    override fun Formatting.format(state: ParserState): Int {
        print("when {")
        val lines = conditions@ indent {
            blocks.fencedForEach { block ->
                print(block.first.toString(state) + " : ")
                if (block.second.count() < 2) {
                    val lines = block.second.firstOrNull()?.onFormat(this, state) ?: print("{ }")
                    print(",")
                    lines + 1
                } else {
                    printLine("{")
                    val lines = singleCondition@ indent {
                        block.second.fencedForEach {
                            it.onFormat(this, state)
                        }
                    }
                    print("},")
                    lines + 2
                }
            } + if (elseBlock != null) {
                append("else: ")
                if (elseBlock.count() < 2) {
                    (elseBlock.firstOrNull()?.onFormat(this, state) ?: print("{ }")) + 1
                } else {
                    printLine("{")
                    val lines = elseBlock.fencedForEach {
                        it.onFormat(this, state)
                    }
                    printLine("}")
                    lines + 2
                }
            } else 0
        }
        append("}")
        return lines + 2
    }
}

@KParcelize
data class Skip(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Environment) = Finished
    override fun Formatting.format(state: ParserState) = print("skip")
}

@KParcelize
data class Abort(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Environment) = AbortEvaluation("Abort has been called!")

    override fun Formatting.format(state: ParserState) = print("abort")

}

abstract class Loop(
    open val condition: Exp<*>,
    open val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match)

@KParcelize
data class While(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = condition.join(env) {
        when (it) {
            is VBool -> {
                if (it.value) {
                    EvalSequence(statements + this@While)
                } else {
                    Finished
                }
            }

            else -> AbortEvaluation("Condition must be a logical expression")
        }
    }

    override fun Formatting.format(state: ParserState): Int {
        val result = preFormat {
            statements.fencedForEach {
                it.onFormat(
                    this@preFormat,
                    state
                )
            }
        }
        print("while ")
        print(condition.toString(state))
        if (result.count() == 0) {
            print(" { ")
            appendAll(result)
            print(" }")
        } else {
            print(" {")
            indent {
                appendAll(result)
            }
            append("}")
        }
        return if (result.count() == 0) 1 else result.count() + 2
    }

}

@KParcelize
data class DoWhile(
    override val condition: Exp<*>,
    override val statements: ArrayList<out Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: ArrayList<out Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        EvalSequence(statements + While(condition, statements, MatchPos.ZERO))

    override fun Formatting.format(state: ParserState): Int {
        val result = preFormat {
            statements.fencedForEach {
                it.onFormat(
                    this@preFormat,
                    state
                )
            }
        }
        val lines = result.count()
        print("do {")
        if (lines == 0) {
            appendAll(result)
            print("} while")
        } else {
            indent {
                appendAll(result)
            }
            append("} while ")
        }
        print(condition.toString(state))
        return if (lines == 0) 1 else lines + 2
    }
}

@KParcelize
data class Assign(
    val label: String, val value: Exp<*>, override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(label: String, value: Exp<*>, match: MatchPos) :
            this(label, value, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = value.join(env) {
        env.set(label, it)
        Finished
    }

    override fun Formatting.format(state: ParserState): Int {
        print(buildString {
            append(label)
            append(" := ")
            append(value.toString(state))
        })
        return 0
    }
}

@KParcelize
data class Select(
    val label: String, val set: String /*Specification: Type*/,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(label: String, set: String, match: MatchPos) :
            this(label, set, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult {
        TODO("Implement 'Zsák objektum'")
    }

    override fun Formatting.format(state: ParserState): Int {
        TODO("Not yet implemented")
    }
}

@KParcelize
data class ParallelAssign(
    val assigns: ArrayList<Pair<String, Exp<Value>>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(assigns: ArrayList<Pair<String, Exp<Value>>>, match: MatchPos) :
            this(assigns, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        assigns.map { it.second }.joinAll(env) {
            for (assign in assigns.map { it.first }.zip(it)) env.set(
                assign.first,
                assign.second
            )
            Finished
        }

    override fun Formatting.format(state: ParserState): Int {
        print(assigns.joinToString(", ") { it.first.toString() })
        print(" := ")
        print(assigns.joinToString(", ") { it.second.toString(state) })
        breakLine()
        return 1
    }
}

@KParcelize
data class Expression(
    val expression: Exp<Value>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(expression: Exp<Value>, match: MatchPos) : this(expression, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult {
        return try {
            expression.join(env) { Finished }
        } catch (e: Exception) {
            AbortEvaluation(e.message ?: "Error while evaluating Expression!")
        }
    }

    override fun Formatting.format(state: ParserState) = print(expression.toString(state))
}

@KParcelize
data class LineError(val content: String) : KParcelable

@KParcelize
data class Parallel(
    val blocks: ArrayList<out ArrayList<out Statement>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(blocks: ArrayList<out ArrayList<out Statement>>, match: MatchPos) :
            this(blocks, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        ParallelEvaluation(blocks.map { EvalSequence(it) })

    override fun Formatting.format(state: ParserState): Int {
        val results = buildList<List<Pair<String, Int>>> {
            blocks.forEach { statements ->
                add(preFormat {
                    statements.fencedForEach { statement -> statement.onFormat(this, state) }
                })
            }
        }
        if (results.sumOf { it.sumOf { it.second } } == 0) {
            printLine(
                results.joinToString(
                    " }|{ ",
                    prefix = "{ ",
                    postfix = " }"
                ) { it.first().first })
            return 1
        } else {
            print("{")
            var i = 0
            while (i < results.count() - 1) {
                indent {
                    appendAll(results[i])
                }
                breakLine()
                print("}|{")
                i++
            }
            if (results.isNotEmpty()) {
                indent {
                    appendAll(results.last())
                }
                append("}")
            } else {
                print(" }")
            }
            return results.sumOf { it.sumOf { it.second + 2 } } + 2
        }
    }
}

@KParcelize
data class Atomic(
    val statements: ArrayDeque<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(statements: List<Statement>, match: MatchPos) :
            this(ArrayDeque(statements), Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = AtomicEvaluation(this.statements)
    override fun Formatting.format(state: ParserState): Int {
        val result = preFormat {
            statements.fencedForEach { it.onFormat(this, state) }
        }
        val lines = result.count()
        return if (lines == 0) {
            print("[{")
            print(result)
            print("}]")
            lines
        } else {
            print("[{")
            indent { appendAll(result) }
            append("}]")
            lines + 2
        }
    }
}

@KParcelize
data class Wait(
    val condition: Exp<*>,
    val atomic: Atomic,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(condition: Exp<*>, atomic: Atomic, match: MatchPos) :
            this(condition, atomic, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        condition.join(env) {
            when (it) {
                is VBool -> {
                    if (it.value) {
                        AtomicEvaluation(atomic.statements)
                    } else {
                        SingleStatement(this@Wait)
                    }
                }

                else -> AbortEvaluation("Condition must be a logical expression")
            }
        }

    override fun Formatting.format(state: ParserState): Int =
        print("await ${condition.toString(state)} ") + atomic.onFormat(this, state)

}
