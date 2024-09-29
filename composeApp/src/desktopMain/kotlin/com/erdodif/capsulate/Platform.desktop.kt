package com.erdodif.capsulate

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.screen.Screen

@Composable
actual fun resolveColors(): ColorScheme = MaterialTheme.colorScheme
actual interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()