@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcelable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
actual fun resolveColors(): ColorScheme {
    val context = LocalContext.current
    return when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        else -> MaterialTheme.colorScheme
    }
}

actual typealias KParcelable = Parcelable
actual typealias KIgnoredOnParcel = kotlinx.parcelize.IgnoredOnParcel
actual typealias KParceler<T> = Parceler<T>
actual typealias KTypeParceler<T, R> = TypeParceler<T, R>

@Composable
actual fun LocateSetting() {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_SHOW_APP_INFO)
    context.startActivity(intent)
}

actual val onMobile: Boolean = true

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.toPngByteArray(): ByteArray =
    ByteArrayOutputStream().use {
        this.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        return@use it.toByteArray()
    }

actual val supportedExtensions: List<String>? = null