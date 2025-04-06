package com.erdodif.capsulate.utility

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min


fun min(a: PaddingValues, b: PaddingValues): PaddingValues = PaddingValues(
    min(a.calculateLeftPadding(LayoutDirection.Ltr),b.calculateLeftPadding(
        LayoutDirection.Ltr)),
    min(a.calculateTopPadding(),b.calculateTopPadding()),
    min(a.calculateRightPadding(LayoutDirection.Ltr),b.calculateRightPadding(
        LayoutDirection.Ltr)),
    min(a.calculateBottomPadding(),b.calculateBottomPadding()),
)
fun max(a: PaddingValues, b: PaddingValues): PaddingValues = PaddingValues(
    max(a.calculateLeftPadding(LayoutDirection.Ltr),b.calculateLeftPadding(
        LayoutDirection.Ltr)),
    max(a.calculateTopPadding(),b.calculateTopPadding()),
    max(a.calculateRightPadding(LayoutDirection.Ltr),b.calculateRightPadding(
        LayoutDirection.Ltr)),
    max(a.calculateBottomPadding(),b.calculateBottomPadding()),
)
