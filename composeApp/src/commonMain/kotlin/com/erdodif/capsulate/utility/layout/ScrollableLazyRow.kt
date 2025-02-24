package com.erdodif.capsulate.utility.layout

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

const val SCROLL_MULTIPLIER = 40

@Composable
fun ScrollableLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: LazyListScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LazyRow(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                while (true) {
                    val event = awaitPointerEvent()
                    val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                    coroutineScope.launch {
                        state.scrollBy(scrollY * SCROLL_MULTIPLIER)
                    }
                }
            }
        },
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}