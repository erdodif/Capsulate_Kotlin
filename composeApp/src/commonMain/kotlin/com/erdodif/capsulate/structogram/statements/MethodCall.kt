@file:OptIn(ExperimentalUuidApi::class)

package com.erdodif.capsulate.structogram.statements

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.KParcelize
import com.erdodif.capsulate.lang.program.grammar.function.MethodCall
import com.erdodif.capsulate.lang.util.ParserState
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KParcelize
class MethodCallStatement(
    val content: String,
    override val statement: MethodCall,
) : ComposableStatement<MethodCall>(statement) {
    constructor(statement: MethodCall, state: ParserState) : this(
        statement.toString(state),
        statement
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Show(
        modifier: Modifier,
        draggable: Boolean,
        activeStatement: Uuid?
    ) {
        val sheetState = rememberModalBottomSheetState()
        val coroutineScope = rememberCoroutineScope()
        Command(content, statement).Show(modifier.clickable {
            coroutineScope.launch {
                sheetState.show()
            }
        }, draggable, activeStatement)
        if (sheetState.isVisible) {
            ModalBottomSheet(
                { coroutineScope.launch { sheetState.hide() } },
                sheetState = sheetState
            ) {
                Text("This will be one time a structogram")
                /*Structogram(statement.method.program.toTypedArray(), content).Content(
                    modifier,
                    false,
                    activeStatement
                )*/
            }
        }
    }
}