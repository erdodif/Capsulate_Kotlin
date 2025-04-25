@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.lang.program.grammar

import co.touchlab.kermit.Logger
import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Exp
import com.erdodif.capsulate.lang.program.grammar.expression.VBool
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.evaluation.AbortEvaluation
import com.erdodif.capsulate.lang.program.evaluation.AdditionalExpressionEvaluator
import com.erdodif.capsulate.lang.program.evaluation.AtomicEvaluation
import com.erdodif.capsulate.lang.program.evaluation.Environment
import com.erdodif.capsulate.lang.program.evaluation.EvalSequence
import com.erdodif.capsulate.lang.program.evaluation.EvaluationResult
import com.erdodif.capsulate.lang.program.evaluation.ExpressionEvaluator
import com.erdodif.capsulate.lang.program.evaluation.ExpressionListEvaluator
import com.erdodif.capsulate.lang.program.evaluation.Finished
import com.erdodif.capsulate.lang.program.evaluation.ParallelEvaluation
import com.erdodif.capsulate.lang.program.evaluation.SingleStatement
import com.erdodif.capsulate.lang.program.grammar.expression.VArray.Index
import com.erdodif.capsulate.lang.program.grammar.expression.VNum
import com.erdodif.capsulate.lang.util.toInt
import com.erdodif.capsulate.lang.util.toIntOrNull
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.erdodif.capsulate.lang.util.Either
import com.erdodif.capsulate.lang.util.Formatting
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.lang.util.filterLeft
import kotlinx.serialization.Serializable
import kotlin.collections.plus
import kotlin.jvm.JvmSerializableLambda
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

}

@KParcelize
@Serializable
data class If(
    val condition: Exp<*>,
    val statementsTrue: List<Statement>,
    val statementsFalse: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match), ExpressionEvaluator<Value> {
    constructor(
        condition: Exp<*>,
        statementsTrue: List<Statement>,
        statementsFalse: List<Statement>,
        match: MatchPos
    ) : this(condition, statementsTrue, statementsFalse, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = condition.join(env)
    override fun onValue(context: Environment, value: Value): EvaluationResult = when {
        value is VBool && value.value -> EvalSequence(statementsTrue)
        value is VBool -> EvalSequence(statementsFalse)
        else -> AbortEvaluation("Condition must be a logical expression")
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
) : Statement(id, match), AdditionalExpressionEvaluator<Value, List<Statement>> {
    constructor(
        block: List<Pair<Exp<*>, List<Statement>>>,
        elseBlock: List<Statement>? = null,
        match: MatchPos
    ) : this(block.toMutableList(), elseBlock, Uuid.random(), match)

    override fun onValue(
        context: Environment,
        value: Value,
        extra: List<Statement>
    ): EvaluationResult = if (value is VBool) {
        when {
            value.value -> EvalSequence(extra)
            blocks.isEmpty() ->
                AbortEvaluation("When conditions exhausted, Abort happens by definition")

            else -> SingleStatement(this@When)
        }
    } else {
        AbortEvaluation("Condition must be a logical expression")
    }


    override fun evaluate(env: Environment): EvaluationResult {
        if (blocks.isEmpty()) return AbortEvaluation("When conditions exhausted, Abort happens by definition")
        val source = blocks.removeAt(env.random.nextInt(blocks.size))
        return source.first.join(env, source.second)
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
    open val condition: Exp<*>,
    open val statements: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match)

@KParcelize
@Serializable
data class While(
    override val condition: Exp<*>,
    override val statements: List<Statement>,
    override val id: Uuid,
    override val match: MatchPos
) : Loop(condition, statements, id, match), ExpressionEvaluator<Value> {
    constructor(condition: Exp<*>, statements: List<Statement>, match: MatchPos) :
            this(condition, statements, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = condition.join(env)
    override fun onValue(context: Environment, value: Value): EvaluationResult = when (value) {
        is VBool -> if (value.value) EvalSequence(statements + this@While) else Finished
        else -> AbortEvaluation("Condition must be a logical expression")
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
@Serializable
data class Assign(
    val label: Either<Index, String>, val value: Exp<*>,
    override val id: Uuid, override val match: MatchPos
) : Statement(id, match), AdditionalExpressionEvaluator<Value, String>,
    ExpressionListEvaluator<Value> {
    constructor(label: String, value: Exp<*>, match: MatchPos) :
            this(Right(label), value, Uuid.random(), match)

    constructor(label: Either<Index, String>, value: Exp<*>, match: MatchPos) :
            this(label, value, Uuid.random(), match)

    override fun onValue(context: Environment, value: Value, label: String): EvaluationResult {
        context.set(label, value)
        return Finished
    }

    override fun onEvery(context: Environment, values: List<Value>): EvaluationResult {
        if (values.any { it !is VNum<*> }) {
            error(
                "Non number indexer found " + values
                    .mapIndexed { i, v -> v to i }
                    .filter { it.first !is VNum<*> }
                    .joinToString(prefix = "(", postfix = ")") { (v, i) ->
                        "$v at $i"
                    }
            )
        }
        value.join(env)
    }

    override fun evaluate(env: Environment): EvaluationResult = when (label) {
        is Right -> value.join(env, label.value)
        is Left -> label.value.indexers.joinAll(env) @JvmSerializableLambda { indexers ->
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
            value.join(env) @JvmSerializableLambda { value ->
                env.set(
                    label.value.id,
                    value,
                    *indexers.mapNotNull { ((it as? VNum<*>)?.value as? BigInteger)?.toIntOrNull() }
                        .toIntArray()
                )
                Finished
            }

        }
    }

    override fun Formatting.format(state: ParserState): Int {
        print(buildString {
            when (label) {
                is Right -> append(label.value)
                is Left -> {
                    append(label.value.id)
                    label.value.indexers.forEach {
                        append('[')
                        append(it.toString(state))
                        append(']')
                    }
                }
            }
            append(" := ")
            append(value.toString(state))
        })
        return 0
    }
}

@KParcelize
@Serializable
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
    val assigns: List<Pair<Either<Index, String>, Exp<Value>>>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match) {
    constructor(assigns: List<Pair<Either<Index, String>, Exp<Value>>>, match: MatchPos) :
            this(assigns, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult =
        assigns.map { it.first }.filterLeft().joinAll(env) @JvmSerializableLambda { indexes ->
            var count = 0
            assigns.map { it.second }.joinAll(env) @JvmSerializableLambda {
                for (assign in assigns.map { it.first }.zip(it)) {
                    when (val label = assign.first) {
                        is Right -> env.set(
                            label.value,
                            assign.second
                        )

                        is Left -> {
                            when (val index = indexes[count]) {
                                is VNum<*> -> env.set(
                                    label.value.id,
                                    assign.second,
                                    (index.value as? BigInteger)?.toInt()
                                        ?: error("Decimal index is not supported!")
                                )

                                else -> error("Non number indexer found (namely $index)")
                            }
                            count += 1
                        }

                    }

                }
                Finished
            }
        }


    override fun Formatting.format(state: ParserState): Int {
        print(assigns.joinToString(", ") {
            when (val label = it.first) {
                is Right -> label.value
                is Left -> label.value.id + label.value.indexers.joinToString {
                    "[${it.toString(state)}]"
                }
            }
        })
        print(" := ")
        print(assigns.joinToString(", ") { it.second.toString(state) })
        return 1
    }
}

@KParcelize
@Serializable
data class Expression(
    val expression: Exp<Value>,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match), ExpressionEvaluator<Value> {
    constructor(expression: Exp<Value>, match: MatchPos) : this(expression, Uuid.random(), match)

    override fun onValue(context: Environment, value: Value): EvaluationResult = Finished
    override fun evaluate(env: Environment): EvaluationResult {
        return try {
            expression.join(env)
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
@Serializable
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
@Serializable
data class Wait(
    val condition: Exp<*>,
    val atomic: Atomic,
    override val id: Uuid,
    override val match: MatchPos
) : Statement(id, match), ExpressionEvaluator<Value> {
    constructor(condition: Exp<*>, atomic: Atomic, match: MatchPos) :
            this(condition, atomic, Uuid.random(), match)

    override fun evaluate(env: Environment): EvaluationResult = condition.join(env)
    override fun onValue(context: Environment, value: Value): EvaluationResult = when {
        value is VBool && value.value -> AtomicEvaluation(atomic.statements)
        value is VBool -> SingleStatement(this@Wait)
        else -> AbortEvaluation("Condition must be a logical expression")
    }

    override fun Formatting.format(state: ParserState): Int =
        print("await ${condition.toString(state)} ") + atomic.onFormat(this, state)

}
