package com.erdodif.capsulate

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme

@Composable
actual fun resolveColors(): ColorScheme = dynamicColorScheme(Color(106,56,193), true, false)
actual interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()

@Composable
actual fun locateSetting() {
}

actual val onMobile: Boolean = false