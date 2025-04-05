package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.io.encoding.ExperimentalEncodingApi

actual val onMobile: Boolean = false
actual val supportedExtensions: List<String>? = listOf("struk", "stk", "txt")

@Composable
actual fun LocateSetting() = Unit

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.toPngByteArray(): ByteArray =
    Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG, 100)!!.bytes
