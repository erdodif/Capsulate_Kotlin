package com.erdodif.capsulate

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.screen.Screen
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

@Composable
actual fun resolveColors(): ColorScheme = MaterialTheme.colorScheme
actual interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()

@Composable
actual fun locateSetting() {}
actual val onMobile: Boolean = false