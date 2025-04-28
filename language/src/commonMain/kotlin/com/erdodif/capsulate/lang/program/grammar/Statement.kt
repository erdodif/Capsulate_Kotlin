@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar

import co.touchlab.kermit.Logger
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
import com.erdodif.capsulate.lang.program.grammar.expression.Index
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.util.toInt
import com.erdodif.capsulate.lang.util.toIntOrNull
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
abstract class Statement(
    @SerialName("base_id")
    open val id: Uuid = Uuid.random(),
    @SerialName("base-match")
    open val match: MatchPos
) : KParcelable {
    abstract fun evaluate(env: Environment): EvaluationResult
    override fun equals(other: Any?): Boolean = other is Statement && other.id == id
    override fun hashCode(): Int = id.hashCode()
    abstract fun Formatting.format(state: ParserState): Int
    fun onFormat(formatting: Formatting, state: ParserState): Int = formatting.format(state)
    fun getFormat(state: ParserState): String = Formatting(0).apply { format(state) }.finalize()

    fun <T : Value> Exp<T>.join(
        context: Environment,
        onValue: Environment.(T) -> EvaluationResult
    ): EvaluationResult = try {
        when (val result = evaluate(context)) {
            is Left -> onValue(context, result.value)
            is Right -> PendingFunctionEvaluation(result.value, onValue)
        }
    } catch (e: Exception) {
        AbortEvaluation(e.message ?: "Error while evaluating expression: $e")
    }

    fun <T : Value> List<Exp<T>>.joinAll(
        context: Environment,
        onEvery: Environment.(List<T>) -> EvaluationResult
    ): EvaluationResult = if (isEmpty()) onEvery(context, emptyList()) else
        this[0].join(context) {
            this@joinAll.drop(1).joinAll(context) { values ->
                onEvery(this, buildList { add(it); addAll(values) })
            }
        }
}

@KParcelize
@Serializable
data class If(
    val condition: Exp<*>,
    val statementsTrue: List<Statement>,
    val statementsFalse: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(
        condition: Exp<*>,
        statementsTrue: List<Statement>,
        statementsFalse: List<Statement>,
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
@Serializable
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
        if (blocks.isEmpty()) return AbortEvaluation("When conditions exhausted, Abort happens by definition")
        val shallowBlocks = blocks.toList().toMutableList()
        val source = shallowBlocks.removeAt((env.random.nextInt(blocks.size)))
        return source.first.join(env) {
            if (it is VBool) {
                when {
                    it.value -> EvalSequence(source.second)
                    shallowBlocks.isEmpty() && (this@When.elseBlock == null) ->
                        AbortEvaluation("When conditions exhausted, Abort happens by definition")

                    shallowBlocks.isEmpty() -> EvalSequence(this@When.elseBlock!!.toList())
                    else -> SingleStatement(
                        this@When.copy(
                            blocks = shallowBlocks,
                            elseBlock = this@When.elseBlock?.toList()
                        )
                    )
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
                    print("{")
                    val lines = block.second.firstOrNull()?.onFormat(this, state) ?: 0
                    print("}")
                    print(",")
                    lines + 1
                } else {
                    print("{")
                    val lines = singleCondition@ indent {
                        block.second.fencedForEach {
                            it.onFormat(this, state)
                        }
                    }
                    breakLine()
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
@Serializable
data class Skip(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Environment) = Finished
    override fun Formatting.format(state: ParserState) = print("skip")
}

@KParcelize
@Serializable
data class Abort(override val id: Uuid, override val match: MatchPos) : Statement(id, match) {
    constructor(match: MatchPos) : this(Uuid.random(), match)

    override fun evaluate(env: Environment) = AbortEvaluation("Abort has been called!")
    override fun Formatting.format(state: ParserState) = print("abort")
}

@Serializable
abstract class Loop(
    @SerialName("base_condition")
    open val condition: Exp<*>,
    @SerialName("base_statements")
    open val statements: List<Statement>,
    @SerialName("base_loop_id")
    override val id: Uuid,
    @SerialName("base_loop_match")
    override val match: MatchPos
) : Statement(id, match)

@KParcelize
@Serializable
data class While(
    override val condition: Exp<*>,
    override val statements: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: List<Statement>, match: MatchPos) :
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
@Serializable
data class DoWhile(
    override val condition: Exp<*>,
    override val statements: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match) {
    constructor(condition: Exp<*>, statements: List<Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        EvalSequence(statements + While(condition, statements, id, MatchPos.ZERO))

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
    val label: Index, val value: Exp<*>,
    override val id: Uuid, override val match: MatchPos
) : Statement(id, match) {
    constructor(label: String, value: Exp<*>, match: MatchPos) : this(Index(label), value, match)
    constructor(label: Index, value: Exp<*>, match: MatchPos) :
            this(label, value, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        label.indexers.joinAll(env) { indexers ->
            if (indexers.any { it !is VNum<*> }) {
                error(
                    "Non number indexer found " + indexers
                        .mapIndexed { i, v -> v to i }
                        .filter { it.first !is VNum<*> }
                        .joinToString(prefix = "(", postfix = ")") { (v, i) ->
                            "$v at $i"
                        }
                )
            }
            value.join(env) { value ->
                env.set(
                    label.id,
                    value,
                    *indexers.mapNotNull { ((it as? VNum<*>)?.value as? BigInteger)?.toIntOrNull() }
                        .toIntArray()
                )
                Finished
            }
        }

    override fun Formatting.format(state: ParserState): Int {
        print(buildString {
            append(label.id)
            label.indexers.forEach {
                append('[')
                append(it.toString(state))
                append(']')
            }
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

    override fun evaluate(env: Environment): EvaluationResult =
        AbortEvaluation("Sets are not implemented") //TODO Implement Zsák objektum

    override fun Formatting.format(state: ParserState): Int = print(state[match])
}

@KParcelize
@Serializable
data class ParallelAssign(
    private val indexes: List<Index>,
    private val values: List<Exp<Value>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    val assigns: List<Pair<Index, Exp<Value>>>
        get() = indexes.zip(values)

    constructor(assigns: List<Pair<Index, Exp<Value>>>, match: MatchPos) :
            this(assigns.map { it.first }, assigns.map { it.second }, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        assigns.map { it.first }.flatMap { it.indexers }.joinAll(env) { indexValues ->
            var i = 0
            val indexes = assigns.map { (arrayIndexer, _) ->
                val subList = indexValues.subList(i, i + arrayIndexer.indexers.size)
                require(subList.all { it is VNum<*> && it.value is BigInteger }) {
                    "Indexer must be at least an integer, got: " + subList.joinToString()
                }
                i += subList.size
                arrayIndexer.id to subList.map { ((it as VNum<*>).value as BigInteger).toInt() }
            }
            assigns.map { it.second }.joinAll(env) { values ->
                for ((index, value) in indexes.zip(values)) {
                    env.set(index.first, value, indexes = index.second.toIntArray())
                }
                Finished
            }
        }


    override fun Formatting.format(state: ParserState): Int {
        print(assigns.joinToString(", ") {
            it.first.id + it.first.indexers.joinToString {
                "[${it.toString(state)}]"
            }
        })
        print(" := ")
        print(assigns.joinToString(", ") { it.second.toString(state) })
        breakLine()
        return 1
    }
}

@KParcelize
@Serializable
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
@Serializable
data class LineError(val content: String, val line: Int) : KParcelable {
    init {
        Logger.e("LINEERROR") { " $line: $content" }
    }
}

@KParcelize
@Serializable
data class Parallel(
    val blocks: List<List<Statement>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(blocks: List<List<Statement>>, match: MatchPos) :
            this(blocks, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        ParallelEvaluation(blocks.map {
            if (it.size == 1) it.first() else EvalSequence(it)
        })

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
@Serializable
data class Atomic(
    val statements: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(statements: List<Statement>, pos: MatchPos) : this(statements, Uuid.random(), pos)

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
@Serializable
data class Wait(
    val condition: Exp<*>,
    val atomic: Atomic,
    val didLock: Boolean = false,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(condition: Exp<*>, atomic: Atomic, match: MatchPos) :
            this(condition, atomic, false, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        condition.join(env) {
            when (it) {
                is VBool -> {
                    if (it.value) {
                        AtomicEvaluation(atomic.statements)
                    } else {
                        SingleStatement(this@Wait.copy(didLock = true))
                    }
                }

                else -> AbortEvaluation("Condition must be a logical expression")
            }
        }

    override fun Formatting.format(state: ParserState): Int =
        print("await ${condition.toString(state)} ") + atomic.onFormat(this, state)

}
