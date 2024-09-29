package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.core.FileKitPlatformSettings

actual interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()

@Composable
actual fun locateSetting() {}
