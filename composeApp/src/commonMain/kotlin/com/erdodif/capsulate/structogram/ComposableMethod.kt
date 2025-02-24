package com.erdodif.capsulate.structogram

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.function.Method
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.structogram.statements.ComposableStatement

@KParcelize
class ComposableMethod(val name: String, val statements: List<ComposableStatement<*>>) :
    KParcelable {
    constructor(method: Method, state: ParserState) : this(
        method.pattern.toString(),
        method.program.map { ComposableStatement.fromStatement(state, it) })

    fun asStructogram(): Structogram = Structogram.fromStatements(statements, name = name)
}