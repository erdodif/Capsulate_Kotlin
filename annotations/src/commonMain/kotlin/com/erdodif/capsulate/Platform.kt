@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun resolveColors(): ColorScheme

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
// No `expect` keyword here
annotation class KParcelize

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
// No `expect` keyword here
annotation class RawValue

expect interface KParcelable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
expect annotation class KIgnoredOnParcel()

expect interface KParceler<T>
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(value = AnnotationRetention.SOURCE)
@Repeatable
expect annotation class KTypeParceler<T, R: KParceler<in T>>

@Composable
expect fun LocateSetting()

expect val onMobile: Boolean

expect val supportedExtensions: List<String>?

expect fun ImageBitmap.toPngByteArray() : ByteArray