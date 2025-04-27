package com.erdodif.capsulate.utility.layout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass.Companion.COMPACT

@Composable
fun WindowWidthLayout(
    onWide: @Composable () -> Unit,
    onTall: @Composable () -> Unit
) = if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == COMPACT) {
    onTall()
} else {
    onWide()
}

@Composable
fun WindowWidthLayout(
    onWide: @Composable (@Composable () -> Unit) -> Unit,
    onTall: @Composable (@Composable () -> Unit) -> Unit,
    content: @Composable () -> Unit
) = WindowWidthLayout({ onWide(content) }) { onTall(content) }
