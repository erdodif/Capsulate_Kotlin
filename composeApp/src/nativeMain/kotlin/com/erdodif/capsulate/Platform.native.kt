@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import dev.zwander.kotlin.file.IPlatformFile
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.io.encoding.ExperimentalEncodingApi

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()
actual interface KParcelable
actual interface KParceler<T>

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
actual annotation class KTypeParceler<T, R : KParceler<in T>>
actual class FileParceler : KParceler<IPlatformFile?>

@Composable
actual fun locateSetting() {
}

actual val supportedExtensions: List<String>? = listOf("struk", "stk", "txt")

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.toPngByteArray(): ByteArray =
    Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG, 100)!!.bytes

