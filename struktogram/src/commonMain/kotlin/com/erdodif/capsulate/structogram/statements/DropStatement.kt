@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.erdodif.capsulate.KIgnoredOnParcel
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.structogram.LocalDraggingStatement
import com.erdodif.capsulate.lang.program.grammar.Abort
import com.erdodif.capsulate.lang.program.grammar.Statement
import com.erdodif.capsulate.lang.program.grammar.parseProgram
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.MatchPos
import com.erdodif.capsulate.lang.util.ParserState
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.utility.onDpSize
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
@Serializable
class DropStatement(val string: String) : ComposableStatement<Statement>(
    when (val result = parseProgram(string)) {
        is Pass -> result.value.first()
        is Fail -> Abort(MatchPos.ZERO)
    }
) {
    @KIgnoredOnParcel
    private val toDrag = fromStatement(ParserState(string), statement)

    override fun toString(state: ParserState): String = string

    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        var size: DpSize by remember { mutableStateOf(DpSize.Zero) }
        key(LocalDraggingStatement.current.draggingInProgress) {
            DraggableArea(modifier.onDpSize(LocalDensity.current) { size = it }, true, size) {
                toDrag.Show(Modifier, false, null)
            }
        }
    }


}
