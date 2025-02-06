package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.AnyUniqueStatement
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement
import com.erdodif.capsulate.lang.program.grammar.UniqueStatement.Companion.unique

@KParcelize
class AtomicStatement(
    val atom: UniqueStatement<Atomic>
) : Statement<Atomic>(atom) {
    constructor(atom: Atomic): this(atom.unique())

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: AnyUniqueStatement?
    ) {
        TODO("Not yet implemented, needs design")
    }

}
