package com.erdodif.capsulate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import io.github.aakira.napier.Napier

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

actual typealias KParcelable = android.os.Parcelable

actual typealias KIgnoredOnParcel = kotlinx.parcelize.IgnoredOnParcel

@Composable
actual fun locateSetting() {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_SHOW_APP_INFO)
    context.startActivity(intent)
}

actual val onMobile: Boolean = true