@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.ionspin.kotlin.bignum.integer.BigInteger
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.io.encoding.ExperimentalEncodingApi

actual val onMobile: Boolean = false
actual val supportedExtensions: List<String>? = listOf("struk", "stk", "txt")

@Composable
actual fun LocateSetting() = Unit

actual interface KParcelable
actual interface KParceler<T>

@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual annotation class KParcelize
@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
actual annotation class KRawValue
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(value = AnnotationRetention.SOURCE)
@Repeatable
actual annotation class KTypeParceler<T, P: KParceler<in T>>

actual object BigIntParceler: KParceler<BigInteger>

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.toPngByteArray(): ByteArray =
    Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG, 100)!!.bytes
