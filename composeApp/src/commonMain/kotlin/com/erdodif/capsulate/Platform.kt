package com.erdodif.capsulate

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable expect fun resolveColors(): ColorScheme

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
// No `expect` keyword here
annotation class KParcelize()

expect interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
expect annotation class KIgnoredOnParcel()
