package com.erdodif.capsulate.utility.layout

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun WindowLazyList(
    modifier: Modifier = Modifier,
    axisWidth: Dp = Dp.Unspecified,
    rowPreferenceOn: WindowWidthSizeClass= WindowWidthSizeClass.Companion.COMPACT,
    content: LazyListScope.() -> Unit
) = WindowLazyList(modifier, axisWidth, axisWidth, rowPreferenceOn, content)

@Composable
fun WindowLazyList(
    modifier: Modifier = Modifier,
    widthColumn: Dp,
    heightRow: Dp,
    rowPreferenceOn: WindowWidthSizeClass= WindowWidthSizeClass.Companion.COMPACT,
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val windowSize = currentWindowAdaptiveInfo()
    if (windowSize.windowSizeClass.windowWidthSizeClass == rowPreferenceOn) {
        ScrollableLazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = modifier.fillMaxWidth().height(heightRow),
            content = content
        )
    } else {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = modifier.fillMaxHeight().width(widthColumn),
            content = content
        )
    }
}
