package com.erdodif.capsulate.utility

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize

fun Modifier.dim(dim: Boolean, amount: Float = 0.3f): Modifier =
    if (dim) this.alpha(amount) else this

fun Modifier.onDpSize(density: Density, onDpSize: (DpSize) -> Unit): Modifier =
    this.onGloballyPositioned {
        with(density) { onDpSize(DpSize(it.size.width.toDp(), it.size.height.toDp())) }
    }
