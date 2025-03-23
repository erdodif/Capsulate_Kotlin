package com.erdodif.capsulate.structogram

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.expression.Value
import com.erdodif.capsulate.lang.program.grammar.function.Function
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.statements.ComposableStatement

@KParcelize
class ComposableFunction(
    val name: String,
    val statements: List<ComposableStatement<*>>,
    val function: Function<Value>
) : KParcelable {
    constructor(function: Function<Value>, state: ParserState) : this(
        function.getHead(),
        function.body.map { ComposableStatement.fromStatement(state, it) },
        function
    )

    fun asStructogram(): Structogram = Structogram.fromStatements(statements, name = name)
}
