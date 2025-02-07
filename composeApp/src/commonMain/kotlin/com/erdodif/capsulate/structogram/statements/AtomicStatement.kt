package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.Atomic
import com.erdodif.capsulate.lang.program.grammar.Statement

@KParcelize
class AtomicStatement(val atom: Atomic) : ComposableStatement<Atomic>(atom) {

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Statement?
    ) {
        TODO("Not yet implemented, needs design")
    }

}
